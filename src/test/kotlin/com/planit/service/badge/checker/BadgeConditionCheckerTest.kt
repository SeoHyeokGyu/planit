package com.planit.service.badge.checker

import com.planit.entity.User
import com.planit.enums.BadgeType
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BadgeConditionCheckerTest {

    // Helper class to test default implementation of interface
    class TestChecker : BadgeConditionChecker {
        override val supportedType: BadgeType = BadgeType.CERTIFICATION_STREAK
        override fun getCurrentValue(user: User): Long = 10L
    }

    @Test
    @DisplayName("기본 check 메서드 동작 확인 - 조건 만족")
    fun `check should return true when value is sufficient`() {
        val checker = TestChecker()
        val user = mockk<User>()
        assertTrue(checker.check(user, 5L))
        assertTrue(checker.check(user, 10L))
    }

    @Test
    @DisplayName("기본 check 메서드 동작 확인 - 조건 미달")
    fun `check should return false when value is insufficient`() {
        val checker = TestChecker()
        val user = mockk<User>()
        assertFalse(checker.check(user, 11L))
    }
}
