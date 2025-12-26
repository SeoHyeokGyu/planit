package com.planit.service

import com.planit.dto.FeedResponse
import com.planit.exception.UserNotFoundException
import com.planit.repository.CertificationRepository
import com.planit.repository.FollowRepository
import com.planit.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedService(
    private val followRepository: FollowRepository,
    private val certificationRepository: CertificationRepository,
    private val userRepository: UserRepository
) {

    /**
     * 현재 사용자가 팔로우하는 사람들의 최근 인증 피드를 조회합니다.
     * @param userLoginId 현재 사용자의 로그인 ID
     * @param pageable 페이징 정보
     * @return 페이징된 인증 피드
     */
    @Transactional(readOnly = true)
    fun getFeed(userLoginId: String, pageable: Pageable): Page<FeedResponse> {
        // 현재 사용자 조회
        val currentUser = userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")

        // 팔로우하는 사람들의 ID 조회
        val followingUserIds = followRepository.findFollowingIdsByFollowerId(currentUser.id!!).toMutableList()
        
        // 내 인증도 포함
        followingUserIds.add(currentUser.id!!)

        // 팔로우하는 사람들의 최근 인증 조회 (시간 역순)
        val certificationPage = certificationRepository.findByUser_IdInOrderByCreatedAtDesc(
            followingUserIds,
            pageable
        )

        // Certification을 FeedResponse로 변환
        val feedResponses = certificationPage.content.map { FeedResponse.from(it) }

        return PageImpl(feedResponses, pageable, certificationPage.totalElements)
    }
}
