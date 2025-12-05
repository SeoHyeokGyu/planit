package com.planit.service

import com.planit.dto.*
import com.planit.entity.Challenge
import com.planit.entity.ChallengeParticipant
import com.planit.enums.ParticipantStatusEnum
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.ChallengeRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException
import java.util.concurrent.TimeUnit

@Service
@Transactional(readOnly = true)
class ChallengeService(
    private val challengeRepository: ChallengeRepository,
    private val participantRepository: ChallengeParticipantRepository,
    private val redisTemplate: RedisTemplate<String, String>
) {

    companion object {
        private const val VIEW_COUNT_KEY_PREFIX = "challenge:view:"
        private const val VIEW_COUNT_SYNC_THRESHOLD = 10
    }

    /**
     * 챌린지 생성
     */
    @Transactional
    fun createChallenge(request: ChallengeRequest, loginId: String): ChallengeResponse {
        val challenge = Challenge(
            title = request.title,
            description = request.description,
            category = request.category,
            startDate = request.startDate,
            endDate = request.endDate,
            difficulty = request.difficulty,
            createdId = request.loginId,
            viewCnt = 0,
            participantCnt = 0,
            certificationCnt = 0
        )

        val savedChallenge = challengeRepository.save(challenge)
        return ChallengeResponse.from(savedChallenge)
    }

    /**
     * 챌린지 상세 조회
     */
    fun getChallengeById(challengeId: String): ChallengeResponse {
        val challenge = findChallengeById(challengeId)
        return ChallengeResponse.from(challenge)
    }

    /**
     * 챌린지 목록 조회 (필터링)
     */
    fun getChallenges(request: ChallengeSearchRequest): List<ChallengeListResponse> {
        val challenges = when {
            // 키워드 검색 (최우선)
            !request.keyword.isNullOrBlank() -> {
                challengeRepository.findByTitleContainingOrDescriptionContaining(
                    request.keyword,
                    request.keyword
                )
            }
            // 카테고리 + 난이도
            !request.category.isNullOrBlank() && !request.difficulty.isNullOrBlank() -> {
                challengeRepository.findByCategoryAndDifficulty(request.category, request.difficulty)
            }
            // 카테고리만
            !request.category.isNullOrBlank() -> {
                challengeRepository.findByCategory(request.category)
            }
            // 난이도만
            !request.difficulty.isNullOrBlank() -> {
                challengeRepository.findByDifficulty(request.difficulty)
            }
            // 기본: 전체 조회
            else -> {
                challengeRepository.findAll()
            }
        }

        return challenges.map { ChallengeListResponse.from(it) }
    }

    /**
     * 챌린지 검색 (키워드)
     */
    fun searchChallenges(keyword: String): List<ChallengeListResponse> {
        val challenges = challengeRepository.findByTitleContainingOrDescriptionContaining(
            keyword,
            keyword
        )
        return challenges.map { ChallengeListResponse.from(it) }
    }

    /**
     * 챌린지 수정
     */
    @Transactional
    fun updateChallenge(challengeId: String, request: ChallengeRequest, loginId: String): ChallengeResponse {
        val challenge = findChallengeById(challengeId)

        // 권한 검증
        if (challenge.createdId != loginId) {
            throw IllegalArgumentException("챌린지를 수정할 권한이 없습니다")
        }

        // 진행중인 챌린지는 수정 제한
        if (challenge.isActive()) {
            throw IllegalStateException("진행중인 챌린지는 수정할 수 없습니다")
        }

        challenge.apply {
            title = request.title
            description = request.description
            category = request.category
            startDate = request.startDate
            endDate = request.endDate
            difficulty = request.difficulty
        }

        val updatedChallenge = challengeRepository.save(challenge)
        return ChallengeResponse.from(updatedChallenge)
    }

    /**
     * 챌린지 삭제
     */
    @Transactional
    fun deleteChallenge(challengeId: String, loginId: String) {
        val challenge = findChallengeById(challengeId)

        // 권한 검증
        if (challenge.createdId != loginId) {
            throw IllegalArgumentException("챌린지를 삭제할 권한이 없습니다")
        }

        challengeRepository.delete(challenge)
    }

    /**
     * 챌린지 참여
     */
    @Transactional
    fun joinChallenge(challengeId: String, loginId: String): ParticipateResponse {
        val challenge = findChallengeById(challengeId)

        // 이미 참여중인지 확인
        if (participantRepository.existsByIdAndLoginId(challengeId, loginId)) {
            throw IllegalStateException("이미 참여중인 챌린지입니다")
        }

        // 종료된 챌린지는 참여 불가
        if (challenge.isEnded()) {
            throw IllegalStateException("종료된 챌린지에는 참여할 수 없습니다")
        }

        val participant = ChallengeParticipant(
            id = challengeId,
            loginId = loginId
        )

        val savedParticipant = participantRepository.save(participant)

        // 참여자 수 증가
        challenge.participantCnt++
        challengeRepository.save(challenge)

        return ParticipateResponse.from(savedParticipant)
    }

    /**
     * 챌린지 탈퇴
     */
    @Transactional
    fun withdrawChallenge(challengeId: String, loginId: String) {
        val challenge = findChallengeById(challengeId)

        val participant = participantRepository.findByIdAndLoginId(challengeId, loginId)
            .orElseThrow { NoSuchElementException("참여 정보를 찾을 수 없습니다") }

        if (participant.status != ParticipantStatusEnum.ACTIVE) {
            throw IllegalStateException("이미 탈퇴했거나 완료된 챌린지입니다")
        }

        participant.withdraw()
        participantRepository.save(participant)

        // 참여자 수 감소
        challenge.participantCnt--
        challengeRepository.save(challenge)
    }

    /**
     * 조회수 증가 (Redis 카운터 사용)
     */
    @Transactional
    fun incrementViewCount(challengeId: String) {
        val key = "$VIEW_COUNT_KEY_PREFIX$challengeId"
        val viewCount = redisTemplate.opsForValue().increment(key) ?: 1L

        // TTL 설정 (24시간)
        redisTemplate.expire(key, 24, TimeUnit.HOURS)

        // 일정 조회수마다 DB 동기화
        if (viewCount % VIEW_COUNT_SYNC_THRESHOLD == 0L) {
            syncViewCountToDatabase(challengeId, viewCount)
        }
    }

    /**
     * Redis의 조회수를 DB에 동기화
     */
    @Transactional
    fun syncViewCountToDatabase(challengeId: String, viewCount: Long) {
        val challenge = findChallengeById(challengeId)
        challenge.viewCnt = viewCount
        challengeRepository.save(challenge)
    }

    /**
     * 참여자 목록 조회
     */
    fun getParticipants(challengeId: String): List<ParticipateResponse> {
        val challenge = findChallengeById(challengeId)
        val participants = participantRepository.findByChallenge_Id(challengeId)
        return participants.map { ParticipateResponse.from(it) }
    }

    /**
     * 챌린지 통계 조회
     */
    fun getChallengeStatistics(challengeId: String): ChallengeStatisticsResponse {
        val challenge = findChallengeById(challengeId)

        val totalParticipants = participantRepository.countById(challengeId).toInt()
        val activeParticipants = participantRepository.countByIdAndStatus(
            challengeId, ParticipantStatusEnum.ACTIVE
        ).toInt()
        val completedParticipants = participantRepository.countByIdAndStatus(
            challengeId, ParticipantStatusEnum.COMPLETED
        ).toInt()
        val withdrawnParticipants = participantRepository.countByIdAndStatus(
            challengeId, ParticipantStatusEnum.WITHDRAWN
        ).toInt()
        val totalCertifications = participantRepository.sumCertificationCountById(challengeId)

        val completionRate = if (totalParticipants > 0) {
            (completedParticipants.toDouble() / totalParticipants) * 100
        } else {
            0.0
        }

        val averageCertificationPerParticipant = if (totalParticipants > 0) {
            totalCertifications.toDouble() / totalParticipants
        } else {
            0.0
        }

        // Redis에서 조회수 가져오기
        val key = "$VIEW_COUNT_KEY_PREFIX$challengeId"
        val viewCount = redisTemplate.opsForValue().get(key)?.toLongOrNull()
            ?: challenge.viewCnt

        return ChallengeStatisticsResponse(
            challengeId = challengeId,
            totalParticipants = totalParticipants,
            activeParticipants = activeParticipants,
            completedParticipants = completedParticipants,
            withdrawnParticipants = withdrawnParticipants,
            totalCertifications = totalCertifications,
            completionRate = completionRate,
            averageCertificationPerParticipant = averageCertificationPerParticipant,
            viewCount = viewCount
        )
    }

    /**
     * 챌린지 조회 헬퍼 메서드
     */
    private fun findChallengeById(challengeId: String): Challenge {
        return challengeRepository.findById(challengeId)
            .orElseThrow { NoSuchElementException("챌린지를 찾을 수 없습니다: $challengeId") }
    }
}