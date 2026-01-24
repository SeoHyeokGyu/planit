package com.planit.dto

import com.planit.enums.ChallengeCategoryEnum
import com.planit.enums.ChallengeDifficultyEnum

data class ChallengeRecommendationResponse(
    val title: String,
    val description: String,
    val category: ChallengeCategoryEnum,
    val difficulty: ChallengeDifficultyEnum,
    val reason: String // AI가 이 챌린지를 추천한 이유
)

data class UserContext(
    val nickname: String?,
    val recentCategories: List<ChallengeCategoryEnum>,
    val ongoingChallengeTitles: List<String>,
    val popularChallenges: List<String>
)
