package com.planit.dto

import com.planit.entity.Certification
import java.time.LocalDateTime

/**
 * 새로운 인증 생성을 위한 요청 DTO
 * @property challengeId 인증을 생성할 챌린지의 ID
 * @property title 인증 제목
 * @property content 인증 내용
 */
data class CertificationCreateRequest(
    val challengeId: String,
    val title: String,
    val content: String
)

/**
 * 인증 정보 업데이트를 위한 요청 DTO
 * @property title 업데이트할 인증 제목
 * @property content 업데이트할 인증 내용
 */
data class CertificationUpdateRequest(
    val title: String,
    val content: String
)

/**
 * AI 인증 사진 분석 결과 DTO
 * @property isSuitable 챌린지 주제 적합 여부
 * @property reason 판단 근거 또는 실패 사유
 */
data class CertificationAnalysisResponse(
    val isSuitable: Boolean,
    val reason: String
)

/**
 * 인증 정보 응답 DTO
 * @property id 인증 ID
 * @property title 인증 제목
 * @property content 인증 내용
 * @property photoUrl 인증 사진 URL
 * @property authorNickname 작성자 닉네임 (닉네임이 없을 경우 로그인 ID 사용)
 * @property senderNickname 작성자 닉네임 (피드용)
 * @property senderLoginId 작성자 로그인 ID (피드용)
 * @property challengeTitle 챌린지 제목
 * @property createdAt 인증 생성일시
 * @property updatedAt 인증 최종 수정일시
 */
data class CertificationResponse(
    val id: Long,
    val title: String,
    val content: String,
    val photoUrl: String?,
    val analysisResult: String?,
    val authorNickname: String,
    val senderNickname: String? = null,
    val senderLoginId: String? = null,
    val challengeId: String,
    val challengeTitle: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * Certification 엔티티로부터 CertificationResponse DTO를 생성합니다.
         * @param certification Certification 엔티티
         * @return CertificationResponse DTO
         */
        fun from(certification: Certification): CertificationResponse {
            return CertificationResponse(
                id = certification.id!!,
                title = certification.title,
                content = certification.content,
                photoUrl = certification.photoUrl,
                analysisResult = certification.analysisResult,
                // 작성자 닉네임이 없을 경우 로그인 ID 사용
                authorNickname = certification.user.nickname ?: certification.user.loginId,
                senderNickname = certification.user.nickname ?: certification.user.loginId,
                senderLoginId = certification.user.loginId,
                challengeId = certification.challenge.id,
                challengeTitle = certification.challenge.title,
                createdAt = certification.createdAt,
                updatedAt = certification.updatedAt
            )
        }
    }
}
