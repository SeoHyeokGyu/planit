package com.planit.enums

/**
 * 포인트(Point) 보상 규칙 정의
 *
 * - 포인트: 모든 활동에 대한 보상 (상점용 재화)
 */
enum class RewardType(
    val points: Long,
    val description: String,
) {
    // 인증 관련
    CERTIFICATION(10L, "챌린지 인증"),

    // 댓글 관련 (향후 구현)
    COMMENT(2L, "댓글 작성"),

    // 좋아요 관련 (향후 구현)
    LIKE(1L, "좋아요"),

    // 배지 획득 (향후 구현)
    BADGE(5L, "배지 획득"),

    // 첫 챌린지 참여
    FIRST_CHALLENGE_JOIN(5L, "첫 챌린지 참여"),

    // 연속 인증 보너스
    STREAK_BONUS_7(10L, "7일 연속 인증"),
    STREAK_BONUS_30(25L, "30일 연속 인증"),
}