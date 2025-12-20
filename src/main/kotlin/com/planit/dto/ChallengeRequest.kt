package com.planit.dto

import java.time.LocalDateTime

data class ChallengeRequest(
    var title: String,
    var description: String,
    var category: String,
    var difficulty: String,
    var loginId: String = "",
    var startDate: LocalDateTime,
    var endDate: LocalDateTime
)