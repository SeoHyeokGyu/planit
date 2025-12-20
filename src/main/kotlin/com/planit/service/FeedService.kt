package com.planit.service

import com.planit.dto.CertificationResponse
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
    fun getFeed(userLoginId: String, pageable: Pageable): Page<CertificationResponse> {
        // 현재 사용자 조회
        val currentUser = userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")

        // 팔로우하는 사람들의 ID 조회
        val followingUserIds = followRepository.findFollowingIdsByFollowerId(currentUser.id!!)

        // 팔로우하는 사람이 없으면 빈 페이지 반환
        if (followingUserIds.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0)
        }

        // 팔로우하는 사람들의 최근 인증 조회 (시간 역순)
        val certificationPage = certificationRepository.findByUser_IdInOrderByCreatedAtDesc(
            followingUserIds,
            pageable
        )

        // Certification을 CertificationResponse로 변환
        val certificationResponses = certificationPage.content.map { certification ->
            CertificationResponse(
                id = certification.id!!,
                title = certification.title,
                content = certification.content,
                photoUrl = certification.photoUrl,
                authorNickname = certification.user.nickname ?: certification.user.loginId,
                senderNickname = certification.user.nickname ?: certification.user.loginId,
                senderLoginId = certification.user.loginId,
                challengeId = certification.challenge.id!!,
                challengeTitle = certification.challenge.title,
                createdAt = certification.createdAt.toString(),
                updatedAt = certification.updatedAt.toString()
            )
        }

        return PageImpl(certificationResponses, pageable, certificationPage.totalElements)
    }
}
