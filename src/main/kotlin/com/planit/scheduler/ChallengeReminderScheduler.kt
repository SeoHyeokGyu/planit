package com.planit.scheduler

import com.planit.dto.NotificationResponse
import com.planit.enums.NotificationType
import com.planit.enums.ParticipantStatusEnum
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.ChallengeRepository
import com.planit.repository.UserRepository
import com.planit.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class ChallengeReminderScheduler(
    private val challengeRepository: ChallengeRepository,
    private val participantRepository: ChallengeParticipantRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(ChallengeReminderScheduler::class.java)

    /**
     * 매일 오전 9시 실행 - 종료 3일 전 알림
     */
    @Scheduled(cron = "0 40 20 * * *")
    @Transactional(readOnly = true)
    fun sendThreeDaysBeforeReminder() {
        logger.info("챌린지 종료 3일 전 리마인드 시작")

        val today = LocalDate.now()
        val threeDaysLater = today.plusDays(3)

        val challenges = challengeRepository.findByEndDateOn(threeDaysLater)

        logger.info("3일 전 리마인드 대상 챌린지 ${challenges.size}개 발견")

        challenges.forEach { challenge ->
            try {
                sendReminderToParticipants(
                    challengeId = challenge.id,
                    challengeTitle = challenge.title,
                    creatorId = challenge.createdId,
                    message = "'${challenge.title}' 챌린지가 3일 후 종료됩니다. 조금만 더 힘내세요! 🔥"
                )

                logger.info("챌린지 '${challenge.title}' 3일 전 리마인드 발송 완료")
            } catch (e: Exception) {
                logger.error("챌린지 ${challenge.id} 3일 전 리마인드 발송 실패", e)
            }
        }

        logger.info("챌린지 종료 3일 전 리마인드 완료")
    }

    /**
     * 매일 오전 9시 실행 - 종료 1주일 전 알림
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    fun sendOneWeekBeforeReminder() {
        logger.info("챌린지 종료 1주일 전 리마인드 시작")

        val today = LocalDate.now()
        val oneWeekLater = today.plusDays(7)

        val challenges = challengeRepository.findByEndDateOn(oneWeekLater)

        logger.info("1주일 전 리마인드 대상 챌린지 ${challenges.size}개 발견")

        challenges.forEach { challenge ->
            try {
                sendReminderToParticipants(
                    challengeId = challenge.id,
                    challengeTitle = challenge.title,
                    creatorId = challenge.createdId,
                    message = "'${challenge.title}' 챌린지가 일주일 후 종료됩니다. 마지막 스퍼트! 💪"
                )

                logger.info("챌린지 '${challenge.title}' 1주일 전 리마인드 발송 완료")
            } catch (e: Exception) {
                logger.error("챌린지 ${challenge.id} 1주일 전 리마인드 발송 실패", e)
            }
        }

        logger.info("챌린지 종료 1주일 전 리마인드 완료")
    }

    /**
     * 매일 오전 9시 실행 - 종료 당일 알림
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    fun sendEndDayReminder() {
        logger.info("챌린지 종료 당일 리마인드 시작")

        val today = LocalDate.now()
        val challenges = challengeRepository.findByEndDateOn(today)

        logger.info("종료 당일 리마인드 대상 챌린지 ${challenges.size}개 발견")

        challenges.forEach { challenge ->
            try {
                sendReminderToParticipants(
                    challengeId = challenge.id,
                    challengeTitle = challenge.title,
                    creatorId = challenge.createdId,
                    message = "'${challenge.title}' 챌린지가 오늘 종료됩니다! 마지막 인증을 완료하세요! 🎯"
                )

                logger.info("챌린지 '${challenge.title}' 종료 당일 리마인드 발송 완료")
            } catch (e: Exception) {
                logger.error("챌린지 ${challenge.id} 종료 당일 리마인드 발송 실패", e)
            }
        }

        logger.info("챌린지 종료 당일 리마인드 완료")
    }

    /**
     * 챌린지 참여자들(생성자 포함)에게 리마인드 알림 발송
     */
    private fun sendReminderToParticipants(
        challengeId: String,
        challengeTitle: String,
        creatorId: String,
        message: String
    ) {
        // Repository를 통해 참여자 조회 (ACTIVE 상태만)
        val activeParticipants = participantRepository.findByIdAndStatus(
            challengeId,
            ParticipantStatusEnum.ACTIVE
        )

        // 생성자 + 참여자 모두에게 발송하기 위해 Set 사용 (중복 제거)
        val allUserLoginIds = mutableSetOf<String>()
        allUserLoginIds.add(creatorId)  // 생성자 추가
        activeParticipants.forEach { allUserLoginIds.add(it.loginId) }  // 참여자들 추가

        var successCount = 0
        var failCount = 0

        allUserLoginIds.forEach { loginId ->
            try {
                val user = userRepository.findByLoginId(loginId)

                if (user != null) {
                    // SSE 실시간 알림만 발송 (DB 저장 X)
                    notificationService.sendNotification(
                        NotificationResponse(
                            id = -1L,  // DB에 저장하지 않으므로 임시 ID
                            receiverId = user.id!!,
                            receiverLoginId = user.loginId,
                            senderId = null,  // 시스템 알림
                            senderLoginId = null,
                            senderNickname = null,
                            type = NotificationType.CHALLENGE,
                            message = message,
                            relatedId = challengeId,
                            relatedType = "CHALLENGE",
                            isRead = false,
                            createdAt = LocalDateTime.now()
                        )
                    )
                    successCount++
                } else {
                    logger.warn("사용자를 찾을 수 없음: $loginId")
                    failCount++
                }
            } catch (e: Exception) {
                logger.error("사용자 $loginId 에게 알림 발송 실패", e)
                failCount++
            }
        }

        logger.info("챌린지 '$challengeTitle' 알림 발송 완료 - 성공: $successCount, 실패: $failCount")
    }
}