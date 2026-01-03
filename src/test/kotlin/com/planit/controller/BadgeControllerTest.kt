package com.planit.controller

import com.planit.dto.BadgeResponse
import com.planit.entity.Badge
import com.planit.service.BadgeService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.util.Collections

@WebMvcTest(BadgeController::class)
@AutoConfigureMockMvc(addFilters = false)
class BadgeControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var badgeService: BadgeService

    @MockkBean(relaxed = true)
    private lateinit var jwtAuthFilter: com.planit.config.JwtAuthenticationFilter

    private fun setAuthentication(username: String) {
        val user: UserDetails = User(username, "password", Collections.emptyList())
        val authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)
        SecurityContextHolder.getContext().authentication = authentication
    }

    @Test
    @DisplayName("전체 배지 목록 조회 성공")
    fun `getAllBadges should return list of badges`() {
        // Given
        val username = "testuser"
        setAuthentication(username)
        
        val badgeResponse = BadgeResponse(
            code = "TEST_BADGE",
            name = "Test Badge",
            description = "Description",
            iconCode = "TEST_ICON",
            grade = com.planit.enums.BadgeGrade.BRONZE,
            isAcquired = true,
            acquiredAt = LocalDateTime.now()
        )
        
        every { badgeService.getAllBadges(username) } returns listOf(badgeResponse)

        // When & Then
        mockMvc.perform(get("/api/badges")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].code").value("TEST_BADGE"))
            .andExpect(jsonPath("$[0].name").value("Test Badge"))
            .andExpect(jsonPath("$[0].isAcquired").value(true))
    }

    @Test
    @DisplayName("내 배지 목록 조회 성공")
    fun `getMyBadges should return list of acquired badges`() {
        // Given
        val username = "testuser"
        setAuthentication(username)

        val badgeResponse = BadgeResponse(
            code = "TEST_BADGE",
            name = "Test Badge",
            description = "Description",
            iconCode = "TEST_ICON",
            grade = com.planit.enums.BadgeGrade.BRONZE,
            isAcquired = true,
            acquiredAt = LocalDateTime.now()
        )

        every { badgeService.getMyBadges(username) } returns listOf(badgeResponse)

        // When & Then
        mockMvc.perform(get("/api/badges/my")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].code").value("TEST_BADGE"))
            .andExpect(jsonPath("$[0].name").value("Test Badge"))
    }
}
