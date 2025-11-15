package com.planit.repository

import com.planit.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>{
  fun findByLoginId(loginId: String): User?
}
