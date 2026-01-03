package com.planit.service

import com.planit.entity.Badge
import com.planit.entity.User
import com.planit.entity.UserBadge
import com.planit.enums.BadgeGrade
import com.planit.enums.BadgeType
import com.planit.exception.UserNotFoundException
import com.planit.repository.BadgeRepository
import com.planit.repository.UserBadgeRepository
import com.planit.repository.UserRepository
import com.planit.util.setPrivateProperty
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

/** 배지 서비스의 핵심 비즈니스 로직을 검증하는 테스트 클래스입니다. */
@ExtendWith(MockKExtension::class)
class BadgeServiceTest {
  @MockK private lateinit var badgeRepository: BadgeRepository
  @MockK private lateinit var userBadgeRepository: UserBadgeRepository
  @MockK private lateinit var userRepository: UserRepository
  @MockK private lateinit var notificationService: NotificationService
  @InjectMockKs private lateinit var badgeService: BadgeService

  private lateinit var user: User
  private lateinit var badge: Badge

  @BeforeEach
  fun setUp() {
    // 테스트에 사용할 사용자 엔티티 생성
    user = User(loginId = "testuser", password = "password", nickname = "tester")
    // ID 필드가 private val이므로 리플렉션을 사용하여 강제로 설정
    user.setPrivateProperty("id", 1L)
    // 테스트에 사용할 배지 엔티티 생성
    badge =
      Badge(
        code = "TEST_BADGE",
        name = "Test Badge",
        description = "This is a test badge",
        iconCode = "TEST_ICON",
        type = BadgeType.CERTIFICATION_COUNT,
        grade = BadgeGrade.BRONZE,
        requiredValue = 10,
      )
  }

  @Test
  @DisplayName("배지 지급 성공 - 새로운 배지 획득")
  fun `awardBadge should return true when user acquires a new badge`() {
    // Given: 사용자와 배지가 존재하고, 아직 획득하지 않은 상태 설정
    every { userRepository.findByLoginId(user.loginId) } returns user
    every { badgeRepository.findByCode(badge.code) } returns badge
    every { userBadgeRepository.existsByUserIdAndBadgeCode(user.id!!, badge.code) } returns false
    every { userBadgeRepository.save(any()) } returns UserBadge(user = user, badge = badge)
    every { notificationService.createNotification(any()) } returns Unit

    // When: 배지 지급 시도
    val result = badgeService.awardBadge(user.loginId, badge.code)

    // Then: 지급 결과가 true이고 저장 로직이 호출되었는지 검증
    assertTrue(result)
    verify { userBadgeRepository.save(any()) }
    verify { notificationService.createNotification(any()) }
  }

  @Test
  @DisplayName("배지 지급 실패 - 사용자를 찾을 수 없음")
  fun `awardBadge should throw IllegalArgumentException when user not found`() {
    // Given: 존재하지 않는 사용자 ID 설정
    every { userRepository.findByLoginId(user.loginId) } returns null

    // When & Then: 예외 발생 여부 검증
    val exception =
      assertThrows<UserNotFoundException> { badgeService.awardBadge(user.loginId, badge.code) }
    assertEquals("사용자를 찾을 수 없습니다", exception.message)
  }

  @Test
  @DisplayName("배지 지급 실패 - 배지를 찾을 수 없음")
  fun `awardBadge should return false when badge not found`() {
    // Given: 사용자는 존재하지만 배지 코드가 잘못된 경우 설정
    every { userRepository.findByLoginId(user.loginId) } returns user
    every { badgeRepository.findByCode(badge.code) } returns null

    // When: 배지 지급 시도
    val result = badgeService.awardBadge(user.loginId, badge.code)

    // Then: 지급 결과가 false이고 저장 로직이 호출되지 않았는지 검증
    assertFalse(result)
    verify(exactly = 0) { userBadgeRepository.save(any()) }
  }

  @Test
  @DisplayName("배지 지급 실패 - 이미 획득한 배지")
  fun `awardBadge should return false when user already has the badge`() {
    // Given: 이미 배지를 보유하고 있는 상태 설정
    every { userRepository.findByLoginId(user.loginId) } returns user
    every { badgeRepository.findByCode(badge.code) } returns badge
    every { userBadgeRepository.existsByUserIdAndBadgeCode(user.id!!, badge.code) } returns true

    // When: 배지 지급 시도
    val result = badgeService.awardBadge(user.loginId, badge.code)

    // Then: 중복 지급되지 않고 false를 반환하는지 검증
    assertFalse(result)
    verify(exactly = 0) { userBadgeRepository.save(any()) }
  }

  @Test
  @DisplayName("전체 배지 목록 조회 - 로그인하지 않음")
  fun `getAllBadges should return all badges with isAcquired false when user not logged in`() {
    // Given: 전체 배지 목록만 존재
    every { badgeRepository.findAll() } returns listOf(badge)

    // When: 로그인하지 않은 상태(null)로 목록 조회
    val result = badgeService.getAllBadges(null)

    // Then: 모든 배지의 획득 상태가 false인지 확인
    assertEquals(1, result.size)
    assertFalse(result[0].isAcquired)
    assertNull(result[0].acquiredAt)
  }

  @Test
  @DisplayName("전체 배지 목록 조회 - 로그인함")
  fun `getAllBadges should return all badges with correct isAcquired status when user logged in`() {
    // Given: 특정 배지를 획득한 사용자 상태 설정
    val userBadge = UserBadge(user = user, badge = badge, acquiredAt = LocalDateTime.now())
    every { badgeRepository.findAll() } returns listOf(badge)
    every { userBadgeRepository.findByUserLoginId(user.loginId) } returns listOf(userBadge)

    // When: 로그인한 사용자로 목록 조회
    val result = badgeService.getAllBadges(user.loginId)

    // Then: 획득한 배지의 상태가 true이고 획득 날짜가 포함되었는지 확인
    assertEquals(1, result.size)
    assertTrue(result[0].isAcquired)
    assertEquals(userBadge.acquiredAt, result[0].acquiredAt)
  }

  @Test
  @DisplayName("내 배지 목록 조회")
  fun `getMyBadges should return only acquired badges`() {
    // Given: 사용자가 획득한 배지 정보 설정
    val userBadge = UserBadge(user = user, badge = badge, acquiredAt = LocalDateTime.now())
    every { userBadgeRepository.findByUserLoginId(user.loginId) } returns listOf(userBadge)

    // When: 내 배지 목록 조회
    val result = badgeService.getMyBadges(user.loginId)

    // Then: 내가 획득한 배지만 반환되는지 확인
    assertEquals(1, result.size)
    assertTrue(result[0].isAcquired)
    assertEquals(badge.code, result[0].code)
  }
}
