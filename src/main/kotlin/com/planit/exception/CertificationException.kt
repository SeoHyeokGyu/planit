package com.planit.exception

import org.springframework.http.HttpStatus

/**
 * 인증 수정 또는 삭제 권한이 없을 때 발생하는 예외입니다.
 * 403 Forbidden 반환
 */
class CertificationUpdateForbiddenException(message: String = "이 인증을 수정할 권한이 없습니다") : 
    BusinessException(message, "FORBIDDEN", HttpStatus.FORBIDDEN)

/**
 * 인증 수정 기한(24시간)이 만료되었을 때 발생하는 예외입니다.
 * 400 Bad Request 반환
 */
class CertificationUpdatePeriodExpiredException(message: String = "인증은 생성 후 24시간 이내에만 수정할 수 있습니다") : 
    BusinessException(message, "UPDATE_PERIOD_EXPIRED", HttpStatus.BAD_REQUEST)