package com.planit.service

import com.planit.dto.CertificationCreateRequest
import com.planit.dto.CertificationUpdateRequest
import com.planit.entity.Certification
import com.planit.entity.Challenge
import com.planit.entity.User
import com.planit.exception.CertificationUpdateForbiddenException
import com.planit.exception.CertificationUpdatePeriodExpiredException
import com.planit.repository.CertificationRepository
import com.planit.repository.ChallengeParticipantRepository
import com.planit.repository.ChallengeRepository
import com.planit.repository.UserRepository
import com.planit.enums.BadgeType
import com.planit.service.badge.BadgeService
import com.planit.service.storage.FileStorageService
import com.planit.util.setPrivateProperty
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("CertificationService 테스트")
class CertificationServiceTest {
  @MockK private lateinit var certificationRepository: CertificationRepository
  @MockK private lateinit var userRepository: UserRepository
  @MockK private lateinit var challengeRepository: ChallengeRepository
  @MockK private lateinit var participantRepository: ChallengeParticipantRepository
  @MockK private lateinit var notificationService: NotificationService
  @MockK private lateinit var rewardService: RewardService
  @MockK private lateinit var badgeService: BadgeService
  @MockK private lateinit var streakService: StreakService
  @MockK private lateinit var fileStorageService: FileStorageService
  @MockK private lateinit var geminiService: GeminiService
  @InjectMockKs private lateinit var certificationService: CertificationService

  private lateinit var user: User
  private lateinit var challenge: Challenge
  private val challengeId = "CHL-12345678"

  @BeforeEach
  fun setUp() {
    user = User(loginId = "testuser", password = "password", nickname = "testuser")
    user.setPrivateProperty("id", 1L)

    challenge =
      Challenge(
        title = "Test Challenge",
        description = "Test Description",
        category = "Test",
        startDate = LocalDateTime.now(),
        endDate = LocalDateTime.now().plusDays(10),
        difficulty = "Easy",
        createdId = "creator",
        certificationCnt = 0,
      )
    challenge.setPrivateProperty("id", challengeId)
  }

  @Nested
  @DisplayName("analyzeCertificationPhoto 메서드는")
  inner class DescribeAnalyzeCertificationPhoto {
    private lateinit var certification: Certification
    @MockK private lateinit var file: MultipartFile

    @BeforeEach
    fun setup() {
      certification = Certification(
        user = user,
        challenge = challenge,
        title = "Title",
        content = "Content"
      )
      certification.setPrivateProperty("id", 1L)
    }

    @Test
    @DisplayName("Gemini를 호출하여 사진 분석 결과를 반환한다")
    fun `calls Gemini and returns analysis result`() {
      // Given
      val expectedResult = "적합 여부: 예\n이유: 운동 중입니다."
      every { certificationRepository.findById(1L) } returns Optional.of(certification)
      every { geminiService.analyzeImage(any(), any()) } returns expectedResult

      // When
      val result = certificationService.analyzeCertificationPhoto(1L, file)

      // Then
      assertThat(result).isEqualTo(expectedResult)
      verify(exactly = 1) { geminiService.analyzeImage(any(), file) }
    }

    @Test
    @DisplayName("Gemini 분석 실패 시 기본 메시지를 반환한다")
    fun `returns default message when Gemini analysis fails`() {
      // Given
      every { certificationRepository.findById(1L) } returns Optional.of(certification)
      every { geminiService.analyzeImage(any(), any()) } throws RuntimeException("API Error")

      // When
      val result = certificationService.analyzeCertificationPhoto(1L, file)

      // Then
      assertThat(result).contains("이미지 분석을 완료할 수 없습니다")
    }
  }

