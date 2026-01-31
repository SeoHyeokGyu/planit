package com.planit.scheduler

import com.planit.enums.RankingPeriodType
import com.planit.service.RankingService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class RankingSchedulerTest {

    @MockK
    private lateinit var rankingService: RankingService

    @InjectMockKs
    private lateinit var scheduler: RankingScheduler

    @Test
    @DisplayName("전체 랭킹 동기화")
    fun `syncAlltimeRanking coverage`() {
        // Success
        every { rankingService.syncToDatabase(any(), any(), any()) } returns 10
        scheduler.syncAlltimeRanking()
        
        // Fail
        every { rankingService.syncToDatabase(any(), any(), any()) } throws RuntimeException("Error")
        scheduler.syncAlltimeRanking()
        
        verify(exactly = 2) { rankingService.syncToDatabase(any(), any(), any()) }
    }

    @Test
    @DisplayName("주간 랭킹 아카이브")
    fun `archiveWeeklyRanking coverage`() {
        every { rankingService.getCurrentWeekKey() } returns "2026-W05"
        
        // Success
        every { rankingService.syncToDatabase(any(), any(), any()) } returns 5
        scheduler.archiveWeeklyRanking()
        
        // Fail
        every { rankingService.syncToDatabase(any(), any(), any()) } throws RuntimeException("Error")
        scheduler.archiveWeeklyRanking()
        
        verify(exactly = 2) { rankingService.syncToDatabase(RankingPeriodType.WEEKLY, "2026-W05", any()) }
    }

    @Test
    @DisplayName("월간 랭킹 아카이브")
    fun `archiveMonthlyRanking coverage`() {
        every { rankingService.getCurrentMonthKey() } returns "2026-01"
        
        // Success
        every { rankingService.syncToDatabase(any(), any(), any()) } returns 5
        scheduler.archiveMonthlyRanking()
        
        // Fail
        every { rankingService.syncToDatabase(any(), any(), any()) } throws RuntimeException("Error")
        scheduler.archiveMonthlyRanking()
        
        verify(exactly = 2) { rankingService.syncToDatabase(RankingPeriodType.MONTHLY, "2026-01", any()) }
    }

    @Test
    @DisplayName("일일 백업")
    fun `dailyBackup coverage`() {
        every { rankingService.getCurrentWeekKey() } returns "W"
        every { rankingService.getCurrentMonthKey() } returns "M"
        
        // Success
        every { rankingService.syncToDatabase(any(), any(), any()) } returns 1
        scheduler.dailyBackup()
        
        // Fail on first sync
        every { rankingService.syncToDatabase(RankingPeriodType.WEEKLY, "W", any()) } throws RuntimeException("Error")
        scheduler.dailyBackup()
        
        // verify: 2 from first call (weekly, monthly), 1 from second call (only weekly before exception)
        verify(exactly = 3) { rankingService.syncToDatabase(any(), any(), any()) }
    }
}
