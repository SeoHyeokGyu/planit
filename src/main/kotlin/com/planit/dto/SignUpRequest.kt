package com.planit.dto

import jakarta.validation.constraints.NotBlank

// 회원가입 요청 DTO
data class SignUpRequest(
  val loginId: String,
  val password: String,
  val nickname: String?
)

