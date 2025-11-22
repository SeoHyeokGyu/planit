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
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.createChallenge(request, userId)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(challenge))
    }

    /**
     * 챌린지 상세 조회
     * GET /api/v1/challenges/{id}
     */
    @GetMapping("/{id}")
    fun getChallengeById(@PathVariable id: Long): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.getChallengeById(id)
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
     * PUT /api/v1/challenges/{id}
     */
    @PutMapping("/{id}")
    fun updateChallenge(
        @PathVariable id: Long,
        @Valid @RequestBody request: ChallengeRequest,
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.updateChallenge(id, request, userId)
        return ResponseEntity.ok(ApiResponse.success(challenge))
    }

    /**
     * 챌린지 삭제
     * DELETE /api/v1/challenges/{id}
     */
    @DeleteMapping("/{id}")
    fun deleteChallenge(
        @PathVariable id: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        challengeService.deleteChallenge(id, userId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    /**
     * 챌린지 참여
     * POST /api/v1/challenges/{id}/join
     */
    @PostMapping("/{id}/join")
    fun joinChallenge(
        @PathVariable id: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ApiResponse<ParticipateResponse>> {
        val participant = challengeService.joinChallenge(id, userId)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(participant))
    }

    /**
     * 챌린지 탈퇴
     * POST /api/v1/challenges/{id}/withdraw
     */
    @PostMapping("/{id}/withdraw")
    fun withdrawChallenge(
        @PathVariable id: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        challengeService.withdrawChallenge(id, userId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    /**
     * 조회수 증가
     * POST /api/v1/challenges/{id}/view
     */
    @PostMapping("/{id}/view")
    fun incrementViewCount(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        challengeService.incrementViewCount(id)
        return ResponseEntity.ok(ApiResponse.success())
    }

    /**
     * 참여자 목록 조회
     * GET /api/v1/challenges/{id}/participants
     */
    @GetMapping("/{id}/participants")
    fun getParticipants(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<List<ParticipateResponse>>> {
        val participants = challengeService.getParticipants(id)
        return ResponseEntity.ok(ApiResponse.success(participants))
    }

    /**
     * 챌린지 통계 조회
     * GET /api/v1/challenges/{id}/statistics
     */
    @GetMapping("/{id}/statistics")
    fun getChallengeStatistics(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<ChallengeStatisticsResponse>> {
        val statistics = challengeService.getChallengeStatistics(id)
        return ResponseEntity.ok(ApiResponse.success(statistics))
    }
}