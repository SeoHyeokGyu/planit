package com.planit.service

import com.planit.dto.UserPasswordUpdateRequest
import com.planit.entity.User
import com.planit.repository.*
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockKExtension::class)
class UserServiceTest {
  @MockK private lateinit var userRepository: UserRepository
  @MockK private lateinit var passwordEncoder: PasswordEncoder
  @MockK private lateinit var certificationRepository: CertificationRepository
  @MockK private lateinit var challengeParticipantRepository: ChallengeParticipantRepository
  @MockK private lateinit var likeRepository: LikeRepository
  @MockK private lateinit var followRepository: FollowRepository
  @MockK private lateinit var notificationRepository: NotificationRepository
  @MockK private lateinit var userPointRepository: UserPointRepository
  @MockK private lateinit var userExperienceRepository: UserExperienceRepository
  @MockK private lateinit var commentRepository: CommentRepository
  @MockK private lateinit var challengeRepository: ChallengeRepository
  
  @InjectMockKs private lateinit var userService: UserService

  private lateinit var user: User
  private lateinit var request: UserPasswordUpdateRequest

  @BeforeEach
  fun setUp() {
    user = User(loginId = "testuser", password = "password", nickname = "tester")
    request = UserPasswordUpdateRequest(oldPassword = "password", newPassword = "newPassword")
  }

  @Test
  @DisplayName("비밀번호 변경 성공")
  fun `updatePassword should succeed with correct old password`() {
    // Given
    val newHashedPassword = "hashedNewPassword"
    every { userRepository.findByLoginId(user.loginId) } returns user
    every { passwordEncoder.matches(request.oldPassword, user.password) } returns true
    every { passwordEncoder.encode(request.newPassword) } returns newHashedPassword

    // When
    userService.updatePassword(user.loginId, request)

    // Then
    verify { userRepository.findByLoginId(user.loginId)}
//    verify { passwordEncoder.matches(request.oldPassword, user.password) }
    verify { passwordEncoder.encode(request.newPassword) }
    assertEquals(newHashedPassword, user.password)
  }

  @Test
  @DisplayName("비밀번호 변경 실패 - 사용자를 찾을 수 없음")
  fun `updatePassword should throw NoSuchElementException when user not found`() {
    // Given
    val nonExistentLoginId = "unknownuser"
    every { userRepository.findByLoginId(nonExistentLoginId) } returns null

    // When & Then
    val exception =
        assertThrows<NoSuchElementException> {
          userService.updatePassword(nonExistentLoginId, request)
        }
    assertEquals("사용자를 찾을 수 없습니다: $nonExistentLoginId", exception.message)
    verify(exactly = 1) { userRepository.findByLoginId(nonExistentLoginId) }
    verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
  }

  @Test
  @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
  fun `updatePassword should throw IllegalArgumentException for invalid current password`() {
    // Given
    every { userRepository.findByLoginId(user.loginId) } returns user
    every { passwordEncoder.matches(request.oldPassword, user.password) } returns false

    // When & Then
    val exception =
        assertThrows<IllegalArgumentException> { userService.updatePassword(user.loginId, request) }
    assertEquals("현재 비밀번호가 일치하지 않습니다.", exception.message)
    verify(exactly = 1) { userRepository.findByLoginId(user.loginId) }
    verify(exactly = 1) { passwordEncoder.matches(request.oldPassword, user.password) }
    verify(exactly = 0) { passwordEncoder.encode(any()) }
  }
}
