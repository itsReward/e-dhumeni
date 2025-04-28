package com.edhumeni.application.mapper

import com.edhumeni.application.dto.request.ContractRequest
import com.edhumeni.application.dto.response.ContractResponse
import com.edhumeni.application.dto.response.DeliverySummaryResponse
import com.edhumeni.application.dto.response.FarmerSummaryResponse
import com.edhumeni.domain.model.Contract
import com.edhumeni.domain.model.Farmer
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ContractMapper(private val farmerMapper: FarmerMapper) {

    fun toEntity(request: ContractRequest): Contract {
        // Create a placeholder Farmer that will be replaced in the service
        val placeholderFarmer = Farmer(
            id = request.farmerId,
            name = "",
            age = 0,
            gender = com.edhumeni.domain.model.Gender.OTHER,
            contactNumber = "",
            region = com.edhumeni.domain.model.Region(
                name = "",
                province = "",
                district = ""
            ),
            province = "",
            ward = "",
            naturalRegion = "",
            soilType = "",
            landOwnershipType = com.edhumeni.domain.model.LandOwnershipType.OTHER,
            farmSizeHectares = 0.0
        )

        return Contract(
            farmer = placeholderFarmer,
            contractNumber = request.contractNumber,
            startDate = request.startDate,
            endDate = request.endDate,
            type = request.type,
            expectedDeliveryKg = request.expectedDeliveryKg,
            pricePerKg = request.pricePerKg,
            advancePayment = request.advancePayment,
            inputSupportValue = request.inputSupportValue,
            signingBonus = request.signingBonus,
            repaymentStatus = request.repaymentStatus,
            totalRepaidAmount = request.totalRepaidAmount,
            totalOwedAmount = 0.0, // This will be calculated in the service
            challengesMeetingTerms = request.challengesMeetingTerms,
            hasLoanComponent = request.hasLoanComponent,
            active = request.active
        )
    }

    fun toResponse(entity: Contract): ContractResponse {
        return ContractResponse(
            id = entity.id,
            farmer = farmerMapper.toSummaryResponse(entity.farmer),
            contractNumber = entity.contractNumber,
            startDate = entity.startDate,
            endDate = entity.endDate,
            type = entity.type,
            expectedDeliveryKg = entity.expectedDeliveryKg,
            pricePerKg = entity.pricePerKg,
            advancePayment = entity.advancePayment,
            inputSupportValue = entity.inputSupportValue,
            signingBonus = entity.signingBonus,
            repaymentStatus = entity.repaymentStatus,
            totalRepaidAmount = entity.totalRepaidAmount,
            totalOwedAmount = entity.totalOwedAmount,
            challengesMeetingTerms = entity.challengesMeetingTerms,
            hasLoanComponent = entity.hasLoanComponent,
            active = entity.active,
            deliveries = entity.deliveries.map { delivery ->
                DeliverySummaryResponse(
                    id = delivery.id,
                    deliveryDate = delivery.deliveryDate,
                    quantityKg = delivery.quantityKg,
                    qualityGrade = delivery.qualityGrade,
                    totalAmountPaid = delivery.totalAmountPaid
                )
            },
            totalDeliveredKg = entity.getTotalDeliveredKg(),
            deliveryCompletionPercentage = entity.getDeliveryCompletionPercentage(),
            behindSchedule = entity.isBehindSchedule(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}