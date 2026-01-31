package com.planit.service.storage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.mock.web.MockMultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.imageio.ImageIO

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
    val content = createTestImage()
    val file = MockMultipartFile("file", "test.jpg", "image/jpeg", content)

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
  fun `storeFile_잘못된_확장자_예외발생`() {
    // given
    val content = "Invalid content".toByteArray()
    val file = MockMultipartFile("file", "test.txt", "text/plain", content)

    // when & then
    assertThrows(IllegalArgumentException::class.java) {
      fileStorageService.storeFile(file)
    }
  }

  @Test
  fun `storeFile_잘못된_MIME타입_예외발생`() {
    // given
    val content = createTestImage()
    // 확장자는 jpg지만 mime type이 text/plain인 경우
    val file = MockMultipartFile("file", "test.jpg", "text/plain", content)

    // when & then
    assertThrows(IllegalArgumentException::class.java) {
      fileStorageService.storeFile(file)
    }
  }

  @Test
  fun `deleteFile_파일을_삭제한다`() {
    // given
    val content = createTestImage()
    val file = MockMultipartFile("file", "delete.png", "image/png", content)
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

  private fun createTestImage(): ByteArray {
    val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()
    graphics.dispose()

    val baos = ByteArrayOutputStream()
    ImageIO.write(image, "jpg", baos)
    return baos.toByteArray()
  }

  private fun createDummyFile(filename: String, creationTime: Instant): Path {
    val subDir = uploadDir.resolve("2026/01/01")
    Files.createDirectories(subDir)

    val file = subDir.resolve(filename)
    // 썸네일 생성기가 읽을 수 있는 유효한 이미지 데이터 쓰기
    Files.write(file, createTestImage())

    // 생성 시간 조작
    Files.setAttribute(file, "creationTime", FileTime.from(creationTime))

    return file
  }

  private fun toUrl(path: Path): String {
    val relativePath = uploadDir.relativize(path)
    return "$uploadUrlPath/${relativePath.toString().replace("\\", "/")}"
  }

  @Test
  fun `storeFile_빈_파일_예외발생`() {
    val file = MockMultipartFile("file", "empty.jpg", "image/jpeg", ByteArray(0))
    assertThrows(IllegalArgumentException::class.java) {
      fileStorageService.storeFile(file)
    }
  }

  @Test
  fun `storeFile_GIF_파일_원본_저장_분기`() {
    val content = "GIF89a".toByteArray()
    val file = MockMultipartFile("file", "test.gif", "image/gif", content)
    val fileUrl = fileStorageService.storeFile(file)
    assertThat(fileUrl).endsWith(".gif")
  }

  @Test
  fun `getFile_성공`() {
    val content = createTestImage()
    val file = MockMultipartFile("file", "get.jpg", "image/jpeg", content)
    val fileUrl = fileStorageService.storeFile(file)
    
    val retrieved = fileStorageService.getFile(fileUrl)
    assertTrue(retrieved.exists())
  }

  @Test
  fun `getFile_실패_케이스들`() {
    assertThrows(IllegalArgumentException::class.java) { fileStorageService.getFile("") }
    assertThrows(IllegalArgumentException::class.java) { fileStorageService.getFile("/invalid/path") }
    assertThrows(IllegalArgumentException::class.java) { fileStorageService.getFile("/images/notexist.jpg") }
  }

  @Test
  fun `deleteFile_잘못된_경로_조작_방지`() {
    assertThrows(IllegalArgumentException::class.java) {
      fileStorageService.deleteFile("/images/../etc/passwd")
    }
  }

  @Test
  fun `deleteFile_URL_형식_안맞으면_무시`() {
    fileStorageService.deleteFile("/wrong/prefix/test.jpg")
    // No exception
  }
}
