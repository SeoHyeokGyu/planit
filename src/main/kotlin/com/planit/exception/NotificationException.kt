package com.planit.exception

/**
 * 알림(Notification)을 찾을 수 없을 때 발생하는 예외
 * @param message 예외 메시지 (기본값: "알림을 찾을 수 없습니다")
 */
class NotificationNotFoundException(message: String = "알림을 찾을 수 없습니다") : RuntimeException(message)

/**
 * 알림 접근 권한이 없을 때 발생하는 예외
 * @param message 예외 메시지 (기본값: "이 알림에 접근할 권한이 없습니다")
 */
class NotificationAccessForbiddenException(message: String = "이 알림에 접근할 권한이 없습니다") : RuntimeException(message)
