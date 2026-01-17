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

    // ===== 정렬 쿼리 메서드 추가 =====

    // 전체 조회 - 최신순
    @Query("SELECT c FROM Challenge c ORDER BY c.createdAt DESC")
    fun findAllOrderByCreatedAtDesc(): List<Challenge>

    // 전체 조회 - 이름순
    @Query("SELECT c FROM Challenge c ORDER BY c.title ASC")
    fun findAllOrderByTitleAsc(): List<Challenge>

    // 전체 조회 - 난이도순
    @Query("""
        SELECT c FROM Challenge c 
        ORDER BY 
            CASE c.difficulty 
                WHEN 'EASY' THEN 1 
                WHEN 'MEDIUM' THEN 2 
                WHEN 'HARD' THEN 3 
                ELSE 99 
            END ASC,
            c.title ASC
    """)
    fun findAllOrderByDifficulty(): List<Challenge>

    // 전체 조회 - 인기순
    @Query("SELECT c FROM Challenge c ORDER BY c.participantCnt DESC")
    fun findAllOrderByParticipantCntDesc(): List<Challenge>

    // 카테고리별 - 최신순
    @Query("SELECT c FROM Challenge c WHERE c.category = :category ORDER BY c.createdAt DESC")
    fun findByCategoryOrderByCreatedAtDesc(@Param("category") category: String): List<Challenge>

    // 카테고리별 - 이름순
    @Query("SELECT c FROM Challenge c WHERE c.category = :category ORDER BY c.title ASC")
    fun findByCategoryOrderByTitleAsc(@Param("category") category: String): List<Challenge>

    // 카테고리별 - 난이도순
    @Query("""
        SELECT c FROM Challenge c 
        WHERE c.category = :category
        ORDER BY 
            CASE c.difficulty 
                WHEN 'EASY' THEN 1 
                WHEN 'MEDIUM' THEN 2 
                WHEN 'HARD' THEN 3 
                ELSE 99 
            END ASC,
            c.title ASC
    """)
    fun findByCategoryOrderByDifficulty(@Param("category") category: String): List<Challenge>

    // 카테고리별 - 인기순
    @Query("SELECT c FROM Challenge c WHERE c.category = :category ORDER BY c.participantCnt DESC")
    fun findByCategoryOrderByParticipantCntDesc(@Param("category") category: String): List<Challenge>

    // 난이도별 - 최신순
    @Query("SELECT c FROM Challenge c WHERE c.difficulty = :difficulty ORDER BY c.createdAt DESC")
    fun findByDifficultyOrderByCreatedAtDesc(@Param("difficulty") difficulty: String): List<Challenge>

    // 난이도별 - 이름순
    @Query("SELECT c FROM Challenge c WHERE c.difficulty = :difficulty ORDER BY c.title ASC")
    fun findByDifficultyOrderByTitleAsc(@Param("difficulty") difficulty: String): List<Challenge>

    // 난이도별 - 인기순
    @Query("SELECT c FROM Challenge c WHERE c.difficulty = :difficulty ORDER BY c.participantCnt DESC")
    fun findByDifficultyOrderByParticipantCntDesc(@Param("difficulty") difficulty: String): List<Challenge>

    // 카테고리+난이도 - 최신순
    @Query("""
        SELECT c FROM Challenge c 
        WHERE c.category = :category AND c.difficulty = :difficulty 
        ORDER BY c.createdAt DESC
    """)
    fun findByCategoryAndDifficultyOrderByCreatedAtDesc(
        @Param("category") category: String,
        @Param("difficulty") difficulty: String
    ): List<Challenge>

    // 카테고리+난이도 - 이름순
    @Query("""
        SELECT c FROM Challenge c 
        WHERE c.category = :category AND c.difficulty = :difficulty 
        ORDER BY c.title ASC
    """)
    fun findByCategoryAndDifficultyOrderByTitleAsc(
        @Param("category") category: String,
        @Param("difficulty") difficulty: String
    ): List<Challenge>

    // 카테고리+난이도 - 인기순
    @Query("""
        SELECT c FROM Challenge c 
        WHERE c.category = :category AND c.difficulty = :difficulty 
        ORDER BY c.participantCnt DESC
    """)
    fun findByCategoryAndDifficultyOrderByParticipantCntDesc(
        @Param("category") category: String,
        @Param("difficulty") difficulty: String
    ): List<Challenge>

    // 키워드 검색 - 최신순
    @Query("""
        SELECT c FROM Challenge c 
        WHERE c.title LIKE %:keyword% OR c.description LIKE %:keyword%
        ORDER BY c.createdAt DESC
    """)
    fun findByKeywordOrderByCreatedAtDesc(@Param("keyword") keyword: String): List<Challenge>

    // 키워드 검색 - 이름순
    @Query("""
        SELECT c FROM Challenge c 
        WHERE c.title LIKE %:keyword% OR c.description LIKE %:keyword%
        ORDER BY c.title ASC
    """)
    fun findByKeywordOrderByTitleAsc(@Param("keyword") keyword: String): List<Challenge>

    // 키워드 검색 - 난이도순
    @Query("""
        SELECT c FROM Challenge c 
        WHERE c.title LIKE %:keyword% OR c.description LIKE %:keyword%
        ORDER BY 
            CASE c.difficulty 
                WHEN 'EASY' THEN 1 
                WHEN 'MEDIUM' THEN 2 
                WHEN 'HARD' THEN 3 
                ELSE 99 
            END ASC,
            c.title ASC
    """)
    fun findByKeywordOrderByDifficulty(@Param("keyword") keyword: String): List<Challenge>

    // 키워드 검색 - 인기순
    @Query("""
        SELECT c FROM Challenge c 
        WHERE c.title LIKE %:keyword% OR c.description LIKE %:keyword%
        ORDER BY c.participantCnt DESC
    """)
    fun findByKeywordOrderByParticipantCntDesc(@Param("keyword") keyword: String): List<Challenge>
}