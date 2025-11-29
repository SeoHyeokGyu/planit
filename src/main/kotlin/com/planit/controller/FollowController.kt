package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.CustomUserDetails
import com.planit.dto.UserProfileResponse
import com.planit.service.FollowService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * 팔로우(Follow) 관련 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/follows")
class FollowController(
    private val followService: FollowService,
) {

  /**
   * 특정 사용자를 팔로우합니다.
   * @param userDetails 현재 로그인한 사용자 정보
   * @param followingLoginId 팔로우 대상 사용자의 로그인 ID
   * @return 성공 응답
   */
  @PostMapping("/{followingLoginId}")
  fun follow(
      @AuthenticationPrincipal userDetails: CustomUserDetails, // 현재 로그인한 사용자 (loginId 포함)
      @PathVariable followingLoginId: String, // 팔로우 대상 사용자의 로그인ID
  ): ResponseEntity<ApiResponse<Unit>> {
    followService.follow(userDetails.username, followingLoginId)
    return ResponseEntity.ok(ApiResponse.success())
  }

  /**
   * 특정 사용자를 언팔로우합니다.
   * @param userDetails 현재 로그인한 사용자 정보
   * @param followingLoginId 언팔로우 대상 사용자의 로그인 ID
   * @return 성공 응답
   */
  @DeleteMapping("/{followingLoginId}")
  fun unfollow(
      @AuthenticationPrincipal userDetails: CustomUserDetails,
      @PathVariable followingLoginId: String, // 팔로우 대상 사용자의 로그인ID
  ): ResponseEntity<ApiResponse<Unit>> {
    followService.unfollow(userDetails.username, followingLoginId)
    return ResponseEntity.ok(ApiResponse.success())
  }

  /**
   * 특정 사용자의 팔로워 수를 조회합니다.
   * @param userLoginId 팔로워 수를 조회할 사용자의 로그인 ID
   * @return 팔로워 수
   */
  @GetMapping("/{userLoginId}/follower-count")
  fun getFollowerCount(@PathVariable userLoginId: String): ResponseEntity<ApiResponse<Long>> {
    val followerCount = followService.getFollowerCount(userLoginId)
    return ResponseEntity.ok(ApiResponse.success(followerCount))
  }

  /**
   * 특정 사용자의 팔로잉 수를 조회합니다.
   * @param userLoginId 팔로잉 수를 조회할 사용자의 로그인 ID
   * @return 팔로잉 수
   */
  @GetMapping("/{userLoginId}/following-count")
  fun getFollowingCount(@PathVariable userLoginId: String): ResponseEntity<ApiResponse<Long>> {
    val followingCount = followService.getFollowingCount(userLoginId)
    return ResponseEntity.ok(ApiResponse.success(followingCount))
  }

  /**
   * 특정 사용자의 팔로워 목록을 페이징하여 조회합니다.
   * @param userLoginId 팔로워 목록을 조회할 사용자의 로그인 ID
   * @param pageable 페이징 정보 (기본 20개)
   * @return 페이징된 팔로워 사용자 목록
   */
  @GetMapping("/{userLoginId}/followers")
  fun getFollowers(
      @PathVariable userLoginId: String,
      @PageableDefault(size = 20) pageable: Pageable
  ): ResponseEntity<ApiResponse<List<UserProfileResponse>>> {
      val userPage = followService.getFollowers(userLoginId, pageable)
      return ResponseEntity.ok(ApiResponse.pagedSuccess(userPage.content, userPage))
  }

  /**
   * 특정 사용자의 팔로잉 목록을 페이징하여 조회합니다.
   * @param userLoginId 팔로잉 목록을 조회할 사용자의 로그인 ID
   * @param pageable 페이징 정보 (기본 20개)
   * @return 페이징된 팔로잉 사용자 목록
   */
  @GetMapping("/{userLoginId}/followings")
  fun getFollowings(
      @PathVariable userLoginId: String,
      @PageableDefault(size = 20) pageable: Pageable
  ): ResponseEntity<ApiResponse<List<UserProfileResponse>>> {
      val userPage = followService.getFollowings(userLoginId, pageable)
      return ResponseEntity.ok(ApiResponse.pagedSuccess(userPage.content, userPage))
  }
}
