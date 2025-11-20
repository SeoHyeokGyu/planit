package com.planit.dto

// 로그인 응답 DTO (JWT 토큰 포함)
data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer" // 토큰 타입 (관례)
)