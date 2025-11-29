package com.planit.service

import com.planit.entity.Follow
import com.planit.repository.FollowRepository
import com.planit.repository.UserRepository
import java.util.NoSuchElementException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
) {

  @Transactional
  fun follow(followerId: String, followingId: String) {
    if (followerId.contentEquals(followingId)) {
      throw IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.")
    }
    val follower =
        userRepository.findByLoginId(followerId)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $followerId")
    val following =
        userRepository.findByLoginId(followingId)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $followerId")

    if (followRepository.existsByFollowerIdAndFollowingId(follower.id!!, following.id!!)) {
      throw IllegalStateException("이미 팔로우한 사용자입니다.")
    }

    followRepository.save(Follow(follower = follower, following = following))
  }

  @Transactional
  fun unfollow(followerId: String, followingId: String) {
    val follower =
        userRepository.findByLoginId(followerId)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $followerId")
    val following =
        userRepository.findByLoginId(followingId)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $followerId")

    val follow =
        followRepository.findByFollowerIdAndFollowingId(follower.id!!, following.id!!)
            ?: throw IllegalStateException("팔로우 관계가 존재하지 않습니다.")
    followRepository.delete(follow)
  }

  @Transactional(readOnly = true)
  fun getFollowerCount(userLoginId: String): Long {
    val follower =
        userRepository.findByLoginId(userLoginId)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $userLoginId")
    return followRepository.countByFollowingId(follower.id!!)
  }

  @Transactional(readOnly = true)
  fun getFollowingCount(userLoginId: String): Long {
    val following =
        userRepository.findByLoginId(userLoginId)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $userLoginId")
    return followRepository.countByFollowerId(following.id!!)
  }
}
