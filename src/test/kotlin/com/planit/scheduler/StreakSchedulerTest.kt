package com.planit.scheduler

import com.planit.dto.NotificationResponse
import com.planit.entity.Challenge
import com.planit.entity.Streak
import com.planit.entity.User
import com.planit.repository.ChallengeRepository
import com.planit.repository.StreakRepository
import com.planit.repository.UserRepository
import com.planit.service.NotificationService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class StreakSchedulerTest {

    @MockK
    private lateinit var streakRepository: StreakRepository

    @MockK
    private lateinit var challengeRepository: ChallengeRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var notificationService: NotificationService

    @InjectMockKs
    private lateinit var streakScheduler: StreakScheduler

    private val loginId = "testuser"
    private val challengeId = "CHL-1"
    private lateinit var user: User
    private lateinit var challenge: Challenge
    private lateinit var streak: Streak

    @BeforeEach
    fun setUp() {
        user = User(loginId = loginId, password = "password", nickname = "Tester")
        val userIdField = User::class.java.getDeclaredField("id")
        userIdField.isAccessible = true
        userIdField.set(user, 1L)

        challenge = Challenge(
            title = "Test Challenge",
            description = "Desc",
            category = "Health",
            startDate = LocalDateTime.now().minusDays(5),
            endDate = LocalDateTime.now().plusDays(5),
            difficulty = "Easy",
            createdId = "admin"
        )
        val chlIdField = Challenge::class.java.getDeclaredField("id")
        chlIdField.isAccessible = true
        chlIdField.set(challenge, challengeId)

        streak = Streak(challengeId, loginId, currentStreak = 5, longestStreak = 10)
    }

    @Test
    @DisplayName("만료된 스트릭 초기화 - 성공")
    fun `resetExpiredStreaks should reset and notify`() {
        // Given
        every { streakRepository.findActiveStreaksNotCertifiedToday(any()) } returns listOf(streak)
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { streakRepository.save(any<Streak>()) } returns streak
        every { userRepository.findByLoginId(loginId) } returns user
        every { notificationService.sendNotification(any<NotificationResponse>()) } just Runs

        // When
        streakScheduler.resetExpiredStreaks()

        // Then
        verify { streakRepository.save(match { it.currentStreak == 0 }) }
        verify { notificationService.sendNotification(any<NotificationResponse>()) }
    }

    @Test
    @DisplayName("스트릭 리마인더 발송 - 성공")
    fun `sendStreakReminders should send notification to at risk users`() {
        // Given
        every { streakRepository.findActiveStreaksNotCertifiedToday(any()) } returns listOf(streak)
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { userRepository.findByLoginId(loginId) } returns user
        every { notificationService.sendNotification(any<NotificationResponse>()) } just Runs

        // When
        streakScheduler.sendStreakReminders()

        // Then
        verify { notificationService.sendNotification(match { it.message.contains("위험해요") }) }
    }

    @Test
    @DisplayName("주간 스트릭 리포트 발송 - 성공")
    fun `sendWeeklyStreakReport should send summary`() {
        // Given
        every { streakRepository.findAll() } returns listOf(streak)
        every { userRepository.findByLoginId(loginId) } returns user
        every { notificationService.sendNotification(any<NotificationResponse>()) } just Runs

        // When
        streakScheduler.sendWeeklyStreakReport()

        // Then
        verify { notificationService.sendNotification(match { it.message.contains("이번 주 활동 리포트") }) }
    }
}
