package com.planit.service

import com.planit.dto.RankingEntryResponse
import com.planit.dto.RankingUpdateEvent
import com.planit.dto.SseConnectionStatus
import com.planit.dto.UpdatedUserInfo
import com.planit.enums.RankingPeriodType
import com.planit.repository.UserRepository
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * 랭킹 실시간 업데이트를 위한 SSE 서비스입니다.
 * 모든 연결된 클라이언트에게 Top 10 랭킹 변경을 브로드캐스트합니다.
 */
@Service
class RankingSseService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // 모든 연결된 SSE Emitter를 관리 (client ID -> Emitter)
    private val emitters = ConcurrentHashMap<String, SseEmitter>()
    private val clientIdGenerator = AtomicLong(0)

    companion object {
        private const val SSE_TIMEOUT = 30 * 60 * 1000L // 30분
        private const val HEARTBEAT_INTERVAL = 30 * 1000L // 30초
        private const val TOP_10_LIMIT = 10
    }

    /**
     * 새로운 SSE 클라이언트 연결을 생성합니다.
     * @return SseEmitter 인스턴스
     */
    fun subscribe(): SseEmitter {
        val clientId = "ranking-${clientIdGenerator.incrementAndGet()}"
        val emitter = SseEmitter(SSE_TIMEOUT)

        emitters[clientId] = emitter
        log.info("랭킹 SSE 구독 시작: {} (현재 연결: {}명)", clientId, emitters.size)

        // 연결 종료 핸들러
        emitter.onCompletion {
            log.debug("랭킹 SSE 연결 완료: {}", clientId)
            emitters.remove(clientId)
        }

        emitter.onTimeout {
            log.debug("랭킹 SSE 타임아웃: {}", clientId)
            emitters.remove(clientId)
        }

        emitter.onError { ex ->
            log.warn("랭킹 SSE 에러: {} - {}", clientId, ex.message)
            emitters.remove(clientId)
        }

        // 초기 연결 확인 이벤트 전송
        try {
            val connectionStatus = SseConnectionStatus(
                connectedClients = emitters.size,
                status = "connected"
            )
            emitter.send(
                SseEmitter.event()
                    .name("connect")
                    .data(connectionStatus)
            )

            // 연결 직후 현재 Top 10 전송
            sendInitialRankings(emitter)

        } catch (e: IOException) {
            log.warn("랭킹 SSE 초기 데이터 전송 실패: {}", clientId)
            emitters.remove(clientId)
        }

        return emitter
    }

    /**
     * 연결 직후 현재 Top 10 랭킹을 전송합니다.
     */
    private fun sendInitialRankings(emitter: SseEmitter) {
        try {
            // 전체 랭킹 Top 10 전송
            val alltimeTop10 = getTop10FromRedis(RankingPeriodType.ALLTIME, "alltime")
            val initialEvent = RankingUpdateEvent(
                eventType = "INITIAL_RANKING",
                periodType = RankingPeriodType.ALLTIME,
                periodKey = "alltime",
                top10 = alltimeTop10,
                updatedUser = null
            )

            emitter.send(
                SseEmitter.event()
                    .name("ranking")
                    .data(initialEvent)
            )
        } catch (e: Exception) {
            log.warn("초기 랭킹 데이터 전송 실패: {}", e.message)
        }
    }

    /**
     * 점수 업데이트 후 Top 10에 변경이 있으면 모든 클라이언트에게 브로드캐스트합니다.
     *
     * @param userLoginId 점수가 업데이트된 사용자 로그인 ID
     * @param points 증가된 점수
     * @param periodType 기간 유형
     * @param periodKey 기간 키
     */
    fun broadcastIfTop10Changed(
        userLoginId: String,
        points: Long,
        periodType: RankingPeriodType,
        periodKey: String
    ) {
        val redisKey = "${periodType.keyPrefix}:$periodKey"
        val zSetOps = redisTemplate.opsForZSet()

        // 사용자의 현재 순위 확인 (0-indexed)
        val currentRankIndex = zSetOps.reverseRank(redisKey, userLoginId)

        // Top 10 내에 있는지 확인
        if (currentRankIndex != null && currentRankIndex < TOP_10_LIMIT) {
            val currentRank = currentRankIndex.toInt() + 1
            val currentScore = zSetOps.score(redisKey, userLoginId)?.toLong() ?: 0L
            val previousScore = currentScore - points
            val previousRankIndex = calculatePreviousRank(redisKey, userLoginId, previousScore)

            val user = userRepository.findByLoginId(userLoginId)
            val updatedUserInfo = UpdatedUserInfo(
                userId = user?.id,
                loginId = userLoginId,
                nickname = user?.nickname,
                previousRank = previousRankIndex?.let { it + 1 },
                currentRank = currentRank,
                scoreDelta = points,
                newScore = currentScore
            )

            // Top 10 목록 가져오기
            val top10 = getTop10FromRedis(periodType, periodKey)

            val event = RankingUpdateEvent(
                periodType = periodType,
                periodKey = periodKey,
                top10 = top10,
                updatedUser = updatedUserInfo
            )

            broadcastEvent(event)
        }
    }

    /**
     * 이전 점수 기준 순위를 계산합니다 (근사치).
     */
    @Suppress("UNUSED_PARAMETER")
    private fun calculatePreviousRank(redisKey: String, userLoginId: String, previousScore: Long): Int? {
        if (previousScore <= 0) return null

        val zSetOps = redisTemplate.opsForZSet()
        // 이전 점수보다 높은 사용자 수 = 이전 순위 - 1
        val higherCount = zSetOps.count(redisKey, previousScore.toDouble() + 0.1, Double.MAX_VALUE)
        return higherCount?.toInt()
    }

    /**
     * Redis에서 Top 10 랭킹을 가져옵니다.
     */
    private fun getTop10FromRedis(periodType: RankingPeriodType, periodKey: String): List<RankingEntryResponse> {
        val redisKey = "${periodType.keyPrefix}:$periodKey"
        val zSetOps = redisTemplate.opsForZSet()

        val topUsers = zSetOps.reverseRangeWithScores(redisKey, 0, (TOP_10_LIMIT - 1).toLong())
            ?: return emptyList()

        return topUsers.mapIndexedNotNull { index, typedTuple ->
            val loginId = typedTuple.value?.toString() ?: return@mapIndexedNotNull null
            val score = typedTuple.score?.toLong() ?: 0L
            val user = userRepository.findByLoginId(loginId)

            RankingEntryResponse(
                rank = index + 1,
                userId = user?.id,
                loginId = loginId,
                nickname = user?.nickname,
                score = score
            )
        }
    }

    /**
     * 모든 연결된 클라이언트에게 이벤트를 브로드캐스트합니다.
     */
    private fun broadcastEvent(event: RankingUpdateEvent) {
        if (emitters.isEmpty()) {
            log.debug("브로드캐스트 스킵: 연결된 클라이언트 없음")
            return
        }

        log.info("랭킹 업데이트 브로드캐스트: {} {} (연결된 클라이언트: {}명)",
            event.periodType, event.periodKey, emitters.size)

        val deadEmitters = mutableListOf<String>()

        emitters.forEach { (clientId, emitter) ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("ranking")
                        .data(event)
                )
            } catch (e: Exception) {
                log.debug("클라이언트 {} 전송 실패, 제거 예정", clientId)
                deadEmitters.add(clientId)
            }
        }

        // 실패한 emitter 정리
        deadEmitters.forEach { clientId ->
            emitters.remove(clientId)
        }

        if (deadEmitters.isNotEmpty()) {
            log.info("비활성 클라이언트 {}개 제거, 남은 연결: {}명", deadEmitters.size, emitters.size)
        }
    }

    /**
     * Heartbeat를 전송하여 연결을 유지합니다.
     * 30초마다 실행됩니다.
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL)
    fun sendHeartbeat() {
        if (emitters.isEmpty()) return

        val deadEmitters = mutableListOf<String>()

        emitters.forEach { (clientId, emitter) ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("heartbeat")
                        .data(mapOf("timestamp" to System.currentTimeMillis()))
                )
            } catch (e: Exception) {
                deadEmitters.add(clientId)
            }
        }

        deadEmitters.forEach { clientId ->
            emitters.remove(clientId)
        }

        if (deadEmitters.isNotEmpty()) {
            log.debug("Heartbeat 실패로 {}개 연결 제거", deadEmitters.size)
        }
    }

    /**
     * 현재 연결된 클라이언트 수를 반환합니다.
     */
    fun getConnectedClientCount(): Int = emitters.size
}
