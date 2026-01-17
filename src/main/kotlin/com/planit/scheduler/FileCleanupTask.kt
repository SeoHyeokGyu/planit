package com.planit.scheduler

import com.planit.repository.CertificationRepository
import com.planit.service.storage.FileStorageService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 주기적으로 파일 저장소를 정리하는 스케줄러입니다.
 */
@Component
class FileCleanupTask(
    private val certificationRepository: CertificationRepository,
    private val fileStorageService: FileStorageService
) {

    private val logger = LoggerFactory.getLogger(FileCleanupTask::class.java)

    /**
     * 매일 새벽 3시에 실행되어 고아 파일(DB에 없는 파일)을 정리합니다.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional(readOnly = true)
    fun cleanupOrphanFiles() {
        logger.info("고아 파일 정리 작업 시작")
        
        try {
            // 1. DB의 모든 사진 URL 조회 (Soft Delete된 것도 포함)
            val photoUrls = certificationRepository.findAllPhotoUrls().toSet()
            logger.info("DB에 존재하는 파일 수: ${photoUrls.size}")

            // 2. 파일 스토리지 정리 요청
            val deletedCount = fileStorageService.cleanupFiles(photoUrls)
            
            logger.info("고아 파일 정리 작업 완료 - 삭제된 파일 수: $deletedCount")
        } catch (e: Exception) {
            logger.error("고아 파일 정리 작업 중 오류 발생", e)
        }
    }
}
