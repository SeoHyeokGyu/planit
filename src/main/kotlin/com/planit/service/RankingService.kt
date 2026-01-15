package com.planit.service

import com.planit.dto.AllMyRankingsResponse
import com.planit.dto.MyRankingResponse
import com.planit.dto.RankingEntryResponse
import com.planit.dto.RankingListResponse
import com.planit.entity.UserRanking
import com.planit.enums.RankingPeriodType
import com.planit.exception.UserNotFoundException
import com.planit.repository.UserRankingRepository
import com.planit.repository.UserRepository
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Redis Sorted Set(ZSET)을 사용한 랭킹 서비스입니다.
 * 주간, 월간, 전체 랭킹을 관리합니다.
 */
@Service
class RankingService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val userRepository: UserRepository,
    private val userRankingRepository: UserRankingRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // SSE 서비스는 순환 의존성 방지를 위해 setter injection 사용
    private var rankingSseService: RankingSseService? = null

    @org.springframework.beans.factory.annotation.Autowired
    fun setRankingSseService(@org.springframework.context.annotation.Lazy sseService: RankingSseService) {
        this.rankingSseService = sseService
    }

    companion object {
        private const val DEFAULT_TOP_LIMIT = 100
        private const val DEFAULT_PAGE_SIZE = 20
        private const val MAX_PAGE_SIZE = 100
        private val WEEKLY_TTL = Duration.ofDays(14)
        private val MONTHLY_TTL = Duration.ofDays(45)
        private const val ALLTIME_KEY = "alltime"
    }

    // ==================== Score Update Operations ====================

    /**
     * 사용자 점수를 증가시킵니다.
     * 주간, 월간, 전체 랭킹 모두 업데이트합니다.
     * Top 10에 변경이 있으면 SSE로 실시간 브로드캐스트합니다.
     *
     * @param userLoginId 사용자 로그인 ID
     * @param points 증가시킬 점수
     */
    fun incrementScore(userLoginId: String, points: Long) {
        if (points <= 0) return

        val weeklyKey = buildWeeklyKey()
        val monthlyKey = buildMonthlyKey()
        val alltimeKey = buildAlltimeKey()

        try {
            // 주간 랭킹 업데이트
            incrementAndSetTTL(weeklyKey, userLoginId, points, WEEKLY_TTL)

            // 월간 랭킹 업데이트
            incrementAndSetTTL(monthlyKey, userLoginId, points, MONTHLY_TTL)

            // 전체 랭킹 업데이트 (TTL 없음)
            redisTemplate.opsForZSet().incrementScore(alltimeKey, userLoginId, points.toDouble())

            log.debug("랭킹 점수 업데이트: {} +{} points", userLoginId, points)

            // SSE로 Top 10 변경 브로드캐스트 (전체 랭킹 기준)
            broadcastTop10Update(userLoginId, points)

        } catch (e: Exception) {
            log.error("랭킹 점수 업데이트 실패: {}", userLoginId, e)
        }
    }

    /**
     * Top 10 변경 시 SSE로 브로드캐스트합니다.
     */
    private fun broadcastTop10Update(userLoginId: String, points: Long) {
        try {
            rankingSseService?.let { sseService ->
                // 전체 랭킹 기준으로 Top 10 변경 확인 및 브로드캐스트
                sseService.broadcastIfTop10Changed(
                    userLoginId = userLoginId,
                    points = points,
                    periodType = RankingPeriodType.ALLTIME,
                    periodKey = ALLTIME_KEY
                )

                // 주간 랭킹도 브로드캐스트 (선택적)
                sseService.broadcastIfTop10Changed(
                    userLoginId = userLoginId,
                    points = points,
                    periodType = RankingPeriodType.WEEKLY,
                    periodKey = getCurrentWeekKey()
                )

                // 월간 랭킹도 브로드캐스트 (선택적)
                sseService.broadcastIfTop10Changed(
                    userLoginId = userLoginId,
                    points = points,
                    periodType = RankingPeriodType.MONTHLY,
                    periodKey = getCurrentMonthKey()
                )
            }
        } catch (e: Exception) {
            log.warn("SSE 브로드캐스트 실패 (무시됨): {}", e.message)
        }
    }

    private fun incrementAndSetTTL(key: String, member: String, points: Long, ttl: Duration) {
        val zSetOps = redisTemplate.opsForZSet()
        val isNewKey = zSetOps.size(key) == 0L

        zSetOps.incrementScore(key, member, points.toDouble())

        // 새로운 키인 경우에만 TTL 설정
        if (isNewKey) {
            redisTemplate.expire(key, ttl)
            log.debug("새 랭킹 키 생성 및 TTL 설정: {} (TTL: {})", key, ttl)
        }
    }

    // ==================== Ranking Query Operations ====================

    /**
     * 타입별 페이지네이션된 랭킹을 조회합니다.
     * 통합 API 엔드포인트에서 사용됩니다.
     *
     * @param type 랭킹 타입 (weekly, monthly, all)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 페이지네이션된 랭킹 목록
     */
    @Transactional(readOnly = true)
    fun getRankingByType(type: String, page: Int, size: Int): RankingListResponse {
        val periodType = when (type.lowercase()) {
            "weekly" -> RankingPeriodType.WEEKLY
            "monthly" -> RankingPeriodType.MONTHLY
            "all", "alltime" -> RankingPeriodType.ALLTIME
            else -> throw IllegalArgumentException("지원하지 않는 랭킹 타입입니다: $type (weekly, monthly, all 중 선택)")
        }

        val periodKey = when (periodType) {
            RankingPeriodType.WEEKLY -> getCurrentWeekKey()
            RankingPeriodType.MONTHLY -> getCurrentMonthKey()
            RankingPeriodType.ALLTIME -> ALLTIME_KEY
        }

        return getRankingPaginated(periodType, periodKey, page, size)
    }

    /**
     * 주간 랭킹을 조회합니다.
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 주간 랭킹 목록
     */
    @Transactional(readOnly = true)
    fun getWeeklyRanking(page: Int = 0, size: Int = DEFAULT_PAGE_SIZE): RankingListResponse {
        val periodKey = getCurrentWeekKey()
        return getRankingPaginated(RankingPeriodType.WEEKLY, periodKey, page, size)
    }

    /**
     * 월간 랭킹을 조회합니다.
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 월간 랭킹 목록
     */
    @Transactional(readOnly = true)
    fun getMonthlyRanking(page: Int = 0, size: Int = DEFAULT_PAGE_SIZE): RankingListResponse {
        val periodKey = getCurrentMonthKey()
        return getRankingPaginated(RankingPeriodType.MONTHLY, periodKey, page, size)
    }

    /**
     * 전체 랭킹을 조회합니다.
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 전체 랭킹 목록
     */
    @Transactional(readOnly = true)
    fun getAlltimeRanking(page: Int = 0, size: Int = DEFAULT_PAGE_SIZE): RankingListResponse {
        return getRankingPaginated(RankingPeriodType.ALLTIME, ALLTIME_KEY, page, size)
    }

    /**
     * 페이지네이션된 랭킹을 조회합니다.
     * Redis ZREVRANGE를 사용하여 점수 내림차순으로 조회합니다.
     *
     * @param periodType 기간 유형
     * @param periodKey 기간 키
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 페이지네이션된 랭킹 목록
     */
    private fun getRankingPaginated(
        periodType: RankingPeriodType,
        periodKey: String,
        page: Int,
        size: Int
    ): RankingListResponse {
        val redisKey = buildRedisKey(periodType, periodKey)
        val zSetOps = redisTemplate.opsForZSet()

        // 페이지 크기 제한
        val validSize = size.coerceIn(1, MAX_PAGE_SIZE)
        val validPage = maxOf(0, page)

        // 전체 참여자 수
        val totalParticipants = zSetOps.size(redisKey) ?: 0L

        // 페이지네이션 계산
        val startIndex = (validPage * validSize).toLong()
        val endIndex = startIndex + validSize - 1

        // Redis ZREVRANGE로 점수 내림차순 조회
        val rankedUsers = zSetOps.reverseRangeWithScores(redisKey, startIndex, endIndex)
            ?: emptySet()

        // 사용자 정보 조회 및 DTO 변환
        val rankings = rankedUsers.mapIndexedNotNull { index, typedTuple ->
            val loginId = typedTuple.value?.toString() ?: return@mapIndexedNotNull null
            val score = typedTuple.score?.toLong() ?: 0L
            val user = userRepository.findByLoginId(loginId)

            RankingEntryResponse(
                rank = (startIndex + index + 1).toInt(), // 전체 순위 계산
                userId = user?.id,
                loginId = loginId,
                nickname = user?.nickname,
                score = score
            )
        }

        // 페이지네이션 정보 계산
        val totalPages = if (totalParticipants == 0L) 1 else ((totalParticipants + validSize - 1) / validSize).toInt()

        return RankingListResponse(
            periodType = periodType,
            periodKey = periodKey,
            rankings = rankings,
            totalParticipants = totalParticipants,
            page = validPage,
            size = validSize,
            totalPages = totalPages,
            isFirst = validPage == 0,
            isLast = validPage >= totalPages - 1
        )
    }

    /**
     * 레거시 호환: limit 기반 랭킹 조회
     */
    private fun getRanking(
        periodType: RankingPeriodType,
        periodKey: String,
        limit: Int
    ): RankingListResponse {
        val redisKey = buildRedisKey(periodType, periodKey)
        val zSetOps = redisTemplate.opsForZSet()

        // Redis에서 상위 N명 조회 (점수 내림차순)
        val topUsers = zSetOps.reverseRangeWithScores(redisKey, 0, (limit - 1).toLong())
            ?: emptySet()

        val totalParticipants = zSetOps.size(redisKey) ?: 0L

        // 사용자 정보 조회 및 DTO 변환
        val rankings = topUsers.mapIndexedNotNull { index, typedTuple ->
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

        return RankingListResponse(
            periodType = periodType,
            periodKey = periodKey,
            rankings = rankings,
            totalParticipants = totalParticipants,
            page = 0,
            size = limit,
            totalPages = 1,
            isFirst = true,
            isLast = true
        )
    }

    // ==================== User-specific Ranking Operations ====================

    /**
     * 현재 사용자의 모든 기간 랭킹 정보를 조회합니다.
     *
     * @param userLoginId 사용자 로그인 ID
     * @return 모든 기간의 랭킹 정보
     */
    @Transactional(readOnly = true)
    fun getMyRankings(userLoginId: String): AllMyRankingsResponse {
        // 사용자 존재 확인
        userRepository.findByLoginId(userLoginId) ?: throw UserNotFoundException()

        return AllMyRankingsResponse(
            weekly = getMyRanking(userLoginId, RankingPeriodType.WEEKLY, getCurrentWeekKey()),
            monthly = getMyRanking(userLoginId, RankingPeriodType.MONTHLY, getCurrentMonthKey()),
            alltime = getMyRanking(userLoginId, RankingPeriodType.ALLTIME, ALLTIME_KEY)
        )
    }

    /**
     * 특정 기간에서 현재 사용자의 랭킹 정보를 조회합니다.
     */
    private fun getMyRanking(
        userLoginId: String,
        periodType: RankingPeriodType,
        periodKey: String
    ): MyRankingResponse {
        val redisKey = buildRedisKey(periodType, periodKey)
        val zSetOps = redisTemplate.opsForZSet()

        // 사용자 순위 조회 (0-indexed, 내림차순)
        val rankIndex = zSetOps.reverseRank(redisKey, userLoginId)
        val rank = rankIndex?.let { it.toInt() + 1 }

        // 사용자 점수 조회
        val score = zSetOps.score(redisKey, userLoginId)?.toLong() ?: 0L

        // 전체 참여자 수
        val totalParticipants = zSetOps.size(redisKey) ?: 0L

        return MyRankingResponse(
            periodType = periodType,
            periodKey = periodKey,
            rank = rank,
            score = score,
            totalParticipants = totalParticipants
        )
    }

    // ==================== DB Synchronization Operations ====================

    /**
     * Redis 랭킹 데이터를 DB에 동기화합니다.
     *
     * @param periodType 동기화할 기간 유형
     * @param periodKey 동기화할 기간 키
     * @param limit 동기화할 상위 사용자 수
     */
    @Transactional
    fun syncToDatabase(
        periodType: RankingPeriodType,
        periodKey: String,
        limit: Int = DEFAULT_TOP_LIMIT
    ): Int {
        val redisKey = buildRedisKey(periodType, periodKey)
        val zSetOps = redisTemplate.opsForZSet()

        val topUsers = zSetOps.reverseRangeWithScores(redisKey, 0, (limit - 1).toLong())
            ?: return 0

        var syncCount = 0

        topUsers.forEachIndexed { index, typedTuple ->
            val loginId = typedTuple.value?.toString() ?: return@forEachIndexed
            val score = typedTuple.score?.toLong() ?: 0L
            val rank = index + 1

            val user = userRepository.findByLoginId(loginId) ?: return@forEachIndexed

            // 기존 레코드 찾기 또는 새로 생성
            val userRanking = userRankingRepository.findByUserAndPeriodTypeAndPeriodKey(
                user, periodType, periodKey
            ) ?: UserRanking(
                user = user,
                periodType = periodType,
                periodKey = periodKey
            )

            userRanking.updateScore(score)
            userRanking.updateRank(rank)
            userRankingRepository.save(userRanking)
            syncCount++
        }

        log.info("DB 동기화 완료: {} {} - {}건", periodType, periodKey, syncCount)
        return syncCount
    }

    /**
     * DB에서 Redis로 랭킹 데이터를 복구합니다.
     * 서버 재시작 시 사용됩니다.
     */
    @Transactional(readOnly = true)
    fun rebuildFromDatabase() {
        log.info("=== Redis 랭킹 데이터 복구 시작 ===")

        // 전체 랭킹 복구 (user.totalPoint 기반)
        rebuildAlltimeRanking()

        // 현재 주간/월간 랭킹 복구
        rebuildCurrentPeriodRankings()

        log.info("=== Redis 랭킹 데이터 복구 완료 ===")
    }

    private fun rebuildAlltimeRanking() {
        val alltimeKey = buildAlltimeKey()
        val existingSize = redisTemplate.opsForZSet().size(alltimeKey) ?: 0L

        if (existingSize > 0) {
            log.info("전체 랭킹 이미 존재함: {}건", existingSize)
            return
        }

        val users = userRepository.findAll()
        var count = 0

        users.forEach { user ->
            if (user.totalPoint > 0) {
                redisTemplate.opsForZSet().add(alltimeKey, user.loginId, user.totalPoint.toDouble())
                count++
            }
        }

        log.info("전체 랭킹 복구 완료: {}건", count)
    }

    private fun rebuildCurrentPeriodRankings() {
        val weeklyKey = buildWeeklyKey()
        val monthlyKey = buildMonthlyKey()

        // 기존 데이터가 없는 경우에만 DB에서 복구
        val weeklySize = redisTemplate.opsForZSet().size(weeklyKey) ?: 0L
        val monthlySize = redisTemplate.opsForZSet().size(monthlyKey) ?: 0L

        if (weeklySize == 0L) {
            val weeklyRankings = userRankingRepository.findByPeriodTypeAndPeriodKeyOrderByScoreDesc(
                RankingPeriodType.WEEKLY, getCurrentWeekKey()
            )
            weeklyRankings.forEach { ranking ->
                redisTemplate.opsForZSet().add(weeklyKey, ranking.user.loginId, ranking.score.toDouble())
            }
            if (weeklyRankings.isNotEmpty()) {
                redisTemplate.expire(weeklyKey, WEEKLY_TTL)
            }
            log.info("주간 랭킹 복구 완료: {}건", weeklyRankings.size)
        }

        if (monthlySize == 0L) {
            val monthlyRankings = userRankingRepository.findByPeriodTypeAndPeriodKeyOrderByScoreDesc(
                RankingPeriodType.MONTHLY, getCurrentMonthKey()
            )
            monthlyRankings.forEach { ranking ->
                redisTemplate.opsForZSet().add(monthlyKey, ranking.user.loginId, ranking.score.toDouble())
            }
            if (monthlyRankings.isNotEmpty()) {
                redisTemplate.expire(monthlyKey, MONTHLY_TTL)
            }
            log.info("월간 랭킹 복구 완료: {}건", monthlyRankings.size)
        }
    }

    // ==================== Key Building Utilities ====================

    private fun buildRedisKey(periodType: RankingPeriodType, periodKey: String): String {
        return "${periodType.keyPrefix}:$periodKey"
    }

    private fun buildWeeklyKey(): String {
        return "${RankingPeriodType.WEEKLY.keyPrefix}:${getCurrentWeekKey()}"
    }

    private fun buildMonthlyKey(): String {
        return "${RankingPeriodType.MONTHLY.keyPrefix}:${getCurrentMonthKey()}"
    }

    private fun buildAlltimeKey(): String {
        return "${RankingPeriodType.ALLTIME.keyPrefix}:$ALLTIME_KEY"
    }

    /**
     * 현재 주차 키를 생성합니다.
     * 형식: "2026-W03" (ISO 주차 기준)
     */
    fun getCurrentWeekKey(): String {
        val now = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekNumber = now.get(weekFields.weekOfWeekBasedYear())
        val year = now.get(weekFields.weekBasedYear())
        return "$year-W${weekNumber.toString().padStart(2, '0')}"
    }

    /**
     * 현재 월 키를 생성합니다.
     * 형식: "2026-01"
     */
    fun getCurrentMonthKey(): String {
        val now = LocalDate.now()
        return "${now.year}-${now.monthValue.toString().padStart(2, '0')}"
    }
}
