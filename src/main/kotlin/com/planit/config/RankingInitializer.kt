package com.planit.config

import com.planit.service.RankingService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * 애플리케이션 시작 시 Redis 랭킹 데이터를 복구하는 초기화 컴포넌트입니다.
 * Redis 데이터가 유실된 경우 DB에서 복구합니다.
 */
@Component
class RankingInitializer(
    private val rankingService: RankingService
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments?) {
        log.info("랭킹 데이터 초기화 시작...")
        try {
            rankingService.rebuildFromDatabase()
            log.info("랭킹 데이터 초기화 완료")
        } catch (e: Exception) {
            log.error("랭킹 데이터 초기화 실패", e)
        }
    }
}
