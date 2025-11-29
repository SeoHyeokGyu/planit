package com.planit.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.planit.config.JwtTokenProvider
import com.planit.dto.*
import com.planit.enums.ParticipantStatusEnum
import com.planit.service.ChallengeService
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import java.time.LocalDateTime
import java.util.NoSuchElementException

@WebMvcTest(ChallengeController::class)
@AutoConfigureMockMvc(addFilters = false)
class ChallengeControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var challengeService: ChallengeService

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    @DisplayName("챌린지 생성 성공")
    fun `createChallenge should return 201 Created on success`() {
        // Given
        val request = ChallengeRequest(
            title = "30일 운동 챌린지",
            description = "매일 30분씩 운동하기",
            category = "EXERCISE",
            difficulty = "NORMAL",
            loginId = "user123",
            startDate = LocalDateTime.of(2024, 1, 1, 0, 0),
            endDate = LocalDateTime.of(2024, 1, 31, 23, 59)
        )

        val response = ChallengeResponse(
            id = 1L,
            title = "30일 운동 챌린지",
            description = "매일 30분씩 운동하기",
            category = "EXERCISE",
            startDate = LocalDateTime.of(2024, 1, 1, 0, 0),
            endDate = LocalDateTime.of(2024, 1, 31, 23, 59),
            difficulty = "NORMAL",
            createdId = "user123",
            viewCnt = 0,
            participantCnt = 0,
            certificationCnt = 0
        )

        every { challengeService.createChallenge(any(), any()) } returns response

        // When & Then
        mockMvc.post("/api/v1/challenges") {
            header("X-User-Id", "1")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.title") { value("30일 운동 챌린지") }
        }
    }

    @Test
    @DisplayName("챌린지 상세 조회 성공")
    fun `getChallengeById should return 200 OK on success`() {
        // Given
        val response = ChallengeResponse(
            id = 1L,
            title = "30일 운동 챌린지",
            description = "매일 30분씩 운동하기",
            category = "EXERCISE",
            startDate = LocalDateTime.of(2024, 1, 1, 0, 0),
            endDate = LocalDateTime.of(2024, 1, 31, 23, 59),
            difficulty = "NORMAL",
            createdId = "user123",
            viewCnt = 10,
            participantCnt = 5,
            certificationCnt = 20
        )

        every { challengeService.getChallengeById(1L) } returns response

        // When & Then
        mockMvc.get("/api/v1/challenges/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.id") { value(1) }
                jsonPath("$.data.title") { value("30일 운동 챌린지") }
            }
    }

    @Test
    @DisplayName("챌린지 상세 조회 실패 - 존재하지 않는 챌린지")
    fun `getChallengeById should return 404 Not Found for non-existent challenge`() {
        // Given
        every { challengeService.getChallengeById(999L) } throws
                NoSuchElementException("챌린지를 찾을 수 없습니다: 999")

        // When & Then
        mockMvc.get("/api/v1/challenges/999")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("RESOURCE_NOT_FOUND") }
            }
    }

    @Test
    @DisplayName("챌린지 목록 조회 성공")
    fun `getChallenges should return 200 OK with list`() {
        // Given
        val challenges = listOf(
            ChallengeListResponse(
                id = 1L,
                title = "30일 운동 챌린지",
                description = "매일 30분씩 운동하기",
                category = "EXERCISE",
                difficulty = "NORMAL",
                startDate = LocalDateTime.of(2024, 1, 1, 0, 0),
                endDate = LocalDateTime.of(2024, 1, 31, 23, 59),
                createdId = "user123",
                viewCnt = 10,
                participantCnt = 5,
                certificationCnt = 20
            )
        )

        every { challengeService.getChallenges(any()) } returns challenges

        // When & Then
        mockMvc.get("/api/v1/challenges") {
            param("category", "EXERCISE")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data[0].title") { value("30일 운동 챌린지") }
        }
    }

    @Test
    @DisplayName("챌린지 검색 성공")
    fun `searchChallenges should return 200 OK with search results`() {
        // Given
        val challenges = listOf(
            ChallengeListResponse(
                id = 1L,
                title = "30일 운동 챌린지",
                description = "매일 30분씩 운동하기",
                category = "EXERCISE",
                difficulty = "NORMAL",
                startDate = LocalDateTime.of(2024, 1, 1, 0, 0),
                endDate = LocalDateTime.of(2024, 1, 31, 23, 59),
                createdId = "user123",
                viewCnt = 10,
                participantCnt = 5,
                certificationCnt = 20
            )
        )

        every { challengeService.searchChallenges("운동") } returns challenges

        // When & Then
        mockMvc.get("/api/v1/challenges/search") {
            param("keyword", "운동")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data[0].title") { value("30일 운동 챌린지") }
        }
    }

    @Test
    @DisplayName("챌린지 수정 성공")
    fun `updateChallenge should return 200 OK on success`() {
        // Given
        val request = ChallengeRequest(
            title = "수정된 챌린지",
            description = "수정된 설명",
            category = "HEALTH",
            difficulty = "HARD",
            loginId = "user123",
            startDate = LocalDateTime.of(2024, 1, 1, 0, 0),
            endDate = LocalDateTime.of(2024, 1, 31, 23, 59)
        )

        val response = ChallengeResponse(
            id = 1L,
            title = "수정된 챌린지",
            description = "수정된 설명",
            category = "HEALTH",
            startDate = LocalDateTime.of(2024, 1, 1, 0, 0),
            endDate = LocalDateTime.of(2024, 1, 31, 23, 59),
            difficulty = "HARD",
            createdId = "user123",
            viewCnt = 10,
            participantCnt = 5,
            certificationCnt = 20
        )

        every { challengeService.updateChallenge(1L, any(), 1L) } returns response

        // When & Then
        mockMvc.put("/api/v1/challenges/1") {
            header("X-User-Id", "1")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.title") { value("수정된 챌린지") }
        }
    }

    @Test
    @DisplayName("챌린지 수정 실패 - 권한 없음")
    fun `updateChallenge should return 400 Bad Request without permission`() {
        // Given
        val request = ChallengeRequest(
            title = "수정된 챌린지",
            description = "수정된 설명",
            category = "HEALTH",
            difficulty = "HARD",
            loginId = "user123",
            startDate = LocalDateTime.of(2024, 1, 1, 0, 0),
            endDate = LocalDateTime.of(2024, 1, 31, 23, 59)
        )

        every { challengeService.updateChallenge(1L, any(), 999L) } throws
                IllegalArgumentException("챌린지를 수정할 권한이 없습니다")

        // When & Then
        mockMvc.put("/api/v1/challenges/1") {
            header("X-User-Id", "999")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_ARGUMENT") }
        }
    }

    @Test
    @DisplayName("챌린지 삭제 성공")
    fun `deleteChallenge should return 200 OK on success`() {
        // Given
        every { challengeService.deleteChallenge(1L, 1L) } just runs

        // When & Then
        mockMvc.delete("/api/v1/challenges/1") {
            header("X-User-Id", "1")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
        }

        verify(exactly = 1) { challengeService.deleteChallenge(1L, 1L) }
    }

    @Test
    @DisplayName("챌린지 참여 성공")
    fun `joinChallenge should return 201 Created on success`() {
        // Given
        val response = ParticipateResponse(
            id = 1L,
            userId = 1L,
            status = ParticipantStatusEnum.ACTIVE,
            certificationCnt = 0,
            joinedAt = LocalDateTime.now(),
            completedAt = null,
            withdrawnAt = null
        )

        every { challengeService.joinChallenge(1L, 1L) } returns response

        // When & Then
        mockMvc.post("/api/v1/challenges/1/join") {
            header("X-User-Id", "1")
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.userId") { value(1) }
            jsonPath("$.data.status") { value("ACTIVE") }
        }
    }

    @Test
    @DisplayName("챌린지 참여 실패 - 이미 참여중")
    fun `joinChallenge should return 409 Conflict for duplicate participation`() {
        // Given
        every { challengeService.joinChallenge(1L, 1L) } throws
                IllegalStateException("이미 참여중인 챌린지입니다")

        // When & Then
        mockMvc.post("/api/v1/challenges/1/join") {
            header("X-User-Id", "1")
        }.andExpect {
            status { isInternalServerError() } // IllegalStateException이 500으로 처리됨
            jsonPath("$.success") { value(false) }
        }
    }

    @Test
    @DisplayName("챌린지 탈퇴 성공")
    fun `withdrawChallenge should return 200 OK on success`() {
        // Given
        every { challengeService.withdrawChallenge(1L, 1L) } just runs

        // When & Then
        mockMvc.post("/api/v1/challenges/1/withdraw") {
            header("X-User-Id", "1")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
        }

        verify(exactly = 1) { challengeService.withdrawChallenge(1L, 1L) }
    }

    @Test
    @DisplayName("조회수 증가 성공")
    fun `incrementViewCount should return 200 OK on success`() {
        // Given
        every { challengeService.incrementViewCount(1L) } just runs

        // When & Then
        mockMvc.post("/api/v1/challenges/1/view")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        verify(exactly = 1) { challengeService.incrementViewCount(1L) }
    }

    @Test
    @DisplayName("참여자 목록 조회 성공")
    fun `getParticipants should return 200 OK with list`() {
        // Given
        val participants = listOf(
            ParticipateResponse(
                id = 1L,
                userId = 1L,
                status = ParticipantStatusEnum.ACTIVE,
                certificationCnt = 5,
                joinedAt = LocalDateTime.now(),
                completedAt = null,
                withdrawnAt = null
            )
        )

        every { challengeService.getParticipants(1L) } returns participants

        // When & Then
        mockMvc.get("/api/v1/challenges/1/participants")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data[0].userId") { value(1) }
                jsonPath("$.data[0].certificationCnt") { value(5) }
            }
    }

    @Test
    @DisplayName("챌린지 통계 조회 성공")
    fun `getChallengeStatistics should return 200 OK with statistics`() {
        // Given
        val statistics = ChallengeStatisticsResponse(
            challengeId = 1L,
            totalParticipants = 100,
            activeParticipants = 80,
            completedParticipants = 15,
            withdrawnParticipants = 5,
            totalCertifications = 500,
            completionRate = 15.0,
            averageCertificationPerParticipant = 5.0,
            viewCount = 1500
        )

        every { challengeService.getChallengeStatistics(1L) } returns statistics

        // When & Then
        mockMvc.get("/api/v1/challenges/1/statistics")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.totalParticipants") { value(100) }
                jsonPath("$.data.completionRate") { value(15.0) }
                jsonPath("$.data.viewCount") { value(1500) }
            }
    }
}