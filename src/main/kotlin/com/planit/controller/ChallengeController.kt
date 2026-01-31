package com.planit.controller

import com.planit.dto.*
import com.planit.service.ChallengeRecommendService
import com.planit.service.ChallengeService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/challenge")
class ChallengeController(
    private val challengeService: ChallengeService,
    private val recommendService: ChallengeRecommendService
) {

    /**
     * 사용자 맞춤형 새로운 챌린지 제안 (생성 시 참고)
     */
    @GetMapping("/recommend")
    fun recommendNewChallenges(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<List<ChallengeRecommendationResponse>>> {
        val response = recommendService.recommendNewChallenges(userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 기존 챌린지 중에서 추천 (참여 용도)
     */
    @GetMapping("/recommend-existing")
    fun recommendExistingChallenges(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<List<ExistingChallengeRecommendationResponse>>> {
        val response = recommendService.recommendExistingChallenges(userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 사용자의 기분/상황에 따른 기존 챌린지 추천
     */
    @GetMapping("/recommend-existing/query")
    fun recommendExistingChallengesWithQuery(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam query: String
    ): ResponseEntity<ApiResponse<List<ExistingChallengeRecommendationResponse>>> {
        val response = recommendService.recommendExistingChallengesWithQuery(userDetails.username, query)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 챌린지 생성
     */
    @PostMapping
    fun createChallenge(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: ChallengeRequest
    ): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.createChallenge(request, userDetails.user.loginId)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(challenge))
    }

    /**
     * 내가 참여중인 챌린지 목록 조회
     * GET /api/challenge/my
     */
    @GetMapping("/my")
    fun getMyChallenges(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<List<ChallengeListResponse>>> {
        val challenges = challengeService.getParticipatingChallenges(userDetails.user.loginId)
        return ResponseEntity.ok(ApiResponse.success(challenges))
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
     * 챌린지 목록 조회 (필터링 + 정렬)
     * GET /api/challenge
     */
    @GetMapping
    fun getChallenges(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) difficulty: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false, defaultValue = "LATEST") sortBy: String?
    ): ResponseEntity<ApiResponse<List<ChallengeListResponse>>> {
        val request = ChallengeSearchRequest(
            keyword = keyword,
            category = category,
            difficulty = difficulty,
            status = status,
            sortBy = sortBy
        )

        // 디버깅 로그
        println("=== Challenge Search Request ===")
        println("keyword: $keyword")
        println("category: $category")
        println("difficulty: $difficulty")
        println("status: $status")
        println("sortBy: $sortBy")

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
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: ChallengeRequest
    ): ResponseEntity<ApiResponse<ChallengeResponse>> {
        val challenge = challengeService.updateChallenge(challengeId, request, userDetails.user.loginId)
        return ResponseEntity.ok(ApiResponse.success(challenge))
    }

    /**
     * 챌린지 삭제
     * DELETE /api/challenge/{challengeId}
     */
    @DeleteMapping("/{challengeId}")
    fun deleteChallenge(
        @PathVariable challengeId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<Unit>> {
        challengeService.deleteChallenge(challengeId, userDetails.user.loginId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    /**
     * 챌린지 참여
     * POST /api/challenge/{challengeId}/join
     */
    @PostMapping("/{challengeId}/join")
    fun joinChallenge(
        @PathVariable challengeId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<ParticipateResponse>> {
        val participant = challengeService.joinChallenge(challengeId, userDetails.user.loginId)
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
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<Unit>> {
        challengeService.withdrawChallenge(challengeId, userDetails.user.loginId)
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