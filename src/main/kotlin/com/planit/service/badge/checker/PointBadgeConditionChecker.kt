package com.planit.service.badge.checker

import com.planit.entity.User
import com.planit.enums.BadgeType
import org.springframework.stereotype.Component

@Component
class PointBadgeConditionChecker : BadgeConditionChecker {

    override val supportedType = BadgeType.POINT_ACCUMULATION

    override fun getCurrentValue(user: User): Long {
        return user.totalPoint
    }
}
