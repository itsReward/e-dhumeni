package com.edhumeni.domain.service

import com.edhumeni.domain.model.Delivery
import com.edhumeni.domain.repository.ContractRepository
import com.edhumeni.domain.repository.DeliveryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class DeliveryService(
    private val deliveryRepository: DeliveryRepository,
    private val contractRepository: ContractRepository
) {

    fun findAll(): List<Delivery> = deliveryRepository.findAll()

    fun findById(id: UUID): Delivery? = deliveryRepository.findByIdOrNull(id)

    fun findByContractId(contractId: UUID): List<Delivery> = deliveryRepository.findByContractId(contractId)

    fun findByFarmerId(farmerId: UUID): List<Delivery> = deliveryRepository.findByContractFarmerId(farmerId)

    @Transactional
    fun recordDelivery(delivery: Delivery, contractId: UUID): Delivery {
        val contract = contractRepository.findByIdOrNull(contractId)
            ?: throw IllegalArgumentException("Contract with ID $contractId not found")

        delivery.contract = contract

        // Set verified information if not already set
        if (delivery.verifiedBy == null) {
            delivery.verifiedAt = LocalDateTime.now()
        }

        // Calculate total amount paid if not provided
        if (delivery.totalAmountPaid == null && delivery.pricePaidPerKg != null) {
            delivery.totalAmountPaid = (delivery.pricePaidPerKg!! * delivery.quantityKg) - delivery.deductionAmount
        }

        val savedDelivery = deliveryRepository.save(delivery)

        // Add the delivery to the contract's delivery list
        contract.deliveries.add(savedDelivery)
        contractRepository.save(contract)

        return savedDelivery
    }

    @Transactional
    fun updateDelivery(id: UUID, updatedDelivery: Delivery): Delivery {
        val existingDelivery = deliveryRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Delivery with ID $id not found")

        // Update properties but don't change the contract relationship
        with(existingDelivery) {
            deliveryDate = updatedDelivery.deliveryDate
            quantityKg = updatedDelivery.quantityKg
            qualityGrade = updatedDelivery.qualityGrade
            moistureContent = updatedDelivery.moistureContent
            pricePaidPerKg = updatedDelivery.pricePaidPerKg

            // Update total amount paid if price changed
            if (pricePaidPerKg != null) {
                totalAmountPaid = (pricePaidPerKg!! * quantityKg) - deductionAmount
            } else {
                totalAmountPaid = updatedDelivery.totalAmountPaid
            }

            deductionAmount = updatedDelivery.deductionAmount
            deductionReason = updatedDelivery.deductionReason
            receiptNumber = updatedDelivery.receiptNumber
            notes = updatedDelivery.notes

            // Update verification info if provided
            if (updatedDelivery.verifiedBy != null && verifiedBy != updatedDelivery.verifiedBy) {
                verifiedBy = updatedDelivery.verifiedBy
                verifiedAt = LocalDateTime.now()
            }
        }

        return deliveryRepository.save(existingDelivery)
    }

    @Transactional
    fun deleteDelivery(id: UUID): Boolean {
        val delivery = deliveryRepository.findByIdOrNull(id) ?: return false

        // Get the contract to update its associations
        val contract = delivery.contract

        // Remove the delivery from the contract's delivery list
        contract.deliveries.removeIf { it.id == id }
        contractRepository.save(contract)

        // Delete the delivery
        deliveryRepository.delete(delivery)
        return true
    }

    fun calculateDeliveryStats(contractId: UUID): Map<String, Any> {
        val deliveries = deliveryRepository.findByContractId(contractId)

        if (deliveries.isEmpty()) {
            return mapOf(
                "totalDeliveries" to 0,
                "totalQuantityKg" to 0.0,
                "averageQualityGrade" to "N/A",
                "totalAmountPaid" to 0.0
            )
        }

        val totalQuantity = deliveries.sumOf { it.quantityKg }
        val totalAmount = deliveries.mapNotNull { it.totalAmountPaid }.sum()

        // Calculate quality distribution
        val qualityDistribution = deliveries
            .groupBy { it.qualityGrade }
            .mapValues { (_, deliveriesWithGrade) ->
                (deliveriesWithGrade.size.toDouble() / deliveries.size) * 100
            }

        // Calculate most common quality grade
        val mostCommonGrade = deliveries
            .groupBy { it.qualityGrade }
            .maxByOrNull { (_, list) -> list.size }
            ?.key
            ?.toString() ?: "N/A"

        return mapOf(
            "totalDeliveries" to deliveries.size,
            "totalQuantityKg" to totalQuantity,
            "averageQualityGrade" to mostCommonGrade,
            "qualityDistribution" to qualityDistribution,
            "totalAmountPaid" to totalAmount,
            "averagePricePerKg" to if (totalQuantity > 0) totalAmount / totalQuantity else 0.0,
            "firstDeliveryDate" to deliveries.minOfOrNull { it.deliveryDate },
            "lastDeliveryDate" to deliveries.maxOfOrNull { it.deliveryDate }
        ) as Map<String, Any>
    }

    fun findRecentDeliveries(limit: Int = 10): List<Delivery> {
        return deliveryRepository.findTopNByOrderByDeliveryDateDesc(limit)
    }

    fun findDeliveriesByQualityGrade(qualityGrade: com.edhumeni.domain.model.QualityGrade): List<Delivery> {
        return deliveryRepository.findByQualityGrade(qualityGrade)
    }

    fun findDeliveriesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Delivery> {
        return deliveryRepository.findByDeliveryDateBetween(startDate, endDate)
    }
}