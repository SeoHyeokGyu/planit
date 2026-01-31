package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.CustomUserDetails
import com.planit.dto.UserDashboardStats
import com.planit.dto.UserDeleteRequest
import com.planit.dto.UserPasswordUpdateRequest
import com.planit.dto.UserProfileResponse
import com.planit.dto.UserUpdateRequest
import com.planit.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(val userService: UserService) {

  @GetMapping("/me")
  fun getMyProfile(
      // SecurityContext에서 principal(CustomUserDetails)을 바로 주입받음
      @AuthenticationPrincipal userDetails: CustomUserDetails
  ): ResponseEntity<ApiResponse<UserProfileResponse>> {

    // userDetails 객체에서 User 엔티티와 ID를 편하게 꺼내 씀
    val profile = UserProfileResponse.of(userDetails.user)

    return ResponseEntity.ok(ApiResponse.success(profile))
  }

  @GetMapping("/me/stats")
  fun getMyStats(
      @AuthenticationPrincipal userDetails: CustomUserDetails
  ): ResponseEntity<ApiResponse<UserDashboardStats>> {
    val stats = userService.getDashboardStats(userDetails.user.loginId)
    return ResponseEntity.ok(ApiResponse.success(stats))
  }

  @PatchMapping("/me/password")
  fun updateMyPassword(
      @AuthenticationPrincipal userDetails: CustomUserDetails,
      @Valid @RequestBody request: UserPasswordUpdateRequest,
  ): ResponseEntity<ApiResponse<Unit>> {
    userService.updatePassword(userDetails.user.loginId, request)
    return ResponseEntity.ok(ApiResponse.success())
  }

  @PutMapping("/me/update-profile")
  fun updateMyProfile(
      @AuthenticationPrincipal userDetails: CustomUserDetails,
      @Valid @RequestBody request: UserUpdateRequest,
  ): ResponseEntity<ApiResponse<UserProfileResponse>> {
    val updatedProfile = userService.updateUser(userDetails.user, request)
    return ResponseEntity.ok(ApiResponse.success(updatedProfile))
  }

  @GetMapping("/{loginId}/profile")
  fun getUserProfile(
      @PathVariable loginId: String
  ): ResponseEntity<ApiResponse<UserProfileResponse>> {
    val profile = userService.getUserProfileByLoginId(loginId)
    return ResponseEntity.ok(ApiResponse.success(profile))
  }

  @GetMapping("/search")
  fun searchUsers(
      @RequestParam keyword: String,
      @PageableDefault(size = 20) pageable: Pageable
  ): ResponseEntity<ApiResponse<List<UserProfileResponse>>> {
    val usersPage = userService.searchUsers(keyword, pageable)
    return ResponseEntity.ok(ApiResponse.pagedSuccess(usersPage.content, usersPage))
  }

  /**
   * 랜덤 사용자 목록 조회
   * 피드 추천 사용자 기능에서 사용
   */
  @GetMapping("/random")
  fun getRandomUsers(
      @RequestParam(defaultValue = "3") size: Int
  ): ResponseEntity<ApiResponse<List<UserProfileResponse>>> {
    val users = userService.getRandomUsers(size)
    return ResponseEntity.ok(ApiResponse.success(users))
  }

  @DeleteMapping("/me")
  fun deleteMyAccount(
      @AuthenticationPrincipal userDetails: CustomUserDetails,
      @Valid @RequestBody request: UserDeleteRequest
  ): ResponseEntity<ApiResponse<Unit>> {
    userService.deleteUser(userDetails.username, request)
    return ResponseEntity.ok(ApiResponse.success())
  }
}
