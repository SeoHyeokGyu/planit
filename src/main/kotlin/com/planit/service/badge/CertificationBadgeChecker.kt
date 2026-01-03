package com.planit.service.badge

import com.planit.repository.CertificationRepository
import com.planit.service.BadgeService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CertificationBadgeChecker(
  private val badgeService: BadgeService,
  private val certificationRepository: CertificationRepository,
) : BadgeChecker {

  /** 인증 관련 배지 획득 조건 체크 인증 글 작성 후 비동기로 호출 권장 */
  @Async
  @Transactional
  override fun checkBadges(userLoginId: String) {
    // 1. 누적 인증 횟수 조회
    val certCount = certificationRepository.countByUser_LoginId(userLoginId)

    // 2. 조건별 배지 지급 시도
    if (certCount >= 1) badgeService.awardBadge(userLoginId, "CERT_1")
    if (certCount >= 10) badgeService.awardBadge(userLoginId, "CERT_10")
    if (certCount >= 50) badgeService.awardBadge(userLoginId, "CERT_50")
    if (certCount >= 100) badgeService.awardBadge(userLoginId, "CERT_100")
  }
}
