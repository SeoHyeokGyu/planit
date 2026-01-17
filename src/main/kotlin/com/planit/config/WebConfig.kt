package com.planit.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/** Spring Web MVC 설정을 담당하는 클래스입니다. SPA 라우팅 및 정적 리소스(업로드 파일 등) 서빙을 설정합니다. */
@Configuration
class WebConfig(
  // 실제 파일이 저장된 물리적 경로 (예: uploads 또는 /app/uploads)
  @param:Value("\${file.upload-dir}") private val uploadDir: String,
  // 웹에서 접근할 때 사용할 URL 접두사 (예: /images)
  @param:Value("\${file.upload-url-path}") private val uploadUrlPath: String,
) : WebMvcConfigurer {

  /**
   * 정적 리소스 핸들러 설정 특정 URL 패턴으로 들어오는 요청을 서버의 물리적 파일 시스템 경로로 매핑합니다.
   *
   * [설정 내용]
   * - URL 패턴: 설정된 경로 하위의 모든 파일 (예: /images/photo.png)
   * - 물리적 경로: 서버 내부의 실제 저장소 위치 (예: file:uploads/)
   */
  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry.addResourceHandler("$uploadUrlPath/**").addResourceLocations("file:$uploadDir/")
  }
}
