package com.planit.service.badge.checker

import com.planit.entity.User
import com.planit.enums.BadgeType

/** 배지 획득 조건을 검사하는 전략 인터페이스 */
interface BadgeConditionChecker {
  /** 이 체커가 처리할 수 있는 배지 타입 */
  val supportedType: BadgeType

  /**
   * 배지 획득 조건을 만족하는지 검사
   *
   * @param user 대상 사용자
   * @param requiredValue 배지 획득에 필요한 기준값 (예: 10회, 3일 등)
   * @return 획득 조건을 만족하면 true
   */
  fun check(user: User, requiredValue: Long): Boolean = getCurrentValue(user) >= requiredValue

  /**
   * 현재 사용자의 진행 상황(값)을 반환
   *
   * @param user 대상 사용자
   * @return 현재 값 (예: 현재 팔로워 수, 현재 포인트 등)
   */
  fun getCurrentValue(user: User): Long
}
