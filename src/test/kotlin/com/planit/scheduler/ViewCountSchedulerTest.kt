package com.planit.scheduler

import com.planit.service.ChallengeService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations

@ExtendWith(MockKExtension::class)
class ViewCountSchedulerTest {

    @MockK
    private lateinit var challengeService: ChallengeService

    @MockK
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @MockK
    private lateinit var valueOperations: ValueOperations<String, String>

    @InjectMockKs
    private lateinit var scheduler: ViewCountScheduler

    @Test
    @DisplayName("전체 조회수 동기화 - 성공")
    fun `syncAllViewCounts should sync all keys from redis`() {
        // Given
        val key1 = "challenge:view:CHL-1"
        val key2 = "challenge:view:CHL-2"
        val keys = setOf(key1, key2)
        
        every { redisTemplate.keys(any()) } returns keys
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.get(key1) } returns "100"
        every { valueOperations.get(key2) } returns "200"
        every { challengeService.syncViewCountToDatabase(any(), any()) } just Runs

        // When
        scheduler.syncAllViewCounts()

        // Then
        verify { challengeService.syncViewCountToDatabase("CHL-1", 100L) }
        verify { challengeService.syncViewCountToDatabase("CHL-2", 200L) }
    }

    @Test
    @DisplayName("전체 조회수 동기화 - 예외 발생 시 로그 출력 및 계속 진행")
    fun `syncAllViewCounts should handle exceptions within loop`() {
        // Given
        val key1 = "challenge:view:CHL-1"
        every { redisTemplate.keys(any()) } returns setOf(key1)
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.get(key1) } throws RuntimeException("Redis error")

        // When
        scheduler.syncAllViewCounts()

        // Then
        // Should not throw exception
        verify(exactly = 0) { challengeService.syncViewCountToDatabase(any(), any()) }
    }

    @Test
    @DisplayName("전체 조회수 동기화 - 값이 null이거나 0이면 스킵")
    fun `syncAllViewCounts should skip null or zero counts`() {
        // Given
        val keyNull = "challenge:view:NULL"
        val keyZero = "challenge:view:ZERO"
        
        every { redisTemplate.keys(any()) } returns setOf(keyNull, keyZero)
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.get(keyNull) } returns null
        every { valueOperations.get(keyZero) } returns "0"

        // When
        scheduler.syncAllViewCounts()

        // Then
        verify(exactly = 0) { challengeService.syncViewCountToDatabase(any(), any()) }
    }
}
