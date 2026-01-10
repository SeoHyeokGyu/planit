package com.planit.repository

import com.planit.entity.UserPoint
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface UserPointRepository : JpaRepository<UserPoint, Long> {
  fun findByUser_LoginId(loginId: String, pageable: Pageable): Page<UserPoint>

  fun countByUser_LoginId(loginId: String): Long

  fun deleteByUser_Id(userId: Long): Int

  /**
   * 특정 사용자의 기간별 일별 포인트 합계를 조회합니다.
   * @param loginId 사용자 로그인 ID
   * @param startDate 시작 날짜 (포함)
   * @param endDate 종료 날짜 (포함)
   * @return 일별 포인트 데이터 (날짜, 포인트 합계, 거래 수)
   */
  @Query("""
      SELECT CAST(up.createdAt AS date) as date,
             SUM(up.points) as totalPoints,
             COUNT(up.id) as transactionCount
      FROM UserPoint up
      WHERE up.user.loginId = :loginId
      AND up.createdAt >= :startDate
      AND up.createdAt < :endDate
      GROUP BY CAST(up.createdAt AS date)
      ORDER BY date ASC
  """)
  fun findDailyPointStatistics(
      @Param("loginId") loginId: String,
      @Param("startDate") startDate: LocalDateTime,
      @Param("endDate") endDate: LocalDateTime,
  ): List<DailyPointProjection>

  /**
   * 특정 날짜 이전의 누적 포인트를 조회합니다.
   * @param loginId 사용자 로그인 ID
   * @param beforeDate 기준 날짜 (해당 날짜는 포함하지 않음)
   * @return 누적 포인트
   */
  @Query("""
      SELECT COALESCE(SUM(up.points), 0)
      FROM UserPoint up
      WHERE up.user.loginId = :loginId
      AND up.createdAt < :beforeDate
  """)
  fun sumPointsBeforeDate(
      @Param("loginId") loginId: String,
      @Param("beforeDate") beforeDate: LocalDateTime,
  ): Long
}

/**
 * 일별 포인트 통계 Projection
 */
interface DailyPointProjection {
  fun getDate(): java.sql.Date
  fun getTotalPoints(): Long
  fun getTransactionCount(): Int
}
