package com.planit.dto

import java.time.LocalDate

/**
 * 일별 포인트 통계 응답 DTO
 */
data class DailyPointStatistics(
    val date: LocalDate,
    val pointsEarned: Long,
    val cumulativePoints: Long,
    val transactionCount: Int,
) {
    companion object {
        fun from(
            date: LocalDate,
            pointsEarned: Long,
            cumulativePoints: Long,
            transactionCount: Int,
        ): DailyPointStatistics {
            return DailyPointStatistics(
                date = date,
                pointsEarned = pointsEarned,
                cumulativePoints = cumulativePoints,
                transactionCount = transactionCount,
            )
        }
    }
}


/**
 * 포인트 통계 범위 응답 (일별/주별/월별)
 */
data class PointStatisticsResponse(
    val statistics: List<DailyPointStatistics>,
    val summary: PointStatisticsSummary,
) {
    companion object {
        fun from(
            statistics: List<DailyPointStatistics>,
            summary: PointStatisticsSummary,
        ): PointStatisticsResponse {
            return PointStatisticsResponse(
                statistics = statistics,
                summary = summary,
            )
        }
    }
}


/**
 * 포인트 통계 요약 정보
 */
data class PointStatisticsSummary(
    val totalPointsEarned: Long,
    val averagePointsPerDay: Double,
    val totalTransactions: Int,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
) {
    companion object {
        fun from(
            totalPointsEarned: Long,
            averagePointsPerDay: Double,
            totalTransactions: Int,
            periodStart: LocalDate,
            periodEnd: LocalDate,
        ): PointStatisticsSummary {
            return PointStatisticsSummary(
                totalPointsEarned = totalPointsEarned,
                averagePointsPerDay = averagePointsPerDay,
                totalTransactions = totalTransactions,
                periodStart = periodStart,
                periodEnd = periodEnd,
            )
        }
    }
}