  @Nested
  @DisplayName("uploadCertificationPhoto 메서드는")
  inner class DescribeUploadCertificationPhoto {
    private lateinit var certification: Certification

    @BeforeEach
    fun setup() {
      certification = Certification(
        user = user,
        challenge = challenge,
        title = "Title",
        content = "Content"
      )
      certification.setPrivateProperty("id", 1L)
    }

    @Test
    @DisplayName("사진 URL과 분석 결과를 저장한다")
    fun `saves photo url and analysis result`() {
      // Given
      val photoUrl = "/images/test.jpg"
      val analysisResult = "AI 분석 결과"
      every { certificationRepository.findById(1L) } returns Optional.of(certification)
      every { certificationRepository.save(any()) } answers { firstArg() }

      // When
      val response = certificationService.uploadCertificationPhoto(1L, photoUrl, analysisResult, user.loginId)

      // Then
      assertThat(response.photoUrl).isEqualTo(photoUrl)
      assertThat(response.analysisResult).isEqualTo(analysisResult)
      verify(exactly = 1) { certificationRepository.save(any()) }
    }
  }

  @Nested
  @DisplayName("createCertification 메서드는")
  inner class DescribeCreateCertification {
    @Test
    @DisplayName("성공적으로 인증을 생성한다")
    fun `creates a certification successfully`() {
      // Given
      val request =
        CertificationCreateRequest(
          challengeId = challengeId,
          title = "Test Title",
          content = "Test Content",
        )
      val certification =
        Certification(
          user = user,
          challenge = challenge,
          title = request.title,
          content = request.content,
        )
      certification.setPrivateProperty("id", 1L)

      every { userRepository.findByLoginId(user.loginId) } returns user
      every { challengeRepository.findById(request.challengeId) } returns Optional.of(challenge)
      every { certificationRepository.save(any()) } returns certification
      every { challengeRepository.save(any()) } returns challenge
      every { participantRepository.findByIdAndLoginId(challengeId, user.loginId) } returns
        Optional.empty()
      every { rewardService.grantCertificationReward(any()) } answers {}
      every { badgeService.checkAndAwardBadges(any(), any()) } returns 0
      justRun { streakService.recordCertification(any(), any()) }

      // When
      val response = certificationService.createCertification(request, user.loginId)

      // Then
      assertThat(response.title).isEqualTo(request.title)
      assertThat(response.content).isEqualTo(request.content)
      assertThat(challenge.certificationCnt).isEqualTo(1L)
      verify(exactly = 1) { certificationRepository.save(any()) }
      verify(exactly = 1) { challengeRepository.save(any()) }
      verify(exactly = 1) { streakService.recordCertification(challengeId, user.loginId) }
    }
  }

  @Nested
  @DisplayName("updateCertification 메서드는")
  inner class DescribeUpdateCertification {

    private lateinit var certification: Certification
    private val updateRequest =
      CertificationUpdateRequest(title = "Updated Title", content = "Updated Content")

    @BeforeEach
    fun setup() {
      certification =
        Certification(
          user = user,
          challenge = challenge,
          title = "Original Title",
          content = "Original Content",
        )
      certification.setPrivateProperty("id", 1L)
    }

    @Test
    @DisplayName("자신이 작성한 인증을 24시간 이내에 성공적으로 수정한다")
    fun `updates a certification successfully within 24 hours`() {
      // Given
      certification.setPrivateProperty("createdAt", LocalDateTime.now().minusHours(1))
      every { certificationRepository.findById(1L) } returns Optional.of(certification)
      every { certificationRepository.save(any()) } answers { firstArg() }

      // When
      val response = certificationService.updateCertification(1L, updateRequest, user.loginId)

      // Then
      assertThat(response.title).isEqualTo(updateRequest.title)
      assertThat(response.content).isEqualTo(updateRequest.content)
    }

    @Test
    @DisplayName("다른 사람이 작성한 인증 수정을 시도하면 CertificationUpdateForbiddenException을 던진다")
    fun `throws CertificationUpdateForbiddenException when trying to update another user's certification`() {
      // Given
      val anotherUser = User("anotheruser", "password", "anotheruser")
      anotherUser.setPrivateProperty("id", 2L)
      certification.setPrivateProperty("user", anotherUser)

      every { certificationRepository.findById(1L) } returns Optional.of(certification)

      // When & Then
      assertThrows<CertificationUpdateForbiddenException> {
        certificationService.updateCertification(1L, updateRequest, user.loginId)
      }
    }

    @Test
    @DisplayName("24시간이 지난 인증 수정을 시도하면 CertificationUpdatePeriodExpiredException을 던진다")
    fun `throws CertificationUpdatePeriodExpiredException when trying to update after 24 hours`() {
      // Given
      certification.setPrivateProperty("createdAt", LocalDateTime.now().minusHours(25))
      every { certificationRepository.findById(1L) } returns Optional.of(certification)

      // When & Then
      assertThrows<CertificationUpdatePeriodExpiredException> {
        certificationService.updateCertification(1L, updateRequest, user.loginId)
      }
    }
  }

