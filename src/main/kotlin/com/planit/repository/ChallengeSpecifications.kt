package com.planit.repository

import com.planit.entity.Challenge
import com.planit.entity.ChallengeCategory
import com.planit.entity.ChallengeDifficulty
import org.springframework.data.jpa.domain.Specification


/**
 * 동적 쿼리를 위한 JPA Specification
 * 
 * 사용법:
 * val spec = ChallengeSpecifications.hasCategory(category)
 *     .and(ChallengeSpecifications.isActive())
 * repository.findAll(spec, pageable)
 */
object ChallengeSpecifications {

    /**
     * 키워드 검색 (제목 또는 설명에 포함)
     */
//    fun containsKeyword(keyword: String?): Specification<Challenge> {
//        return Specification { root, _, criteriaBuilder ->
//            if (keyword.isNullOrBlank()) {
//                return@Specification criteriaBuilder.conjunction()
//            }
//
//            val keywordPattern = "%${keyword.lowercase()}%"
//            criteriaBuilder.or(
//                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keywordPattern),
//                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), keywordPattern)
//            )
//        }
//    }
//
//    /**
//     * 카테고리 필터
//     */
//    fun hasCategory(category: ChallengeCategory?): Specification<Challenge> {
//        return Specification { root, _, criteriaBuilder ->
//            category?.let { criteriaBuilder.equal(root.get<ChallengeCategory>("category"), it) }
//                ?: criteriaBuilder.conjunction()
//        }
//    }
//
//    /**
//     * 난이도 필터
//     */
//    fun hasDifficulty(difficulty: ChallengeDifficulty?): Specification<Challenge> {
//        return Specification { root, _, criteriaBuilder ->
//            difficulty?.let { criteriaBuilder.equal(root.get<ChallengeDifficulty>("difficulty"), it) }
//                ?: criteriaBuilder.conjunction()
//        }
//    }
//
//    /**
//     * 진행중인 챌린지
//     */
//    fun isActive(now: LocalDateTime = LocalDateTime.now()): Specification<Challenge> {
//        return Specification { root, _, criteriaBuilder ->
//            criteriaBuilder.and(
//                criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), now),
//                criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), now)
//            )
//        }
//    }
//
//
//    /**
//     * 예정된 챌린지
//     */
//    fun isUpcoming(now: LocalDateTime = LocalDateTime.now()): Specification<Challenge> {
//        return Specification { root, _, criteriaBuilder ->
//            criteriaBuilder.greaterThan(root.get("startDate"), now)
//        }
//    }
//
//    /**
//     * 종료된 챌린지
//     */
//    fun isEnded(now: LocalDateTime = LocalDateTime.now()): Specification<Challenge> {
//        return Specification { root, _, criteriaBuilder ->
//            criteriaBuilder.lessThan(root.get("endDate"), now)
//        }
//    }
}