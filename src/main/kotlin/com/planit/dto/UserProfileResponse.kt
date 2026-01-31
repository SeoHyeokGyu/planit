package com.planit.dto

import com.planit.entity.User
import java.time.LocalDateTime

// 사용자 프로필 정보 응답 DTO
data class UserProfileResponse(
  val id: Long,
  val loginId: String,
  val nickname: String?,
  val totalPoint: Long,
  val createdAt: LocalDateTime?
) {
  companion object {
    fun of(user: User): UserProfileResponse {
      return UserProfileResponse(
          id = user.id!!,
          loginId = user.loginId,
          nickname = user.nickname,
          totalPoint = user.totalPoint,
          createdAt = user.createdAt
      )
    }
  }

}