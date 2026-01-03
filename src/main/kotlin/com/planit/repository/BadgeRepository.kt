package com.planit.repository

import com.planit.entity.Badge
import org.springframework.data.jpa.repository.JpaRepository

interface BadgeRepository : JpaRepository<Badge, Long> {
  fun existsByCode(code: String): Boolean

  fun findByCode(code: String): Badge?
}
