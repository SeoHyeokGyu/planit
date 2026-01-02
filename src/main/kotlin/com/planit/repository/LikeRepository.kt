package com.planit.repository

import com.planit.dto.CertificationCountProjection
import com.planit.entity.Like
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface LikeRepository : JpaRepository<Like, Long> {
    fun countByCertificationId(certificationId: Long): Long
    fun existsByCertificationIdAndUserLoginId(certificationId: Long, userLoginId: String): Boolean
    fun findByCertificationIdAndUserLoginId(certificationId: Long, userLoginId: String): Like?

    @Query("SELECT l.certification.id as certificationId, COUNT(l) as count FROM Like l WHERE l.certification.id IN :certificationIds GROUP BY l.certification.id")
    fun countByCertificationIdIn(@Param("certificationIds") certificationIds: List<Long>): List<CertificationCountProjection>

    @Query("SELECT l.certification.id FROM Like l WHERE l.certification.id IN :certificationIds AND l.user.loginId = :userLoginId")
    fun findLikedCertificationIds(@Param("certificationIds") certificationIds: List<Long>, @Param("userLoginId") userLoginId: String): List<Long>

    fun deleteByUser_LoginId(userLoginId: String): Int
}
