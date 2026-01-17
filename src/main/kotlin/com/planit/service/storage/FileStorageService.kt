package com.planit.service.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/** 파일 저장 처리를 담당하는 서비스 클래스입니다. */
@Service
class FileStorageService(
  @param:Value("\${file.upload-dir}") private val uploadDirStr: String,
  @param:Value("\${file.upload-url-path}") private val uploadUrlPath: String,
) {

  private val uploadDir = Paths.get(uploadDirStr)

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
   *
   * @return 설정된 URL 접두사가 포함된 파일 경로 (예: /images/2026/01/13/uuid_filename.jpg)
   */
  fun storeFile(file: MultipartFile): String {
    if (file.isEmpty) {
      throw IllegalArgumentException("빈 파일은 저장할 수 없습니다.")
    }

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
      val filename = "${UUID.randomUUID()}_${originalFilename.replace("\\s".toRegex(), "_")}"

      // 3. 저장될 절대 경로 계산
      val targetLocation = targetDir.resolve(filename).normalize().toAbsolutePath()

      if (!targetLocation.startsWith(uploadDir.toAbsolutePath())) {
        throw IllegalArgumentException("보안 위험: 허용되지 않은 경로로의 접근입니다.")
      }

      // 4. 파일 저장
      file.inputStream.use { inputStream ->
        Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
      }

      // 5. URL 반환 (예: /images/2026/01/13/filename.jpg)
      return "$uploadUrlPath/$datePath/$filename"
    } catch (e: IOException) {
      // 에러 로그에 파일명을 포함하지만 UUID는 제외하고 싶다면 originalFilename 사용
      throw RuntimeException("파일 저장 실패", e)
    }
  }
}
