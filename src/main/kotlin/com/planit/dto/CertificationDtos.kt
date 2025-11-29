package com.planit.dto

import com.planit.entity.Certification
import java.time.LocalDateTime

data class CertificationCreateRequest(
    val challengeId: Long,
    val title: String,
    val content: String
)

data class CertificationUpdateRequest(
    val title: String,
    val content: String
)

data class CertificationResponse(
    val id: Long,
    val title: String,
    val content: String,
    val photoUrl: String?,
    val authorNickname: String,
    val challengeTitle: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(certification: Certification): CertificationResponse {
            return CertificationResponse(
                id = certification.id!!,
                title = certification.title,
                content = certification.content,
                photoUrl = certification.photoUrl,
                authorNickname = certification.user.nickname ?: certification.user.loginId,
                challengeTitle = certification.challenge.title,
                createdAt = certification.createdAt,
                updatedAt = certification.updatedAt
            )
        }
    }
}
