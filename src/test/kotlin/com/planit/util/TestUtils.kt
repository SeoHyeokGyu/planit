package com.planit.util

// 코틀린 객체의 private 프로퍼티에 값을 설정하는 유틸리티 함수
fun <T : Any> T.setPrivateProperty(propertyName: String, value: Any?) {
  val field = this::class.java.getDeclaredField(propertyName)
  field.isAccessible = true
  field.set(this, value)
}
