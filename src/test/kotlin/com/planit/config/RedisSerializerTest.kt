package com.planit.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.planit.dto.NotificationResponse
import com.planit.enums.NotificationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class RedisSerializerTest {

    @Test
    fun `GenericJackson2JsonRedisSerializer should serialize LocalDateTime correctly`() {
        // Given
        val objectMapper = ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            // DefaultTyping 설정을 GenericJackson2JsonRedisSerializer 내부와 유사하게 맞춤
            activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
            )
        }
        val serializer = GenericJackson2JsonRedisSerializer(objectMapper)

        // LocalDateTime의 나노초 정밀도 차이로 인한 테스트 실패 방지를 위해 초 단위까지만 맞춤
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val notification = NotificationResponse(
            id = 1L,
            receiverId = 1L,
            receiverLoginId = "receiver",
            senderId = 2L,
            senderLoginId = "sender",
            senderNickname = "Sender",
            type = NotificationType.LIKE,
            message = "Test Message",
            relatedId = "100",
            relatedType = "CERTIFICATION",
            isRead = false,
            createdAt = now
        )

        // When
        val bytes = serializer.serialize(notification)
        println("Serialized JSON: ${String(bytes!!)}")
        val result = serializer.deserialize(bytes)

        // Then
        assertThat(result).isNotNull
        assertThat(result).isInstanceOf(NotificationResponse::class.java)
        val deserialized = result as NotificationResponse
        assertThat(deserialized.message).isEqualTo(notification.message)
        assertThat(deserialized.createdAt).isEqualTo(notification.createdAt)
    }
}