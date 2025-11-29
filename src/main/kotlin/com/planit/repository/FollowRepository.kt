package com.planit.repository

import com.planit.entity.Follow
import org.springframework.data.jpa.repository.JpaRepository

interface FollowRepository : JpaRepository<Follow, Long> {
  fun existsByFollowerIdAndFollowingId(followerId: Long, followingId: Long): Boolean

  fun findByFollowerIdAndFollowingId(followerId: Long, followingId: Long): Follow?

  fun findAllByFollowerId(followerId: Long): List<Follow>

  fun findAllByFollowingId(followingId: Long): List<Follow>

  fun countByFollowingId(followingId: Long): Long

  fun countByFollowerId(followerId: Long): Long
}
