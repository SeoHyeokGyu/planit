package com.planit.util

import java.lang.reflect.Field

// 코틀린 객체의 private 프로퍼티에 값을 설정하는 유틸리티 함수
// 상속 계층의 상위 클래스에 선언된 private 프로퍼티도 설정할 수 있도록 수정
fun <T : Any> T.setPrivateProperty(propertyName: String, value: Any?) {
    var clazz: Class<*>? = this::class.java
    while (clazz != null) {
        try {
            val field = clazz.getDeclaredField(propertyName)
            field.isAccessible = true
            field.set(this, value)
            return // 필드를 찾고 값을 설정했으면 종료
        } catch (e: NoSuchFieldException) {
            clazz = clazz.superclass // 상속 계층을 따라 상위 클래스로 이동
        }
    }
    // 필드를 찾지 못했으면 예외 발생
    throw NoSuchFieldException("Field '$propertyName' not found in class ${this::class.java.name} or its superclasses.")
}
