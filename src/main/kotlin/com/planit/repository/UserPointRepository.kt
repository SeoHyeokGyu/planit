package com.planit.repository

import com.planit.entity.UserPoint
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface UserPointRepository : JpaRepository<UserPoint, Long> {
  fun findByUser_LoginId(loginId: String, pageable: Pageable): Page<UserPoint>

  fun countByUser_LoginId(loginId: String): Long

  fun deleteByUser_Id(userId: Long): Int
}
