package com.planit.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import org.springframework.data.domain.Page

/**
 * 공용 API 응답 포맷을 위한 데이터 클래스입니다.
 * @param T 응답 데이터의 타입
 * @property success 성공 여부
 * @property data 응답 데이터
 * @property pagination 페이징 정보 (페이징 응답 시에만 포함)
 * @property error 에러 정보 (실패 시에만 포함)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val pagination: PaginationInfo? = null,
    val error: ApiError? = null
) {
  /**
   * API 에러 정보를 담는 데이터 클래스입니다. ApiResponse 내부에 중첩되어 사용됩니다.
   *
   * @param code 머신이 읽을 수 있는 에러 코드
   * @param message 사람이 읽을 수 있는 에러 메시지
   * @param timestamp 에러 발생 시각
   */
  data class ApiError(
      val code: String,
      val message: String,
      val timestamp: LocalDateTime = LocalDateTime.now()
  )

  companion object {
    /**
     * 성공 응답을 생성합니다. (데이터만 포함)
     * @param data 포함할 데이터
     */
    fun <T> success(data: T): ApiResponse<T> {
      return ApiResponse(success = true, data = data)
    }

    /**
     * 성공 응답을 생성합니다. (페이징 데이터 포함)
     * @param data 실제 데이터 목록
     * @param page Spring Data의 Page 객체
     */
    fun <T, E> pagedSuccess(data: List<T>, page: Page<E>): ApiResponse<List<T>> {
      return ApiResponse(
          success = true,
          data = data,
          pagination = PaginationInfo.from(page)
      )
    }

    /** 데이터가 없는 성공 응답을 생성합니다. */
    fun success(): ApiResponse<Unit> {
      return ApiResponse(success = true, data = Unit)
    }

    /**
     * 실패 응답을 생성합니다.
     * @param code 에러 코드
     * @param message 에러 메시지
     */
    fun error(code: String, message: String): ApiResponse<Unit> {
      return ApiResponse(success = false, error = ApiError(code, message))
    }
  }
}

/**
 * 페이징된 응답의 페이지 정보를 담는 DTO입니다.
 * @property pageNumber 현재 페이지 번호 (0부터 시작)
 * @property pageSize 페이지당 항목 수
 * @property totalElements 전체 항목 수
 * @property totalPages 전체 페이지 수
 * @property isLast 현재 페이지가 마지막 페이지인지 여부
 */
data class PaginationInfo(
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isLast: Boolean
) {
    companion object {
        /**
         * Spring Data JPA의 Page 객체로부터 PaginationInfo DTO를 생성합니다.
         * @param page 원본 Page 객체
         * @return PaginationInfo DTO
         */
        fun <T> from(page: Page<T>): PaginationInfo {
            return PaginationInfo(
                pageNumber = page.number,
                pageSize = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                isLast = page.isLast
            )
        }
    }
}
