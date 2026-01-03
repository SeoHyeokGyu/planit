package com.planit.service.badge

import com.planit.repository.FollowRepository
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
class SocialBadgeCheckerTest {

    @MockK
    private lateinit var badgeService: BadgeService

    @MockK
    private lateinit var followRepository: FollowRepository

    @InjectMockKs
    private lateinit var socialBadgeChecker: SocialBadgeChecker

    @Test
    @DisplayName("소셜 배지 체크 - 팔로워 0명 시 배지 미지급")
    fun `checkFollowerBadges should not award any badge if follower count is 0`() {
        // Given
        val userLoginId = "testuser"
        every { followRepository.countByFollowing_LoginId(userLoginId) } returns 0L

        // When
        socialBadgeChecker.checkFollowerBadges(userLoginId)

        // Then
        verify(exactly = 0) { badgeService.awardBadge(any(), any()) }
    }

    @Test
    @DisplayName("소셜 배지 체크 - 팔로워 1명 이상 시 SOCIAL_1 배지 지급")
    fun `checkFollowerBadges should award SOCIAL_1 badge if follower count is 1`() {
        // Given
        val userLoginId = "testuser"
        every { followRepository.countByFollowing_LoginId(userLoginId) } returns 1L
        every { badgeService.awardBadge(userLoginId, "SOCIAL_1") } returns true

        // When
        socialBadgeChecker.checkFollowerBadges(userLoginId)

        // Then
        verify { badgeService.awardBadge(userLoginId, "SOCIAL_1") }
        verify(exactly = 0) { badgeService.awardBadge(userLoginId, "SOCIAL_10") }
        verify(exactly = 0) { badgeService.awardBadge(userLoginId, "SOCIAL_100") }
    }

    @Test
    @DisplayName("소셜 배지 체크 - 팔로워 100명 이상 시 모든 단계 배지 지급 시도")
    fun `checkFollowerBadges should attempt to award all badges if follower count is 100`() {
        // Given
        val userLoginId = "testuser"
        every { followRepository.countByFollowing_LoginId(userLoginId) } returns 100L
        every { badgeService.awardBadge(any(), any()) } returns true

        // When
        socialBadgeChecker.checkFollowerBadges(userLoginId)

        // Then
        verify { badgeService.awardBadge(userLoginId, "SOCIAL_1") }
        verify { badgeService.awardBadge(userLoginId, "SOCIAL_10") }
        verify { badgeService.awardBadge(userLoginId, "SOCIAL_100") }
    }
}
