package com.planit.repository

import com.planit.entity.ChallengeParticipant
import com.planit.entity.ParticipantStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**
 * 챌린지 참여자 Repository
 * 
 * JpaRepository가 기본 CRUD 제공
 */
@Repository
interface ChallengeParticipantRepository : JpaRepository<ChallengeParticipant, Long> {

    // ===== JPA 메서드 이름 규칙으로 자동 쿼리 생성 =====
    
    // 챌린지 ID로 참여자 조회 (자동 생성)
    fun findByChallengeId(challengeId: Long, pageable: Pageable): Page<ChallengeParticipant>

    // 중복 참여 체크 (자동 생성)
    fun existsByChallengeIdAndUserId(challengeId: Long, userId: Long): Boolean

    // 특정 사용자의 챌린지 참여 정보 조회 (자동 생성)
    fun findByChallengeIdAndUserId(challengeId: Long, userId: Long): Optional<ChallengeParticipant>

    // 사용자의 참여 목록 (자동 생성)
    fun findByUserId(userId: Long, pageable: Pageable): Page<ChallengeParticipant>
    
    // 사용자의 상태별 참여 목록 (자동 생성)
    fun findByUserIdAndStatus(userId: Long, status: ParticipantStatus, pageable: Pageable): Page<ChallengeParticipant>
    
    // 챌린지의 상태별 참여자 조회 (자동 생성)
    fun findByChallengeIdAndStatus(
        challengeId: Long,
        status: ParticipantStatus,
        pageable: Pageable
    ): Page<ChallengeParticipant>

    // 챌린지의 전체 참여자 수 (자동 생성)
    fun countByChallengeId(challengeId: Long): Long
    
    // 챌린지의 상태별 참여자 수 (자동 생성)
    fun countByChallengeIdAndStatus(challengeId: Long, status: ParticipantStatus): Long

    // ===== 복잡한 집계만 @Query 사용 =====
    
    // 챌린지의 전체 인증 수 합계 (SUM 집계)
    @Query("""
        SELECT COALESCE(SUM(p.certificationCount), 0) 
        FROM ChallengeParticipant p 
        WHERE p.challenge.id = :challengeId
    """)
    fun sumCertificationCountByChallengeId(@Param("challengeId") challengeId: Long): Long
}
