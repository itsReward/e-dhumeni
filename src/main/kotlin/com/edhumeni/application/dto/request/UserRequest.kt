package com.edhumeni.application.dto.request

import com.edhumeni.domain.model.UserRole
import jakarta.validation.constraints.*

data class UserRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Full name is required")
    val fullName: String,

    @field:NotNull(message = "Role is required")
    val role: UserRole,

    val enabled: Boolean = true
)

data class PasswordChangeRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 6, message = "New password must be at least 6 characters")
    val newPassword: String
)