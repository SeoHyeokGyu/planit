package com.planit.dto

/**
 * 실시간 피드 이벤트를 나타내는 데이터 클래스입니다.
 *
 * @property type 이벤트 타입 (예: "new_certification", "new_like")
 * @property data 이벤트와 함께 전송될 데이터
 */
data class FeedEvent(
    val type: String,
    val data: Any
)
