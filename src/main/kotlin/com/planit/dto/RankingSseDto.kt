package com.planit.dto

import com.planit.enums.RankingPeriodType
import java.time.LocalDateTime

/**
 * 랭킹 업데이트 SSE 이벤트 DTO입니다.
 * Top 10 랭킹이 변경될 때 모든 클라이언트에게 브로드캐스트됩니다.
 *
 * @property eventType 이벤트 타입 (RANKING_UPDATE)
 * @property periodType 랭킹 기간 유형 (WEEKLY, MONTHLY, ALLTIME)
 * @property periodKey 기간 식별자
 * @property top10 상위 10명의 랭킹 목록
 * @property updatedUser 점수가 업데이트된 사용자 정보 (nullable)
 * @property timestamp 이벤트 발생 시각
 */
data class RankingUpdateEvent(
    val eventType: String = "RANKING_UPDATE",
    val periodType: RankingPeriodType,
    val periodKey: String,
    val top10: List<RankingEntryResponse>,
    val updatedUser: UpdatedUserInfo?,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 업데이트된 사용자 정보를 담는 DTO입니다.
 * @property userId 사용자 ID
 * @property loginId 로그인 ID
 * @property nickname 닉네임
 * @property previousRank 이전 순위 (신규 진입 시 null)
 * @property currentRank 현재 순위
 * @property scoreDelta 점수 변화량
 * @property newScore 현재 총 점수
 */
data class UpdatedUserInfo(
    val userId: Long?,
    val loginId: String,
    val nickname: String?,
    val previousRank: Int?,
    val currentRank: Int,
    val scoreDelta: Long,
    val newScore: Long
)

/**
 * SSE 연결 상태 응답 DTO입니다.
 * @property connectedClients 현재 연결된 클라이언트 수
 * @property status 연결 상태 메시지
 */
data class SseConnectionStatus(
    val connectedClients: Int,
    val status: String = "connected"
)
