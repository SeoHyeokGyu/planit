package com.planit.exception

import org.springframework.http.HttpStatus

/**
 * 비즈니스 로직 상에서 발생하는 예외들의 상위 클래스입니다.
 * 이 예외를 상속받는 모든 예외의 메시지는 사용자에게 그대로 전달될 수 있습니다.
 *
 * @property message 사용자에게 보여줄 에러 메시지
 * @property errorCode 프론트엔드에서 식별할 수 있는 에러 코드 (옵션)
 * @property status HTTP 상태 코드 (기본값: 400 Bad Request)
 */
open class BusinessException(
    override val message: String,
    val errorCode: String = "BAD_REQUEST",
    val status: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException(message)
