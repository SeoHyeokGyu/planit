package com.planit.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.planit.service.NotificationSubscriber
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
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

/** Redis 관련 설정을 정의하는 Configuration 클래스입니다. RedisTemplate과 Redis CacheManager을 구성합니다. */
@Configuration
@EnableCaching // Spring의 캐싱 기능을 활성화합니다.
class RedisConfig {

  companion object {
    const val NOTIFICATION_CHANNEL = "planit:notifications"
  }

  /**
   * Redis 직렬화를 위한 Serializer 빈 생성
   * JavaTimeModule 등록으로 LocalDateTime 직렬화 지원
   */
  @Bean
  fun redisSerializer(): GenericJackson2JsonRedisSerializer {
    val objectMapper = ObjectMapper().apply {
      registerModule(JavaTimeModule())
      registerModule(KotlinModule.Builder().build())
      disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      activateDefaultTyping(
          LaissezFaireSubTypeValidator.instance,
          ObjectMapper.DefaultTyping.EVERYTHING,
          JsonTypeInfo.As.PROPERTY
      )
    }
    return GenericJackson2JsonRedisSerializer(objectMapper)
  }

  /**
   * Redis 데이터 작업을 위한 RedisTemplate을 구성합니다. key는 String, value는 JSON 형식으로 직렬화하여 저장합니다.
   *
   * @param connectionFactory Redis 연결 팩토리
   * @return 구성된 RedisTemplate
   */
  @Bean
  fun redisTemplate(connectionFactory: RedisConnectionFactory, redisSerializer: GenericJackson2JsonRedisSerializer): RedisTemplate<String, Any> {
    return RedisTemplate<String, Any>().apply {
      setConnectionFactory(connectionFactory)
      keySerializer = StringRedisSerializer() // Key 직렬화
      valueSerializer = redisSerializer // Value 직렬화 (JSON + JavaTime)
      hashKeySerializer = StringRedisSerializer() // Hash Key 직렬화
      hashValueSerializer = redisSerializer // Hash Value 직렬화 (JSON + JavaTime)
    }
  }

  /** Redis 메시지 리스너 컨테이너를 설정합니다. */
  @Bean
  fun redisMessageListenerContainer(
      connectionFactory: RedisConnectionFactory,
      listenerAdapter: MessageListenerAdapter,
  ): RedisMessageListenerContainer {
    return RedisMessageListenerContainer().apply {
      setConnectionFactory(connectionFactory)
      addMessageListener(listenerAdapter, ChannelTopic(NOTIFICATION_CHANNEL))
    }
  }

  /** 메시지 리스너 어댑터를 설정합니다. */
  @Bean
  fun listenerAdapter(notificationSubscriber: NotificationSubscriber): MessageListenerAdapter {
    return MessageListenerAdapter(notificationSubscriber, "onMessage")
  }

  /**
   * Spring Cache 추상화를 위한 RedisCacheManager를 구성합니다. 특정 캐시(`followerCount`, `followingCount`)에 대한
   * TTL을 설정합니다.
   *
   * @param connectionFactory Redis 연결 팩토리
   * @return 구성된 CacheManager
   */
  @Bean
  fun cacheManager(connectionFactory: RedisConnectionFactory, redisSerializer: GenericJackson2JsonRedisSerializer): CacheManager {
    // 기본 캐시 설정
    val redisCacheConfiguration =
        RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            ) // Key 직렬화
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    redisSerializer
                )
            ) // Value 직렬화
            .entryTtl(Duration.ofMinutes(10)) // 캐시 엔트리의 기본 TTL (10분)

    // 특정 캐시별 설정 (여기서는 모두 기본 TTL 사용)
    val cacheConfigurations =
        mapOf(
            "followerCount" to redisCacheConfiguration,
            "followingCount" to redisCacheConfiguration,
        )

    // RedisCacheManager 빌더를 통해 CacheManager 생성
    return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory)
        .withInitialCacheConfigurations(cacheConfigurations) // 초기 캐시 설정 적용
        .build()
  }
}
