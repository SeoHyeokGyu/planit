package com.planit.repository

import com.planit.entity.DailyActivity
import com.planit.entity.DailyActivityId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DailyActivityRepository : JpaRepository<DailyActivity, DailyActivityId> {

    /**
     * 특정 사용자의 특정 날짜 활동 조회
     */
    fun findByLoginIdAndActivityDate(loginId: String, activityDate: LocalDate): DailyActivity?

    /**
     * 특정 기간의 일별 활동 조회 (잔디 데이터)
     */
    @Query("""
        SELECT d FROM DailyActivity d 
        WHERE d.loginId = :loginId 
        AND d.activityDate BETWEEN :startDate AND :endDate
        ORDER BY d.activityDate ASC
    """)
    fun findActivitiesByDateRange(
        @Param("loginId") loginId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<DailyActivity>

    /**
     * 최근 N일 활동 조회
     */
    @Query("""
        SELECT d FROM DailyActivity d 
        WHERE d.loginId = :loginId 
        AND d.activityDate >= :fromDate
        ORDER BY d.activityDate DESC
    """)
    fun findRecentActivities(
        @Param("loginId") loginId: String,
        @Param("fromDate") fromDate: LocalDate
    ): List<DailyActivity>

    /**
     * 일별 통계 조회
     */
    @Query("""
        SELECT new map(
            d.activityDate as date,
            SUM(d.certificationCount) as totalCertifications,
            COUNT(DISTINCT d.challengeCount) as activeChallenges
        )
        FROM DailyActivity d 
        WHERE d.loginId = :loginId 
        AND d.activityDate BETWEEN :startDate AND :endDate
        GROUP BY d.activityDate
        ORDER BY d.activityDate ASC
    """)
    fun getDailyStatistics(
        @Param("loginId") loginId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Map<String, Any>>

    /**
     * 월별 통계 조회
     */
    @Query("""
        SELECT new map(
            FUNCTION('YEAR', d.activityDate) as year,
            FUNCTION('MONTH', d.activityDate) as month,
            SUM(d.certificationCount) as totalCertifications,
            COUNT(DISTINCT d.activityDate) as activeDays
        )
        FROM DailyActivity d 
        WHERE d.loginId = :loginId 
        AND d.activityDate BETWEEN :startDate AND :endDate
        GROUP BY FUNCTION('YEAR', d.activityDate), FUNCTION('MONTH', d.activityDate)
        ORDER BY year DESC, month DESC
    """)
    fun getMonthlyStatistics(
        @Param("loginId") loginId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Map<String, Any>>
}
