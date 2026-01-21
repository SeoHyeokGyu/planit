package com.planit.service

import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.Part
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class GeminiService {

  private val client = Client()
  private val modelId = "gemini-3-flash-preview"

  fun analyzeImage(prompt: String, image: MultipartFile): String {
    val imagePart = Part.fromBytes(image.bytes, image.contentType ?: "image/jpeg")
    val textPart = Part.fromText(prompt)

    // Use Content.fromParts to avoid builder issues
    val content = Content.fromParts(textPart, imagePart)

    val response = client.models.generateContent(modelId, content, null)
    return response.text() ?: "분석 결과가 없습니다."
  }

  fun generateContent(prompt: String): String {
    val response = client.models.generateContent(modelId, prompt, null)
    return response.text() ?: "추천 결과를 생성할 수 없습니다."
  }
}
