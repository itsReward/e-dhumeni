package com.edhumeni.application.dto.response

import java.util.UUID

data class AuthResponse(
    val token: String,
    val type: String,
    val userId: UUID,
    val username: String,
    val email: String,
    val fullName: String,
    val roles: List<String>
)