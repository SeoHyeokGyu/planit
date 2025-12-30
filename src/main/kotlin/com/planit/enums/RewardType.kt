package com.planit.enums

/**
 * 경험치(EXP)와 포인트(Point) 보상 규칙 정의
 *
 * - 경험치: 레벨업에 사용 (100 EXP = 1 레벨)
 * - 포인트: 상점용 재화 (향후 아이템 구매 등)
 */
enum class RewardType(
    val experience: Long,
    val points: Long,
    val description: String,
) {
    // 인증 관련
    CERTIFICATION(15L, 10L, "챌린지 인증"),

    // 댓글 관련 (향후 구현)
    COMMENT(5L, 2L, "댓글 작성"),

    // 좋아요 관련 (향후 구현)
    LIKE(2L, 1L, "좋아요"),

    // 배지 획득 (향후 구현)
    BADGE(25L, 5L, "배지 획득"),

    // 첫 챌린지 참여
    FIRST_CHALLENGE_JOIN(10L, 5L, "첫 챌린지 참여"),

    // 연속 인증 보너스
    STREAK_BONUS_7(20L, 10L, "7일 연속 인증"),
    STREAK_BONUS_30(50L, 25L, "30일 연속 인증"),
}