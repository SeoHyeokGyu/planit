package com.planit.service

import com.planit.config.JwtTokenProvider
import com.planit.dto.LoginRequest
import com.planit.dto.SignUpRequest
import com.planit.entity.User
import com.planit.repository.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockKExtension::class)
class AuthServiceTest {
  @MockK private lateinit var userRepository: UserRepository
  @MockK private lateinit var passwordEncoder: PasswordEncoder
  @MockK private lateinit var authenticationManager: AuthenticationManager
  @MockK private lateinit var jwtTokenProvider: JwtTokenProvider
  @MockK private lateinit var redisTemplate: RedisTemplate<String, Any>
  @InjectMockKs private lateinit var authService: AuthService

  private lateinit var signUpRequest: SignUpRequest
  private lateinit var user: User

  @BeforeEach
  fun setUp() {
    signUpRequest = SignUpRequest(loginId = "testuser", password = "password", nickname = "tester")
    user = User(loginId = "testuser", password = "encodedPassword", nickname = "tester")
  }

  @Test
  @DisplayName("회원가입 성공")
  fun `signUp should succeed with valid request`() {
    // Given
    every { userRepository.findByLoginId(signUpRequest.loginId) } returns null
    every { passwordEncoder.encode(signUpRequest.password) } returns "encodedPassword"
    every { userRepository.save(any()) } returns user

    // When
    val result = authService.signUp(signUpRequest)

    // Then
    assertNotNull(result)
    assertEquals(user.loginId, result.loginId)
    verify(exactly = 1) { userRepository.findByLoginId(signUpRequest.loginId) }
    verify(exactly = 1) { passwordEncoder.encode(signUpRequest.password) }
    verify(exactly = 1) { userRepository.save(any()) }
  }

  @Test
  @DisplayName("회원가입 실패 - 중복된 ID")
  fun `signUp should throw IllegalArgumentException for duplicate loginId`() {
    // Given
    every { userRepository.findByLoginId(signUpRequest.loginId) } returns user

    // When & Then
    val exception = assertThrows<IllegalArgumentException> { authService.signUp(signUpRequest) }
    assertEquals("이미 사용 중인 ID입니다.", exception.message)
    verify(exactly = 1) { userRepository.findByLoginId(signUpRequest.loginId) }
    verify(exactly = 0) { passwordEncoder.encode(any()) }
    verify(exactly = 0) { userRepository.save(any()) }
  }

  @Test
  @DisplayName("로그인 성공")
  fun `login should succeed with valid credentials`() {
    // Given
    val loginRequest = LoginRequest(loginId = "testuser", password = "password")
    val authentication = mockk<org.springframework.security.core.Authentication>()
    val userDetails = mockk<com.planit.dto.CustomUserDetails>()

    every { authenticationManager.authenticate(any()) } returns authentication
    every { authentication.principal } returns userDetails
    every { userDetails.username } returns "testuser"
    every { jwtTokenProvider.createToken(any()) } returns "accessToken"

    // When
    val result = authService.login(loginRequest)

    // Then
    assertNotNull(result)
    assertEquals("accessToken", result.accessToken)
    verify(exactly = 1) { authenticationManager.authenticate(any()) }
    verify(exactly = 1) { jwtTokenProvider.createToken("testuser") }
  }
}
