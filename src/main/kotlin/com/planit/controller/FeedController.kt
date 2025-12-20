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

@RestController
@RequestMapping("/api")
class FeedController(private val feedService: FeedService) {

    /**
     * 팔로우하는 사람들의 최근 인증 피드를 조회합니다.
     * @param userDetails 현재 인증된 사용자 정보
     * @param pageable 페이징 정보 (기본값: 0페이지, 20개)
     * @return 페이징된 인증 피드
     */
    @GetMapping("/feed")
    fun getFeed(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<List<CertificationResponse>>> {
        val feedPage = feedService.getFeed(userDetails.username, pageable)
        return ResponseEntity.ok(ApiResponse.pagedSuccess(feedPage.content, feedPage))
    }
}
