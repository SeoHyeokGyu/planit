package com.planit.entity

import jakarta.persistence.*

@Entity
@Table(name = "users") // 'user'는 DB 예약어인 경우가 많아 'users'를 권장합니다.
class User(
    loginId: String, // 로그인 ID로 사용
    password: String, // 비밀번호는 항상 해시하여 저장
    nickname: String?,
) : BaseEntity() { // BaseTimeEntity 상속
  @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null

  @Column(nullable = false, unique = true)
  var loginId = loginId
    private set

  @Column(nullable = false)
  var password = password
    private set

  @Column(nullable = true)
  var nickname = nickname
    private set

  @Column(nullable = false)
  var totalPoint: Long = 0
    protected set

  fun changePassword(password: String) {
    this.password = password
  }

  fun changeNickname(nickname: String) {
    this.nickname = nickname
  }

  fun addPoint(point: Long) {
    this.totalPoint += point
  }

  fun subtractPoint(point: Long) {
    this.totalPoint = maxOf(0, this.totalPoint - point)
  }
}