  @Nested
  @DisplayName("deleteCertification 메서드는")
  inner class DescribeDeleteCertification {

    private lateinit var certification: Certification

    @BeforeEach
    fun setup() {
      certification =
        Certification(
          user = user,
          challenge = challenge,
          title = "Original Title",
          content = "Original Content",
        )
      certification.setPrivateProperty("id", 1L)
    }

    @Test
    @DisplayName("자신이 작성한 인증을 성공적으로 삭제한다")
    fun `deletes a certification successfully`() {
      // Given
      challenge.certificationCnt = 1L
      every { certificationRepository.findById(1L) } returns Optional.of(certification)
      justRun { certificationRepository.delete(certification) }
      every { challengeRepository.save(any()) } returns challenge
      every { participantRepository.findByIdAndLoginId(challengeId, user.loginId) } returns
        Optional.empty()

      // When
      certificationService.deleteCertification(1L, user.loginId)

      // Then
      assertThat(challenge.certificationCnt).isEqualTo(0L)
      verify(exactly = 1) { certificationRepository.delete(certification) }
      verify(exactly = 1) { challengeRepository.save(any()) }
    }

    @Test
    @DisplayName("다른 사람이 작성한 인증 삭제를 시도하면 CertificationUpdateForbiddenException을 던진다")
    fun `throws CertificationUpdateForbiddenException when trying to delete another user's certification`() {
      // Given
      val anotherUser =
        User(loginId = "anotheruser", password = "password", nickname = "anotheruser")
      anotherUser.setPrivateProperty("id", 2L)
      certification.setPrivateProperty("user", anotherUser)
      every { certificationRepository.findById(1L) } returns Optional.of(certification)

      // When & Then
      assertThrows<CertificationUpdateForbiddenException> {
        certificationService.deleteCertification(1L, user.loginId)
      }
    }
  }

  @Nested
  @DisplayName("deleteCertificationPhoto 메서드는")
  inner class DescribeDeleteCertificationPhoto {
    private lateinit var certification: Certification

    @BeforeEach
    fun setup() {
      certification =
        Certification(
          user = user,
          challenge = challenge,
          title = "Title",
          content = "Content",
          photoUrl = "/images/photo.jpg"
        )
      certification.setPrivateProperty("id", 1L)
    }

    @Test
    @DisplayName("인증 사진을 성공적으로 삭제하고 파일 삭제를 요청한다")
    fun `deletes certification photo and requests file deletion`() {
      // Given
      every { certificationRepository.findById(1L) } returns Optional.of(certification)
      every { certificationRepository.save(any()) } answers { firstArg() }
      justRun { fileStorageService.deleteFile(any()) }

      // When
      val response = certificationService.deleteCertificationPhoto(1L, user.loginId)

      // Then
      assertThat(response.photoUrl).isNull()
      verify(exactly = 1) { fileStorageService.deleteFile("/images/photo.jpg") }
      verify(exactly = 1) { certificationRepository.save(any()) }
    }

    @Test
    @DisplayName("다른 사람이 사진 삭제를 시도하면 Forbidden 예외를 던진다")
    fun `throws exception when unauthorized user tries to delete photo`() {
      // Given
      val otherUser = User("other", "pw", "other")
      certification.setPrivateProperty("user", otherUser)
      
      every { certificationRepository.findById(1L) } returns Optional.of(certification)

      // When & Then
      assertThrows<CertificationUpdateForbiddenException> {
        certificationService.deleteCertificationPhoto(1L, user.loginId)
      }
      
      // 파일 삭제가 호출되지 않아야 함
      verify(exactly = 0) { fileStorageService.deleteFile(any()) }
    }
  }
}
