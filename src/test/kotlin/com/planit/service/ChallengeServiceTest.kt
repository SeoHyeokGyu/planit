package com.planit.service

import com.planit.dto.ChallengeRequest
import com.planit.dto.ChallengeSearchRequest
import com.planit.entity.Challenge
import com.planit.entity.ChallengeParticipant
import com.planit.enum.ParticipantStatusEnum
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.ChallengeRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class ChallengeServiceTest {
    @MockK
    private lateinit var challengeRepository: ChallengeRepository

    @MockK
    private lateinit var participantRepository: ChallengeParticipantRepository

    @MockK
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @MockK
    private lateinit var valueOperations: ValueOperations<String, String>

    @InjectMockKs
    private lateinit var challengeService: ChallengeService

    private lateinit var challenge: Challenge
    private lateinit var challengeRequest: ChallengeRequest
    private lateinit var participant: ChallengeParticipant

    @BeforeEach
    fun setUp() {
        challengeRequest = ChallengeRequest(
            title = "30일 운동 챌린지",
            description = "매일 30분씩 운동하기",
            category = "EXERCISE",
            difficulty = "NORMAL",
            loginId = "user123",
            startDate = LocalDateTime.of(2024, 1, 1, 0, 0),
            endDate = LocalDateTime.of(2024, 1, 31, 23, 59)
        )

        challenge = Challenge(
            title = "30일 운동 챌린지",
            description = "매일 30분씩 운동하기",
            category = "EXERCISE",
            difficulty = "NORMAL",
            startDate = LocalDateTime.of(2024, 1, 1, 0, 0),
            endDate = LocalDateTime.of(2024, 1, 31, 23, 59),
            createdId = "user123",
            viewCnt = 0,
            participantCnt = 0,
            certificationCnt = 0
        )

        participant = ChallengeParticipant(
            challengeId = "CHL-12345678",
            loginId = 1L
        )

        every { redisTemplate.opsForValue() } returns valueOperations
    }

    @Test
    @DisplayName("챌린지 생성 성공")
    fun `createChallenge should succeed with valid request`() {
        // Given
        every { challengeRepository.save(any()) } returns challenge

        // When
        val result = challengeService.createChallenge(challengeRequest, 1L)

        // Then
        assertNotNull(result)
        assertEquals("30일 운동 챌린지", result.title)
        assertEquals("EXERCISE", result.category)
        verify(exactly = 1) { challengeRepository.save(any()) }
    }

    @Test
    @DisplayName("챌린지 상세 조회 성공")
    fun `getChallengeById should succeed when challenge exists`() {
        // Given
        every { challengeRepository.findById(1L) } returns Optional.of(challenge)

        // When
        val result = challengeService.getChallengeById(1L)

        // Then
        assertNotNull(result)
        assertEquals("30일 운동 챌린지", result.title)
        verify(exactly = 1) { challengeRepository.findById(1L) }
    }

    @Test
    @DisplayName("챌린지 상세 조회 실패 - 존재하지 않는 챌린지")
    fun `getChallengeById should throw NoSuchElementException when challenge not found`() {
        // Given
        every { challengeRepository.findById(999L) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<NoSuchElementException> {
            challengeService.getChallengeById(999L)
        }
        assertEquals("챌린지를 찾을 수 없습니다: 999", exception.message)
        verify(exactly = 1) { challengeRepository.findById(999L) }
    }

    @Test
    @DisplayName("챌린지 목록 조회 성공 - 전체 조회")
    fun `getChallenges should return all challenges when no filter`() {
        // Given
        val searchRequest = ChallengeSearchRequest()
        val challenges = listOf(challenge)
        every { challengeRepository.findAll() } returns challenges

        // When
        val result = challengeService.getChallenges(searchRequest)

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("30일 운동 챌린지", result[0].title)
        verify(exactly = 1) { challengeRepository.findAll() }
    }

    @Test
    @DisplayName("챌린지 목록 조회 성공 - 카테고리 필터")
    fun `getChallenges should return filtered challenges by category`() {
        // Given
        val searchRequest = ChallengeSearchRequest(category = "EXERCISE")
        val challenges = listOf(challenge)
        every { challengeRepository.findByCategory("EXERCISE") } returns challenges

        // When
        val result = challengeService.getChallenges(searchRequest)

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("EXERCISE", result[0].category)
        verify(exactly = 1) { challengeRepository.findByCategory("EXERCISE") }
    }

    @Test
    @DisplayName("챌린지 검색 성공")
    fun `searchChallenges should return matched challenges`() {
        // Given
        val challenges = listOf(challenge)
        every { challengeRepository.findByTitleContainingOrDescriptionContaining("운동", "운동") } returns challenges

        // When
        val result = challengeService.searchChallenges("운동")

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertTrue(result[0].title.contains("운동"))
        verify(exactly = 1) { challengeRepository.findByTitleContainingOrDescriptionContaining("운동", "운동") }
    }

    @Test
    @DisplayName("챌린지 수정 성공")
    fun `updateChallenge should succeed with valid request and permission`() {
        // Given
        val updateRequest = ChallengeRequest(
            title = "수정된 챌린지",
            description = "수정된 설명",
            category = "HEALTH",
            difficulty = "HARD",
            loginId = "user123",
            startDate = LocalDateTime.of(2024, 2, 1, 0, 0),
            endDate = LocalDateTime.of(2024, 2, 28, 23, 59)
        )

        val updatedChallenge = Challenge(
            title = "수정된 챌린지",
            description = "수정된 설명",
            category = "HEALTH",
            difficulty = "HARD",
            startDate = LocalDateTime.of(2024, 2, 1, 0, 0),
            endDate = LocalDateTime.of(2024, 2, 28, 23, 59),
            createdId = "123",
            viewCnt = 0,
            participantCnt = 0,
            certificationCnt = 0
        )

        every { challengeRepository.findById(1L) } returns Optional.of(challenge)
        every { challengeRepository.save(any()) } returns updatedChallenge

        // When
        val result = challengeService.updateChallenge(1L, updateRequest, 123L)

        // Then
        assertNotNull(result)
        assertEquals("수정된 챌린지", result.title)
        verify(exactly = 1) { challengeRepository.findById(1L) }
        verify(exactly = 1) { challengeRepository.save(any()) }
    }

    @Test
    @DisplayName("챌린지 수정 실패 - 권한 없음")
    fun `updateChallenge should throw IllegalArgumentException without permission`() {
        // Given
        every { challengeRepository.findById(1L) } returns Optional.of(challenge)

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            challengeService.updateChallenge(1L, challengeRequest, 999L)
        }
        assertEquals("챌린지를 수정할 권한이 없습니다", exception.message)
        verify(exactly = 1) { challengeRepository.findById(1L) }
        verify(exactly = 0) { challengeRepository.save(any()) }
    }

    @Test
    @DisplayName("챌린지 삭제 성공")
    fun `deleteChallenge should succeed with valid permission`() {
        // Given
        every { challengeRepository.findById(1L) } returns Optional.of(challenge)
        every { challengeRepository.delete(any()) } just runs

        // When
        challengeService.deleteChallenge(1L, 123L)

        // Then
        verify(exactly = 1) { challengeRepository.findById(1L) }
        verify(exactly = 1) { challengeRepository.delete(any()) }
    }

    @Test
    @DisplayName("챌린지 삭제 실패 - 권한 없음")
    fun `deleteChallenge should throw IllegalArgumentException without permission`() {
        // Given
        every { challengeRepository.findById(1L) } returns Optional.of(challenge)

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            challengeService.deleteChallenge(1L, 999L)
        }
        assertEquals("챌린지를 삭제할 권한이 없습니다", exception.message)
        verify(exactly = 1) { challengeRepository.findById(1L) }
        verify(exactly = 0) { challengeRepository.delete(any()) }
    }

    @Test
    @DisplayName("챌린지 참여 성공")
    fun `joinChallenge should succeed when not already participating`() {
        // Given
        every { challengeRepository.findById(1L) } returns Optional.of(challenge)
        every { participantRepository.existsByChallengeIdAndLoginId(any(), any()) } returns false
        every { participantRepository.save(any()) } returns participant
        every { challengeRepository.incrementParticipantCount(1L) } just runs

        // When
        val result = challengeService.joinChallenge(1L, 1L)

        // Then
        assertNotNull(result)
        assertEquals(1L, result.userId)
        assertEquals(ParticipantStatusEnum.ACTIVE, result.status)
        verify(exactly = 1) { participantRepository.existsByChallengeIdAndLoginId(any(), any()) }
        verify(exactly = 1) { participantRepository.save(any()) }
        verify(exactly = 1) { challengeRepository.incrementParticipantCount(1L) }
    }

    @Test
    @DisplayName("챌린지 참여 실패 - 이미 참여중")
    fun `joinChallenge should throw IllegalStateException when already participating`() {
        // Given
        every { challengeRepository.findById(1L) } returns Optional.of(challenge)
        every { participantRepository.existsByChallengeIdAndLoginId(any(), any()) } returns true

        // When & Then
        val exception = assertThrows<IllegalStateException> {
            challengeService.joinChallenge(1L, 1L)
        }
        assertEquals("이미 참여중인 챌린지입니다", exception.message)
        verify(exactly = 1) { participantRepository.existsByChallengeIdAndLoginId(any(), any()) }
        verify(exactly = 0) { participantRepository.save(any()) }
    }

    @Test
    @DisplayName("챌린지 탈퇴 성공")
    fun `withdrawChallenge should succeed when actively participating`() {
        // Given
        every { challengeRepository.findById(1L) } returns Optional.of(challenge)
        every { participantRepository.findByChallengeIdAndLoginId(any(), any()) } returns Optional.of(participant)
        every { participantRepository.save(any()) } returns participant
        every { challengeRepository.decrementParticipantCount(1L) } just runs

        // When
        challengeService.withdrawChallenge(1L, 1L)

        // Then
        assertEquals(ParticipantStatusEnum.WITHDRAWN, participant.status)
        assertNotNull(participant.withdrawnAt)
        verify(exactly = 1) { participantRepository.findByChallengeIdAndLoginId(any(), any()) }
        verify(exactly = 1) { participantRepository.save(any()) }
        verify(exactly = 1) { challengeRepository.decrementParticipantCount(1L) }
    }

    @Test
    @DisplayName("챌린지 탈퇴 실패 - 참여 정보 없음")
    fun `withdrawChallenge should throw NoSuchElementException when not participating`() {
        // Given
        every { challengeRepository.findById(1L) } returns Optional.of(challenge)
        every { participantRepository.findByChallengeIdAndLoginId(any(), any()) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<NoSuchElementException> {
            challengeService.withdrawChallenge(1L, 1L)
        }
        assertEquals("참여 정보를 찾을 수 없습니다", exception.message)
        verify(exactly = 1) { participantRepository.findByChallengeIdAndLoginId(any(), any()) }
        verify(exactly = 0) { participantRepository.save(any()) }
    }

    @Test
    @DisplayName("조회수 증가 성공")
    fun `incrementViewCount should increment Redis counter`() {
        // Given
        every { valueOperations.increment(any()) } returns 5L
        every { redisTemplate.expire(any(), any(), any()) } returns true

        // When
        challengeService.incrementViewCount(1L)

        // Then
        verify(exactly = 1) { valueOperations.increment(any()) }
        verify(exactly = 1) { redisTemplate.expire(any(), any(), any()) }
    }

    @Test
    @DisplayName("조회수 DB 동기화 성공")
    fun `syncViewCountToDatabase should update challenge view count`() {
        // Given
        every { challengeRepository.findById(1L) } returns Optional.of(challenge)
        every { challengeRepository.save(any()) } returns challenge

        // When
        challengeService.syncViewCountToDatabase(1L, 100L)

        // Then
        assertEquals(100L, challenge.viewCnt)
        verify(exactly = 1) { challengeRepository.findById(1L) }
        verify(exactly = 1) { challengeRepository.save(any()) }
    }

    @Test
    @DisplayName("참여자 목록 조회 성공")
    fun `getParticipants should return list of participants`() {
        // Given
        val participants = listOf(participant)
        every { challengeRepository.findById(1L) } returns Optional.of(challenge)
        every { participantRepository.findByChallengeId(any()) } returns participants

        // When
        val result = challengeService.getParticipants(1L)

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(1L, result[0].userId)
        verify(exactly = 1) { participantRepository.findByChallengeId(any()) }
    }

    @Test
    @DisplayName("챌린지 통계 조회 성공")
    fun `getChallengeStatistics should return statistics`() {
        // Given
        every { challengeRepository.findById(1L) } returns Optional.of(challenge)
        every { participantRepository.countByChallengeId(any()) } returns 100L
        every { participantRepository.countByChallengeIdAndStatus(any(), ParticipantStatusEnum.ACTIVE) } returns 80L
        every { participantRepository.countByChallengeIdAndStatus(any(), ParticipantStatusEnum.COMPLETED) } returns 15L
        every { participantRepository.countByChallengeIdAndStatus(any(), ParticipantStatusEnum.WITHDRAWN) } returns 5L
        every { participantRepository.sumCertificationCountByChallengeId(any()) } returns 500L
        every { valueOperations.get(any()) } returns "1500"

        // When
        val result = challengeService.getChallengeStatistics(1L)

        // Then
        assertNotNull(result)
        assertEquals(1L, result.challengeId)
        assertEquals(100, result.totalParticipants)
        assertEquals(80, result.activeParticipants)
        assertEquals(15, result.completedParticipants)
        assertEquals(5, result.withdrawnParticipants)
        assertEquals(500L, result.totalCertifications)
        assertEquals(15.0, result.completionRate)
        assertEquals(5.0, result.averageCertificationPerParticipant)
        assertEquals(1500L, result.viewCount)
    }
}