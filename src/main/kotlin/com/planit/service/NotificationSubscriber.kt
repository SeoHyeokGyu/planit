package com.planit.service

import com.planit.dto.NotificationResponse
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.stereotype.Service

/** Redis Pub/Sub 채널을 구독하여 실시간 알림을 처리하는 리스너입니다. */
@Service
class NotificationSubscriber(
    private val notificationService: NotificationService,
    private val redisSerializer: GenericJackson2JsonRedisSerializer,
) : MessageListener {

  private val logger = LoggerFactory.getLogger(NotificationSubscriber::class.java)

  /** Redis 메시지를 수신했을 때 호출됩니다. */
  override fun onMessage(message: Message, pattern: ByteArray?) {
    try {
      // RedisConfig에서 설정된 serializer(JavaTimeModule 포함)를 사용하여 역직렬화
      val notification = redisSerializer.deserialize(message.body) as NotificationResponse

      logger.info("Redis 알림 수신: ${notification.receiverLoginId}")

      // 로컬에 연결된 SSE Emitter가 있는지 확인하고 전송
      notificationService.sendToLocalEmitter(notification.receiverLoginId, notification)
    } catch (e: Exception) {
      logger.error("Redis 메시지 처리 실패", e)
    }
  }
}
