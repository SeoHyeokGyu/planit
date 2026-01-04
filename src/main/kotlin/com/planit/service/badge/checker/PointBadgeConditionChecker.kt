package com.planit.service.badge.checker

import com.planit.entity.User
import com.planit.enums.BadgeType
import org.springframework.stereotype.Component

@Component
class PointBadgeConditionChecker : BadgeConditionChecker {

    override val supportedType = BadgeType.POINT_ACCUMULATION

    override fun check(user: User, requiredValue: Long): Boolean {
        // user.totalPoint가 있다고 가정 (기존 PointBadgeChecker 참조)
        // 기존 코드: val currentPoint = user.totalPoint
        // User 엔티티를 확인하여 정확한 필드명을 써야 함. 
        // 일단 totalPoint로 작성 후 컴파일 에러 시 User 확인.
        return user.totalPoint >= requiredValue
    }
}
