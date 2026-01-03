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
import com.planit.service.badge.CertificationBadgeChecker
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
  @MockK private lateinit var badgeChecker: CertificationBadgeChecker
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
      every { badgeChecker.checkBadges(any()) } answers {}

      // When
      val response = certificationService.createCertification(request, user.loginId)

      // Then
      assertThat(response.title).isEqualTo(request.title)
      assertThat(response.content).isEqualTo(request.content)
      assertThat(challenge.certificationCnt).isEqualTo(1L)
      verify(exactly = 1) { certificationRepository.save(any()) }
      verify(exactly = 1) { challengeRepository.save(any()) }
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
}
