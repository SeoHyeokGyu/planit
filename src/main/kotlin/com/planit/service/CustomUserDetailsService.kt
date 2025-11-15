package com.planit.service

import com.planit.dto.CustomUserDetails
import com.planit.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

  /**
   * Spring Security가 인증 시 호출하는 메서드
   *
   * @param loginId 여기서는 loginId를 username으로 사용합니다.
   * @return UserDetails를 구현한 User 객체
   * @throws UsernameNotFoundException 해당 loginId의 유저가 없을 경우
   */
  override fun loadUserByUsername(loginId: String): UserDetails {
    val user =
        userRepository.findByLoginId(loginId)
            ?: throw UsernameNotFoundException("해당 ID를 가진 유저를 찾을 수 없습니다: $loginId")
    return CustomUserDetails(user)
  }
}
