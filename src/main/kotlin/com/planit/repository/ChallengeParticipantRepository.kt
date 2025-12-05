package com.planit.repository

import com.planit.entity.ChallengeParticipant
import com.planit.enums.ParticipantStatusEnum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChallengeParticipantRepository : JpaRepository<ChallengeParticipant, String> {

    // 챌린지 ID로 참여자 조회
    fun findByChallenge_Id(id: String): List<ChallengeParticipant>

    // 중복 참여 체크
    fun existsByIdAndLoginId(id: String, loginId: String): Boolean

    // 특정 사용자의 챌린지 참여 정보 조회
    fun findByIdAndLoginId(id: String, loginId: String): Optional<ChallengeParticipant>

    // 사용자의 참여 목록
    fun findByLoginId(loginId: String): List<ChallengeParticipant>

    // 사용자의 상태별 참여 목록
    fun findByLoginIdAndStatus(
        loginId: String,
        status: ParticipantStatusEnum
    ): List<ChallengeParticipant>

    // 챌린지의 상태별 참여자 조회
    fun findByIdAndStatus(
        id: String,
        status: ParticipantStatusEnum
    ): List<ChallengeParticipant>

    // 챌린지의 전체 참여자 수
    fun countById(id: String): Long

    // 챌린지의 상태별 참여자 수
    fun countByIdAndStatus(id: String, status: ParticipantStatusEnum): Long

    // 챌린지의 전체 인증 수 합계
    @Query("""
        SELECT COALESCE(SUM(p.certificationCnt), 0) 
        FROM ChallengeParticipant p 
        WHERE p.id = :id
    """)
    fun sumCertificationCountById(@Param("id") id: String): Long
}