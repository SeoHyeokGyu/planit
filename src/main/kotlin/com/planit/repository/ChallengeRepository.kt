package com.planit.repository

import com.planit.entity.Challenge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ChallengeRepository : JpaRepository<Challenge, String> {

    // 작성자로 조회
    fun findByCreatedId(createdId: String): List<Challenge>

    // 카테고리로 조회
    fun findByCategory(category: String): List<Challenge>

    // 난이도로 조회
    fun findByDifficulty(difficulty: String): List<Challenge>

    // 제목 또는 설명에 키워드 포함
    fun findByTitleContainingOrDescriptionContaining(
        title: String,
        description: String
    ): List<Challenge>

    // 카테고리와 난이도로 조회
    fun findByCategoryAndDifficulty(category: String, difficulty: String): List<Challenge>

    // 특정 날짜에 종료되는 챌린지 조회
    @Query("""
        SELECT c FROM Challenge c 
        WHERE DATE(c.endDate) = :targetDate
    """)
    fun findByEndDateOn(@Param("targetDate") targetDate: LocalDate): List<Challenge>

    // 조회수 증가
    @Modifying
    @Query("UPDATE Challenge c SET c.viewCnt = c.viewCnt + 1 WHERE c.id = :id")
    fun incrementViewCount(@Param("id") id: String)

    // 참여자 수 증가
    @Modifying
    @Query("UPDATE Challenge c SET c.participantCnt = c.participantCnt + 1 WHERE c.id = :id")
    fun incrementParticipantCount(@Param("id") id: String)

    // 참여자 수 감소
    @Modifying
    @Query("UPDATE Challenge c SET c.participantCnt = c.participantCnt - 1 WHERE c.id = :id AND c.participantCnt > 0")
    fun decrementParticipantCount(@Param("id") id: String)
}