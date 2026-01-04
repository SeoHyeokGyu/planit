package com.planit.config

import com.planit.service.CustomUserDetailsService
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @param:Value("${"$"}{jwt.secret}") private val secretString: String,
    @param:Value("${"$"}{jwt.expiration}") private val validityInMilliseconds: Long,
    private val customUserDetailsService: CustomUserDetailsService,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    private const val TOKEN_PREFIX = "Bearer " // 접두사
  }

  private lateinit var secretKey: SecretKey

  @PostConstruct
  private fun init() {
    secretKey = Keys.hmacShaKeyFor(secretString.toByteArray())
  }

  /** 인증 정보(loginId)를 기반으로 JWT를 생성합니다. */
  fun createToken(loginId: String): String {
    val now = Date()
    val validity = Date(now.time + validityInMilliseconds)

    // (4) JWT 생성
    return Jwts.builder()
        .subject(loginId)
        .issuedAt(now)
        .expiration(validity)
        .signWith(secretKey) // (5) 비밀 키로 서명
        .compact()
  }

  /** 토큰을 파싱하여 클레임(Payload)을 추출합니다. (검증과 추출을 동시에 수행) */
  private fun getClaims(token: String): Claims {
    return Jwts.parser()
        .verifyWith(secretKey) // (8) verifyWith: 검증에 사용할 키
        .build()
        .parseSignedClaims(token) // (9) parseSignedClaims: 서명된 토큰 파싱
        .payload
  }

  /** 토큰에서 인증(Authentication) 객체를 추출합니다. (JWT 필터에서 사용) */
  fun getAuthentication(token: String): Authentication {
    val claims = getClaims(token)
    val userDetails = customUserDetailsService.loadUserByUsername(claims.subject)
    return UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
  }

  /** 토큰의 유효성을 검증합니다. (만료, 서명 위조 등) */
  fun validateToken(token: String): Boolean {
    try {
      getClaims(token) // (13) 파싱 시도 (실패 시 예외 발생)
      return true
    } catch (e: Exception) {
      // (14) 다양한 예외 처리
      when (e) {
        is SecurityException,
        is MalformedJwtException -> logger.warn("잘못된 JWT 서명입니다.")
        is ExpiredJwtException -> logger.warn("만료된 JWT 토큰입니다.")
        is UnsupportedJwtException -> logger.warn("지원되지 않는 JWT 토큰입니다.")
        is IllegalArgumentException -> logger.warn("JWT 클레임이 비어있습니다.")
        is SignatureException -> logger.warn("JWT 서명 검증에 실패했습니다.")
        else -> logger.warn("JWT 토큰 처리 중 알 수 없는 오류 발생: ${e.message}")
      }
      return false
    }
  }

  fun resolveToken(request: HttpServletRequest): String? {
    val bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION)
    if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
      return bearerToken.substring(7)
    }
    // SSE 연결 등 헤더를 사용할 수 없는 경우 쿼리 파라미터에서 토큰 추출
    return request.getParameter("token")
  }

  /** 토큰의 남은 유효 시간(밀리초)을 계산합니다. */
  fun getRemainingTime(token: String): Long {
    try {
      // 1. 토큰 파싱하여 Claims(내용) 추출
      val claims = getClaims(token)

      // 2. 토큰의 만료 시간(exp) 가져오기
      val expiration: Date = claims.expiration

      // 3. 현재 시간과의 차이 계산
      val now = Date().time
      return expiration.time - now
    } catch (_: Exception) {
      // 토큰이 잘못되었거나 이미 만료된 경우 등 파싱 실패 시 0 반환
      // (이미 만료된 토큰은 블랙리스트에 넣을 필요가 없으므로 0 리턴이 안전함)
      return 0
    }
  }
}
