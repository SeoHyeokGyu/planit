package com.planit.service

import com.planit.dto.BadgeResponse
import com.planit.entity.UserBadge
import com.planit.exception.UserNotFoundException
import com.planit.repository.BadgeRepository
import com.planit.repository.UserBadgeRepository
import com.planit.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BadgeService(
  private val badgeRepository: BadgeRepository,
  private val userBadgeRepository: UserBadgeRepository,
  private val userRepository: UserRepository,
  private val notificationService: NotificationService,
) {
  private val logger = LoggerFactory.getLogger(BadgeService::class.java)

  /**
   * 배지 지급 (조건 만족 시 호출)
   *
   * @return 배지를 새로 획득했으면 true, 이미 가지고 있으면 false
   */
  @Transactional
  fun awardBadge(userLoginId: String, badgeCode: String): Boolean {
    val user = userRepository.findByLoginId(userLoginId) ?: throw UserNotFoundException()
    val badge = badgeRepository.findByCode(badgeCode) ?: return false

    // 이미 획득했는지 확인
    if (userBadgeRepository.existsByUserIdAndBadgeCode(user.id!!, badgeCode)) {
      return false
    }

    // 배지 지급
    val userBadge = UserBadge(user = user, badge = badge)
    userBadgeRepository.save(userBadge)

    // 알림 발송
    try {
      notificationService.createNotification(
        com.planit.dto.NotificationCreateRequest(
          receiverLoginId = userLoginId,
          type = com.planit.enums.NotificationType.BADGE,
          message = "새로운 배지를 획득했습니다: ${badge.name}",
          relatedId = badge.code,
          relatedType = "BADGE",
        )
      )
    } catch (e: Exception) {
      // 알림 발송 실패가 배지 지급 트랜잭션을 롤백시키지 않도록 로깅만 함
      // (혹은 필요에 따라 롤백시킬 수도 있지만, 알림은 부가 기능이므로 보통 무시)
      logger.error("배지 알림 발송 실패", e)
    }

    return true
  }

  /** 전체 배지 목록 조회 (획득 여부 포함) */
  fun getAllBadges(userLoginId: String?): List<BadgeResponse> {
    val allBadges = badgeRepository.findAll()

    // 로그인하지 않은 경우 획득 여부 모두 false
    if (userLoginId == null) {
      return allBadges.map { BadgeResponse.from(it) }
    }

    // 사용자가 획득한 배지 정보를 Map으로 변환 (Key: Badge Code)
    val acquiredBadges =
      userBadgeRepository.findByUserLoginId(userLoginId).associateBy { it.badge.code }

    return allBadges.map { badge ->
      val userBadge = acquiredBadges[badge.code]
      BadgeResponse.from(
        badge = badge,
        isAcquired = userBadge != null,
        acquiredAt = userBadge?.acquiredAt,
      )
    }
  }

  /** 내가 획득한 배지 목록 조회 */
  fun getMyBadges(userLoginId: String): List<BadgeResponse> {
    return userBadgeRepository.findByUserLoginId(userLoginId).map { userBadge ->
      BadgeResponse.from(
        badge = userBadge.badge,
        isAcquired = true,
        acquiredAt = userBadge.acquiredAt,
      )
    }
  }
}
