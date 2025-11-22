package com.planit.repository

import com.planit.entity.ChallengeParticipant
import com.planit.enum.ParticipantStatusEnum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChallengeParticipantRepository : JpaRepository<ChallengeParticipant, Long> {

    // 챌린지 ID로 참여자 조회
    fun findByChallengeId(challengeId: String): List<ChallengeParticipant>

    // 중복 참여 체크
    fun existsByChallengeIdAndLoginId(challengeId: String, loginId: Long): Boolean

    // 특정 사용자의 챌린지 참여 정보 조회
    fun findByChallengeIdAndLoginId(challengeId: String, loginId: Long): Optional<ChallengeParticipant>

    // 사용자의 참여 목록
    fun findByLoginId(loginId: Long): List<ChallengeParticipant>

    // 사용자의 상태별 참여 목록
    fun findByLoginIdAndStatus(
        loginId: Long,
        status: ParticipantStatusEnum
    ): List<ChallengeParticipant>

    // 챌린지의 상태별 참여자 조회
    fun findByChallengeIdAndStatus(
        challengeId: String,
        status: ParticipantStatusEnum
    ): List<ChallengeParticipant>

    // 챌린지의 전체 참여자 수
    fun countByChallengeId(challengeId: String): Long

    // 챌린지의 상태별 참여자 수
    fun countByChallengeIdAndStatus(challengeId: String, status: ParticipantStatusEnum): Long

    // 챌린지의 전체 인증 수 합계
    @Query("""
        SELECT COALESCE(SUM(p.certificationCnt), 0) 
        FROM ChallengeParticipant p 
        WHERE p.challengeId = :challengeId
    """)
    fun sumCertificationCountByChallengeId(@Param("challengeId") challengeId: String): Long
}