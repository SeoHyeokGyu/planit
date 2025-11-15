package com.planit.dto

import com.planit.entity.ChallengeCategory
import com.planit.entity.ChallengeDifficulty
import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class ChallengeCreateRequest(
    @field:NotBlank(message = "제목은 필수입니다")
    @field:Size(min = 2, max = 200, message = "제목은 2자 이상 200자 이하여야 합니다")
    val title: String,

    @field:NotBlank(message = "설명은 필수입니다")
    @field:Size(min = 10, max = 5000, message = "설명은 10자 이상 5000자 이하여야 합니다")
    val description: String,

    @field:NotNull(message = "카테고리는 필수입니다")
    val category: ChallengeCategory,

    @field:NotNull(message = "시작일은 필수입니다")
    @field:Future(message = "시작일은 현재보다 미래여야 합니다")
    val startDate: LocalDateTime,

    @field:NotNull(message = "종료일은 필수입니다")
    val endDate: LocalDateTime,

    @field:NotNull(message = "난이도는 필수입니다")
    val difficulty: ChallengeDifficulty,

    ) {
//    init {
//        require(endDate.isAfter(startDate)) {
//            "종료일은 시작일보다 이후여야 합니다"
//        }
//    }
}

data class ChallengeUpdateRequest(
    @field:NotBlank(message = "제목은 필수입니다")
    @field:Size(min = 2, max = 200, message = "제목은 2자 이상 200자 이하여야 합니다")
    val title: String,

    @field:NotBlank(message = "설명은 필수입니다")
    @field:Size(min = 10, max = 5000, message = "설명은 10자 이상 5000자 이하여야 합니다")
    val description: String,

    @field:NotNull(message = "카테고리는 필수입니다")
    val category: ChallengeCategory,

    @field:NotNull(message = "시작일은 필수입니다")
    val startDate: LocalDateTime,

    @field:NotNull(message = "종료일은 필수입니다")
    val endDate: LocalDateTime,

    @field:NotNull(message = "난이도는 필수입니다")
    val difficulty: ChallengeDifficulty,

    ) {
//    init {
//        require(endDate.isAfter(startDate)) {
//            "종료일은 시작일보다 이후여야 합니다"
//        }
//    }
}

data class ChallengeSearchRequest(
    val keyword: String? = null,
    val category: ChallengeCategory? = null,
    val difficulty: ChallengeDifficulty? = null,
    val status: ChallengeStatusFilter? = null,
    val tags: Set<String>? = null,

    @field:Min(0, message = "페이지 번호는 0 이상이어야 합니다")
    val page: Int = 0,

    @field:Min(1, message = "페이지 크기는 1 이상이어야 합니다")
    @field:Max(100, message = "페이지 크기는 100 이하여야 합니다")
    val size: Int = 20,

    val sort: String = "createdAt",
    val direction: String = "DESC"
)

enum class ChallengeStatusFilter {
    UPCOMING,   // 예정
    ACTIVE,     // 진행중
    ENDED,      // 종료
    ALL         // 전체
}
