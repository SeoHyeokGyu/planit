package com.planit.controller

import com.planit.dto.CustomUserDetails
import com.planit.service.FeedService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/feed")
class FeedController(
    private val feedService: FeedService,
) {

    /**
     * 실시간 피드 스트림에 구독합니다. (SSE)
     *
     * @param userDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return SseEmitter
     */
    @GetMapping("/stream", produces = ["text/event-stream;charset=UTF-8"])
    fun stream(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<SseEmitter> {
        val emitter = feedService.subscribe(userDetails.username)
        return ResponseEntity.ok(emitter)
    }

    /**
     * 사용자가 참여 중인 챌린지 기반의 피드를 조회합니다.
     *
     * @param userDetails 인증된 사용자 정보
     * @param pageable 페이징 정보
     * @param sort 정렬 기준 (latest, likes, comments) - 현재는 latest만 지원
     * @return 페이징된 피드 응답
     */
    @GetMapping
    fun getFeed(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PageableDefault(size = 10) pageable: Pageable,
        @RequestParam(defaultValue = "latest") sort: String
    ): ResponseEntity<Any> {
        val customPageable = createPageableWithSort(pageable, sort)
        val feedPage = feedService.getFeedForUser(userDetails.username, customPageable)
        return ResponseEntity.ok(feedPage)
    }

    /**
     * 사용자가 팔로우하는 사람들의 피드를 조회합니다.
     *
     * @param userDetails 인증된 사용자 정보
     * @param pageable 페이징 정보
     * @param sort 정렬 기준 (latest, likes, comments) - 현재는 latest만 지원
     * @return 페이징된 피드 응답
     */
    @GetMapping("/following")
    fun getFollowingFeed(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PageableDefault(size = 10) pageable: Pageable,
        @RequestParam(defaultValue = "latest") sort: String
    ): ResponseEntity<Any> {
        val customPageable = createPageableWithSort(pageable, sort)
        val feedPage = feedService.getFollowingFeed(userDetails.username, customPageable)
        return ResponseEntity.ok(feedPage)
    }

    private fun createPageableWithSort(pageable: Pageable, sort: String): Pageable {
        val sortBy = when (sort.lowercase()) {
            "latest" -> Sort.by(Sort.Direction.DESC, "createdAt")
            // TODO: 향후 좋아요, 댓글순 정렬 기능 추가
            // "likes" -> Sort.by(Sort.Direction.DESC, "likeCount")
            // "comments" -> Sort.by(Sort.Direction.DESC, "commentCount")
            else -> Sort.by(Sort.Direction.DESC, "createdAt")
        }
        return PageRequest.of(pageable.pageNumber, pageable.pageSize, sortBy)
    }
}