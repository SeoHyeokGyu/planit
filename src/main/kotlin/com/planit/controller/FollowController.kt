package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.CustomUserDetails
import com.planit.service.FollowService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/follows")
class FollowController(
    private val followService: FollowService,
) {

  @PostMapping("/{followingLoginId}")
  fun follow(
      @AuthenticationPrincipal userDetails: CustomUserDetails, // 현재 로그인한 사용자 (loginId 포함)
      @PathVariable followingLoginId: String, // 팔로우 대상 사용자의 로그인ID
  ): ResponseEntity<ApiResponse<Unit>> {
    followService.follow(userDetails.username, followingLoginId)
    return ResponseEntity.ok(ApiResponse.success())
  }

  @DeleteMapping("/{followingLoginId}")
  fun unfollow(
      @AuthenticationPrincipal userDetails: CustomUserDetails,
      @PathVariable followingLoginId: String, // 팔로우 대상 사용자의 로그인ID
  ): ResponseEntity<ApiResponse<Unit>> {
    followService.unfollow(userDetails.username, followingLoginId)
    return ResponseEntity.ok(ApiResponse.success())
  }

  @GetMapping("/{userLoginId}/follower-count")
  fun getFollowerCount(@PathVariable userLoginId: String): ResponseEntity<ApiResponse<Long>> {
    val followerCount = followService.getFollowerCount(userLoginId)
    return ResponseEntity.ok(ApiResponse.success(followerCount))
  }

  @GetMapping("/{userLoginId}/following-count")
  fun getFollowingCount(@PathVariable userLoginId: String): ResponseEntity<ApiResponse<Long>> {
    val followingCount = followService.getFollowingCount(userLoginId)
    return ResponseEntity.ok(ApiResponse.success(followingCount))
  }
}
