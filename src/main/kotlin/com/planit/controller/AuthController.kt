package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.LoginRequest
import com.planit.dto.LoginResponse
import com.planit.dto.SignUpRequest
import com.planit.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

  @PostMapping("/signup")
  fun signUp(@RequestBody request: SignUpRequest): ResponseEntity<ApiResponse<Unit>> {
    authService.signUp(request)
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success())
  }

  @PostMapping("/login")
  fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
    val loginResponse = authService.login(request)
    return ResponseEntity.ok(ApiResponse.success(loginResponse))
  }
}
