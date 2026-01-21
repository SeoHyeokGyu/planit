package com.planit.service

import com.planit.dto.CertificationCreateRequest
import com.planit.dto.CertificationResponse
import com.planit.dto.CertificationUpdateRequest
import com.planit.entity.Certification
import com.planit.enums.BadgeType
import com.planit.exception.*
import com.planit.repository.CertificationRepository
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.ChallengeRepository
import com.planit.repository.UserRepository
import com.planit.service.badge.BadgeService
import com.planit.service.storage.FileStorageService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

/** 챌린지 인증(Certification)과 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다. */
@Service
class CertificationService(
  private val certificationRepository: CertificationRepository,
  private val userRepository: UserRepository,
  private val challengeRepository: ChallengeRepository,
  private val participantRepository: ChallengeParticipantRepository,
  private val rewardService: RewardService,
  private val badgeService: BadgeService,
  private val streakService: StreakService,
  private val fileStorageService: FileStorageService,
  private val geminiService: GeminiService
) {

  private val logger = LoggerFactory.getLogger(CertificationService::class.java)

  /**
   * Gemini를 사용하여 인증 사진을 분석합니다. (별도 메소드로 분리)
   */
  fun analyzeCertificationPhoto(
    certificationId: Long,
    file: MultipartFile
  ): String {
    val certification = certificationRepository.findById(certificationId)
      .orElseThrow { CertificationNotFoundException() }

    val prompt = """
            이 사진이 다음 챌린지 주제에 적합한지 판단해줘.
            챌린지 제목: ${certification.challenge.title}
            
            답변은 짧게 다음 형식으로만 해줘:
            적합 여부: [예/아니오]
            이유: [한 문장으로 설명]
        """.trimIndent()

    return try {
      geminiService.analyzeImage(prompt, file)
    } catch (e: Exception) {
      logger.error("Gemini 이미지 분석 실패", e)
      "이미지 분석을 완료할 수 없습니다."
    }
  }

  /**
   * 새로운 인증을 생성합니다.
   *
   * @param request 인증 생성에 필요한 데이터 (챌린지 ID, 제목, 내용)
   * @param userLoginId 현재 로그인한 사용자의 ID
   * @return 생성된 인증의 응답 객체
   * @throws UserNotFoundException 사용자를 찾을 수 없을 때
   * @throws ChallengeNotFoundException 챌린지를 찾을 수 없을 때
   */
  @Transactional
  fun createCertification(
    request: CertificationCreateRequest,
    userLoginId: String,
  ): CertificationResponse {
    val user = userRepository.findByLoginId(userLoginId) ?: throw UserNotFoundException()
    val challenge =
      challengeRepository.findById(request.challengeId).orElseThrow { ChallengeNotFoundException() }

    val now = LocalDateTime.now()
    if (challenge.startDate.isAfter(now)) {
      throw CertificationNotStartedException()
    }
    if (challenge.endDate.isBefore(now)) {
      throw CertificationEndedException()
    }

    val certification =
      Certification(
        user = user,
        challenge = challenge,
        title = request.title,
        content = request.content,
      )

    val savedCertification = certificationRepository.save(certification)

    // 챌린지 전체 인증 수 증가
    challenge.certificationCnt++
    challengeRepository.save(challenge)

    // 참여자별 인증 수 증가
    participantRepository.findByIdAndLoginId(challenge.id, userLoginId).ifPresent { participant ->
      participant.certificationCnt++
      participantRepository.save(participant)
    }

    // 인증 보상 지급 (경험치 +15, 포인트 +10)
    rewardService.grantCertificationReward(userLoginId)

    // 배지 획득 조건 체크
    badgeService.checkAndAwardBadges(user, BadgeType.CERTIFICATION_COUNT)

    // 스트릭 기록 추가
    try {
      streakService.recordCertification(challenge.id, userLoginId)
    } catch (e: Exception) {
      logger.error("스트릭 기록 실패 - challengeId: ${challenge.id}, loginId: $userLoginId", e)
    }

    return CertificationResponse.from(savedCertification)
  }

  /**
   * 특정 인증 ID로 인증 정보를 조회합니다.
   *
   * @param certificationId 조회할 인증의 ID
   * @return 조회된 인증의 응답 객체
   * @throws CertificationNotFoundException 인증을 찾을 수 없을 때
   */
  @Transactional(readOnly = true)
  fun getCertification(certificationId: Long): CertificationResponse {
    val certification =
      certificationRepository.findById(certificationId).orElseThrow {
        CertificationNotFoundException()
      }
    return CertificationResponse.from(certification)
  }

  /**
   * 특정 사용자가 작성한 인증 목록을 페이징하여 조회합니다.
   *
   * @param userLoginId 인증 목록을 조회할 사용자의 로그인 ID
   * @param pageable 페이징 정보
   * @return 페이징된 인증 엔티티 목록 (`Page<Certification>`)
   */
  @Transactional(readOnly = true)
  fun getCertificationsByUser(userLoginId: String, pageable: Pageable): Page<Certification> {
    return certificationRepository.findByUser_LoginIdOrderByCreatedAtDesc(userLoginId, pageable)
  }

  /**
   * 특정 챌린지에 속한 인증 목록을 페이징하여 조회합니다.
   *
   * @param challengeId 인증 목록을 조회할 챌린지의 ID
   * @param pageable 페이징 정보
   * @return 페이징된 인증 엔티티 목록 (`Page<Certification>`)
   */
  @Transactional(readOnly = true)
  fun getCertificationsByChallenge(challengeId: String, pageable: Pageable): Page<Certification> {
    return certificationRepository.findByChallenge_Id(challengeId, pageable)
  }

  /**
   * 특정 사용자가 특정 기간 내에 작성한 인증 목록을 조회합니다.
   *
   * @param userLoginId 사용자 로그인 ID
   * @param start 시작 일시
   * @param end 종료 일시
   * @return 인증 목록 응답 객체 리스트
   */
  @Transactional(readOnly = true)
  fun getCertificationsByDateRange(
    userLoginId: String,
    start: LocalDateTime,
    end: LocalDateTime,
  ): List<CertificationResponse> {
    val certifications =
      certificationRepository.findByUser_LoginIdAndCreatedAtBetween(userLoginId, start, end)
    return certifications.map { CertificationResponse.from(it) }
  }

  /**
   * 인증 정보를 수정합니다. (제목, 내용) 작성자만 수정 가능하며, 생성 후 24시간 이내에만 수정 가능합니다.
   *
   * @param certificationId 수정할 인증의 ID
   * @param request 수정할 인증 데이터
   * @param userLoginId 현재 로그인한 사용자의 ID
   * @return 수정된 인증의 응답 객체
   * @throws CertificationNotFoundException 인증을 찾을 수 없을 때
   * @throws CertificationUpdateForbiddenException 수정 권한이 없을 때
   * @throws CertificationUpdatePeriodExpiredException 24시간 수정 기한이 지났을 때
   */
  @Transactional
  fun updateCertification(
    certificationId: Long,
    request: CertificationUpdateRequest,
    userLoginId: String,
  ): CertificationResponse {
    val certification =
      certificationRepository.findById(certificationId).orElseThrow {
        CertificationNotFoundException()
      }

    // 인증 작성자와 현재 로그인한 사용자가 일치하는지 확인
    if (certification.user.loginId != userLoginId) {
      throw CertificationUpdateForbiddenException()
    }

    // 인증 생성 후 24시간이 지났는지 확인
    if (certification.createdAt.isBefore(LocalDateTime.now().minusHours(24))) {
      throw CertificationUpdatePeriodExpiredException()
    }

    certification.title = request.title
    certification.content = request.content

    val updatedCertification = certificationRepository.save(certification)
    return CertificationResponse.from(updatedCertification)
  }

  /**
   * 인증을 삭제합니다. (Soft Delete) 작성자만 삭제 가능합니다.
   *
   * @param certificationId 삭제할 인증의 ID
   * @param userLoginId 현재 로그인한 사용자의 ID
   * @throws CertificationNotFoundException 인증을 찾을 수 없을 때
   * @throws CertificationUpdateForbiddenException 삭제 권한이 없을 때
   */
  @Transactional
  fun deleteCertification(certificationId: Long, userLoginId: String) {
    val certification =
      certificationRepository.findById(certificationId).orElseThrow {
        CertificationNotFoundException()
      }

    // 인증 작성자와 현재 로그인한 사용자가 일치하는지 확인
    if (certification.user.loginId != userLoginId) {
      throw CertificationUpdateForbiddenException("이 인증을 삭제할 권한이 없습니다")
    }

    val challenge = certification.challenge

    // 실제 삭제 대신 isDeleted 플래그를 true로 변경 (Soft Delete)
    certificationRepository.delete(certification)

    // 챌린지 전체 인증 수 감소
    if (challenge.certificationCnt > 0) {
      challenge.certificationCnt--
      challengeRepository.save(challenge)
    }

    // 참여자별 인증 수 감소
    participantRepository.findByIdAndLoginId(challenge.id, userLoginId).ifPresent { participant ->
      if (participant.certificationCnt > 0) {
        participant.certificationCnt--
        participantRepository.save(participant)
      }
    }
  }

  /**
   * 특정 인증에 사진 URL과 AI 분석 결과를 등록합니다.
   *
   * @param certificationId 사진 URL을 등록할 인증의 ID
   * @param photoUrl 업로드된 사진의 URL
   * @param analysisResult AI 분석 결과 (선택 사항)
   * @param userLoginId 현재 로그인한 사용자의 ID
   * @return 사진 정보가 업데이트된 인증의 응답 객체
   */
  @Transactional
  fun uploadCertificationPhoto(
    certificationId: Long,
    photoUrl: String,
    analysisResult: String?,
    userLoginId: String,
  ): CertificationResponse {
    val certification =
      certificationRepository.findById(certificationId).orElseThrow {
        CertificationNotFoundException()
      }
    // 인증 작성자와 현재 로그인한 사용자가 일치하는지 확인
    if (certification.user.loginId != userLoginId) {
      throw CertificationUpdateForbiddenException("이 인증에 사진을 업로드할 권한이 없습니다")
    }

    certification.photoUrl = photoUrl
    certification.analysisResult = analysisResult
    val updatedCertification = certificationRepository.save(certification)
    return CertificationResponse.from(updatedCertification)
  }

  /**
   * 특정 인증의 사진을 삭제합니다. (DB에서 photoUrl 제거)
   *
   * @param certificationId 사진을 삭제할 인증의 ID
   * @param userLoginId 현재 로그인한 사용자의 ID
   * @return 사진 정보가 삭제된 인증의 응답 객체
   * @throws CertificationNotFoundException 인증을 찾을 수 없을 때
   * @throws CertificationUpdateForbiddenException 사진 삭제 권한이 없을 때
   */
  @Transactional
  fun deleteCertificationPhoto(certificationId: Long, userLoginId: String): CertificationResponse {
    val certification =
      certificationRepository.findById(certificationId).orElseThrow {
        CertificationNotFoundException()
      }

    if (certification.user.loginId != userLoginId) {
      throw CertificationUpdateForbiddenException("이 인증의 사진을 삭제할 권한이 없습니다")
    }

    // 실제 파일 삭제
    certification.photoUrl?.let { fileStorageService.deleteFile(it) }
    certification.photoUrl = null

    val updatedCertification = certificationRepository.save(certification)
    return CertificationResponse.from(updatedCertification)
  }
}
