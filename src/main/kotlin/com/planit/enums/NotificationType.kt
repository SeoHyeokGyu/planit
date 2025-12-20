package com.planit.enums

/**
 * 알림 타입을 정의하는 Enum 클래스
 */
enum class NotificationType {
    FOLLOW,      // 팔로우 알림
    COMMENT,     // 댓글 알림
    LIKE,        // 좋아요 알림
    BADGE,       // 배지 획득 알림
    LEVEL_UP     // 레벨업 알림
}
