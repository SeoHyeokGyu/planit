package com.planit.service

import com.planit.config.InstanceIdProvider
import com.planit.entity.*
import com.planit.enums.ParticipantStatusEnum
import com.planit.repository.*
import com.planit.util.setPrivateProperty
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Duration
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("FeedService 테스트")
class FeedServiceTest {

    // Mocks for dependencies
    @MockK private lateinit var emitterRepository: EmitterRepository
    @MockK private lateinit var certificationRepository: CertificationRepository
    @MockK private lateinit var challengeParticipantRepository: ChallengeParticipantRepository
    @MockK private lateinit var followRepository: FollowRepository
    @MockK private lateinit var challengeRepository: ChallengeRepository
    @MockK private lateinit var instanceIdProvider: InstanceIdProvider
    @MockK private lateinit var redisTemplate: RedisTemplate<String, Any>
    @MockK private lateinit var valueOperations: ValueOperations<String, Any>
    @MockK private lateinit var userRepository: UserRepository

    @InjectMockKs private lateinit var feedService: FeedService

    // Test data
    private lateinit var user: User
    private lateinit var otherUser1: User
    private lateinit var challenge1: Challenge
    private lateinit var challenge2: Challenge

    @BeforeEach
    fun setUp() {
        user = User("user", "pw", "user-nick").apply { setPrivateProperty("id", 1L) }
        otherUser1 = User("other1", "pw", "other1-nick").apply { setPrivateProperty("id", 2L) }
        challenge1 = Challenge("Challenge 1", "", "", LocalDateTime.now(), LocalDateTime.now().plusDays(1), "", "").apply {
            setPrivateProperty("id", 101L)
            setPrivateProperty("challengeId", "CHL-101")
        }
        challenge2 = Challenge("Challenge 2", "", "", LocalDateTime.now(), LocalDateTime.now().plusDays(1), "", "").apply {
            setPrivateProperty("id", 102L)
            setPrivateProperty("challengeId", "CHL-102")
        }

        // Mock redisTemplate behavior
        every { redisTemplate.opsForValue() } returns valueOperations
        every { redisTemplate.delete(any<String>()) } returns true
        every { redisTemplate.expire(any<String>(), any<Duration>()) } returns true
        every { valueOperations.set(any(), any(), any<Duration>()) } just Runs
    }

    @Nested
    @DisplayName("getFollowingFeed 메서드는")
    inner class DescribeGetFollowingFeed {
        @Test
        @DisplayName("팔로우하는 사용자들의 인증 피드를 페이지로 반환한다")
        fun `returns a paginated feed of certifications from followed users`() {
            // Given
            val pageable = PageRequest.of(0, 10)
            val follow = Follow(follower = user, following = otherUser1)
            val certifications = listOf(Certification(otherUser1, challenge1, "Cert 1", "Content"))
            val certPage = PageImpl(certifications, pageable, 1)

            every { userRepository.findByLoginId(user.loginId) } returns user
            every { followRepository.findAllByFollowerId(user.id!!, any()) } returns PageImpl(listOf(follow))
            every { certificationRepository.findByUser_IdInOrderByCreatedAtDesc(listOf(otherUser1.id!!), pageable) } returns certPage

            // When
            val result = feedService.getFollowingFeed(user.loginId, pageable)

            // Then
            assertThat(result.totalElements).isEqualTo(1)
            assertThat(result.content.first().title).isEqualTo("Cert 1")
            verify { certificationRepository.findByUser_IdInOrderByCreatedAtDesc(listOf(otherUser1.id!!), pageable) }
        }
    }

