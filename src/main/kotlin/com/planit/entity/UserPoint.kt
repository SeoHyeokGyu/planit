package com.planit.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_points")
class UserPoint(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val points: Long,

    @Column(nullable = false)
    val reason: String,
) : BaseEntity() {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null
}
