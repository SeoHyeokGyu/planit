package com.planit.dto

import com.planit.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class CustomUserDetails(val user: User) : UserDetails {
  override fun getAuthorities(): Collection<GrantedAuthority?> = mutableListOf()

  override fun getPassword(): String = user.password

  override fun getUsername(): String = user.loginId

  override fun isAccountNonExpired(): Boolean = true

  override fun isAccountNonLocked(): Boolean = true

  override fun isCredentialsNonExpired(): Boolean = true

  override fun isEnabled(): Boolean = true
}