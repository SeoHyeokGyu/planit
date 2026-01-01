package com.planit.dto

import com.planit.entity.*
import java.time.LocalDate

data class ChallengeResponse(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val createdId: String,
    val viewCnt: Long,
    val participantCnt: Long,
    val certificationCnt: Long,
    val isActive: Boolean,
    val isUpcoming: Boolean,
    val isEnded: Boolean
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
                isActive = challenge.isActive(),
                isUpcoming = challenge.isUpcoming(),
                isEnded = challenge.isEnded()
            )
        }
    }
}