package com.planit.service.badge.checker

import com.planit.entity.User
import com.planit.enums.BadgeType
import com.planit.repository.FollowRepository
import org.springframework.stereotype.Component

@Component
class SocialBadgeConditionChecker(
    private val followRepository: FollowRepository
) : BadgeConditionChecker {

    override val supportedType = BadgeType.FOLLOWER_COUNT

    override fun check(user: User, requiredValue: Long): Boolean {
        val followerCount = followRepository.countByFollowing_LoginId(user.loginId)
        return followerCount >= requiredValue
    }
}
