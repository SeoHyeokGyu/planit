package com.planit.repository

import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

/**
 * SSE Emitter를 메모리에 저장하고 관리하는 리포지토리입니다.
 *
 * 참고: 이 구현은 단일 애플리케이션 인스턴스 환경에서만 동작합니다.
 * 애플리케이션을 수평적으로 확장(scale-out)할 경우, 여러 인스턴스 간에 Emitter를 공유하기 위해
 * Redis, Kafka 등 외부 메시징 시스템을 이용한 별도의 구독 메커니즘이 필요합니다.
 */
@Repository
class EmitterRepository {

    private val emitters: MutableMap<String, SseEmitter> = ConcurrentHashMap()

    /**
     * SseEmitter를 저장합니다.
     *
     * @param loginId  사용자 Login ID
     * @param emitter  SseEmitter 인스턴스
     */
    fun save(loginId: String, emitter: SseEmitter) {
        emitters[loginId] = emitter
    }

    /**
     * 사용자 Login ID로 SseEmitter를 조회합니다.
     *
     * @param loginId 사용자 Login ID
     * @return SseEmitter 인스턴스, 없으면 null
     */
    fun get(loginId: String): SseEmitter? {
        return emitters[loginId]
    }

    /**
     * 사용자 Login ID로 SseEmitter를 삭제합니다.
     *
     * @param loginId 사용자 Login ID
     */
    fun deleteById(loginId: String) {
        emitters.remove(loginId)
    }

    /**
     * 저장된 모든 SseEmitter를 조회합니다.
     *
     * @return 모든 SseEmitter의 Map
     */
    fun findAll(): Map<String, SseEmitter> {
        return emitters.toMap()
    }
}
