package com.planit.service

import com.planit.entity.Certification
import com.planit.entity.Like
import com.planit.entity.User
import com.planit.enums.NotificationType
import com.planit.exception.CertificationNotFoundException
import com.planit.exception.UserNotFoundException
import com.planit.repository.CertificationRepository
import com.planit.repository.LikeRepository
import com.planit.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class LikeServiceTest {

    @MockK
    private lateinit var likeRepository: LikeRepository

    @MockK
    private lateinit var certificationRepository: CertificationRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var notificationService: NotificationService

    @InjectMockKs
    private lateinit var likeService: LikeService

    private val userLoginId = "liker"
    private lateinit var liker: User
    private lateinit var owner: User
    private lateinit var certification: Certification

    @BeforeEach
    fun setUp() {
        liker = User(loginId = userLoginId, password = "p", nickname = "Liker")
        val likerField = User::class.java.getDeclaredField("id")
        likerField.isAccessible = true
        likerField.set(liker, 1L)

        owner = User(loginId = "owner", password = "p", nickname = "Owner")
        val ownerField = User::class.java.getDeclaredField("id")
        ownerField.isAccessible = true
        ownerField.set(owner, 2L)

        val challenge = mockk<com.planit.entity.Challenge>()
        certification = Certification(user = owner, challenge = challenge, title = "T", content = "C")
        val certField = Certification::class.java.getDeclaredField("id")
        certField.isAccessible = true
        certField.set(certification, 100L)
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요 추가 및 알림 전송")
    fun `toggleLike should add like and notify`() {
        // Given
        every { userRepository.findByLoginId(userLoginId) } returns liker
        every { certificationRepository.findById(100L) } returns Optional.of(certification)
        every { likeRepository.findByCertificationIdAndUserLoginId(100L, userLoginId) } returns null
        every { likeRepository.save(any()) } returns mockk<Like>()
        every { notificationService.sendNotification(any()) } just Runs

        // When
        val result = likeService.toggleLike(100L, userLoginId)

        // Then
        assertTrue(result)
        verify { likeRepository.save(any()) }
        verify { notificationService.sendNotification(match { it.type == NotificationType.LIKE }) }
    }

    @Test
    @DisplayName("좋아요 토글 - 본인 게시글 좋아요 시 알림 전송 안 함")
    fun `toggleLike should add like but not notify if owner`() {
        // Given
        val ownerLoginId = "owner"
        every { userRepository.findByLoginId(ownerLoginId) } returns owner
        every { certificationRepository.findById(100L) } returns Optional.of(certification)
        every { likeRepository.findByCertificationIdAndUserLoginId(100L, ownerLoginId) } returns null
        every { likeRepository.save(any()) } returns mockk<Like>()

        // When
        val result = likeService.toggleLike(100L, ownerLoginId)

        // Then
        assertTrue(result)
        verify { likeRepository.save(any()) }
        verify(exactly = 0) { notificationService.sendNotification(any()) }
    }

    @Test
    @DisplayName("좋아요 토글 - 닉네임 없는 사용자 좋아요")
    fun `toggleLike should handle null nickname for notification`() {
        // Given
        val likerNoNick = User("nonick", "p", null)
        val likerNoNickField = User::class.java.getDeclaredField("id")
        likerNoNickField.isAccessible = true
        likerNoNickField.set(likerNoNick, 99L)
        
        every { userRepository.findByLoginId("nonick") } returns likerNoNick
        every { certificationRepository.findById(100L) } returns Optional.of(certification)
        every { likeRepository.findByCertificationIdAndUserLoginId(100L, "nonick") } returns null
        every { likeRepository.save(any()) } returns mockk<Like>()
        every { notificationService.sendNotification(any()) } just Runs

        // When
        likeService.toggleLike(100L, "nonick")

        // Then
        verify { notificationService.sendNotification(match { it.message.contains("nonick") }) }
    }
}
