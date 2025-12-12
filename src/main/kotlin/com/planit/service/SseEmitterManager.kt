package com.planit.service

import com.planit.dto.FeedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

/**
 * SSE (Server-Sent Events) 연결을 관리하는 매니저 클래스
 * 사용자별 SSE 연결을 저장하고, 실시간 이벤트를 전송합니다.
 */
@Component
class SseEmitterManager {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 사용자 ID별 SSE Emitter 저장소
     * Thread-safe한 ConcurrentHashMap 사용
     */
    private val emitters = ConcurrentHashMap<Long, SseEmitter>()

    companion object {
        /**
         * SSE 연결 타임아웃 (1시간)
         * 클라이언트 연결이 1시간 동안 활동이 없으면 자동으로 종료됩니다.
         */
        private const val TIMEOUT = 60 * 60 * 1000L // 1시간
    }

    /**
     * 새로운 SSE Emitter를 생성하고 사용자와 연결합니다.
     * 기존 연결이 있으면 제거하고 새로운 연결을 생성합니다.
     * @param userId 연결할 사용자 ID
     * @return 생성된 SseEmitter
     */
    fun addEmitter(userId: Long): SseEmitter {
        val emitter = SseEmitter(TIMEOUT)

        // 기존 연결이 있다면 제거 (중복 연결 방지)
        emitters[userId]?.complete()
        emitters[userId] = emitter

        // 연결 완료 시 콜백
        emitter.onCompletion {
            logger.info("SSE connection completed for user: $userId")
            emitters.remove(userId)
        }

        // 연결 타임아웃 시 콜백
        emitter.onTimeout {
            logger.info("SSE connection timeout for user: $userId")
            emitters.remove(userId)
        }

        // 연결 에러 시 콜백
        emitter.onError { throwable ->
            logger.error("SSE error for user: $userId", throwable)
            emitters.remove(userId)
        }

        logger.info("SSE emitter added for user: $userId (Total connections: ${emitters.size})")
        return emitter
    }

    /**
     * 특정 사용자에게 피드 이벤트를 전송합니다.
     * @param userId 이벤트를 받을 사용자 ID
     * @param event 전송할 피드 이벤트
     */
    fun sendToUser(userId: Long, event: FeedEvent) {
        emitters[userId]?.let { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("feed")
                        .data(event)
                )
                logger.debug("Sent event to user: $userId")
            } catch (e: Exception) {
                logger.error("Failed to send event to user: $userId", e)
                emitters.remove(userId)
            }
        } ?: run {
            logger.debug("No active connection for user: $userId")
        }
    }

    /**
     * 모든 연결된 사용자에게 피드 이벤트를 브로드캐스트합니다.
     * @param event 전송할 피드 이벤트
     */
    fun sendToAll(event: FeedEvent) {
        logger.info("Broadcasting event to ${emitters.size} connected users")
        val failedUsers = mutableListOf<Long>()

        emitters.forEach { (userId, emitter) ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("feed")
                        .data(event)
                )
            } catch (e: Exception) {
                logger.error("Failed to send event to user: $userId", e)
                failedUsers.add(userId)
            }
        }

        // 전송 실패한 연결 제거
        failedUsers.forEach { userId ->
            emitters.remove(userId)
        }

        if (failedUsers.isNotEmpty()) {
            logger.warn("Removed ${failedUsers.size} failed connections")
        }
    }

    /**
     * 현재 연결된 모든 사용자 ID 목록을 반환합니다.
     * @return 연결된 사용자 ID 집합
     */
    fun getConnectedUsers(): Set<Long> = emitters.keys.toSet()

    /**
     * 특정 사용자가 연결되어 있는지 확인합니다.
     * @param userId 확인할 사용자 ID
     * @return 연결 여부
     */
    fun isConnected(userId: Long): Boolean = emitters.containsKey(userId)

    /**
     * 특정 사용자의 SSE 연결을 제거합니다.
     * @param userId 제거할 사용자 ID
     */
    fun removeEmitter(userId: Long) {
        emitters.remove(userId)?.complete()
        logger.info("Removed emitter for user: $userId (Remaining connections: ${emitters.size})")
    }

    /**
     * 현재 활성 연결 수를 반환합니다.
     * @return 연결 수
     */
    fun getConnectionCount(): Int = emitters.size
}
