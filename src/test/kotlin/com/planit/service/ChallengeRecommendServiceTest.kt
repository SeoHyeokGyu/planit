package com.planit.service

import com.fasterxml.jackson.core.type.TypeReference
import com.planit.dto.ChallengeRecommendationResponse
import com.planit.entity.Challenge
import com.planit.entity.ChallengeParticipant
import com.planit.entity.User
import com.planit.enums.ChallengeCategoryEnum
import com.planit.enums.ChallengeDifficultyEnum
import com.planit.enums.ParticipantStatusEnum
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.ChallengeRepository
import com.planit.repository.UserRepository
import com.planit.util.setPrivateProperty
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ChallengeRecommendServiceTest {
    private val challengeRepository: ChallengeRepository = mockk()
    private val participantRepository: ChallengeParticipantRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val geminiService: GeminiService = mockk()

    private lateinit var recommendService: ChallengeRecommendService

    @BeforeEach
    fun setUp() {
        recommendService = ChallengeRecommendService(
            challengeRepository,
            participantRepository,
            userRepository,
            geminiService
        )
    }

    @Test
    @DisplayName("새로운 챌린지 제안(생성용) 성공")
    fun `recommendNewChallenges should return 3 AI-generated recommendations`() {
        // Given
        val loginId = "user123"
        val user = User(loginId = loginId, password = "password", nickname = "tester")
        
        val challenge = Challenge(
            title = "기존 챌린지",
            description = "설명",
            category = ChallengeCategoryEnum.HEALTH.name,
            difficulty = ChallengeDifficultyEnum.EASY.name,
            startDate = LocalDateTime.now().minusDays(5),
            endDate = LocalDateTime.now().plusDays(5),
            createdId = "other"
        ).apply { setPrivateProperty("id", "CHL-1") }
        
        val participant = ChallengeParticipant(id = "CHL-1", loginId = loginId).apply {
            this.challenge = challenge
            this.status = ParticipantStatusEnum.ACTIVE
        }

        every { userRepository.findByLoginId(loginId) } returns user
        every { participantRepository.findByLoginId(loginId) } returns listOf(participant)
        every { challengeRepository.findAllOrderByParticipantCntDesc() } returns listOf(challenge)
        
        val aiResponses = listOf(
            ChallengeRecommendationResponse(
                title = "AI 추천 1",
                description = "AI 설명 1",
                category = ChallengeCategoryEnum.HEALTH,
                difficulty = ChallengeDifficultyEnum.NORMAL,
                reason = "이유 1"
            )
        )
        
        every { 
            geminiService.generateContent(any<String>(), any<TypeReference<List<ChallengeRecommendationResponse>>>()) 
        } returns aiResponses

        // When
        val result = recommendService.recommendNewChallenges(loginId)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("AI 추천 1")
    }

    @Test
    @DisplayName("기존 챌린지 중에서 추천(참여용) 성공")
    fun `recommendExistingChallenges should return recommendations from existing challenges`() {
        // Given
        val loginId = "user123"
        val user = User(loginId = loginId, password = "password", nickname = "tester")
        
        val existingChallenge = Challenge(
            title = "참여 가능한 챌린지",
            description = "설명",
            category = ChallengeCategoryEnum.STUDY.name,
            difficulty = ChallengeDifficultyEnum.NORMAL.name,
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(10),
            createdId = "admin"
        ).apply { setPrivateProperty("id", "CHL-AVAILABLE") }

        every { userRepository.findByLoginId(loginId) } returns user
        every { participantRepository.findByLoginId(loginId) } returns emptyList()
        every { challengeRepository.findAllOrderByCreatedAtDesc() } returns listOf(existingChallenge)
        every { challengeRepository.findAllOrderByParticipantCntDesc() } returns listOf(existingChallenge)
        
        val aiSelection = listOf(
            mapOf("challengeId" to "CHL-AVAILABLE", "reason" to "과거 패턴상 적합합니다.")
        )
        
        every { 
            geminiService.generateContent(any<String>(), any<TypeReference<List<Map<String, String>>>>()) 
        } returns aiSelection

        // When
        val result = recommendService.recommendExistingChallenges(loginId)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].challenge.id).isEqualTo("CHL-AVAILABLE")
        assertThat(result[0].reason).isEqualTo("과거 패턴상 적합합니다.")
    }
}