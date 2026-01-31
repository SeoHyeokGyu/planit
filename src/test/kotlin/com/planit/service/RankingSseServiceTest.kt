package com.planit.service

import com.planit.dto.RankingUpdateEvent
import com.planit.entity.User
import com.planit.enums.RankingPeriodType
import com.planit.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@ExtendWith(MockKExtension::class)
class RankingSseServiceTest {

    @MockK
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @MockK
    private lateinit var userRepository: UserRepository
    
    @MockK
    private lateinit var zSetOps: ZSetOperations<String, Any>

    @InjectMockKs
    private lateinit var rankingSseService: RankingSseService

    private val userLoginId = "testuser"
    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = User(loginId = userLoginId, password = "password", nickname = "TestUser")
        every { redisTemplate.opsForZSet() } returns zSetOps
    }

    @Test
    @DisplayName("SSE 구독 성공")
    fun `subscribe should return emitter and send initial data`() {
        // Given
        val redisKey = "${RankingPeriodType.ALLTIME.keyPrefix}:alltime"
        every { zSetOps.reverseRangeWithScores(redisKey, any(), any()) } returns emptySet()

        // When
        val emitter = rankingSseService.subscribe()

        // Then
        assertNotNull(emitter)
        assertEquals(1, rankingSseService.getConnectedClientCount())
    }

    @Test
    @DisplayName("Top 10 변경 시 브로드캐스트 - 랭킹 진입")
    fun `broadcastIfTop10Changed should broadcast when user in top 10`() {
        // Given
        val points = 100L
        val periodType = RankingPeriodType.WEEKLY
        val periodKey = "2026-W05"
        val redisKey = "${periodType.keyPrefix}:$periodKey"
        
        // Mock redis ranking check
        every { zSetOps.reverseRank(redisKey, userLoginId) } returns 0L // Rank 1
        every { zSetOps.score(redisKey, userLoginId) } returns 100.0
        every { zSetOps.count(redisKey, any(), any()) } returns 0L // No previous higher scores
        
        // Mock top 10 retrieval
        val tuple = mockk<ZSetOperations.TypedTuple<Any>>()
        every { tuple.value } returns userLoginId
        every { tuple.score } returns 100.0
        every { zSetOps.reverseRangeWithScores(redisKey, 0, 9L) } returns setOf(tuple)
        
        every { userRepository.findByLoginId(userLoginId) } returns user

        // Add a subscriber to verify broadcast
        val emitter = rankingSseService.subscribe()
        
        // When
        rankingSseService.broadcastIfTop10Changed(userLoginId, points, periodType, periodKey)

        // Then
        // We can't easily verify emitter.send() was called without spying on the specific emitter created inside subscribe()
        // But we can verify interactions with mocks that happen during broadcast
        verify { zSetOps.reverseRank(redisKey, userLoginId) }
        verify { zSetOps.reverseRangeWithScores(redisKey, 0, 9L) }
        
        // Verify user repository call
        verify(atLeast = 1) { userRepository.findByLoginId(userLoginId) }
    }

    @Test
    @DisplayName("Top 10 밖이면 브로드캐스트 안 함")
    fun `broadcastIfTop10Changed should skip when user out of top 10`() {
        // Given
        val points = 100L
        val periodType = RankingPeriodType.WEEKLY
        val periodKey = "2026-W05"
        val redisKey = "${periodType.keyPrefix}:$periodKey"
        
        // Mock redis ranking check - rank 11 (index 10)
        every { zSetOps.reverseRank(redisKey, userLoginId) } returns 10L 
        
        // When
        rankingSseService.broadcastIfTop10Changed(userLoginId, points, periodType, periodKey)

        // Then
        verify { zSetOps.reverseRank(redisKey, userLoginId) }
        verify(exactly = 0) { zSetOps.reverseRangeWithScores(any(), any(), any()) }
    }
    
    @Test
    @DisplayName("Heartbeat 전송")
    fun `sendHeartbeat should send event to connected clients`() {
        // Given
        val redisKey = "${RankingPeriodType.ALLTIME.keyPrefix}:alltime"
        every { zSetOps.reverseRangeWithScores(redisKey, any(), any()) } returns emptySet()
        rankingSseService.subscribe() // Add 1 client

        // When
        rankingSseService.sendHeartbeat()
        
        // Then
        assertEquals(1, rankingSseService.getConnectedClientCount())
        // Cannot verify specific send calls easily as emitter is internal
    }
}
