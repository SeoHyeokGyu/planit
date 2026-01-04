package com.planit.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
  name = "user_badges",
  uniqueConstraints =
    [
      UniqueConstraint(columnNames = ["user_id", "badge_id"]) // 중복 획득 방지
    ],
)
class UserBadge(
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) val user: User,
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "badge_id", nullable = false)
  val badge: Badge,
  @Column(nullable = false) val acquiredAt: LocalDateTime = LocalDateTime.now(),
  @Column(nullable = false) var isNew: Boolean = true, // 새로운 배지 알림용
) : BaseEntity() {
  @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null
}
