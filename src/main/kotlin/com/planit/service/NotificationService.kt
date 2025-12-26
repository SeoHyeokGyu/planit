package com.planit.service

import com.planit.config.RedisConfig
import com.planit.dto.NotificationCreateRequest
import com.planit.dto.NotificationDto
import com.planit.dto.NotificationResponse
import com.planit.dto.UnreadCountResponse
import com.planit.entity.Notification
import com.planit.enums.NotificationType
import com.planit.exception.NotificationAccessForbiddenException
import com.planit.exception.NotificationNotFoundException
import com.planit.exception.UserNotFoundException
import com.planit.repository.NotificationRepository
import com.planit.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository? = null,
    private val userRepository: UserRepository? = null,
    private val cacheManager: CacheManager? = null,
    private val redisTemplate: RedisTemplate<String, Any>? = null,
) {

  private val logger = LoggerFactory.getLogger(NotificationService::class.java)
  // 사용자 ID (loginId)를 키로 사용하여 SseEmitter 관리
  private val emitters = ConcurrentHashMap<String, SseEmitter>()

  // ==================== SSE 기반 실시간 알림 ====================

  /** 클라이언트가 SSE 구독을 요청할 때 호출됩니다. */
  fun subscribe(userLoginId: String): SseEmitter {
    val emitter = SseEmitter(Long.MAX_VALUE)
    emitters[userLoginId] = emitter

    logger.info("SSE 구독 시작: $userLoginId")

    emitter.onCompletion {
      logger.info("SSE 구독 완료: $userLoginId")
      emitters.remove(userLoginId)
    }
    emitter.onTimeout {
      logger.info("SSE 구독 타임아웃: $userLoginId")
      emitters.remove(userLoginId)
    }
    emitter.onError {
      logger.error("SSE 구독 에러: $userLoginId", it)
      emitters.remove(userLoginId)
    }

    try {
      emitter.send(SseEmitter.event().name("connect").data("연결 성공!"))
    } catch (e: IOException) {
      emitters.remove(userLoginId)
    }

    return emitter
  }

  /** 특정 사용자에게 알림을 전송합니다. (Redis Pub/Sub을 통한 브로드캐스트) */
  fun sendNotification(notification: NotificationResponse) {
    if (redisTemplate != null) {
      try {
        logger.info("Redis 알림 발행 시도: $notification")
        // Redis 채널로 알림 발행 (모든 서버 인스턴스가 수신)
        redisTemplate.convertAndSend(RedisConfig.NOTIFICATION_CHANNEL, notification)
        logger.info("Redis 알림 발행 완료: ${notification.receiverLoginId}")
      } catch (e: Exception) {
        logger.error("Redis 알림 발행 실패", e)
      }
    } else {
      // RedisTemplate이 없는 경우 (테스트 환경 등) 로컬에서 직접 시도
      sendToLocalEmitter(notification.receiverLoginId, notification)
    }
  }

  /** 로컬 서버에 연결된 Emitter가 있으면 알림을 전송합니다. (Subscriber에 의해 호출됨) */
  fun sendToLocalEmitter(userLoginId: String, notification: Any) {
    val emitter = emitters[userLoginId]
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().name("notification").data(notification))
        logger.info("로컬 알림 전송 완료: $userLoginId")
      } catch (e: IOException) {
        logger.error("로컬 알림 전송 실패: $userLoginId", e)
        emitters.remove(userLoginId)
      }
    }
  }

  // ==================== DB 기반 알림 관리 ====================

  /** 새로운 알림을 생성하고 DB에 저장합니다. */
  @Transactional
  fun createNotification(request: NotificationCreateRequest) {
    if (notificationRepository == null || userRepository == null) return

    val receiver =
        userRepository.findByLoginId(request.receiverLoginId)
            ?: throw UserNotFoundException("수신자를 찾을 수 없습니다: ${request.receiverLoginId}")

    val sender =
        request.senderLoginId?.let {
          userRepository.findByLoginId(it) ?: throw UserNotFoundException("발신자를 찾을 수 없습니다: $it")
        }

    val notification =
        Notification(
            receiver = receiver,
            sender = sender,
            type = request.type,
            message = request.message,
            relatedId = request.relatedId,
            relatedType = request.relatedType,
        )

    notificationRepository.save(notification)

    // 읽지 않은 알림 개수 캐시 증가
    incrementUnreadCountCache(receiver.id!!)

    // Redis Pub/Sub을 통한 실시간 알림 브로드캐스트
    sendNotification(NotificationResponse.from(notification))
  }

  /**
   * 특정 사용자에게 알림을 전송합니다. (SSE 실시간 + DB 저장)
   *
   * @deprecated 이 메서드 대신 NotificationResponse를 인자로 받는 sendNotification을 사용하세요.
   */
  fun sendNotification(userLoginId: String, notification: NotificationDto) {
    // 하위 호환성을 위해 유지하되, Redis 브로드캐스트 방식을 권장
    val emitter = emitters[userLoginId]
    if (emitter != null) {
      sendToLocalEmitter(userLoginId, notification)
    }
  }

  /** 특정 사용자의 알림 목록을 조회합니다. */
  @Transactional(readOnly = true)
  fun getNotifications(
      userLoginId: String,
      isRead: Boolean? = null,
      type: NotificationType? = null,
      pageable: Pageable,
  ): Page<NotificationResponse> {
    if (notificationRepository == null || userRepository == null) {
      return Page.empty()
    }

    val user =
        userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")

    val notificationPage =
        when {
          isRead != null && type != null -> {
            notificationRepository.findAllByReceiverIdAndIsReadAndTypeOrderByCreatedAtDesc(
                user.id!!,
                isRead,
                type,
                pageable,
            )
          }
          isRead != null -> {
            notificationRepository.findAllByReceiverIdAndIsReadOrderByCreatedAtDesc(
                user.id!!,
                isRead,
                pageable,
            )
          }
          type != null -> {
            notificationRepository.findAllByReceiverIdAndTypeOrderByCreatedAtDesc(
                user.id!!,
                type,
                pageable,
            )
          }
          else -> {
            notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(user.id!!, pageable)
          }
        }

    return notificationPage.map { NotificationResponse.from(it) }
  }

  /** 특정 사용자의 읽지 않은 알림 개수를 조회합니다. */
  @Transactional(readOnly = true)
  fun getUnreadCount(userLoginId: String): UnreadCountResponse {
    if (notificationRepository == null || userRepository == null || cacheManager == null) {
      return UnreadCountResponse(0L)
    }

    val user =
        userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")

    val cache = cacheManager.getCache("unreadNotificationCount")
    val cachedCount = cache?.get(user.id.toString(), Long::class.java)
    if (cachedCount != null) {
      return UnreadCountResponse(cachedCount)
    }

    val dbCount = notificationRepository.countByReceiverIdAndIsRead(user.id!!, false)
    cache?.put(user.id.toString(), dbCount)
    return UnreadCountResponse(dbCount)
  }

  /** 특정 알림을 읽음 상태로 표시합니다. */
  @Transactional
  fun markAsRead(notificationId: Long, userLoginId: String) {
    if (notificationRepository == null || userRepository == null) return

    val user =
        userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")

    val notification =
        notificationRepository.findById(notificationId).orElseThrow {
          NotificationNotFoundException("알림을 찾을 수 없습니다: $notificationId")
        }

    if (notification.receiver.id != user.id) {
      throw NotificationAccessForbiddenException("이 알림에 접근할 권한이 없습니다")
    }

    if (!notification.isRead) {
      notification.markAsRead()
      notificationRepository.save(notification)
      decrementUnreadCountCache(user.id!!)
    }
  }

  /** 특정 사용자의 모든 읽지 않은 알림을 읽음 상태로 일괄 표시합니다. */
  @Transactional
  fun markAllAsRead(userLoginId: String): Int {
    if (notificationRepository == null || userRepository == null || cacheManager == null) return 0

    val user =
        userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")

    val updatedCount = notificationRepository.markAllAsReadByReceiverId(user.id!!)

    val cache = cacheManager.getCache("unreadNotificationCount")
    cache?.put(user.id.toString(), 0L)

    return updatedCount
  }

  /** 특정 알림을 삭제합니다. */
  @Transactional
  fun deleteNotification(notificationId: Long, userLoginId: String) {
    if (notificationRepository == null || userRepository == null) return

    val user =
        userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")

    val notification =
        notificationRepository.findById(notificationId).orElseThrow {
          NotificationNotFoundException("알림을 찾을 수 없습니다: $notificationId")
        }

    if (notification.receiver.id != user.id) {
      throw NotificationAccessForbiddenException("이 알림을 삭제할 권한이 없습니다")
    }

    if (!notification.isRead) {
      decrementUnreadCountCache(user.id!!)
    }

    notificationRepository.delete(notification)
  }

  /** 특정 사용자의 모든 읽은 알림을 일괄 삭제합니다. */
  @Transactional
  fun deleteAllRead(userLoginId: String): Int {
    if (notificationRepository == null || userRepository == null) return 0

    val user =
        userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")

    val deletedCount = notificationRepository.deleteAllReadByReceiverId(user.id!!)
    return deletedCount
  }

  // ==================== 캐시 유틸리티 ====================

  private fun incrementUnreadCountCache(userId: Long) {
    if (cacheManager == null) return
    val cache = cacheManager.getCache("unreadNotificationCount")
    val currentValue = cache?.get(userId.toString(), Long::class.java)
    if (currentValue != null) {
      cache.put(userId.toString(), currentValue + 1)
    }
  }

  private fun decrementUnreadCountCache(userId: Long) {
    if (cacheManager == null) return
    val cache = cacheManager.getCache("unreadNotificationCount")
    val currentValue = cache?.get(userId.toString(), Long::class.java)
    if (currentValue != null && currentValue > 0) {
      cache.put(userId.toString(), currentValue - 1)
    }
  }
}
