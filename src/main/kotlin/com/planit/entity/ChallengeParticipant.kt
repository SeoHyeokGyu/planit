package com.planit.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "challenge_participants",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["challenge_id", "user_id"])
    ],
    indexes = [
        Index(name = "idx_user_id", columnList = "user_id"),
        Index(name = "idx_challenge_id", columnList = "challenge_id"),
        Index(name = "idx_status", columnList = "status")
    ]
)
data class ChallengeParticipant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    val challenge: Challenge,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: ParticipantStatus = ParticipantStatus.ACTIVE,

    @Column(nullable = false)
    var certificationCount: Int = 0,

    @Column(nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var completedAt: LocalDateTime? = null,

    @Column
    var withdrawnAt: LocalDateTime? = null
) {
    fun complete() {
        status = ParticipantStatus.COMPLETED
        completedAt = LocalDateTime.now()
    }

    fun withdraw() {
        status = ParticipantStatus.WITHDRAWN
        withdrawnAt = LocalDateTime.now()
    }

    fun isActive(): Boolean {
        return status == ParticipantStatus.ACTIVE
    }
}

enum class ParticipantStatus {
    ACTIVE,      // 참여중
    COMPLETED,   // 완료
    WITHDRAWN    // 탈퇴
}
