package com.planit.service

import com.planit.dto.NotificationDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Service
class NotificationService {

    private val logger = LoggerFactory.getLogger(NotificationService::class.java)
    // 사용자 ID (loginId)를 키로 사용하여 SseEmitter 관리
    private val emitters = ConcurrentHashMap<String, SseEmitter>()

    /**
     * 클라이언트가 SSE 구독을 요청할 때 호출됩니다.
     */
    fun subscribe(userLoginId: String): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE) // 타임아웃 무제한 (또는 적절한 값 설정)
        emitters[userLoginId] = emitter

        logger.info("SSE 구독 시작: $userLoginId")

        // 연결 종료 시 처리
        emitter.onCompletion {
            logger.info("SSE 구독 완료: $userLoginId")
            emitters.remove(userLoginId)
        }
        emitter.onTimeout {
            logger.info("SSE 구독 타임아웃: $userLoginId")
            emitters.remove(userLoginId)
        }
        emitter.onError {
            logger.error("SSE 구독 에러: $userLoginId", it)
            emitters.remove(userLoginId)
        }

        // 연결 수립 시 더미 이벤트 전송 (연결 확인용)
        try {
            emitter.send(SseEmitter.event().name("connect").data("연결 성공!"))
        } catch (e: IOException) {
            emitters.remove(userLoginId)
        }

        return emitter
    }

    /**
     * 특정 사용자에게 알림을 전송합니다.
     */
    fun sendNotification(userLoginId: String, notification: NotificationDto) {
        val emitter = emitters[userLoginId]
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification))
                logger.info("알림 전송 완료: $userLoginId")
            } catch (e: IOException) {
                logger.error("알림 전송 실패: $userLoginId", e)
                emitters.remove(userLoginId)
            }
        } else {
            // 사용자가 현재 접속해 있지 않음 (추후 DB에 저장하여 오프라인 알림 처리 가능)
            logger.debug("사용자 $userLoginId 접속 중이 아님 (SSE)")
        }
    }
}
