package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.UserExperienceResponse
import com.planit.dto.UserLevelResponse
import com.planit.dto.UserPointResponse
import com.planit.dto.UserPointSummaryResponse
import com.planit.dto.UserProgressResponse
import com.planit.service.UserExperienceService
import com.planit.service.UserPointService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/points")
class PointController(
    private val userPointService: UserPointService,
    private val userExperienceService: UserExperienceService,
) {

  @GetMapping("/me")
  fun getMyPoints(authentication: Authentication): ResponseEntity<ApiResponse<UserPointSummaryResponse>> {
    val loginId = authentication.name
    val response = userPointService.getUserPointSummary(loginId)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  @GetMapping("/me/history")
  fun getMyPointHistory(
      authentication: Authentication,
      pageable: Pageable,
  ): ResponseEntity<ApiResponse<Page<UserPointResponse>>> {
    val loginId = authentication.name
    val response = userPointService.getUserPointHistory(loginId, pageable)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  @GetMapping("/experience/me")
  fun getMyExperience(authentication: Authentication): ResponseEntity<ApiResponse<UserLevelResponse>> {
    val loginId = authentication.name
    val response = userExperienceService.getUserLevel(loginId)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  @GetMapping("/experience/me/history")
  fun getMyExperienceHistory(
      authentication: Authentication,
      pageable: Pageable,
  ): ResponseEntity<ApiResponse<Page<UserExperienceResponse>>> {
    val loginId = authentication.name
    val response = userExperienceService.getUserExperienceHistory(loginId, pageable)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  @GetMapping("/me/progress")
  fun getMyProgress(authentication: Authentication): ResponseEntity<ApiResponse<UserProgressResponse>> {
    val loginId = authentication.name
    val response = userExperienceService.getUserProgress(loginId)
    return ResponseEntity.ok(ApiResponse.success(response))
  }
}
