package com.planit.service

import com.planit.dto.UserPointResponse
import com.planit.dto.UserPointSummaryResponse
import com.planit.entity.UserPoint
import com.planit.repository.UserRepository
import com.planit.repository.UserPointRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserPointService(
    private val userPointRepository: UserPointRepository,
    private val userRepository: UserRepository,
) {

  fun addPoint(userLoginId: String, points: Long, reason: String) {
    val user = userRepository.findByLoginId(userLoginId)
        ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userLoginId")

    // User 엔티티에 포인트 추가
    user.addPoint(points)

    // 포인트 히스토리 저장
    val userPoint = UserPoint(user, points, reason)
    userPointRepository.save(userPoint)
  }

  fun subtractPoint(userLoginId: String, points: Long, reason: String) {
    val user = userRepository.findByLoginId(userLoginId)
        ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userLoginId")

    // User 엔티티에서 포인트 차감 (0 이하로 떨어지지 않음)
    user.subtractPoint(points)

    // 포인트 히스토리 저장 (음수로 저장)
    val userPoint = UserPoint(user, -points, reason)
    userPointRepository.save(userPoint)
  }

  @Transactional(readOnly = true)
  fun getUserPointHistory(
      userLoginId: String,
      pageable: Pageable,
  ): Page<UserPointResponse> {
    return userPointRepository.findByUser_LoginId(userLoginId, pageable)
        .map { userPoint ->
          UserPointResponse(
              id = userPoint.id!!,
              points = userPoint.points,
              reason = userPoint.reason,
              createdAt = userPoint.createdAt,
          )
        }
  }

  @Transactional(readOnly = true)
  fun getUserPointSummary(userLoginId: String): UserPointSummaryResponse {
    val user = userRepository.findByLoginId(userLoginId)
        ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userLoginId")

    val pointCount = userPointRepository.countByUser_LoginId(userLoginId)

    return UserPointSummaryResponse(
        totalPoint = user.totalPoint,
        pointCount = pointCount,
    )
  }
}
