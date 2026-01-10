package com.planit.dto

import com.planit.entity.Streak
import java.time.LocalDate

/**
 * 개별 챌린지 스트릭 응답 DTO
 */
data class StreakResponse(
    val challengeId: String,
    val challengeTitle: String,
    val loginId: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCertificationDate: LocalDate?,
    val isCertifiedToday: Boolean,
    val isStreakAtRisk: Boolean
) {
    companion object {
        fun from(streak: Streak, challengeTitle: String): StreakResponse {
            return StreakResponse(
                challengeId = streak.challengeId,
                challengeTitle = challengeTitle,
                loginId = streak.loginId,
                currentStreak = streak.currentStreak,
                longestStreak = streak.longestStreak,
                lastCertificationDate = streak.lastCertificationDate,
                isCertifiedToday = streak.isCertifiedToday(),
                isStreakAtRisk = streak.isStreakAtRisk()
            )
        }
    }
}

/**
 * 사용자 전체 스트릭 요약 DTO
 */
data class StreakSummaryResponse(
    val loginId: String,
    val totalCurrentStreak: Int,
    val maxLongestStreak: Int,
    val activeStreakCount: Int,
    val streaks: List<StreakResponse>
)

/**
 * GitHub 잔디 스타일 일별 활동 DTO
 */
data class DailyActivityResponse(
    val date: LocalDate,
    val certificationCount: Int,
    val challengeCount: Int,
    val activityLevel: Int  // 0-4 (색상 단계)
)

/**
 * 잔디 캘린더 응답 DTO (최근 30일)
 */
data class ActivityCalendarResponse(
    val loginId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalDays: Int,
    val activeDays: Int,
    val totalCertifications: Int,
    val activities: List<DailyActivityResponse>
)

/**
 * 스트릭 통계 DTO
 */
data class StreakStatisticsResponse(
    val loginId: String,
    val period: String, // "daily", "weekly", "monthly"
    val statistics: List<StatisticItem>
)

data class StatisticItem(
    val label: String,  // 날짜 또는 기간
    val certificationCount: Int,
    val challengeCount: Int,
    val activeDay: Boolean
)

/**
 * 스트릭 리더보드 DTO
 */
data class StreakLeaderboardResponse(
    val challengeId: String,
    val challengeTitle: String,
    val leaders: List<LeaderboardEntry>
)

data class LeaderboardEntry(
    val rank: Int,
    val loginId: String,
    val userName: String?,
    val currentStreak: Int,
    val longestStreak: Int
)

/**
 * 스트릭 경고 알림 DTO
 */
data class StreakWarningNotification(
    val loginId: String,
    val challengeId: String,
    val challengeTitle: String,
    val currentStreak: Int,
    val lastCertificationDate: LocalDate?,
    val message: String
)
