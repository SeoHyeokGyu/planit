package com.planit.exception

/**
 * 인증 수정 또는 삭제 권한이 없을 때 발생하는 예외입니다.
 * @param message 예외 메시지 (기본값: "이 인증을 수정할 권한이 없습니다")
 */
class CertificationUpdateForbiddenException(message: String = "이 인증을 수정할 권한이 없습니다") : RuntimeException(message)

/**
 * 인증 수정 기한(24시간)이 만료되었을 때 발생하는 예외입니다.
 * @param message 예외 메시지 (기본값: "인증은 생성 후 24시간 이내에만 수정할 수 있습니다")
 */
class CertificationUpdatePeriodExpiredException(message: String = "인증은 생성 후 24시간 이내에만 수정할 수 있습니다") : RuntimeException(message)
