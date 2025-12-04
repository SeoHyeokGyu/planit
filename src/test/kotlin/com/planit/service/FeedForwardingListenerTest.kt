package com.planit.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.planit.dto.FeedEvent
import com.planit.dto.ForwardedFeedEvent
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.Message

@DisplayName("FeedForwardingListener 테스트")
class FeedForwardingListenerTest {

    private lateinit var feedService: FeedService
    private lateinit var objectMapper: ObjectMapper
    private lateinit var feedForwardingListener: FeedForwardingListener

    @BeforeEach
    fun setUp() {
        feedService = mockk<FeedService>(relaxed = true)
        // Use a real ObjectMapper for this test to ensure serialization/deserialization works
        objectMapper = jacksonObjectMapper()
        feedForwardingListener = FeedForwardingListener(objectMapper, feedService)
    }

    @Nested
    @DisplayName("onMessage 메서드는")
    inner class DescribeOnMessage {

        @Test
        @DisplayName("전달된 이벤트를 받으면 FeedService.send를 호출한다")
        fun `calls feedService send when it receives a forwarded event`() {
            // Given
            val feedEvent = FeedEvent(type = "new-certification", data = mapOf("id" to 1L))
            val forwardedEvent = ForwardedFeedEvent(loginId = "123", feedEvent = feedEvent)
            val json = objectMapper.writeValueAsBytes(forwardedEvent)
            val message = mockk<Message>()

            every { message.body } returns json
            every { feedService.send(any(), any()) } just runs

            // When
            feedForwardingListener.onMessage(message, null)

            // Then
            verify { feedService.send("123", feedEvent) }
        }

        @Test
        @DisplayName("메시지 역직렬화에 실패하면 아무것도 하지 않는다")
        fun `does nothing if message deserialization fails`() {
            // Given
            val invalidJson = "{]".toByteArray()
            val message = mockk<Message>()

            every { message.body } returns invalidJson

            // When
            feedForwardingListener.onMessage(message, null)

            // Then
            verify(exactly = 0) { feedService.send(any(), any()) }
        }
    }
}
