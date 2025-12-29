package com.planit.dto

import java.time.LocalDateTime

data class UserPointResponse(
    val id: Long,
    val points: Long,
    val reason: String,
    val createdAt: LocalDateTime,
)

data class UserPointSummaryResponse(
    val totalPoint: Long,
    val pointCount: Long,
)
