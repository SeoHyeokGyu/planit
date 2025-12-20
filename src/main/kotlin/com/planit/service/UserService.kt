package com.planit.service

import com.planit.dto.UserDashboardStats
import com.planit.dto.UserPasswordUpdateRequest
import com.planit.dto.UserProfileResponse
import com.planit.dto.UserUpdateRequest
import com.planit.entity.User
import com.planit.enums.ParticipantStatusEnum
import com.planit.repository.CertificationRepository
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.UserRepository
import java.util.NoSuchElementException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val certificationRepository: CertificationRepository,
    private val challengeParticipantRepository: ChallengeParticipantRepository
) {
  fun updateUser(user: User, updateRequest: UserUpdateRequest): UserProfileResponse {
    // ID로 사용자를 다시 조회하여 영속 상태(Managed)로 만듭니다.
    val managedUser =
        userRepository.findByLoginId(user.loginId)
            ?: throw NoSuchElementException("ID ${user.loginId}에 해당하는 사용자를 찾을 수 없습니다.")

    if (!updateRequest.nickname.isNullOrBlank()) {
      managedUser.changeNickname(updateRequest.nickname)
    }

    // 다른 속성 추가시 추가작성.
    return UserProfileResponse.of(managedUser)
  }

  fun updatePassword(loginId: String, request: UserPasswordUpdateRequest) {
    val user =
        userRepository.findByLoginId(loginId)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $loginId")

    if (!passwordEncoder.matches(request.oldPassword, user.password)) {
      throw IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.")
    }

    val newHashedPassword = passwordEncoder.encode(request.newPassword)
    user.changePassword(newHashedPassword)
    // @Transactional에 의해 메소드 종료 시 변경 감지(dirty checking)로 DB에 업데이트됩니다.
  }

  @Transactional(readOnly = true)
  fun getDashboardStats(loginId: String): UserDashboardStats {
    val challengeCount = challengeParticipantRepository.countByLoginIdAndStatus(loginId, ParticipantStatusEnum.ACTIVE)
    val certificationCount = certificationRepository.countByUser_LoginId(loginId)

    return UserDashboardStats(
        challengeCount = challengeCount,
        certificationCount = certificationCount
    )
  }
}
