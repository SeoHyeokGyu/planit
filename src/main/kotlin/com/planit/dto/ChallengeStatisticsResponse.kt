package com.planit.dto

data class ChallengeStatisticsResponse(
    val challengeId: String,
    val totalParticipants: Int,
    val activeParticipants: Int,
    val completedParticipants: Int,
    val withdrawnParticipants: Int,
    val totalCertifications: Long,
    val completionRate: Double,
    val averageCertificationPerParticipant: Double,
    val viewCount: Long
)