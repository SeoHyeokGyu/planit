package com.planit.repository

import com.planit.entity.Certification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CertificationRepository : JpaRepository<Certification, Long> {
    fun findByUser_LoginId(userLoginId: String, pageable: Pageable): Page<Certification>
    fun findByChallenge_Id(challengeId: Long, pageable: Pageable): Page<Certification>
}
