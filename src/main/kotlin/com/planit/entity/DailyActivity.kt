package com.planit.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "daily_activities",
    indexes = [
        Index(name = "idx_daily_login_activity_date", columnList = "login_id, activity_date"),
        Index(name = "idx_daily_activity_date", columnList = "activity_date")
    ]
)
@IdClass(DailyActivityId::class)
class DailyActivity(
    @Id
    @Column(name = "login_id", nullable = false)
    val loginId: String,

    @Id
    @Column(name = "activity_date", nullable = false)
    val activityDate: LocalDate,

    @Column(nullable = false)
    var certificationCount: Int = 0,

    @Column(nullable = false)
    var challengeCount: Int = 0,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) : Serializable {

    /**
     * 인증 추가
     */
    fun addCertification(challengeId: String) {
        certificationCount++
        updatedAt = LocalDateTime.now()
    }

    /**
     * 활동 강도 레벨 계산 (0-4)
     * GitHub 잔디처럼 색상 단계를 위한 레벨
     */
    fun getActivityLevel(): Int {
        return when (certificationCount) {
            0 -> 0
            1 -> 1
            2, 3 -> 2
            4, 5 -> 3
            else -> 4
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DailyActivity) return false
        return loginId == other.loginId && activityDate == other.activityDate
    }

    override fun hashCode(): Int {
        var result = loginId.hashCode()
        result = 31 * result + activityDate.hashCode()
        return result
    }
}

data class DailyActivityId(
    val loginId: String = "",
    val activityDate: LocalDate = LocalDate.now()
) : Serializable