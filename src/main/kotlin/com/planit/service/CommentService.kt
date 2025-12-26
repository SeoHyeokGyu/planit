package com.planit.service

import com.planit.dto.CommentCreateRequest
import com.planit.dto.CommentResponse
import com.planit.entity.Comment
import com.planit.exception.CertificationNotFoundException
import com.planit.exception.UserNotFoundException
import com.planit.repository.CertificationRepository
import com.planit.repository.CommentRepository
import com.planit.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val certificationRepository: CertificationRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createComment(certificationId: Long, request: CommentCreateRequest, userLoginId: String): CommentResponse {
        val user = userRepository.findByLoginId(userLoginId) ?: throw UserNotFoundException()
        val certification = certificationRepository.findById(certificationId).orElseThrow { CertificationNotFoundException() }

        val comment = Comment(
            user = user,
            certification = certification,
            content = request.content
        )

        val savedComment = commentRepository.save(comment)
        return CommentResponse.from(savedComment, userLoginId)
    }

    @Transactional(readOnly = true)
    fun getComments(certificationId: Long, userLoginId: String?): List<CommentResponse> {
        val comments = commentRepository.findByCertificationIdOrderByCreatedAtAsc(certificationId)
        return comments.map { CommentResponse.from(it, userLoginId) }
    }

    @Transactional
    fun deleteComment(commentId: Long, userLoginId: String) {
        val comment = commentRepository.findById(commentId).orElseThrow { IllegalArgumentException("댓글을 찾을 수 없습니다.") }

        if (comment.user.loginId != userLoginId) {
            throw IllegalArgumentException("댓글 삭제 권한이 없습니다.")
        }

        // Soft delete handled by entity/repository via custom query or just setter if implemented manually
        // Since I used @SQLDelete annotation on entity, repository.delete() calls the SQL.
        // However, standard delete() removes row from context. 
        // @SQLDelete works when hibernate executes the delete.
        commentRepository.delete(comment)
    }
}
