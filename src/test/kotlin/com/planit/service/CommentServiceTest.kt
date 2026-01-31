package com.planit.service

import com.planit.dto.CommentCreateRequest
import com.planit.entity.Certification
import com.planit.entity.Comment
import com.planit.entity.User
import com.planit.repository.CertificationRepository
import com.planit.repository.CommentRepository
import com.planit.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class CommentServiceTest {

    @MockK
    private lateinit var commentRepository: CommentRepository

    @MockK
    private lateinit var certificationRepository: CertificationRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var notificationService: NotificationService

    @InjectMockKs
    private lateinit var commentService: CommentService

    private val userLoginId = "testuser"
    private lateinit var user: User
    private lateinit var certification: Certification

    @BeforeEach
    fun setUp() {
        user = User(loginId = userLoginId, password = "password", nickname = "TestUser")
        val userField = User::class.java.getDeclaredField("id")
        userField.isAccessible = true
        userField.set(user, 1L)

        // Mock certification with a different user as owner
        val owner = User(loginId = "owner", password = "pw", nickname = "Owner")
        val ownerField = User::class.java.getDeclaredField("id")
        ownerField.isAccessible = true
        ownerField.set(owner, 2L)

        val challenge = io.mockk.mockk<com.planit.entity.Challenge>()
        // If Challenge needs ID for equals/hashcode inside logic, we might need to set it, 
        // but for CommentService it's likely just passed through.
        
        certification = Certification(
            user = owner, 
            challenge = challenge, 
            title = "Cert Title",
            content = "Cert Content",
            photoUrl = "url"
        )
        val certField = Certification::class.java.getDeclaredField("id")
        certField.isAccessible = true
        certField.set(certification, 10L)
    }

    @Test
    @DisplayName("댓글 생성 성공 - 알림 전송")
    fun `createComment should save comment and send notification`() {
        // Given
        val request = CommentCreateRequest(content = "Nice work!")
        every { userRepository.findByLoginId(userLoginId) } returns user
        every { certificationRepository.findById(10L) } returns Optional.of(certification)
        every { commentRepository.save(any()) } answers {
            val arg = firstArg<Comment>()
            val field = Comment::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(arg, 123L)
            arg
        }
        every { notificationService.sendNotification(any<com.planit.dto.NotificationResponse>()) } just Runs

        // When
        val result = commentService.createComment(10L, request, userLoginId)

        // Then
        assertNotNull(result)
        assertEquals("Nice work!", result.content)
        verify { commentRepository.save(any()) }
        verify { notificationService.sendNotification(any<com.planit.dto.NotificationResponse>()) }
    }

    @Test
    @DisplayName("댓글 생성 - 본인 게시글이면 알림 전송 안함")
    fun `createComment should not send notification if owner`() {
        // Given
        val ownerLoginId = "owner"
        val request = CommentCreateRequest(content = "Self comment")
        
        // Use the owner user defined in setup
        val owner = certification.user
        every { userRepository.findByLoginId(ownerLoginId) } returns owner
        every { certificationRepository.findById(10L) } returns Optional.of(certification)
        every { commentRepository.save(any()) } answers {
            val arg = firstArg<Comment>()
            val field = Comment::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(arg, 123L)
            arg
        }

        // When
        commentService.createComment(10L, request, ownerLoginId)

        // Then
        verify { commentRepository.save(any()) }
        verify(exactly = 0) { notificationService.sendNotification(any<com.planit.dto.NotificationResponse>()) }
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    fun `getComments should return list`() {
        // Given
        val comment = Comment(user, certification, "Content")
        val field = Comment::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(comment, 100L)
        
        every { commentRepository.findByCertificationIdOrderByCreatedAtAsc(10L) } returns listOf(comment)

        // When
        val result = commentService.getComments(10L, userLoginId)

        // Then
        assertEquals(1, result.size)
        assertEquals("Content", result[0].content)
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    fun `deleteComment should delete if authorized`() {
        // Given
        val commentId = 100L
        val comment = Comment(user, certification, "Content")
        every { commentRepository.findById(commentId) } returns Optional.of(comment)
        every { commentRepository.delete(comment) } just Runs

        // When
        commentService.deleteComment(commentId, userLoginId)

        // Then
        verify { commentRepository.delete(comment) }
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없음")
    fun `deleteComment should throw if not owner`() {
        // Given
        val commentId = 100L
        val comment = Comment(user, certification, "Content") // Owner is 'testuser'
        every { commentRepository.findById(commentId) } returns Optional.of(comment)

        // When & Then
        assertThrows<IllegalArgumentException> {
            commentService.deleteComment(commentId, "otheruser")
        }
    }

    @Test
    @DisplayName("댓글 생성 실패 - 사용자 없음")
    fun `createComment should throw UserNotFoundException`() {
        every { userRepository.findByLoginId(any()) } returns null
        assertThrows<com.planit.exception.UserNotFoundException> {
            commentService.createComment(10L, CommentCreateRequest("C"), "u")
        }
    }

    @Test
    @DisplayName("댓글 생성 실패 - 인증 없음")
    fun `createComment should throw CertificationNotFoundException`() {
        every { userRepository.findByLoginId(any()) } returns user
        every { certificationRepository.findById(any()) } returns Optional.empty()
        assertThrows<com.planit.exception.CertificationNotFoundException> {
            commentService.createComment(999L, CommentCreateRequest("C"), "u")
        }
    }

    @Test
    @DisplayName("댓글 목록 조회 성공 - 닉네임 없는 사용자 및 로그아웃 유저")
    fun `getComments should handle null nickname and null currentLoginId`() {
        // Given
        val userNoNickname = User(loginId = "nonick", password = "p", nickname = null)
        val comment = Comment(userNoNickname, certification, "Content")
        val field = Comment::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(comment, 100L)
        
        every { commentRepository.findByCertificationIdOrderByCreatedAtAsc(10L) } returns listOf(comment)

        // When
        val result = commentService.getComments(10L, null)

        // Then
        assertEquals(1, result.size)
        assertEquals("nonick", result[0].authorNickname)
        assertFalse(result[0].isMyComment)
    }
}
