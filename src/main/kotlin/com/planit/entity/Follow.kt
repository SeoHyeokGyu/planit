package com.planit.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "follows",
    uniqueConstraints =
        [
            UniqueConstraint(
                name = "uk_follower_following",
                columnNames = ["follower_id", "following_id"],
            )
        ],
)
class Follow(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    val follower: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    val following: User,
) : BaseEntity() {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0
}
