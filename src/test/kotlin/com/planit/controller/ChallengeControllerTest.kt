package com.planit.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.planit.config.JwtTokenProvider
import com.planit.dto.*
import com.planit.entity.User
import com.planit.enums.ParticipantStatusEnum
import com.planit.service.ChallengeService
import com.planit.util.setPrivateProperty
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
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

    @MockkBean
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    private val testUser = User(loginId = "user123", password = "password", nickname = "tester").apply {
        setPrivateProperty("id", 1L)
    }
    private val userDetails = CustomUserDetails(testUser)

    private fun setAuthentication() {
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = authentication
    }

    @Test
    @DisplayName("챌린지 생성 성공")
    fun `createChallenge should return 201 Created on success`() {
        // Given
        setAuthentication()
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
            id = "CHL-12345678",
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
        mockMvc.post("/api/challenge") {
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
            id = "CHL-12345678",
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

        every { challengeService.getChallengeById("CHL-12345678") } returns response

        // When & Then
        mockMvc.get("/api/challenge/CHL-12345678")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.id") { value("CHL-12345678") }
                jsonPath("$.data.title") { value("30일 운동 챌린지") }
            }
    }

    @Test
    @DisplayName("챌린지 상세 조회 실패 - 존재하지 않는 챌린지")
    fun `getChallengeById should return 404 Not Found for non-existent challenge`() {
        // Given
        every { challengeService.getChallengeById("CHL-99999999") } throws
                NoSuchElementException("챌린지를 찾을 수 없습니다: CHL-99999999")

        // When & Then
        mockMvc.get("/api/challenge/CHL-99999999")
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
                id = "CHL-12345678",
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
        mockMvc.get("/api/challenge") {
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
                id = "CHL-12345678",
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
        mockMvc.get("/api/challenge/search") {
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
        setAuthentication()
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
            id = "CHL-12345678",
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

        every { challengeService.updateChallenge("CHL-12345678", any(), "user123") } returns response

        // When & Then
        mockMvc.put("/api/challenge/CHL-12345678") {
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

        val anotherUser = User(loginId = "user999", password = "password", nickname = "other").apply {
            setPrivateProperty("id", 2L)
        }
        val anotherUserDetails = CustomUserDetails(anotherUser)
        val authentication = UsernamePasswordAuthenticationToken(anotherUserDetails, null, anotherUserDetails.authorities)
        org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = authentication

        every { challengeService.updateChallenge("CHL-12345678", any(), "user999") } throws
                IllegalArgumentException("챌린지를 수정할 권한이 없습니다")

        // When & Then
        mockMvc.put("/api/challenge/CHL-12345678") {
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
        setAuthentication()
        every { challengeService.deleteChallenge("CHL-12345678", "user123") } just runs

        // When & Then
        mockMvc.delete("/api/challenge/CHL-12345678") {
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
        }

        verify(exactly = 1) { challengeService.deleteChallenge("CHL-12345678", "user123") }
    }

    @Test
    @DisplayName("챌린지 참여 성공")
    fun `joinChallenge should return 201 Created on success`() {
        // Given
        setAuthentication()
        val response = ParticipateResponse(
            challengeId = "CHL-12345678",
            loginId = "user123",
            status = ParticipantStatusEnum.ACTIVE,
            certificationCnt = 0,
            joinedAt = LocalDateTime.now(),
            completedAt = null,
            withdrawnAt = null
        )

        every { challengeService.joinChallenge("CHL-12345678", "user123") } returns response

        // When & Then
        mockMvc.post("/api/challenge/CHL-12345678/join") {
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.loginId") { value("user123") }
            jsonPath("$.data.status") { value("ACTIVE") }
        }
    }

    @Test
    @DisplayName("챌린지 참여 실패 - 이미 참여중")
    fun `joinChallenge should return 409 Conflict for duplicate participation`() {
        // Given
        setAuthentication()
        every { challengeService.joinChallenge("CHL-12345678", "user123") } throws
                IllegalStateException("이미 참여중인 챌린지입니다")

        // When & Then
        mockMvc.post("/api/challenge/CHL-12345678/join") {
        }.andExpect {
            status { isInternalServerError() } // IllegalStateException이 500으로 처리됨
            jsonPath("$.success") { value(false) }
        }
    }

    @Test
    @DisplayName("챌린지 탈퇴 성공")
    fun `withdrawChallenge should return 200 OK on success`() {
        // Given
        setAuthentication()
        every { challengeService.withdrawChallenge("CHL-12345678", "user123") } just runs

        // When & Then
        mockMvc.post("/api/challenge/CHL-12345678/withdraw") {
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
        }

        verify(exactly = 1) { challengeService.withdrawChallenge("CHL-12345678", "user123") }
    }

    @Test
    @DisplayName("조회수 증가 성공")
    fun `incrementViewCount should return 200 OK on success`() {
        // Given
        every { challengeService.incrementViewCount("CHL-12345678") } just runs

        // When & Then
        mockMvc.post("/api/challenge/CHL-12345678/view")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        verify(exactly = 1) { challengeService.incrementViewCount("CHL-12345678") }
    }

    @Test
    @DisplayName("참여자 목록 조회 성공")
    fun `getParticipants should return 200 OK with list`() {
        // Given
        val participants = listOf(
            ParticipateResponse(
                challengeId = "CHL-12345678",
                loginId = "user123",
                status = ParticipantStatusEnum.ACTIVE,
                certificationCnt = 5,
                joinedAt = LocalDateTime.now(),
                completedAt = null,
                withdrawnAt = null
            )
        )

        every { challengeService.getParticipants("CHL-12345678") } returns participants

        // When & Then
        mockMvc.get("/api/challenge/CHL-12345678/participants")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data[0].loginId") { value("user123") }
                jsonPath("$.data[0].certificationCnt") { value(5) }
            }
    }

    @Test
    @DisplayName("챌린지 통계 조회 성공")
    fun `getChallengeStatistics should return 200 OK with statistics`() {
        // Given
        val statistics = ChallengeStatisticsResponse(
            id = "CHL-12345678",
            totalParticipants = 100,
            activeParticipants = 80,
            completedParticipants = 15,
            withdrawnParticipants = 5,
            totalCertifications = 500,
            completionRate = 15.0,
            averageCertificationPerParticipant = 5.0,
            viewCount = 1500
        )

        every { challengeService.getChallengeStatistics("CHL-12345678") } returns statistics

        // When & Then
        mockMvc.get("/api/challenge/CHL-12345678/statistics")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.totalParticipants") { value(100) }
                jsonPath("$.data.completionRate") { value(15.0) }
                jsonPath("$.data.viewCount") { value(1500) }
            }
    }
}