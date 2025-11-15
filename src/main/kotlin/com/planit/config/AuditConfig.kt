package com.planit.config

import com.planit.dto.CustomUserDetails
import java.util.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder

@Configuration
@EnableJpaAuditing(
    auditorAwareRef = "auditorProvider"
) // auditorProvider라는 이름의 AuditorAware 빈을 사용하도록 설정
class AuditConfig {

  @Bean
  fun auditorProvider(): AuditorAware<String> {
    return AuditorAware {
      Optional.ofNullable(SecurityContextHolder.getContext().authentication)
          .filter { it.isAuthenticated } // 인증된 사용자인지 확인
          .map { it.principal } // principal 객체를 가져옴
          .map { principal ->
            // principal이 CustomUserDetails 타입이면 사용자의 loginId를, 그렇지 않으면 principal의 toString()을 반환
            when (principal) {
              is CustomUserDetails -> principal.user.loginId
              else -> principal.toString()
            }
          }
    }
  }
}
