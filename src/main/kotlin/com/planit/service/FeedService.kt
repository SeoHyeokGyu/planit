package com.planit.service

import com.planit.config.InstanceIdProvider
import com.planit.dto.CertificationResponse
import com.planit.dto.FeedEvent
import com.planit.enums.ParticipantStatusEnum
import com.planit.exception.UserNotFoundException
import com.planit.repository.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.time.Duration

@Service
class FeedService(
  private val emitterRepository: EmitterRepository,
  private val certificationRepository: CertificationRepository,
  private val challengeParticipantRepository: ChallengeParticipantRepository,
  private val challengeRepository: ChallengeRepository,
    private val followRepository: FollowRepository,
  private val instanceIdProvider: InstanceIdProvider,
  private val redisTemplate: RedisTemplate<String, Any>,
  private val userRepository: UserRepository
) {

  private val HEARTBEAT_TIMEOUT = 60L * 60 *  1000 // 60분
  private val USER_INSTANCE_MAP_TTL = Duration.ofMinutes(2)

  companion object {
    const val USER_INSTANCE_KEY_PREFIX = "sse:user-instance:"
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  /**
   * 사용자 loginId를 기반으로 SSE Emitter를 생성하고 구독합니다.
   * 다중 인스턴스 환경을 위해 Redis에 사용자-인스턴스 맵을 저장합니다.
   * @param loginId 사용자의 로그인 ID
   */
  fun subscribe(loginId: String): SseEmitter {
    // 사용자 ID 조회는 필요 없지만, UserNotFoundException 처리를 위해 수행
    userRepository.findByLoginId(loginId) ?: throw UserNotFoundException()

    val emitter = createEmitter(loginId)
    // 초기 연결 시 더미 데이터 전송
    sendToClient(emitter, loginId, "connected", "SSE 연결이 성공적으로 수립되었습니다")
    // Redis에 사용자-인스턴스 정보 저장
    val userInstanceKey = USER_INSTANCE_KEY_PREFIX + loginId
    redisTemplate.opsForValue().set(userInstanceKey, instanceIdProvider.id, USER_INSTANCE_MAP_TTL)
    return emitter
  }

    /**
     * 특정 사용자에게 피드 이벤트를 전송합니다. (로컬 Emitter에만)
     * @param loginId 대상 사용자의 로그인 ID
     * @param feedEvent 전송할 피드 이벤트
     */
    fun send(loginId: String, feedEvent: FeedEvent) {
      emitterRepository.get(loginId)?.let { emitter ->
        try {
          sendToClient(emitter, loginId, feedEvent.type, feedEvent.data)
        } catch (e: IOException) {
          emitterRepository.deleteById(loginId)
          // 연결 종료 시 Redis 맵도 삭제해야 하지만, onCompletion 핸들러가 처리합니다.
        }
      }
    }

  /**
   * 30초마다 모든 클라이언트에게 하트비트를 전송하고,
   * Redis의 사용자-인스턴스 맵 TTL을 갱신합니다.
   */
  @Scheduled(fixedRate = 30 * 1000)
  fun sendHeartbeat() {
    emitterRepository.findAll().forEach { (loginId, emitter) ->
      try {
        // 클라이언트 상태 체크를 위한 이벤트 전송 (ping)
        sendToClient(emitter, loginId, "heartbeat", "ping")
        // Redis 맵 TTL 갱신
        val userInstanceKey = USER_INSTANCE_KEY_PREFIX + loginId
        redisTemplate.expire(userInstanceKey, USER_INSTANCE_MAP_TTL)
      } catch (e: IOException) {
        emitterRepository.deleteById(loginId)
        // onCompletion 핸들러가 Redis 맵 삭제를 처리합니다.
      }
    }
  }

  /**
   * 사용자가 참여 중인 챌린지의 피드를 조회합니다.
   * @param loginId 사용자의 로그인 ID
     * @param pageable 페이징 정보
   * @return 페이징된 인증 응답
     */
  fun getFeedForUser(loginId: String, pageable: Pageable): Page<CertificationResponse> {
    val user = userRepository.findByLoginId(loginId) ?: throw UserNotFoundException()
    val userId = user.id!!

    val participatingChallenges =
      challengeParticipantRepository.findByLoginIdAndStatus(userId, ParticipantStatusEnum.ACTIVE)
    val challengeStringIds = participatingChallenges.map { it.challengeId }
    val challenges = challengeRepository.findByChallengeIdIn(challengeStringIds)
    val challengeIds = challenges.mapNotNull { it.id }

    val certifications = certificationRepository.findByChallenge_IdIn(challengeIds, pageable)

    return certifications.map { CertificationResponse.from(it) }
  }

  /**
   * 사용자가 팔로우하는 사용자들의 피드를 조회합니다.
   * @param loginId 사용자의 로그인 ID
   * @param pageable 페이징 정보
   * @return 페이징된 인증 응답
   */
  fun getFollowingFeed(loginId: String, pageable: Pageable): Page<CertificationResponse> {
    val user = userRepository.findByLoginId(loginId) ?: throw UserNotFoundException()
    val userId = user.id!!

    val followingUsers = followRepository.findAllByFollowerId(userId, Pageable.unpaged()).content
    val followingIds = followingUsers.mapNotNull { it.following.id }
    val certifications = certificationRepository.findByUser_IdIn(followingIds, pageable)
    return certifications.map { CertificationResponse.from(it) }
  }

  private fun createEmitter(loginId: String): SseEmitter {
    val emitter = SseEmitter(HEARTBEAT_TIMEOUT)
    val userInstanceKey = USER_INSTANCE_KEY_PREFIX + loginId

    emitterRepository.save(loginId, emitter)

    // Emitter 완료 또는 타임아웃 시 로컬 및 Redis에서 모두 제거
    val onCompletionOrTimeout = Runnable {
      emitterRepository.deleteById(loginId)
      redisTemplate.delete(userInstanceKey)
    }

    emitter.onCompletion(onCompletionOrTimeout)
    emitter.onTimeout(onCompletionOrTimeout)
    emitter.onError {
      log.error("SSE Emitter 연결 해제 오류: ${it.message}")
      onCompletionOrTimeout.run()
    }

    return emitter
  }

  private fun sendToClient(emitter: SseEmitter, id: String, name: String, data: Any) {
    emitter.send(
      SseEmitter.event()
        .id(id)
        .name(name)
        .data(data)
    )
  }
}