package com.planit.service.badge

import com.planit.dto.BadgeResponse
import com.planit.dto.NotificationResponse
import com.planit.entity.Badge
import com.planit.entity.User
import com.planit.entity.UserBadge
import com.planit.enums.BadgeType
import com.planit.enums.NotificationType
import com.planit.exception.UserNotFoundException
import com.planit.repository.BadgeRepository
import com.planit.repository.UserBadgeRepository
import com.planit.repository.UserRepository
import com.planit.service.NotificationService
import com.planit.service.badge.checker.BadgeCheckerFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class BadgeService(
  private val badgeRepository: BadgeRepository,
  private val userBadgeRepository: UserBadgeRepository,
  private val userRepository: UserRepository,
  private val notificationService: NotificationService,
  private val badgeCheckerFactory: BadgeCheckerFactory,
) {
  private val logger = LoggerFactory.getLogger(BadgeService::class.java)

  /**
   * 특정 타입의 배지 획득 조건을 일괄 검사하고 지급
   *
   * @param user 대상 사용자
   * @param type 검사할 배지 타입 (예: CERTIFICATION_COUNT)
   * @return 새로 획득한 배지 개수
   */
  @Transactional
  fun checkAndAwardBadges(user: User, type: BadgeType): Int {
    val badges = badgeRepository.findAllByType(type)
    if (badges.isEmpty()) return 0

    val checker = badgeCheckerFactory.getChecker(type)
    var newBadgesCount = 0

    badges.forEach { badge ->
      // 이미 획득한 배지는 건너뜀
      if (userBadgeRepository.existsByUserIdAndBadgeCode(user.id!!, badge.code)) {
        return@forEach
      }

      // 조건 충족 시 지급
      if (checker.check(user, badge.requiredValue)) {
        giveBadge(user, badge)
        newBadgesCount++
      }
    }
    return newBadgesCount
  }

  /**
   * 사용자의 모든 배지 획득 조건을 검사하고 지급 (수동 트리거용)
   *
   * @param userLoginId 사용자 로그인 ID
   * @return 새로 획득한 배지 개수
   */
  @Transactional
  fun checkAllBadges(userLoginId: String): Int {
    val user = userRepository.findByLoginId(userLoginId) ?: throw UserNotFoundException()
    var totalNewBadges = 0

    BadgeType.entries.forEach { type ->
      totalNewBadges += checkAndAwardBadges(user, type)
    }

    return totalNewBadges
  }

  /**
   * 배지 지급 (조건 만족 시 호출 - 레거시 지원용)
   *
   * @return 배지를 새로 획득했으면 true, 이미 가지고 있으면 false
   */
  @Transactional
  fun awardBadge(userLoginId: String, badgeCode: String): Boolean {
    val user = userRepository.findByLoginId(userLoginId) ?: throw UserNotFoundException()
    val badge = badgeRepository.findByCode(badgeCode) ?: return false

    if (userBadgeRepository.existsByUserIdAndBadgeCode(user.id!!, badgeCode)) {
      return false
    }

    giveBadge(user, badge)
    return true
  }

  /** 실제 배지 지급 및 알림 발송 로직 */
  private fun giveBadge(user: User, badge: Badge) {
    val userBadge = UserBadge(user = user, badge = badge)
    userBadgeRepository.save(userBadge)

    logger.info("Badge awarded to user ${user.loginId}: ${badge.name} (${badge.code})")

    notificationService.sendNotification(
      NotificationResponse(
        id = java.util.UUID.randomUUID().toString(),
        receiverId = user.id!!,
        receiverLoginId = user.loginId,
        senderId = null,
        senderLoginId = null,
        senderNickname = null,
        type = NotificationType.BADGE,
        message = "새로운 배지를 획득했습니다: ${badge.name}",
        relatedId = badge.id.toString(),
        relatedType = "BADGE",
        isRead = false,
        createdAt = LocalDateTime.now(),
      )
    )
  }

  /** 전체 배지 목록 조회 (획득 여부 포함) */
  fun getAllBadges(userLoginId: String?): List<BadgeResponse> {
    val allBadges = badgeRepository.findAll()

    // 로그인하지 않은 경우 획득 여부 모두 false
    if (userLoginId == null) {
      return allBadges.map { BadgeResponse.from(it) }
    }

    val user = userRepository.findByLoginId(userLoginId) ?: throw UserNotFoundException()

    // 사용자가 획득한 배지 정보를 Map으로 변환 (Key: Badge Code)
    val acquiredBadges =
      userBadgeRepository.findByUserLoginId(userLoginId).associateBy { it.badge.code }

    // 배지 타입별 현재 값 캐싱 (중복 DB 조회 방지)
    val typeValues = mutableMapOf<BadgeType, Long>()

    return allBadges.map { badge ->
      val userBadge = acquiredBadges[badge.code]

      val currentValue =
        typeValues.computeIfAbsent(badge.type) { type ->
          val checker = badgeCheckerFactory.getChecker(type)
          checker.getCurrentValue(user)
        }

      BadgeResponse.from(
        badge = badge,
        isAcquired = userBadge != null,
        acquiredAt = userBadge?.acquiredAt,
        currentValue = currentValue,
      )
    }
  }

  /** 내가 획득한 배지 목록 조회 */
  fun getMyBadges(userLoginId: String): List<BadgeResponse> {
    val user = userRepository.findByLoginId(userLoginId) ?: throw UserNotFoundException()
    val userBadges = userBadgeRepository.findByUserLoginId(userLoginId)

    // 배지 타입별 현재 값 캐싱
    val typeValues = mutableMapOf<BadgeType, Long>()

    return userBadges.map { userBadge ->
      val currentValue =
        typeValues.computeIfAbsent(userBadge.badge.type) { type ->
          val checker = badgeCheckerFactory.getChecker(type)
          checker.getCurrentValue(user)
        }

      BadgeResponse.from(
        badge = userBadge.badge,
        isAcquired = true,
        acquiredAt = userBadge.acquiredAt,
        currentValue = currentValue,
      )
    }
  }
}
