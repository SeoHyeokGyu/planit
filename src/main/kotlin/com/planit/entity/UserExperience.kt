package com.planit.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_experiences")
class UserExperience(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val experience: Long,

    @Column(nullable = false)
    val reason: String,
) : BaseEntity() {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null
}
