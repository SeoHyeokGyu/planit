package com.planit.service.storage

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

/**
 * 파일 저장 처리를 담당하는 서비스 클래스입니다.
 * 업로드된 파일을 로컬 파일 시스템에 저장하고, 저장된 파일의 접근 경로를 반환합니다.
 */
@Service
class FileStorageService {
//
//    // 파일을 저장할 디렉토리 경로 (프로젝트 루트의 'uploads' 폴더)
//    private val uploadDir = Paths.get("uploads")
//
//    /**
//     * FileStorageService 초기화 시, 'uploads' 디렉토리가 없으면 생성합니다.
//     */
//    init {
//        if (!Files.exists(uploadDir)) {
//            Files.createDirectories(uploadDir)
//        }
//    }
//
//    /**
//     * MultipartFile을 받아 로컬 파일 시스템에 저장하고, 저장된 파일의 접근 URL을 반환합니다.
//     * @param file 저장할 MultipartFile (업로드된 파일)
//     * @return 저장된 파일에 접근할 수 있는 URL 경로
//     */
//    fun storeFile(file: MultipartFile): String {
//        // 파일명 충돌 방지를 위해 UUID를 사용하여 고유한 파일명 생성
//        val filename = "${UUID.randomUUID()}-${file.originalFilename}"
//        // 파일을 저장할 최종 경로
//        val targetLocation = uploadDir.resolve(filename)
//        // 파일 스트림을 복사하여 저장
//        Files.copy(file.inputStream, targetLocation)
//        // 데이터베이스에 저장할 경로 반환
//        return "/uploads/$filename"
//    }
}
