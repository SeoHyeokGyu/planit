package com.planit.service

import com.planit.entity.Certification
import com.planit.repository.CertificationRepository
import com.planit.repository.FollowRepository
import com.planit.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.planit.exception.UserNotFoundException // UserNotFoundException import 추가

/**
 * 팔로잉 피드(Following Feed) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 사용자가 팔로우하는 사람들의 최신 인증 목록을 제공합니다.
 */
@Service
class FeedService(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val certificationRepository: CertificationRepository
) {
    /**
     * 특정 사용자가 팔로우하는 사람들의 최신 인증 목록을 페이징하여 조회합니다.
     * @param userLoginId 피드를 조회할 사용자의 로그인 ID
     * @param pageable 페이징 정보
     * @return 팔로우하는 사용자들의 인증 엔티티 페이지
     * @throws UserNotFoundException 사용자를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    fun getFollowingFeed(userLoginId: String, pageable: Pageable): Page<Certification> {
        val user = userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")
        
        // 현재 사용자가 팔로우하는 모든 사용자들의 ID 목록을 효율적으로 조회
        val followingUserIds = followRepository.findFollowingIdsByFollowerId(user.id!!)

        // 팔로우하는 사용자가 없으면 빈 페이지 반환
        if (followingUserIds.isEmpty()) {
            return Page.empty(pageable)
        }

        // 팔로우하는 사용자들의 인증을 생성일 내림차순으로 페이징하여 조회
        return certificationRepository.findByUser_IdInOrderByCreatedAtDesc(followingUserIds, pageable)
    }
}
