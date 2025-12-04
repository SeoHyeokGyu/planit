package com.planit.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.planit.config.JwtTokenProvider
import com.planit.dto.CertificationResponse
import com.planit.service.CustomUserDetailsService
import com.planit.service.FeedService
import com.planit.util.WithMockCustomUser
import io.mockk.every
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDateTime

@WebMvcTest(FeedController::class)
@DisplayName("FeedController 테스트")
class FeedControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  @MockkBean
  private lateinit var feedService: FeedService

  // Security-related beans to satisfy filter dependencies
  @MockkBean
  private lateinit var customUserDetailsService: CustomUserDetailsService

  @MockkBean
  private lateinit var jwtTokenProvider: JwtTokenProvider

  @Nested
  @DisplayName("/api/feed GET 요청은")
  inner class DescribeGetFeed {

    @Test
    @WithMockCustomUser(loginId = "testuser", userId = 1L)
    @DisplayName("인증된 사용자의 참여 챌린지 피드를 반환한다")
    fun `returns feed for user's challenges`() {
      // Given
      val pageableSlot = slot<Pageable>()
      val responses = listOf(
        CertificationResponse(
          id = 1,
          title = "title",
          content = "content",
          photoUrl = null,
          authorNickname = "author",
          challengeTitle = "challenge",
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now(),
        )
      )
      val responsePage = PageImpl(responses, PageRequest.of(0, 10), 1)
      every { feedService.getFeedForUser("testuser", capture(pageableSlot)) } returns responsePage

      // When & Then
      mockMvc.get("/api/feed?page=0&size=10&sort=latest") {
        accept = org.springframework.http.MediaType.APPLICATION_JSON
      }.andExpect {
        status { isOk() }
        content { contentType(org.springframework.http.MediaType.APPLICATION_JSON) }
        jsonPath("$.content.length()") { value(1) }
        jsonPath("$.content[0].title") { value("title") }
        jsonPath("$.totalElements") { value(1) }
      }

      // Verify the captured pageable has correct sort
      val capturedPageable = pageableSlot.captured
      assertThat(capturedPageable.sort).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"))
    }
  }

  @Nested
  @DisplayName("/api/feed/following GET 요청은")
  inner class DescribeGetFollowingFeed {
    @Test
    @WithMockCustomUser(loginId = "testuser", userId = 1L)
    @DisplayName("인증된 사용자의 팔로잉 피드를 반환한다")
    fun `returns feed for followed users`() {
      // Given
      val pageableSlot = slot<Pageable>()
      val responses = listOf(
        CertificationResponse(
          id = 1,
          title = "title",
          content = "content",
          photoUrl = null,
          authorNickname = "author",
          challengeTitle = "challenge",
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now(),
        )
      )
      val responsePage = PageImpl(responses, PageRequest.of(0, 10), 1)
      every { feedService.getFollowingFeed("testuser", capture(pageableSlot)) } returns responsePage

      // When & Then
      mockMvc.get("/api/feed/following?page=0&size=10&sort=latest") {
        accept = org.springframework.http.MediaType.APPLICATION_JSON
      }.andExpect {
        status { isOk() }
        content { contentType(org.springframework.http.MediaType.APPLICATION_JSON) }
        jsonPath("$.content.length()") { value(1) }
        jsonPath("$.content[0].title") { value("following_title") }
        jsonPath("$.totalElements") { value(1) }
      }

      // Verify the captured pageable has correct sort
      val capturedPageable = pageableSlot.captured
      assertThat(capturedPageable.sort).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"))
    }
  }
}
