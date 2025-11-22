package com.planit.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(private val jwtTokenProvider: JwtTokenProvider) :
    OncePerRequestFilter() {

  companion object {
    private const val TOKEN_HEADER = "Authorization" // 헤더 이름
    private const val TOKEN_PREFIX = "Bearer " // 접두사
  }

  /** 실제 필터링 로직이 수행되는 곳 */
  override fun doFilterInternal(
      request: HttpServletRequest,
      response: HttpServletResponse,
      filterChain: FilterChain,
  ) {
    // (1) 요청 헤더에서 JWT 토큰을 추출
    resolveToken(request)
        ?.takeIf { jwtTokenProvider.validateToken(it) } // (2) 유효성 검증
        ?.let {
          // (3) 토큰이 유효하면 인증 객체를 받아옵니다.
          val authentication = jwtTokenProvider.getAuthentication(it)
          // (4) SecurityContextHolder에 인증 객체를 저장하여, 이 요청을 인증된 사용자의 요청으로 처리합니다.
          SecurityContextHolder.getContext().authentication = authentication
        }

    // (5) 다음 필터 체인으로 요청을 전달합니다.
    filterChain.doFilter(request, response)
  }

  /** Request Header에서 "Bearer " 접두사를 제거하고 순수 토큰을 반환 */
  private fun resolveToken(request: HttpServletRequest): String? {
    return request
        .getHeader(TOKEN_HEADER)
        ?.takeIf { it.isNotBlank() && it.startsWith(TOKEN_PREFIX) }
        ?.removePrefix(TOKEN_PREFIX)
  }
}
