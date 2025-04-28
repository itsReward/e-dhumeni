package com.edhumeni.application.dto.request

import com.edhumeni.domain.model.QualityGrade
import jakarta.validation.constraints.*
import java.time.LocalDateTime
import java.util.UUID

data class DeliveryRequest(
    @field:NotNull(message = "Contract ID is required")
    val contractId: UUID,

    @field:NotNull(message = "Delivery date is required")
    val deliveryDate: LocalDateTime,

    @field:DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    val quantityKg: Double,

    @field:NotNull(message = "Quality grade is required")
    val qualityGrade: QualityGrade,

    val moistureContent: Double?,

    val pricePaidPerKg: Double?,

    val totalAmountPaid: Double?,

    val deductionAmount: Double = 0.0,

    val deductionReason: String?,

    val receiptNumber: String?,

    val notes: String?
)
