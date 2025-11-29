package com.planit.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.domain.Page

/**
 * 공용 API 응답 포맷을 위한 데이터 클래스입니다.
 *
 * @param <T> 응답 데이터의 타입
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(val success: Boolean, val data: T? = null, val error: ApiError? = null) {
  /**
   * API 에러 정보를 담는 데이터 클래스입니다. ApiResponse 내부에 중첩되어 사용됩니다.
   *
   * @param code 머신이 읽을 수 있는 에러 코드
   * @param message 사람이 읽을 수 있는 에러 메시지
   */
  data class ApiError(val code: String, val message: String)

  companion object {
    /**
     * 성공 응답을 생성합니다.
     *
     * @param data 포함할 데이터
     */
    fun <T> success(data: T): ApiResponse<T> {
      return ApiResponse(success = true, data = data)
    }

    /** 데이터가 없는 성공 응답을 생성합니다. */
    fun success(): ApiResponse<Unit> {
      return ApiResponse(success = true, data = Unit)
    }

    /**
     * 실패 응답을 생성합니다.
     *
     * @param code 에러 코드
     * @param message 에러 메시지
     */
    fun error(code: String, message: String): ApiResponse<Unit> {
      return ApiResponse(success = false, error = ApiError(code, message))
    }
  }
}

data class PagedResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isLast: Boolean
) {
    companion object {
        fun <T, U> from(page: Page<T>, content: List<U>): PagedResponse<U> {
            return PagedResponse(
                content = content,
                pageNumber = page.number,
                pageSize = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                isLast = page.isLast
            )
        }
    }
}
