package com.planit.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.planit.config.RedisConfig
import com.planit.dto.FeedEvent
import com.planit.dto.ForwardedFeedEvent
import com.planit.enums.ParticipantStatusEnum
import com.planit.repository.CertificationRepository
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.FollowRepository
import com.planit.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

/**
 * 전역 피드 채널(GLOBAL_FEED_CHANNEL)을 구독하는 리스너 (코디네이터 역할).
 * 새로운 인증 이벤트가 발생하면, 이벤트를 수신하여 알림을 받을 모든 사용자 목록을 계산하고,
 * 각 사용자가 연결된 인스턴스의 전용 채널로 이벤트를 다시 전달(forward)합니다.
 */
@Service
class RedisSubscriber(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val redisPublisher: RedisPublisher,
    private val certificationRepository: CertificationRepository,
    private val followRepository: FollowRepository,
    private val challengeParticipantRepository: ChallengeParticipantRepository,
    private val userRepository: UserRepository
) : MessageListener {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Redis 전역 채널에서 메시지를 수신했을 때 호출됩니다.
     */
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val certificationId = try {
            objectMapper.readValue(message.body, Long::class.java)
        } catch (e: Exception) {
            log.error("메시지 역직렬화 실패: ${message.body}", e)
            return
        }

        log.info("코디네이터가 새로운 인증 이벤트를 수신했습니다. 인증 ID: $certificationId")

        val certification = certificationRepository.findById(certificationId).orElse(null)
        if (certification == null) {
            log.warn("인증 ID $certificationId 에 해당하는 인증을 찾을 수 없습니다. 메시지를 무시합니다.")
            return
        }

        val author = certification.user
        val challenge = certification.challenge
        val authorId = author.id
        if (authorId == null) {
            log.warn("인증 ID $certificationId 의 작성자 ID가 null입니다. 메시지를 무시합니다.")
            return
        }

        // 1. 알림 대상자 ID 목록 계산 (기존 로직과 동일)
        val followers = followRepository.findAllByFollowingId(authorId, Pageable.unpaged()).content
        val followerIds = followers.mapNotNull { it.follower.id }.toSet()
        val participants = challengeParticipantRepository.findByChallengeIdAndStatus(challenge.challengeId, ParticipantStatusEnum.ACTIVE)
        val participantIds = participants.map { it.loginId }.toSet()
        val recipientUserIds = (followerIds + participantIds) - authorId

        if (recipientUserIds.isEmpty()) {
            log.info("인증 ID $certificationId 에 대한 수신자가 없습니다.")
            return
        }

        // 2. 숫자 userId를 loginId로 변환
        val recipientUsers = userRepository.findAllById(recipientUserIds)
        val recipientLoginIds = recipientUsers.mapNotNull { it.loginId }.toSet()

        // 3. 피드 이벤트 페이로드 생성
        val feedEvent = FeedEvent(
            type = "new-certification",
            data = mapOf(
                "certificationId" to certification.id,
                "title" to certification.title,
                "content" to certification.content,
                "photoUrl" to certification.photoUrl,
                "createdAt" to certification.createdAt.toString(),
                "author" to mapOf("id" to authorId, "nickname" to author.nickname),
                "challenge" to mapOf("id" to challenge.challengeId, "title" to challenge.title)
            )
        )

        // 4. 각 수신자가 연결된 인스턴스로 이벤트 전달
        recipientLoginIds.forEach { loginId ->
            val userInstanceKey = FeedService.USER_INSTANCE_KEY_PREFIX + loginId
            val instanceId = redisTemplate.opsForValue().get(userInstanceKey) as? String

            if (instanceId != null) {
                val instanceChannel = RedisConfig.INSTANCE_FEED_CHANNEL_PREFIX + instanceId
                val forwardedEvent = ForwardedFeedEvent(loginId, feedEvent)
                log.info("사용자 '$loginId'에 대한 이벤트를 인스턴스 채널 '$instanceChannel'로 전달합니다.")
                redisPublisher.publish(instanceChannel, forwardedEvent)
            } else {
                log.warn("사용자 '$loginId'에 대한 인스턴스를 찾을 수 없습니다. SSE 연결이 존재하지 않을 수 있습니다.")
            }
        }
    }
}
