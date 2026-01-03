package com.planit.service.badge

import com.planit.entity.User
import com.planit.repository.UserRepository
import com.planit.service.BadgeService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PointBadgeCheckerTest {

    @MockK
    private lateinit var badgeService: BadgeService

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var pointBadgeChecker: PointBadgeChecker

    @Test
    @DisplayName("포인트 배지 체크 - 사용자를 찾을 수 없음")
    fun `checkPointBadges should do nothing when user not found`() {
        // Given
        val userLoginId = "testuser"
        every { userRepository.findByLoginId(userLoginId) } returns null

        // When
        pointBadgeChecker.checkPointBadges(userLoginId)

        // Then
        verify(exactly = 0) { badgeService.awardBadge(any(), any()) }
    }

    @Test
    @DisplayName("포인트 배지 체크 - 100점 미만 시 배지 미지급")
    fun `checkPointBadges should not award any badge if points are less than 100`() {
        // Given
        val userLoginId = "testuser"
        val user = User(loginId = userLoginId, password = "pwd", nickname = "nick")
        user.addPoint(99)
        every { userRepository.findByLoginId(userLoginId) } returns user

        // When
        pointBadgeChecker.checkPointBadges(userLoginId)

        // Then
        verify(exactly = 0) { badgeService.awardBadge(any(), any()) }
    }

    @Test
    @DisplayName("포인트 배지 체크 - 100점 이상 시 POINT_100 배지 지급")
    fun `checkPointBadges should award POINT_100 badge if points are 100`() {
        // Given
        val userLoginId = "testuser"
        val user = User(loginId = userLoginId, password = "pwd", nickname = "nick")
        user.addPoint(100)
        every { userRepository.findByLoginId(userLoginId) } returns user
        every { badgeService.awardBadge(userLoginId, "POINT_100") } returns true

        // When
        pointBadgeChecker.checkPointBadges(userLoginId)

        // Then
        verify { badgeService.awardBadge(userLoginId, "POINT_100") }
        verify(exactly = 0) { badgeService.awardBadge(userLoginId, "POINT_1000") }
    }

    @Test
    @DisplayName("포인트 배지 체크 - 1000점 이상 시 모든 단계 배지 지급 시도")
    fun `checkPointBadges should attempt to award all badges if points are 1000`() {
        // Given
        val userLoginId = "testuser"
        val user = User(loginId = userLoginId, password = "pwd", nickname = "nick")
        user.addPoint(1000)
        every { userRepository.findByLoginId(userLoginId) } returns user
        every { badgeService.awardBadge(any(), any()) } returns true

        // When
        pointBadgeChecker.checkPointBadges(userLoginId)

        // Then
        verify { badgeService.awardBadge(userLoginId, "POINT_100") }
        verify { badgeService.awardBadge(userLoginId, "POINT_1000") }
    }
}
