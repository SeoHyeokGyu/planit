package com.planit.service

import com.planit.dto.UserExperienceResponse
import com.planit.dto.UserLevelResponse
import com.planit.dto.UserProgressResponse
import com.planit.entity.UserExperience
import com.planit.repository.UserRepository
import com.planit.repository.UserExperienceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserExperienceService(
    private val userExperienceRepository: UserExperienceRepository,
    private val userRepository: UserRepository,
) {
  companion object {
    private const val EXP_PER_LEVEL = 100L
  }

  fun addExperience(userLoginId: String, experience: Long, reason: String) {
    val user = userRepository.findByLoginId(userLoginId)
        ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userLoginId")

    val previousLevel = user.level

    // User 엔티티에 경험치 추가 (레벨 자동 계산)
    user.addExperience(experience)

    // 경험치 히스토리 저장
    val userExperience = UserExperience(user, experience, reason)
    userExperienceRepository.save(userExperience)

    // 레벨업 감지 (필요시 알림 등의 로직 추가)
    if (user.level > previousLevel) {
      // 레벨업 알림 로직은 별도로 구현할 수 있음
    }
  }

  @Transactional(readOnly = true)
  fun getUserExperienceHistory(
      userLoginId: String,
      pageable: Pageable,
  ): Page<UserExperienceResponse> {
    return userExperienceRepository.findByUser_LoginId(userLoginId, pageable)
        .map { userExp ->
          UserExperienceResponse(
              id = userExp.id!!,
              experience = userExp.experience,
              reason = userExp.reason,
              createdAt = userExp.createdAt,
          )
        }
  }

  @Transactional(readOnly = true)
  fun getUserLevel(userLoginId: String): UserLevelResponse {
    val user = userRepository.findByLoginId(userLoginId)
        ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userLoginId")

    val currentLevelExperience = (user.level - 1) * EXP_PER_LEVEL
    val nextLevelExperience = user.level * EXP_PER_LEVEL
    val experienceInCurrentLevel = user.totalExperience - currentLevelExperience
    val experienceNeededForNextLevel = nextLevelExperience - currentLevelExperience
    val progress = if (experienceNeededForNextLevel > 0) {
      (experienceInCurrentLevel.toDouble() / experienceNeededForNextLevel) * 100
    } else {
      100.0
    }

    return UserLevelResponse(
        totalExperience = user.totalExperience,
        level = user.level,
        nextLevelExperience = nextLevelExperience,
        currentLevelExperience = currentLevelExperience,
        experienceProgress = progress.coerceIn(0.0, 100.0),
    )
  }

  @Transactional(readOnly = true)
  fun getUserProgress(userLoginId: String): UserProgressResponse {
    val user = userRepository.findByLoginId(userLoginId)
        ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userLoginId")

    val currentLevelExperience = (user.level - 1) * EXP_PER_LEVEL
    val nextLevelExperience = user.level * EXP_PER_LEVEL
    val experienceInCurrentLevel = user.totalExperience - currentLevelExperience
    val experienceNeededForNextLevel = nextLevelExperience - currentLevelExperience
    val progress = if (experienceNeededForNextLevel > 0) {
      (experienceInCurrentLevel.toDouble() / experienceNeededForNextLevel) * 100
    } else {
      100.0
    }

    return UserProgressResponse(
        totalPoint = user.totalPoint,
        totalExperience = user.totalExperience,
        level = user.level,
        experienceProgress = progress.coerceIn(0.0, 100.0),
        nextLevelExperience = nextLevelExperience,
    )
  }
}
