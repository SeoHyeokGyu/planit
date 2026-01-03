package com.planit.controller

import com.ninjasquad.springmockk.MockkBean
import com.planit.config.JwtAuthenticationFilter
import com.planit.dto.BadgeResponse
import com.planit.service.BadgeService
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
import java.util.*

/**
 * 배지 관련 API 엔드포인트의 동작을 검증하는 테스트 클래스입니다.
 */
@WebMvcTest(BadgeController::class)
@AutoConfigureMockMvc(addFilters = false)
class BadgeControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc
  @MockkBean private lateinit var badgeService: BadgeService
  @MockkBean(relaxed = true) private lateinit var jwtAuthFilter: JwtAuthenticationFilter

  /**
   * 스프링 시큐리티 컨텍스트에 테스트용 인증 정보를 설정합니다.
   * @AuthenticationPrincipal을 사용하는 컨트롤러 테스트를 위해 필요합니다.
   */
  private fun setAuthentication(username: String) {
    val user: UserDetails = User(username, "password", Collections.emptyList())
    val authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)
    SecurityContextHolder.getContext().authentication = authentication
  }

  @Test
  @DisplayName("전체 배지 목록 조회 성공")
  fun `getAllBadges should return list of badges`() {
    // Given: 인증된 사용자 정보와 서비스 반환 데이터 설정
    val username = "testuser1"
    setAuthentication(username)

    val badgeResponse =
      BadgeResponse(
        code = "TEST_BADGE",
        name = "Test Badge",
        description = "Description",
        iconCode = "TEST_ICON",
        grade = com.planit.enums.BadgeGrade.BRONZE,
        isAcquired = true,
        acquiredAt = LocalDateTime.now(),
      )

    every { badgeService.getAllBadges(username) } returns listOf(badgeResponse)

    // When: GET /api/badges 요청 실행
    // Then: 200 OK 상태코드와 함께 JSON 응답 검증
    mockMvc
      .perform(get("/api/badges").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].code").value("TEST_BADGE"))
      .andExpect(jsonPath("$[0].name").value("Test Badge"))
      .andExpect(jsonPath("$[0].isAcquired").value(true))
  }

  @Test
  @DisplayName("내 배지 목록 조회 성공")
  fun `getMyBadges should return list of acquired badges`() {
    // Given: 인증된 사용자 정보와 서비스 반환 데이터 설정
    val username = "testuser2"
    setAuthentication(username)

    val badgeResponse =
      BadgeResponse(
        code = "TEST_BADGE",
        name = "Test Badge",
        description = "Description",
        iconCode = "TEST_ICON",
        grade = com.planit.enums.BadgeGrade.BRONZE,
        isAcquired = true,
        acquiredAt = LocalDateTime.now(),
      )

    every { badgeService.getMyBadges(username) } returns listOf(badgeResponse)

    // When: GET /api/badges/my 요청 실행
    // Then: 200 OK 상태코드와 함께 획득한 배지 목록 검증
    mockMvc
      .perform(get("/api/badges/my").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].code").value("TEST_BADGE"))
      .andExpect(jsonPath("$[0].name").value("Test Badge"))
  }
}
