package com.planit.dto

import com.planit.entity.Challenge
import java.time.LocalDateTime

data class ChallengeListResponse(
    val challengeId: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val createdId: String,
    val viewCnt: Long,
    val participantCnt: Long,
    val certificationCnt: Long
) {
    companion object {
        fun from(challenge: Challenge): ChallengeListResponse {
            return ChallengeListResponse(
                challengeId = challenge.id,
                title = challenge.title,
                description = challenge.description,
                category = challenge.category,
                difficulty = challenge.difficulty,
                startDate = challenge.startDate,
                endDate = challenge.endDate,
                createdId = challenge.createdId,
                viewCnt = challenge.viewCnt,
                participantCnt = challenge.participantCnt,
                certificationCnt = challenge.certificationCnt
            )
        }
    }
}