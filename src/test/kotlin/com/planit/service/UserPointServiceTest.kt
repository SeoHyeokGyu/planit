package com.planit.service

import com.planit.dto.DailyPointStatistics
import com.planit.entity.User
import com.planit.entity.UserPoint
import com.planit.repository.DailyPointProjection
import com.planit.repository.UserPointRepository
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class UserPointServiceTest {

    @MockK
    private lateinit var userPointRepository: UserPointRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var userPointService: UserPointService

    private val userLoginId = "testuser"
    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = User(loginId = userLoginId, password = "password", nickname = "TestUser")
    }

    @Test
    @DisplayName("포인트 적립 성공")
    fun `addPoint should add points and save history`() {
        // Given
        val points = 100L
        val reason = "Bonus"
        every { userRepository.findByLoginId(userLoginId) } returns user
        every { userPointRepository.save(any<UserPoint>()) } returnsArgument 0

        // When
        userPointService.addPoint(userLoginId, points, reason)

        // Then
        assertEquals(100L, user.totalPoint)
        verify { userPointRepository.save(any<UserPoint>()) }
    }

    @Test
    @DisplayName("포인트 차감 성공")
    fun `subtractPoint should subtract points and save history`() {
        // Given
        user.addPoint(200L)
        val points = 50L
        val reason = "Penalty"
        every { userRepository.findByLoginId(userLoginId) } returns user
        every { userPointRepository.save(any<UserPoint>()) } returnsArgument 0

        // When
        userPointService.subtractPoint(userLoginId, points, reason)

        // Then
        assertEquals(150L, user.totalPoint)
        verify { userPointRepository.save(match { it.points == -50L }) }
    }

    @Test
    @DisplayName("포인트 차감 - 0 이하로 떨어지지 않음")
    fun `subtractPoint should not go below zero`() {
        // Given
        user.addPoint(50L) // 현재 50
        val points = 100L // 100 차감 시도
        val reason = "Penalty"
        every { userRepository.findByLoginId(userLoginId) } returns user
        every { userPointRepository.save(any<UserPoint>()) } returnsArgument 0

        // When
        userPointService.subtractPoint(userLoginId, points, reason)

        // Then
        assertEquals(0L, user.totalPoint) // 0이어야 함
        verify { userPointRepository.save(match { it.points == -100L }) }
    }
    
    @Test
    @DisplayName("포인트 내역 조회 성공")
    fun `getUserPointHistory should return page of points`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val userPoint = UserPoint(user, 100L, "Reason")
        val field = UserPoint::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(userPoint, 1L)
        
        every { userPointRepository.findByUser_LoginId(userLoginId, pageable) } returns PageImpl(listOf(userPoint))

        // When
        val result = userPointService.getUserPointHistory(userLoginId, pageable)

        // Then
        assertEquals(1, result.content.size)
        assertEquals(100L, result.content[0].points)
    }

    @Test
    @DisplayName("포인트 요약 조회 성공")
    fun `getUserPointSummary should return summary`() {
        // Given
        user.addPoint(500L)
        every { userRepository.findByLoginId(userLoginId) } returns user
        every { userPointRepository.countByUser_LoginId(userLoginId) } returns 10L

        // When
        val result = userPointService.getUserPointSummary(userLoginId)

        // Then
        assertEquals(500L, result.totalPoint)
        assertEquals(10L, result.pointCount)
    }

    @Test
    @DisplayName("포인트 통계 조회 성공")
    fun `getPointStatistics should return daily stats filled`() {
        // Given
        val startDate = LocalDate.now().minusDays(2)
        val endDate = LocalDate.now()
        
        val projection = mockk<DailyPointProjection>()
        val date = java.sql.Date.valueOf(startDate)
        every { projection.getDate() } returns date
        every { projection.getTotalPoints() } returns 100L
        every { projection.getTransactionCount() } returns 2
        
        every { userPointRepository.findDailyPointStatistics(any(), any(), any()) } returns listOf(projection)
        every { userPointRepository.sumPointsBeforeDate(any(), any()) } returns 50L

        // When
        val result = userPointService.getPointStatistics(userLoginId, startDate, endDate)

        // Then
        assertNotNull(result)
        assertEquals(3, result.statistics.size) // 3 days
        
        assertEquals(startDate, result.statistics[0].date)
        assertEquals(100L, result.statistics[0].pointsEarned)
        assertEquals(150L, result.statistics[0].cumulativePoints)
        
        assertEquals(startDate.plusDays(1), result.statistics[1].date)
        assertEquals(0L, result.statistics[1].pointsEarned)
        assertEquals(150L, result.statistics[1].cumulativePoints)
    }
    
    @Test
    @DisplayName("사용자 찾을 수 없음 예외")
    fun `addPoint should throw exception when user not found`() {
        // Given
        every { userRepository.findByLoginId(any()) } returns null
        
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            userPointService.addPoint("unknown", 100, "test")
        }
    }

    @Test
    @DisplayName("포인트 차감 시 사용자 찾을 수 없음 예외")
    fun `subtractPoint should throw exception when user not found`() {
        // Given
        every { userRepository.findByLoginId(any()) } returns null
        
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            userPointService.subtractPoint("unknown", 100, "test")
        }
    }

    @Test
    @DisplayName("포인트 요약 조회 시 사용자 찾을 수 없음 예외")
    fun `getUserPointSummary should throw exception when user not found`() {
        // Given
        every { userRepository.findByLoginId(any()) } returns null
        
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            userPointService.getUserPointSummary("unknown")
        }
    }
    
    @Test
    @DisplayName("포인트 통계 조회 - 빈 결과 (날짜 범위 역전)")
    fun `getPointStatistics should handle reverse date range`() {
        // Given
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        every { userPointRepository.findDailyPointStatistics(any(), any(), any()) } returns emptyList()
        every { userPointRepository.sumPointsBeforeDate(any(), any()) } returns 0L

        // When
        val result = userPointService.getPointStatistics(userLoginId, today, yesterday)

        // Then
        assertTrue(result.statistics.isEmpty())
    }
}
