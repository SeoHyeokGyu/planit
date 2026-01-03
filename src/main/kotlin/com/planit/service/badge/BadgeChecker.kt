package com.planit.service.badge

/** 배지 획득 조건을 체크하는 공통 인터페이스입니다. */
interface BadgeChecker {
  /**
   * 특정 사용자의 배지 획득 조건을 체크하고 조건을 만족하면 배지를 지급합니다.
   *
   * @param userLoginId 사용자 로그인 ID
   */
  fun checkBadges(userLoginId: String)
}
