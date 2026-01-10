package com.planit.repository

import com.planit.entity.UserExperience
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface UserExperienceRepository : JpaRepository<UserExperience, Long> {
  fun findByUser_LoginId(loginId: String, pageable: Pageable): Page<UserExperience>

  fun countByUser_LoginId(loginId: String): Long

  fun deleteByUser_Id(userId: Long): Int

  /**
   * 특정 사용자의 기간별 일별 경험치 합계를 조회합니다.
   * @param loginId 사용자 로그인 ID
   * @param startDate 시작 날짜 (포함)
   * @param endDate 종료 날짜 (포함)
   * @return 일별 경험치 데이터 (날짜, 경험치 합계, 거래 수)
   */
  @Query("""
      SELECT CAST(ue.createdAt AS date) as date,
             SUM(ue.experience) as totalExperience,
             COUNT(ue.id) as transactionCount
      FROM UserExperience ue
      WHERE ue.user.loginId = :loginId
      AND ue.createdAt >= :startDate
      AND ue.createdAt < :endDate
      GROUP BY CAST(ue.createdAt AS date)
      ORDER BY date ASC
  """)
  fun findDailyExperienceStatistics(
      @Param("loginId") loginId: String,
      @Param("startDate") startDate: LocalDateTime,
      @Param("endDate") endDate: LocalDateTime,
  ): List<DailyExperienceProjection>

  /**
   * 특정 날짜 이전의 누적 경험치를 조회합니다.
   * @param loginId 사용자 로그인 ID
   * @param beforeDate 기준 날짜 (해당 날짜는 포함하지 않음)
   * @return 누적 경험치
   */
  @Query("""
      SELECT COALESCE(SUM(ue.experience), 0)
      FROM UserExperience ue
      WHERE ue.user.loginId = :loginId
      AND ue.createdAt < :beforeDate
  """)
  fun sumExperienceBeforeDate(
      @Param("loginId") loginId: String,
      @Param("beforeDate") beforeDate: LocalDateTime,
  ): Long
}

/**
 * 일별 경험치 통계 Projection
 */
interface DailyExperienceProjection {
  fun getDate(): java.sql.Date
  fun getTotalExperience(): Long
  fun getTransactionCount(): Int
}
