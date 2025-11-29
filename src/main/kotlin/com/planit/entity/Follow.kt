package com.planit.entity

import jakarta.persistence.*

/**
 * 팔로우(Follow) 관계를 나타내는 엔티티입니다.
 * 한 사용자가 다른 사용자를 팔로우하는 관계를 저장합니다.
 */
@Entity
@Table(
    name = "follows",
    uniqueConstraints =
        [
            UniqueConstraint(
                name = "uk_follower_following", // 팔로워-팔로잉 쌍에 대한 유니크 제약 조건
                columnNames = ["follower_id", "following_id"],
            )
        ],
    indexes = [
        Index(name = "idx_follower_id", columnList = "follower_id"), // 팔로워 ID에 대한 인덱스
        Index(name = "idx_following_id", columnList = "following_id") // 팔로잉 ID에 대한 인덱스
    ]
)
class Follow(
    /**
     * 팔로우를 하는 사용자 (팔로워)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    val follower: User,
    /**
     * 팔로우를 당하는 사용자 (팔로잉 대상)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    val following: User,
) : BaseEntity() {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0
}
