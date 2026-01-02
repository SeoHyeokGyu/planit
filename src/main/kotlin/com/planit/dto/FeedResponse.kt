package com.planit.dto

import com.planit.entity.Certification
import java.time.LocalDateTime

/**
 * 피드 조회를 위한 전용 응답 DTO
 * @property id 인증 ID
 * @property title 인증 제목
 * @property content 인증 내용
 * @property photoUrl 인증 사진 URL
 * @property authorNickname 작성자 닉네임
 * @property authorLoginId 작성자 로그인 ID
 * @property challengeId 챌린지 ID
 * @property challengeTitle 챌린지 제목
 * @property createdAt 인증 생성일시
 * @property updatedAt 인증 최종 수정일시
 */
data class FeedResponse(
    val id: Long,
    val title: String,
    val content: String,
    val photoUrl: String?,
    val authorNickname: String,
    val authorLoginId: String,
    val challengeId: String,
    val challengeTitle: String,
    val likeCount: Long,
    val commentCount: Long,
    val isLiked: Boolean,
    val isMine: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(
            certification: Certification,
            likeCount: Long,
            commentCount: Long,
            isLiked: Boolean,
            currentUserId: Long
        ): FeedResponse {
            val author = certification.user
            val authorNickname = author?.nickname ?: author?.loginId ?: "탈퇴한 사용자"
            val authorLoginId = author?.loginId ?: "deleted_user"

            return FeedResponse(
                id = certification.id!!,
                title = certification.title,
                content = certification.content,
                photoUrl = certification.photoUrl,
                authorNickname = authorNickname,
                authorLoginId = authorLoginId,
                challengeId = certification.challenge.id,
                challengeTitle = certification.challenge.title,
                likeCount = likeCount,
                commentCount = commentCount,
                isLiked = isLiked,
                isMine = author?.id == currentUserId,
                createdAt = certification.createdAt,
                updatedAt = certification.updatedAt
            )
        }
    }
}
