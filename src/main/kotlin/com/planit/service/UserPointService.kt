package com.planit.service

import com.planit.dto.DailyPointStatistics
import com.planit.dto.PointStatisticsResponse
import com.planit.dto.PointStatisticsSummary
import com.planit.dto.UserPointResponse
import com.planit.dto.UserPointSummaryResponse
import com.planit.entity.UserPoint
import com.planit.repository.UserRepository
import com.planit.repository.UserPointRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

  @Transactional(readOnly = true)
  fun getPointStatistics(
      userLoginId: String,
      startDate: LocalDate,
      endDate: LocalDate,
  ): PointStatisticsResponse {
    val startDateTime = startDate.atStartOfDay()
    val endDateTime = endDate.plusDays(1).atStartOfDay() // Exclusive end

    // Get daily statistics from repository
    val dailyData = userPointRepository.findDailyPointStatistics(
        userLoginId,
        startDateTime,
        endDateTime
    )

    // Get cumulative points before period start
    val initialCumulativePoints = userPointRepository.sumPointsBeforeDate(
        userLoginId,
        startDateTime
    )

    // Build daily statistics with cumulative sums
    var runningTotal = initialCumulativePoints
    val statistics = dailyData.map { projection ->
      val date = projection.getDate().toLocalDate()
      val pointsEarned = projection.getTotalPoints()
      runningTotal += pointsEarned

      DailyPointStatistics.from(
          date = date,
          pointsEarned = pointsEarned,
          cumulativePoints = runningTotal,
          transactionCount = projection.getTransactionCount(),
      )
    }

    // Fill in missing dates with zero values
    val filledStatistics = fillMissingDates(statistics, startDate, endDate, initialCumulativePoints)

    // Calculate summary
    val totalPointsEarned = filledStatistics.sumOf { it.pointsEarned }
    val dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1
    val averagePointsPerDay = if (dayCount > 0) {
      totalPointsEarned.toDouble() / dayCount
    } else {
      0.0
    }
    val totalTransactions = filledStatistics.sumOf { it.transactionCount }

    val summary = PointStatisticsSummary.from(
        totalPointsEarned = totalPointsEarned,
        averagePointsPerDay = averagePointsPerDay,
        totalTransactions = totalTransactions,
        periodStart = startDate,
        periodEnd = endDate,
    )

    return PointStatisticsResponse.from(filledStatistics, summary)
  }

  /**
   * Fill in missing dates in statistics with zero values
   */
  private fun fillMissingDates(
      statistics: List<DailyPointStatistics>,
      startDate: LocalDate,
      endDate: LocalDate,
      initialCumulative: Long,
  ): List<DailyPointStatistics> {
    val statisticsMap = statistics.associateBy { it.date }
    val result = mutableListOf<DailyPointStatistics>()

    var currentDate = startDate
    var lastCumulative = initialCumulative

    while (!currentDate.isAfter(endDate)) {
      val stat = statisticsMap[currentDate]
      if (stat != null) {
        result.add(stat)
        lastCumulative = stat.cumulativePoints
      } else {
        // No transactions on this day
        result.add(
            DailyPointStatistics.from(
                date = currentDate,
                pointsEarned = 0L,
                cumulativePoints = lastCumulative,
                transactionCount = 0,
            )
        )
      }
      currentDate = currentDate.plusDays(1)
    }

    return result
  }
}
