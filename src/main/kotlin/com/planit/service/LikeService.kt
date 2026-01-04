package com.planit.service

import com.planit.dto.NotificationResponse
import com.planit.entity.Like
import com.planit.enums.NotificationType
import com.planit.exception.CertificationNotFoundException
import com.planit.exception.UserNotFoundException
import com.planit.repository.CertificationRepository
import com.planit.repository.LikeRepository
import com.planit.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class LikeService(
    private val likeRepository: LikeRepository,
    private val certificationRepository: CertificationRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {

    @Transactional
    fun toggleLike(certificationId: Long, userLoginId: String): Boolean {
        val user = userRepository.findByLoginId(userLoginId) ?: throw UserNotFoundException()
        val certification = certificationRepository.findById(certificationId).orElseThrow { CertificationNotFoundException() }

        val existingLike = likeRepository.findByCertificationIdAndUserLoginId(certificationId, userLoginId)

        return if (existingLike != null) {
            likeRepository.delete(existingLike)
            false // Unliked
        } else {
            likeRepository.save(Like(user = user, certification = certification))
            
            // Send real-time notification if liker is not the author (Redis Broadcast)
            if (certification.user.loginId != userLoginId) {
                notificationService.sendNotification(
                    NotificationResponse(
                        id = java.util.UUID.randomUUID().toString(),
                        receiverId = certification.user.id!!,
                        receiverLoginId = certification.user.loginId,
                        senderId = user.id,
                        senderLoginId = user.loginId,
                        senderNickname = user.nickname,
                        type = NotificationType.LIKE,
                        message = "${user.nickname ?: user.loginId}님이 회원님의 인증을 좋아합니다.",
                        relatedId = certification.id.toString(),
                        relatedType = "CERTIFICATION",
                        isRead = false,
                        createdAt = LocalDateTime.now()
                    )
                )
            }
            true // Liked
        }
    }
}
