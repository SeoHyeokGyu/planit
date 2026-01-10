package com.planit.controller

import com.planit.dto.*
import com.planit.service.StreakService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/streaks")
class StreakController(
    private val streakService: StreakService
) {

    /**
     * 특정 챌린지의 스트릭 조회
     * GET /api/streaks/{challengeId}?loginId=xxx
     */
    @GetMapping("/{challengeId}")
    fun getStreak(
        @PathVariable challengeId: String,
        @RequestParam loginId: String
    ): ResponseEntity<ApiResponse<StreakResponse>> {
        val streak = streakService.getStreak(challengeId, loginId)
        return ResponseEntity.ok(ApiResponse.success(streak))
    }

    /**
     * 사용자의 모든 스트릭 조회
     * GET /api/streaks?loginId=xxx
     */
    @GetMapping
    fun getAllStreaks(
        @RequestParam loginId: String
    ): ResponseEntity<ApiResponse<StreakSummaryResponse>> {
        val summary = streakService.getAllStreaks(loginId)
        return ResponseEntity.ok(ApiResponse.success(summary))
    }

    /**
     * 인증 성공 시 스트릭 기록
     * POST /api/streaks/{challengeId}/record
     */
    @PostMapping("/{challengeId}/record")
    fun recordCertification(
        @PathVariable challengeId: String,
        @RequestParam loginId: String
    ): ResponseEntity<ApiResponse<StreakResponse>> {
        val streak = streakService.recordCertification(challengeId, loginId)
        return ResponseEntity.ok(ApiResponse.success(streak))
    }

    /**
     * 잔디 캘린더 데이터 조회 (최근 N일)
     * GET /api/streaks/calendar?loginId=xxx&days=30
     */
    @GetMapping("/calendar")
    fun getActivityCalendar(
        @RequestParam loginId: String,
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<ApiResponse<ActivityCalendarResponse>> {
        val calendar = streakService.getActivityCalendar(loginId, days)
        return ResponseEntity.ok(ApiResponse.success(calendar))
    }

    /**
     * 스트릭 통계 조회 (일별/주별/월별)
     * GET /api/streaks/statistics?loginId=xxx&period=daily&days=30
     */
    @GetMapping("/statistics")
    fun getStreakStatistics(
        @RequestParam loginId: String,
        @RequestParam(defaultValue = "daily") period: String,
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<ApiResponse<StreakStatisticsResponse>> {
        val statistics = streakService.getStreakStatistics(loginId, period, days)
        return ResponseEntity.ok(ApiResponse.success(statistics))
    }

    /**
     * 챌린지별 스트릭 리더보드
     * GET /api/streaks/{challengeId}/leaderboard?limit=10
     */
    @GetMapping("/{challengeId}/leaderboard")
    fun getStreakLeaderboard(
        @PathVariable challengeId: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<ApiResponse<StreakLeaderboardResponse>> {
        val leaderboard = streakService.getStreakLeaderboard(challengeId, limit)
        return ResponseEntity.ok(ApiResponse.success(leaderboard))
    }
}