package com.edhumeni.application.dto.response

import com.edhumeni.domain.model.UserRole
import java.time.LocalDateTime
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val enabled: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)