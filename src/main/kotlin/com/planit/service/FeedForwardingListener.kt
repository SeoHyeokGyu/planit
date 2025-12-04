package com.planit.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.planit.dto.ForwardedFeedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Service

/**
 * 인스턴스 전용 채널에서 전달된(forwarded) 피드 이벤트를 수신하여
 * 해당 인스턴스에 연결된 클라이언트에게 최종적으로 SSE 이벤트를 발송합니다.
 */
@Service
class FeedForwardingListener(
    private val objectMapper: ObjectMapper,
    private val feedService: FeedService
) : MessageListener {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun onMessage(message: Message, pattern: ByteArray?) {
        try {
            val forwardedEvent = objectMapper.readValue(message.body, ForwardedFeedEvent::class.java)
            log.info("인스턴스 채널에서 사용자 '${forwardedEvent.loginId}'에 대한 전달된 이벤트를 수신했습니다.")
            feedService.send(forwardedEvent.loginId, forwardedEvent.feedEvent)
        } catch (e: Exception) {
            log.error("전달된 피드 이벤트를 처리하는 데 실패했습니다: ${message.body}", e)
        }
    }
}
