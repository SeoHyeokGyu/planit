package com.planit.service.badge

import com.planit.repository.FollowRepository
import com.planit.service.BadgeService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SocialBadgeChecker(
    private val badgeService: BadgeService,
    private val followRepository: FollowRepository
) {

    /**
     * 팔로워 관련 배지 획득 조건 체크
     * 누군가 나를 팔로우했을 때 비동기로 호출
     */
    @Async
    @Transactional
    fun checkFollowerBadges(userLoginId: String) {
        // 1. 내 팔로워 수 조회
        val followerCount = followRepository.countByFollowing_LoginId(userLoginId)

        // 2. 조건별 배지 지급 시도
        if (followerCount >= 1) badgeService.awardBadge(userLoginId, "SOCIAL_1")
        if (followerCount >= 10) badgeService.awardBadge(userLoginId, "SOCIAL_10")
        if (followerCount >= 100) badgeService.awardBadge(userLoginId, "SOCIAL_100")
    }
}
