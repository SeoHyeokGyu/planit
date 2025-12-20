package com.planit.scheduler

import com.planit.service.ChallengeService
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ViewCountScheduler(
    private val challengeService: ChallengeService,
    private val redisTemplate: RedisTemplate<String, String>
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val VIEW_COUNT_KEY_PATTERN = "challenge:view:*"
        private const val VIEW_COUNT_KEY_PREFIX = "challenge:view:"
    }

    /**
     * 매일 새벽 3시에 모든 Redis 조회수를 DB에 동기화
     * - Redis에 있는 모든 조회수 데이터를 DB에 저장
     * - Redis 만료(24시간) 전에 데이터 유실 방지
     */
    @Scheduled(cron = "0 0 3 * * *")
    fun syncAllViewCounts() {
        logger.info("=== 조회수 동기화 스케줄러 시작 ===")

        try {
            val keys = redisTemplate.keys(VIEW_COUNT_KEY_PATTERN) ?: emptySet()
            var successCount = 0
            var failCount = 0

            logger.info("동기화 대상: ${keys.size}개 챌린지")

            keys.forEach { key ->
                try {
                    val challengeId = key.removePrefix(VIEW_COUNT_KEY_PREFIX)
                    val viewCount = redisTemplate.opsForValue().get(key)?.toLongOrNull()

                    if (viewCount != null && viewCount > 0) {
                        challengeService.syncViewCountToDatabase(challengeId, viewCount)
                        logger.debug("동기화 완료: $challengeId -> $viewCount")
                        successCount++
                    }
                } catch (e: Exception) {
                    logger.error("동기화 실패: $key", e)
                    failCount++
                }
            }

            logger.info("=== 조회수 동기화 완료 ===  성공: $successCount, 실패: $failCount")

        } catch (e: Exception) {
            logger.error("조회수 동기화 스케줄러 오류", e)
        }
    }

}