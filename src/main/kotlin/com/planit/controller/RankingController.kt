package com.planit.controller

import com.planit.dto.AllMyRankingsResponse
import com.planit.dto.ApiResponse
import com.planit.dto.RankingListResponse
import com.planit.service.RankingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Ranking", description = "랭킹 관련 API")
@RestController
@RequestMapping("/api/rankings")
class RankingController(
    private val rankingService: RankingService
) {
    companion object {
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_SIZE = 20
        private const val MAX_SIZE = 100
    }

    // ==================== 통합 API (type 파라미터 기반) ====================

    @Operation(
        summary = "랭킹 조회 (통합)",
        description = """
            타입별 페이지네이션된 랭킹을 조회합니다.
            - type: weekly (주간), monthly (월간), all (전체)
            - page: 페이지 번호 (0부터 시작)
            - size: 페이지 크기 (기본값: 20, 최대: 100)

            응답에는 rank, userId, loginId, nickname(username), score가 포함됩니다.
        """
    )
    @GetMapping
    fun getRanking(
        @Parameter(description = "랭킹 타입 (weekly, monthly, all)", required = true)
        @RequestParam type: String,
        @Parameter(description = "페이지 번호 (0부터 시작)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<RankingListResponse>> {
        val validSize = size.coerceIn(1, MAX_SIZE)
        val validPage = maxOf(0, page)
        val response = rankingService.getRankingByType(type, validPage, validSize)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    // ==================== 개별 타입 API (레거시 호환) ====================

    @Operation(summary = "주간 랭킹 조회", description = "이번 주 포인트 랭킹을 페이지네이션하여 조회합니다.")
    @GetMapping("/weekly")
    fun getWeeklyRanking(
        @Parameter(description = "페이지 번호 (0부터 시작)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<RankingListResponse>> {
        val validSize = size.coerceIn(1, MAX_SIZE)
        val validPage = maxOf(0, page)
        val response = rankingService.getWeeklyRanking(validPage, validSize)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(summary = "월간 랭킹 조회", description = "이번 달 포인트 랭킹을 페이지네이션하여 조회합니다.")
    @GetMapping("/monthly")
    fun getMonthlyRanking(
        @Parameter(description = "페이지 번호 (0부터 시작)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<RankingListResponse>> {
        val validSize = size.coerceIn(1, MAX_SIZE)
        val validPage = maxOf(0, page)
        val response = rankingService.getMonthlyRanking(validPage, validSize)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(summary = "전체 랭킹 조회", description = "누적 포인트 랭킹을 페이지네이션하여 조회합니다.")
    @GetMapping("/alltime")
    fun getAlltimeRanking(
        @Parameter(description = "페이지 번호 (0부터 시작)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<RankingListResponse>> {
        val validSize = size.coerceIn(1, MAX_SIZE)
        val validPage = maxOf(0, page)
        val response = rankingService.getAlltimeRanking(validPage, validSize)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    // ==================== 사용자별 랭킹 조회 ====================

    @Operation(summary = "내 랭킹 조회", description = "현재 사용자의 모든 기간별 랭킹 정보를 조회합니다.")
    @GetMapping("/me")
    fun getMyRankings(
        authentication: Authentication
    ): ResponseEntity<ApiResponse<AllMyRankingsResponse>> {
        val loginId = authentication.name
        val response = rankingService.getMyRankings(loginId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
