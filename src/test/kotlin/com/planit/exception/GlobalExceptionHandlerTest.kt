package com.planit.exception

import com.planit.dto.ApiResponse
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.servlet.resource.NoResourceFoundException
import org.springframework.http.HttpMethod

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    @DisplayName("BusinessException 처리 테스트")
    fun `handleBusinessException test`() {
        val ex = object : BusinessException(message = "Message", errorCode = "CODE", status = HttpStatus.BAD_REQUEST) {}
        val response = handler.handleBusinessException(ex)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("CODE", response.body?.error?.code)
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 처리 테스트")
    fun `handleMethodArgumentNotValidException test`() {
        val bindingResult = mockk<BindingResult>()
        val fieldError = FieldError("obj", "field", "rejected", false, null, null, "default message")
        every { bindingResult.fieldErrors } returns listOf(fieldError)
        
        val ex = MethodArgumentNotValidException(mockk(), bindingResult)
        val response = handler.handleMethodArgumentNotValidException(ex)
        
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("INVALID_INPUT", response.body?.error?.code)
    }

    @Test
    @DisplayName("IllegalArgumentException 처리 테스트")
    fun `handleIllegalArgumentException test`() {
        val ex = IllegalArgumentException("Invalid")
        val response = handler.handleIllegalArgumentException(ex)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Invalid", response.body?.error?.message)
    }

    @Test
    @DisplayName("NoSuchElementException 처리 테스트")
    fun `handleNoSuchElementException test`() {
        val ex = NoSuchElementException("Missing")
        val response = handler.handleNoSuchElementException(ex)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Missing", response.body?.error?.message)
    }

    @Test
    @DisplayName("NoResourceFoundException 처리 테스트")
    fun `handleNoResourceFoundException test`() {
        val ex = NoResourceFoundException(HttpMethod.GET, "/static")
        val response = handler.handleNoResourceFoundException(ex)
        assertEquals(null, response)
    }

    @Test
    @DisplayName("사용자 정의 404 예외들 처리 테스트")
    fun `custom 404 exceptions test`() {
        assertEquals(HttpStatus.NOT_FOUND, handler.handleUserNotFoundException(UserNotFoundException("U")).statusCode)
        assertEquals(HttpStatus.NOT_FOUND, handler.handleChallengeNotFoundException(ChallengeNotFoundException("C")).statusCode)
        assertEquals(HttpStatus.NOT_FOUND, handler.handleCertificationNotFoundException(CertificationNotFoundException("Cert")).statusCode)
        assertEquals(HttpStatus.NOT_FOUND, handler.handleNotificationNotFoundException(NotificationNotFoundException("N")).statusCode)
    }

    @Test
    @DisplayName("권한 및 기한 만료 예외 처리 테스트")
    fun `forbidden and expired exceptions test`() {
        assertEquals(HttpStatus.FORBIDDEN, handler.handleCertificationUpdateForbiddenException(CertificationUpdateForbiddenException("F")).statusCode)
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleCertificationUpdatePeriodExpiredException(CertificationUpdatePeriodExpiredException("E")).statusCode)
        assertEquals(HttpStatus.FORBIDDEN, handler.handleNotificationAccessForbiddenException(NotificationAccessForbiddenException("NF")).statusCode)
    }

    @Test
    @DisplayName("일반 Exception 처리 테스트")
    fun `handleGlobalException test`() {
        val response = handler.handleGlobalException(Exception("Global"))
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }
}
