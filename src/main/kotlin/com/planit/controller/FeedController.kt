package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.CertificationResponse
import com.planit.dto.CustomUserDetails
import com.planit.dto.FeedResponse
import com.planit.enums.FeedType
import com.planit.service.FeedService
import com.planit.service.SseEmitterManager
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.LocalDateTime

/**
 * 팔로잉 피드(Following Feed) 및 실시간 SSE 스트림 관련 API를 제공하는 컨트롤러입니다.
 * 사용자가 팔로우하는 사람들의 최신 인증 목록을 조회하고, 실시간 이벤트를 전송합니다.
 */
@RestController
@RequestMapping("/api/feed")
class FeedController(
    private val feedService: FeedService,
    private val sseEmitterManager: SseEmitterManager
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * SSE 스트림 연결 엔드포인트
     * 클라이언트가 이 엔드포인트에 연결하면 실시간으로 피드 이벤트를 수신할 수 있습니다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @return SSE Emitter
     */
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamFeed(@AuthenticationPrincipal userDetails: CustomUserDetails): SseEmitter {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        logger.info("SSE connection requested by user: $userId (${userDetails.username})")

        val emitter = sseEmitterManager.addEmitter(userId)

        // 초기 연결 메시지 전송
        try {
            emitter.send(
                SseEmitter.event()
                    .name("connected")
                    .data(mapOf(
                        "message" to "Connected to feed stream",
                        "userId" to userId,
                        "timestamp" to LocalDateTime.now()
                    ))
            )
            logger.info("Initial connection message sent to user: $userId")
        } catch (e: Exception) {
            logger.error("Failed to send initial connection message to user: $userId", e)
            sseEmitterManager.removeEmitter(userId)
            throw e
        }

        return emitter
    }

    /**
     * 현재 로그인한 사용자의 팔로잉 피드를 조회합니다.
     * 팔로우하는 사용자들의 최신 인증 목록을 페이징하여 반환합니다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param pageable 페이징 정보 (기본 20개)
     * @return 페이징된 인증 응답 DTO 목록
     */
    @GetMapping
    fun getFollowingFeed(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<List<CertificationResponse>>> {
        val certificationPage = feedService.getFollowingFeed(userDetails.username, pageable)
        val certificationResponses = certificationPage.content.map { CertificationResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.pagedSuccess(certificationResponses, certificationPage))
    }

    /**
     * 피드 목록을 조회합니다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param pageable 페이징 정보
     * @param type 필터링할 피드 타입 (nullable)
     * @return 페이징된 피드 응답 DTO 목록
     */
    @GetMapping("/feeds")
    fun getFeeds(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PageableDefault(size = 20) pageable: Pageable,
        @RequestParam(required = false) type: FeedType?
    ): ResponseEntity<ApiResponse<List<FeedResponse>>> {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        val feedPage = feedService.getFeeds(userId, pageable, type)
        return ResponseEntity.ok(ApiResponse.pagedSuccess(feedPage.content, feedPage))
    }

    /**
     * 팔로잉하는 사용자들의 피드를 조회합니다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param pageable 페이징 정보
     * @return 페이징된 피드 응답 DTO 목록
     */
    @GetMapping("/following")
    fun getFollowingFeeds(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<List<FeedResponse>>> {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        val feedPage = feedService.getFollowingFeeds(userId, pageable)
        return ResponseEntity.ok(ApiResponse.pagedSuccess(feedPage.content, feedPage))
    }

    /**
     * 읽지 않은 피드 수를 조회합니다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 읽지 않은 피드 수
     */
    @GetMapping("/unread-count")
    fun getUnreadCount(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<Map<String, Long>> {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        val count = feedService.getUnreadCount(userId)
        return ResponseEntity.ok(mapOf("unreadCount" to count))
    }

    /**
     * 특정 피드를 읽음 처리합니다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param feedId 읽음 처리할 피드 ID
     * @return 성공 응답
     */
    @PostMapping("/{feedId}/read")
    fun markAsRead(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable feedId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        feedService.markAsRead(userId, feedId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    /**
     * 사용자의 모든 피드를 읽음 처리합니다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/read-all")
    fun markAllAsRead(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<ApiResponse<Unit>> {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        feedService.markAllAsRead(userId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    /**
     * 현재 활성화된 SSE 연결 수를 조회합니다. (디버깅/모니터링 용도)
     * @return 활성 연결 수
     */
    @GetMapping("/connections")
    fun getConnectionCount(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<Map<String, Any>> {
        val count = sseEmitterManager.getConnectionCount()
        val isConnected = sseEmitterManager.isConnected(userDetails.user.id ?: 0L)
        return ResponseEntity.ok(mapOf(
            "totalConnections" to count,
            "isConnected" to isConnected
        ))
    }
}
