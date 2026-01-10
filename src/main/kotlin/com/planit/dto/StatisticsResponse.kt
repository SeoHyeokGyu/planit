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
 * 일별 경험치 통계 응답 DTO
 */
data class DailyExperienceStatistics(
    val date: LocalDate,
    val experienceEarned: Long,
    val cumulativeExperience: Long,
    val level: Int,
    val levelUpOccurred: Boolean,
    val transactionCount: Int,
) {
    companion object {
        fun from(
            date: LocalDate,
            experienceEarned: Long,
            cumulativeExperience: Long,
            level: Int,
            levelUpOccurred: Boolean,
            transactionCount: Int,
        ): DailyExperienceStatistics {
            return DailyExperienceStatistics(
                date = date,
                experienceEarned = experienceEarned,
                cumulativeExperience = cumulativeExperience,
                level = level,
                levelUpOccurred = levelUpOccurred,
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
 * 경험치 통계 범위 응답 (일별/주별/월별)
 */
data class ExperienceStatisticsResponse(
    val statistics: List<DailyExperienceStatistics>,
    val summary: ExperienceStatisticsSummary,
) {
    companion object {
        fun from(
            statistics: List<DailyExperienceStatistics>,
            summary: ExperienceStatisticsSummary,
        ): ExperienceStatisticsResponse {
            return ExperienceStatisticsResponse(
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

/**
 * 경험치 통계 요약 정보
 */
data class ExperienceStatisticsSummary(
    val totalExperienceEarned: Long,
    val averageExperiencePerDay: Double,
    val totalTransactions: Int,
    val levelUpsCount: Int,
    val startLevel: Int,
    val endLevel: Int,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
) {
    companion object {
        fun from(
            totalExperienceEarned: Long,
            averageExperiencePerDay: Double,
            totalTransactions: Int,
            levelUpsCount: Int,
            startLevel: Int,
            endLevel: Int,
            periodStart: LocalDate,
            periodEnd: LocalDate,
        ): ExperienceStatisticsSummary {
            return ExperienceStatisticsSummary(
                totalExperienceEarned = totalExperienceEarned,
                averageExperiencePerDay = averageExperiencePerDay,
                totalTransactions = totalTransactions,
                levelUpsCount = levelUpsCount,
                startLevel = startLevel,
                endLevel = endLevel,
                periodStart = periodStart,
                periodEnd = periodEnd,
            )
        }
    }
}
