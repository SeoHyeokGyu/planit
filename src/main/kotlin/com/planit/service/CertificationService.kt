package com.planit.service

import com.planit.dto.*
import com.planit.entity.Certification
import com.planit.exception.*
import com.planit.repository.CertificationRepository
import com.planit.repository.ChallengeRepository
import com.planit.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CertificationService(
    private val certificationRepository: CertificationRepository,
    private val userRepository: UserRepository,
    private val challengeRepository: ChallengeRepository
) {

    @Transactional
    fun createCertification(request: CertificationCreateRequest, userLoginId: String): CertificationResponse {
        val user = userRepository.findByLoginId(userLoginId) ?: throw UserNotFoundException()
        val challenge = challengeRepository.findById(request.challengeId).orElseThrow { ChallengeNotFoundException() }

        val certification = Certification(
            user = user,
            challenge = challenge,
            title = request.title,
            content = request.content
        )

        val savedCertification = certificationRepository.save(certification)
        return CertificationResponse.from(savedCertification)
    }

    @Transactional(readOnly = true)
    fun getCertification(certificationId: Long): CertificationResponse {
        val certification = certificationRepository.findById(certificationId).orElseThrow { CertificationNotFoundException() }
        return CertificationResponse.from(certification)
    }

    @Transactional(readOnly = true)
    fun getCertificationsByUser(userLoginId: String, pageable: Pageable): PagedResponse<CertificationResponse> {
        val certificationPage = certificationRepository.findByUser_LoginId(userLoginId, pageable)
        val content = certificationPage.content.map { CertificationResponse.from(it) }
        return PagedResponse.from(certificationPage, content)
    }

    @Transactional(readOnly = true)
    fun getCertificationsByChallenge(challengeId: Long, pageable: Pageable): PagedResponse<CertificationResponse> {
        val certificationPage = certificationRepository.findByChallenge_Id(challengeId, pageable)
        val content = certificationPage.content.map { CertificationResponse.from(it) }
        return PagedResponse.from(certificationPage, content)
    }

    @Transactional
    fun updateCertification(certificationId: Long, request: CertificationUpdateRequest, userLoginId: String): CertificationResponse {
        val certification = certificationRepository.findById(certificationId).orElseThrow { CertificationNotFoundException() }

        if (certification.user.loginId != userLoginId) {
            throw CertificationUpdateForbiddenException()
        }

        if (certification.createdAt.isBefore(LocalDateTime.now().minusHours(24))) {
            throw CertificationUpdatePeriodExpiredException()
        }

        certification.title = request.title
        certification.content = request.content
        certification.updatedAt = LocalDateTime.now()

        val updatedCertification = certificationRepository.save(certification)
        return CertificationResponse.from(updatedCertification)
    }

    @Transactional
    fun deleteCertification(certificationId: Long, userLoginId: String) {
        val certification = certificationRepository.findById(certificationId).orElseThrow { CertificationNotFoundException() }

        if (certification.user.loginId != userLoginId) {
            throw CertificationUpdateForbiddenException("You are not allowed to delete this certification")
        }

        certificationRepository.delete(certification)
    }
    
    @Transactional
    fun uploadCertificationPhoto(certificationId: Long, photoUrl: String, userLoginId: String): CertificationResponse {
        val certification = certificationRepository.findById(certificationId).orElseThrow { CertificationNotFoundException() }
        if (certification.user.loginId != userLoginId) {
            throw CertificationUpdateForbiddenException("You are not allowed to upload photo to this certification")
        }
        
        certification.photoUrl = photoUrl
        val updatedCertification = certificationRepository.save(certification)
        return CertificationResponse.from(updatedCertification)
    }
}
