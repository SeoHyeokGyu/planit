package com.planit.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class BusinessExceptionTest {

    @Test
    @DisplayName("BusinessException 생성자 및 프로퍼티 테스트")
    fun `test BusinessException properties`() {
        val message = "Test Message"
        val errorCode = "TEST_ERROR"
        val status = HttpStatus.NOT_FOUND
        
        val ex = BusinessException(message, errorCode, status)
        
        assertEquals(message, ex.message)
        assertEquals(errorCode, ex.errorCode)
        assertEquals(status, ex.status)
    }

    @Test
    @DisplayName("BusinessException 기본값 테스트")
    fun `test BusinessException defaults`() {
        val ex = BusinessException("Default")
        
        assertEquals("Default", ex.message)
        assertEquals("BAD_REQUEST", ex.errorCode)
        assertEquals(HttpStatus.BAD_REQUEST, ex.status)
    }
}
