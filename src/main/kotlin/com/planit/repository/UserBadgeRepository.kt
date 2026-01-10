package com.planit.repository

import com.planit.entity.UserBadge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserBadgeRepository : JpaRepository<UserBadge, Long> {
  fun findByUserId(userId: Long): List<UserBadge>

  @Query("SELECT ub FROM UserBadge ub JOIN FETCH ub.badge WHERE ub.user.loginId = :loginId")
  fun findByUserLoginId(loginId: String): List<UserBadge>

  fun existsByUserIdAndBadgeCode(userId: Long, badgeCode: String): Boolean
}
