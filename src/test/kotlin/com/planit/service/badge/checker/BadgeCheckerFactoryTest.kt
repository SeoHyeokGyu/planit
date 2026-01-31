package com.planit.service.badge.checker

import com.planit.enums.BadgeType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BadgeCheckerFactoryTest {

    @Test
    @DisplayName("배지 체커 조회 성공")
    fun `getChecker should return matching checker`() {
        // Given
        val checker = mockk<BadgeConditionChecker>()
        every { checker.supportedType } returns BadgeType.CERTIFICATION_STREAK
        val factory = BadgeCheckerFactory(listOf(checker))

        // When
        val result = factory.getChecker(BadgeType.CERTIFICATION_STREAK)

        // Then
        assertEquals(checker, result)
    }

    @Test
    @DisplayName("배지 체커 조회 실패 - 지원하지 않는 타입")
    fun `getChecker should throw exception for unsupported type`() {
        // Given
        val factory = BadgeCheckerFactory(emptyList())

        // When & Then
        assertThrows<IllegalArgumentException> {
            factory.getChecker(BadgeType.POINT_ACCUMULATION)
        }
    }
}
