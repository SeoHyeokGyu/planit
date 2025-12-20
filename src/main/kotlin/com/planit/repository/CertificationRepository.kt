package com.planit.repository

import com.planit.entity.Certification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

/**
 * 챌린지 인증(Certification) 엔티티를 관리하는 JpaRepository 인터페이스입니다.
 */
interface CertificationRepository : JpaRepository<Certification, Long> {
    /**
     * 특정 사용자의 로그인 ID를 통해 인증 목록을 페이징하여 조회합니다.
     * @param userLoginId 사용자 로그인 ID
     * @param pageable 페이징 정보
     * @return 페이징된 인증 목록
     */
    fun findByUser_LoginId(userLoginId: String, pageable: Pageable): Page<Certification>

    /**
     * 특정 챌린지 ID를 통해 인증 목록을 페이징하여 조회합니다.
     * @param challengeId 챌린지 ID
     * @param pageable 페이징 정보
     * @return 페이징된 인증 목록
     */
    fun findByChallenge_Id(challengeId: String, pageable: Pageable): Page<Certification>

    /**
     * 주어진 사용자 ID 목록에 포함된 사용자들이 작성한 인증 목록을 생성일 내림차순으로 페이징하여 조회합니다.
     * @param userIds 사용자 ID 목록
     * @param pageable 페이징 정보
     * @return 페이징된 인증 목록
     */
    fun findByUser_IdInOrderByCreatedAtDesc(userIds: List<Long>, pageable: Pageable): Page<Certification>

    /**
     * 특정 사용자가 특정 기간 내에 작성한 인증 목록을 조회합니다.
     * @param userLoginId 사용자 로그인 ID
     * @param start 시작 일시
     * @param end 종료 일시
     * @return 인증 목록
     */
    fun findByUser_LoginIdAndCreatedAtBetween(userLoginId: String, start: LocalDateTime, end: LocalDateTime): List<Certification>

    /**
     * 특정 사용자가 작성한 전체 인증 개수를 조회합니다.
     * @param userLoginId 사용자 로그인 ID
     * @return 인증 개수
     */
    fun countByUser_LoginId(userLoginId: String): Long
}
