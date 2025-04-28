package com.edhumeni.application.mapper

import com.edhumeni.application.dto.request.DeliveryRequest
import com.edhumeni.application.dto.response.DeliveryResponse
import com.edhumeni.domain.model.Contract
import com.edhumeni.domain.model.Delivery
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class DeliveryMapper {

    fun toEntity(request: DeliveryRequest): Delivery {
        // Create a placeholder Contract that will be replaced in the service
        val placeholderContract = Contract(
            id = request.contractId,
            farmer = com.edhumeni.domain.model.Farmer(
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
            ),
            contractNumber = "",
            startDate = java.time.LocalDate.now(),
            endDate = java.time.LocalDate.now(),
            type = com.edhumeni.domain.model.ContractType.BASIC,
            expectedDeliveryKg = 0.0
        )

        return Delivery(
            contract = placeholderContract,
            deliveryDate = request.deliveryDate,
            quantityKg = request.quantityKg,
            qualityGrade = request.qualityGrade,
            moistureContent = request.moistureContent,
            pricePaidPerKg = request.pricePaidPerKg,
            totalAmountPaid = request.totalAmountPaid ?:
            (request.pricePaidPerKg?.let { it * request.quantityKg - request.deductionAmount } ?: 0.0),
            deductionAmount = request.deductionAmount,
            deductionReason = request.deductionReason,
            receiptNumber = request.receiptNumber,
            notes = request.notes
        )
    }

    fun toResponse(entity: Delivery): DeliveryResponse {
        return DeliveryResponse(
            id = entity.id,
            contractId = entity.contract.id,
            contractNumber = entity.contract.contractNumber,
            farmerId = entity.contract.farmer.id,
            farmerName = entity.contract.farmer.name,
            deliveryDate = entity.deliveryDate,
            quantityKg = entity.quantityKg,
            qualityGrade = entity.qualityGrade,
            moistureContent = entity.moistureContent,
            pricePaidPerKg = entity.pricePaidPerKg,
            totalAmountPaid = entity.totalAmountPaid,
            deductionAmount = entity.deductionAmount,
            deductionReason = entity.deductionReason,
            receiptNumber = entity.receiptNumber,
            verifiedBy = entity.verifiedBy,
            verifiedAt = entity.verifiedAt,
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}