package com.planit.service.badge

import com.planit.repository.UserRepository
import com.planit.service.BadgeService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PointBadgeChecker(
  private val badgeService: BadgeService,
  private val userRepository: UserRepository,
) : BadgeChecker {

  /** 포인트 관련 배지 획득 조건 체크 포인트 획득 시 비동기로 호출 */
  @Async
  @Transactional
  override fun checkBadges(userLoginId: String) {
    // 1. 내 현재 포인트 조회 (누적 포인트 필드가 있다면 그것을 사용하는 것이 더 정확함)
    // 현재는 보유 포인트(point)를 기준으로 체크
    val user = userRepository.findByLoginId(userLoginId) ?: return
    val currentPoint = user.totalPoint

    // 2. 조건별 배지 지급 시도
    if (currentPoint >= 100) badgeService.awardBadge(userLoginId, "POINT_100")
    if (currentPoint >= 1000) badgeService.awardBadge(userLoginId, "POINT_1000")
  }
}
