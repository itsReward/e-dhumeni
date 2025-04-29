package com.edhumeni.application.dto.response

import java.time.LocalDateTime
import java.util.UUID

data class AeoResponse(
    val id: UUID,
    val name: String,
    val contactNumber: String,
    val email: String,
    val employeeId: String,
    val qualification: String?,
    val yearsOfExperience: Int,
    val assignedRegions: List<RegionSummaryResponse>,
    val farmerCount: Int,
    val farmersNeedingSupportCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)