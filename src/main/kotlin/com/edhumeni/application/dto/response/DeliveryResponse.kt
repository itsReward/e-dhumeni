package com.edhumeni.application.dto.response

import com.edhumeni.domain.model.QualityGrade
import java.time.LocalDateTime
import java.util.UUID

data class DeliveryResponse(
    val id: UUID,
    val contractId: UUID,
    val contractNumber: String,
    val farmerId: UUID,
    val farmerName: String,
    val deliveryDate: LocalDateTime,
    val quantityKg: Double,
    val qualityGrade: QualityGrade,
    val moistureContent: Double?,
    val pricePaidPerKg: Double?,
    val totalAmountPaid: Double?,
    val deductionAmount: Double,
    val deductionReason: String?,
    val receiptNumber: String?,
    val verifiedBy: String?,
    val verifiedAt: LocalDateTime?,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class DeliverySummaryResponse(
    val id: UUID,
    val deliveryDate: LocalDateTime,
    val quantityKg: Double,
    val qualityGrade: QualityGrade,
    val totalAmountPaid: Double?
)

