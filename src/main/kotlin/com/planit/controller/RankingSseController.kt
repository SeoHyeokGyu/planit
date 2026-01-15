package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.service.RankingSseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Tag(name = "Ranking SSE", description = "실시간 랭킹 업데이트 SSE API")
@RestController
@RequestMapping("/api/rankings")
class RankingSseController(
    private val rankingSseService: RankingSseService
) {

    @Operation(
        summary = "실시간 랭킹 스트림 구독",
        description = """
            SSE(Server-Sent Events)를 통해 실시간 랭킹 업데이트를 수신합니다.

            ## 이벤트 타입
            - **connect**: 연결 성공 시 전송 (연결된 클라이언트 수 포함)
            - **ranking**: 랭킹 업데이트 이벤트 (Top 10 변경 시)
            - **heartbeat**: 연결 유지용 heartbeat (30초 간격)

            ## 응답 예시
            ```
            event: connect
            data: {"connectedClients": 5, "status": "connected"}

            event: ranking
            data: {
              "eventType": "RANKING_UPDATE",
              "periodType": "ALLTIME",
              "periodKey": "alltime",
              "top10": [...],
              "updatedUser": {
                "loginId": "user123",
                "currentRank": 3,
                "scoreDelta": 100
              },
              "timestamp": "2026-01-15T10:30:00"
            }
            ```

            ## 사용 예시 (JavaScript)
            ```javascript
            const eventSource = new EventSource('/api/rankings/stream');

            eventSource.addEventListener('ranking', (event) => {
              const data = JSON.parse(event.data);
              console.log('Top 10 Updated:', data.top10);
            });
            ```
        """
    )
    @GetMapping(
        "/stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun streamRankings(): SseEmitter {
        return rankingSseService.subscribe()
    }

    @Operation(
        summary = "SSE 연결 상태 확인",
        description = "현재 랭킹 SSE에 연결된 클라이언트 수를 반환합니다."
    )
    @GetMapping("/stream/status")
    fun getStreamStatus(): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val status = mapOf(
            "connectedClients" to rankingSseService.getConnectedClientCount(),
            "status" to "active"
        )
        return ResponseEntity.ok(ApiResponse.success(status))
    }
}
