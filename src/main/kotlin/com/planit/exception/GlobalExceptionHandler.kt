package com.planit.exception

import com.planit.dto.ApiResponse
import java.util.NoSuchElementException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  /** 400 Bad Request - JSR-303 Validator 주로 @Valid 또는 @Validated 애노테이션을 사용한 유효성 검사 실패 시 발생합니다. */
  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(
      ex: MethodArgumentNotValidException
  ): ResponseEntity<ApiResponse<Unit>> {
    val errors =
        ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
    log.warn("유효성 검사 오류: {}", errors)
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("INVALID_INPUT", "입력 값 유효성 검사 실패: $errors"))
  }

  /** 400 Bad Request - 부적절한 인자 메서드에 전달된 인자가 부적절할 때 발생합니다. */
  @ExceptionHandler(IllegalArgumentException::class)
  fun handleIllegalArgumentException(
      ex: IllegalArgumentException
  ): ResponseEntity<ApiResponse<Unit>> {
    val message = ex.message ?: "부적절한 인자가 제공되었습니다."
    log.warn("부적절한 인자: {}", message)
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("INVALID_ARGUMENT", message))
  }

  /** 404 Not Found - 리소스를 찾을 수 없음 */
  @ExceptionHandler(NoSuchElementException::class)
  fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<ApiResponse<Unit>> {
    val message = ex.message ?: "요청하신 리소스를 찾을 수 없습니다."
    log.warn("리소스를 찾을 수 없음: {}", message)
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error("RESOURCE_NOT_FOUND", message))
  }

  /** 500 Internal Server Error - 처리되지 않은 모든 예외 서버 내부 로직에서 발생하는 예외를 처리합니다. */
  @ExceptionHandler(Exception::class)
  fun handleGlobalException(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
    log.error("내부 서버 오류", ex)
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ApiResponse.error(
                "INTERNAL_SERVER_ERROR",
                "예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            )
        )
  }
}
