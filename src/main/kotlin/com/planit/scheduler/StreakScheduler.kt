package com.planit.scheduler

import com.planit.dto.NotificationResponse
import com.planit.enums.NotificationType
import com.planit.repository.ChallengeRepository
import com.planit.repository.StreakRepository
import com.planit.repository.UserRepository
import com.planit.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class StreakScheduler(
    private val streakRepository: StreakRepository,
    private val challengeRepository: ChallengeRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {

    private val logger = LoggerFactory.getLogger(StreakScheduler::class.java)

    /**
     * 매일 자정 1분에 실행
     * 오늘 인증하지 않은 스트릭 초기화 및 경고 알림 발송
     */
    @Scheduled(cron = "0 01 00 * * *")  // 매일 00:01:00
    @Transactional
    fun resetExpiredStreaks() {
        logger.info("스트릭 초기화 스케줄러 시작")

        val today = LocalDate.now()

        // 어제까지 인증하지 않은 활성 스트릭 조회
        val expiredStreaks = streakRepository.findActiveStreaksNotCertifiedToday(today)

        logger.info("만료된 스트릭 ${expiredStreaks.size}개 발견")

        var successCount = 0
        var failCount = 0

        expiredStreaks.forEach { streak ->
            try {
                val challenge = challengeRepository.findById(streak.challengeId).orElse(null)

                if (challenge != null && challenge.isActive()) {
                    // 스트릭 초기화
                    val previousStreak = streak.currentStreak
                    streak.resetStreak()
                    streakRepository.save(streak)

                    logger.debug(
                        "스트릭 초기화 완료 - user=${streak.loginId}, " +
                                "challenge=${streak.challengeId}, " +
                                "이전 스트릭=$previousStreak"
                    )

                    // 경고 알림 발송
                    val user = userRepository.findByLoginId(streak.loginId)
                    if (user != null) {
                        notificationService.sendNotification(
                            NotificationResponse(
                                id = java.util.UUID.randomUUID().toString(),
                                receiverId = user.id!!,
                                receiverLoginId = user.loginId,
                                senderId = null,  // 시스템 알림
                                senderLoginId = null,
                                senderNickname = null,
                                type = NotificationType.CHALLENGE,
                                message = "챌린지 '${challenge.title}'의 ${previousStreak}일 연속 기록이 끊어졌습니다. 오늘부터 다시 시작하세요! 💪",
                                relatedId = streak.challengeId,
                                relatedType = "CHALLENGE",
                                isRead = false,
                                createdAt = LocalDateTime.now()
                            )
                        )
                        successCount++
                    } else {
                        logger.warn("사용자를 찾을 수 없음: ${streak.loginId}")
                        failCount++
                    }
                }
            } catch (e: Exception) {
                logger.error("스트릭 초기화 실패: ${streak.challengeId}-${streak.loginId}", e)
                failCount++
            }
        }

        logger.info("스트릭 초기화 스케줄러 완료 - ${expiredStreaks.size}개 초기화, 성공: $successCount, 실패: $failCount")
    }

    /**
     * 매일 저녁 8시에 실행
     * 오늘 인증하지 않은 사용자에게 리마인더 발송
     */
    @Scheduled(cron = "0 0 20 * * *")  // 매일 20:00:00
    @Transactional(readOnly = true)
    fun sendStreakReminders() {
        logger.info("스트릭 리마인더 스케줄러 시작")

        val today = LocalDate.now()

        // 오늘 인증하지 않은 활성 스트릭 조회
        val atRiskStreaks = streakRepository.findActiveStreaksNotCertifiedToday(today)
            .filter { streak ->
                val challenge = challengeRepository.findById(streak.challengeId).orElse(null)
                challenge?.isActive() == true
            }

        logger.info("위험 상태 스트릭 ${atRiskStreaks.size}개 발견")

        var successCount = 0
        var failCount = 0

        atRiskStreaks.forEach { streak ->
            try {
                val challenge = challengeRepository.findById(streak.challengeId).orElse(null)
                val user = userRepository.findByLoginId(streak.loginId)

                if (challenge != null && user != null) {
                    notificationService.sendNotification(
                        NotificationResponse(
                            id = java.util.UUID.randomUUID().toString(),
                            receiverId = user.id!!,
                            receiverLoginId = user.loginId,
                            senderId = null,  // 시스템 알림
                            senderLoginId = null,
                            senderNickname = null,
                            type = NotificationType.CHALLENGE,
                            message = "챌린지 '${challenge.title}'의 ${streak.currentStreak}일 연속 기록이 위험해요! 오늘 인증을 완료하세요. 🔥",
                            relatedId = streak.challengeId,
                            relatedType = "CHALLENGE",
                            isRead = false,
                            createdAt = LocalDateTime.now()
                        )
                    )
                    successCount++
                    logger.debug("리마인더 발송 완료: ${streak.loginId} - ${challenge.title}")
                } else {
                    logger.warn("챌린지 또는 사용자를 찾을 수 없음: challenge=${streak.challengeId}, user=${streak.loginId}")
                    failCount++
                }
            } catch (e: Exception) {
                logger.error("리마인더 발송 실패: ${streak.challengeId}-${streak.loginId}", e)
                failCount++
            }
        }

        logger.info("스트릭 리마인더 스케줄러 완료 - 성공: $successCount, 실패: $failCount")
    }

    /**
     * 매주 월요일 오전 9시에 실행
     * 주간 스트릭 통계 발송
     */
    @Scheduled(cron = "0 0 9 * * MON")  // 매주 월요일 09:00:00
    @Transactional(readOnly = true)
    fun sendWeeklyStreakReport() {
        logger.info("주간 스트릭 리포트 스케줄러 시작")

        val today = LocalDate.now()
        val oneWeekAgo = today.minusWeeks(1)

        // 활성 스트릭이 있는 모든 사용자 조회
        val activeUserStreaks = streakRepository.findAll()
            .filter { it.currentStreak > 0 || it.longestStreak > 0 }
            .groupBy { it.loginId }

        logger.info("주간 리포트 대상 사용자: ${activeUserStreaks.size}명")

        var successCount = 0
        var failCount = 0

        activeUserStreaks.forEach { (loginId, streaks) ->
            try {
                val user = userRepository.findByLoginId(loginId)

                if (user != null) {
                    // 사용자의 활성 스트릭 수와 최고 기록 계산
                    val activeStreakCount = streaks.count { it.currentStreak > 0 }
                    val totalCurrentStreak = streaks.sumOf { it.currentStreak }
                    val maxLongestStreak = streaks.maxOfOrNull { it.longestStreak } ?: 0

                    // 의미 있는 활동이 있는 경우에만 알림 발송
                    if (activeStreakCount > 0 || maxLongestStreak > 0) {
                        val message = buildWeeklyReportMessage(
                            activeStreakCount = activeStreakCount,
                            totalCurrentStreak = totalCurrentStreak,
                            maxLongestStreak = maxLongestStreak
                        )

                        notificationService.sendNotification(
                            NotificationResponse(
                                id = java.util.UUID.randomUUID().toString(),
                                receiverId = user.id!!,
                                receiverLoginId = user.loginId,
                                senderId = null,  // 시스템 알림
                                senderLoginId = null,
                                senderNickname = null,
                                type = NotificationType.STREAK,
                                message = message,
                                relatedId = null,
                                relatedType = "WEEKLY_REPORT",
                                isRead = false,
                                createdAt = LocalDateTime.now()
                            )
                        )
                        successCount++
                    }
                } else {
                    logger.warn("사용자를 찾을 수 없음: $loginId")
                    failCount++
                }
            } catch (e: Exception) {
                logger.error("주간 리포트 발송 실패: $loginId", e)
                failCount++
            }
        }

        logger.info("주간 스트릭 리포트 스케줄러 완료 - 성공: $successCount, 실패: $failCount")
    }

    /**
     * 주간 리포트 메시지 생성
     */
    private fun buildWeeklyReportMessage(
        activeStreakCount: Int,
        totalCurrentStreak: Int,
        maxLongestStreak: Int
    ): String {
        val parts = mutableListOf<String>()

        parts.add("📊 이번 주 활동 리포트")

        if (activeStreakCount > 0) {
            parts.add("현재 ${activeStreakCount}개 챌린지에서 연속 기록 유지 중!")
            parts.add("누적 연속일: ${totalCurrentStreak}일")
        }

        if (maxLongestStreak > 0) {
            parts.add("최고 기록: ${maxLongestStreak}일 연속 🏆")
        }

        parts.add("이번 주도 화이팅! 💪")

        return parts.joinToString(" ")
    }
}