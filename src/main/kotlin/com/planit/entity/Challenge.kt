package com.planit.entity

import jakarta.persistence.*
import java.time.LocalDate
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
    startDate: LocalDate,
    endDate: LocalDate,
    difficulty: String,
    createdId: String,
    viewCnt: Long? = 0,
    participantCnt: Long? = 0,
    certificationCnt: Long? = 0
) {
    @Id
    @Column(nullable = false, unique = true)
    var id: String = generateId()
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
        val today = LocalDate.now()
        return !startDate.isAfter(today) && !endDate.isBefore(today)
    }

    fun isEnded(): Boolean {
        return endDate.isBefore(LocalDate.now())
    }

    fun isUpcoming(): Boolean {
        return startDate.isAfter(LocalDate.now())
    }

    companion object {
        private fun generateId(): String {
            return "CHL-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        }
    }

    override fun toString(): String {
        return "Challenge(id='$id', title='$title', category='$category', " +
                "difficulty='$difficulty', startDate=$startDate, endDate=$endDate, " +
                "createdId='$createdId', viewCnt=$viewCnt, participantCnt=$participantCnt, " +
                "certificationCnt=$certificationCnt, createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}