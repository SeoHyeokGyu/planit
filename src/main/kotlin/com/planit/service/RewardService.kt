package com.planit.service

import com.planit.enums.RewardType
import com.planit.service.badge.PointBadgeChecker
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 보상(경험치/포인트) 지급 서비스 각 행동에 대해 경험치와 포인트를 자동으로 지급합니다. */
@Service
@Transactional
class RewardService(
  private val userExperienceService: UserExperienceService,
  private val userPointService: UserPointService,
  private val badgeChecker: PointBadgeChecker,
) {

  /**
   * 보상 지급
   *
   * @param userLoginId 사용자 로그인 ID
   * @param rewardType 보상 타입
   */
  fun grantReward(userLoginId: String, rewardType: RewardType) {
    // 경험치 지급
    userExperienceService.addExperience(
      userLoginId = userLoginId,
      experience = rewardType.experience,
      reason = rewardType.description,
    )

    // 포인트 지급
    userPointService.addPoint(
      userLoginId = userLoginId,
      points = rewardType.points,
      reason = rewardType.description,
    )

    // 배지 체크 (비동기)
    badgeChecker.checkBadges(userLoginId)
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
