package com.edhumeni.application.dto.response

import com.edhumeni.domain.model.ContractType
import com.edhumeni.domain.model.RepaymentStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class ContractResponse(
    val id: UUID,
    val farmer: FarmerSummaryResponse,
    val contractNumber: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: ContractType,
    val expectedDeliveryKg: Double,
    val pricePerKg: Double?,
    val advancePayment: Double?,
    val inputSupportValue: Double?,
    val signingBonus: Double?,
    val repaymentStatus: RepaymentStatus,
    val totalRepaidAmount: Double,
    val totalOwedAmount: Double,
    val challengesMeetingTerms: String?,
    val hasLoanComponent: Boolean,
    val active: Boolean,
    val deliveries: List<DeliverySummaryResponse>,
    val totalDeliveredKg: Double,
    val deliveryCompletionPercentage: Double,
    val behindSchedule: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ContractSummaryResponse(
    val id: UUID,
    val contractNumber: String,
    val farmerName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: ContractType,
    val expectedDeliveryKg: Double,
    val actualDeliveryKg: Double,
    val completionPercentage: Double,
    val active: Boolean,
    val repaymentStatus: RepaymentStatus,
    val atRisk: Boolean,
    val remainingDays: Long,
    val deliveriesCount: Int
) {
    constructor(summaryMap: Map<String, Any>) : this(
        id = summaryMap["id"] as UUID,
        contractNumber = summaryMap["contractNumber"] as String,
        farmerName = summaryMap["farmerName"] as String,
        startDate = summaryMap["startDate"] as LocalDate,
        endDate = summaryMap["endDate"] as LocalDate,
        type = summaryMap["type"] as ContractType,
        expectedDeliveryKg = summaryMap["expectedDeliveryKg"] as Double,
        actualDeliveryKg = summaryMap["actualDeliveryKg"] as Double,
        completionPercentage = summaryMap["completionPercentage"] as Double,
        active = summaryMap["active"] as Boolean,
        repaymentStatus = summaryMap["repaymentStatus"] as RepaymentStatus,
        atRisk = summaryMap["atRisk"] as Boolean,
        remainingDays = summaryMap["remainingDays"] as Long,
        deliveriesCount = summaryMap["deliveriesCount"] as Int
    )
}