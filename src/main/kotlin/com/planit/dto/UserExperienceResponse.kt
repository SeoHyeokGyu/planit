package com.planit.dto

import java.time.LocalDateTime

data class UserExperienceResponse(
    val id: Long,
    val experience: Long,
    val reason: String,
    val createdAt: LocalDateTime,
)

data class UserLevelResponse(
    val totalExperience: Long,
    val level: Int,
    val nextLevelExperience: Long,
    val currentLevelExperience: Long,
    val experienceProgress: Double, // 0.0 ~ 100.0
)

data class UserProgressResponse(
    val totalPoint: Long,
    val totalExperience: Long,
    val level: Int,
    val experienceProgress: Double,
    val nextLevelExperience: Long,
)
