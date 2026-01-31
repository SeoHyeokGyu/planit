package com.planit.controller

import com.planit.dto.CustomUserDetails
import com.planit.enums.FeedSortType
import com.planit.service.FeedService
import com.planit.entity.User
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.data.domain.PageImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class FeedControllerTest {

    private val feedService = mockk<FeedService>()
    private val feedController = FeedController(feedService)
    
    private val user = User(loginId = "testuser", password = "p", nickname = "n")
    private val userDetails = CustomUserDetails(user)

    private val mockMvc = MockMvcBuilders.standaloneSetup(feedController)
        .setCustomArgumentResolvers(
            object : HandlerMethodArgumentResolver {
                override fun supportsParameter(parameter: MethodParameter): Boolean {
                    return parameter.parameterType == CustomUserDetails::class.java
                }
                override fun resolveArgument(
                    parameter: MethodParameter,
                    mavContainer: ModelAndViewContainer?,
                    webRequest: NativeWebRequest,
                    binderFactory: WebDataBinderFactory?
                ): Any = userDetails
            },
            org.springframework.data.web.PageableHandlerMethodArgumentResolver()
        )
        .build()

    @Test
    @DisplayName("피드 조회 API 테스트")
    fun `getFeed test`() {
        every { feedService.getFeed("testuser", any(), any()) } returns PageImpl(emptyList())

        mockMvc.perform(get("/api/feed")
            .param("sortBy", "LATEST"))
            .andExpect(status().isOk)
    }
}
