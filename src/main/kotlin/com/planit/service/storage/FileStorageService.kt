package com.planit.service.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
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
   * 파일을 저장하고, 접근 가능한 URL을 반환합니다.
   *
   * @return 설정된 URL 접두사가 포함된 파일 경로 (예: /images/uuid_filename.jpg)
   */
  fun storeFile(file: MultipartFile): String {
    if (file.isEmpty) {
      throw IllegalArgumentException("빈 파일은 저장할 수 없습니다.")
    }

    val originalFilename = file.originalFilename ?: "unknown"
    val filename = "${UUID.randomUUID()}_${originalFilename.replace("\\s".toRegex(), "_")}"

    try {
      val targetLocation = uploadDir.resolve(filename).normalize().toAbsolutePath()

      if (!targetLocation.startsWith(uploadDir.toAbsolutePath())) {
        throw IllegalArgumentException("보안 위험: 허용되지 않은 경로로의 접근입니다.")
      }

      file.inputStream.use { inputStream ->
        Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
      }

      // 하드코딩 대신 설정된 URL 접두사를 사용하여 반환합니다.
      return "$uploadUrlPath/$filename"
    } catch (e: IOException) {
      throw RuntimeException("파일 저장 실패: $filename", e)
    }
  }
}
