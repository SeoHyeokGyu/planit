package com.planit.service

import com.planit.dto.FeedEvent
import com.planit.dto.FeedResponse
import com.planit.dto.toEvent
import com.planit.dto.toResponse
import com.planit.entity.Certification
import com.planit.entity.Challenge
import com.planit.entity.Feed
import com.planit.entity.User
import com.planit.enums.FeedType
import com.planit.exception.UserNotFoundException
import com.planit.repository.CertificationRepository
import com.planit.repository.FeedRepository
import com.planit.repository.FollowRepository
import com.planit.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 팔로잉 피드(Following Feed) 및 실시간 피드 이벤트 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 사용자가 팔로우하는 사람들의 최신 인증 목록을 제공하고, 실시간 피드 이벤트를 생성 및 브로드캐스트합니다.
 */
@Service
@Transactional
class FeedService(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val certificationRepository: CertificationRepository,
    private val feedRepository: FeedRepository,
    private val feedEventTemplate: RedisTemplate<String, FeedEvent>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 특정 사용자가 팔로우하는 사람들의 최신 인증 목록을 페이징하여 조회합니다.
     * @param userLoginId 피드를 조회할 사용자의 로그인 ID
     * @param pageable 페이징 정보
     * @return 팔로우하는 사용자들의 인증 엔티티 페이지
     * @throws UserNotFoundException 사용자를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    fun getFollowingFeed(userLoginId: String, pageable: Pageable): Page<Certification> {
        val user = userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")

        // 현재 사용자가 팔로우하는 모든 사용자들의 ID 목록을 효율적으로 조회
        val followingUserIds = followRepository.findFollowingIdsByFollowerId(user.id!!)

        // 팔로우하는 사용자가 없으면 빈 페이지 반환
        if (followingUserIds.isEmpty()) {
            return Page.empty(pageable)
        }

        // 팔로우하는 사용자들의 인증을 생성일 내림차순으로 페이징하여 조회
        return certificationRepository.findByUser_IdInOrderByCreatedAtDesc(followingUserIds, pageable)
    }

    /**
     * 인증 생성 시 피드를 생성하고 Redis Pub/Sub을 통해 브로드캐스트합니다.
     * @param user 인증을 생성한 사용자
     * @param certification 생성된 인증
     * @param challenge 관련 챌린지
     * @return 생성된 Feed 엔티티
     */
    fun createCertificationFeed(user: User, certification: Certification, challenge: Challenge): Feed {
        val message = "${user.nickname ?: user.loginId}님이 '${challenge.title}' 챌린지를 인증했습니다!"

        val feed = Feed(
            user = user,
            certification = certification,
            challenge = challenge,
            type = FeedType.CERTIFICATION,
            message = message
        )

        val savedFeed = feedRepository.save(feed)
        logger.info("Created certification feed: id=${savedFeed.id}, user=${user.loginId}, challenge=${challenge.title}")

        // Redis Pub/Sub으로 이벤트 발행
        publishFeedEvent(savedFeed)

        return savedFeed
    }

    /**
     * 피드 이벤트를 Redis Pub/Sub 채널에 발행합니다.
     * @param feed 발행할 피드 엔티티
     */
    private fun publishFeedEvent(feed: Feed) {
        try {
            val event = feed.toEvent()
            feedEventTemplate.convertAndSend("feed-events", event)
            logger.info("Published feed event to Redis: feedId=${feed.id}, type=${feed.type}")
        } catch (e: Exception) {
            logger.error("Failed to publish feed event to Redis: feedId=${feed.id}", e)
            // 발행 실패해도 피드는 저장되므로 예외를 던지지 않음
        }
    }

    /**
     * 피드 목록을 조회합니다.
     * @param userId 조회할 사용자 ID
     * @param pageable 페이징 정보
     * @param type 필터링할 피드 타입 (nullable)
     * @return 피드 응답 DTO 페이지
     */
    @Transactional(readOnly = true)
    fun getFeeds(userId: Long, pageable: Pageable, type: FeedType?): Page<FeedResponse> {
        val feeds = if (type != null) {
            feedRepository.findByTypeOrderByCreatedAtDesc(type, pageable)
        } else {
            feedRepository.findAll(pageable)
        }

        return feeds.map { it.toResponse() }
    }

    /**
     * 팔로잉하는 사용자들의 피드를 조회합니다.
     * @param userId 조회할 사용자 ID
     * @param pageable 페이징 정보
     * @return 피드 응답 DTO 페이지
     */
    @Transactional(readOnly = true)
    fun getFollowingFeeds(userId: Long, pageable: Pageable): Page<FeedResponse> {
        return feedRepository.findByFollowingUsers(userId, pageable)
            .map { it.toResponse() }
    }

    /**
     * 읽지 않은 피드 수를 조회합니다.
     * @param userId 조회할 사용자 ID
     * @return 읽지 않은 피드 수
     */
    @Transactional(readOnly = true)
    fun getUnreadCount(userId: Long): Long {
        return feedRepository.countUnreadFeeds(userId)
    }

    /**
     * 특정 피드를 읽음 처리합니다.
     * @param userId 사용자 ID (권한 확인용)
     * @param feedId 읽음 처리할 피드 ID
     */
    fun markAsRead(userId: Long, feedId: Long) {
        feedRepository.findById(feedId).ifPresent { feed ->
            if (feed.user.id == userId) {
                feed.markAsRead()
                feedRepository.save(feed)
                logger.debug("Marked feed as read: feedId=$feedId, userId=$userId")
            }
        }
    }

    /**
     * 사용자의 모든 피드를 읽음 처리합니다.
     * @param userId 사용자 ID
     */
    fun markAllAsRead(userId: Long) {
        feedRepository.markAllAsRead(userId)
        logger.info("Marked all feeds as read for user: $userId")
    }
}
