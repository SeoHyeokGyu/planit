package com.planit.repository

import com.planit.entity.Certification
import com.planit.entity.Challenge
import com.planit.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDateTime

@DataJpaTest
class CertificationRepositoryTest {

  @Autowired lateinit var certificationRepository: CertificationRepository

  @Autowired lateinit var userRepository: UserRepository

  @Autowired lateinit var challengeRepository: ChallengeRepository

  private lateinit var user: User
  private lateinit var challenge: Challenge

  @BeforeEach
  fun setUp() {
    user = userRepository.save(User("testuser", "password", "nickname"))

    challenge =
      challengeRepository.save(
        Challenge(
          title = "Test Challenge",
          description = "Desc",
          category = "Cat",
          startDate = LocalDateTime.now(),
          endDate = LocalDateTime.now().plusDays(7),
          difficulty = "Easy",
          createdId = "creator",
          certificationCnt = 0,
        )
      )
  }

  @Test
  @DisplayName("findAllPhotoUrls_삭제된_인증의_사진_URL도_포함하여_조회한다")
  fun findAllPhotoUrls() {
    // given
    // 1. 일반 인증 (사진 있음)
    val cert1 = Certification(user, challenge, "Title1", "Content1", "/images/1.jpg")
    certificationRepository.save(cert1)

    // 2. 삭제된 인증 (사진 있음, Soft Delete)
    val cert2 = Certification(user, challenge, "Title2", "Content2", "/images/2.jpg")
    cert2.isDeleted = true
    certificationRepository.save(cert2)

    // 3. 사진 없는 인증
    val cert3 = Certification(user, challenge, "Title3", "Content3", null)
    certificationRepository.save(cert3)

    // when
    val urls = certificationRepository.findAllPhotoUrls()

    // then
    assertThat(urls).hasSize(2)
    assertThat(urls).containsExactlyInAnyOrder("/images/1.jpg", "/images/2.jpg")
  }
}
