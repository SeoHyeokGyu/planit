package com.planit.controller

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class HealthControllerTest {

    private val context = mockk<ApplicationContext>()
    private val healthController = HealthController(context)
    private val mockMvc = MockMvcBuilders.standaloneSetup(healthController).build()

    @Test
    @DisplayName("Health Check API 테스트")
    fun `health test`() {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("Planit API"))
    }

    @Test
    @DisplayName("Bean 목록 조회 API 테스트")
    fun `listBeans test`() {
        every { context.beanDefinitionNames } returns arrayOf("bean1", "bean2")

        mockMvc.perform(get("/api/beans"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.beans[0]").value("bean1"))
            .andExpect(jsonPath("$.beans[1]").value("bean2"))
    }
}
