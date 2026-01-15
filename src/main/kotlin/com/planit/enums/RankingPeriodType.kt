package com.planit.enums

/**
 * 랭킹 기간 유형을 정의하는 Enum 클래스입니다.
 * @property keyPrefix Redis 키 생성에 사용되는 접두사
 * @property displayName 화면 표시용 이름
 */
enum class RankingPeriodType(
    val keyPrefix: String,
    val displayName: String
) {
    WEEKLY("ranking:weekly", "주간"),
    MONTHLY("ranking:monthly", "월간"),
    ALLTIME("ranking:alltime", "전체");

    companion object {
        const val REDIS_KEY_PREFIX = "ranking"
    }
}
