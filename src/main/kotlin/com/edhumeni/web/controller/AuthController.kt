// src/main/kotlin/com/edhumeni/web/controller/AuthController.kt
package com.edhumeni.web.controller

import com.edhumeni.application.dto.request.LoginRequest
import com.edhumeni.application.dto.response.AuthResponse
import com.edhumeni.domain.repository.UserRepository
import com.edhumeni.infrastructure.security.JwtTokenProvider
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) {

    @PostMapping("/login")
    fun authenticateUser(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
        )

        val roles = authentication.authorities.map { it.authority }
        val token = jwtTokenProvider.createToken(loginRequest.username, roles)

        val user = userRepository.findByUsername(loginRequest.username)
            .orElseThrow { RuntimeException("User not found") }

        return ResponseEntity.ok(
            AuthResponse(
                token = token,
                type = "Bearer",
                userId = user.id,
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                roles = roles
            )
        )
    }
}