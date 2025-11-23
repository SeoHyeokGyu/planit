package com.planit.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

  @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun filterChain(http: HttpSecurity, jwtAuthFilter: JwtAuthenticationFilter): SecurityFilterChain {
    http
        .csrf { it.disable() }
        .cors { it.configurationSource(corsConfigurationSource()) }
        .formLogin { it.disable() } // 폼 로그인 비활성화 (JWT 등 토큰 기반 인증 시)
        .httpBasic { it.disable() } // HTTP Basic 인증 비활성화
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
          // Swagger UI 접근 허용
          it.requestMatchers(
                  "/swagger-ui/**",
                  "/v3/api-docs/**",
                  "/swagger-resources/**",
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

  @Bean
  fun corsConfigurationSource(): CorsConfigurationSource {
    val configuration = CorsConfiguration()
    configuration.allowedOrigins = listOf(
      "http://localhost:3000",
      "http://168.107.9.243:3000"
    )
    configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    configuration.allowedHeaders = listOf("*")
    configuration.allowCredentials = true
    configuration.maxAge = 3600L

    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", configuration)
    return source
  }
}
