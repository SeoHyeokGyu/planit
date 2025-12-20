package com.planit.service

import com.planit.dto.UserProfileResponse
import com.planit.dto.NotificationDto
import com.planit.entity.Follow
import com.planit.repository.FollowRepository
import com.planit.repository.UserRepository
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.planit.exception.UserNotFoundException
import java.time.LocalDateTime
import java.util.UUID

/**
 * 팔로우(Follow) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 팔로우/언팔로우, 팔로워/팔로잉 수 조회 및 목록 조회 기능을 제공합니다.
 * 팔로우 카운트는 Redis를 이용하여 캐싱합니다.
 */
@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val cacheManager: CacheManager,
    private val notificationService: NotificationService
) {

    /**
     * 특정 사용자가 다른 사용자를 팔로우하도록 처리합니다.
     * @param followerLoginId 팔로우를 요청한 사용자의 로그인 ID
     * @param followingLoginId 팔로우 대상 사용자의 로그인 ID
     * @throws IllegalArgumentException 자기 자신을 팔로우하려고 할 때
     * @throws UserNotFoundException 사용자(팔로워 또는 팔로잉 대상)를 찾을 수 없을 때
     * @throws IllegalStateException 이미 팔로우한 사용자일 때
     */
    @Transactional
    fun follow(followerLoginId: String, followingLoginId: String) {
        if (followerLoginId.contentEquals(followingLoginId)) {
            throw IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.")
        }
        val follower =
            userRepository.findByLoginId(followerLoginId)
                ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $followerLoginId")
        val following =
            userRepository.findByLoginId(followingLoginId)
                ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $followingLoginId")

        if (followRepository.existsByFollowerIdAndFollowingId(follower.id!!, following.id!!)) {
            throw IllegalStateException("이미 팔로우한 사용자입니다.")
        }

        followRepository.save(Follow(follower = follower, following = following))

        // 팔로우 카운트 캐시 업데이트: 팔로우한 사람의 팔로잉 수 증가, 팔로우 당한 사람의 팔로워 수 증가
        incrementCacheValue("followingCount", followerLoginId)
        incrementCacheValue("followerCount", followingLoginId)

        // 알림 전송
        notificationService.sendNotification(
            followingLoginId,
            NotificationDto(
                id = UUID.randomUUID().toString(),
                type = "INFO",
                message = "${follower.nickname}님이 회원님을 팔로우하기 시작했습니다.",
                createdAt = LocalDateTime.now()
            )
        )
    }

    /**
     * 특정 사용자가 다른 사용자를 언팔로우하도록 처리합니다.
     * @param followerLoginId 언팔로우를 요청한 사용자의 로그인 ID
     * @param followingLoginId 언팔로우 대상 사용자의 로그인 ID
     * @throws UserNotFoundException 사용자(팔로워 또는 팔로잉 대상)를 찾을 수 없을 때
     * @throws IllegalStateException 팔로우 관계가 존재하지 않을 때
     */
    @Transactional
    fun unfollow(followerLoginId: String, followingLoginId: String) {
        val follower =
            userRepository.findByLoginId(followerLoginId)
                ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $followerLoginId")
        val following =
            userRepository.findByLoginId(followingLoginId)
                ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $followingLoginId")

        val follow =
            followRepository.findByFollowerIdAndFollowingId(follower.id!!, following.id!!)
                ?: throw IllegalStateException("팔로우 관계가 존재하지 않습니다.")
        followRepository.delete(follow)

        // 팔로우 카운트 캐시 업데이트: 언팔로우한 사람의 팔로잉 수 감소, 언팔로우 당한 사람의 팔로워 수 감소
        decrementCacheValue("followingCount", followerLoginId)
        decrementCacheValue("followerCount", followingLoginId)
    }

    /**
     * 특정 사용자의 팔로워 수를 조회합니다. (캐시 우선)
     * @param userLoginId 팔로워 수를 조회할 사용자의 로그인 ID
     * @return 팔로워 수
     * @throws UserNotFoundException 사용자를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    fun getFollowerCount(userLoginId: String): Long {
        // 캐시에서 먼저 조회
        val cache = cacheManager.getCache("followerCount")
        val cachedValue = cache?.get(userLoginId)?.get()
        if (cachedValue != null) {
            return when (cachedValue) {
                is Long -> cachedValue
                is Number -> cachedValue.toLong()
                else -> cachedValue.toString().toLong()
            }
        }

        // 캐시에 없으면 DB에서 조회 후 캐시에 저장
        val user =
            userRepository.findByLoginId(userLoginId)
                ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")
        val dbCount = followRepository.countByFollowingId(user.id!!)
        cache?.put(userLoginId, dbCount)
        return dbCount
    }

    /**
     * 특정 사용자의 팔로잉 수를 조회합니다. (캐시 우선)
     * @param userLoginId 팔로잉 수를 조회할 사용자의 로그인 ID
     * @return 팔로잉 수
     * @throws UserNotFoundException 사용자를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    fun getFollowingCount(userLoginId: String): Long {
        // 캐시에서 먼저 조회
        val cache = cacheManager.getCache("followingCount")
        val cachedValue = cache?.get(userLoginId)?.get()
        if (cachedValue != null) {
            return when (cachedValue) {
                is Long -> cachedValue
                is Number -> cachedValue.toLong()
                else -> cachedValue.toString().toLong()
            }
        }

        // 캐시에 없으면 DB에서 조회 후 캐시에 저장
        val user =
            userRepository.findByLoginId(userLoginId)
                ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")
        val dbCount = followRepository.countByFollowerId(user.id!!)
        cache?.put(userLoginId, dbCount)
        return dbCount
    }

    /**
     * 특정 사용자를 팔로우하는 사용자 목록(팔로워 목록)을 페이징하여 조회합니다.
     * @param userLoginId 팔로워 목록을 조회할 사용자의 로그인 ID
     * @param pageable 페이징 정보
     * @return 팔로워 사용자 DTO 페이지
     * @throws UserNotFoundException 사용자를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    fun getFollowers(userLoginId: String, pageable: Pageable): Page<UserProfileResponse> {
        val user = userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")
        val followerPage = followRepository.findAllByFollowingId(user.id!!, pageable)
        return followerPage.map { follow -> UserProfileResponse.of(follow.follower) }
    }

    /**
     * 특정 사용자가 팔로우하는 사용자 목록(팔로잉 목록)을 페이징하여 조회합니다.
     * @param userLoginId 팔로잉 목록을 조회할 사용자의 로그인 ID
     * @param pageable 페이징 정보
     * @return 팔로잉 사용자 DTO 페이지
     * @throws UserNotFoundException 사용자를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    fun getFollowings(userLoginId: String, pageable: Pageable): Page<UserProfileResponse> {
        val user = userRepository.findByLoginId(userLoginId)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다: $userLoginId")
        val followingPage = followRepository.findAllByFollowerId(user.id!!, pageable)
        return followingPage.map { follow -> UserProfileResponse.of(follow.following) }
    }

    /**
     * 캐시의 특정 키에 해당하는 값을 1 증가시킵니다.
     * 캐시에 값이 없을 경우 (초기화) 이 메서드만으로는 캐시가 생성되지 않고,
     * get*Count 메서드를 통해 DB에서 조회 후 캐시가 채워집니다.
     * @param cacheName 캐시 이름 (예: "followingCount", "followerCount")
     * @param key 캐시 키 (사용자 로그인 ID)
     */
    private fun incrementCacheValue(cacheName: String, key: String) {
        val cache = cacheManager.getCache(cacheName)
        // 캐시에 값이 존재할 경우에만 업데이트
        val cachedValue = cache?.get(key)?.get()
        if (cachedValue != null) {
            val currentValue = when (cachedValue) {
                is Long -> cachedValue
                is Number -> cachedValue.toLong()
                else -> cachedValue.toString().toLong()
            }
            cache.put(key, currentValue + 1)
        }
    }

    /**
     * 캐시의 특정 키에 해당하는 값을 1 감소시킵니다.
     * 캐시에 값이 없을 경우 (초기화) 이 메서드만으로는 캐시가 생성되지 않고,
     * get*Count 메서드를 통해 DB에서 조회 후 캐시가 채워집니다.
     * @param cacheName 캐시 이름
     * @param key 캐시 키
     */
    private fun decrementCacheValue(cacheName: String, key: String) {
        val cache = cacheManager.getCache(cacheName)
        // 캐시에 값이 존재할 경우에만 업데이트
        val cachedValue = cache?.get(key)?.get()
        if (cachedValue != null) {
            val currentValue = when (cachedValue) {
                is Long -> cachedValue
                is Number -> cachedValue.toLong()
                else -> cachedValue.toString().toLong()
            }
            cache.put(key, currentValue - 1)
        }
    }
}
