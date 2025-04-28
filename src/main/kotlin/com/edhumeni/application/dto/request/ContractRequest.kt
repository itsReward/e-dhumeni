package com.edhumeni.application.dto.request

import com.edhumeni.domain.model.ContractType
import com.edhumeni.domain.model.RepaymentStatus
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.util.UUID

data class ContractRequest(
    @field:NotNull(message = "Farmer ID is required")
    val farmerId: UUID,

    val contractNumber: String = "",  // Can be auto-generated if empty

    @field:NotNull(message = "Start date is required")
    val startDate: LocalDate,

    @field:NotNull(message = "End date is required")
    val endDate: LocalDate,

    @field:NotNull(message = "Contract type is required")
    val type: ContractType,

    @field:DecimalMin(value = "0.01", message = "Expected delivery must be greater than 0")
    val expectedDeliveryKg: Double,

    val pricePerKg: Double?,

    val advancePayment: Double?,

    val inputSupportValue: Double?,

    val signingBonus: Double?,

    val repaymentStatus: RepaymentStatus = RepaymentStatus.NOT_STARTED,

    val totalRepaidAmount: Double = 0.0,

    val challengesMeetingTerms: String?,

    val hasLoanComponent: Boolean = false,

    val active: Boolean = true
)
