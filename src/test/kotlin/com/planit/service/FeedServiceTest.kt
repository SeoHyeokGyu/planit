package com.planit.service

import com.planit.dto.CertificationCountProjection
import com.planit.entity.Certification
import com.planit.entity.User
import com.planit.enums.FeedSortType
import com.planit.repository.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class FeedServiceTest {

    @MockK
    private lateinit var followRepository: FollowRepository

    @MockK
    private lateinit var certificationRepository: CertificationRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var likeRepository: LikeRepository

    @MockK
    private lateinit var commentRepository: CommentRepository

    @InjectMockKs
    private lateinit var feedService: FeedService

    private val userLoginId = "testuser"
    private lateinit var user: User
    private lateinit var certification: Certification

    @BeforeEach
    fun setUp() {
        user = User(loginId = userLoginId, password = "password", nickname = "TestUser")
        val userField = User::class.java.getDeclaredField("id")
        userField.isAccessible = true
        userField.set(user, 1L)

        val challenge = mockk<com.planit.entity.Challenge>()
        every { challenge.id } returns "CHL-1"
        every { challenge.title } returns "Challenge Title"
        
        certification = Certification(
            user = user,
            challenge = challenge,
            title = "Title",
            content = "Content"
        )
        val certField = Certification::class.java.getDeclaredField("id")
        certField.isAccessible = true
        certField.set(certification, 100L)
    }

    @Test
    @DisplayName("피드 조회 - 최신순 정렬")
    fun `getFeed should return latest sorted feed`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        
        every { userRepository.findByLoginId(userLoginId) } returns user
        every { followRepository.findFollowingIdsByFollowerId(1L) } returns listOf(2L)
        every { certificationRepository.findByUser_IdInOrderByCreatedAtDesc(any(), pageable) } returns PageImpl(listOf(certification))
        
        // Mock counts
        val countProjection = mockk<CertificationCountProjection>()
        every { countProjection.getCertificationId() } returns 100L
        every { countProjection.getCount() } returns 5L
        
        every { likeRepository.countByCertificationIdIn(any()) } returns listOf(countProjection)
        every { commentRepository.countByCertificationIdIn(any()) } returns listOf(countProjection)
        every { likeRepository.findLikedCertificationIds(any(), userLoginId) } returns listOf(100L)

        // When
        val result = feedService.getFeed(userLoginId, FeedSortType.LATEST, pageable)

        // Then
        assertNotNull(result)
        assertEquals(1, result.content.size)
        assertEquals(5L, result.content[0].likeCount)
        assertEquals(5L, result.content[0].commentCount)
        verify { certificationRepository.findByUser_IdInOrderByCreatedAtDesc(any(), pageable) }
    }

    @Test
    @DisplayName("피드 조회 - 인기순 정렬 (메모리 정렬)")
    fun `getFeed should return popular sorted feed`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        
        every { userRepository.findByLoginId(userLoginId) } returns user
        every { followRepository.findFollowingIdsByFollowerId(1L) } returns listOf(2L)
        // Note: Memory sort calls with unpaged/larger size
        every { certificationRepository.findByUser_IdInOrderByCreatedAtDesc(any(), any()) } returns PageImpl(listOf(certification))
        
        // Mock counts
        val countProjection = mockk<CertificationCountProjection>()
        every { countProjection.getCertificationId() } returns 100L
        every { countProjection.getCount() } returns 10L // Higher count
        
        every { likeRepository.countByCertificationIdIn(any()) } returns listOf(countProjection)
        every { commentRepository.countByCertificationIdIn(any()) } returns listOf(countProjection)
        every { likeRepository.findLikedCertificationIds(any(), userLoginId) } returns emptyList()

        // When
        val result = feedService.getFeed(userLoginId, FeedSortType.POPULAR, pageable)

        // Then
        assertNotNull(result)
        assertEquals(1, result.content.size)
        verify { certificationRepository.findByUser_IdInOrderByCreatedAtDesc(any(), any()) }
    }
}
