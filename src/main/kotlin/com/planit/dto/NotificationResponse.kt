package com.planit.dto

import com.planit.entity.Notification
import com.planit.enums.NotificationType
import java.time.LocalDateTime

/**
 * 알림 응답 DTO
 */
data class NotificationResponse(
    val id: Long,
    val receiverId: Long,
    val receiverLoginId: String,
    val senderId: Long?,
    val senderLoginId: String?,
    val senderNickname: String?,
    val type: NotificationType,
    val message: String,
    val relatedId: String?,
    val relatedType: String?,
    val isRead: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        /**
         * Notification 엔티티로부터 NotificationResponse DTO를 생성합니다.
         */
        fun from(notification: Notification): NotificationResponse {
            return NotificationResponse(
                id = notification.id,
                receiverId = notification.receiver.id!!,
                receiverLoginId = notification.receiver.loginId,
                senderId = notification.sender?.id,
                senderLoginId = notification.sender?.loginId,
                senderNickname = notification.sender?.nickname,
                type = notification.type,
                message = notification.message,
                relatedId = notification.relatedId,
                relatedType = notification.relatedType,
                isRead = notification.isRead,
                createdAt = notification.createdAt
            )
        }
    }
}

/**
 * 읽지 않은 알림 개수 응답 DTO
 */
data class UnreadCountResponse(
    val count: Long
)