    @Nested
    @DisplayName("getFeedForUser 메서드는")
    inner class DescribeGetFeedForUser {
        @Test
        @DisplayName("사용자가 참여중인 챌린지들의 인증 피드를 페이지로 반환한다")
        fun `returns a paginated feed from challenges the user is participating in`() {
            // Given
            val pageable = PageRequest.of(0, 10)
            val participant1 = ChallengeParticipant(challengeId = challenge1.challengeId, loginId = user.id!!)
            val certifications = listOf(Certification(otherUser1, challenge1, "Cert 1", "Content"))
            val certPage = PageImpl(certifications, pageable, 1)

            every { userRepository.findByLoginId(user.loginId) } returns user
            every { challengeParticipantRepository.findByLoginIdAndStatus(user.id!!, ParticipantStatusEnum.ACTIVE) } returns listOf(participant1)
            every { challengeRepository.findByChallengeIdIn(listOf(challenge1.challengeId)) } returns listOf(challenge1)
            every { certificationRepository.findByChallenge_IdInOrderByCreatedAtDesc(listOf(challenge1.id!!), pageable) } returns certPage

            // When
            val result = feedService.getFeedForUser(user.loginId, pageable)

            // Then
            assertThat(result.totalElements).isEqualTo(1)
            assertThat(result.content.first().title).isEqualTo("Cert 1")
            verify { certificationRepository.findByChallenge_IdInOrderByCreatedAtDesc(listOf(challenge1.id!!), pageable) }
        }
    }

    @Nested
    @DisplayName("subscribe 메서드는")
    inner class DescribeSubscribe {
        @Test
        @DisplayName("SseEmitter를 생성하고 Redis에 사용자-인스턴스 정보를 저장한다")
        fun `creates an SseEmitter and stores user-instance info in Redis`() {
            // Given
            val loginId = user.loginId
            val userId = user.id!! // Still needed for userRepository.findByLoginId
            val instanceId = "test-instance-id"
            val emitter = SseEmitter(60000L)
            val capturedRunnable = slot<Runnable>()

            every { userRepository.findByLoginId(loginId) } returns user
            every { instanceIdProvider.id } returns instanceId
            every { emitterRepository.save(loginId, any()) } just Runs // Changed to loginId
            // Capture the onCompletion runnable
            every { any<SseEmitter>().onCompletion(capture(capturedRunnable)) } just Runs
            every { any<SseEmitter>().onTimeout(any()) } just Runs
            every { any<SseEmitter>().send(any<SseEmitter.SseEventBuilder>()) } returns mockk()

            // When
            val resultEmitter = feedService.subscribe(loginId) // Changed to loginId
            // Manually trigger onCompletion to test cleanup
            capturedRunnable.captured.run()


            // Then
            assertThat(resultEmitter).isNotNull
            verify { emitterRepository.save(loginId, resultEmitter) } // Changed to loginId
            val expectedKey = FeedService.USER_INSTANCE_KEY_PREFIX + loginId // Changed to loginId
            verify { valueOperations.set(expectedKey, instanceId, any<Duration>()) }
            
            // Verify cleanup
            verify { emitterRepository.deleteById(loginId) } // Changed to loginId
            verify { redisTemplate.delete(expectedKey) }
        }
    }

    @Nested
    @DisplayName("sendHeartbeat 메서드는")
    inner class DescribeSendHeartbeat {
        @Test
        @DisplayName("모든 연결된 클라이언트에게 하트비트를 전송하고 Redis TTL을 갱신한다")
        fun `sends heartbeat to all connected clients and refreshes Redis TTL`() {
            // Given
            val loginId = user.loginId
            val emitter = SseEmitter(60000L)
            val emittersMap = mapOf(loginId to emitter)

            every { emitterRepository.findAll() } returns emittersMap
            every { emitter.send(any<SseEmitter.SseEventBuilder>()) } just Runs
            every { redisTemplate.expire(any<String>(), any<Duration>()) } returns true

            // When
            feedService.sendHeartbeat()

            // Then
            verify(exactly = 1) { emitter.send(any<SseEmitter.SseEventBuilder>()) }
            verify(exactly = 1) { redisTemplate.expire(FeedService.USER_INSTANCE_KEY_PREFIX + loginId, any<Duration>()) }
        }
    }
}