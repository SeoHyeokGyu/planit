package com.planit.service

import com.planit.entity.Badge
import com.planit.entity.User
import com.planit.entity.UserBadge
import com.planit.repository.BadgeRepository
import com.planit.repository.UserBadgeRepository
import com.planit.repository.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class BadgeServiceTest {

    @MockK
    private lateinit var badgeRepository: BadgeRepository

    @MockK
    private lateinit var userBadgeRepository: UserBadgeRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var badgeService: BadgeService

    private lateinit var user: User
    private lateinit var badge: Badge

    @BeforeEach
    fun setUp() {
        user = User(
            loginId = "testuser",
            password = "password",
            nickname = "tester"
        )
        // Reflection to set ID
        val idField = User::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(user, 1L)

        badge = Badge(
            code = "TEST_BADGE",
            name = "Test Badge",
            description = "This is a test badge",
            iconCode = "TEST_ICON",
            type = com.planit.enums.BadgeType.CERTIFICATION_COUNT,
            grade = com.planit.enums.BadgeGrade.BRONZE,
            requiredValue = 10
        )
    }

    @Test
    @DisplayName("배지 지급 성공 - 새로운 배지 획득")
    fun `awardBadge should return true when user acquires a new badge`() {
        // Given
        every { userRepository.findByLoginId(user.loginId) } returns user
        every { badgeRepository.findByCode(badge.code) } returns badge
        every { userBadgeRepository.existsByUserIdAndBadgeCode(user.id!!, badge.code) } returns false
        every { userBadgeRepository.save(any()) } returns UserBadge(user = user, badge = badge)

        // When
        val result = badgeService.awardBadge(user.loginId, badge.code)

        // Then
        assertTrue(result)
        verify { userBadgeRepository.save(any()) }
    }

    @Test
    @DisplayName("배지 지급 실패 - 사용자를 찾을 수 없음")
    fun `awardBadge should throw IllegalArgumentException when user not found`() {
        // Given
        every { userRepository.findByLoginId(user.loginId) } returns null

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            badgeService.awardBadge(user.loginId, badge.code)
        }
        assertEquals("User not found", exception.message)
    }

    @Test
    @DisplayName("배지 지급 실패 - 배지를 찾을 수 없음")
    fun `awardBadge should return false when badge not found`() {
        // Given
        every { userRepository.findByLoginId(user.loginId) } returns user
        every { badgeRepository.findByCode(badge.code) } returns null

        // When
        val result = badgeService.awardBadge(user.loginId, badge.code)

        // Then
        assertFalse(result)
        verify(exactly = 0) { userBadgeRepository.save(any()) }
    }

    @Test
    @DisplayName("배지 지급 실패 - 이미 획득한 배지")
    fun `awardBadge should return false when user already has the badge`() {
        // Given
        every { userRepository.findByLoginId(user.loginId) } returns user
        every { badgeRepository.findByCode(badge.code) } returns badge
        every { userBadgeRepository.existsByUserIdAndBadgeCode(user.id!!, badge.code) } returns true

        // When
        val result = badgeService.awardBadge(user.loginId, badge.code)

        // Then
        assertFalse(result)
        verify(exactly = 0) { userBadgeRepository.save(any()) }
    }

    @Test
    @DisplayName("전체 배지 목록 조회 - 로그인하지 않음")
    fun `getAllBadges should return all badges with isAcquired false when user not logged in`() {
        // Given
        every { badgeRepository.findAll() } returns listOf(badge)

        // When
        val result = badgeService.getAllBadges(null)

        // Then
        assertEquals(1, result.size)
        assertFalse(result[0].isAcquired)
        assertNull(result[0].acquiredAt)
    }

    @Test
    @DisplayName("전체 배지 목록 조회 - 로그인함")
    fun `getAllBadges should return all badges with correct isAcquired status when user logged in`() {
        // Given
        val userBadge = UserBadge(user = user, badge = badge, acquiredAt = LocalDateTime.now())
        every { badgeRepository.findAll() } returns listOf(badge)
        every { userBadgeRepository.findByUserLoginId(user.loginId) } returns listOf(userBadge)

        // When
        val result = badgeService.getAllBadges(user.loginId)

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0].isAcquired)
        assertEquals(userBadge.acquiredAt, result[0].acquiredAt)
    }

    @Test
    @DisplayName("내 배지 목록 조회")
    fun `getMyBadges should return only acquired badges`() {
        // Given
        val userBadge = UserBadge(user = user, badge = badge, acquiredAt = LocalDateTime.now())
        every { userBadgeRepository.findByUserLoginId(user.loginId) } returns listOf(userBadge)

        // When
        val result = badgeService.getMyBadges(user.loginId)

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0].isAcquired)
        assertEquals(badge.code, result[0].code)
    }
}
