package com.planit.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class CustomExceptionTest {

    @Test
    @DisplayName("Certification 관련 예외 테스트")
    fun `test certification exceptions`() {
        val ex1 = CertificationUpdateForbiddenException("F")
        assertEquals("F", ex1.message)
        assertEquals(HttpStatus.FORBIDDEN, ex1.status)

        val ex2 = CertificationUpdatePeriodExpiredException("E")
        assertEquals("E", ex2.message)
        assertEquals(HttpStatus.BAD_REQUEST, ex2.status)

        val ex3 = CertificationNotStartedException("S")
        assertEquals("S", ex3.message)
        assertEquals(HttpStatus.BAD_REQUEST, ex3.status)

        val ex4 = CertificationEndedException("D")
        assertEquals("D", ex4.message)
        assertEquals(HttpStatus.BAD_REQUEST, ex4.status)
    }

    @Test
    @DisplayName("EntityNotFound 관련 예외 테스트")
    fun `test entity not found exceptions`() {
        val ex1 = UserNotFoundException("U")
        assertEquals("U", ex1.message)
        assertEquals(HttpStatus.NOT_FOUND, ex1.status)

        val ex2 = ChallengeNotFoundException("C")
        assertEquals("C", ex2.message)
        assertEquals(HttpStatus.NOT_FOUND, ex2.status)

        val ex3 = CertificationNotFoundException("Cert")
        assertEquals("Cert", ex3.message)
        assertEquals(HttpStatus.NOT_FOUND, ex3.status)
    }

    @Test
    @DisplayName("Notification 관련 예외 테스트")
    fun `test notification exceptions`() {
        val ex1 = NotificationNotFoundException("N")
        assertEquals("N", ex1.message)

        val ex2 = NotificationAccessForbiddenException("A")
        assertEquals("A", ex2.message)
    }
}
