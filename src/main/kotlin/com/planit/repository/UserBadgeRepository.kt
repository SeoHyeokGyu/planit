package com.planit.repository

import com.planit.entity.UserBadge
import org.springframework.data.jpa.repository.JpaRepository

interface UserBadgeRepository : JpaRepository<UserBadge, Long> {
  fun findByUserId(userId: Long): List<UserBadge>

  fun findByUserLoginId(loginId: String): List<UserBadge>

  fun existsByUserIdAndBadgeCode(userId: Long, badgeCode: String): Boolean
}
