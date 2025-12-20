package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.CustomUserDetails
import com.planit.dto.NotificationCreateRequest
import com.planit.dto.NotificationDto
import com.planit.dto.NotificationRequest
import com.planit.dto.NotificationResponse
import com.planit.dto.UnreadCountResponse
import com.planit.enums.NotificationType
import com.planit.service.NotificationService
import java.time.LocalDateTime
import java.util.*
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api")
class NotificationController(private val notificationService: NotificationService) {

  // ==================== SSE 구독 ====================

  /**
   * SSE 실시간 알림 구독
   */
  @GetMapping(
      "/subscribe",
      produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
  )
  fun subscribe(@AuthenticationPrincipal userDetails: CustomUserDetails): SseEmitter {
    return notificationService.subscribe(userDetails.username)
  }

  // ==================== 테스트 API ====================

  /**
   * 테스트용 알림 전송 API (SSE)
   */
  @PostMapping("/subscribe/test")
  fun sendTestNotification(
      @RequestBody request: NotificationRequest
  ): ResponseEntity<ApiResponse<Unit>> {
    val notification =
        NotificationDto(
            id = UUID.randomUUID().toString(),
            type = request.type,
            message = request.message,
            createdAt = LocalDateTime.now(),
        )

    notificationService.sendNotification(request.userLoginId, notification)

    return ResponseEntity.ok(ApiResponse.success())
  }

  // ==================== DB 기반 알림 API ====================

  /**
   * 알림 목록 조회 (페이징, 필터링)
   */
  @GetMapping("/notifications")
  fun getNotifications(
      @AuthenticationPrincipal userDetails: CustomUserDetails,
      @RequestParam(required = false) isRead: Boolean?,
      @RequestParam(required = false) type: NotificationType?,
      @PageableDefault(size = 20) pageable: Pageable
  ): ResponseEntity<ApiResponse<List<NotificationResponse>>> {
    val notificationPage = notificationService.getNotifications(
        userDetails.username,
        isRead,
        type,
        pageable
    )
    return ResponseEntity.ok(ApiResponse.pagedSuccess(notificationPage.content, notificationPage))
  }

  /**
   * 읽지 않은 알림 개수 조회
   */
  @GetMapping("/notifications/unread-count")
  fun getUnreadCount(
      @AuthenticationPrincipal userDetails: CustomUserDetails
  ): ResponseEntity<ApiResponse<UnreadCountResponse>> {
    val count = notificationService.getUnreadCount(userDetails.username)
    return ResponseEntity.ok(ApiResponse.success(count))
  }

  /**
   * 특정 알림 읽음 처리
   */
  @PatchMapping("/notifications/{notificationId}/read")
  fun markAsRead(
      @AuthenticationPrincipal userDetails: CustomUserDetails,
      @PathVariable notificationId: Long
  ): ResponseEntity<ApiResponse<Unit>> {
    notificationService.markAsRead(notificationId, userDetails.username)
    return ResponseEntity.ok(ApiResponse.success())
  }

  /**
   * 모든 알림 일괄 읽음 처리
   */
  @PatchMapping("/notifications/read-all")
  fun markAllAsRead(
      @AuthenticationPrincipal userDetails: CustomUserDetails
  ): ResponseEntity<ApiResponse<Int>> {
    val updatedCount = notificationService.markAllAsRead(userDetails.username)
    return ResponseEntity.ok(ApiResponse.success(updatedCount))
  }

  /**
   * 특정 알림 삭제
   */
  @DeleteMapping("/notifications/{notificationId}")
  fun deleteNotification(
      @AuthenticationPrincipal userDetails: CustomUserDetails,
      @PathVariable notificationId: Long
  ): ResponseEntity<ApiResponse<Unit>> {
    notificationService.deleteNotification(notificationId, userDetails.username)
    return ResponseEntity.ok(ApiResponse.success())
  }

  /**
   * 읽은 알림 일괄 삭제
   */
  @DeleteMapping("/notifications/read")
  fun deleteAllRead(
      @AuthenticationPrincipal userDetails: CustomUserDetails
  ): ResponseEntity<ApiResponse<Int>> {
    val deletedCount = notificationService.deleteAllRead(userDetails.username)
    return ResponseEntity.ok(ApiResponse.success(deletedCount))
  }
}
