package com.planit.controller

import com.planit.dto.*
import com.planit.service.CertificationService
import com.planit.service.storage.FileStorageService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/certifications")
class CertificationController(
    private val certificationService: CertificationService,
    private val fileStorageService: FileStorageService
) {

    @PostMapping
    fun createCertification(
        @RequestBody request: CertificationCreateRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<CertificationResponse>> {
        val response = certificationService.createCertification(request, userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    @PostMapping("/{id}/photo")
    fun uploadPhoto(
        @PathVariable id: Long,
        @RequestParam("file") file: MultipartFile,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<CertificationResponse>> {
        val photoUrl = fileStorageService.storeFile(file)
        val response = certificationService.uploadCertificationPhoto(id, photoUrl, userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/{id}")
    fun getCertification(@PathVariable id: Long): ResponseEntity<ApiResponse<CertificationResponse>> {
        val response = certificationService.getCertification(id)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/user/{userLoginId}")
    fun getCertificationsByUser(
        @PathVariable userLoginId: String,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<ApiResponse<PagedResponse<CertificationResponse>>> {
        val response = certificationService.getCertificationsByUser(userLoginId, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/challenge/{challengeId}")
    fun getCertificationsByChallenge(
        @PathVariable challengeId: Long,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<ApiResponse<PagedResponse<CertificationResponse>>> {
        val response = certificationService.getCertificationsByChallenge(challengeId, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PutMapping("/{id}")
    fun updateCertification(
        @PathVariable id: Long,
        @RequestBody request: CertificationUpdateRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<CertificationResponse>> {
        val response = certificationService.updateCertification(id, request, userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @DeleteMapping("/{id}")
    fun deleteCertification(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<Unit>> {
        certificationService.deleteCertification(id, userDetails.username)
        return ResponseEntity.ok(ApiResponse.success())
    }
}
