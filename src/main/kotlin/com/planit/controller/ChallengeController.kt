package com.planit.controller

import com.planit.dto.*
import com.planit.service.ChallengeService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
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
        @Valid @RequestBody request: ChallengeCreateRequest,
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.createChallenge(request, userId)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(challenge, "챌린지가 생성되었습니다"))
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
     * 챌린지 목록 조회 (페이징, 필터링, 정렬)
     * GET /api/v1/challenges
     */
    @GetMapping
    fun getChallenges(
        @Valid @ModelAttribute request: ChallengeSearchRequest
    ): ResponseEntity<ApiResponse<PageResponse<ChallengeListResponse>>> {
        val challenges = challengeService.getChallenges(request)
        return ResponseEntity.ok(ApiResponse.success(challenges))
    }

    /**
     * 챌린지 검색 (Full-Text Search)
     * GET /api/v1/challenges/search
     */
    @GetMapping("/search")
    fun searchChallenges(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) size: Int
    ): ResponseEntity<ApiResponse<PageResponse<ChallengeListResponse>>> {
        val challenges = challengeService.searchChallenges(keyword, page, size)
        return ResponseEntity.ok(ApiResponse.success(challenges))
    }

    /**
     * 챌린지 수정
     * PUT /api/v1/challenges/{id}
     */
    @PutMapping("/{id}")
    fun updateChallenge(
        @PathVariable id: Long,
        @Valid @RequestBody request: ChallengeUpdateRequest,
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.updateChallenge(id, request, userId)
        return ResponseEntity.ok(ApiResponse.success(challenge, "챌린지가 수정되었습니다"))
    }

    /**
     * 챌린지 삭제 (소프트 삭제)
     * DELETE /api/v1/challenges/{id}
     */
    @DeleteMapping("/{id}")
    fun deleteChallenge(
        @PathVariable id: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        challengeService.deleteChallenge(id, userId)
        return ResponseEntity.ok(ApiResponse.success("챌린지가 삭제되었습니다"))
    }

    /**
     * 챌린지 참여
     * POST /api/v1/challenges/{id}/join
     */
    @PostMapping("/{id}/join")
    fun joinChallenge(
        @PathVariable id: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ApiResponse<ParticipantResponse>> {
        val participant = challengeService.joinChallenge(id, userId)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(participant, "챌린지에 참여했습니다"))
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
        return ResponseEntity.ok(ApiResponse.success("챌린지에서 탈퇴했습니다"))
    }

    /**
     * 조회수 증가
     * POST /api/v1/challenges/{id}/view
     */
    @PostMapping("/{id}/view")
    fun incrementViewCount(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        challengeService.incrementViewCount(id)
        return ResponseEntity.ok(ApiResponse.success("조회수가 증가했습니다"))
    }

    /**
     * 참여자 목록 조회
     * GET /api/v1/challenges/{id}/participants
     */
    @GetMapping("/{id}/participants")
    fun getParticipants(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) size: Int
    ): ResponseEntity<ApiResponse<PageResponse<ParticipantResponse>>> {
        val participants = challengeService.getParticipants(id, page, size)
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
