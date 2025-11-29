package com.planit.exception

class CertificationNotFoundException(message: String = "Certification not found") : RuntimeException(message)
class CertificationUpdateForbiddenException(message: String = "You are not allowed to update this certification") : RuntimeException(message)
class CertificationUpdatePeriodExpiredException(message: String = "You can only update a certification within 24 hours of its creation") : RuntimeException(message)
class ChallengeNotFoundException(message: String = "Challenge not found") : RuntimeException(message)
class UserNotFoundException(message: String = "User not found") : RuntimeException(message)
