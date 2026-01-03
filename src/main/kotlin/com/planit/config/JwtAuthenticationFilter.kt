package com.planit.config

import com.planit.service.AuthService.Companion.BLACKLIST_PREFIX
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
  private val jwtTokenProvider: JwtTokenProvider,
  private val redisTemplate: RedisTemplate<String, Any>,
) : OncePerRequestFilter() {

  /** OPTIONS 요청(CORS 프리플라이트)은 필터링하지 않음 */
  override fun shouldNotFilter(request: HttpServletRequest): Boolean {
    return request.method == "OPTIONS"
  }

  /** 실제 필터링 로직이 수행되는 곳 */
  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
  ) {
    // (1) 요청 헤더에서 JWT 토큰을 추출

    val token = jwtTokenProvider.resolveToken(request)
    token?.let {
      if (jwtTokenProvider.validateToken(it)) {
        val blacklisted = redisTemplate.opsForValue().get(BLACKLIST_PREFIX + it) != null
        if (!blacklisted) {
          val authentication = jwtTokenProvider.getAuthentication(it)
          SecurityContextHolder.getContext().authentication = authentication
        }
      }
    }

    // (5) 다음 필터 체인으로 요청을 전달합니다.
    filterChain.doFilter(request, response)
  }
}
