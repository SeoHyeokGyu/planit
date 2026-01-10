package com.planit.service.badge.checker

import com.planit.entity.User
import com.planit.enums.BadgeType
import com.planit.repository.CertificationRepository
import org.springframework.stereotype.Component

@Component
class CertificationCountChecker(
    private val certificationRepository: CertificationRepository
) : BadgeConditionChecker {

    override val supportedType = BadgeType.CERTIFICATION_COUNT

    override fun getCurrentValue(user: User): Long {
        return certificationRepository.countByUserId(user.id!!)
    }
}
