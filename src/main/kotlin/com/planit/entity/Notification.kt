package com.planit.entity

import com.planit.enums.NotificationType
import jakarta.persistence.*

/**
 * 알림(Notification) 엔티티
 * 사용자에게 발생한 이벤트(팔로우, 댓글, 좋아요, 배지, 레벨업 등)에 대한 알림을 저장합니다.
 */
@Entity
@Table(
    name = "notifications",
    indexes = [
        Index(name = "idx_receiver_id", columnList = "receiver_id"),
        Index(name = "idx_receiver_is_read", columnList = "receiver_id,is_read"),
        Index(name = "idx_receiver_type", columnList = "receiver_id,notification_type"),
        Index(name = "idx_created_at", columnList = "created_at")
    ]
)
class Notification(
    /**
     * 알림을 받는 사용자 (수신자)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: User,

    /**
     * 알림을 발생시킨 사용자 (발신자, null 가능 - 시스템 알림의 경우)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = true)
    val sender: User?,

    /**
     * 알림 타입 (FOLLOW, COMMENT, LIKE, BADGE, LEVEL_UP)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    val type: NotificationType,

    /**
     * 알림 메시지 (API에서 직접 전달됨, 템플릿 기반 아님)
     */
    @Column(nullable = false, length = 500)
    val message: String,

    /**
     * 관련 리소스 ID (챌린지 ID, 인증 ID 등, 선택적)
     */
    @Column(name = "related_id", nullable = true)
    val relatedId: String? = null,

    /**
     * 관련 리소스 타입 (CHALLENGE, CERTIFICATION, USER 등, 선택적)
     */
    @Column(name = "related_type", nullable = true, length = 50)
    val relatedType: String? = null,

    /**
     * 읽음 여부
     */
    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    /**
     * 알림을 읽음 상태로 표시합니다.
     */
    fun markAsRead() {
        this.isRead = true
    }
}
