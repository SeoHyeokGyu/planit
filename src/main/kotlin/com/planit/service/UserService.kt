package com.planit.service

import com.planit.dto.*
import com.planit.entity.User
import com.planit.enums.ParticipantStatusEnum
import com.planit.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val certificationRepository: CertificationRepository,
    private val challengeParticipantRepository: ChallengeParticipantRepository,
    private val likeRepository: LikeRepository,
    private val followRepository: FollowRepository,
    private val notificationRepository: NotificationRepository,
    private val userPointRepository: UserPointRepository,
    private val commentRepository: CommentRepository,
    private val challengeRepository: ChallengeRepository
) {
  fun updateUser(user: User, updateRequest: UserUpdateRequest): UserProfileResponse {
    // ID로 사용자를 다시 조회하여 영속 상태(Managed)로 만듭니다.
    val managedUser =
        userRepository.findByLoginId(user.loginId)
            ?: throw NoSuchElementException("ID ${user.loginId}에 해당하는 사용자를 찾을 수 없습니다.")

    if (!updateRequest.nickname.isNullOrBlank()) {
      managedUser.changeNickname(updateRequest.nickname)
    }

    // 다른 속성 추가시 추가작성.
    return UserProfileResponse.of(managedUser)
  }

  fun updatePassword(loginId: String, request: UserPasswordUpdateRequest) {
    val user =
        userRepository.findByLoginId(loginId)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $loginId")

    if (!passwordEncoder.matches(request.oldPassword, user.password)) {
      throw IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.")
    }

    val newHashedPassword = passwordEncoder.encode(request.newPassword)
    user.changePassword(newHashedPassword)
    // @Transactional에 의해 메소드 종료 시 변경 감지(dirty checking)로 DB에 업데이트됩니다.
  }

  @Transactional(readOnly = true)
  fun getDashboardStats(loginId: String): UserDashboardStats {
    val challengeCount = challengeParticipantRepository.countByLoginIdAndStatus(loginId, ParticipantStatusEnum.ACTIVE)
    val certificationCount = certificationRepository.countByUser_LoginId(loginId)
    val followerCount = followRepository.countByFollowing_LoginId(loginId)
    val followingCount = followRepository.countByFollower_LoginId(loginId)

    return UserDashboardStats(
        challengeCount = challengeCount,
        certificationCount = certificationCount,
        followerCount = followerCount,
        followingCount = followingCount
    )
  }

  @Transactional(readOnly = true)
  fun getUserProfileByLoginId(loginId: String): UserProfileResponse {
    val user = userRepository.findByLoginId(loginId)
        ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $loginId")
    return UserProfileResponse.of(user)
  }

  @Transactional(readOnly = true)
  fun searchUsers(keyword: String, pageable: Pageable): Page<UserProfileResponse> {
    val usersPage = userRepository.findByLoginIdContainingOrNicknameContaining(keyword, keyword, pageable)
    return usersPage.map { UserProfileResponse.of(it) }
  }

  @Transactional
  fun deleteUser(loginId: String, request: UserDeleteRequest) {
    // 1. 사용자 조회 및 비밀번호 확인
    val user = userRepository.findByLoginId(loginId)
        ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $loginId")

    if (!passwordEncoder.matches(request.password, user.password)) {
      throw IllegalArgumentException("비밀번호가 일치하지 않습니다.")
    }

    val userId = user.id!!

    // 2. Like 삭제
    likeRepository.deleteByUser_LoginId(loginId)

    // 3. Follow 삭제 (양방향)
    followRepository.deleteByFollower_Id(userId)
    followRepository.deleteByFollowing_Id(userId)

    // 4. Notification 처리
    notificationRepository.deleteByReceiver_Id(userId)
    notificationRepository.nullifySenderBySenderId(userId)

    // 5. UserPoint 삭제
    userPointRepository.deleteByUser_Id(userId)

    // 6. ChallengeParticipant 삭제 + 챌린지 통계 업데이트
    val participants = challengeParticipantRepository.findByLoginId(loginId)
    participants.forEach { participant ->
      val challenge = participant.challenge
      if (challenge.participantCnt > 0) {
        challenge.participantCnt--
      }
      if (challenge.certificationCnt >= participant.certificationCnt.toLong()) {
        challenge.certificationCnt -= participant.certificationCnt.toLong()
      }
      challengeRepository.save(challenge)
    }
    challengeParticipantRepository.deleteByLoginId(loginId)

    // 7. Certification, Comment 익명화 (탈퇴 유저로 재할당)
    val withdrawalUser = getOrCreateWithdrawalUser()
    certificationRepository.reassignUserByUserId(userId, withdrawalUser.id!!)
    commentRepository.reassignUserByUserId(userId, withdrawalUser.id!!)

    // 8. User 삭제 (하드 삭제)
    userRepository.delete(user)
  }

  /**
   * 탈퇴한 사용자의 게시물 및 댓글을 유지하기 위한 전용 '탈퇴한 사용자'를 조회하거나 생성합니다.
   */
  private fun getOrCreateWithdrawalUser(): User {
    return userRepository.findByLoginId("withdrawn_user")
        ?: userRepository.save(
            User(
                loginId = "withdrawn_user",
                password = passwordEncoder.encode(UUID.randomUUID().toString()),
                nickname = "탈퇴한 사용자"
            )
        )
  }
}
