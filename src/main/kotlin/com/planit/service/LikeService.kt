package com.planit.service

import com.planit.entity.Like
import com.planit.exception.CertificationNotFoundException
import com.planit.exception.UserNotFoundException
import com.planit.repository.CertificationRepository
import com.planit.repository.LikeRepository
import com.planit.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeService(
    private val likeRepository: LikeRepository,
    private val certificationRepository: CertificationRepository,
    private val userRepository: UserRepository
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
            true // Liked
        }
    }
}
