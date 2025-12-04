package com.planit.config

import com.planit.config.InstanceIdProvider
import com.planit.service.FeedForwardingListener
import com.planit.service.RedisSubscriber
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * Redis 관련 설정을 정의하는 Configuration 클래스입니다.
 * Pub/Sub 리스너, RedisTemplate, CacheManager를 구성합니다.
 */
@Configuration
@EnableCaching
class RedisConfig {

    companion object {
        const val GLOBAL_FEED_CHANNEL = "feed:global-events"
        const val INSTANCE_FEED_CHANNEL_PREFIX = "feed:instance-events:"
    }

    /**
     * Redis 데이터 작업을 위한 RedisTemplate을 구성합니다.
     */
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = GenericJackson2JsonRedisSerializer()
        }
    }

    /**
     * [전역 채널용] 새로운 인증 이벤트를 수신하는 리스너 컨테이너입니다.
     * 모든 인스턴스가 이 채널을 구독하며, 이벤트를 수신한 하나의 인스턴스가 코디네이터가 됩니다.
     */
    @Bean
    fun globalListenerContainer(
        connectionFactory: RedisConnectionFactory,
        globalListenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            addMessageListener(globalListenerAdapter, ChannelTopic(GLOBAL_FEED_CHANNEL))
        }
    }

    /**
     * [전역 채널용] RedisSubscriber를 MessageListener로 변환합니다.
     */
    @Bean
    fun globalListenerAdapter(subscriber: RedisSubscriber): MessageListenerAdapter {
        return MessageListenerAdapter(subscriber, "onMessage")
    }

    /**
     * [인스턴스 채널용] 특정 인스턴스로 전달된 이벤트를 수신하는 리스너 컨테이너입니다.
     * 각 인스턴스는 자신의 고유 채널만 구독합니다.
     */
    @Bean
    fun instanceListenerContainer(
        connectionFactory: RedisConnectionFactory,
        instanceIdProvider: InstanceIdProvider,
        forwardingListenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {
        val channelName = INSTANCE_FEED_CHANNEL_PREFIX + instanceIdProvider.id
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            addMessageListener(forwardingListenerAdapter, ChannelTopic(channelName))
        }
    }

    /**
     * [인스턴스 채널용] FeedForwardingListener를 MessageListener로 변환합니다.
     */
    @Bean
    fun forwardingListenerAdapter(listener: FeedForwardingListener): MessageListenerAdapter {
        // FeedForwardingListener에 기본 메시지 처리 메서드인 onMessage를 사용합니다.
        return MessageListenerAdapter(listener, "onMessage")
    }

    /**
     * Spring Cache 추상화를 위한 RedisCacheManager를 구성합니다.
     */
    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer()
                )
            )
            .entryTtl(Duration.ofMinutes(10))

        val cacheConfigurations = mapOf(
            "followerCount" to redisCacheConfiguration,
            "followingCount" to redisCacheConfiguration
        )

        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}