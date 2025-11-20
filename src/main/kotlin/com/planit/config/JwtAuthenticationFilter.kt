package com.planit.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    companion object {
        private const val TOKEN_HEADER = "Authorization" // 헤더 이름
        private const val TOKEN_PREFIX = "Bearer "       // 접두사
    }

    /**
     * 실제 필터링 로직이 수행되는 곳
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // (1) 요청 헤더에서 JWT 토큰 추출
        val token = resolveToken(request)

        // (2) 토큰이 존재하고 유효한지 검증
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // (3) 토큰이 유효하면 인증 객체를 받아옴
            val authentication = jwtTokenProvider.getAuthentication(token)

            // (4) SecurityContextHolder에 인증 객체 저장
            // => 이 요청은 인증된 사용자의 요청으로 처리됨
            SecurityContextHolder.getContext().authentication = authentication
        }

        // (5) 다음 필터 체인으로 요청 전달
        filterChain.doFilter(request, response)
    }

    /**
     * Request Header에서 "Bearer " 접두사를 제거하고 순수 토큰을 반환
     */
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(TOKEN_HEADER)

        // 헤더가 존재하고 "Bearer "로 시작하는지 확인
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            bearerToken.substring(TOKEN_PREFIX.length) // "Bearer " 이후의 토큰 값 반환
        } else {
            null // 유효하지 않은 경우 null 반환
        }
    }
}