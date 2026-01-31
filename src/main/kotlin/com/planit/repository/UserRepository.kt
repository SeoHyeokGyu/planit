package com.planit.repository

import com.planit.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long>{
  fun findByLoginId(loginId: String): User?

  fun findByLoginIdContainingOrNicknameContaining(
    loginId: String,
    nickname: String,
    pageable: Pageable
  ): Page<User>

  @Query("SELECT u FROM User u WHERE u.loginId != 'withdrawn_user' ORDER BY FUNCTION('RANDOM')")
  fun findRandomUsers(pageable: Pageable): Page<User>
}
