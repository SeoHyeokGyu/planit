package com.planit.dto

data class UserPasswordUpdateRequest(
    val oldPassword: String,
    val newPassword: String
)
