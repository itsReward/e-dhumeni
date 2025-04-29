package com.edhumeni.web.controller

import com.edhumeni.application.dto.request.PasswordChangeRequest
import com.edhumeni.application.dto.request.UserRequest
import com.edhumeni.application.dto.response.UserResponse
import com.edhumeni.application.mapper.UserMapper
import com.edhumeni.domain.model.User
import com.edhumeni.domain.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing users")
class UserController(
    private val userService: UserService,
    private val userMapper: UserMapper
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves all users")
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userService.findAll()
        return ResponseEntity.ok(users.map { userMapper.toResponse(it) })
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val user = userService.findById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(userMapper.toResponse(user))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a user", description = "Creates a new user")
    fun createUser(@Valid @RequestBody request: UserRequest): ResponseEntity<UserResponse> {
        val user = userMapper.toEntity(request)
        val createdUser = userService.createUser(user, request.password)

        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(createdUser))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    @Operation(summary = "Update a user", description = "Updates an existing user")
    fun updateUser(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UserRequest
    ): ResponseEntity<UserResponse> {
        val user = userMapper.toEntity(request)
        val updatedUser = userService.updateUser(id, user)

        return ResponseEntity.ok(userMapper.toResponse(updatedUser))
    }

    @PostMapping("/{id}/change-password")
    @PreAuthorize("authentication.principal.id == #id")
    @Operation(summary = "Change password", description = "Changes the password for a user")
    fun changePassword(
        @PathVariable id: UUID,
        @Valid @RequestBody request: PasswordChangeRequest
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.changePassword(id, request.currentPassword, request.newPassword)
        return ResponseEntity.ok(userMapper.toResponse(updatedUser))
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset password", description = "Resets the password for a user (admin only)")
    fun resetPassword(
        @PathVariable id: UUID,
        @RequestParam newPassword: String
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.resetPassword(id, newPassword)
        return ResponseEntity.ok(userMapper.toResponse(updatedUser))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user", description = "Deletes a user")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<Void> {
        val deleted = userService.deleteUser(id)

        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle user status", description = "Enables or disables a user")
    fun toggleUserStatus(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val updatedUser = userService.toggleUserStatus(id)
        return ResponseEntity.ok(userMapper.toResponse(updatedUser))
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user statistics", description = "Retrieves statistics about users")
    fun getUserStats(): ResponseEntity<Map<String, Any>> {
        val stats = userService.getUserStats()
        return ResponseEntity.ok(stats)
    }
}