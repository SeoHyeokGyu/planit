package com.planit.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "애플리케이션 상태 확인 API")
class HealthController {

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
}
