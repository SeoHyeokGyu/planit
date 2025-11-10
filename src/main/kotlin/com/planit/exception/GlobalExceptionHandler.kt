package com.planit.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<Map<String, String>> {
        val errorResponse = mapOf(
            "error" to "Not Found",
            "message" to (ex.message ?: "Resource not found")
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
}
