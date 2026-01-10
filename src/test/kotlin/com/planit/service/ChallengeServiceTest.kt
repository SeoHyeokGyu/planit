package com.planit.service

import com.planit.dto.ChallengeRequest
import com.planit.dto.ChallengeSearchRequest
import com.planit.entity.Challenge
import com.planit.entity.ChallengeParticipant
import com.planit.entity.User
import com.planit.enums.ParticipantStatusEnum
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

import com.planit.repository.LikeRepository
import com.planit.repository.UserRepository

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
    
    @MockK
    private lateinit var likeRepository: LikeRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var notificationService: NotificationService

    @InjectMockKs
    private lateinit var challengeService: ChallengeService

    private lateinit var challenge: Challenge
    private lateinit var challengeRequest: ChallengeRequest
    private lateinit var participant: ChallengeParticipant

    val now = LocalDateTime.now()
    val challengeId = "CHL-12345678"
    val loginId = "user123"

    @BeforeEach
    fun setUp() {

        challengeRequest = ChallengeRequest(
            title = "30일 운동 챌린지",
            description = "매일 30분씩 운동하기",
            category = "EXERCISE",
            difficulty = "NORMAL",
            loginId = "user123",
            startDate = now.minusDays(5),  // 5일 전 시작
            endDate = now.plusDays(25)     // 25일 후 종료
        )

        challenge = Challenge(
            title = "30일 운동 챌린지",
            description = "매일 30분씩 운동하기",
            category = "EXERCISE",
            difficulty = "NORMAL",
            startDate = now.minusDays(5),  // 5일 전 시작
            endDate = now.plusDays(25),    // 25일 후 종료
            createdId = "user123",
            viewCnt = 0,
            participantCnt = 0,
            certificationCnt = 0
        )

        participant = ChallengeParticipant(
            id = challengeId,
            loginId = "user123"
        )

        every { redisTemplate.opsForValue() } returns valueOperations
    }

    @Test
    @DisplayName("챌린지 생성 성공")
    fun `createChallenge should succeed with valid request`() {
        // Given
        every { challengeRepository.save(any()) } returns challenge

        // When
        val result = challengeService.createChallenge(challengeRequest, "user123")

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
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { valueOperations.get(any()) } returns "10"

        // When
        val result = challengeService.getChallengeById(challengeId)

        // Then
        assertNotNull(result)
        assertEquals("30일 운동 챌린지", result.title)
        assertEquals(10L, result.viewCnt)
        verify(exactly = 1) { challengeRepository.findById(challengeId) }
    }

    @Test
    @DisplayName("챌린지 상세 조회 실패 - 존재하지 않는 챌린지")
    fun `getChallengeById should throw NoSuchElementException when challenge not found`() {
        // Given
        val nonExistentId = "CHL-99999999"
        every { challengeRepository.findById(nonExistentId) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<NoSuchElementException> {
            challengeService.getChallengeById(nonExistentId)
        }
        assertEquals("챌린지를 찾을 수 없습니다: $nonExistentId", exception.message)
        verify(exactly = 1) { challengeRepository.findById(nonExistentId) }
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
            startDate = now.plusDays(1),  // 미래 시작일
            endDate = now.plusDays(31)     // 미래 종료일
        )

        val futureChallenge = Challenge(
            title = "30일 운동 챌린지",
            description = "매일 30분씩 운동하기",
            category = "EXERCISE",
            difficulty = "NORMAL",
            startDate = now.plusDays(1),  // 미래 시작일 (진행 중이 아님)
            endDate = now.plusDays(31),
            createdId = "user123",
            viewCnt = 0,
            participantCnt = 0,
            certificationCnt = 0
        )

        val updatedChallenge = Challenge(
            title = "수정된 챌린지",
            description = "수정된 설명",
            category = "HEALTH",
            difficulty = "HARD",
            startDate = now.plusDays(1),
            endDate = now.plusDays(31),
            createdId = "user123",
            viewCnt = 0,
            participantCnt = 0,
            certificationCnt = 0
        )

        every { challengeRepository.findById(challengeId) } returns Optional.of(futureChallenge)
        every { challengeRepository.save(any()) } returns updatedChallenge

        // When
        val result = challengeService.updateChallenge(challengeId, updateRequest, "user123")

        // Then
        assertNotNull(result)
        assertEquals("수정된 챌린지", result.title)
        verify(exactly = 1) { challengeRepository.findById(challengeId) }
        verify(exactly = 1) { challengeRepository.save(any()) }
    }

    @Test
    @DisplayName("챌린지 수정 실패 - 권한 없음")
    fun `updateChallenge should throw IllegalArgumentException without permission`() {
        // Given
        val futureChallenge = Challenge(
            title = "30일 운동 챌린지",
            description = "매일 30분씩 운동하기",
            category = "EXERCISE",
            difficulty = "NORMAL",
            startDate = now.plusDays(1),
            endDate = now.plusDays(31),
            createdId = "user123",
            viewCnt = 0,
            participantCnt = 0,
            certificationCnt = 0
        )

        every { challengeRepository.findById(challengeId) } returns Optional.of(futureChallenge)

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            challengeService.updateChallenge(challengeId, challengeRequest, "user999")
        }
        assertEquals("챌린지를 수정할 권한이 없습니다", exception.message)
        verify(exactly = 1) { challengeRepository.findById(challengeId) }
        verify(exactly = 0) { challengeRepository.save(any()) }
    }

    @Test
    @DisplayName("챌린지 삭제 성공")
    fun `deleteChallenge should succeed with valid permission`() {
        // Given
        val futureChallenge = Challenge(
            title = "30일 운동 챌린지",
            description = "매일 30분씩 운동하기",
            category = "EXERCISE",
            difficulty = "NORMAL",
            startDate = now.plusDays(10), // 시작 전
            endDate = now.plusDays(40),
            createdId = "user123",
            viewCnt = 0,
            participantCnt = 0,
            certificationCnt = 0
        )
        every { challengeRepository.findById(challengeId) } returns Optional.of(futureChallenge)
        every { participantRepository.findByIdAndStatus(challengeId, ParticipantStatusEnum.ACTIVE) } returns emptyList()
        every { participantRepository.deleteAllByChallengeId(challengeId) } returns 0
        every { challengeRepository.delete(any()) } just runs

        // When
        challengeService.deleteChallenge(challengeId, "user123")

        // Then
        verify(exactly = 1) { challengeRepository.findById(challengeId) }
        verify(exactly = 1) { challengeRepository.delete(any()) }
    }

    @Test
    @DisplayName("챌린지 삭제 실패 - 권한 없음")
    fun `deleteChallenge should throw IllegalArgumentException without permission`() {
        // Given
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            challengeService.deleteChallenge(challengeId, "user999")
        }
        assertEquals("챌린지를 삭제할 권한이 없습니다", exception.message)
        verify(exactly = 1) { challengeRepository.findById(challengeId) }
        verify(exactly = 0) { challengeRepository.delete(any()) }
    }

    @Test
    @DisplayName("챌린지 참여 성공")
    fun `joinChallenge should succeed when not already participating`() {
        // Given
        val user = User(loginId = "user123", password = "password", nickname = "tester")
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { participantRepository.existsByIdAndLoginId(challengeId, "user123") } returns false
        every { participantRepository.save(any()) } returns participant
        every { challengeRepository.save(any()) } returns challenge
        every { userRepository.findByLoginId("user123") } returns user

        // When
        val result = challengeService.joinChallenge(challengeId, "user123")

        // Then
        assertNotNull(result)
        assertEquals("user123", result.loginId)
        assertEquals(ParticipantStatusEnum.ACTIVE, result.status)
        verify(exactly = 1) { participantRepository.existsByIdAndLoginId(challengeId, "user123") }
        verify(exactly = 1) { participantRepository.save(any()) }
        verify(exactly = 1) { challengeRepository.save(any()) }
    }

    @Test
    @DisplayName("챌린지 참여 실패 - 이미 참여중")
    fun `joinChallenge should throw IllegalStateException when already participating`() {
        // Given
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { participantRepository.existsByIdAndLoginId(challengeId, "user123") } returns true

        // When & Then
        val exception = assertThrows<IllegalStateException> {
            challengeService.joinChallenge(challengeId, "user123")
        }
        assertEquals("이미 참여중인 챌린지입니다", exception.message)
        verify(exactly = 1) { participantRepository.existsByIdAndLoginId(challengeId, "user123") }
        verify(exactly = 0) { participantRepository.save(any()) }
    }

    @Test
    @DisplayName("챌린지 탈퇴 성공")
    fun `withdrawChallenge should succeed when actively participating`() {
        // Given
        val user = User(loginId = "user123", password = "password", nickname = "tester")
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { participantRepository.findByIdAndLoginId(challengeId, "user123") } returns Optional.of(participant)
        every { participantRepository.save(any()) } returns participant
        every { challengeRepository.save(any()) } returns challenge
        every { userRepository.findByLoginId("user123") } returns user

        // When
        challengeService.withdrawChallenge(challengeId, "user123")

        // Then
        assertEquals(ParticipantStatusEnum.WITHDRAWN, participant.status)
        assertNotNull(participant.withdrawnAt)
        verify(exactly = 1) { participantRepository.findByIdAndLoginId(challengeId, "user123") }
        verify(exactly = 1) { participantRepository.save(any()) }
        verify(exactly = 1) { challengeRepository.save(any()) }
    }

    @Test
    @DisplayName("챌린지 탈퇴 실패 - 참여 정보 없음")
    fun `withdrawChallenge should throw NoSuchElementException when not participating`() {
        // Given
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { participantRepository.findByIdAndLoginId(challengeId, "user123") } returns Optional.empty()

        // When & Then
        val exception = assertThrows<NoSuchElementException> {
            challengeService.withdrawChallenge(challengeId, "user123")
        }
        assertEquals("참여 정보를 찾을 수 없습니다", exception.message)
        verify(exactly = 1) { participantRepository.findByIdAndLoginId(challengeId, "user123") }
        verify(exactly = 0) { participantRepository.save(any()) }
    }

    @Test
    @DisplayName("조회수 증가 성공")
    fun `incrementViewCount should increment Redis counter`() {
        // Given
        every { valueOperations.increment(any()) } returns 5L
        every { redisTemplate.expire(any(), any(), any()) } returns true

        // When
        challengeService.incrementViewCount(challengeId)

        // Then
        verify(exactly = 1) { valueOperations.increment(any()) }
        verify(exactly = 1) { redisTemplate.expire(any(), any(), any()) }
    }

    @Test
    @DisplayName("조회수 DB 동기화 성공")
    fun `syncViewCountToDatabase should update challenge view count`() {
        // Given
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { challengeRepository.save(any()) } returns challenge

        // When
        challengeService.syncViewCountToDatabase(challengeId, 100L)

        // Then
        assertEquals(100L, challenge.viewCnt)
        verify(exactly = 1) { challengeRepository.findById(challengeId) }
        verify(exactly = 1) { challengeRepository.save(any()) }
    }

    @Test
    @DisplayName("참여자 목록 조회 성공")
    fun `getParticipants should return list of participants`() {
        // Given
        val participants = listOf(participant)
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { participantRepository.findByChallenge_Id(challengeId) } returns participants

        // When
        val result = challengeService.getParticipants(challengeId)

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("user123", result[0].loginId)
        verify(exactly = 1) { participantRepository.findByChallenge_Id(challengeId) }
    }

    @Test
    @DisplayName("챌린지 통계 조회 성공")
    fun `getChallengeStatistics should return statistics`() {
        // Given
        every { challengeRepository.findById(challengeId) } returns Optional.of(challenge)
        every { participantRepository.countById(challengeId) } returns 100L
        every { participantRepository.countByIdAndStatus(challengeId, ParticipantStatusEnum.ACTIVE) } returns 80L
        every { participantRepository.countByIdAndStatus(challengeId, ParticipantStatusEnum.COMPLETED) } returns 15L
        every { participantRepository.countByIdAndStatus(challengeId, ParticipantStatusEnum.WITHDRAWN) } returns 5L
        every { participantRepository.sumCertificationCountById(challengeId) } returns 500L
        every { valueOperations.get(any()) } returns "1500"

        // When
        val result = challengeService.getChallengeStatistics(challengeId)

        // Then
        assertNotNull(result)
        assertEquals(challengeId, result.id)
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