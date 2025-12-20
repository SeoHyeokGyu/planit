package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.CustomUserDetails
import com.planit.dto.NotificationDto
import com.planit.dto.NotificationRequest
import com.planit.service.NotificationService
import java.time.LocalDateTime
import java.util.*
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/subscribe")
class NotificationController(private val notificationService: NotificationService) {

  @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
  fun subscribe(@AuthenticationPrincipal userDetails: CustomUserDetails): SseEmitter {

    return notificationService.subscribe(userDetails.username)
  }

  /** 테스트용 알림 전송 API */
  @PostMapping("/test")
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
}
