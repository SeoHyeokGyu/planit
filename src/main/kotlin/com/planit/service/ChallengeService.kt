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
    fun createChallenge(request: ChallengeRequest, loginId: Long): ChallengeResponse {
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
    fun getChallengeById(id: Long): ChallengeResponse {
        val challenge = findChallengeById(id)
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
    fun updateChallenge(id: Long, request: ChallengeRequest, loginId: Long): ChallengeResponse {
        val challenge = findChallengeById(id)

        // 권한 검증
        if (challenge.createdId != loginId.toString()) {
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
    fun deleteChallenge(id: Long, loginId: Long) {
        val challenge = findChallengeById(id)

        // 권한 검증
        if (challenge.createdId != loginId.toString()) {
            throw IllegalArgumentException("챌린지를 삭제할 권한이 없습니다")
        }

        challengeRepository.delete(challenge)
    }

    /**
     * 챌린지 참여
     */
    @Transactional
    fun joinChallenge(challengeId: Long, loginId: Long): ParticipateResponse {
        val challenge = findChallengeById(challengeId)

        // 이미 참여중인지 확인
        if (participantRepository.existsByChallengeIdAndLoginId(challenge.challengeId, loginId)) {
            throw IllegalStateException("이미 참여중인 챌린지입니다")
        }

        // 종료된 챌린지는 참여 불가
        if (challenge.isEnded()) {
            throw IllegalStateException("종료된 챌린지에는 참여할 수 없습니다")
        }

        val participant = ChallengeParticipant(
            challengeId = challenge.challengeId,
            loginId = loginId
        )

        val savedParticipant = participantRepository.save(participant)

        // 참여자 수 증가
        challengeRepository.incrementParticipantCount(challengeId)

        return ParticipateResponse.from(savedParticipant)
    }

    /**
     * 챌린지 탈퇴
     */
    @Transactional
    fun withdrawChallenge(challengeId: Long, loginId: Long) {
        val challenge = findChallengeById(challengeId)

        val participant = participantRepository.findByChallengeIdAndLoginId(challenge.challengeId, loginId)
            .orElseThrow { NoSuchElementException("참여 정보를 찾을 수 없습니다") }

        if (participant.status != ParticipantStatusEnum.ACTIVE) {
            throw IllegalStateException("이미 탈퇴했거나 완료된 챌린지입니다")
        }

        participant.withdraw()
        participantRepository.save(participant)

        // 참여자 수 감소
        challengeRepository.decrementParticipantCount(challengeId)
    }

    /**
     * 조회수 증가 (Redis 카운터 사용)
     */
    @Transactional
    fun incrementViewCount(id: Long) {
        val key = "$VIEW_COUNT_KEY_PREFIX$id"
        val viewCount = redisTemplate.opsForValue().increment(key) ?: 1L

        // TTL 설정 (24시간)
        redisTemplate.expire(key, 24, TimeUnit.HOURS)

        // 일정 조회수마다 DB 동기화
        if (viewCount % VIEW_COUNT_SYNC_THRESHOLD == 0L) {
            syncViewCountToDatabase(id, viewCount)
        }
    }

    /**
     * Redis의 조회수를 DB에 동기화
     */
    @Transactional
    fun syncViewCountToDatabase(id: Long, viewCount: Long) {
        val challenge = findChallengeById(id)
        challenge.viewCnt = viewCount
        challengeRepository.save(challenge)
    }

    /**
     * 참여자 목록 조회
     */
    fun getParticipants(challengeId: Long): List<ParticipateResponse> {
        val challenge = findChallengeById(challengeId)
        val participants = participantRepository.findByChallengeId(challenge.challengeId)
        return participants.map { ParticipateResponse.from(it) }
    }

    /**
     * 챌린지 통계 조회
     */
    fun getChallengeStatistics(challengeId: Long): ChallengeStatisticsResponse {
        val challenge = findChallengeById(challengeId)

        val totalParticipants = participantRepository.countByChallengeId(challenge.challengeId).toInt()
        val activeParticipants = participantRepository.countByChallengeIdAndStatus(
            challenge.challengeId, ParticipantStatusEnum.ACTIVE
        ).toInt()
        val completedParticipants = participantRepository.countByChallengeIdAndStatus(
            challenge.challengeId, ParticipantStatusEnum.COMPLETED
        ).toInt()
        val withdrawnParticipants = participantRepository.countByChallengeIdAndStatus(
            challenge.challengeId, ParticipantStatusEnum.WITHDRAWN
        ).toInt()
        val totalCertifications = participantRepository.sumCertificationCountByChallengeId(challenge.challengeId)

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
            ?: challenge.viewCnt ?: 0L

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
    private fun findChallengeById(id: Long): Challenge {
        return challengeRepository.findById(id)
            .orElseThrow { NoSuchElementException("챌린지를 찾을 수 없습니다: $id") }
    }
}