package com.planit.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where

@Entity
@Table(name = "certifications")
@SQLDelete(sql = "UPDATE certifications SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
class Certification(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    val challenge: Challenge,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, length = 1000)
    var content: String,

    @Column(nullable = true)
    var photoUrl: String? = null,

    @Column(nullable = false)
    var isDeleted: Boolean = false,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) : BaseEntity()
