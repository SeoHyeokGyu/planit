package com.planit.util

import com.planit.dto.CustomUserDetails
import com.planit.entity.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
annotation class WithMockCustomUser(
    val loginId: String = "testuser",
    val nickname: String = "테스트 사용자",
    val userId: Long = 1L
)

class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {
    override fun createSecurityContext(annotation: WithMockCustomUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val user = User(
            loginId = annotation.loginId,
            password = "password",
            nickname = annotation.nickname
        ).apply {
            setPrivateProperty("id", annotation.userId)
        }
        val userDetails = CustomUserDetails(user)
        val auth = UsernamePasswordAuthenticationToken(userDetails, "password", userDetails.authorities)
        context.authentication = auth
        return context
    }
}
