package com.planit.dto

/**
 * 인스턴스 간 피드 이벤트를 전달하기 위한 데이터 클래스입니다.
 *
 * @property loginId 최종적으로 이벤트를 수신할 사용자의 Login ID
 * @property feedEvent 사용자에게 전송될 실제 피드 이벤트
 */
data class ForwardedFeedEvent(
    val loginId: String,
    val feedEvent: FeedEvent
)
