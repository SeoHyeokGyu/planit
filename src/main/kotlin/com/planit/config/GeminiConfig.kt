package com.planit.config

import com.google.genai.Client
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeminiConfig(@param:Value("\${gemini.api-key}") private val apiKey: String) {

  /** Gemini API 클라이언트를 스프링 빈으로 등록합니다. 'gemini.api-key' 프로퍼티 값을 사용하여 Client를 초기화합니다. */
  @Bean
  fun geminiClient(): Client {

    if (apiKey.isBlank()) {
      throw IllegalArgumentException("Gemini API 키가 설정되지 않았습니다.")
    }

    return Client.builder().apiKey(apiKey).build()
  }
}
