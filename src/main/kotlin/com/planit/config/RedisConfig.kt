package com.planit.config

import com.planit.dto.FeedEvent
import com.planit.service.FeedMessageListener
import java.time.Duration
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * Redis 관련 설정을 정의하는 Configuration 클래스입니다.
 * RedisTemplate, Redis CacheManager, 그리고 Redis Pub/Sub을 구성합니다.
 */
@Configuration
@EnableCaching // Spring의 캐싱 기능을 활성화합니다.
class RedisConfig {

    /**
     * Redis 데이터 작업을 위한 RedisTemplate을 구성합니다.
     * key는 String, value는 JSON 형식으로 직렬화하여 저장합니다.
     * @param connectionFactory Redis 연결 팩토리
     * @return 구성된 RedisTemplate
     */
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer() // Key 직렬화
            valueSerializer = GenericJackson2JsonRedisSerializer() // Value 직렬화 (JSON)
            hashKeySerializer = StringRedisSerializer() // Hash Key 직렬화
            hashValueSerializer = GenericJackson2JsonRedisSerializer() // Hash Value 직렬화 (JSON)
        }
    }

    /**
     * 피드 이벤트 전용 RedisTemplate을 구성합니다.
     * SSE를 통한 실시간 피드 브로드캐스트에 사용됩니다.
     * @param connectionFactory Redis 연결 팩토리
     * @return FeedEvent 타입 전용 RedisTemplate
     */
    @Bean
    fun feedEventTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, FeedEvent> {
        return RedisTemplate<String, FeedEvent>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = Jackson2JsonRedisSerializer(FeedEvent::class.java)
        }
    }

    /**
     * Redis Pub/Sub 메시지 리스너 컨테이너를 구성합니다.
     * 'feed-events' 채널을 구독하여 피드 이벤트를 수신합니다.
     * @param connectionFactory Redis 연결 팩토리
     * @param feedMessageListener 피드 메시지를 처리할 리스너
     * @return 구성된 RedisMessageListenerContainer
     */
    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        feedMessageListener: FeedMessageListener
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(
            feedMessageListener,
            ChannelTopic("feed-events")
        )
        return container
    }

    /**
     * Spring Cache 추상화를 위한 RedisCacheManager를 구성합니다.
     * 특정 캐시(`followerCount`, `followingCount`)에 대한 TTL을 설정합니다.
     * @param connectionFactory Redis 연결 팩토리
     * @return 구성된 CacheManager
     */
    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        // 기본 캐시 설정
        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())) // Key 직렬화
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer())) // Value 직렬화
            .entryTtl(Duration.ofMinutes(10)) // 캐시 엔트리의 기본 TTL (10분)

        // 특정 캐시별 설정 (여기서는 모두 기본 TTL 사용)
        val cacheConfigurations = mapOf(
            "followerCount" to redisCacheConfiguration,
            "followingCount" to redisCacheConfiguration
        )

        // RedisCacheManager 빌더를 통해 CacheManager 생성
        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory)
            .withInitialCacheConfigurations(cacheConfigurations) // 초기 캐시 설정 적용
            .build()
    }
}
