package com.planit.service.badge.checker

import com.planit.entity.User
import com.planit.enums.BadgeType
import com.planit.repository.CertificationRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class CertificationStreakChecker(
    private val certificationRepository: CertificationRepository
) : BadgeConditionChecker {

    override val supportedType = BadgeType.CERTIFICATION_STREAK

    override fun check(user: User, requiredValue: Long): Boolean {
        // TODO: 연속 인증 일수 체크 로직 구현 필요
        // 현재는 placeholder로 false 반환
        return false
    }
}
