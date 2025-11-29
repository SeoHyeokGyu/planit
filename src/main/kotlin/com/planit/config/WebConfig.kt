package com.planit.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Spring Web MVC 설정을 담당하는 클래스입니다.
 * SPA 라우팅 및 리소스 핸들러를 설정합니다.
 */
@Configuration
class WebConfig : WebMvcConfigurer {

  /**
   * SPA(Single Page Application) 라우팅 설정
   * API 요청이 아닌 모든 경로를 index.html로 포워딩하여 클라이언트 사이드 라우팅을 지원합니다.
   */
  override fun addViewControllers(registry: ViewControllerRegistry) {
    // 루트 경로를 index.html로 포워딩
    registry.addViewController("/").setViewName("forward:/index.html")
    // API가 아닌 모든 경로를 index.html로 포워딩 (SPA 라우팅)
    registry.addViewController("/{x:[\\w\\-]+}/**").setViewName("forward:/index.html")
  }

  /**
   *   정적 리소스 핸들러를 추가하여 특정 경로의 파일을 서빙합니다.
   *   /uploads/\**  경로로 들어오는 요청에 대해 'uploads/' 디렉토리의 파일을 제공합니다.
   */
  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry.addResourceHandler("/uploads/**")
      .addResourceLocations("file:uploads/")
  }
}





