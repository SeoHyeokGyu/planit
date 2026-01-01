package com.planit.dto

import com.planit.entity.*
import java.time.LocalDateTime

data class ChallengeResponse(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val createdId: String,
    val viewCnt: Long,
    val participantCnt: Long,
    val certificationCnt: Long,
) {
    companion object {
        fun from(challenge: Challenge): ChallengeResponse {
            return ChallengeResponse(
                id = challenge.id,
                title = challenge.title,
                description = challenge.description,
                category = challenge.category,
                difficulty = challenge.difficulty,
                startDate = challenge.startDate,
                endDate = challenge.endDate,
                createdId = challenge.createdId,
                viewCnt = challenge.viewCnt,
                participantCnt = challenge.participantCnt,
                certificationCnt = challenge.certificationCnt,
            )
        }
    }
}