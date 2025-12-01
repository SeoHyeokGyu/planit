package com.planit.entity

import com.planit.enums.FeedType
import jakarta.persistence.*

/**
 * 피드 엔티티
 * 실시간 피드 스트림에 표시되는 이벤트를 저장
 */
@Entity
@Table(
    name = "feeds",
    indexes = [
        Index(name = "idx_feed_user_id", columnList = "user_id"),
        Index(name = "idx_feed_created_at", columnList = "created_at"),
        Index(name = "idx_feed_type", columnList = "type")
    ]
)
class Feed(
    /**
     * 피드를 생성한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    /**
     * 관련된 인증 (인증 타입일 경우)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id", nullable = true)
    val certification: Certification? = null,

    /**
     * 관련된 챌린지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = true)
    val challenge: Challenge? = null,

    /**
     * 피드 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val type: FeedType,

    /**
     * 피드 메시지
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    val message: String,

    /**
     * 읽음 여부
     */
    @Column(nullable = false, name = "is_read")
    var isRead: Boolean = false,

    /**
     * 피드 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

) : BaseEntity() {

    /**
     * 읽음 처리
     */
    fun markAsRead() {
        this.isRead = true
    }
}
