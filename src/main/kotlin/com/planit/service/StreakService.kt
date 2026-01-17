package com.planit.service

import com.planit.dto.*
import com.planit.entity.DailyActivity
import com.planit.entity.Streak
import com.planit.repository.ChallengeRepository
import com.planit.repository.DailyActivityRepository
import com.planit.repository.StreakRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class StreakService(
    private val streakRepository: StreakRepository,
    private val dailyActivityRepository: DailyActivityRepository,
    private val challengeRepository: ChallengeRepository
) {

    /**
     * 인증 성공 시 스트릭 업데이트
     */
    @Transactional
    fun recordCertification(challengeId: String, loginId: String): StreakResponse {
        val today = LocalDate.now()

        // 1. 스트릭 조회 또는 생성
        val streak = streakRepository.findByChallengeIdAndLoginId(challengeId, loginId)
            ?: Streak(challengeId = challengeId, loginId = loginId).also {
                streakRepository.save(it)
            }

        // 2. 오늘 이미 인증했는지 확인
        if (streak.isCertifiedToday()) {
            val challenge = challengeRepository.findById(challengeId)
                .orElseThrow { IllegalArgumentException("Challenge not found") }
            return StreakResponse.from(streak, challenge.title)
        }

        // 3. 스트릭 연속성 확인 및 업데이트
        if (streak.lastCertificationDate == null || streak.isCertifiedYesterday()) {
            // 연속 인증 성공
            streak.incrementStreak(today)
        } else {
            // 스트릭 끊김 - 새로 시작
            streak.resetStreak()
            streak.incrementStreak(today)
        }

        // 4. 일별 활동 기록 업데이트
        updateDailyActivity(loginId, today, challengeId)

        // 5. 저장 및 응답
        streakRepository.save(streak)
        val challenge = challengeRepository.findById(challengeId)
            .orElseThrow { IllegalArgumentException("Challenge not found") }

        return StreakResponse.from(streak, challenge.title)
    }

    /**
     * 특정 챌린지의 스트릭 조회
     */
    @Transactional(readOnly = true)
    fun getStreak(challengeId: String, loginId: String): StreakResponse {
        val streak = streakRepository.findByChallengeIdAndLoginId(challengeId, loginId)
            ?: throw IllegalArgumentException("Streak not found")

        val challenge = challengeRepository.findById(challengeId)
            .orElseThrow { IllegalArgumentException("Challenge not found") }

        return StreakResponse.from(streak, challenge.title)
    }

    /**
     * 사용자의 모든 스트릭 조회
     */
    @Transactional(readOnly = true)
    fun getAllStreaks(loginId: String): StreakSummaryResponse {
        val streaks = streakRepository.findAllByLoginId(loginId)
        val statistics = streakRepository.getStreakStatistics(loginId)

        val streakResponses = streaks.map { streak ->
            val challenge = challengeRepository.findById(streak.challengeId)
                .orElseThrow { IllegalArgumentException("Challenge not found") }
            StreakResponse.from(streak, challenge.title)
        }

        return StreakSummaryResponse(
            loginId = loginId,
            totalCurrentStreak = (statistics["totalCurrentStreak"] as Number).toInt(),
            maxLongestStreak = (statistics["maxLongestStreak"] as Number).toInt(),
            activeStreakCount = (statistics["activeStreakCount"] as Number).toInt(),
            streaks = streakResponses
        )
    }

    /**
     * 잔디 캘린더 데이터 조회 (연도별 - 1년치)
     */
    @Transactional(readOnly = true)
    fun getActivityCalendar(loginId: String, year: Int?): ActivityCalendarResponse {
        val targetYear = year ?: LocalDate.now().year

        // 해당 연도의 1월 1일부터 12월 31일까지 (또는 현재 날짜까지)
        val startDate = LocalDate.of(targetYear, 1, 1)
        val today = LocalDate.now()
        val endDate = if (targetYear == today.year) today else LocalDate.of(targetYear, 12, 31)

        val activities = dailyActivityRepository.findActivitiesByDateRange(loginId, startDate, endDate)

        // 모든 날짜에 대해 데이터 생성 (활동 없는 날은 0으로)
        val activityMap = activities.associateBy { it.activityDate }
        val allDates = generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it <= endDate }
            .toList()

        val dailyActivities = allDates.map { date ->
            val activity = activityMap[date]
            DailyActivityResponse(
                date = date,
                certificationCount = activity?.certificationCount ?: 0,
                challengeCount = activity?.challengeCount ?: 0,
                activityLevel = activity?.getActivityLevel() ?: 0
            )
        }

        val totalCertifications = activities.sumOf { it.certificationCount }
        val activeDays = activities.count { it.certificationCount > 0 }
        val totalDays = allDates.size

        return ActivityCalendarResponse(
            loginId = loginId,
            startDate = startDate,
            endDate = endDate,
            totalDays = totalDays,
            activeDays = activeDays,
            totalCertifications = totalCertifications,
            activities = dailyActivities
        )
    }

    /**
     * 스트릭 통계 조회 (일별/주별/월별)
     */
    @Transactional(readOnly = true)
    fun getStreakStatistics(loginId: String, period: String = "daily", days: Int = 30): StreakStatisticsResponse {
        val endDate = LocalDate.now()
        val startDate = when (period) {
            "weekly" -> endDate.minusWeeks(4)
            "monthly" -> endDate.minusMonths(6)
            else -> endDate.minusDays(days.toLong())
        }

        return when (period) {
            "daily" -> getDailyStatistics(loginId, startDate, endDate)
            "weekly" -> getWeeklyStatistics(loginId, startDate, endDate)
            "monthly" -> getMonthlyStatistics(loginId, startDate, endDate)
            else -> throw IllegalArgumentException("Invalid period: $period")
        }
    }

    /**
     * 챌린지별 스트릭 리더보드
     */
    @Transactional(readOnly = true)
    fun getStreakLeaderboard(challengeId: String, limit: Int = 10): StreakLeaderboardResponse {
        val challenge = challengeRepository.findById(challengeId)
            .orElseThrow { IllegalArgumentException("Challenge not found") }

        val topStreaks = streakRepository.findTopStreaksByChallengeId(challengeId)
            .take(limit)

        val leaders = topStreaks.mapIndexed { index, streak ->
            LeaderboardEntry(
                rank = index + 1,
                loginId = streak.loginId,
                userName = null, // User 엔티티 조인 필요 시 추가
                currentStreak = streak.currentStreak,
                longestStreak = streak.longestStreak
            )
        }

        return StreakLeaderboardResponse(
            challengeId = challengeId,
            challengeTitle = challenge.title,
            leaders = leaders
        )
    }

    // ========== Private Helper Methods ==========

    private fun updateDailyActivity(loginId: String, date: LocalDate, challengeId: String) {
        val activity = dailyActivityRepository.findByLoginIdAndActivityDate(loginId, date)
            ?: DailyActivity(loginId = loginId, activityDate = date).also {
                dailyActivityRepository.save(it)
            }

        activity.addCertification(challengeId)
        dailyActivityRepository.save(activity)
    }

    private fun getDailyStatistics(loginId: String, startDate: LocalDate, endDate: LocalDate): StreakStatisticsResponse {
        val statistics = dailyActivityRepository.getDailyStatistics(loginId, startDate, endDate)

        val items = statistics.map { stat ->
            StatisticItem(
                label = (stat["date"] as LocalDate).toString(),
                certificationCount = (stat["totalCertifications"] as Number).toInt(),
                challengeCount = (stat["activeChallenges"] as Number).toInt(),
                activeDay = (stat["totalCertifications"] as Number).toLong() > 0
            )
        }

        return StreakStatisticsResponse(
            loginId = loginId,
            period = "daily",
            statistics = items
        )
    }

    private fun getWeeklyStatistics(loginId: String, startDate: LocalDate, endDate: LocalDate): StreakStatisticsResponse {
        // 주별 집계 로직 구현
        val activities = dailyActivityRepository.findActivitiesByDateRange(loginId, startDate, endDate)

        val weeklyMap = activities.groupBy {
            it.activityDate.minusDays((it.activityDate.dayOfWeek.value % 7).toLong())
        }

        val items = weeklyMap.map { (weekStart, weekActivities) ->
            StatisticItem(
                label = "Week of ${weekStart}",
                certificationCount = weekActivities.sumOf { it.certificationCount },
                challengeCount = weekActivities.distinctBy { it.challengeCount }.size,
                activeDay = weekActivities.any { it.certificationCount > 0 }
            )
        }.sortedBy { it.label }

        return StreakStatisticsResponse(
            loginId = loginId,
            period = "weekly",
            statistics = items
        )
    }

    private fun getMonthlyStatistics(loginId: String, startDate: LocalDate, endDate: LocalDate): StreakStatisticsResponse {
        val statistics = dailyActivityRepository.getMonthlyStatistics(loginId, startDate, endDate)

        val items = statistics.map { stat ->
            val year = (stat["year"] as Int)
            val month = (stat["month"] as Int)
            StatisticItem(
                label = "${year}-${month.toString().padStart(2, '0')}",
                certificationCount = (stat["totalCertifications"] as Number).toInt(),
                challengeCount = 0, // 월별은 챌린지 수 집계 생략 가능
                activeDay = (stat["activeDays"] as Number).toLong() > 0
            )
        }

        return StreakStatisticsResponse(
            loginId = loginId,
            period = "monthly",
            statistics = items
        )
    }
}