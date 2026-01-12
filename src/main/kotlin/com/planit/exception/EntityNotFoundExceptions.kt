package com.planit.exception

import org.springframework.http.HttpStatus

/**
 * 사용자(User)를 찾을 수 없을 때 발생하는 예외입니다.
 */
class UserNotFoundException(message: String = "사용자를 찾을 수 없습니다") : 
    BusinessException(message, "USER_NOT_FOUND", HttpStatus.NOT_FOUND)

/**
 * 챌린지(Challenge)를 찾을 수 없을 때 발생하는 예외입니다.
 */
class ChallengeNotFoundException(message: String = "챌린지를 찾을 수 없습니다") : 
    BusinessException(message, "CHALLENGE_NOT_FOUND", HttpStatus.NOT_FOUND)

/**
 * 인증(Certification)을 찾을 수 없을 때 발생하는 예외입니다.
 */
class CertificationNotFoundException(message: String = "인증을 찾을 수 없습니다") : 
    BusinessException(message, "CERTIFICATION_NOT_FOUND", HttpStatus.NOT_FOUND)