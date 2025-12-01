package com.planit.dto

import com.planit.entity.Feed
import com.planit.enums.FeedType
import java.time.LocalDateTime

/**
 * 피드 응답 DTO (일반 조회용)
 */
data class FeedResponse(
    val id: Long,
    val userId: Long,
    val username: String,
    val profileImage: String?,
    val type: FeedType,
    val message: String,
    val challengeId: Long?,
    val challengeTitle: String?,
    val certificationId: Long?,
    val certificationPhotoUrl: String?,
    val createdAt: LocalDateTime,
    val isRead: Boolean
)

/**
 * 피드 이벤트 DTO (SSE 전송용)
 */
data class FeedEvent(
    val id: Long,
    val userId: Long,
    val username: String,
    val profileImage: String?,
    val type: FeedType,
    val message: String,
    val challengeId: Long?,
    val challengeTitle: String?,
    val certificationId: Long?,
    val certificationPhotoUrl: String?,
    val timestamp: LocalDateTime
)

/**
 * Feed 엔티티를 FeedResponse로 변환
 */
fun Feed.toResponse() = FeedResponse(
    id = id ?: 0L,
    userId = user.id ?: 0L,
    username = user.nickname ?: user.loginId,
    profileImage = null, // TODO: User 엔티티에 profileImage 필드 추가 필요
    type = type,
    message = message,
    challengeId = challenge?.id,
    challengeTitle = challenge?.title,
    certificationId = certification?.id,
    certificationPhotoUrl = certification?.photoUrl,
    createdAt = createdAt,
    isRead = isRead
)

/**
 * Feed 엔티티를 FeedEvent로 변환 (SSE 브로드캐스트용)
 */
fun Feed.toEvent() = FeedEvent(
    id = id ?: 0L,
    userId = user.id ?: 0L,
    username = user.nickname ?: user.loginId,
    profileImage = null, // TODO: User 엔티티에 profileImage 필드 추가 필요
    type = type,
    message = message,
    challengeId = challenge?.id,
    challengeTitle = challenge?.title,
    certificationId = certification?.id,
    certificationPhotoUrl = certification?.photoUrl,
    timestamp = createdAt
)
