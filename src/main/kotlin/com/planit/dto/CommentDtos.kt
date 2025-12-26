package com.planit.dto

import com.planit.entity.Comment
import java.time.LocalDateTime

data class CommentCreateRequest(
    val content: String
)

data class CommentResponse(
    val id: Long,
    val content: String,
    val authorNickname: String,
    val authorLoginId: String,
    val createdAt: LocalDateTime,
    val isMyComment: Boolean
) {
    companion object {
        fun from(comment: Comment, currentLoginId: String?): CommentResponse {
            return CommentResponse(
                id = comment.id!!,
                content = comment.content,
                authorNickname = comment.user.nickname ?: comment.user.loginId,
                authorLoginId = comment.user.loginId,
                createdAt = comment.createdAt,
                isMyComment = comment.user.loginId == currentLoginId
            )
        }
    }
}
