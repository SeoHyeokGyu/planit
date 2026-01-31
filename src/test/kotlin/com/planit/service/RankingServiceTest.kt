package com.planit.service

import com.planit.entity.User
import com.planit.enums.RankingPeriodType
import com.planit.repository.UserRankingRepository
import com.planit.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import java.time.Duration

@ExtendWith(MockKExtension::class)
class RankingServiceTest {

    @MockK
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var userRankingRepository: UserRankingRepository
    
    @MockK
    private lateinit var rankingSseService: RankingSseService

    @MockK
    private lateinit var zSetOps: ZSetOperations<String, Any>

    @InjectMockKs
    private lateinit var rankingService: RankingService

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        // Mock RedisTemplate operations
        every { redisTemplate.opsForZSet() } returns zSetOps
        
        // Mock RankingSseService injection (since it's setter injected)
        rankingService.setRankingSseService(rankingSseService)

        user = User(
            loginId = "testuser",
            password = "password",
            nickname = "TestUser"
        )
    }

    @Test
    @DisplayName("점수 증가 - 성공 시 Redis 업데이트 및 SSE 브로드캐스트 호출")
    fun `incrementScore should update redis and broadcast`() {
        // Given
        val points = 100L
        val weeklyKey = rankingService.getCurrentWeekKey()
        
        // Mocking redis calls
        every { zSetOps.size(any()) } returns 0L // For isNewKey check
        every { zSetOps.incrementScore(any(), any(), any()) } returns 100.0
        every { redisTemplate.expire(any(), any()) } returns true
        
        // Mocking SSE
        every { rankingSseService.broadcastIfTop10Changed(any(), any(), any(), any()) } just Runs

        // When
        rankingService.incrementScore(user.loginId, points)

        // Then
        // Verify weekly ranking update
        verify { 
            zSetOps.incrementScore(
                match { it.contains(RankingPeriodType.WEEKLY.keyPrefix) }, 
                user.loginId, 
                points.toDouble()
            ) 
        }
        
        // Verify monthly ranking update
        verify { 
            zSetOps.incrementScore(
                match { it.contains(RankingPeriodType.MONTHLY.keyPrefix) }, 
                user.loginId, 
                points.toDouble()
            ) 
        }
        
        // Verify alltime ranking update
        verify { 
            zSetOps.incrementScore(
                match { it.contains(RankingPeriodType.ALLTIME.keyPrefix) }, 
                user.loginId, 
                points.toDouble()
            ) 
        }
        
        // Verify SSE broadcast called 3 times (alltime, weekly, monthly)
        verify(exactly = 3) { rankingSseService.broadcastIfTop10Changed(any(), any(), any(), any()) }
    }

    @Test
    @DisplayName("점수 증가 - 0점 이하일 경우 무시")
    fun `incrementScore should ignore non-positive points`() {
        // When
        rankingService.incrementScore(user.loginId, 0)
        rankingService.incrementScore(user.loginId, -10)

        // Then
        verify(exactly = 0) { zSetOps.incrementScore(any(), any(), any()) }
    }

    @Test
    @DisplayName("랭킹 조회 - 주간 랭킹 조회 성공")
    fun `getWeeklyRanking should return ranking list`() {
        // Given
        val loginId = "testuser"
        val score = 100.0
        val tuple = mockk<ZSetOperations.TypedTuple<Any>>()
        every { tuple.value } returns loginId
        every { tuple.score } returns score

        val rankedUsers: Set<ZSetOperations.TypedTuple<Any>> = setOf(tuple)

        every { zSetOps.size(any()) } returns 1L
        every { zSetOps.reverseRangeWithScores(any(), any(), any()) } returns rankedUsers
        every { userRepository.findByLoginId(loginId) } returns user

        // When
        val result = rankingService.getWeeklyRanking(0, 20)

        // Then
        assertNotNull(result)
        assertEquals(1, result.rankings.size)
        assertEquals(user.nickname, result.rankings[0].nickname)
        assertEquals(100L, result.rankings[0].score)
        assertEquals(RankingPeriodType.WEEKLY, result.periodType)
    }

    @Test
    @DisplayName("내 랭킹 조회 - 성공")
    fun `getMyRankings should return all rankings`() {
        // Given
        every { userRepository.findByLoginId(user.loginId) } returns user
        every { zSetOps.reverseRank(any(), user.loginId) } returns 0L // Rank 1
        every { zSetOps.score(any(), user.loginId) } returns 100.0
        every { zSetOps.size(any()) } returns 10L

        // When
        val result = rankingService.getMyRankings(user.loginId)

        // Then
        assertNotNull(result)
        assertEquals(1, result.weekly.rank)
        assertEquals(100L, result.weekly.score)
        
        assertEquals(1, result.monthly.rank)
        assertEquals(100L, result.monthly.score)
        
        assertEquals(1, result.alltime.rank)
        assertEquals(100L, result.alltime.score)
    }

    @Test
    @DisplayName("랭킹 조회 - 타입별 조회 성공")
    fun `getRankingByType should return correct ranking`() {
        // Given
        every { zSetOps.size(any()) } returns 0L
        every { zSetOps.reverseRangeWithScores(any(), any(), any()) } returns emptySet()

        // When
        val weeklyResult = rankingService.getRankingByType("weekly", 0, 10)
        val monthlyResult = rankingService.getRankingByType("monthly", 0, 10)
        val alltimeResult = rankingService.getRankingByType("all", 0, 10)

        // Then
        assertEquals(RankingPeriodType.WEEKLY, weeklyResult.periodType)
        assertEquals(RankingPeriodType.MONTHLY, monthlyResult.periodType)
        assertEquals(RankingPeriodType.ALLTIME, alltimeResult.periodType)
    }

    @Test
    @DisplayName("DB 동기화 - 성공")
    fun `syncToDatabase should save rankings to repository`() {
        // Given
        val limit = 10
        val tuple = mockk<ZSetOperations.TypedTuple<Any>>()
        every { tuple.value } returns user.loginId
        every { tuple.score } returns 100.0
        
        every { zSetOps.reverseRangeWithScores(any(), 0, (limit - 1).toLong()) } returns setOf(tuple)
        every { userRepository.findByLoginId(user.loginId) } returns user
        every { userRankingRepository.findByUserAndPeriodTypeAndPeriodKey(any(), any(), any()) } returns null
        every { userRankingRepository.save(any()) } returnsArgument 0

        // When
        val count = rankingService.syncToDatabase(RankingPeriodType.WEEKLY, rankingService.getCurrentWeekKey(), limit)

        // Then
        assertEquals(1, count)
        verify { userRankingRepository.save(any()) }
    }

    @Test
    @DisplayName("랭킹 조회 - 지원하지 않는 타입 예외")
    fun `getRankingByType should throw exception for invalid type`() {
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            rankingService.getRankingByType("invalid", 0, 10)
        }
    }

    @Test
    @DisplayName("점수 증가 - Redis 예외 발생 시 로그 출력 및 계속 진행")
    fun `incrementScore should handle redis exceptions`() {
        // Given
        every { zSetOps.size(any()) } returns 0L
        every { zSetOps.incrementScore(any(), any(), any()) } throws RuntimeException("Redis down")

        // When
        rankingService.incrementScore(user.loginId, 100L)

        // Then
        // Should not throw exception
        verify { zSetOps.incrementScore(any(), any(), any()) }
    }

    @Test
    @DisplayName("Redis 복구 - 포인트가 0인 사용자는 제외")
    fun `rebuildAlltimeRanking should skip users with 0 points`() {
        // Given
        val userWithZeroPoints = User(loginId = "zero", password = "p", nickname = "n")
        every { zSetOps.size(any()) } returns 0L
        every { userRepository.findAll() } returns listOf(userWithZeroPoints)
        every { userRankingRepository.findByPeriodTypeAndPeriodKeyOrderByScoreDesc(any(), any()) } returns emptyList()
        
        // When
        rankingService.rebuildFromDatabase()

        // Then
        verify(exactly = 0) { zSetOps.add(any(), "zero", any()) }
    }

    @Test
    @DisplayName("기타 랭킹 조회 메서드들 커버리지")
    fun `other ranking query methods coverage`() {
        // Given
        every { zSetOps.size(any()) } returns 0L
        every { zSetOps.reverseRangeWithScores(any(), any(), any()) } returns emptySet()

        // When
        rankingService.getWeeklyRanking()
        rankingService.getMonthlyRanking()
        rankingService.getAlltimeRanking()

        // Then
        verify { zSetOps.reverseRangeWithScores(any(), 0, 19L) }
    }
}
