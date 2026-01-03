package com.planit.dto

import com.planit.entity.Badge
import com.planit.enums.BadgeGrade
import java.time.LocalDateTime

data class BadgeResponse(
  val code: String,
  val name: String,
  val description: String,
  val iconCode: String,
  val grade: BadgeGrade,
  val isAcquired: Boolean,
  val acquiredAt: LocalDateTime? = null,
) {
  companion object {
    fun from(
      badge: Badge,
      isAcquired: Boolean = false,
      acquiredAt: LocalDateTime? = null,
    ): BadgeResponse {
      return BadgeResponse(
        code = badge.code,
        name = badge.name,
        description = badge.description,
        iconCode = badge.iconCode,
        grade = badge.grade,
        isAcquired = isAcquired,
        acquiredAt = acquiredAt,
      )
    }
  }
}
