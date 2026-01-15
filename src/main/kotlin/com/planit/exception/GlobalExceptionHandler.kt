package com.planit.exception

import com.planit.dto.ApiResponse
import java.util.NoSuchElementException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

// Custom exception imports
import com.planit.exception.UserNotFoundException
import com.planit.exception.ChallengeNotFoundException
import com.planit.exception.CertificationNotFoundException
import com.planit.exception.CertificationUpdateForbiddenException
import com.planit.exception.CertificationUpdatePeriodExpiredException
import com.planit.exception.NotificationNotFoundException
import com.planit.exception.NotificationAccessForbiddenException

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

  /** 404 Not Found - 정적 리소스를 찾을 수 없음 (Swagger UI 등) */
  @ExceptionHandler(NoResourceFoundException::class)
  fun handleNoResourceFoundException(ex: NoResourceFoundException): ResponseEntity<Unit>? {
    // Swagger UI 및 기타 정적 리소스는 Spring이 처리하도록 null 반환
    log.debug("정적 리소스를 찾을 수 없음: {}", ex.message)
    return null
  }

  /** 401 Unauthorized - 인증 실패 (잘못된 자격증명) */
  @ExceptionHandler(BadCredentialsException::class)
  fun handleBadCredentialsException(
      ex: BadCredentialsException
  ): ResponseEntity<ApiResponse<Unit>> {
    log.warn("인증 실패: {}", ex.message)
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다."))
  }

  /** 404 Not Found - 사용자를 찾을 수 없음 */
  @ExceptionHandler(UserNotFoundException::class)
  fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<ApiResponse<Unit>> {
    log.warn("사용자를 찾을 수 없음: {}", ex.message)
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error("USER_NOT_FOUND", ex.message ?: "사용자를 찾을 수 없습니다"))
  }

  /** 404 Not Found - 챌린지를 찾을 수 없음 */
  @ExceptionHandler(ChallengeNotFoundException::class)
  fun handleChallengeNotFoundException(ex: ChallengeNotFoundException): ResponseEntity<ApiResponse<Unit>> {
    log.warn("챌린지를 찾을 수 없음: {}", ex.message)
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error("CHALLENGE_NOT_FOUND", ex.message ?: "챌린지를 찾을 수 없습니다"))
  }

  /** 404 Not Found - 인증을 찾을 수 없음 */
  @ExceptionHandler(CertificationNotFoundException::class)
  fun handleCertificationNotFoundException(ex: CertificationNotFoundException): ResponseEntity<ApiResponse<Unit>> {
    log.warn("인증을 찾을 수 없음: {}", ex.message)
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error("CERTIFICATION_NOT_FOUND", ex.message ?: "인증을 찾을 수 없습니다"))
  }

  /** 403 Forbidden - 인증 수정/삭제 권한 없음 */
  @ExceptionHandler(CertificationUpdateForbiddenException::class)
  fun handleCertificationUpdateForbiddenException(ex: CertificationUpdateForbiddenException): ResponseEntity<ApiResponse<Unit>> {
    log.warn("인증 권한 없음: {}", ex.message)
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error("CERTIFICATION_FORBIDDEN", ex.message ?: "이 인증을 수정할 권한이 없습니다"))
  }

  /** 400 Bad Request - 인증 수정 기한 만료 */
  @ExceptionHandler(CertificationUpdatePeriodExpiredException::class)
  fun handleCertificationUpdatePeriodExpiredException(ex: CertificationUpdatePeriodExpiredException): ResponseEntity<ApiResponse<Unit>> {
    log.warn("인증 수정 기한 만료: {}", ex.message)
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("CERTIFICATION_UPDATE_EXPIRED", ex.message ?: "인증은 생성 후 24시간 이내에만 수정할 수 있습니다"))
  }

  /** 404 Not Found - 알림을 찾을 수 없음 */
  @ExceptionHandler(NotificationNotFoundException::class)
  fun handleNotificationNotFoundException(ex: NotificationNotFoundException): ResponseEntity<ApiResponse<Unit>> {
    log.warn("알림을 찾을 수 없음: {}", ex.message)
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error("NOTIFICATION_NOT_FOUND", ex.message ?: "알림을 찾을 수 없습니다"))
  }

  /** 403 Forbidden - 알림 접근 권한 없음 */
  @ExceptionHandler(NotificationAccessForbiddenException::class)
  fun handleNotificationAccessForbiddenException(ex: NotificationAccessForbiddenException): ResponseEntity<ApiResponse<Unit>> {
    log.warn("알림 접근 권한 없음: {}", ex.message)
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error("NOTIFICATION_FORBIDDEN", ex.message ?: "이 알림에 접근할 권한이 없습니다"))
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
