package com.planit.service

import com.fasterxml.jackson.core.type.TypeReference
import com.planit.dto.ChallengeListResponse
import com.planit.dto.ChallengeRecommendationResponse
import com.planit.dto.ExistingChallengeRecommendationResponse
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
) {
  private val log = LoggerFactory.getLogger(javaClass)

  fun recommendExistingChallenges(loginId: String): List<ExistingChallengeRecommendationResponse> {
    val user =
      userRepository.findByLoginId(loginId) ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

    val participants = participantRepository.findByLoginId(loginId)
    val participatingChallengeIds = participants.map { it.challenge.id }.toSet()

    // 후보군 선정: 최신순 + 인기순 혼합
    val recent = challengeRepository.findAllOrderByCreatedAtDesc().take(30)
    val popular = challengeRepository.findAllOrderByParticipantCntDesc().take(30)

    val candidates =
      (recent + popular)
        .asSequence()
        .distinctBy { it.id }
        .filter { it.endDate > LocalDateTime.now() } // 종료되지 않은 것
        .filter { it.id !in participatingChallengeIds } // 이미 참여하지 않은 것
        .take(50)
        .toList()

    if (candidates.isEmpty()) {
      return emptyList()
    }

    val candidateListString =
      candidates.joinToString("\n") {
        "- ID: ${it.id}, Title: ${it.title}, Category: ${it.category}, Difficulty: ${it.difficulty}, Description: ${it.description.take(50)}..."
      }

    val prompt =
      """
            다음 사용자에게 가장 적합한 챌린지를 위 후보 목록에서 3개 선택해서 추천해줘.
            
            사용자 정보:
            - 닉네임: ${user.nickname ?: user.loginId}
            - 과거 참여 카테고리: ${participants.map { it.challenge.category }.distinct().joinToString()}
            
            후보 챌린지 목록:
            $candidateListString
            
            조건:
            1. 위 목록에 있는 ID만 사용해야 함.
            2. 응답은 반드시 JSON 배열 형식이어야 하며, 다른 텍스트는 포함하지 마.
            3. 각 객체는 'challengeId'와 'reason' 필드를 가져야 함.
            
            JSON 예시:
            [
              { "challengeId": "uuid-1234", "reason": "이 챌린지는 ..." }
            ]
        """
        .trimIndent()

    val recommendations =
      geminiService.generateContent(
        prompt,
        object : TypeReference<List<Map<String, String>>>() {},
      )

    val candidateMap = candidates.associateBy { it.id }

    return recommendations.mapNotNull { rec ->
      val id = rec["challengeId"]
      val reason = rec["reason"]
      if (id != null && reason != null) {
        candidateMap[id]?.let { challenge ->
          ExistingChallengeRecommendationResponse(
            challenge = ChallengeListResponse.from(challenge),
            reason = reason,
          )
        }
      } else {
        null
      }
    }
  }

  fun recommendExistingChallengesWithQuery(loginId: String, userQuery: String): List<ExistingChallengeRecommendationResponse> {
    val user =
      userRepository.findByLoginId(loginId) ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

    val participants = participantRepository.findByLoginId(loginId)
    val participatingChallengeIds = participants.map { it.challenge.id }.toSet()

    // 후보군 선정: 최신순 + 인기순 혼합
    val recent = challengeRepository.findAllOrderByCreatedAtDesc().take(30)
    val popular = challengeRepository.findAllOrderByParticipantCntDesc().take(30)

    val candidates =
      (recent + popular)
        .asSequence()
        .distinctBy { it.id }
        .filter { it.endDate > LocalDateTime.now() } // 종료되지 않은 것
        .filter { it.id !in participatingChallengeIds } // 이미 참여하지 않은 것
        .take(50)
        .toList()

    if (candidates.isEmpty()) {
      return emptyList()
    }

    val candidateListString =
      candidates.joinToString("\n") {
        "- ID: ${it.id}, Title: ${it.title}, Category: ${it.category}, Difficulty: ${it.difficulty}, Description: ${it.description.take(50)}..."
      }

    val prompt =
      """
            다음 사용자에게 가장 적합한 챌린지를 위 후보 목록에서 3개 선택해서 추천해줘.
            
            사용자 정보:
            - 닉네임: ${user.nickname ?: user.loginId}
            - 과거 참여 카테고리: ${participants.map { it.challenge.category }.distinct().joinToString()}
            - 사용자의 현재 요청/기분: "$userQuery" (이 내용을 최우선으로 고려해줘)
            
            후보 챌린지 목록:
            $candidateListString
            
            조건:
            1. 위 목록에 있는 ID만 사용해야 함.
            2. 응답은 반드시 JSON 배열 형식이어야 하며, 다른 텍스트는 포함하지 마.
            3. 각 객체는 'challengeId'와 'reason' 필드를 가져야 함.
            4. reason은 사용자의 요청이나 상황에 맞춰 설득력 있게 작성해줘.
            
            JSON 예시:
            [
              { "challengeId": "uuid-1234", "reason": "요즘 무기력하시다면, 작은 성취를 느낄 수 있는 이 챌린지가 딱이에요!" }
            ]
        """
        .trimIndent()

    val recommendations =
      geminiService.generateContent(
        prompt,
        object : TypeReference<List<Map<String, String>>>() {},
      )

    val candidateMap = candidates.associateBy { it.id }

    return recommendations.mapNotNull { rec ->
      val id = rec["challengeId"]
      val reason = rec["reason"]
      if (id != null && reason != null) {
        candidateMap[id]?.let { challenge ->
          ExistingChallengeRecommendationResponse(
            challenge = ChallengeListResponse.from(challenge),
            reason = reason,
          )
        }
      } else {
        null
      }
    }
  }

  fun recommendNewChallengesWithQuery(loginId: String, userQuery: String): List<ChallengeRecommendationResponse> {
    val user =
      userRepository.findByLoginId(loginId) ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

    val participants = participantRepository.findByLoginId(loginId)
    val recentCategories =
      participants
        .take(10)
        .mapNotNull {
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

    val prompt =
      """
            사용자 맞춤형 챌린지를 3개 추천해줘.
            
            사용자 정보:
            - 닉네임: ${user.nickname ?: user.loginId}
            - 최근 관심 카테고리: ${recentCategories.joinToString()}
            - 현재 참여 중인 챌린지: ${ongoingChallenges.joinToString()}
            - 현재 인기 있는 챌린지: ${popularChallenges.joinToString()}
            - 사용자의 현재 요청/기분: "$userQuery" (이 내용을 최우선으로 고려해줘)
            - 현재 일시: ${LocalDateTime.now()}

            조건:
            1. 카테고리는 다음 중 하나여야 함: ${ChallengeCategoryEnum.entries.joinToString()}
            2. 난이도는 다음 중 하나여야 함: ${ChallengeDifficultyEnum.entries.joinToString()}
            3. 응답은 반드시 JSON 배열 형식이어야 하며, 다른 텍스트는 포함하지 마.
            4. 각 객체는 title, description, category, difficulty, reason 필드를 가져야 함.
            5. reason은 사용자의 요청이나 상황에 맞춰 설득력 있게 작성해줘.
            
            JSON 예시:
            [
              {
                "title": "매일 물 2L 마시기",
                "description": "건강을 위해 매일 물 2L를 마시는 습관을 들입니다.",
                "category": "HEALTH",
                "difficulty": "EASY",
                "reason": "요즘 무기력하시다면, 수분 보충으로 활력을 찾아보세요!"
              }
            ]
        """
        .trimIndent()

    return geminiService.generateContent(
      prompt,
      object : TypeReference<List<ChallengeRecommendationResponse>>() {},
    )
  }

  fun recommendNewChallenges(loginId: String): List<ChallengeRecommendationResponse> {
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
    return geminiService.generateContent(
      prompt,
      object : TypeReference<List<ChallengeRecommendationResponse>>() {},
    )
  }
}
