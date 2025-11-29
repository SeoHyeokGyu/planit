package com.planit.repository

import com.planit.entity.Follow
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * 팔로우(Follow) 엔티티를 관리하는 JpaRepository 인터페이스입니다.
 */
interface FollowRepository : JpaRepository<Follow, Long> {
  /**
   * 특정 팔로워 ID와 팔로잉 ID를 가진 팔로우 관계가 존재하는지 확인합니다.
   * @param followerId 팔로우하는 사용자의 ID
   * @param followingId 팔로우 대상 사용자의 ID
   * @return 팔로우 관계 존재 여부
   */
  fun existsByFollowerIdAndFollowingId(followerId: Long, followingId: Long): Boolean

  /**
   * 특정 팔로워 ID와 팔로잉 ID를 가진 팔로우 관계를 조회합니다.
   * @param followerId 팔로우하는 사용자의 ID
   * @param followingId 팔로우 대상 사용자의 ID
   * @return 팔로우 엔티티 (존재하지 않으면 null)
   */
  fun findByFollowerIdAndFollowingId(followerId: Long, followingId: Long): Follow?

  /**
   * 특정 팔로워 ID를 가진 모든 팔로우 관계를 페이징하여 조회합니다. (해당 유저가 팔로우하는 사람들)
   * @param followerId 팔로우하는 사용자의 ID
   * @param pageable 페이징 정보
   * @return 팔로우 관계 페이지
   */
  fun findAllByFollowerId(followerId: Long, pageable: Pageable): Page<Follow>

  /**
   * 특정 팔로잉 ID를 가진 모든 팔로우 관계를 페이징하여 조회합니다. (해당 유저를 팔로우하는 사람들)
   * @param followingId 팔로우 대상 사용자의 ID
   * @param pageable 페이징 정보
   * @return 팔로우 관계 페이지
   */
  fun findAllByFollowingId(followingId: Long, pageable: Pageable): Page<Follow>

  /**
   * 특정 팔로잉 ID를 가진 팔로우 관계의 개수를 조회합니다. (해당 유저의 팔로워 수)
   * @param followingId 팔로우 대상 사용자의 ID
   * @return 팔로워 수
   */
  fun countByFollowingId(followingId: Long): Long

  /**
   * 특정 팔로워 ID를 가진 팔로우 관계의 개수를 조회합니다. (해당 유저가 팔로우하는 사람 수)
   * @param followerId 팔로우하는 사용자의 ID
   * @return 팔로잉 수
   */
  fun countByFollowerId(followerId: Long): Long

  /**
   * 특정 팔로워 ID를 가진 사용자가 팔로우하는 모든 사용자의 ID 목록을 조회합니다.
   * @param followerId 팔로우하는 사용자의 ID
   * @return 팔로우 대상 사용자의 ID 목록
   */
  @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId")
  fun findFollowingIdsByFollowerId(@Param("followerId") followerId: Long): List<Long>
}
