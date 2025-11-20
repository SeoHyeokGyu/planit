package com.planit.service

import com.planit.config.JwtTokenProvider
import com.planit.dto.LoginRequest
import com.planit.dto.LoginResponse
import com.planit.dto.SignUpRequest
import com.planit.dto.CustomUserDetails
import com.planit.entity.User
import com.planit.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
) {

  /** 회원가입 */
  @Transactional
  fun signUp(request: SignUpRequest): User {
    // (1) 이메일 중복 체크
    if (userRepository.findByLoginId(request.loginId) != null) {
      throw IllegalArgumentException("이미 사용 중인 ID입니다.")
    }

    // (2) 사용자 생성
    val user =
        User(
            loginId = request.loginId,
            password = passwordEncoder.encode(request.password), // (3) 비밀번호 암호화
            nickname = request.nickname,
        )

    // (4) DB에 저장
    return userRepository.save(user)
  }

  /** 로그인 */
  @Transactional
  fun login(request: LoginRequest): LoginResponse {
    val authentication: Authentication =
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.loginId, request.password)
        )

    SecurityContextHolder.getContext().authentication = authentication

    // (1) principal이 이제 CustomUserDetails 타입
    val userDetails = authentication.principal as CustomUserDetails

    // (2) JwtTokenProvider에 넘길 때 userDetails에서 필요한 값을 꺼내 씀
    val accessToken =
        jwtTokenProvider.createToken(
            userDetails.username, // user.loginId
        )
    return LoginResponse(accessToken = accessToken)
  }
}
