package com.planit.entity

import com.planit.enums.RankingPeriodType
import jakarta.persistence.*

/**
 * 사용자 랭킹 정보를 영구 저장하기 위한 엔티티입니다.
 * Redis ZSET의 스냅샷을 DB에 저장하여 데이터 내구성을 보장합니다.
 *
 * @property user 사용자 엔티티
 * @property periodType 랭킹 기간 유형 (WEEKLY, MONTHLY, ALLTIME)
 * @property periodKey 기간 식별자 (예: "2026-W03", "2026-01", "alltime")
 * @property score 해당 기간 누적 점수
 * @property rank 동기화 시점의 순위 스냅샷
 */
@Entity
@Table(
    name = "user_rankings",
    indexes = [
        Index(name = "idx_user_ranking_period", columnList = "period_type, period_key"),
        Index(name = "idx_user_ranking_user_period", columnList = "user_id, period_type, period_key")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_period",
            columnNames = ["user_id", "period_type", "period_key"]
        )
    ]
)
class UserRanking(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    val periodType: RankingPeriodType,

    @Column(name = "period_key", nullable = false, length = 20)
    val periodKey: String,

    @Column(nullable = false)
    var score: Long = 0,

    @Column(name = "rank_position")
    var rank: Int? = null
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun updateScore(newScore: Long) {
        this.score = newScore
    }

    fun updateRank(newRank: Int?) {
        this.rank = newRank
    }
}
