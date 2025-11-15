package com.planit.repository

import com.planit.entity.Challenge
import com.planit.entity.ChallengeCategory
import com.planit.entity.ChallengeDifficulty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 챌린지 Repository
 * 
 * JpaRepository가 기본 CRUD 제공:
 * - save(), findById(), findAll(), delete() 등
 * 
 * JpaSpecificationExecutor로 동적 쿼리 지원
 */
@Repository
interface ChallengeRepository : JpaRepository<Challenge, Long>, JpaSpecificationExecutor<Challenge> {

    // ===== JPA 메서드 이름 규칙으로 자동 쿼리 생성 =====
    
    // 작성자로 조회 (자동 생성)
    fun findByCreatedBy(createdBy: Long, pageable: Pageable): Page<Challenge>

    // 카테고리로 조회 (자동 생성)
    fun findByCategory(category: ChallengeCategory, pageable: Pageable): Page<Challenge>

    // 난이도로 조회 (자동 생성)
    fun findByDifficulty(difficulty: ChallengeDifficulty, pageable: Pageable): Page<Challenge>
    
    // 제목 또는 설명에 키워드 포함 (자동 생성)
    fun findByTitleContainingOrDescriptionContaining(
        title: String,
        description: String,
        pageable: Pageable
    ): Page<Challenge>

    // ===== 복잡한 조건만 @Query 사용 =====

    // ===== 벌크 업데이트 (성능 최적화) =====
    
    // 조회수 증가 - 1개 필드만 업데이트할 때 효율적
    @Modifying
    @Query("UPDATE Challenge c SET c.viewCount = c.viewCount + 1 WHERE c.id = :id")
    fun incrementViewCount(@Param("id") id: Long)

    // 참여자 수 증가
    @Modifying
    @Query("UPDATE Challenge c SET c.participantCount = c.participantCount + 1 WHERE c.id = :id")
    fun incrementParticipantCount(@Param("id") id: Long)

    // 참여자 수 감소
    @Modifying
    @Query("UPDATE Challenge c SET c.participantCount = c.participantCount - 1 WHERE c.id = :id AND c.participantCount > 0")
    fun decrementParticipantCount(@Param("id") id: Long)
}
