package com.planit.exception

/**
 * 사용자(User)를 찾을 수 없을 때 발생하는 예외입니다.
 * @param message 예외 메시지 (기본값: "사용자를 찾을 수 없습니다")
 */
class UserNotFoundException(message: String = "사용자를 찾을 수 없습니다") : RuntimeException(message)

/**
 * 챌린지(Challenge)를 찾을 수 없을 때 발생하는 예외입니다.
 * @param message 예외 메시지 (기본값: "챌린지를 찾을 수 없습니다")
 */
class ChallengeNotFoundException(message: String = "챌린지를 찾을 수 없습니다") : RuntimeException(message)

/**
 * 인증(Certification)을 찾을 수 없을 때 발생하는 예외입니다.
 * @param message 예외 메시지 (기본값: "인증을 찾을 수 없습니다")
 */
class CertificationNotFoundException(message: String = "인증을 찾을 수 없습니다") : RuntimeException(message)
