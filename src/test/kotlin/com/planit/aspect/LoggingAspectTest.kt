package com.planit.aspect

import io.mockk.*
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import org.junit.jupiter.api.*
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.multipart.MultipartFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

class LoggingAspectTest {

    private val loggingAspect = LoggingAspect()
    private val joinPoint = mockk<ProceedingJoinPoint>()
    private val signature = mockk<Signature>()

    @BeforeEach
    fun setUp() {
        MDC.clear()
        every { joinPoint.signature } returns signature
        every { signature.declaringType } returns String::class.java
        every { signature.name } returns "testMethod"
        every { joinPoint.args } returns emptyArray()
        every { joinPoint.proceed() } returns "result"
    }

    @AfterEach
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
        MDC.clear()
    }

    @Test
    @DisplayName("API 호출 로깅 테스트 - 익명 사용자")
    fun `logApiCall with anonymous user`() {
        // Given
        val request = MockHttpServletRequest("GET", "/api/test")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        // When
        val result = loggingAspect.logApiCall(joinPoint)

        // Then
        assertEquals("result", result)
        // MDC should be cleared in finally
        Assertions.assertNull(MDC.get("apiInfo"))
    }

    @Test
    @DisplayName("서비스 호출 로깅 테스트")
    fun `logServiceCall test`() {
        // When
        val result = loggingAspect.logServiceCall(joinPoint)

        // Then
        assertEquals("result", result)
        assertNotNull(MDC.get("requestId"))
    }

    @Test
    @DisplayName("리포지토리 호출 로깅 테스트")
    fun `logRepositoryCall test`() {
        // When
        val result = loggingAspect.logRepositoryCall(joinPoint)

        // Then
        assertEquals("result", result)
    }

    @Test
    @DisplayName("에러 발생 시 로깅 테스트")
    fun `executeLogging with exception`() {
        // Given
        every { joinPoint.proceed() } throws RuntimeException("Error")

        // When & Then
        assertThrows<RuntimeException> {
            loggingAspect.logServiceCall(joinPoint)
        }
    }

    @Test
    @DisplayName("다양한 파라미터 타입 포맷팅 테스트")
    fun `formatArgs test`() {
        val mockFile = mockk<MultipartFile>()
        every { mockFile.originalFilename } returns "test.png"
        every { mockFile.size } returns 1024L
        
        val mockFileNoName = mockk<MultipartFile>()
        every { mockFileNoName.originalFilename } returns null
        every { mockFileNoName.size } returns 0L

        val request = mockk<jakarta.servlet.http.HttpServletRequest>()
        
        every { joinPoint.args } returns arrayOf(null, mockFile, mockFileNoName, request, "simple string")
        
        // When
        loggingAspect.logServiceCall(joinPoint)
        
        // Then
        // Should handle all types
    }

    @Test
    @DisplayName("API 호출 로깅 테스트 - 인증된 사용자")
    fun `logApiCall with authenticated user`() {
        // Given
        val request = MockHttpServletRequest("GET", "/api/test")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        
        val auth = mockk<org.springframework.security.core.Authentication>()
        every { auth.isAuthenticated } returns true
        every { auth.name } returns "testuser"
        every { auth.principal } returns "somePrincipal"
        org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = auth

        // When
        loggingAspect.logApiCall(joinPoint)

        // Then
        // Should not throw and use username in log
        org.springframework.security.core.context.SecurityContextHolder.clearContext()
    }

    @Test
    @DisplayName("UUID 초기화 테스트")
    fun `getOrInitUuid should generate new uuid if not present`() {
        // When
        loggingAspect.logServiceCall(joinPoint)
        
        // Then
        assertNotNull(MDC.get("requestId"))
    }

    @Test
    @DisplayName("결과값 길이 제한 테스트")
    fun `formatResult with long string`() {
        val longResult = "a".repeat(1100)
        every { joinPoint.proceed() } returns longResult
        
        // When
        val result = loggingAspect.logServiceCall(joinPoint)
        
        // Then
        assertEquals(longResult, result)
    }

    @Test
    @DisplayName("응답 객체 파라미터 포맷팅 테스트")
    fun `formatArgs with HttpServletResponse`() {
        val response = mockk<jakarta.servlet.http.HttpServletResponse>()
        every { joinPoint.args } returns arrayOf(response)
        
        // When
        loggingAspect.logServiceCall(joinPoint)
        
        // Then
        // Should handle without error
    }
}
