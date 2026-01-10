package com.planit.service.badge.checker

import com.planit.entity.User
import com.planit.enums.BadgeType
import org.springframework.stereotype.Component

@Component
class CertificationStreakChecker() : BadgeConditionChecker {

  override val supportedType = BadgeType.CERTIFICATION_STREAK

  override fun getCurrentValue(user: User): Long {
    // TODO: 인증 스트릭 계산 로직 구현 필요
    return 0
  }
}
