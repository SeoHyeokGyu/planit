package com.planit.config

import com.planit.entity.Badge
import com.planit.enums.BadgeGrade
import com.planit.enums.BadgeType
import com.planit.repository.BadgeRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.Transactional

@Configuration
class BadgeInitializer(private val badgeRepository: BadgeRepository) {

  @Bean @Transactional fun initBadges() = CommandLineRunner { initializeBadgeData() }

  @Transactional
  fun initializeBadgeData() {
    val badges =
      listOf(
        // 인증 관련
        createBadge(
          "CERT_1",
          "첫 발걸음",
          "설레는 첫 챌린지 인증을 완료했습니다.",
          "FOOTPRINT",
          BadgeType.CERTIFICATION_COUNT,
          BadgeGrade.BRONZE,
          1,
        ),
        createBadge(
          "CERT_10",
          "열정의 시작",
          "인증 10회를 달성했습니다.",
          "RUNNING_SHOE",
          BadgeType.CERTIFICATION_COUNT,
          BadgeGrade.SILVER,
          10,
        ),
        createBadge(
          "CERT_50",
          "꾸준함의 증명",
          "인증 50회를 달성했습니다.",
          "MEDAL",
          BadgeType.CERTIFICATION_COUNT,
          BadgeGrade.GOLD,
          50,
        ),
        createBadge(
          "CERT_100",
          "전설의 인증러",
          "인증 100회를 달성했습니다.",
          "TROPHY",
          BadgeType.CERTIFICATION_COUNT,
          BadgeGrade.PLATINUM,
          100,
        ),

        // 연속 인증
        createBadge(
          "STREAK_3",
          "작심삼일 탈출",
          "3일 연속으로 인증했습니다.",
          "FLAME",
          BadgeType.CERTIFICATION_STREAK,
          BadgeGrade.BRONZE,
          3,
        ),
        createBadge(
          "STREAK_7",
          "불타는 일주일",
          "7일 연속 인증에 성공했습니다.",
          "FIRE",
          BadgeType.CERTIFICATION_STREAK,
          BadgeGrade.SILVER,
          7,
        ),
        createBadge(
          "STREAK_30",
          "습관의 완성",
          "30일 연속 인증! 전설의 시작입니다.",
          "PHOENIX",
          BadgeType.CERTIFICATION_STREAK,
          BadgeGrade.PLATINUM,
          30,
        ),

        // 팔로워
        createBadge(
          "SOCIAL_1",
          "새로운 만남",
          "첫 팔로워가 생겼습니다.",
          "USER",
          BadgeType.FOLLOWER_COUNT,
          BadgeGrade.BRONZE,
          1,
        ),
        createBadge(
          "SOCIAL_10",
          "주목받는 루키",
          "팔로워 10명을 달성했습니다.",
          "GROUP",
          BadgeType.FOLLOWER_COUNT,
          BadgeGrade.SILVER,
          10,
        ),
        createBadge(
          "SOCIAL_100",
          "인플루언서",
          "팔로워 100명을 달성했습니다.",
          "CROWN",
          BadgeType.FOLLOWER_COUNT,
          BadgeGrade.GOLD,
          100,
        ),

        // 포인트
        createBadge(
          "POINT_100",
          "티끌 모아 태산",
          "누적 포인트 100점을 달성했습니다.",
          "COIN",
          BadgeType.POINT_ACCUMULATION,
          BadgeGrade.BRONZE,
          100,
        ),
        createBadge(
          "POINT_1000",
          "부자의 꿈",
          "누적 포인트 1000점을 달성했습니다.",
          "MONEY_BAG",
          BadgeType.POINT_ACCUMULATION,
          BadgeGrade.GOLD,
          1000,
        ),
      )

    badges.forEach { badge ->
      if (!badgeRepository.existsByCode(badge.code)) {
        badgeRepository.save(badge)
      }
    }
  }

  private fun createBadge(
    code: String,
    name: String,
    description: String,
    iconCode: String,
    type: BadgeType,
    grade: BadgeGrade,
    requiredValue: Long,
  ): Badge {
    return Badge(
      code = code,
      name = name,
      description = description,
      iconCode = iconCode,
      type = type,
      grade = grade,
      requiredValue = requiredValue,
    )
  }
}
