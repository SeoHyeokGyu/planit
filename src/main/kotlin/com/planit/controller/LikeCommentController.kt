package com.planit.controller

import com.planit.dto.ApiResponse
import com.planit.dto.CommentCreateRequest
import com.planit.dto.CommentResponse
import com.planit.dto.CustomUserDetails
import com.planit.service.CommentService
import com.planit.service.LikeService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class LikeCommentController(
    private val likeService: LikeService,
    private val commentService: CommentService
) {

    @PostMapping("/certifications/{id}/likes")
    fun toggleLike(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<Boolean>> {
        val isLiked = likeService.toggleLike(id, userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(isLiked))
    }

    @GetMapping("/certifications/{id}/comments")
    fun getComments(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails?
    ): ResponseEntity<ApiResponse<List<CommentResponse>>> {
        val comments = commentService.getComments(id, userDetails?.username)
        return ResponseEntity.ok(ApiResponse.success(comments))
    }

    @PostMapping("/certifications/{id}/comments")
    fun createComment(
        @PathVariable id: Long,
        @RequestBody request: CommentCreateRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<CommentResponse>> {
        val response = commentService.createComment(id, request, userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @DeleteMapping("/comments/{id}")
    fun deleteComment(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<Unit>> {
        commentService.deleteComment(id, userDetails.username)
        return ResponseEntity.ok(ApiResponse.success())
    }
}
