package com.planit.service

import com.planit.entity.Follow
import com.planit.entity.User
import com.planit.repository.FollowRepository
import com.planit.repository.UserRepository
import com.planit.util.setPrivateProperty
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import java.util.NoSuchElementException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("FollowService 테스트")
class FollowServiceTest {
  @MockK private lateinit var followRepository: FollowRepository
  @MockK private lateinit var userRepository: UserRepository
  @InjectMockKs private lateinit var followService: FollowService

  private lateinit var userA: User
  private lateinit var userB: User

  @BeforeEach
  fun setUp() {
    userA =
        User(
            loginId = "userA",
            password = "password",
            nickname = "UserA",
        )
    userA.setPrivateProperty("id", 1L)
    userB =
        User(
            loginId = "userB",
            password = "password",
            nickname = "UserB",
        )
    userB.setPrivateProperty("id", 2L)
  }

  @Nested
  @DisplayName("follow 메서드는")
  inner class DescribeFollow {

    @Test
    @DisplayName("성공적으로 다른 사용자를 팔로우한다")
    fun `successfully follows another user`() {
      // Given
      every { userRepository.findByLoginId(userA.loginId) } returns userA
      every { userRepository.findByLoginId(userB.loginId) } returns userB
      every { followRepository.existsByFollowerIdAndFollowingId(userA.id!!, userB.id!!) } returns
          false
      every { followRepository.save(any()) } returns Follow(follower = userA, following = userB)

      // When
      followService.follow(userA.loginId, userB.loginId)

      // Then
      verify(exactly = 1) { followRepository.save(any()) }
    }

    @Test
    @DisplayName("자기 자신을 팔로우하려고 하면 IllegalArgumentException을 던진다")
    fun `throws IllegalArgumentException when trying to follow self`() {
      // When & Then
      assertThrows<IllegalArgumentException> { followService.follow(userA.loginId, userA.loginId) }
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 팔로우하려고 하면 NoSuchElementException을 던진다")
    fun `throws NoSuchElementException when follower does not exist`() {
      // Given
      every { userRepository.findByLoginId("nonexistent") } returns null
      every { userRepository.findByLoginId(userB.loginId) } returns userB

      // When & Then
      assertThrows<NoSuchElementException> { followService.follow("nonexistent", userB.loginId) }
    }

    @Test
    @DisplayName("이미 팔로우한 사용자를 다시 팔로우하려고 하면 IllegalStateException을 던진다")
    fun `throws IllegalStateException when already following`() {
      // Given
      every { userRepository.findByLoginId(userA.loginId) } returns userA
      every { userRepository.findByLoginId(userB.loginId) } returns userB
      every { followRepository.existsByFollowerIdAndFollowingId(userA.id!!, userB.id!!) } returns
          true

      // When & Then
      assertThrows<IllegalStateException> { followService.follow(userA.loginId, userB.loginId) }
    }
  }

  @Nested
  @DisplayName("unfollow 메서드는")
  inner class DescribeUnfollow {

    @Test
    @DisplayName("성공적으로 다른 사용자를 언팔로우한다")
    fun `successfully unfollows another user`() {
      // Given
      val followRelation = Follow(follower = userA, following = userB)
      every { userRepository.findByLoginId(userA.loginId) } returns userA
      every { userRepository.findByLoginId(userB.loginId) } returns userB
      every { followRepository.findByFollowerIdAndFollowingId(userA.id!!, userB.id!!) } returns
          followRelation
      justRun { followRepository.delete(followRelation) }

      // When
      followService.unfollow(userA.loginId, userB.loginId)

      // Then
      verify(exactly = 1) { followRepository.delete(followRelation) }
    }

    @Test
    @DisplayName("팔로우 관계가 존재하지 않을 때 언팔로우를 시도하면 IllegalStateException을 던진다")
    fun `throws IllegalStateException when follow relation does not exist`() {
      // Given
      every { userRepository.findByLoginId(userA.loginId) } returns userA
      every { userRepository.findByLoginId(userB.loginId) } returns userB
      every { followRepository.findByFollowerIdAndFollowingId(userA.id!!, userB.id!!) } returns null

      // When & Then
      assertThrows<IllegalStateException> { followService.unfollow(userA.loginId, userB.loginId) }
    }
  }

  @Nested
  @DisplayName("getFollowerCount 메서드는")
  inner class DescribeGetFollowerCount {

    @Test
    @DisplayName("사용자의 팔로워 수를 정확히 반환한다")
    fun `returns the correct follower count`() {
      // Given
      val expectedCount = 10L
      every { userRepository.findByLoginId(userB.loginId) } returns userB
      every { followRepository.countByFollowingId(userB.id!!) } returns expectedCount

      // When
      val count = followService.getFollowerCount(userB.loginId)

      // Then
      assertThat(count).isEqualTo(expectedCount)
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 NoSuchElementException을 던진다")
    fun `throws NoSuchElementException when user does not exist`() {
      // Given
      every { userRepository.findByLoginId("nonexistent") } returns null

      // When & Then
      assertThrows<NoSuchElementException> { followService.getFollowerCount("nonexistent") }
    }
  }

  @Nested
  @DisplayName("getFollowingCount 메서드는")
  inner class DescribeGetFollowingCount {

    @Test
    @DisplayName("사용자의 팔로잉 수를 정확히 반환한다")
    fun `returns the correct following count`() {
      // Given
      val expectedCount = 5L
      every { userRepository.findByLoginId(userA.loginId) } returns userA
      every { followRepository.countByFollowerId(userA.id!!) } returns expectedCount

      // When
      val count = followService.getFollowingCount(userA.loginId)

      // Then
      assertThat(count).isEqualTo(expectedCount)
    }
  }
}
