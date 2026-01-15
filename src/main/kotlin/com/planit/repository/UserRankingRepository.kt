package com.planit.repository

import com.planit.entity.User
import com.planit.entity.UserRanking
import com.planit.enums.RankingPeriodType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRankingRepository : JpaRepository<UserRanking, Long> {

    /**
     * 특정 기간의 사용자 랭킹을 조회합니다.
     */
    fun findByUserAndPeriodTypeAndPeriodKey(
        user: User,
        periodType: RankingPeriodType,
        periodKey: String
    ): UserRanking?

    /**
     * 특정 기간의 상위 N명 랭킹을 조회합니다.
     */
    @Query("""
        SELECT ur FROM UserRanking ur
        JOIN FETCH ur.user
        WHERE ur.periodType = :periodType AND ur.periodKey = :periodKey
        ORDER BY ur.score DESC
        LIMIT :limit
    """)
    fun findTopRankings(
        @Param("periodType") periodType: RankingPeriodType,
        @Param("periodKey") periodKey: String,
        @Param("limit") limit: Int
    ): List<UserRanking>

    /**
     * 특정 기간의 모든 랭킹을 점수 내림차순으로 조회합니다.
     */
    fun findByPeriodTypeAndPeriodKeyOrderByScoreDesc(
        periodType: RankingPeriodType,
        periodKey: String
    ): List<UserRanking>

    /**
     * 특정 기간의 참여자 수를 조회합니다.
     */
    fun countByPeriodTypeAndPeriodKey(
        periodType: RankingPeriodType,
        periodKey: String
    ): Long

    /**
     * 특정 기간 타입의 모든 기간 키를 조회합니다 (아카이브 조회용).
     */
    @Query("""
        SELECT DISTINCT ur.periodKey FROM UserRanking ur
        WHERE ur.periodType = :periodType
        ORDER BY ur.periodKey DESC
    """)
    fun findDistinctPeriodKeysByPeriodType(
        @Param("periodType") periodType: RankingPeriodType
    ): List<String>

    /**
     * 특정 사용자의 모든 랭킹 기록을 조회합니다.
     */
    fun findByUserOrderByPeriodTypeAscPeriodKeyDesc(user: User): List<UserRanking>
}
