package com.planit.repository

import com.planit.dto.CertificationCountProjection
import com.planit.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CommentRepository : JpaRepository<Comment, Long> {
    fun countByCertificationId(certificationId: Long): Long
    fun findByCertificationIdOrderByCreatedAtAsc(certificationId: Long): List<Comment>

    @Query("SELECT c.certification.id as certificationId, COUNT(c) as count FROM Comment c WHERE c.certification.id IN :certificationIds GROUP BY c.certification.id")
    fun countByCertificationIdIn(@Param("certificationIds") certificationIds: List<Long>): List<CertificationCountProjection>

    @Modifying
    @Query("UPDATE Comment c SET c.user.id = :targetUserId WHERE c.user.id = :userId")
    fun reassignUserByUserId(@Param("userId") userId: Long, @Param("targetUserId") targetUserId: Long): Int
}
