package com.planit.service

import com.planit.dto.*
import com.planit.entity.Challenge
import com.planit.entity.ChallengeParticipant
import com.planit.enums.NotificationType
import com.planit.enums.ParticipantStatusEnum
import com.planit.exception.UserNotFoundException
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.ChallengeRepository
import com.planit.repository.LikeRepository
import com.planit.repository.UserRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.NoSuchElementException
import java.util.concurrent.TimeUnit

@Service
@Transactional(readOnly = true)
class ChallengeService(
    private val challengeRepository: ChallengeRepository,
    private val participantRepository: ChallengeParticipantRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val likeRepository: LikeRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService


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
            createdId = loginId,
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

        // Redis에서 최신 조회수 가져오기
        val key = "$VIEW_COUNT_KEY_PREFIX$challengeId"
        val viewCount = redisTemplate.opsForValue().get(key)?.toLongOrNull()
            ?: challenge.viewCnt

        val response = ChallengeResponse.from(challenge)
        return response.copy(viewCnt = viewCount)
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

        // 변경 사항 추적
        val changes = mutableListOf<String>()

        if (challenge.title != request.title) {
            changes.add("제목이 '${challenge.title}'에서 '${request.title}'로 변경되었습니다")
        }
        if (challenge.description != request.description) {
            changes.add("설명이 수정되었습니다")
        }
        if (challenge.category != request.category) {
            changes.add("카테고리가 변경되었습니다")
        }
        if (challenge.difficulty != request.difficulty) {
            changes.add("난이도가 변경되었습니다")
        }

        // 내용 수정 (제목, 설명, 카테고리, 난이도는 언제든지 가능)
        challenge.apply {
            title = request.title
            description = request.description
            category = request.category
            difficulty = request.difficulty
        }

        // 날짜 수정 (진행 중이 아닐 때만 가능)
        if (challenge.isActive()) {
            // 진행 중인 챌린지는 날짜 변경 불가
            if (challenge.startDate != request.startDate || challenge.endDate != request.endDate) {
                throw IllegalStateException("진행 중인 챌린지의 날짜는 수정할 수 없습니다")
            }
        } else {
            // 진행 전 챌린지는 날짜 변경 가능
            if (challenge.startDate != request.startDate) {
                changes.add("시작일이 변경되었습니다")
            }
            if (challenge.endDate != request.endDate) {
                changes.add("종료일이 변경되었습니다")
            }
            challenge.startDate = request.startDate
            challenge.endDate = request.endDate
        }

        val updatedChallenge = challengeRepository.save(challenge)

        // 변경사항이 있고 참여자가 있으면 알림 전송
        if (changes.isNotEmpty()) {
            sendUpdateNotificationToParticipants(challenge, changes, loginId)
        }

        return ChallengeResponse.from(updatedChallenge)
    }

    /**
     * 챌린지 수정 시 참여자들에게 알림 전송
     */
    private fun sendUpdateNotificationToParticipants(
        challenge: Challenge,
        changes: List<String>,
        editorLoginId: String
    ) {
        // 활성 참여자 조회 (생성자 제외)
        val activeParticipants = participantRepository.findByIdAndStatus(
            challenge.id,
            ParticipantStatusEnum.ACTIVE
        ).filter { it.loginId != editorLoginId } // 수정한 사람은 제외

        if (activeParticipants.isEmpty()) {
            return
        }

        // 수정한 사용자 정보 조회
        val editor = userRepository.findByLoginId(editorLoginId)
            ?: throw UserNotFoundException()

        // 변경 내용 요약 메시지 생성
        val changeMessage = if (changes.size == 1) {
            changes[0]
        } else {
            "${changes.size}개 항목이 수정되었습니다"
        }

        var successCount = 0
        var failCount = 0

        // 각 참여자에게 알림 전송
        activeParticipants.forEach { participant ->
            try {
                val user = userRepository.findByLoginId(participant.loginId)

                if (user != null) {
                    notificationService.sendNotification(
                        NotificationResponse(
                            id = -1L,
                            receiverId = user.id!!,
                            receiverLoginId = user.loginId,
                            senderId = editor.id,
                            senderLoginId = editor.loginId,
                            senderNickname = editor.nickname,
                            type = NotificationType.CHALLENGE,
                            message = "'${challenge.title}' 챌린지가 수정되었습니다. $changeMessage",
                            relatedId = challenge.id,
                            relatedType = "CHALLENGE",
                            isRead = false,
                            createdAt = LocalDateTime.now()
                        )
                    )
                    successCount++
                } else {
                    failCount++
                }
            } catch (e: Exception) {
                failCount++
            }
        }

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

        val user = userRepository.findByLoginId(loginId) ?: throw UserNotFoundException()
        if (challenge.createdId != loginId) {

            // 챌린지 생성자 조회
            val challengeCreator = userRepository.findByLoginId(challenge.createdId)
                ?: throw UserNotFoundException()

            notificationService.sendNotification(
                NotificationResponse(
                    id = -1L,
                    receiverId = challengeCreator.id!!,
                    receiverLoginId = challenge.createdId,
                    senderId = user.id,
                    senderLoginId = user.loginId,
                    senderNickname = user.nickname,
                    type = NotificationType.CHALLENGE,
                    message = "${user.nickname ?: user.loginId}님이 회원님의 챌린지에 참여했습니다.",
                    relatedId = challenge.id,
                    relatedType = "CHALLENGE",
                    isRead = false,
                    createdAt = LocalDateTime.now()
                )
            )
        }

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

        val user = userRepository.findByLoginId(loginId) ?: throw UserNotFoundException()
        if (challenge.createdId != loginId) {

            // 챌린지 생성자 조회
            val challengeCreator = userRepository.findByLoginId(challenge.createdId)
                ?: throw UserNotFoundException()

            notificationService.sendNotification(
                NotificationResponse(
                    id = -1L,
                    receiverId = challengeCreator.id!!,
                    receiverLoginId = challenge.createdId,
                    senderId = user.id,
                    senderLoginId = user.loginId,
                    senderNickname = user.nickname,
                    type = NotificationType.CHALLENGE,
                    message = "${user.nickname ?: user.loginId}님이 회원님의 챌린지를 포기했습니다.",
                    relatedId = challenge.id,
                    relatedType = "CHALLENGE",
                    isRead = false,
                    createdAt = LocalDateTime.now()
                )
            )
        }
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
        findChallengeById(challengeId)
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
            id = challengeId,
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

     * 사용자가 참여중인 챌린지 목록 조회

     */

    fun getParticipatingChallenges(loginId: String): List<ChallengeListResponse> {

        val participants = participantRepository.findByLoginIdAndStatus(

            loginId,

            ParticipantStatusEnum.ACTIVE

        )

        return participants.map { ChallengeListResponse.from(it.challenge) }

    }



    /**

     * 챌린지 조회 헬퍼 메서드

     */

    private fun findChallengeById(challengeId: String): Challenge {

        return challengeRepository.findById(challengeId)

            .orElseThrow { NoSuchElementException("챌린지를 찾을 수 없습니다: $challengeId") }

    }

}