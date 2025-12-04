package com.planit.config

import org.springframework.stereotype.Component
import java.util.UUID

/**
 * 애플리케이션 인스턴스의 고유 ID를 제공하는 컴포넌트입니다.
 * 인스턴스가 시작될 때 UUID를 생성하여 고유성을 보장합니다.
 */
@Component
class InstanceIdProvider {
    val id: String = UUID.randomUUID().toString()
}
