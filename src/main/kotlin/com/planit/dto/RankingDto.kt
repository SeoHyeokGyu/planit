package com.planit.dto

import com.planit.enums.RankingPeriodType

/**
 * 랭킹 내 개별 사용자 정보를 담는 DTO입니다.
 * @property rank 순위 (1부터 시작)
 * @property userId 사용자 ID (DB PK)
 * @property loginId 사용자 로그인 ID
 * @property nickname 사용자 닉네임 (username)
 * @property score 해당 기간 누적 점수
 */
data class RankingEntryResponse(
    val rank: Int,
    val userId: Long?,
    val loginId: String,
    val nickname: String?,
    val score: Long
)

/**
 * 페이지네이션된 랭킹 목록 응답 DTO입니다.
 * @property periodType 랭킹 기간 유형 (WEEKLY, MONTHLY, ALLTIME)
 * @property periodKey 기간 식별자 (예: "2026-W03", "2026-01", "alltime")
 * @property rankings 랭킹 목록
 * @property totalParticipants 전체 참여자 수
 * @property page 현재 페이지 (0부터 시작)
 * @property size 페이지 크기
 * @property totalPages 전체 페이지 수
 * @property isFirst 첫 페이지 여부
 * @property isLast 마지막 페이지 여부
 */
data class RankingListResponse(
    val periodType: RankingPeriodType,
    val periodKey: String,
    val rankings: List<RankingEntryResponse>,
    val totalParticipants: Long,
    val page: Int = 0,
    val size: Int = 20,
    val totalPages: Int = 1,
    val isFirst: Boolean = true,
    val isLast: Boolean = true
)

/**
 * 현재 사용자의 랭킹 정보를 담는 DTO입니다.
 * @property periodType 랭킹 기간 유형
 * @property periodKey 기간 식별자
 * @property rank 현재 순위 (1부터 시작, 랭킹에 없으면 null)
 * @property score 해당 기간 누적 점수
 * @property totalParticipants 전체 참여자 수
 */
data class MyRankingResponse(
    val periodType: RankingPeriodType,
    val periodKey: String,
    val rank: Int?,
    val score: Long,
    val totalParticipants: Long
)

/**
 * 모든 기간의 랭킹 정보를 담는 통합 응답 DTO입니다.
 * @property weekly 주간 랭킹 정보
 * @property monthly 월간 랭킹 정보
 * @property alltime 전체 랭킹 정보
 */
data class AllMyRankingsResponse(
    val weekly: MyRankingResponse,
    val monthly: MyRankingResponse,
    val alltime: MyRankingResponse
)
