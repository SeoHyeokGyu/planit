package com.planit.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import java.time.LocalDateTime
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {

  @CreatedDate
  @Column(nullable = false, updatable = false)
  var createdAt: LocalDateTime = LocalDateTime.now()
    protected set

  @LastModifiedDate
  @Column(nullable = false)
  var updatedAt: LocalDateTime = LocalDateTime.now()
    protected set

  @CreatedBy
  @Column(nullable = false, updatable = false)
  var createdBy: String = "SYSTEM" // 기본값을 "SYSTEM" 또는 다른 값으로 설정
    protected set

  @LastModifiedBy
  @Column(nullable = false)
  var lastModifiedBy: String = "SYSTEM"
    protected set
}
