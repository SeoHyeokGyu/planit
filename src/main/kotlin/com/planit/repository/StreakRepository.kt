package com.planit.repository

import com.planit.entity.Streak
import com.planit.entity.StreakId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface StreakRepository : JpaRepository<Streak, StreakId> {

    /**
     * 특정 사용자의 모든 스트릭 조회
     */
    fun findAllByLoginId(loginId: String): List<Streak>

    /**
     * 특정 챌린지의 모든 스트릭 조회
     */
    fun findAllByChallengeId(challengeId: String): List<Streak>

    /**
     * 특정 사용자의 특정 챌린지 스트릭 조회
     */
    fun findByChallengeIdAndLoginId(challengeId: String, loginId: String): Streak?

    /**
     * 오늘 인증하지 않은 활성 스트릭 조회 (배치용)
     */
    @Query("""
        SELECT s FROM Streak s 
        WHERE s.currentStreak > 0 
        AND (s.lastCertificationDate IS NULL OR s.lastCertificationDate < :today)
    """)
    fun findActiveStreaksNotCertifiedToday(@Param("today") today: LocalDate): List<Streak>

    /**
     * 특정 사용자의 총 스트릭 통계
     */
    @Query("""
    SELECT new map(
        COALESCE(SUM(CASE WHEN s.currentStreak > 0 THEN s.currentStreak ELSE 0 END), 0) as totalCurrentStreak,
        COALESCE(MAX(s.longestStreak), 0) as maxLongestStreak,
        COUNT(CASE WHEN s.currentStreak > 0 THEN 1 END) as activeStreakCount
    )
    FROM Streak s 
    WHERE s.loginId = :loginId
""")
    fun getStreakStatistics(@Param("loginId") loginId: String): Map<String, Any>

    /**
     * 스트릭 리더보드 (최장 스트릭 기준)
     */
    @Query("""
        SELECT s FROM Streak s 
        WHERE s.challengeId = :challengeId 
        AND s.currentStreak > 0
        ORDER BY s.currentStreak DESC, s.longestStreak DESC
    """)
    fun findTopStreaksByChallengeId(
        @Param("challengeId") challengeId: String
    ): List<Streak>
}
