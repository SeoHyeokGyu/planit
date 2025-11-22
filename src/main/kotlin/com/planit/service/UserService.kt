package com.planit.service

import com.planit.dto.UserPasswordUpdateRequest
import com.planit.dto.UserProfileResponse
import com.planit.dto.UserUpdateRequest
import com.planit.entity.User
import com.planit.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
  fun updateUser(user: User, updateRequest: UserUpdateRequest): UserProfileResponse {
    if (!updateRequest.nickname.isNullOrBlank()) {
      user.changeNickname(updateRequest.nickname)
    }
    // 다른 속성 추가시 추가작성.

    return UserProfileResponse.of(user)
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
}
