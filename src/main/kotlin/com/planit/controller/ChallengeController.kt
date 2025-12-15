package com.planit.controller

import com.planit.dto.*
import com.planit.service.ChallengeService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/challenge")
@Validated
class ChallengeController(
    private val challengeService: ChallengeService
) {

    /**
     * 챌린지 생성
     * POST /api/challenge
     */
    @PostMapping
    fun createChallenge(
        @Valid @RequestBody request: ChallengeRequest
    ): ResponseEntity<ApiResponse<ChallengeResponse>> {
        // request.loginId를 직접 사용
        val challenge = challengeService.createChallenge(request, request.loginId)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(challenge))
    }

    /**
     * 챌린지 상세 조회
     * GET /api/challenge/{challengeId}
     */
    @GetMapping("/{challengeId}")
    fun getChallengeById(@PathVariable challengeId: String): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.getChallengeById(challengeId)
        return ResponseEntity.ok(ApiResponse.success(challenge))
    }

    /**
     * 챌린지 목록 조회 (필터링)
     * GET /api/challenge
     */
    @GetMapping
    fun getChallenges(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) difficulty: String?,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<ApiResponse<List<ChallengeListResponse>>> {
        val request = ChallengeSearchRequest(
            keyword = keyword,
            category = category,
            difficulty = difficulty,
            status = status
        )

        // 디버깅 로그
        println("=== Challenge Search Request ===")
        println("keyword: $keyword")
        println("category: $category")
        println("difficulty: $difficulty")
        println("status: $status")

        val challenges = challengeService.getChallenges(request)

        println("Found ${challenges.size} challenges")

        return ResponseEntity.ok(ApiResponse.success(challenges))
    }

    /**
     * 챌린지 검색 (키워드)
     * GET /api/challenge/search
     */
    @GetMapping("/search")
    fun searchChallenges(
        @RequestParam keyword: String
    ): ResponseEntity<ApiResponse<List<ChallengeListResponse>>> {
        val challenges = challengeService.searchChallenges(keyword)
        return ResponseEntity.ok(ApiResponse.success(challenges))
    }

    /**
     * 챌린지 수정
     * PUT /api/challenge/{challengeId}
     */
    @PutMapping("/{challengeId}")
    fun updateChallenge(
        @PathVariable challengeId: String,
        @Valid @RequestBody request: ChallengeRequest
    ): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.updateChallenge(challengeId, request, request.loginId)
        return ResponseEntity.ok(ApiResponse.success(challenge))
    }

    /**
     * 챌린지 삭제
     * DELETE /api/challenge/{challengeId}
     */
    @DeleteMapping("/{challengeId}")
    fun deleteChallenge(
        @PathVariable challengeId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        challengeService.deleteChallenge(challengeId, userId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    /**
     * 챌린지 참여
     * POST /api/challenge/{challengeId}/join
     */
    @PostMapping("/{challengeId}/join")
    fun joinChallenge(
        @PathVariable challengeId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<ParticipateResponse>> {
        val participant = challengeService.joinChallenge(challengeId, userId)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(participant))
    }

    /**
     * 챌린지 탈퇴
     * POST /api/challenge/{challengeId}/withdraw
     */
    @PostMapping("/{challengeId}/withdraw")
    fun withdrawChallenge(
        @PathVariable challengeId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        challengeService.withdrawChallenge(challengeId, userId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    /**
     * 조회수 증가
     * POST /api/challenge/{challengeId}/view
     */
    @PostMapping("/{challengeId}/view")
    fun incrementViewCount(@PathVariable challengeId: String): ResponseEntity<ApiResponse<Unit>> {
        challengeService.incrementViewCount(challengeId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    /**
     * 참여자 목록 조회
     * GET /api/challenge/{challengeId}/participants
     */
    @GetMapping("/{challengeId}/participants")
    fun getParticipants(
        @PathVariable challengeId: String
    ): ResponseEntity<ApiResponse<List<ParticipateResponse>>> {
        val participants = challengeService.getParticipants(challengeId)
        return ResponseEntity.ok(ApiResponse.success(participants))
    }

    /**
     * 챌린지 통계 조회
     * GET /api/challenge/{challengeId}/statistics
     */
    @GetMapping("/{challengeId}/statistics")
    fun getChallengeStatistics(
        @PathVariable challengeId: String
    ): ResponseEntity<ApiResponse<ChallengeStatisticsResponse>> {
        val statistics = challengeService.getChallengeStatistics(challengeId)
        return ResponseEntity.ok(ApiResponse.success(statistics))
    }
}