package com.planit.enums

/** 배지 등급 */
enum class BadgeGrade {
  BRONZE,
  SILVER,
  GOLD,
  PLATINUM,
}

/** 배지 획득 조건 타입 */
enum class BadgeType {
  CERTIFICATION_COUNT, // 누적 인증 횟수
  CONTINUOUS_CERTIFICATION, // 연속 인증 일수
  CERTIFICATION_STREAK, // 연속 인증 스트릭
  FOLLOWER_COUNT, // 팔로워 수
  POINT_ACCUMULATION, // 누적 포인트
}
