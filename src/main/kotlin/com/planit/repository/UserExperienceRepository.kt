package com.planit.repository

import com.planit.entity.UserExperience
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface UserExperienceRepository : JpaRepository<UserExperience, Long> {
  fun findByUser_LoginId(loginId: String, pageable: Pageable): Page<UserExperience>

  fun countByUser_LoginId(loginId: String): Long

  fun deleteByUser_Id(userId: Long): Int
}
