package com.planit.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.ApplicationContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "애플리케이션 상태 확인 API")
class HealthController(private val context: ApplicationContext) {

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "애플리케이션 상태 확인")
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "UP",
                "timestamp" to LocalDateTime.now(),
                "service" to "Planit API"
            )
        )
    }

    @GetMapping("/beans")
    @Operation(summary = "List Beans", description = "애플리케이션에 등록된 모든 빈 목록 확인")
    fun listBeans(): ResponseEntity<Map<String, List<String>>> {
        val beanNames = context.beanDefinitionNames.sorted()
        return ResponseEntity.ok(mapOf("beans" to beanNames))
    }
}
