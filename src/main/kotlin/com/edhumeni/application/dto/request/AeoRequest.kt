package com.edhumeni.application.dto.request

import jakarta.validation.constraints.*

data class AeoRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Contact number is required")
    @field:Pattern(regexp = "^\\+?[0-9\\s\\-()]{8,20}$", message = "Invalid contact number format")
    val contactNumber: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Employee ID is required")
    val employeeId: String,

    val qualification: String?,

    @field:Min(value = 0, message = "Years of experience cannot be negative")
    val yearsOfExperience: Int = 0,

    val assignedRegionIds: List<java.util.UUID> = emptyList()
)