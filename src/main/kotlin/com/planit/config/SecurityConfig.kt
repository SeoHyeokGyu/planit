package com.planit.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        // Spring Security 필터 체인을 완전히 무시하는 경로 설정
        return WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers(
                "/api/health",   // Health Check
                "/actuator/**",  // Actuator Endpoints
                "/error"         // Error Page
            )
        }
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Swagger UI 접근 허용
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**"
                    ).permitAll()
                    // 인증 없이 접근 가능한 엔드포인트 (초기 개발 단계)
                    .requestMatchers("/api/**").permitAll()
                    // 위에서 정의한 경로 외 나머지는 인증 필요
                    .anyRequest().authenticated()
            }

        return http.build()
    }
}