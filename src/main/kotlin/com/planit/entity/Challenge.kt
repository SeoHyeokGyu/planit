package com.planit.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "challenges",
    indexes = [
        Index(name = "idx_category", columnList = "category"),
        Index(name = "idx_difficulty", columnList = "difficulty"),
        Index(name = "idx_start_date", columnList = "startDate"),
        Index(name = "idx_end_date", columnList = "endDate")
    ]
)
class Challenge(
    title: String,
    description: String,
    category: String,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    difficulty: String,
    createdId: String,
    viewCnt: Long? = 0,
    participantCnt: Long? = 0,
    certificationCnt: Long? = 0
) {
    @Id
    @Column(nullable = false, unique = true)
    var challengeId: String = generateChallengeId()
        private set

    @Column(nullable = false)
    var title = title

    @Column(nullable = false, length = 1000)
    var description = description

    @Column(nullable = false)
    var category = category

    @Column(nullable = false)
    var startDate = startDate

    @Column(nullable = false)
    var endDate = endDate

    @Column(nullable = false)
    var difficulty = difficulty

    @Column(nullable = false)
    var createdId = createdId

    @Column(nullable = false)
    var viewCnt = viewCnt ?: 0L

    @Column(nullable = false)
    var participantCnt = participantCnt ?: 0L

    @Column(nullable = false)
    var certificationCnt = certificationCnt ?: 0L

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    fun isActive(): Boolean {
        val now = LocalDateTime.now()
        return startDate.isBefore(now) && endDate.isAfter(now)
    }

    fun isEnded(): Boolean {
        return endDate.isBefore(LocalDateTime.now())
    }

    fun isUpcoming(): Boolean {
        return startDate.isAfter(LocalDateTime.now())
    }

    companion object {
        private fun generateChallengeId(): String {
            return "CHL-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        }
    }
}