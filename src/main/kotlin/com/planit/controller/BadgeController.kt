package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.BadgeResponse
import com.planit.service.BadgeService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/badges")
class BadgeController(private val badgeService: BadgeService) {

  @GetMapping
  fun getAllBadges(
    @AuthenticationPrincipal userDetails: UserDetails
  ): ResponseEntity<ApiResponse<List<BadgeResponse>>> {
    val userLoginId = userDetails.username
    return ResponseEntity.ok(ApiResponse.success(badgeService.getAllBadges(userLoginId)))
  }

  @GetMapping("/my")
  fun getMyBadges(
    @AuthenticationPrincipal userDetails: UserDetails
  ): ResponseEntity<ApiResponse<List<BadgeResponse>>> {
    val userLoginId = userDetails.username
    return ResponseEntity.ok(ApiResponse.success(badgeService.getMyBadges(userLoginId)))
  }

  @GetMapping("/user/{loginId}")
  fun getUserBadges(
    @PathVariable loginId: String
  ): ResponseEntity<ApiResponse<List<BadgeResponse>>> {
    return ResponseEntity.ok(ApiResponse.success(badgeService.getAllBadges(loginId)))
  }
}
