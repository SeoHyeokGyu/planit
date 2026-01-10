package com.planit.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "streaks",
    indexes = [
        Index(name = "idx_streak_challenge_login", columnList = "challenge_id, login_id"),
        Index(name = "idx_streak_login_id", columnList = "login_id"),
        Index(name = "idx_streak_last_cert_date", columnList = "last_certification_date")
    ]
)
@IdClass(StreakId::class)
class Streak(
    @Id
    @Column(name = "challenge_id", nullable = false)
    val challengeId: String,

    @Id
    @Column(name = "login_id", nullable = false)
    val loginId: String,

    @Column(nullable = false)
    var currentStreak: Int = 0,

    @Column(nullable = false)
    var longestStreak: Int = 0,

    @Column(name = "last_certification_date")
    var lastCertificationDate: LocalDate? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) : Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", insertable = false, updatable = false)
    lateinit var challenge: Challenge

    /**
     * 인증 성공 시 스트릭 증가
     */
    fun incrementStreak(certificationDate: LocalDate = LocalDate.now()) {
        currentStreak++
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }
        lastCertificationDate = certificationDate
        updatedAt = LocalDateTime.now()
    }

    /**
     * 스트릭 초기화 (연속 끊김)
     */
    fun resetStreak() {
        currentStreak = 0
        updatedAt = LocalDateTime.now()
    }

    /**
     * 오늘 인증 여부 확인
     */
    fun isCertifiedToday(): Boolean {
        return lastCertificationDate == LocalDate.now()
    }

    /**
     * 어제 인증 여부 확인 (연속성 검증용)
     */
    fun isCertifiedYesterday(): Boolean {
        return lastCertificationDate == LocalDate.now().minusDays(1)
    }

    /**
     * 스트릭이 끊길 위험이 있는지 확인
     */
    fun isStreakAtRisk(): Boolean {
        if (currentStreak == 0) return false
        return !isCertifiedToday()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Streak) return false
        return challengeId == other.challengeId && loginId == other.loginId
    }

    override fun hashCode(): Int {
        var result = challengeId.hashCode()
        result = 31 * result + loginId.hashCode()
        return result
    }
}

data class StreakId(
    val challengeId: String = "",
    val loginId: String = ""
) : Serializable