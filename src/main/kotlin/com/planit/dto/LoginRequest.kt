package com.planit.dto

// 로그인 요청 DTO
data class LoginRequest(
  val loginId: String,
  val password: String
)