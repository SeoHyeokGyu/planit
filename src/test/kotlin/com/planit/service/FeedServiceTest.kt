package com.planit.service

import com.planit.entity.Certification
import com.planit.entity.Challenge
import com.planit.entity.User
import com.planit.exception.UserNotFoundException
import com.planit.repository.CertificationRepository
import com.planit.repository.FollowRepository
import com.planit.repository.UserRepository
import com.planit.util.setPrivateProperty
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("FeedService 테스트")
class FeedServiceTest {

  @MockK private lateinit var userRepository: UserRepository
  @MockK private lateinit var followRepository: FollowRepository
  @MockK private lateinit var certificationRepository: CertificationRepository

  @InjectMockKs private lateinit var feedService: FeedService

  private lateinit var currentUser: User
  private lateinit var followedUser1: User
  private lateinit var followedUser2: User
  private lateinit var challenge: Challenge

  @BeforeEach
  fun setUp() {
    currentUser =
        User("currentUser", "password", "Current User").apply { setPrivateProperty("id", 1L) }
    followedUser1 =
        User("followed1", "password", "Followed 1").apply { setPrivateProperty("id", 2L) }
    followedUser2 =
        User("followed2", "password", "Followed 2").apply { setPrivateProperty("id", 3L) }
    challenge =
        Challenge(
                "Test",
                "Desc",
                "Cat",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "Easy",
                "creator",
            )
            .apply { setPrivateProperty("id", 1L) }
  }

  @Nested
  @DisplayName("getFollowingFeed 메서드는")
  inner class DescribeGetFollowingFeed {

    @Test
    @DisplayName("팔로우하는 사용자가 있으면 그들의 인증 목록을 페이지로 반환한다")
    fun `returns a page of certifications from followed users`() {
      // Given
      val pageable = PageRequest.of(0, 10)
      val followedUserIds = listOf(followedUser1.id!!, followedUser2.id!!)
      val certifications =
          listOf(
              Certification(followedUser1, challenge, "Cert 1", "Content 1"),
              Certification(followedUser2, challenge, "Cert 2", "Content 2"),
          )
      val certificationPage: Page<Certification> =
          PageImpl(certifications, pageable, certifications.size.toLong())

      every { userRepository.findByLoginId(currentUser.loginId) } returns currentUser
      every { followRepository.findFollowingIdsByFollowerId(currentUser.id!!) } returns
          followedUserIds
      every {
        certificationRepository.findByUser_IdInOrderByCreatedAtDesc(followedUserIds, pageable)
      } returns certificationPage

      // When
      val result = feedService.getFollowingFeed(currentUser.loginId, pageable)

      // Then
      assertThat(result).isNotNull
      assertThat(result.content).hasSize(2)
      assertThat(result.content.map { it.user })
          .containsExactlyInAnyOrder(followedUser1, followedUser2)
      verify(exactly = 1) {
        certificationRepository.findByUser_IdInOrderByCreatedAtDesc(any(), any())
      }
    }

    @Test
    @DisplayName("팔로우하는 사용자가 없으면 빈 페이지를 반환한다")
    fun `returns an empty page when user follows no one`() {
      // Given
      val pageable = PageRequest.of(0, 10)
      val emptyList = emptyList<Long>()

      every { userRepository.findByLoginId(currentUser.loginId) } returns currentUser
      every { followRepository.findFollowingIdsByFollowerId(currentUser.id!!) } returns emptyList

      // When
      val result = feedService.getFollowingFeed(currentUser.loginId, pageable)

      // Then
      assertThat(result).isNotNull
      assertThat(result.isEmpty).isTrue()
      verify(exactly = 0) {
        certificationRepository.findByUser_IdInOrderByCreatedAtDesc(any(), any())
      }
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 피드를 조회하면 UserNotFoundException을 던진다")
    fun `throws UserNotFoundException for a non-existent user`() {
      // Given
      val nonExistentUserLoginId = "nonexistent"
      val pageable = PageRequest.of(0, 10)
      every { userRepository.findByLoginId(nonExistentUserLoginId) } returns null

      // When & Then
      assertThrows<UserNotFoundException> {
        feedService.getFollowingFeed(nonExistentUserLoginId, pageable)
      }
    }
  }
}
