package com.planit.scheduler

import com.planit.repository.CertificationRepository
import com.planit.service.storage.FileStorageService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FileCleanupTaskTest {

    @MockK
    private lateinit var certificationRepository: CertificationRepository

    @MockK
    private lateinit var fileStorageService: FileStorageService

    @InjectMockKs
    private lateinit var fileCleanupTask: FileCleanupTask

    @Test
    @DisplayName("cleanupOrphanFiles_정상적으로_고아_파일_정리를_수행한다")
    fun cleanupOrphanFiles() {
        // given
        val photoUrls = listOf("/images/1.jpg", "/images/2.jpg")
        every { certificationRepository.findAllPhotoUrls() } returns photoUrls
        every { fileStorageService.cleanupFiles(any()) } returns 5 // 5개 삭제됨

        // when
        fileCleanupTask.cleanupOrphanFiles()

        // then
        verify(exactly = 1) { certificationRepository.findAllPhotoUrls() }
        verify(exactly = 1) { fileStorageService.cleanupFiles(photoUrls.toSet()) }
    }
}
