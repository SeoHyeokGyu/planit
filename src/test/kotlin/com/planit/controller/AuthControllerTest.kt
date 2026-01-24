package com.planit.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.planit.config.JwtAuthenticationFilter
import com.planit.dto.LoginRequest
import com.planit.dto.LoginResponse
import com.planit.dto.SignUpRequest
import com.planit.entity.User
import com.planit.service.AuthService
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthController::class, properties = ["file.upload-url-path=/uploads"])
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc
  @Autowired private lateinit var objectMapper: ObjectMapper

  @MockkBean private lateinit var authService: AuthService
  @MockkBean private lateinit var jwtAuthFilter: JwtAuthenticationFilter

  @Test
  @DisplayName("회원가입 성공")
  fun `signUp should return 201 Created on success`() {
    // Given
    val signUpRequest = SignUpRequest("testuser", "password", "tester")
    val user = User(loginId = "testuser", password = "password", nickname = "tester")
    every { authService.signUp(any()) } returns user

    // When & Then
    mockMvc
        .post("/api/auth/signup") {
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(signUpRequest)
        }
        .andExpect {
          status { isCreated() }
          jsonPath("$.success") { value(true) }
        }
  }

  @Test
  @DisplayName("회원가입 실패 - 중복된 ID")
  fun `signUp should return 400 Bad Request for duplicate loginId`() {
    // Given
    val signUpRequest = SignUpRequest("testuser", "password", "tester")
    every { authService.signUp(any()) } throws IllegalArgumentException("이미 사용 중인 ID입니다.")

    // When & Then
    mockMvc
        .post("/api/auth/signup") {
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(signUpRequest)
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.success") { value(false) }
          jsonPath("$.error.message") { value("이미 사용 중인 ID입니다.") }
        }
  }

  @Test
  @DisplayName("로그인 성공")
  fun `login should return 200 OK with token on success`() {
    // Given
    val loginRequest = LoginRequest("testuser", "password")
    val loginResponse = LoginResponse("accessToken")
    every { authService.login(any()) } returns loginResponse

    // When & Then
    mockMvc
        .post("/api/auth/login") {
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(loginRequest)
        }
        .andExpect {
          status { isOk() }
          jsonPath("$.success") { value(true) }
          jsonPath("$.data.accessToken") { value("accessToken") }
        }
  }

  @Test
  @DisplayName("로그인 실패 - 잘못된 자격 증명")
  fun `login should return 400 Bad Request for invalid credentials`() {
    // Given
    val loginRequest = LoginRequest("testuser", "wrongpassword")
    every { authService.login(any()) } throws IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.")

    // When & Then
    mockMvc
        .post("/api/auth/login") {
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(loginRequest)
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.success") { value(false) }
          jsonPath("$.error.message") { value("아이디 또는 비밀번호가 일치하지 않습니다.") }
        }
  }
}
