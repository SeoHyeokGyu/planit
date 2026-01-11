package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.PointDeductRequest
import com.planit.dto.PointStatisticsResponse
import com.planit.dto.UserPointResponse
import com.planit.dto.UserPointSummaryResponse
import com.planit.service.UserPointService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/points")
class PointController(
    private val userPointService: UserPointService,
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

  @PostMapping("/me/deduct")
  fun deductMyPoints(
      authentication: Authentication,
      @RequestBody request: PointDeductRequest,
  ): ResponseEntity<ApiResponse<UserPointSummaryResponse>> {
    val loginId = authentication.name
    userPointService.subtractPoint(loginId, request.points, request.reason)
    val response = userPointService.getUserPointSummary(loginId)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  /**
   * 사용자의 포인트 통계를 조회합니다.
   * @param authentication 인증 정보
   * @param startDate 시작 날짜 (YYYY-MM-DD)
   * @param endDate 종료 날짜 (YYYY-MM-DD)
   * @return 포인트 통계 데이터
   */
  @GetMapping("/me/statistics")
  fun getMyPointStatistics(
      authentication: Authentication,
      @RequestParam startDate: String,
      @RequestParam endDate: String,
  ): ResponseEntity<ApiResponse<PointStatisticsResponse>> {
    val loginId = authentication.name
    val start = LocalDate.parse(startDate)
    val end = LocalDate.parse(endDate)

    // Validate date range
    if (start.isAfter(end)) {
      throw IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.")
    }

    // Limit to 1 year maximum
    val daysBetween = ChronoUnit.DAYS.between(start, end)
    if (daysBetween > 365) {
      throw IllegalArgumentException("조회 기간은 최대 1년까지 가능합니다.")
    }

    val response = userPointService.getPointStatistics(loginId, start, end)
    return ResponseEntity.ok(ApiResponse.success(response))
  }
}
