package com.planit.dto

data class NotificationRequest(
    val userLoginId: String,
    val type: String,
    val message: String
)
