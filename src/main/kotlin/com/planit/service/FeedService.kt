package com.planit.service

import com.planit.dto.FeedResponse
import com.planit.enums.FeedSortType
import com.planit.exception.UserNotFoundException
import com.planit.repository.CertificationRepository
import com.planit.repository.CommentRepository
import com.planit.repository.FollowRepository
import com.planit.repository.LikeRepository
import com.planit.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedService(
    private val followRepository: FollowRepository,
    private val certificationRepository: CertificationRepository,
    private val userRepository: UserRepository,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository
) {

    /**
     * 현재 사용자가 팔로우하는 사람들의 최근 인증 피드를 조회합니다.
     * @param userLoginId 현재 사용자의 로그인 ID
     * @param sortBy 정렬 기준 (LATEST, LIKES, COMMENTS, POPULAR)
     * @param pageable 페이징 정보
     * @return 페이징된 인증 피드
     */
    @Transactional(readOnly = true)
    fun getFeed(userLoginId: String, sortBy: FeedSortType, pageable: Pageable): Page<FeedResponse> {
        // 현재 사용자 조회
        val currentUser = userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")

        // 팔로우하는 사람들의 ID 조회
        val followingUserIds = followRepository.findFollowingIdsByFollowerId(currentUser.id!!).toMutableList()

        // 내 인증도 포함
        followingUserIds.add(currentUser.id)

        // DB 조회 전략 분기 (LATEST는 DB 정렬, 나머지는 메모리 정렬)
        val certificationPage = when (sortBy) {
            FeedSortType.LATEST -> {
                // 기존 방식: DB에서 직접 정렬
                certificationRepository.findByUser_IdInOrderByCreatedAtDesc(
                    followingUserIds,
                    pageable
                )
            }
            else -> {
                // 메모리 정렬용 충분한 데이터 조회
                val fetchSize = pageable.pageSize * (pageable.pageNumber + 1)
                val unpaged = PageRequest.of(0, maxOf(fetchSize, 100))
                certificationRepository.findByUser_IdInOrderByCreatedAtDesc(
                    followingUserIds,
                    unpaged
                )
            }
        }

        val certificationIds = certificationPage.content.mapNotNull { it.id }

        if (certificationIds.isEmpty()) {
            return PageImpl(emptyList(), pageable, certificationPage.totalElements)
        }

        // 일괄 조회 (N+1 문제 해결)
        val likeCounts = likeRepository.countByCertificationIdIn(certificationIds)
            .associate { it.getCertificationId() to it.getCount() }

        val commentCounts = commentRepository.countByCertificationIdIn(certificationIds)
            .associate { it.getCertificationId() to it.getCount() }

        val likedCertificationIds = likeRepository.findLikedCertificationIds(certificationIds, userLoginId).toSet()

        // Certification을 FeedResponse로 변환
        val feedResponses = certificationPage.content.map { certification ->
            val id = certification.id
            FeedResponse.from(
                certification,
                likeCounts[id] ?: 0L,
                commentCounts[id] ?: 0L,
                likedCertificationIds.contains(id),
                currentUser.id
            )
        }

        // 정렬 적용
        val sortedFeedResponses = when (sortBy) {
            FeedSortType.LATEST -> feedResponses // 이미 DB에서 정렬됨
            FeedSortType.LIKES -> feedResponses.sortedWith(
                compareByDescending<FeedResponse> { it.likeCount }
                    .thenByDescending { it.createdAt }
            )
            FeedSortType.COMMENTS -> feedResponses.sortedWith(
                compareByDescending<FeedResponse> { it.commentCount }
                    .thenByDescending { it.createdAt }
            )
            FeedSortType.POPULAR -> feedResponses.sortedWith(
                compareByDescending<FeedResponse> { it.likeCount + it.commentCount }
                    .thenByDescending { it.createdAt }
            )
        }

        // 메모리 페이징 (LATEST가 아닌 경우)
        val pagedResult = if (sortBy == FeedSortType.LATEST) {
            sortedFeedResponses
        } else {
            val start = pageable.pageNumber * pageable.pageSize
            val end = minOf(start + pageable.pageSize, sortedFeedResponses.size)
            if (start >= sortedFeedResponses.size) emptyList()
            else sortedFeedResponses.subList(start, end)
        }

        return PageImpl(pagedResult, pageable, certificationPage.totalElements)
    }
}
