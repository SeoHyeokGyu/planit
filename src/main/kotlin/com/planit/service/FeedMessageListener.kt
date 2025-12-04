package com.planit.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.planit.dto.FeedEvent
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component

/**
 * Redis Pub/Sub 메시지 리스너
 * 'feed-events' 채널에서 발행된 피드 이벤트를 수신하고,
 * 모든 연결된 SSE 클라이언트에게 브로드캐스트합니다.
 */
@Component
class FeedMessageListener(
    private val sseEmitterManager: SseEmitterManager,
    private val objectMapper: ObjectMapper
) : MessageListener {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Redis Pub/Sub 메시지 수신 시 호출됩니다.
     * @param message Redis에서 수신한 메시지
     * @param pattern 구독 패턴 (사용하지 않음)
     */
    override fun onMessage(message: Message, pattern: ByteArray?) {
        try {
            // 메시지 바이트 배열을 FeedEvent 객체로 역직렬화
            val feedEvent = objectMapper.readValue(
                message.body,
                FeedEvent::class.java
            )

            logger.info("Received feed event: type=${feedEvent.type}, user=${feedEvent.username} (id=${feedEvent.userId})")

            // 모든 연결된 SSE 클라이언트에게 이벤트 브로드캐스트
            sseEmitterManager.sendToAll(feedEvent)

            logger.debug("Successfully broadcasted feed event: ${feedEvent.id}")
        } catch (e: Exception) {
            logger.error("Failed to process feed message from Redis Pub/Sub", e)
        }
    }
}
