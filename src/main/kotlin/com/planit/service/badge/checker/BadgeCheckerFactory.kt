package com.planit.service.badge.checker

import com.planit.enums.BadgeType
import org.springframework.stereotype.Component

@Component
class BadgeCheckerFactory(
    checkers: List<BadgeConditionChecker>
) {
    private val checkerMap = checkers.associateBy { it.supportedType }

    fun getChecker(type: BadgeType): BadgeConditionChecker {
        return checkerMap[type] ?: throw IllegalArgumentException("지원하지 않는 배지 타입입니다: $type")
    }
}
