package com.planit.dto

data class ChallengeSearchRequest(
    val keyword: String? = null,
    val category: String? = null,
    val difficulty: String? = null,
    val status: String? = null,
    val sortBy: String? = "LATEST" // LATEST, NAME, DIFFICULTY, POPULAR
)