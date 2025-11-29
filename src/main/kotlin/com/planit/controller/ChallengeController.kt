package com.planit.controller

import com.planit.dto.*
import com.planit.service.ChallengeService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/challenges")
@Validated
class ChallengeController(
    private val challengeService: ChallengeService
) {

    /**
     * 챌린지 생성
     * POST /api/v1/challenges
     */
    @PostMapping
    fun createChallenge(
        @Valid @RequestBody request: ChallengeRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.createChallenge(request, userId)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(challenge))
    }

    /**
     * 챌린지 상세 조회
     * GET /api/v1/challenges/{challengeId}
     */
    @GetMapping("/{challengeId}")
    fun getChallengeById(@PathVariable challengeId: String): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.getChallengeById(challengeId)
        return ResponseEntity.ok(ApiResponse.success(challenge))
    }

    /**
     * 챌린지 목록 조회 (필터링)
     * GET /api/v1/challenges
     */
    @GetMapping
    fun getChallenges(
        @ModelAttribute request: ChallengeSearchRequest
    ): ResponseEntity<ApiResponse<List<ChallengeListResponse>>> {
        val challenges = challengeService.getChallenges(request)
        return ResponseEntity.ok(ApiResponse.success(challenges))
    }

    /**
     * 챌린지 검색 (키워드)
     * GET /api/v1/challenges/search
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
     * PUT /api/v1/challenges/{challengeId}
     */
    @PutMapping("/{challengeId}")
    fun updateChallenge(
        @PathVariable challengeId: String,
        @Valid @RequestBody request: ChallengeRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.updateChallenge(challengeId, request, userId)
        return ResponseEntity.ok(ApiResponse.success(challenge))
    }

    /**
     * 챌린지 삭제
     * DELETE /api/v1/challenges/{challengeId}
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
     * POST /api/v1/challenges/{challengeId}/join
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
     * POST /api/v1/challenges/{challengeId}/withdraw
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
     * POST /api/v1/challenges/{challengeId}/view
     */
    @PostMapping("/{challengeId}/view")
    fun incrementViewCount(@PathVariable challengeId: String): ResponseEntity<ApiResponse<Unit>> {
        challengeService.incrementViewCount(challengeId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    /**
     * 참여자 목록 조회
     * GET /api/v1/challenges/{challengeId}/participants
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
     * GET /api/v1/challenges/{challengeId}/statistics
     */
    @GetMapping("/{challengeId}/statistics")
    fun getChallengeStatistics(
        @PathVariable challengeId: String
    ): ResponseEntity<ApiResponse<ChallengeStatisticsResponse>> {
        val statistics = challengeService.getChallengeStatistics(challengeId)
        return ResponseEntity.ok(ApiResponse.success(statistics))
    }
}