package com.planit.config

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {

  @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun filterChain(http: HttpSecurity, jwtAuthFilter: JwtAuthenticationFilter): SecurityFilterChain {
    http
        .csrf { it.disable() }
        .cors { it.disable() } // CorsFilter를 직접 추가하므로 비활성화
        .formLogin { it.disable() } // 폼 로그인 비활성화 (JWT 등 토큰 기반 인증 시)
        .httpBasic { it.disable() } // HTTP Basic 인증 비활성화
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
          // CORS 프리플라이트 요청 (OPTIONS) 모두 허용
          it.requestMatchers(HttpMethod.OPTIONS, "/**")
              .permitAll()
              // Swagger UI 접근 허용
              .requestMatchers(
                  "/swagger-ui/**",
                  "/swagger-ui.html",
                  "/v3/api-docs/**",
                  "/swagger-resources/**",
                  "/webjars/**",
                  "/api/health",
                  "/api/beans",
              )
              .permitAll()
              // 프론트엔드 정적 파일 허용 (통합 배포)
              .requestMatchers(
                  "/",
                  "/index.html",
                  "/*.ico",
                  "/*.json",
                  "/*.png",
                  "/*.svg",
                  "/_next/**",
                  "/api-test/**",
              )
              .permitAll()
              // 인증 관련 엔드포인트 허용
              .requestMatchers("/api/auth/**")
              .permitAll()
              // 나머지는 인증 필요
              .anyRequest()
              .authenticated()
        }
        .exceptionHandling {
            it.authenticationEntryPoint { _, response, _ ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
            }
        }
        // ★★★ CORS 필터를 Security 필터 체인의 맨 앞에 추가 ★★★
        .addFilterBefore(CorsFilter(corsConfigurationSource()), UsernamePasswordAuthenticationFilter::class.java)
        // ★★★ JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가 ★★★
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

    return http.build()
  }

  @Bean
  fun authenticationManager(
      authenticationConfiguration: AuthenticationConfiguration
  ): AuthenticationManager {
    return authenticationConfiguration.authenticationManager
  }

  private fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
    val configuration = CorsConfiguration()
    // 모든 origin 허용 (개발 환경용)
    configuration.allowedOriginPatterns = listOf("*")
    configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
    configuration.allowedHeaders = listOf("*")
    configuration.exposedHeaders = listOf("Authorization", "Content-Type")
    configuration.allowCredentials = false
    configuration.maxAge = 3600L

    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", configuration)
    return source
  }
}
