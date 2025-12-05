package com.planit.dto

import com.planit.entity.ChallengeParticipant
import com.planit.enums.ParticipantStatusEnum
import java.time.LocalDateTime

data class ParticipateResponse(
    val challengeId: String,
    val loginId: String,
    val status: ParticipantStatusEnum,
    val certificationCnt: Int,
    val joinedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val withdrawnAt: LocalDateTime?
) {
    companion object {
        fun from(participant: ChallengeParticipant): ParticipateResponse {
            return ParticipateResponse(
                challengeId = participant.id,
                loginId = participant.loginId,
                status = participant.status,
                certificationCnt = participant.certificationCnt,
                joinedAt = participant.joinedAt,
                completedAt = participant.completedAt,
                withdrawnAt = participant.withdrawnAt
            )
        }
    }
}