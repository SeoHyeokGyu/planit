package com.planit.service

import com.planit.dto.UserPasswordUpdateRequest
import com.planit.entity.User
import com.planit.repository.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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
  @MockK private lateinit var commentRepository: CommentRepository
  @MockK private lateinit var challengeRepository: ChallengeRepository

  @InjectMockKs private lateinit var userService: UserService

  private lateinit var user: User
  private lateinit var request: UserPasswordUpdateRequest

  @BeforeEach
  fun setUp() {
    user = User(loginId = "testuser", password = "password", nickname = "tester")
    val userIdField = User::class.java.getDeclaredField("id")
    userIdField.isAccessible = true
    userIdField.set(user, 1L)
    
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
    verify { userRepository.findByLoginId(user.loginId) }
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

  @Test
  @DisplayName("사용자 정보 업데이트 성공")
  fun `updateUser should update nickname`() {
    // Given
    val updateRequest = com.planit.dto.UserUpdateRequest(nickname = "newNickname")
    every { userRepository.findByLoginId(user.loginId) } returns user

    // When
    val result = userService.updateUser(user, updateRequest)

    // Then
    assertEquals("newNickname", result.nickname)
    assertEquals("newNickname", user.nickname)
  }

  @Test
  @DisplayName("대시보드 통계 조회 성공")
  fun `getDashboardStats should return correct stats`() {
    // Given
    every { challengeParticipantRepository.countByLoginIdAndStatus(user.loginId, any()) } returns 5
    every { certificationRepository.countByUser_LoginId(user.loginId) } returns 10
    every { followRepository.countByFollowing_LoginId(user.loginId) } returns 20
    every { followRepository.countByFollower_LoginId(user.loginId) } returns 15

    // When
    val stats = userService.getDashboardStats(user.loginId)

    // Then
    assertEquals(5, stats.challengeCount)
    assertEquals(10, stats.certificationCount)
    assertEquals(20, stats.followerCount)
    assertEquals(15, stats.followingCount)
  }

  @Test
  @DisplayName("사용자 프로필 조회 성공")
  fun `getUserProfileByLoginId should return profile`() {
    // Given
    every { userRepository.findByLoginId(user.loginId) } returns user

    // When
    val profile = userService.getUserProfileByLoginId(user.loginId)

    // Then
    assertEquals(user.loginId, profile.loginId)
    assertEquals(user.nickname, profile.nickname)
  }

  @Test
  @DisplayName("사용자 검색 성공")
  fun `searchUsers should return page of profiles`() {
    // Given
    val keyword = "test"
    val pageable = org.springframework.data.domain.PageRequest.of(0, 10)
    every { userRepository.findByLoginIdContainingOrNicknameContaining(keyword, keyword, pageable) } returns org.springframework.data.domain.PageImpl(listOf(user))

    // When
    val result = userService.searchUsers(keyword, pageable)

    // Then
    assertEquals(1, result.content.size)
    assertEquals(user.loginId, result.content[0].loginId)
  }

  @Test
  @DisplayName("랜덤 사용자 조회 성공")
  fun `getRandomUsers should return list of profiles`() {
    // Given
    val size = 5
    val pageable = org.springframework.data.domain.Pageable.ofSize(size)
    every { userRepository.findRandomUsers(pageable) } returns org.springframework.data.domain.PageImpl(listOf(user))

    // When
    val result = userService.getRandomUsers(size)

    // Then
    assertEquals(1, result.size)
    assertEquals(user.loginId, result[0].loginId)
  }

  @Test
  @DisplayName("사용자 삭제 성공")
  fun `deleteUser should delete all related data`() {
    // Given
    val request = com.planit.dto.UserDeleteRequest(password = "password")
    
    // Set user ID via reflection
    val userIdField = User::class.java.getDeclaredField("id")
    userIdField.isAccessible = true
    userIdField.set(user, 1L)
    
    every { userRepository.findByLoginId(user.loginId) } returns user
    every { passwordEncoder.matches(request.password, user.password) } returns true
    
    // Mock all delete/update calls
    every { likeRepository.deleteByUser_LoginId(user.loginId) } returns 1
    every { followRepository.deleteByFollower_Id(1L) } returns 1
    every { followRepository.deleteByFollowing_Id(1L) } returns 1
    every { notificationRepository.deleteByReceiver_Id(1L) } returns 1
    every { notificationRepository.nullifySenderBySenderId(1L) } returns 1
    every { userPointRepository.deleteByUser_Id(1L) } returns 1
    
    every { challengeParticipantRepository.findByLoginId(user.loginId) } returns emptyList()
    every { challengeParticipantRepository.deleteByLoginId(user.loginId) } returns 1
    
    val withdrawalUser = User("withdrawn_user", "pw", "탈퇴한 사용자")
    val wUserIdField = User::class.java.getDeclaredField("id")
    wUserIdField.isAccessible = true
    wUserIdField.set(withdrawalUser, 999L)
    
    every { userRepository.findByLoginId("withdrawn_user") } returns withdrawalUser
    
    every { certificationRepository.reassignUserByUserId(1L, 999L) } returns 1
    every { commentRepository.reassignUserByUserId(1L, 999L) } returns 1
    every { userRepository.delete(user) } just Runs

    // When
    userService.deleteUser(user.loginId, request)

    // Then
    verify { userRepository.delete(user) }
  }
}
