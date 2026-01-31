package com.planit.controller

import com.planit.service.RankingSseService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

class RankingSseControllerTest {

    private val rankingSseService = mockk<RankingSseService>()
    private val rankingSseController = RankingSseController(rankingSseService)
    private val mockMvc = MockMvcBuilders.standaloneSetup(rankingSseController).build()

    @Test
    @DisplayName("SSE 스트림 구독 API 테스트")
    fun `streamRankings test`() {
        every { rankingSseService.subscribe() } returns SseEmitter()

        mockMvc.perform(get("/api/rankings/stream"))
            .andExpect(request().asyncStarted())
            .andReturn()
    }

    @Test
    @DisplayName("SSE 상태 조회 API 테스트")
    fun `getStreamStatus test`() {
        every { rankingSseService.getConnectedClientCount() } returns 10

        mockMvc.perform(get("/api/rankings/stream/status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.connectedClients").value(10))
    }
}
