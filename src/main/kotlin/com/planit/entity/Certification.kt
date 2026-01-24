package com.planit.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

/**
 * 챌린지 인증 엔티티.
 * 사용자가 챌린지에 참여하고 그 결과를 인증하는 정보를 저장합니다.
 *
 * @SQLDelete: 엔티티 삭제 시 isDeleted 필드를 true로 업데이트하여 Soft Delete를 구현합니다.
 * @SQLRestriction: isDeleted 필드가 false인 엔티티만 조회하도록 필터링합니다. (Deprecated된 @Where 대체)
 */
@Entity
@Table(name = "certifications")
@SQLDelete(sql = "UPDATE certifications SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
class Certification(
  /**
   * 인증을 작성한 사용자 (User 엔티티와 다대일 관계)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  val user: User,
  /**
   * 인증이 속한 챌린지 (Challenge 엔티티와 다대일 관계)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "challenge_id", nullable = false)
  val challenge: Challenge,

  /**
   * 인증의 제목
   */
  @Column(nullable = false)
  var title: String,

  /**
   * 인증의 내용
   */
  @Column(nullable = false, length = 1000)
  var content: String,

  /**
   * 인증 사진의 URL (선택 사항)
   */
  @Column(nullable = true)
  var photoUrl: String? = null,

  /**
   * AI 분석 결과 (선택 사항)
   */
  @Column(nullable = true, length = 2000)
  var analysisResult: String? = null,

  /**
   * AI 분석 주제 적합 여부 (선택 사항)
   */
  @Column(nullable = true)
  var isSuitable: Boolean? = null,

  /**
   * 인증의 삭제 여부 (Soft Delete에 사용)
   */
  @Column(nullable = false)
  var isDeleted: Boolean = false,

  /**
   * 인증의 고유 ID (자동 생성)
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null

) : BaseEntity() // 생성일, 수정일 등 공통 필드를 상속받음
