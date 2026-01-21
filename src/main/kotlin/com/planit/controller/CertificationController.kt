package com.planit.controller

import com.planit.dto.*
import com.planit.service.CertificationService
import com.planit.service.storage.FileStorageService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

/**
 * 챌린지 인증(Certification) 관련 API를 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api/certifications")
class CertificationController(
  private val certificationService: CertificationService,
  private val fileStorageService: FileStorageService
) {

  /**
   * 새로운 인증을 생성합니다. (텍스트 정보만)
   * @param request 인증 생성에 필요한 데이터 (challengeId, title, content)
   * @param userDetails 현재 로그인한 사용자 정보
   * @return 생성된 인증 정보
   */
  @PostMapping
  fun createCertification(
    @RequestBody request: CertificationCreateRequest,
    @AuthenticationPrincipal userDetails: CustomUserDetails
  ): ResponseEntity<ApiResponse<CertificationResponse>> {
    val response = certificationService.createCertification(request, userDetails.username)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  /**
   * 특정 인증에 사진을 업로드합니다.
   * 
   * [동작 과정]
   * 1. 클라이언트가 전송한 사진 파일(MultipartFile)을 받습니다.
   * 2. FileStorageService를 통해 파일을 물리적 저장소에 저장하고, 웹 접근 URL을 반환받습니다.
   * 3. CertificationService를 통해 Gemini 분석을 수행합니다.
   * 4. 반환받은 URL과 분석 결과를 CertificationService를 통해 해당 인증 데이터에 업데이트합니다.
   * 
   * @param id 사진을 추가할 인증의 ID
   * @param file 업로드할 사진 파일 (form-data key: "file")
   * @param userDetails 현재 로그인한 사용자 정보
   * @return 사진 정보가 업데이트된 최종 인증 정보
   */
  @PostMapping("/{id}/photo")
  fun uploadPhoto(
    @PathVariable id: Long,
    @RequestParam("file") file: MultipartFile,
    @AuthenticationPrincipal userDetails: CustomUserDetails
  ): ResponseEntity<ApiResponse<CertificationResponse>> {
    val response = certificationService.processCertificationPhoto(id, file, userDetails.username)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  /**
   * 특정 인증의 사진을 삭제합니다.
   * @param id 사진을 삭제할 인증의 ID
   * @param userDetails 현재 로그인한 사용자 정보
   * @return 사진 정보가 삭제된 인증 정보
   */
  @DeleteMapping("/{id}/photo")
  fun deletePhoto(
    @PathVariable id: Long,
    @AuthenticationPrincipal userDetails: CustomUserDetails
  ): ResponseEntity<ApiResponse<CertificationResponse>> {
    val response = certificationService.deleteCertificationPhoto(id, userDetails.username)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  /**
   * 특정 ID의 인증 정보를 조회합니다.
   * @param id 조회할 인증의 ID
   * @return 조회된 인증 정보
   */
  @GetMapping("/{id}")
  fun getCertification(@PathVariable id: Long): ResponseEntity<ApiResponse<CertificationResponse>> {
    val response = certificationService.getCertification(id)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  /**
   * 특정 사용자가 작성한 인증 목록을 페이징하여 조회합니다.
   * @param userLoginId 인증 목록을 조회할 사용자의 로그인 ID
   * @param pageable 페이징 정보 (기본 10개)
   * @return 페이징된 인증 목록
   */
  @GetMapping("/user/{userLoginId}")
  fun getCertificationsByUser(
    @PathVariable userLoginId: String,
    @PageableDefault(size = 10) pageable: Pageable
  ): ResponseEntity<ApiResponse<List<CertificationResponse>>> {
    val certificationPage = certificationService.getCertificationsByUser(userLoginId, pageable)
    val certificationResponses = certificationPage.content.map { CertificationResponse.from(it) }
    return ResponseEntity.ok(ApiResponse.pagedSuccess(certificationResponses, certificationPage))
  }

  /**
   * 특정 사용자가 특정 기간 내에 작성한 인증 목록을 조회합니다.
   * @param userLoginId 인증 목록을 조회할 사용자의 로그인 ID
   * @param from 시작 일시 (ISO Date Time)
   * @param to 종료 일시 (ISO Date Time)
   * @return 인증 목록
   */
  @GetMapping("/user/{userLoginId}/date-range")
  fun getCertificationsByDateRange(
    @PathVariable userLoginId: String,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime
  ): ResponseEntity<ApiResponse<List<CertificationResponse>>> {
    val responses = certificationService.getCertificationsByDateRange(userLoginId, from, to)
    return ResponseEntity.ok(ApiResponse.success(responses))
  }

  /**
   * 특정 챌린지에 속한 인증 목록을 페이징하여 조회합니다.
   * @param challengeId 인증 목록을 조회할 챌린지의 ID
   * @param pageable 페이징 정보 (기본 10개)
   * @return 페이징된 인증 목록
   */
  @GetMapping("/challenge/{challengeId}")
  fun getCertificationsByChallenge(
    @PathVariable challengeId: String,
    @PageableDefault(size = 10) pageable: Pageable
  ): ResponseEntity<ApiResponse<List<CertificationResponse>>> {
    val certificationPage = certificationService.getCertificationsByChallenge(challengeId, pageable)
    val certificationResponses = certificationPage.content.map { CertificationResponse.from(it) }
    return ResponseEntity.ok(ApiResponse.pagedSuccess(certificationResponses, certificationPage))
  }

  /**
   * 특정 인증의 정보를 수정합니다. (제목, 내용)
   * @param id 수정할 인증의 ID
   * @param request 수정할 인증 데이터
   * @param userDetails 현재 로그인한 사용자 정보
   * @return 수정된 인증 정보
   */
  @PutMapping("/{id}")
  fun updateCertification(
    @PathVariable id: Long,
    @RequestBody request: CertificationUpdateRequest,
    @AuthenticationPrincipal userDetails: CustomUserDetails
  ): ResponseEntity<ApiResponse<CertificationResponse>> {
    val response = certificationService.updateCertification(id, request, userDetails.username)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  /**
   * 특정 인증을 삭제합니다. (Soft Delete)
   * @param id 삭제할 인증의 ID
   * @param userDetails 현재 로그인한 사용자 정보
   * @return 성공 응답
   */
  @DeleteMapping("/{id}")
  fun deleteCertification(
    @PathVariable id: Long,
    @AuthenticationPrincipal userDetails: CustomUserDetails
  ): ResponseEntity<ApiResponse<Unit>> {
    certificationService.deleteCertification(id, userDetails.username)
    return ResponseEntity.ok(ApiResponse.success())
  }
}