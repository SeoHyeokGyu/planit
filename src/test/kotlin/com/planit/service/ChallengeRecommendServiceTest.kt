package com.planit.service

import com.fasterxml.jackson.core.type.TypeReference
import com.planit.entity.Challenge
import com.planit.entity.User
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.ChallengeRepository
import com.planit.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class ChallengeRecommendServiceTest {

    @MockK
    private lateinit var challengeRepository: ChallengeRepository

    @MockK
    private lateinit var participantRepository: ChallengeParticipantRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var geminiService: GeminiService

    @InjectMockKs
    private lateinit var challengeRecommendService: ChallengeRecommendService

    private val loginId = "testuser"
    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = User(loginId, "p", "Tester")
    }

    @Test
    @DisplayName("기존 챌린지 추천 성공")
    fun `recommendExistingChallenges should return recommendations`() {
        // Given
        val challenge = Challenge("T", "D", "C", LocalDateTime.now(), LocalDateTime.now().plusDays(10), "E", "admin")
        val chlId = challenge.id
        
        every { userRepository.findByLoginId(loginId) } returns user
        every { participantRepository.findByLoginId(loginId) } returns emptyList()
        every { challengeRepository.findAllOrderByCreatedAtDesc() } returns listOf(challenge)
        every { challengeRepository.findAllOrderByParticipantCntDesc() } returns emptyList()
        
        val aiResponse = listOf(mapOf("challengeId" to chlId, "reason" to "Good for you"))
        every { geminiService.generateContent(any(), any<TypeReference<List<Map<String, String>>>>()) } returns aiResponse

        // When
        val result = challengeRecommendService.recommendExistingChallenges(loginId)

        // Then
        assertEquals(1, result.size)
        assertEquals(chlId, result[0].challenge.id)
        assertEquals("Good for you", result[0].reason)
    }

    @Test
    @DisplayName("기존 챌린지 추천 - 후보가 없으면 빈 목록 반환")
    fun `recommendExistingChallenges should return empty list if no candidates`() {
        // Given
        every { userRepository.findByLoginId(loginId) } returns user
        every { participantRepository.findByLoginId(loginId) } returns emptyList()
        every { challengeRepository.findAllOrderByCreatedAtDesc() } returns emptyList()
        every { challengeRepository.findAllOrderByParticipantCntDesc() } returns emptyList()

        // When
        val result = challengeRecommendService.recommendExistingChallenges(loginId)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    @DisplayName("새 챌린지 추천 성공")
    fun `recommendNewChallenges should call gemini`() {
        // Given
        every { userRepository.findByLoginId(loginId) } returns user
        every { participantRepository.findByLoginId(loginId) } returns emptyList()
        every { challengeRepository.findAllOrderByParticipantCntDesc() } returns emptyList()
        
        val recommendations = emptyList<com.planit.dto.ChallengeRecommendationResponse>()
        every { geminiService.generateContent(any(), any<TypeReference<List<com.planit.dto.ChallengeRecommendationResponse>>>()) } returns recommendations

        // When
        val result = challengeRecommendService.recommendNewChallenges(loginId)

        // Then
        assertNotNull(result)
    }

    @Test
    @DisplayName("기존 챌린지 추천(쿼리) - null 필드 포함된 AI 응답 처리")
    fun `recommendExistingChallengesWithQuery should handle null fields in AI response`() {
        // Given
        val challenge = Challenge("T", "D", "C", LocalDateTime.now(), LocalDateTime.now().plusDays(10), "E", "admin")
        val chlId = challenge.id
        
        every { userRepository.findByLoginId(loginId) } returns user
        every { participantRepository.findByLoginId(loginId) } returns emptyList()
        every { challengeRepository.findAllOrderByCreatedAtDesc() } returns listOf(challenge)
        every { challengeRepository.findAllOrderByParticipantCntDesc() } returns emptyList()
        
        // Response with missing 'reason' or 'challengeId'
        val aiResponse = listOf(
            mapOf("challengeId" to chlId), // No reason
            mapOf("reason" to "R"),        // No ID
            mapOf("challengeId" to chlId, "reason" to "R") // Valid
        )
        every { geminiService.generateContent(any(), any<TypeReference<List<Map<String, String>>>>()) } returns aiResponse

        // When
        val result = challengeRecommendService.recommendExistingChallengesWithQuery(loginId, "help me")

        // Then
        assertEquals(1, result.size)
    }

    @Test
    @DisplayName("새 챌린지 추천(쿼리) 성공")
    fun `recommendNewChallengesWithQuery should return list`() {
        // Given
        every { userRepository.findByLoginId(loginId) } returns user
        every { participantRepository.findByLoginId(loginId) } returns emptyList()
        every { challengeRepository.findAllOrderByParticipantCntDesc() } returns emptyList()
        
        val recommendations = emptyList<com.planit.dto.ChallengeRecommendationResponse>()
        every { geminiService.generateContent(any(), any<TypeReference<List<com.planit.dto.ChallengeRecommendationResponse>>>()) } returns recommendations

        // When
        val result = challengeRecommendService.recommendNewChallengesWithQuery(loginId, "new one")

        // Then
        assertNotNull(result)
    }
}
