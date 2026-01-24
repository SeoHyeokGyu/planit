package com.planit.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.Part
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files

@Service
class GeminiService(
  private val client: Client,
  @Value("\${gemini.models}") private val models: List<String>,
  private val objectMapper: ObjectMapper,
) {
  private val logger = LoggerFactory.getLogger(GeminiService::class.java)

  fun analyzeImage(prompt: String, image: MultipartFile): String {
    return analyzeImageBytes(prompt, image.bytes, image.contentType ?: "image/jpeg")
  }

  fun <T> analyzeImage(prompt: String, image: MultipartFile, clazz: Class<T>): T {
    val response = analyzeImage(prompt, image)
    return parseResponse(response, clazz)
  }

  fun <T> analyzeImage(prompt: String, image: MultipartFile, typeReference: TypeReference<T>): T {
    val response = analyzeImage(prompt, image)
    return parseResponse(response, typeReference)
  }

  fun analyzeImage(prompt: String, image: File): String {
    val bytes = Files.readAllBytes(image.toPath())
    // 간단한 mime type 추론 (확장자 기반)
    val contentType = when (image.extension.lowercase()) {
      "png" -> "image/png"
      "gif" -> "image/gif"
      "webp" -> "image/webp"
      else -> "image/jpeg"
    }
    return analyzeImageBytes(prompt, bytes, contentType)
  }

  fun <T> analyzeImage(prompt: String, image: File, clazz: Class<T>): T {
    val response = analyzeImage(prompt, image)
    return parseResponse(response, clazz)
  }

  fun <T> analyzeImage(prompt: String, image: File, typeReference: TypeReference<T>): T {
    val response = analyzeImage(prompt, image)
    return parseResponse(response, typeReference)
  }

  private fun analyzeImageBytes(prompt: String, bytes: ByteArray, mimeType: String): String {
    val imagePart = Part.fromBytes(bytes, mimeType)
    val textPart = Part.fromText(prompt)

    // builder 이슈를 피하기 위해 Content.fromParts를 사용합니다.
    val content = Content.fromParts(textPart, imagePart)

    return executeWithModelFallback { modelId ->
      client.models.generateContent(modelId, content, null).text()
    } ?: "분석 결과가 없습니다."
  }

  fun generateContent(prompt: String): String {
    return executeWithModelFallback { modelId ->
      client.models.generateContent(modelId, prompt, null).text()
    } ?: "추천 결과를 생성할 수 없습니다."
  }

  fun <T> generateContent(prompt: String, clazz: Class<T>): T {
    val response = generateContent(prompt)
    return parseResponse(response, clazz)
  }

  fun <T> generateContent(prompt: String, typeReference: TypeReference<T>): T {
    val response = generateContent(prompt)
    return parseResponse(response, typeReference)
  }

  private fun <T> parseResponse(response: String, clazz: Class<T>): T {
    return try {
      val jsonString = cleanJson(response)
      objectMapper.readValue(jsonString, clazz)
    } catch (e: Exception) {
      logger.error("Gemini 응답 파싱 실패. 원본 응답: $response", e)
      throw RuntimeException("AI 응답을 처리하는 중 오류가 발생했습니다.", e)
    }
  }

  private fun <T> parseResponse(response: String, typeReference: TypeReference<T>): T {
    return try {
      val jsonString = cleanJson(response)
      objectMapper.readValue(jsonString, typeReference)
    } catch (e: Exception) {
      logger.error("Gemini 응답 파싱 실패. 원본 응답: $response", e)
      throw RuntimeException("AI 응답을 처리하는 중 오류가 발생했습니다.", e)
    }
  }

  private fun cleanJson(response: String): String {
    return response.replace("```json", "").replace("```", "").trim()
  }

  /**
   * 모델 목록을 순회하며 API 호출을 시도합니다.
   * 실패 시 다음 모델로 재시도하며, 모든 모델 실패 시 마지막 예외를 던집니다.
   */
  private fun <T> executeWithModelFallback(action: (String) -> T): T {
    if (models.isEmpty()) {
      throw IllegalStateException("사용 가능한 Gemini 모델이 설정되지 않았습니다.")
    }

    var lastException: Exception? = null

    for (modelId in models) {
      try {
        return action(modelId)
      } catch (e: Exception) {
        logger.warn("모델 '$modelId' 사용 중 오류 발생: ${e.message}. 다음 모델을 시도합니다.")
        lastException = e
        // 계속해서 다음 모델 시도
      }
    }

    logger.error("모든 Gemini 모델 시도 실패. 마지막 오류: ", lastException)
    throw lastException ?: RuntimeException("Gemini API 호출 중 알 수 없는 오류 발생")
  }
}
