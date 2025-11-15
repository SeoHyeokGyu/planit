package com.planit.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where
import java.time.LocalDateTime

@Entity
@Table(
    name = "challenges",
    indexes = [
        Index(name = "idx_category", columnList = "category"),
        Index(name = "idx_difficulty", columnList = "difficulty"),
        Index(name = "idx_start_date", columnList = "startDate"),
        Index(name = "idx_end_date", columnList = "endDate"),
        Index(name = "idx_deleted", columnList = "deleted")
    ]
)
@SQLDelete(sql = "UPDATE challenges SET deleted = true, deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted = false")
data class Challenge(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    var category: ChallengeCategory,

    @Column(nullable = false)
    var startDate: LocalDateTime,

    @Column(nullable = false)
    var endDate: LocalDateTime,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var difficulty: ChallengeDifficulty,

    @Column(nullable = false)
    val createdBy: Long, // 사용자 ID

    @Column(nullable = false)
    var viewCount: Long = 0,

    @Column(nullable = false)
    var participantCount: Int = 0,

    @Column(nullable = false)
    var certificationCount: Long = 0,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var deleted: Boolean = false,

    @Column
    var deletedAt: LocalDateTime? = null,

//    @OneToMany(mappedBy = "challenge", cascade = [CascadeType.ALL], orphanRemoval = true)
//    val participants: MutableList<ChallengeParticipant> = mutableListOf()
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun isActive(): Boolean {
        val now = LocalDateTime.now()
        return now.isAfter(startDate) && now.isBefore(endDate) && !deleted
    }

    fun isUpcoming(): Boolean {
        return LocalDateTime.now().isBefore(startDate) && !deleted
    }

    fun isEnded(): Boolean {
        return LocalDateTime.now().isAfter(endDate)
    }
}

enum class ChallengeCategory {
    HEALTH,      // 건강
    EXERCISE,    // 운동
    STUDY,       // 학습
    HOBBY,       // 취미
    LIFESTYLE,   // 생활습관
    FINANCE,     // 재테크
    CAREER,      // 커리어
    RELATIONSHIP,// 관계
    CREATIVITY,  // 창작
    OTHER        // 기타
}

enum class ChallengeDifficulty {
    EASY,        // 쉬움
    NORMAL,      // 보통
    HARD,        // 어려움
    EXPERT       // 전문가
}
