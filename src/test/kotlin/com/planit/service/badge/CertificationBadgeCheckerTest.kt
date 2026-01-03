package com.planit.service.badge

import com.planit.repository.CertificationRepository
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
class CertificationBadgeCheckerTest {

    @MockK
    private lateinit var badgeService: BadgeService

    @MockK
    private lateinit var certificationRepository: CertificationRepository

    @InjectMockKs
    private lateinit var certificationBadgeChecker: CertificationBadgeChecker

    @Test
    @DisplayName("인증 배지 체크 - 1회 미만 인증 시 배지 미지급")
    fun `checkCertificationBadges should not award any badge if count is less than 1`() {
        // Given
        val userLoginId = "testuser"
        every { certificationRepository.countByUser_LoginId(userLoginId) } returns 0L

        // When
        certificationBadgeChecker.checkCertificationBadges(userLoginId)

        // Then
        verify(exactly = 0) { badgeService.awardBadge(any(), any()) }
    }

    @Test
    @DisplayName("인증 배지 체크 - 1회 이상 인증 시 CERT_1 배지 지급")
    fun `checkCertificationBadges should award CERT_1 badge if count is 1`() {
        // Given
        val userLoginId = "testuser"
        every { certificationRepository.countByUser_LoginId(userLoginId) } returns 1L
        every { badgeService.awardBadge(userLoginId, "CERT_1") } returns true

        // When
        certificationBadgeChecker.checkCertificationBadges(userLoginId)

        // Then
        verify { badgeService.awardBadge(userLoginId, "CERT_1") }
        verify(exactly = 0) { badgeService.awardBadge(userLoginId, "CERT_10") }
        verify(exactly = 0) { badgeService.awardBadge(userLoginId, "CERT_50") }
        verify(exactly = 0) { badgeService.awardBadge(userLoginId, "CERT_100") }
    }

    @Test
    @DisplayName("인증 배지 체크 - 100회 이상 인증 시 모든 단계 배지 지급 시도")
    fun `checkCertificationBadges should attempt to award all badges if count is 100`() {
        // Given
        val userLoginId = "testuser"
        every { certificationRepository.countByUser_LoginId(userLoginId) } returns 100L
        every { badgeService.awardBadge(any(), any()) } returns true

        // When
        certificationBadgeChecker.checkCertificationBadges(userLoginId)

        // Then
        verify { badgeService.awardBadge(userLoginId, "CERT_1") }
        verify { badgeService.awardBadge(userLoginId, "CERT_10") }
        verify { badgeService.awardBadge(userLoginId, "CERT_50") }
        verify { badgeService.awardBadge(userLoginId, "CERT_100") }
    }
}
