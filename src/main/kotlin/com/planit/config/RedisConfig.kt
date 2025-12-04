package com.planit.config

import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RedisConfig {

  companion object {
    const val GLOBAL_FEED_CHANNEL = "feed:global-events"
    const val INSTANCE_FEED_CHANNEL_PREFIX = "feed:instance-events:"
  }

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
