package com.planit.repository

import com.planit.entity.Notification
import com.planit.enums.NotificationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * 알림(Notification) 엔티티를 관리하는 JpaRepository 인터페이스
 */
interface NotificationRepository : JpaRepository<Notification, Long> {

    /**
     * 특정 수신자의 모든 알림을 페이징하여 조회합니다.
     * @param receiverId 수신자 사용자 ID
     * @param pageable 페이징 정보
     * @return 알림 페이지
     */
    fun findAllByReceiverIdOrderByCreatedAtDesc(receiverId: Long, pageable: Pageable): Page<Notification>

    /**
     * 특정 수신자의 읽지 않은 알림을 페이징하여 조회합니다.
     * @param receiverId 수신자 사용자 ID
     * @param isRead 읽음 여부
     * @param pageable 페이징 정보
     * @return 알림 페이지
     */
    fun findAllByReceiverIdAndIsReadOrderByCreatedAtDesc(
        receiverId: Long,
        isRead: Boolean,
        pageable: Pageable
    ): Page<Notification>

    /**
     * 특정 수신자의 특정 타입 알림을 페이징하여 조회합니다.
     * @param receiverId 수신자 사용자 ID
     * @param type 알림 타입
     * @param pageable 페이징 정보
     * @return 알림 페이지
     */
    fun findAllByReceiverIdAndTypeOrderByCreatedAtDesc(
        receiverId: Long,
        type: NotificationType,
        pageable: Pageable
    ): Page<Notification>

    /**
     * 특정 수신자의 읽음 상태와 타입으로 필터링된 알림을 페이징하여 조회합니다.
     * @param receiverId 수신자 사용자 ID
     * @param isRead 읽음 여부
     * @param type 알림 타입
     * @param pageable 페이징 정보
     * @return 알림 페이지
     */
    fun findAllByReceiverIdAndIsReadAndTypeOrderByCreatedAtDesc(
        receiverId: Long,
        isRead: Boolean,
        type: NotificationType,
        pageable: Pageable
    ): Page<Notification>

    /**
     * 특정 수신자의 읽지 않은 알림 개수를 조회합니다.
     * @param receiverId 수신자 사용자 ID
     * @return 읽지 않은 알림 개수
     */
    fun countByReceiverIdAndIsRead(receiverId: Long, isRead: Boolean): Long

    /**
     * 특정 수신자의 모든 읽지 않은 알림을 읽음 상태로 일괄 업데이트합니다.
     * @param receiverId 수신자 사용자 ID
     * @return 업데이트된 레코드 수
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    fun markAllAsReadByReceiverId(receiverId: Long): Int

    /**
     * 특정 수신자의 모든 읽은 알림을 일괄 삭제합니다.
     * @param receiverId 수신자 사용자 ID
     * @return 삭제된 레코드 수
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.receiver.id = :receiverId AND n.isRead = true")
    fun deleteAllReadByReceiverId(receiverId: Long): Int

    /**
     * 특정 알림이 특정 사용자의 것인지 확인합니다.
     * @param id 알림 ID
     * @param receiverId 수신자 사용자 ID
     * @return 존재 여부
     */
    fun existsByIdAndReceiverId(id: Long, receiverId: Long): Boolean

    /**
     * 특정 수신자의 모든 알림을 삭제합니다.
     * @param receiverId 수신자 사용자 ID
     * @return 삭제된 레코드 수
     */
    fun deleteByReceiver_Id(receiverId: Long): Int

    /**
     * 특정 발신자가 보낸 알림의 발신자를 NULL로 설정합니다.
     * @param senderId 발신자 사용자 ID
     * @return 업데이트된 레코드 수
     */
    @Modifying
    @Query("UPDATE Notification n SET n.sender = null WHERE n.sender.id = :senderId")
    fun nullifySenderBySenderId(@Param("senderId") senderId: Long): Int
}
