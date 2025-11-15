package com.planit.service

import com.planit.dto.*
import com.planit.dto.response.*
import com.planit.entity.Challenge
import com.planit.entity.ChallengeParticipant
import com.planit.entity.ParticipantStatus
import com.planit.exception.ChallengeNotFoundException
import com.planit.exception.DuplicateParticipationException
import com.planit.exception.UnauthorizedException
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.ChallengeRepository
import com.planit.repository.ChallengeSpecifications
import com.planit.entity.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    fun createChallenge(request: ChallengeCreateRequest, userId: Long): ChallengeResponse {
        val challenge = Challenge(
            title = request.title,
            description = request.description,
            category = request.category,
            startDate = request.startDate,
            endDate = request.endDate,
            difficulty = request.difficulty,
            tags = request.tags.toMutableSet(),
            createdBy = userId
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
     * 챌린지 목록 조회 (페이징, 필터링, 정렬)
     */
    fun getChallenges(request: ChallengeSearchRequest): PageResponse<ChallengeListResponse> {
        val pageable = PageRequest.of(
            request.page,
            request.size,
            Sort.by(
                if (request.direction == "ASC") Sort.Direction.ASC else Sort.Direction.DESC,
                request.sort
            )
        )

        val page = when {
            // 키워드 검색 (최우선)
            !request.keyword.isNullOrBlank() -> {
                challengeRepository.findByTitleContainingOrDescriptionContaining(
                    request.keyword,
                    request.keyword,
                    pageable
                )
            }
            // 태그 검색
            !request.tags.isNullOrEmpty() -> {
                challengeRepository.findByTagsIn(request.tags, pageable)
            }
            // 카테고리 필터
            request.category != null -> {
                challengeRepository.findByCategory(request.category, pageable)
            }
            // 난이도 필터
            request.difficulty != null -> {
                challengeRepository.findByDifficulty(request.difficulty, pageable)
            }
            // 상태 필터 (Specification 사용)
            request.status != null -> {
                val spec = when (request.status) {
                    ChallengeStatusFilter.ACTIVE -> ChallengeSpecifications.isActive()
                    ChallengeStatusFilter.UPCOMING -> ChallengeSpecifications.isUpcoming()
                    ChallengeStatusFilter.ENDED -> ChallengeSpecifications.isEnded()
                    else -> Specification.where(null)
                }
                challengeRepository.findAll(spec, pageable)
            }
            // 기본: 전체 조회
            else -> {
                challengeRepository.findAll(pageable)
            }
        }

        return PageResponse(
            content = page.content.map { ChallengeListResponse.from(it) },
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            isFirst = page.isFirst,
            isLast = page.isLast
        )
    }

    /**
     * 챌린지 검색 (Full-Text Search)
     */
    fun searchChallenges(keyword: String, page: Int, size: Int): PageResponse<ChallengeListResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val challengePage = challengeRepository.findByTitleContainingOrDescriptionContaining(
            keyword,
            keyword,
            pageable
        )

        return PageResponse(
            content = challengePage.content.map { ChallengeListResponse.from(it) },
            page = challengePage.number,
            size = challengePage.size,
            totalElements = challengePage.totalElements,
            totalPages = challengePage.totalPages,
            isFirst = challengePage.isFirst,
            isLast = challengePage.isLast
        )
    }

    /**
     * 챌린지 수정
     */
    @Transactional
    fun updateChallenge(id: Long, request: ChallengeUpdateRequest, userId: Long): ChallengeResponse {
        val challenge = findChallengeById(id)

        // 권한 검증
        if (challenge.createdBy != userId) {
            throw UnauthorizedException("챌린지를 수정할 권한이 없습니다")
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
            tags = request.tags.toMutableSet()
        }

        val updatedChallenge = challengeRepository.save(challenge)
        return ChallengeResponse.from(updatedChallenge)
    }

    /**
     * 챌린지 삭제 (소프트 삭제)
     */
    @Transactional
    fun deleteChallenge(id: Long, userId: Long) {
        val challenge = findChallengeById(id)

        // 권한 검증
        if (challenge.createdBy != userId) {
            throw UnauthorizedException("챌린지를 삭제할 권한이 없습니다")
        }

        // 소프트 삭제는 JPA의 @SQLDelete 어노테이션으로 자동 처리됨
        challengeRepository.delete(challenge)
    }

    /**
     * 챌린지 참여
     */
    @Transactional
    fun joinChallenge(challengeId: Long, userId: Long): ParticipantResponse {
        val challenge = findChallengeById(challengeId)

        // 이미 참여중인지 확인
        if (participantRepository.existsByChallengeIdAndUserId(challengeId, userId)) {
            throw DuplicateParticipationException("이미 참여중인 챌린지입니다")
        }

        // 종료된 챌린지는 참여 불가
        if (challenge.isEnded()) {
            throw IllegalStateException("종료된 챌린지에는 참여할 수 없습니다")
        }

        val participant = ChallengeParticipant(
            challenge = challenge,
            userId = userId
        )

        val savedParticipant = participantRepository.save(participant)

        // 참여자 수 증가
        challengeRepository.incrementParticipantCount(challengeId)

        return ParticipantResponse.from(savedParticipant)
    }

    /**
     * 챌린지 탈퇴
     */
    @Transactional
    fun withdrawChallenge(challengeId: Long, userId: Long) {
        val participant = participantRepository.findByChallengeIdAndUserId(challengeId, userId)
            .orElseThrow { IllegalStateException("참여 정보를 찾을 수 없습니다") }

        if (participant.status != ParticipantStatus.ACTIVE) {
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
        challenge.viewCount = viewCount
        challengeRepository.save(challenge)
    }

    /**
     * 참여자 목록 조회
     */
    fun getParticipants(challengeId: Long, page: Int, size: Int): PageResponse<ParticipantResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "joinedAt"))
        val participantPage = participantRepository.findByChallengeId(challengeId, pageable)

        return PageResponse(
            content = participantPage.content.map { ParticipantResponse.from(it) },
            page = participantPage.number,
            size = participantPage.size,
            totalElements = participantPage.totalElements,
            totalPages = participantPage.totalPages,
            isFirst = participantPage.isFirst,
            isLast = participantPage.isLast
        )
    }

    /**
     * 챌린지 통계 조회
     */
    fun getChallengeStatistics(challengeId: Long): ChallengeStatisticsResponse {
        findChallengeById(challengeId) // 챌린지 존재 여부 확인

        val totalParticipants = participantRepository.countByChallengeId(challengeId).toInt()
        val activeParticipants = participantRepository.countByChallengeIdAndStatus(
            challengeId, ParticipantStatus.ACTIVE
        ).toInt()
        val completedParticipants = participantRepository.countByChallengeIdAndStatus(
            challengeId, ParticipantStatus.COMPLETED
        ).toInt()
        val withdrawnParticipants = participantRepository.countByChallengeIdAndStatus(
            challengeId, ParticipantStatus.WITHDRAWN
        ).toInt()
        val totalCertifications = participantRepository.sumCertificationCountByChallengeId(challengeId)

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
            ?: challengeRepository.findById(challengeId).map { it.viewCount }.orElse(0L)

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
            .orElseThrow { ChallengeNotFoundException("챌린지를 찾을 수 없습니다: $id") }
    }
}
