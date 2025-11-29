package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.CertificationResponse
import com.planit.dto.CustomUserDetails
import com.planit.service.FeedService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 팔로잉 피드(Following Feed) 관련 API를 제공하는 컨트롤러입니다.
 * 사용자가 팔로우하는 사람들의 최신 인증 목록을 조회합니다.
 */
@RestController
@RequestMapping("/api/feed")
class FeedController(
    private val feedService: FeedService
) {
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
}
