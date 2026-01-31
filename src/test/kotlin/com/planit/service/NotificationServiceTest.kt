package com.planit.service

import com.planit.dto.NotificationResponse
import com.planit.entity.Notification
import com.planit.entity.User
import com.planit.enums.NotificationType
import com.planit.repository.NotificationRepository
import com.planit.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class NotificationServiceTest {

    @MockK
    private lateinit var notificationRepository: NotificationRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var cacheManager: CacheManager

    @MockK
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @InjectMockKs
    private lateinit var notificationService: NotificationService

    private val userLoginId = "receiver"
    private lateinit var receiver: User
    private lateinit var sender: User

    @BeforeEach
    fun setUp() {
        receiver = User(loginId = userLoginId, password = "password", nickname = "Receiver")
        val receiverField = User::class.java.getDeclaredField("id")
        receiverField.isAccessible = true
        receiverField.set(receiver, 1L)

        sender = User(loginId = "sender", password = "password", nickname = "Sender")
        val senderField = User::class.java.getDeclaredField("id")
        senderField.isAccessible = true
        senderField.set(sender, 2L)
    }

    @Test
    @DisplayName("SSE 구독 성공")
    fun `subscribe should return SseEmitter`() {
        // When
        val emitter = notificationService.subscribe(userLoginId)

        // Then
        assertNotNull(emitter)
    }

    @Test
    @DisplayName("알림 전송 - Redis Pub/Sub 호출 확인")
    fun `sendNotification should call redisTemplate`() {
        // Given
        val notificationResponse = NotificationResponse(
            id = "1",
            receiverId = 1L,
            receiverLoginId = userLoginId,
            senderId = 2L,
            senderLoginId = "sender",
            senderNickname = "Sender",
            type = NotificationType.LIKE,
            message = "Liked your post",
            relatedId = null,
            relatedType = null,
            isRead = false,
            createdAt = LocalDateTime.now()
        )
        every { redisTemplate.convertAndSend(any(), any()) } returns 1L

        // When
        notificationService.sendNotification(notificationResponse)

        // Then
        verify { redisTemplate.convertAndSend(any(), notificationResponse) }
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공")
    fun `getNotifications should return page of notifications`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val notification = Notification(
            receiver = receiver,
            sender = sender,
            type = NotificationType.FOLLOW,
            message = "Started following you",
            relatedId = null,
            relatedType = null,
            isRead = false
        )
        // Set ID using reflection if needed, but here it seems we are using BaseEntity or direct field.
        // Actually, Notification has val id: Long = 0 which is GENERATED.
        
        every { userRepository.findByLoginId(userLoginId) } returns receiver
        every { 
            notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(1L, pageable) 
        } returns PageImpl(listOf(notification))

        // When
        val result = notificationService.getNotifications(userLoginId, pageable = pageable)

        // Then
        assertNotNull(result)
        assertEquals(1, result.content.size)
        assertEquals("Started following you", result.content[0].message)
    }

    @Test
    @DisplayName("알림 목록 조회 - 다양한 필터 조합")
    fun `getNotifications should handle various filters`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        every { userRepository.findByLoginId(userLoginId) } returns receiver
        
        // Mock all finders
        every { notificationRepository.findAllByReceiverIdAndIsReadAndTypeOrderByCreatedAtDesc(any(), any(), any(), any()) } returns PageImpl(emptyList())
        every { notificationRepository.findAllByReceiverIdAndIsReadOrderByCreatedAtDesc(any(), any(), any()) } returns PageImpl(emptyList())
        every { notificationRepository.findAllByReceiverIdAndTypeOrderByCreatedAtDesc(any(), any(), any()) } returns PageImpl(emptyList())
        every { notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(any(), any()) } returns PageImpl(emptyList())

        // When
        notificationService.getNotifications(userLoginId, isRead = true, type = NotificationType.LIKE, pageable = pageable)
        notificationService.getNotifications(userLoginId, isRead = true, type = null, pageable = pageable)
        notificationService.getNotifications(userLoginId, isRead = null, type = NotificationType.LIKE, pageable = pageable)
        notificationService.getNotifications(userLoginId, isRead = null, type = null, pageable = pageable)

        // Then
        verify { notificationRepository.findAllByReceiverIdAndIsReadAndTypeOrderByCreatedAtDesc(any(), any(), any(), any()) }
        verify { notificationRepository.findAllByReceiverIdAndIsReadOrderByCreatedAtDesc(any(), any(), any()) }
        verify { notificationRepository.findAllByReceiverIdAndTypeOrderByCreatedAtDesc(any(), any(), any()) }
        verify { notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(any(), any()) }
    }

    @Test
    @DisplayName("알림 전송 - RedisTemplate 없을 때 로컬 전송")
    fun `sendNotification should fallback to local when redisTemplate is null`() {
        // Given
        val serviceNoRedis = NotificationService(notificationRepository, userRepository, cacheManager, null)
        val notificationResponse = NotificationResponse(
            id = "1", receiverId = 1L, receiverLoginId = userLoginId, senderId = 2L,
            senderLoginId = "sender", senderNickname = "Sender", type = NotificationType.LIKE,
            message = "Liked", relatedId = null, relatedType = null, isRead = false,
            createdAt = LocalDateTime.now()
        )

        // When
        serviceNoRedis.sendNotification(notificationResponse)

        // Then
        // Should execute sendToLocalEmitter logic (no exception)
    }

    @Test
    @DisplayName("리포지토리 또는 캐시 매니저가 null일 때의 분기 커버")
    fun `service should handle null dependencies gracefully`() {
        // Given
        val emptyService = NotificationService(null, null, null, null)
        
        // When & Then
        assertEquals(org.springframework.data.domain.Page.empty<NotificationResponse>(), emptyService.getNotifications("u", pageable = PageRequest.of(0, 10)))
        assertEquals(0L, emptyService.getUnreadCount("u").count)
        
        // These should return early without exception
        emptyService.markAsRead(1L, "u")
        assertEquals(0, emptyService.markAllAsRead("u"))
        emptyService.deleteNotification(1L, "u")
        assertEquals(0, emptyService.deleteAllRead("u"))
    }

    @Test
    @DisplayName("알림 읽음 처리 시 이미 읽었거나 권한 없을 때")
    fun `markAsRead should handle unauthorized or already read`() {
        // Given
        val notificationId = 100L
        val otherUser = User("other", "p", "n")
        val otherUserField = User::class.java.getDeclaredField("id")
        otherUserField.isAccessible = true
        otherUserField.set(otherUser, 99L)
        
        val notification = Notification(receiver = receiver, sender = sender, type = NotificationType.LIKE, message = "M")
        val field = Notification::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(notification, notificationId)

        every { userRepository.findByLoginId(userLoginId) } returns receiver
        every { notificationRepository.findById(notificationId) } returns Optional.of(notification)

        // Case 1: Unauthorized
        val attacker = User("attacker", "p", "n")
        val attackerField = User::class.java.getDeclaredField("id")
        attackerField.isAccessible = true
        attackerField.set(attacker, 666L)
        every { userRepository.findByLoginId("attacker") } returns attacker
        
        assertThrows(com.planit.exception.NotificationAccessForbiddenException::class.java) {
            notificationService.markAsRead(notificationId, "attacker")
        }

        // Case 2: Already read (should not call save or decrement cache)
        notification.markAsRead()
        notificationService.markAsRead(notificationId, userLoginId)
        verify(exactly = 0) { notificationRepository.save(any()) }
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회 - 캐시 미스 시 DB 조회")
    fun `getUnreadCount should query db when cache miss`() {
        // Given
        val cache = mockk<Cache>()
        every { userRepository.findByLoginId(userLoginId) } returns receiver
        every { cacheManager.getCache("unreadNotificationCount") } returns cache
        every { cache.get("1", Long::class.java) } returns null
        every { notificationRepository.countByReceiverIdAndIsRead(1L, false) } returns 10L
        every { cache.put("1", 10L) } just Runs

        // When
        val result = notificationService.getUnreadCount(userLoginId)

        // Then
        assertEquals(10L, result.count)
        verify { notificationRepository.countByReceiverIdAndIsRead(1L, false) }
        verify { cache.put("1", 10L) }
    }

    @Test
    @DisplayName("모두 읽음 처리 - 성공")
    fun `markAllAsRead should update all and clear cache`() {
        // Given
        val cache = mockk<Cache>()
        every { userRepository.findByLoginId(userLoginId) } returns receiver
        every { notificationRepository.markAllAsReadByReceiverId(1L) } returns 5
        every { cacheManager.getCache("unreadNotificationCount") } returns cache
        every { cache.put("1", 0L) } just Runs

        // When
        val count = notificationService.markAllAsRead(userLoginId)

        // Then
        assertEquals(5, count)
        verify { notificationRepository.markAllAsReadByReceiverId(1L) }
        verify { cache.put("1", 0L) }
    }

    @Test
    @DisplayName("알림 삭제 - 성공")
    fun `deleteNotification should delete from repository`() {
        // Given
        val notificationId = 100L
        val notification = Notification(
            receiver = receiver,
            sender = sender,
            type = NotificationType.STREAK,
            message = "To be deleted"
        )
        // Reflection to set ID if needed, but repository delete(entity) uses the entity
        
        every { userRepository.findByLoginId(userLoginId) } returns receiver
        every { notificationRepository.findById(notificationId) } returns Optional.of(notification)
        every { notificationRepository.delete(notification) } just Runs
        
        // Mock cache update since it decrements unread count if unread
        val cache = mockk<Cache>()
        every { cacheManager.getCache("unreadNotificationCount") } returns cache
        every { cache.get("1", Long::class.java) } returns 5L
        every { cache.put("1", 4L) } just Runs

        // When
        notificationService.deleteNotification(notificationId, userLoginId)

        // Then
        verify { notificationRepository.delete(notification) }
    }

    @Test
    @DisplayName("기타 알림 메서드들 커버리지 (Deprecated 포함)")
    fun `other notification methods coverage`() {
        // Given
        val request = com.planit.dto.NotificationCreateRequest(
            receiverLoginId = userLoginId,
            senderLoginId = "sender",
            type = NotificationType.LIKE,
            message = "M"
        )
        every { userRepository.findByLoginId(userLoginId) } returns receiver
        every { userRepository.findByLoginId("sender") } returns sender
        every { notificationRepository.save(any()) } returns mockk()
        every { notificationRepository.countByReceiverIdAndIsRead(any(), any()) } returns 5L
        
        val cache = mockk<Cache>()
        every { cacheManager.getCache(any()) } returns cache
        every { cache.get(any(), Long::class.java) } returns 5L
        every { cache.put(any(), any()) } just Runs
        every { redisTemplate.convertAndSend(any(), any()) } returns 1L

        // When
        notificationService.createNotification(request)
        notificationService.sendNotification(userLoginId, com.planit.dto.NotificationDto("1", "INFO", "M"))

        // Then
        verify { notificationRepository.save(any()) }
    }
}