package com.planit.service

import com.planit.entity.Follow
import com.planit.entity.User
import com.planit.repository.FollowRepository
import com.planit.repository.UserRepository
import com.planit.service.badge.BadgeService
import com.planit.util.setPrivateProperty
import com.planit.exception.UserNotFoundException
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.support.SimpleValueWrapper

@ExtendWith(MockKExtension::class)
@DisplayName("FollowService 테스트")
class FollowServiceTest {

  @MockK private lateinit var followRepository: FollowRepository
  @MockK private lateinit var userRepository: UserRepository

  @MockK
  private lateinit var cacheManager: CacheManager
  
  @MockK
  private lateinit var notificationService: NotificationService
  
  @MockK
  private lateinit var badgeService: BadgeService
  
  @InjectMockKs private lateinit var followService: FollowService

  private lateinit var userA: User
  private lateinit var userB: User
  private lateinit var followerCountCache: Cache
  private lateinit var followingCountCache: Cache

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

    // 캐시 모킹
    followerCountCache = mockk(relaxed = true)
    followingCountCache = mockk(relaxed = true)
    every { cacheManager.getCache("followerCount") } returns followerCountCache
    every { cacheManager.getCache("followingCount") } returns followingCountCache
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
      every { followRepository.existsByFollowerIdAndFollowingId(userA.id!!, userB.id!!) } returns false
      every { followRepository.save(any()) } returns Follow(follower = userA, following = userB)
      // 캐시 동작 모킹 (기존 캐시 값이 없다고 가정)
      every { followingCountCache.get(userA.loginId) } returns null
      every { followerCountCache.get(userB.loginId) } returns null
      
      every { badgeService.checkAndAwardBadges(any(), any()) } returns Unit
      every { notificationService.sendNotification(any(), any()) } returns Unit

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
    @DisplayName("존재하지 않는 사용자를 팔로우하려고 하면 UserNotFoundException을 던진다")
    fun `throws UserNotFoundException when follower does not exist`() {
      // Given
      every { userRepository.findByLoginId("nonexistent") } returns null
      every { userRepository.findByLoginId(userB.loginId) } returns userB

      // When & Then
      assertThrows<UserNotFoundException> { followService.follow("nonexistent", userB.loginId) }
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
      // 캐시 동작 모킹 (기존 캐시 값이 있다고 가정)
      every { followingCountCache.get(userA.loginId) } returns SimpleValueWrapper(5L)
      every { followerCountCache.get(userB.loginId) } returns SimpleValueWrapper(10L)

      // When
      followService.unfollow(userA.loginId, userB.loginId)

      // Then
      verify(exactly = 1) { followRepository.delete(followRelation) }
      verify { followingCountCache.put(userA.loginId, 4L) } // 캐시 값 감소 검증
      verify { followerCountCache.put(userB.loginId, 9L) } // 캐시 값 감소 검증
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
    @DisplayName("캐시에 값이 없으면 DB에서 조회하여 반환하고 캐시에 저장한다")
    fun `returns from DB and saves to cache if not in cache`() {
      // Given
      val expectedCount = 10L
      every { followerCountCache.get(userB.loginId) } returns null // 캐시 없음
      every { userRepository.findByLoginId(userB.loginId) } returns userB
      every { followRepository.countByFollowingId(userB.id!!) } returns expectedCount

      // When
      val count = followService.getFollowerCount(userB.loginId)

      // Then
      assertThat(count).isEqualTo(expectedCount)
      verify(exactly = 1) { followRepository.countByFollowingId(userB.id!!) } // DB 조회 검증
      verify(exactly = 1) { followerCountCache.put(userB.loginId, expectedCount) } // 캐시 저장 검증
    }

    @Test
    @DisplayName("캐시에 값이 있으면 DB 조회 없이 캐시에서 바로 반환한다")
    fun `returns from cache if present`() {
      // Given
      val expectedCount = 10L
      every { followerCountCache.get(userB.loginId) } returns SimpleValueWrapper(expectedCount) // 캐시 있음

      // When
      val count = followService.getFollowerCount(userB.loginId)

      // Then
      assertThat(count).isEqualTo(expectedCount)
      verify(exactly = 0) { followRepository.countByFollowingId(any()) } // DB 조회 안 함
    }
  }

  @Nested
  @DisplayName("getFollowingCount 메서드는")
  inner class DescribeGetFollowingCount {

    @Test
    @DisplayName("캐시에 값이 없으면 DB에서 조회하여 반환하고 캐시에 저장한다")
    fun `returns from DB and saves to cache if not in cache`() {
      // Given
      val expectedCount = 5L
      every { followingCountCache.get(userA.loginId) } returns null // 캐시 없음
      every { userRepository.findByLoginId(userA.loginId) } returns userA
      every { followRepository.countByFollowerId(userA.id!!) } returns expectedCount

      // When
      val count = followService.getFollowingCount(userA.loginId)

      // Then
      assertThat(count).isEqualTo(expectedCount)
      verify(exactly = 1) { followRepository.countByFollowerId(userA.id!!) } // DB 조회 검증
      verify(exactly = 1) { followingCountCache.put(userA.loginId, expectedCount) } // 캐시 저장 검증
    }
  }
}
