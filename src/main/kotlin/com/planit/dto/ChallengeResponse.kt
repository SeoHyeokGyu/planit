package com.planit.dto

import com.planit.entity.*
import java.time.LocalDateTime

data class ChallengeResponse(
    val challengeId: String,
    val title: String,
    val description: String,
    val category: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val difficulty: String,
    val createdId: String,
    val viewCnt: Long,
    val participantCnt: Long,
    val certificationCnt: Long,
) {
    companion object {
        fun from(challenge: Challenge): ChallengeResponse {
            return ChallengeResponse(
                challengeId = challenge.challengeId,
                title = challenge.title,
                description = challenge.description,
                category = challenge.category,
                startDate = challenge.startDate,
                endDate = challenge.endDate,
                difficulty = challenge.difficulty,
                createdId = challenge.createdId,
                viewCnt = challenge.viewCnt,
                participantCnt = challenge.participantCnt,
                certificationCnt = challenge.certificationCnt,
            )
        }
    }
}