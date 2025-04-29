package com.edhumeni.application.mapper

import com.edhumeni.application.dto.request.UserRequest
import com.edhumeni.application.dto.response.UserResponse
import com.edhumeni.domain.model.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toEntity(request: UserRequest): User {
        return User(
            username = request.username,
            password = "", // This will be encoded by the service
            email = request.email,
            fullName = request.fullName,
            role = request.role,
            enabled = request.enabled
        )
    }

    fun toResponse(entity: User): UserResponse {
        return UserResponse(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            fullName = entity.fullName,
            role = entity.role,
            enabled = entity.enabled,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}