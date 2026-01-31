package com.planit.scheduler

import com.planit.dto.NotificationResponse
import com.planit.entity.Challenge
import com.planit.entity.ChallengeParticipant
import com.planit.entity.User
import com.planit.enums.ParticipantStatusEnum
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.ChallengeRepository
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

@ExtendWith(MockKExtension::class)
class ChallengeReminderSchedulerTest {

    @MockK
    private lateinit var challengeRepository: ChallengeRepository

    @MockK
    private lateinit var participantRepository: ChallengeParticipantRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var notificationService: NotificationService

    @InjectMockKs
    private lateinit var scheduler: ChallengeReminderScheduler

    private val creatorId = "creator"
    private val participantId = "participant"
    private val challengeId = "CHL-1"
    private lateinit var challenge: Challenge
    private lateinit var creator: User
    private lateinit var participant: User

    @BeforeEach
    fun setUp() {
        challenge = Challenge(
            title = "Test Chl", description = "D", category = "C", difficulty = "E",
            startDate = LocalDateTime.now(), endDate = LocalDateTime.now(), createdId = creatorId
        )
        val chlIdField = Challenge::class.java.getDeclaredField("id")
        chlIdField.isAccessible = true
        chlIdField.set(challenge, challengeId)

        creator = User(creatorId, "p", "Creator")
        val cIdField = User::class.java.getDeclaredField("id")
        cIdField.isAccessible = true
        cIdField.set(creator, 1L)

        participant = User(participantId, "p", "Participant")
        val pIdField = User::class.java.getDeclaredField("id")
        pIdField.isAccessible = true
        pIdField.set(participant, 2L)
    }

    @Test
    @DisplayName("종료 3일 전 리마인더 발송 - 성공")
    fun `sendThreeDaysBeforeReminder should send notifications`() {
        // Given
        every { challengeRepository.findByEndDateOn(any()) } returns listOf(challenge)
        every { participantRepository.findByIdAndStatus(challengeId, ParticipantStatusEnum.ACTIVE) } returns listOf(
            ChallengeParticipant(id = challengeId, loginId = participantId)
        )
        every { userRepository.findByLoginId(creatorId) } returns creator
        every { userRepository.findByLoginId(participantId) } returns participant
        every { notificationService.sendNotification(any()) } just Runs

        // When
        scheduler.sendThreeDaysBeforeReminder()

        // Then
        verify(exactly = 2) { notificationService.sendNotification(any()) }
    }

    @Test
    @DisplayName("리마인더 발송 - 사용자를 찾을 수 없는 경우 무시")
    fun `sendReminder should skip missing users`() {
        // Given
        every { challengeRepository.findByEndDateOn(any()) } returns listOf(challenge)
        every { participantRepository.findByIdAndStatus(challengeId, ParticipantStatusEnum.ACTIVE) } returns emptyList()
        every { userRepository.findByLoginId(creatorId) } returns null // User not found

        // When
        scheduler.sendEndDayReminder()

        // Then
        verify(exactly = 0) { notificationService.sendNotification(any()) }
    }

    @Test
    @DisplayName("종료 1주일 전 리마인더 발송")
    fun `sendOneWeekBeforeReminder coverage`() {
        // Given
        every { challengeRepository.findByEndDateOn(any()) } returns listOf(challenge)
        every { participantRepository.findByIdAndStatus(any(), any()) } returns emptyList()
        every { userRepository.findByLoginId(any()) } returns creator
        every { notificationService.sendNotification(any()) } just Runs

        // When
        scheduler.sendOneWeekBeforeReminder()

        // Then
        verify { notificationService.sendNotification(any()) }
    }
}
