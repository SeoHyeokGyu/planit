package com.planit.entity

import com.planit.enum.ParticipantStatusEnum
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

    @Column(nullable = false)
    val challengeId: String,

    @Column(nullable = false)
    val loginId: Long,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: ParticipantStatusEnum = ParticipantStatusEnum.ACTIVE,

    @Column(nullable = false)
    var certificationCnt: Int = 0,

    @Column(nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var completedAt: LocalDateTime? = null,

    @Column
    var withdrawnAt: LocalDateTime? = null
) {
    fun complete() {
        status = ParticipantStatusEnum.COMPLETED
        completedAt = LocalDateTime.now()
    }

    fun withdraw() {
        status = ParticipantStatusEnum.WITHDRAWN
        withdrawnAt = LocalDateTime.now()
    }

    fun isActive(): Boolean {
        return status == ParticipantStatusEnum.ACTIVE
    }
}

