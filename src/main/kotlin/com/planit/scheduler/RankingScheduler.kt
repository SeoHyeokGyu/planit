package com.planit.scheduler

import com.planit.enums.RankingPeriodType
import com.planit.service.RankingService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 랭킹 데이터 동기화 스케줄러입니다.
 * Redis 랭킹 데이터를 주기적으로 DB에 저장하여 데이터 내구성을 보장합니다.
 */
@Component
class RankingScheduler(
    private val rankingService: RankingService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val TOP_SYNC_LIMIT = 1000
    }

    /**
     * 매일 새벽 4시에 전체 랭킹을 DB에 동기화합니다.
     * 전체 랭킹은 TTL이 없으므로 주기적인 백업이 필요합니다.
     */
    @Scheduled(cron = "0 0 4 * * *")
    fun syncAlltimeRanking() {
        log.info("=== 전체 랭킹 동기화 시작 ===")
        try {
            val syncCount = rankingService.syncToDatabase(
                periodType = RankingPeriodType.ALLTIME,
                periodKey = "alltime",
                limit = TOP_SYNC_LIMIT
            )
            log.info("=== 전체 랭킹 동기화 완료: {}건 ===", syncCount)
        } catch (e: Exception) {
            log.error("전체 랭킹 동기화 실패", e)
        }
    }

    /**
     * 매주 일요일 23시 55분에 주간 랭킹을 DB에 아카이브합니다.
     * 주간 랭킹이 리셋되기 전에 최종 결과를 저장합니다.
     */
    @Scheduled(cron = "0 55 23 * * SUN")
    fun archiveWeeklyRanking() {
        log.info("=== 주간 랭킹 아카이브 시작 ===")
        try {
            val periodKey = rankingService.getCurrentWeekKey()
            val syncCount = rankingService.syncToDatabase(
                periodType = RankingPeriodType.WEEKLY,
                periodKey = periodKey,
                limit = TOP_SYNC_LIMIT
            )
            log.info("=== 주간 랭킹 아카이브 완료: {} - {}건 ===", periodKey, syncCount)
        } catch (e: Exception) {
            log.error("주간 랭킹 아카이브 실패", e)
        }
    }

    /**
     * 매월 마지막 날 23시 55분에 월간 랭킹을 DB에 아카이브합니다.
     * 월간 랭킹이 리셋되기 전에 최종 결과를 저장합니다.
     */
    @Scheduled(cron = "0 55 23 L * *")
    fun archiveMonthlyRanking() {
        log.info("=== 월간 랭킹 아카이브 시작 ===")
        try {
            val periodKey = rankingService.getCurrentMonthKey()
            val syncCount = rankingService.syncToDatabase(
                periodType = RankingPeriodType.MONTHLY,
                periodKey = periodKey,
                limit = TOP_SYNC_LIMIT
            )
            log.info("=== 월간 랭킹 아카이브 완료: {} - {}건 ===", periodKey, syncCount)
        } catch (e: Exception) {
            log.error("월간 랭킹 아카이브 실패", e)
        }
    }

    /**
     * 매일 새벽 5시에 현재 주간/월간 랭킹을 DB에 백업합니다.
     * Redis 장애 대비 일일 백업입니다.
     */
    @Scheduled(cron = "0 0 5 * * *")
    fun dailyBackup() {
        log.info("=== 랭킹 일일 백업 시작 ===")
        try {
            // 주간 랭킹 백업
            val weeklyKey = rankingService.getCurrentWeekKey()
            val weeklySyncCount = rankingService.syncToDatabase(
                periodType = RankingPeriodType.WEEKLY,
                periodKey = weeklyKey,
                limit = TOP_SYNC_LIMIT
            )

            // 월간 랭킹 백업
            val monthlyKey = rankingService.getCurrentMonthKey()
            val monthlySyncCount = rankingService.syncToDatabase(
                periodType = RankingPeriodType.MONTHLY,
                periodKey = monthlyKey,
                limit = TOP_SYNC_LIMIT
            )

            log.info("=== 랭킹 일일 백업 완료: 주간 {}건, 월간 {}건 ===", weeklySyncCount, monthlySyncCount)
        } catch (e: Exception) {
            log.error("랭킹 일일 백업 실패", e)
        }
    }
}
