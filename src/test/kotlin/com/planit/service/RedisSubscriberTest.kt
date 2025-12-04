package com.planit.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.planit.config.RedisConfig
import com.planit.dto.ForwardedFeedEvent
import com.planit.entity.Certification
import com.planit.entity.Challenge
import com.planit.entity.Follow
import com.planit.entity.User
import com.planit.repository.CertificationRepository
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.FollowRepository
import com.planit.repository.UserRepository
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
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("RedisSubscriber (Coordinator) 테스트")
class RedisSubscriberTest {

    @MockK private lateinit var objectMapper: ObjectMapper
    @MockK(relaxUnitFun = true) private lateinit var redisTemplate: RedisTemplate<String, Any>
    @MockK private lateinit var redisPublisher: RedisPublisher
    @MockK private lateinit var certificationRepository: CertificationRepository
    @MockK private lateinit var followRepository: FollowRepository
    @MockK private lateinit var challengeParticipantRepository: ChallengeParticipantRepository
    @MockK private lateinit var userRepository: UserRepository
    @MockK private lateinit var valueOperations: ValueOperations<String, Any>

    @InjectMockKs private lateinit var redisSubscriber: RedisSubscriber

    // Test Data
    private lateinit var author: User
    private lateinit var follower: User
    private lateinit var challenge: Challenge
    private lateinit var certification: Certification
    private val certificationId = 1L

    @BeforeEach
    fun setUp() {
        author = User("author", "pw", "author-nick").apply { setPrivateProperty("id", 1L) }
        follower = User("follower", "pw", "follower-nick").apply { setPrivateProperty("id", 2L) }
        challenge = Challenge("Challenge", "", "", LocalDateTime.now(), LocalDateTime.now().plusDays(1), "", "").apply {
            setPrivateProperty("id", 101L)
            setPrivateProperty("challengeId", "CHL-101")
        }
        certification = Certification(author, challenge, "Cert Title", "Content").apply {
            setPrivateProperty("id", certificationId)
        }

        every { redisTemplate.opsForValue() } returns valueOperations
    }

    @Nested
    @DisplayName("onMessage 메서드는")
    inner class DescribeOnMessage {
        @Test
        @DisplayName("글로벌 채널에서 인증 ID를 받으면, 수신자들의 인스턴스 채널로 이벤트를 전달한다")
        fun `forwards events to instance channels for recipients`() {
            // Given
            val message = mockk<Message>()
            val followerInstanceId = "instance-follower"

            every { message.body } returns certificationId.toString().toByteArray()
            every { objectMapper.readValue(message.body, Long::class.java) } returns certificationId
            every { certificationRepository.findById(certificationId) } returns Optional.of(certification)

            // Mock recipient calculation (produces userId)
            every { followRepository.findAllByFollowingId(author.id!!, Pageable.unpaged()) } returns PageImpl(listOf(Follow(follower, author)))
            every { challengeParticipantRepository.findByChallengeIdAndStatus(challenge.challengeId, any()) } returns emptyList()

            // Mock userId to loginId conversion
            every { userRepository.findAllById(setOf(follower.id!!)) } returns listOf(follower)

            // Mock Redis lookup for instance ID (uses loginId)
            val followerKey = FeedService.USER_INSTANCE_KEY_PREFIX + follower.loginId
            every { valueOperations.get(followerKey) } returns followerInstanceId
            
            val forwardedEventSlot = slot<ForwardedFeedEvent>()
            every { redisPublisher.publish(any(), capture(forwardedEventSlot)) } just Runs

            // When
            redisSubscriber.onMessage(message, null)

            // Then
            val expectedInstanceChannel = RedisConfig.INSTANCE_FEED_CHANNEL_PREFIX + followerInstanceId
            verify { redisPublisher.publish(expectedInstanceChannel, any()) }

            // Check the content of the forwarded event
            val capturedEvent = forwardedEventSlot.captured
            assertThat(capturedEvent.loginId).isEqualTo(follower.loginId) // Changed to loginId
            assertThat(capturedEvent.feedEvent.type).isEqualTo("new-certification")
            val data = capturedEvent.feedEvent.data as Map<*, *>
            assertThat(data["certificationId"]).isEqualTo(certificationId)
            assertThat(data["title"]).isEqualTo("Cert Title")
        }
    }
}
