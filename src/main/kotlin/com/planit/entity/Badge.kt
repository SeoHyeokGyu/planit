package com.planit.entity

import com.planit.enums.BadgeGrade
import com.planit.enums.BadgeType
import jakarta.persistence.*

@Entity
@Table(name = "badges")
class Badge(
  @Column(unique = true, nullable = false) val code: String, // 예: CERT_1, SOCIAL_10
  @Column(nullable = false) val name: String,
  @Column(nullable = false) val description: String,
  @Column(nullable = false) val iconCode: String, // 프론트엔드 아이콘 매핑용 코드 (FOOTPRINT, FIRE 등)
  @Enumerated(EnumType.STRING) @Column(nullable = false) val type: BadgeType,
  @Enumerated(EnumType.STRING) @Column(nullable = false) val grade: BadgeGrade,
  @Column(nullable = false) val requiredValue: Long,
) : BaseEntity() {
  @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null
}
