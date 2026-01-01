package com.planit.dto

import java.time.LocalDate

data class ChallengeRequest(
    var title: String,
    var description: String,
    var category: String,
    var difficulty: String,
    var loginId: String = "",
    var startDate: LocalDate,
    var endDate: LocalDate
)