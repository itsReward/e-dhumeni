package com.edhumeni.domain.service

import com.edhumeni.domain.model.User
import com.edhumeni.domain.model.UserRole
import com.edhumeni.domain.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun findAll(): List<User> = userRepository.findAll()

    fun findById(id: UUID): User? = userRepository.findByIdOrNull(id)

    fun findByUsername(username: String): User? =
        userRepository.findByUsername(username).orElse(null)

    @Transactional
    fun createUser(user: User, plainPassword: String): User {
        // Check if username already exists
        if (userRepository.existsByUsername(user.username)) {
            throw IllegalArgumentException("Username already exists")
        }

        // Check if email already exists
        if (userRepository.existsByEmail(user.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        // Encode the password
        user.setPassword(passwordEncoder.encode(plainPassword))

        return userRepository.save(user)
    }

    @Transactional
    fun updateUser(id: UUID, updatedUser: User): User {
        val existingUser = userRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("User with ID $id not found")

        // Check if username is being changed and already exists
        if (updatedUser.username != existingUser.username &&
            userRepository.existsByUsername(updatedUser.username)) {
            throw IllegalArgumentException("Username already exists")
        }

        // Check if email is being changed and already exists
        if (updatedUser.email != existingUser.email &&
            userRepository.existsByEmail(updatedUser.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        // Update the fields we allow to be updated
        existingUser.fullName = updatedUser.fullName
        existingUser.email = updatedUser.email
        existingUser.role = updatedUser.role
        existingUser.enabled = updatedUser.enabled
        existingUser.updatedAt = LocalDateTime.now()

        return userRepository.save(existingUser)
    }

    @Transactional
    fun changePassword(id: UUID, currentPassword: String, newPassword: String): User {
        val user = userRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("User with ID $id not found")

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(newPassword))
        user.updatedAt = LocalDateTime.now()

        return userRepository.save(user)
    }

    @Transactional
    fun resetPassword(id: UUID, newPassword: String): User {
        val user = userRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("User with ID $id not found")

        // Set new password
        user.setPassword(passwordEncoder.encode(newPassword))
        user.updatedAt = LocalDateTime.now()

        return userRepository.save(user)
    }

    @Transactional
    fun deleteUser(id: UUID): Boolean {
        val user = userRepository.findByIdOrNull(id) ?: return false

        userRepository.delete(user)
        return true
    }

    @Transactional
    fun toggleUserStatus(id: UUID): User {
        val user = userRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("User with ID $id not found")

        // Toggle enabled status
        user.enabled = !user.enabled
        user.updatedAt = LocalDateTime.now()

        return userRepository.save(user)
    }

    /**
     * Get user statistics
     */
    fun getUserStats(): Map<String, Any> {
        val allUsers = userRepository.findAll()

        // Count users by role
        val usersByRole = allUsers.groupBy { it.role }
            .mapValues { it.value.size }

        // Count active vs. inactive users
        val activeUsers = allUsers.count { it.enabled }
        val inactiveUsers = allUsers.size - activeUsers

        return mapOf(
            "totalUsers" to allUsers.size,
            "activeUsers" to activeUsers,
            "inactiveUsers" to inactiveUsers,
            "usersByRole" to usersByRole
        )
    }
}