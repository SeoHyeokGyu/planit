package com.planit.dto

import com.planit.enums.NotificationType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 알림 생성 요청 DTO (상세 버전)
 * 다른 서비스에서 알림을 생성할 때 사용됩니다.
 */
data class NotificationCreateRequest(
    @field:NotNull(message = "수신자 로그인 ID는 필수입니다")
    val receiverLoginId: String,

    val senderLoginId: String? = null,

    @field:NotNull(message = "알림 타입은 필수입니다")
    val type: NotificationType,

    @field:NotBlank(message = "메시지는 필수입니다")
    @field:Size(max = 500, message = "메시지는 500자 이하여야 합니다")
    val message: String,

    val relatedId: String? = null,

    val relatedType: String? = null
)

/**
 * 알림 생성 요청 DTO (간단 버전, 호환성)
 */
data class NotificationRequest(
    val userLoginId: String,
    val type: String,
    val message: String
)
