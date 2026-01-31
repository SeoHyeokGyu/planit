package com.planit.service

import com.planit.entity.Challenge
import com.planit.entity.DailyActivity
import com.planit.entity.Streak
import com.planit.repository.ChallengeRepository
import com.planit.repository.DailyActivityRepository
import com.planit.repository.StreakRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class StreakServiceTest {

    @MockK
    private lateinit var streakRepository: StreakRepository

    @MockK
    private lateinit var dailyActivityRepository: DailyActivityRepository

    @MockK
    private lateinit var challengeRepository: ChallengeRepository

    @InjectMockKs
    private lateinit var streakService: StreakService

    private val loginId = "testuser"
    private val challengeId = "CHL-12345678"
    private lateinit var challenge: Challenge

    @BeforeEach
    fun setUp() {
        challenge = Challenge(
            title = "Test Challenge",
            description = "Description",
            category = "Health",
            startDate = LocalDateTime.now().minusDays(10),
            endDate = LocalDateTime.now().plusDays(10),
            difficulty = "Easy",
            createdId = "admin"
        )
        // Reflection is used since id has a private setter and is generated automatically
        val field = Challenge::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(challenge, challengeId)
    }

    @Test
    @DisplayName("인증 기록 - 신규 스트릭 생성 및 업데이트 성공")
    fun `recordCertification should create new streak and update`() {
        // Given
        val today = LocalDate.now()
        every { streakRepository.findByChallengeIdAndLoginId(challengeId, loginId) } returns null
        every { streakRepository.save(any<Streak>()) } returnsArgument 0
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { dailyActivityRepository.findByLoginIdAndActivityDate(loginId, today) } returns null
        every { dailyActivityRepository.save(any<DailyActivity>()) } returnsArgument 0

        // When
        val result = streakService.recordCertification(challengeId, loginId)

        // Then
        assertNotNull(result)
        assertEquals(1, result.currentStreak)
        assertEquals(challenge.title, result.challengeTitle)
        
        verify { streakRepository.save(any<Streak>()) }
        verify { dailyActivityRepository.save(any<DailyActivity>()) }
    }

    @Test
    @DisplayName("인증 기록 - 기존 스트릭 연속 인증 성공")
    fun `recordCertification should increment existing streak`() {
        // Given
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val streak = Streak(challengeId, loginId, currentStreak = 5, longestStreak = 5, lastCertificationDate = yesterday)
        
        every { streakRepository.findByChallengeIdAndLoginId(challengeId, loginId) } returns streak
        every { streakRepository.save(any<Streak>()) } returns streak
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { dailyActivityRepository.findByLoginIdAndActivityDate(loginId, today) } returns null
        every { dailyActivityRepository.save(any<DailyActivity>()) } returnsArgument 0

        // When
        val result = streakService.recordCertification(challengeId, loginId)

        // Then
        assertEquals(6, result.currentStreak)
        verify { streakRepository.save(streak) }
    }

    @Test
    @DisplayName("인증 기록 - 오늘 이미 인증한 경우 기존 상태 반환")
    fun `recordCertification should return current state if already certified today`() {
        // Given
        val today = LocalDate.now()
        val streak = Streak(challengeId, loginId, currentStreak = 5, longestStreak = 5, lastCertificationDate = today)
        
        every { streakRepository.findByChallengeIdAndLoginId(challengeId, loginId) } returns streak
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)

        // When
        val result = streakService.recordCertification(challengeId, loginId)

        // Then
        assertEquals(5, result.currentStreak)
        verify(exactly = 0) { streakRepository.save(any<Streak>()) }
    }

    @Test
    @DisplayName("모든 스트릭 조회 - 성공")
    fun `getAllStreaks should return summary`() {
        // Given
        val streak = Streak(challengeId, loginId, currentStreak = 3, longestStreak = 5)
        every { streakRepository.findAllByLoginId(loginId) } returns listOf(streak)
        every { streakRepository.getStreakStatistics(loginId) } returns mapOf(
            "totalCurrentStreak" to 3,
            "maxLongestStreak" to 5,
            "activeStreakCount" to 1
        )
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)

        // When
        val result = streakService.getAllStreaks(loginId)

        // Then
        assertNotNull(result)
        assertEquals(3, result.totalCurrentStreak)
        assertEquals(1, result.streaks.size)
        assertEquals(challenge.title, result.streaks[0].challengeTitle)
    }

    @Test
    @DisplayName("활동 캘린더 조회 - 성공")
    fun `getActivityCalendar should return calendar data`() {
        // Given
        val year = LocalDate.now().year
        val activity = DailyActivity(loginId, LocalDate.now(), certificationCount = 2)
        every { dailyActivityRepository.findActivitiesByDateRange(loginId, any(), any()) } returns listOf(activity)

        // When
        val result = streakService.getActivityCalendar(loginId, year)

        // Then
        assertNotNull(result)
        assertEquals(2, result.totalCertifications)
        // Find today's activity in result
        val todayActivity = result.activities.find { it.date == LocalDate.now() }
        assertNotNull(todayActivity)
        assertEquals(2, todayActivity?.certificationCount)
        assertEquals(2, todayActivity?.activityLevel)
    }

    @Test
    @DisplayName("특정 스트릭 조회 - 성공")
    fun `getStreak should return streak details`() {
        // Given
        val streak = Streak(challengeId, loginId, currentStreak = 10, longestStreak = 15)
        every { streakRepository.findByChallengeIdAndLoginId(challengeId, loginId) } returns streak
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)

        // When
        val result = streakService.getStreak(challengeId, loginId)

        // Then
        assertNotNull(result)
        assertEquals(10, result.currentStreak)
        assertEquals(15, result.longestStreak)
        assertEquals(challenge.title, result.challengeTitle)
    }

    @Test
    @DisplayName("스트릭 통계 조회 - 일별")
    fun `getStreakStatistics should return daily stats`() {
        // Given
        val today = LocalDate.now()
        val dailyStats = mapOf(
            "date" to today,
            "totalCertifications" to 5,
            "activeChallenges" to 3
        )
        every { dailyActivityRepository.getDailyStatistics(any(), any(), any()) } returns listOf(dailyStats)

        // When
        val result = streakService.getStreakStatistics(loginId, "daily")

        // Then
        assertEquals("daily", result.period)
        assertEquals(1, result.statistics.size)
        assertEquals(5, result.statistics[0].certificationCount)
    }

    @Test
    @DisplayName("스트릭 리더보드 조회 - 성공")
    fun `getStreakLeaderboard should return top streaks`() {
        // Given
        val streak1 = Streak(challengeId, "user1", currentStreak = 10)
        val streak2 = Streak(challengeId, "user2", currentStreak = 8)
        
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { streakRepository.findTopStreaksByChallengeId(challengeId) } returns listOf(streak1, streak2)

        // When
        val result = streakService.getStreakLeaderboard(challengeId)

        // Then
        assertEquals(challengeId, result.challengeId)
        assertEquals(2, result.leaders.size)
        assertEquals("user1", result.leaders[0].loginId)
        assertEquals(1, result.leaders[0].rank)
        assertEquals("user2", result.leaders[1].loginId)
        assertEquals(2, result.leaders[1].rank)
    }
}
