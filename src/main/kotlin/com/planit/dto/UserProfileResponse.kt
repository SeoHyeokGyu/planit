package com.planit.dto

import com.planit.entity.User

// 예시 DTO
data class UserProfileResponse(val id: Long, val loginId: String, val nickname: String?) {
  companion object {
    fun of(user: User): UserProfileResponse {
      return UserProfileResponse(
          id = user.id!!,
          loginId = user.loginId,
          nickname = user.nickname,
      )
    }
  }

}