package com.planit.repository

import com.planit.entity.Feed
import com.planit.enums.FeedType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FeedRepository : JpaRepository<Feed, Long> {

    /**
     * 사용자별 피드 조회 (최신순)
     */
    fun findByUserIdOrderByCreatedAtDesc(
        userId: Long,
        pageable: Pageable
    ): Page<Feed>

    /**
     * 피드 타입별 조회 (최신순)
     */
    fun findByTypeOrderByCreatedAtDesc(
        type: FeedType,
        pageable: Pageable
    ): Page<Feed>

    /**
     * 팔로잉하는 사용자들의 피드 조회
     */
    @Query("""
        SELECT f FROM Feed f
        WHERE f.user.id IN (
            SELECT fw.following.id FROM Follow fw
            WHERE fw.follower.id = :userId
        )
        ORDER BY f.createdAt DESC
    """)
    fun findByFollowingUsers(
        @Param("userId") userId: Long,
        pageable: Pageable
    ): Page<Feed>

    /**
     * 읽지 않은 피드 수 조회
     */
    @Query("SELECT COUNT(f) FROM Feed f WHERE f.isRead = false AND f.user.id = :userId")
    fun countUnreadFeeds(@Param("userId") userId: Long): Long

    /**
     * 모든 피드를 읽음 처리
     */
    @Modifying
    @Query("UPDATE Feed f SET f.isRead = true WHERE f.user.id = :userId AND f.isRead = false")
    fun markAllAsRead(@Param("userId") userId: Long)

    /**
     * 특정 챌린지 관련 피드 조회
     */
    fun findByChallengeIdOrderByCreatedAtDesc(
        challengeId: Long,
        pageable: Pageable
    ): Page<Feed>
}
