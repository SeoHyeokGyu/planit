package com.planit.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisPublisher(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    /**
     * 지정된 채널로 메시지를 발행합니다.
     *
     * @param channel 발행할 채널 이름
     * @param message 발행할 메시지
     */
    fun publish(channel: String, message: Any) {
        redisTemplate.convertAndSend(channel, message)
    }
}
