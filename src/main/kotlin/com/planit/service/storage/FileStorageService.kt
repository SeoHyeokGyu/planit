package com.planit.service.storage

import net.coobird.thumbnailator.Thumbnails
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.streams.toList

/** 파일 저장 처리를 담당하는 서비스 클래스입니다. */
@Service
class FileStorageService(
  @param:Value("\${file.upload-dir}") private val uploadDirStr: String,
  @param:Value("\${file.upload-url-path}") private val uploadUrlPath: String,
) {

  private val uploadDir = Paths.get(uploadDirStr)
  
  companion object {
    private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    private const val MAX_IMAGE_SIZE = 1600
    private const val IMAGE_QUALITY = 0.8
  }

  init {
    try {
      if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir)
      }
    } catch (e: IOException) {
      throw RuntimeException("파일 저장 경로를 초기화할 수 없습니다.", e)
    }
  }

  /**
   * 파일을 날짜별 하위 디렉토리(yyyy/MM/dd)에 저장하고, 접근 가능한 URL을 반환합니다.
   * 이미지는 리사이징 및 압축되어 저장됩니다.
   *
   * @return 설정된 URL 접두사가 포함된 파일 경로 (예: /images/2026/01/13/uuid_filename.jpg)
   * @throws IllegalArgumentException 허용되지 않는 파일 형식이거나 빈 파일인 경우
   */
  fun storeFile(file: MultipartFile): String {
    if (file.isEmpty) {
      throw IllegalArgumentException("빈 파일은 저장할 수 없습니다.")
    }

    validateImageFile(file)

    // 1. 날짜별 하위 디렉토리 경로 생성 (예: 2026/01/13)
    val today = LocalDate.now()
    val datePath = today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    val targetDir = uploadDir.resolve(datePath)

    try {
      // 해당 날짜의 디렉토리가 없으면 생성
      if (!Files.exists(targetDir)) {
        Files.createDirectories(targetDir)
      }

      // 2. 고유한 파일명 생성
      val originalFilename = file.originalFilename ?: "unknown"
      val extension = getExtension(originalFilename)
      val filename = "${UUID.randomUUID()}.$extension"

      // 3. 저장될 절대 경로 계산
      val targetLocation = targetDir.resolve(filename).normalize().toAbsolutePath()

      if (!targetLocation.startsWith(uploadDir.toAbsolutePath())) {
        throw IllegalArgumentException("보안 위험: 허용되지 않은 경로로의 접근입니다.")
      }

      // 4. 파일 저장 (리사이징 및 압축 적용)
      // Gif는 애니메이션 유지를 위해 원본 저장하거나 별도 처리 필요하지만, 여기서는 단순화하여 원본 저장 혹은 처리
      // Thumbnailator는 Gif 리사이징 시 첫 프레임만 남을 수 있음. Gif는 예외적으로 원본 저장 추천.
      if (extension.equals("gif", ignoreCase = true)) {
        file.inputStream.use { inputStream ->
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
        }
      } else {
        Thumbnails.of(file.inputStream)
            .size(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
            .outputQuality(IMAGE_QUALITY)
            .toFile(targetLocation.toFile())
      }

      // 5. URL 반환 (예: /images/2026/01/13/filename.jpg)
      return "$uploadUrlPath/$datePath/$filename"
    } catch (e: IOException) {
      throw RuntimeException("파일 저장 실패", e)
    }
  }

  private fun validateImageFile(file: MultipartFile) {
      val filename = file.originalFilename ?: ""
      val extension = getExtension(filename)
      
      if (!ALLOWED_EXTENSIONS.contains(extension.lowercase())) {
          throw IllegalArgumentException("허용되지 않는 파일 형식입니다. 허용된 형식: $ALLOWED_EXTENSIONS")
      }
      
      val contentType = file.contentType ?: ""
      if (!contentType.startsWith("image/")) {
           throw IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.")
      }
  }

  private fun getExtension(filename: String): String {
      val lastDotIndex = filename.lastIndexOf('.')
      return if (lastDotIndex == -1) "" else filename.substring(lastDotIndex + 1)
  }

  /**
   * 주어진 URL에 해당하는 파일을 삭제합니다.
   *
   * @param fileUrl 삭제할 파일의 URL (예: /images/2026/01/13/filename.jpg)
   */
  fun deleteFile(fileUrl: String) {
    if (fileUrl.isBlank()) return

    try {
      // URL에서 경로 부분만 추출 (예: /images/2026/01/13/filename.jpg -> 2026/01/13/filename.jpg)
      // uploadUrlPath (/images) 제거
      val relativePathStr = if (fileUrl.startsWith(uploadUrlPath)) {
        fileUrl.substring(uploadUrlPath.length).trimStart('/')
      } else {
        // URL 형식이 맞지 않으면 경고 로그 남기고 종료하거나 예외 처리
        // 여기서는 안전하게 무시하거나 로그만 남김
        return
      }

      val filePath = uploadDir.resolve(relativePathStr).normalize().toAbsolutePath()

      // 경로 조작 방지 검사
      if (!filePath.startsWith(uploadDir.toAbsolutePath())) {
        throw IllegalArgumentException("잘못된 파일 경로입니다.")
      }

      Files.deleteIfExists(filePath)
    } catch (e: IOException) {
      // 파일 삭제 실패 시 로그 남김 (실패해도 비즈니스 로직을 중단할지는 결정 필요)
      // 여기서는 로그만 남기고 진행
      System.err.println("파일 삭제 실패: $fileUrl, ${e.message}")
    }
  }

  /**
   * 주어진 URL에 해당하는 파일 객체를 반환합니다.
   *
   * @param fileUrl 파일 URL (예: /images/2026/01/13/filename.jpg)
   * @return File 객체
   * @throws IllegalArgumentException 잘못된 경로이거나 파일이 존재하지 않는 경우
   */
  fun getFile(fileUrl: String): File {
    if (fileUrl.isBlank()) throw IllegalArgumentException("파일 URL이 비어있습니다.")

    val relativePathStr = if (fileUrl.startsWith(uploadUrlPath)) {
      fileUrl.substring(uploadUrlPath.length).trimStart('/')
    } else {
      throw IllegalArgumentException("잘못된 파일 URL 형식입니다.")
    }

    val filePath = uploadDir.resolve(relativePathStr).normalize().toAbsolutePath()

    if (!filePath.startsWith(uploadDir.toAbsolutePath())) {
      throw IllegalArgumentException("잘못된 파일 경로입니다.")
    }

    val file = filePath.toFile()
    if (!file.exists() || !file.isFile) {
      throw IllegalArgumentException("파일을 찾을 수 없습니다.")
    }

    return file
  }

  /**
   * DB에 존재하지 않는 고아 파일들을 정리합니다.
   * 생성된 지 24시간이 지난 파일 중 validFileUrls에 없는 파일을 삭제합니다.
   *
   * @param validFileUrls DB에 존재하는 유효한 파일 URL 목록 (예: /images/2026/01/13/xxx.jpg)
   * @return 삭제된 파일 개수
   */
  fun cleanupFiles(validFileUrls: Set<String>): Int {
    if (!Files.exists(uploadDir)) return 0

    var deletedCount = 0
    val retentionPeriod = Instant.now().minus(24, ChronoUnit.HOURS)

    try {
      // uploadDir 하위의 모든 파일 탐색
      Files.walk(uploadDir)
        .filter { Files.isRegularFile(it) } // 파일만 대상
        .forEach { filePath ->
          try {
            // 1. 파일 생성 시간 확인 (24시간 이내 생성된 파일은 건너뜀)
            val attr = Files.readAttributes(filePath, BasicFileAttributes::class.java)
            val creationTime = attr.creationTime().toInstant()

            if (creationTime.isBefore(retentionPeriod)) {
              // 2. 파일 경로 -> URL 변환
              // 절대 경로: /app/uploads/2026/01/13/file.jpg
              // uploadDir: /app/uploads
              // 상대 경로: 2026/01/13/file.jpg
              val relativePath = uploadDir.relativize(filePath)
              // URL: /images/2026/01/13/file.jpg  (윈도우의 경우 역슬래시를 슬래시로 변환 필요)
              val fileUrl = "$uploadUrlPath/${relativePath.toString().replace("\\", "/")}"

              // 3. 유효 목록에 없으면 삭제
              if (!validFileUrls.contains(fileUrl)) {
                Files.delete(filePath)
                deletedCount++
                // 빈 디렉토리 정리 로직은 복잡해지므로 생략 (필요 시 추가 가능)
              }
            }
          } catch (e: Exception) {
            System.err.println("파일 정리 중 오류 발생: $filePath, ${e.message}")
          }
        }
    } catch (e: IOException) {
      System.err.println("파일 탐색 중 오류 발생: ${e.message}")
    }

    return deletedCount
  }
}
