package com.planit.service.storage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.temporal.ChronoUnit

class FileStorageServiceTest {

  @TempDir lateinit var tempDir: Path

  private lateinit var fileStorageService: FileStorageService
  private lateinit var uploadDir: Path
  private val uploadUrlPath = "/images"

  @BeforeEach
  fun setUp() {
    uploadDir = tempDir.resolve("uploads")
    fileStorageService = FileStorageService(uploadDir.toString(), uploadUrlPath)
  }

  @Test
  fun `storeFile_파일을_저장하고_URL을_반환한다`() {
    // given
    val content = "Hello, World!"
    val file = MockMultipartFile("file", "test.txt", "text/plain", content.toByteArray())

    // when
    val fileUrl = fileStorageService.storeFile(file)

    // then
    assertThat(fileUrl).startsWith(uploadUrlPath)

    // 실제 파일 존재 확인
    val relativePath = fileUrl.substring(uploadUrlPath.length).trimStart('/')
    val filePath = uploadDir.resolve(relativePath)
    assertTrue(Files.exists(filePath))
  }

  @Test
  fun `deleteFile_파일을_삭제한다`() {
    // given
    val content = "Delete me"
    val file = MockMultipartFile("file", "delete.txt", "text/plain", content.toByteArray())
    val fileUrl = fileStorageService.storeFile(file)

    // when
    fileStorageService.deleteFile(fileUrl)

    // then
    val relativePath = fileUrl.substring(uploadUrlPath.length).trimStart('/')
    val filePath = uploadDir.resolve(relativePath)
    assertFalse(Files.exists(filePath))
  }

  @Test
  fun `cleanupFiles_고아_파일을_정리한다`() {
    // given
    val now = Instant.now()
    val oldTime = now.minus(25, ChronoUnit.HOURS) // 25시간 전

    // 1. 유효한 파일 (DB에 있음)
    val validFile = createDummyFile("valid.jpg", oldTime)
    val validUrl = toUrl(validFile)

    // 2. 고아 파일 (DB에 없음, 24시간 지남)
    val orphanFile = createDummyFile("orphan.jpg", oldTime)

    // 3. 최신 파일 (DB에 없음, 24시간 이내 - 삭제되면 안됨)
    val recentFile = createDummyFile("recent.jpg", now)

    val validUrls = setOf(validUrl)

    // when
    val deletedCount = fileStorageService.cleanupFiles(validUrls)

    // then
    assertThat(deletedCount).isEqualTo(1)
    assertTrue(Files.exists(validFile), "유효한 파일은 유지되어야 함")
    assertFalse(Files.exists(orphanFile), "오래된 고아 파일은 삭제되어야 함")
    assertTrue(Files.exists(recentFile), "최신 파일은 유지되어야 함")
  }

  private fun createDummyFile(filename: String, creationTime: Instant): Path {
    // 날짜별 디렉토리 구조 흉내 (오늘 날짜 사용)
    // 실제 로직은 날짜별로 폴더를 만들지만, 테스트 단순화를 위해 uploadDir 직속 혹은 임의 폴더 사용 가능
    // 하지만 cleanupFiles 로직이 하위 폴더를 탐색하므로 그냥 uploadDir 바로 아래에 만들어도 무방하나
    // 실제 구조와 유사하게 하기 위해 서브 디렉토리 하나 생성
    val subDir = uploadDir.resolve("2026/01/01")
    Files.createDirectories(subDir)

    val file = subDir.resolve(filename)
    Files.writeString(file, "dummy content")

    // 생성 시간 조작
    Files.setAttribute(file, "creationTime", FileTime.from(creationTime))

    return file
  }

  private fun toUrl(path: Path): String {
    val relativePath = uploadDir.relativize(path)
    return "$uploadUrlPath/${relativePath.toString().replace("\\", "/")}"
  }
}
