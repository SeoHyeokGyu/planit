package com.planit.dto

import com.planit.entity.*
import java.time.LocalDateTime

data class ChallengeResponse(
    val id: Long,
    val title: String,
    val description: String,
    val category: ChallengeCategory,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val difficulty: ChallengeDifficulty,
    val createdBy: Long,
    val viewCount: Long,
    val participantCount: Int,
    val certificationCount: Long,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(challenge: Challenge): ChallengeResponse {
            return ChallengeResponse(
                id = challenge.id!!,
                title = challenge.title,
                description = challenge.description,
                category = challenge.category,
                startDate = challenge.startDate,
                endDate = challenge.endDate,
                difficulty = challenge.difficulty,
                createdBy = challenge.createdBy,
                viewCount = challenge.viewCount,
                participantCount = challenge.participantCount,
                certificationCount = challenge.certificationCount,
                status = when {
                    challenge.isUpcoming() -> "UPCOMING"
                    challenge.isActive() -> "ACTIVE"
                    challenge.isEnded() -> "ENDED"
                    else -> "UNKNOWN"
                },
                createdAt = challenge.createdAt,
                updatedAt = challenge.updatedAt
            )
        }
    }
}

data class ChallengeListResponse(
    val id: Long,
    val title: String,
    val category: ChallengeCategory,
    val difficulty: ChallengeDifficulty,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val participantCount: Int,
    val viewCount: Long,
    val status: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(challenge: Challenge): ChallengeListResponse {
            return ChallengeListResponse(
                id = challenge.id!!,
                title = challenge.title,
                category = challenge.category,
                difficulty = challenge.difficulty,
                startDate = challenge.startDate,
                endDate = challenge.endDate,
                participantCount = challenge.participantCount,
                viewCount = challenge.viewCount,
                status = when {
                    challenge.isUpcoming() -> "UPCOMING"
                    challenge.isActive() -> "ACTIVE"
                    challenge.isEnded() -> "ENDED"
                    else -> "UNKNOWN"
                },
                createdAt = challenge.createdAt
            )
        }
    }
}

data class ChallengeStatisticsResponse(
    val challengeId: Long,
    val totalParticipants: Int,
    val activeParticipants: Int,
    val completedParticipants: Int,
    val withdrawnParticipants: Int,
    val totalCertifications: Long,
    val completionRate: Double,
    val averageCertificationPerParticipant: Double,
    val viewCount: Long
)

data class ParticipantResponse(
    val id: Long,
    val userId: Long,
    val status: ParticipantStatus,
    val certificationCount: Int,
    val joinedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val withdrawnAt: LocalDateTime?
) {
    companion object {
        fun from(participant: ChallengeParticipant): ParticipantResponse {
            return ParticipantResponse(
                id = participant.id!!,
                userId = participant.userId,
                status = participant.status,
                certificationCount = participant.certificationCount,
                joinedAt = participant.joinedAt,
                completedAt = participant.completedAt,
                withdrawnAt = participant.withdrawnAt
            )
        }
    }
}

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isFirst: Boolean,
    val isLast: Boolean
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(success = true, message = message, data = data)
        }

        fun <T> success(message: String): ApiResponse<T> {
            return ApiResponse(success = true, message = message, data = null)
        }

        fun <T> error(message: String): ApiResponse<T> {
            return ApiResponse(success = false, message = message, data = null)
        }
    }
}
