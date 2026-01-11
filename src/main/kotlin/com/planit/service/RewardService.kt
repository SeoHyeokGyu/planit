package com.planit.service

import com.planit.enums.BadgeType
import com.planit.enums.RewardType
import com.planit.exception.UserNotFoundException
import com.planit.repository.UserRepository
import com.planit.service.badge.BadgeService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 보상(포인트) 지급 서비스 각 행동에 대해 포인트를 자동으로 지급합니다. */
@Service
@Transactional
class RewardService(
  private val userPointService: UserPointService,
  private val badgeService: BadgeService,
  private val userRepository: UserRepository
) {

  /**
   * 보상 지급
   *
   * @param userLoginId 사용자 로그인 ID
   * @param rewardType 보상 타입
   */
  fun grantReward(userLoginId: String, rewardType: RewardType) {
    // 포인트 지급
    userPointService.addPoint(
      userLoginId = userLoginId,
      points = rewardType.points,
      reason = rewardType.description,
    )

    // 배지 체크
    val user = userRepository.findByLoginId(userLoginId) ?: throw UserNotFoundException()
    badgeService.checkAndAwardBadges(user, BadgeType.POINT_ACCUMULATION)
  }

  /** 인증 보상 지급 */
  fun grantCertificationReward(userLoginId: String) {
    grantReward(userLoginId, RewardType.CERTIFICATION)
  }

  /** 댓글 보상 지급 (향후 구현) */
  fun grantCommentReward(userLoginId: String) {
    grantReward(userLoginId, RewardType.COMMENT)
  }

  /** 좋아요 보상 지급 (향후 구현) */
  fun grantLikeReward(userLoginId: String) {
    grantReward(userLoginId, RewardType.LIKE)
  }

  /** 배지 획득 보상 지급 (향후 구현) */
  fun grantBadgeReward(userLoginId: String) {
    grantReward(userLoginId, RewardType.BADGE)
  }
}
