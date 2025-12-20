package com.planit.dto

import java.time.LocalDateTime

data class NotificationDto(
    val id: String? = null,
    val type: String, // "INFO", "SUCCESS", "ERROR" etc.
    val message: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
