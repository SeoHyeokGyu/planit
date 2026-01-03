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

/** 포인트 획득량에 따른 배지 자동 지급 조건을 검증하는 테스트 클래스입니다. */
@ExtendWith(MockKExtension::class)
class PointBadgeCheckerTest {
  @MockK private lateinit var badgeService: BadgeService
  @MockK private lateinit var userRepository: UserRepository
  @InjectMockKs private lateinit var pointBadgeChecker: PointBadgeChecker

  @Test
  @DisplayName("포인트 배지 체크 - 사용자를 찾을 수 없음")
  fun `checkPointBadges should do nothing when user not found`() {
    // Given: 존재하지 않는 사용자 ID인 경우 설정
    val userLoginId = "testuser"
    every { userRepository.findByLoginId(userLoginId) } returns null

    // When: 배지 획득 조건 체크 실행
            pointBadgeChecker.checkBadges(userLoginId)
    // Then: 어떠한 로직도 실행되지 않아야 함
    verify(exactly = 0) { badgeService.awardBadge(any(), any()) }
  }

  @Test
  @DisplayName("포인트 배지 체크 - 100점 미만 시 배지 미지급")
  fun `checkPointBadges should not award any badge if points are less than 100`() {
    // Given: 보유 포인트가 99점인 사용자 설정
    val userLoginId = "testuser"
    val user = User(loginId = userLoginId, password = "pwd", nickname = "nick")
    user.addPoint(99)
    every { userRepository.findByLoginId(userLoginId) } returns user

    // When: 배지 획득 조건 체크 실행
            pointBadgeChecker.checkBadges(userLoginId)
    // Then: 배지 지급이 호출되지 않아야 함
    verify(exactly = 0) { badgeService.awardBadge(any(), any()) }
  }

  @Test
  @DisplayName("포인트 배지 체크 - 100점 이상 시 POINT_100 배지 지급")
  fun `checkPointBadges should award POINT_100 badge if points are 100`() {
    // Given: 보유 포인트가 딱 100점인 사용자 설정
    val userLoginId = "testuser"
    val user = User(loginId = userLoginId, password = "pwd", nickname = "nick")
    user.addPoint(100)
    every { userRepository.findByLoginId(userLoginId) } returns user
    every { badgeService.awardBadge(userLoginId, "POINT_100") } returns true

    // When: 배지 획득 조건 체크 실행
            pointBadgeChecker.checkBadges(userLoginId)
    // Then: POINT_100 배지 지급이 호출되었는지 확인
    verify { badgeService.awardBadge(userLoginId, "POINT_100") }
    verify(exactly = 0) { badgeService.awardBadge(userLoginId, "POINT_1000") }
  }

  @Test
  @DisplayName("포인트 배지 체크 - 1000점 이상 시 모든 단계 배지 지급 시도")
  fun `checkPointBadges should attempt to award all badges if points are 1000`() {
    // Given: 보유 포인트가 1000점인 사용자 설정
    val userLoginId = "testuser"
    val user = User(loginId = userLoginId, password = "pwd", nickname = "nick")
    user.addPoint(1000)
    every { userRepository.findByLoginId(userLoginId) } returns user
    every { badgeService.awardBadge(any(), any()) } returns true

    // When: 배지 획득 조건 체크 실행
            pointBadgeChecker.checkBadges(userLoginId)
    // Then: 100점, 1000점 배지 지급이 모두 시도되었는지 확인
    verify { badgeService.awardBadge(userLoginId, "POINT_100") }
    verify { badgeService.awardBadge(userLoginId, "POINT_1000") }
  }
}
