package com.planit.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.planit.dto.ChallengeRecommendationResponse
import com.planit.enums.ChallengeCategoryEnum
import com.planit.enums.ChallengeDifficultyEnum
import com.planit.enums.ParticipantStatusEnum
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.ChallengeRepository
import com.planit.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ChallengeRecommendService(
  private val challengeRepository: ChallengeRepository,
  private val participantRepository: ChallengeParticipantRepository,
  private val userRepository: UserRepository,
  private val geminiService: GeminiService,
  private val objectMapper: ObjectMapper,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  fun recommendChallenges(loginId: String): List<ChallengeRecommendationResponse> {
    val user =
      userRepository.findByLoginId(loginId) ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

    // 1. 사용자 컨텍스트 수집
    val participants = participantRepository.findByLoginId(loginId)
    val recentCategories =
      participants
        .take(10)
        .mapNotNull {
          // 실무에서는 challengeRepository.findById를 쓰거나 Fetch Join을 씁니다.
          // 여기서는 간단하게 enum으로 변환 가능한 형태라고 가정합니다.
          try {
            ChallengeCategoryEnum.valueOf(it.challenge.category)
          } catch (e: Exception) {
            null
          }
        }
        .distinct()

    val ongoingChallenges =
      participants.filter { it.status == ParticipantStatusEnum.ACTIVE }.map { it.challenge.title }

    val popularChallenges =
      challengeRepository.findAllOrderByParticipantCntDesc().take(5).map {
        "${it.title}(${it.category})"
      }

    // 2. Gemini 프롬프트 작성
    val prompt =
      """
            사용자 맞춤형 챌린지를 3개 추천해줘.
            
            사용자 정보:
            - 닉네임: ${user.nickname ?: user.loginId}
            - 최근 관심 카테고리: ${recentCategories.joinToString()}
            - 현재 참여 중인 챌린지: ${ongoingChallenges.joinToString()}
            - 현재 인기 있는 챌린지: ${popularChallenges.joinToString()}
            - 현재 일시: ${LocalDateTime.now()}

            조건:
            1. 카테고리는 다음 중 하나여야 함: ${ChallengeCategoryEnum.entries.joinToString()}
            2. 난이도는 다음 중 하나여야 함: ${ChallengeDifficultyEnum.entries.joinToString()}
            3. 응답은 반드시 JSON 배열 형식이어야 하며, 다른 텍스트는 포함하지 마.
            4. 각 객체는 title, description, category, difficulty, reason 필드를 가져야 함.
            
            JSON 예시:
            [
              {
                "title": "매일 물 2L 마시기",
                "description": "건강을 위해 매일 물 2L를 마시는 습관을 들입니다.",
                "category": "HEALTH",
                "difficulty": "EASY",
                "reason": "최근 운동 챌린지에 참여하셨는데, 수분 보충 습관을 더하면 시너지가 날 것 같습니다."
              }
            ]
        """
        .trimIndent()

    // 3. Gemini 호출 및 파싱
    val responseText = geminiService.generateContent(prompt)
    log.debug("response text: $responseText")

    // JSON 응답에서 불필요한 마크다운 코드 블록(```json ... ```) 제거
    val cleanJson = responseText.replace("```json", "").replace("```", "").trim()
    return objectMapper.readValue(
      cleanJson,
      objectMapper.typeFactory.constructCollectionType(
        List::class.java,
        ChallengeRecommendationResponse::class.java,
      ),
    )
  }
}
