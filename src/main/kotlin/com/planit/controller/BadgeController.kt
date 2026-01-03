package com.planit.controller

import com.planit.dto.BadgeResponse
import com.planit.service.BadgeService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/badges")
class BadgeController(private val badgeService: BadgeService) {

  @GetMapping
  fun getAllBadges(
    @AuthenticationPrincipal userDetails: UserDetails
  ): ResponseEntity<List<BadgeResponse>> {
    val userLoginId = userDetails.username
    return ResponseEntity.ok(badgeService.getAllBadges(userLoginId))
  }

  @GetMapping("/my")
  fun getMyBadges(
    @AuthenticationPrincipal userDetails: UserDetails
  ): ResponseEntity<List<BadgeResponse>> {
    val userLoginId = userDetails.username
    return ResponseEntity.ok(badgeService.getMyBadges(userLoginId))
  }
}
