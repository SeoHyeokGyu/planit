package com.planit.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

  /**
   * SPA(Single Page Application) 라우팅 설정
   * API 요청이 아닌 모든 경로를 index.html로 포워딩
   */
  override fun addViewControllers(registry: ViewControllerRegistry) {
    // 루트 경로를 index.html로 포워딩
    registry.addViewController("/").setViewName("forward:/index.html")
    // API가 아닌 모든 경로를 index.html로 포워딩 (SPA 라우팅)
    registry.addViewController("/{x:[\\w\\-]+}/**").setViewName("forward:/index.html")
  }

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:uploads/")
  }
}
