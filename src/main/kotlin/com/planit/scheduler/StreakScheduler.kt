package com.planit.scheduler

import com.planit.dto.StreakWarningNotification
import com.planit.repository.ChallengeRepository
import com.planit.repository.StreakRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class StreakScheduler(
    private val streakRepository: StreakRepository,
    private val challengeRepository: ChallengeRepository,
    // private val notificationService: NotificationService  // 알림 서비스 (추후 구현)
) {
    
    private val logger = LoggerFactory.getLogger(StreakScheduler::class.java)

    /**
     * 매일 자정 1분에 실행
     * 오늘 인증하지 않은 스트릭 초기화 및 경고 알림 발송
     */
    @Scheduled(cron = "0 1 0 * * *")  // 매일 00:01:00
    @Transactional
    fun resetExpiredStreaks() {
        logger.info("스트릭 초기화 스케줄러 시작")
        
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        // 어제까지 인증하지 않은 활성 스트릭 조회
        val expiredStreaks = streakRepository.findActiveStreaksNotCertifiedToday(today)
        
        logger.info("만료된 스트릭 ${expiredStreaks.size}개 발견")
        
        val warnings = mutableListOf<StreakWarningNotification>()
        
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
                    
                    // 경고 알림 생성
                    val warning = StreakWarningNotification(
                        loginId = streak.loginId,
                        challengeId = streak.challengeId,
                        challengeTitle = challenge.title,
                        currentStreak = 0,
                        lastCertificationDate = streak.lastCertificationDate,
                        message = "챌린지 '${challenge.title}'의 ${previousStreak}일 연속 기록이 끊어졌습니다. 오늘부터 다시 시작하세요!"
                    )
                    warnings.add(warning)
                }
            } catch (e: Exception) {
                logger.error("스트릭 초기화 실패: ${streak.challengeId}-${streak.loginId}", e)
            }
        }
        
        // 경고 알림 발송 (알림 서비스 구현 후 활성화)
        if (warnings.isNotEmpty()) {
            logger.info("스트릭 경고 알림 ${warnings.size}개 발송")
            // notificationService.sendBulkWarnings(warnings)
            // 임시: 로그만 출력
            warnings.forEach { warning ->
                logger.info("경고: ${warning.loginId} - ${warning.message}")
            }
        }
        
        logger.info("스트릭 초기화 스케줄러 완료 - ${expiredStreaks.size}개 초기화")
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
        
        val reminders = atRiskStreaks.map { streak ->
            val challenge = challengeRepository.findById(streak.challengeId)
                .orElseThrow { IllegalStateException("Challenge not found") }
            
            StreakWarningNotification(
                loginId = streak.loginId,
                challengeId = streak.challengeId,
                challengeTitle = challenge.title,
                currentStreak = streak.currentStreak,
                lastCertificationDate = streak.lastCertificationDate,
                message = "챌린지 '${challenge.title}'의 ${streak.currentStreak}일 연속 기록이 위험해요! 오늘 인증을 완료하세요."
            )
        }
        
        // 리마인더 발송 (알림 서비스 구현 후 활성화)
        if (reminders.isNotEmpty()) {
            logger.info("스트릭 리마인더 ${reminders.size}개 발송")
            // notificationService.sendBulkReminders(reminders)
            // 임시: 로그만 출력
            reminders.forEach { reminder ->
                logger.info("리마인더: ${reminder.loginId} - ${reminder.message}")
            }
        }
        
        logger.info("스트릭 리마인더 스케줄러 완료 - ${reminders.size}개 발송")
    }

    /**
     * 매주 월요일 오전 9시에 실행
     * 주간 스트릭 통계 발송
     */
    @Scheduled(cron = "0 0 9 * * MON")  // 매주 월요일 09:00:00
    @Transactional(readOnly = true)
    fun sendWeeklyStreakReport() {
        logger.info("주간 스트릭 리포트 스케줄러 시작")
        
        // 주간 리포트 로직 구현
        // 각 사용자의 지난 주 활동 요약 생성 및 발송
        
        logger.info("주간 스트릭 리포트 스케줄러 완료")
    }
}
