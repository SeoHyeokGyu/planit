package com.planit.entity

import com.planit.enums.ParticipantStatusEnum
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Table(
    name = "challenge_participants",
    indexes = [
        Index(name = "idx_login_id", columnList = "login_id"),
        Index(name = "idx_challenge_id", columnList = "challenge_id"),
        Index(name = "idx_status", columnList = "status")
    ]
)
class ChallengeParticipant(
    @Id
    @Column(name = "challenge_id", nullable = false)
    val challengeId: String,

    @Id
    @Column(name = "login_id", nullable = false)
    val loginId: String,

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
) : Serializable {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChallengeParticipant) return false
        return challengeId == other.challengeId && loginId == other.loginId
    }

    override fun hashCode(): Int {
        var result = challengeId.hashCode()
        result = 31 * result + loginId.hashCode()
        return result
    }
}