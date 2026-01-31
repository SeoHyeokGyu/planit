package com.planit.service

import com.planit.entity.User
import com.planit.repository.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.userdetails.UsernameNotFoundException

@ExtendWith(MockKExtension::class)
class CustomUserDetailsServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @Test
    @DisplayName("사용자 로드 성공")
    fun `loadUserByUsername should return UserDetails`() {
        // Given
        val loginId = "testuser"
        val user = User(loginId, "password", "nickname")
        every { userRepository.findByLoginId(loginId) } returns user

        // When
        val result = customUserDetailsService.loadUserByUsername(loginId)

        // Then
        assertNotNull(result)
        assertEquals(loginId, result.username)
    }

    @Test
    @DisplayName("사용자 로드 실패 - 사용자 없음")
    fun `loadUserByUsername should throw UsernameNotFoundException`() {
        // Given
        val loginId = "unknown"
        every { userRepository.findByLoginId(loginId) } returns null

        // When & Then
        assertThrows<UsernameNotFoundException> {
            customUserDetailsService.loadUserByUsername(loginId)
        }
    }
}
