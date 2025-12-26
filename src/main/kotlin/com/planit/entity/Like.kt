package com.planit.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "likes",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_user_certification", columnNames = ["user_id", "certification_id"])
    ]
)
class Like(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id", nullable = false)
    val certification: Certification
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
