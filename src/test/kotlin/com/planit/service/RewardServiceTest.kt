package com.planit.service

import com.planit.entity.User
import com.planit.enums.BadgeType
import com.planit.enums.RewardType
import com.planit.exception.UserNotFoundException
import com.planit.repository.UserRepository
import com.planit.service.badge.BadgeService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class RewardServiceTest {

    @MockK
    private lateinit var userPointService: UserPointService

    @MockK
    private lateinit var badgeService: BadgeService

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var rankingService: RankingService

    @InjectMockKs
    private lateinit var rewardService: RewardService

    @Test
    @DisplayName("보상 지급 성공 - 모든 서비스 호출 확인")
    fun `grantReward should call all related services`() {
        // Given
        val loginId = "testuser"
        val rewardType = RewardType.CERTIFICATION
        val user = User(loginId, "p", "n")
        
        every { userPointService.addPoint(any(), any(), any()) } just Runs
        every { rankingService.incrementScore(any(), any()) } just Runs
        every { userRepository.findByLoginId(loginId) } returns user
        every { badgeService.checkAndAwardBadges(user, BadgeType.POINT_ACCUMULATION) } returns 0

        // When
        rewardService.grantReward(loginId, rewardType)

        // Then
        verify { userPointService.addPoint(loginId, rewardType.points, rewardType.description) }
        verify { rankingService.incrementScore(loginId, rewardType.points) }
        verify { badgeService.checkAndAwardBadges(user, BadgeType.POINT_ACCUMULATION) }
    }

    @Test
    @DisplayName("보상 지급 실패 - 사용자 없음")
    fun `grantReward should throw exception when user not found`() {
        // Given
        val loginId = "unknown"
        every { userPointService.addPoint(any(), any(), any()) } just Runs
        every { rankingService.incrementScore(any(), any()) } just Runs
        every { userRepository.findByLoginId(loginId) } returns null

        // When & Then
        assertThrows<UserNotFoundException> {
            rewardService.grantReward(loginId, RewardType.CERTIFICATION)
        }
    }

    @Test
    @DisplayName("인증 보상 지급 편리 메서드 호출 확인")
    fun `grantCertificationReward should call grantReward`() {
        // Given
        val loginId = "testuser"
        val user = User(loginId, "p", "n")
        every { userPointService.addPoint(any(), any(), any()) } just Runs
        every { rankingService.incrementScore(any(), any()) } just Runs
        every { userRepository.findByLoginId(loginId) } returns user
        every { badgeService.checkAndAwardBadges(any(), any()) } returns 0

        // When
        rewardService.grantCertificationReward(loginId)
        rewardService.grantCommentReward(loginId)
        rewardService.grantLikeReward(loginId)
        rewardService.grantBadgeReward(loginId)

        // Then
        verify(exactly = 4) { userRepository.findByLoginId(loginId) }
    }
}
