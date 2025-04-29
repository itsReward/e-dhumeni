package com.edhumeni.domain.service

import com.edhumeni.domain.model.Contract
import com.edhumeni.domain.model.ContractType
import com.edhumeni.domain.model.Delivery
import com.edhumeni.domain.model.RepaymentStatus
import com.edhumeni.domain.repository.ContractRepository
import com.edhumeni.domain.repository.FarmerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ContractService(
    private val contractRepository: ContractRepository,
    private val farmerRepository: FarmerRepository
) {

    fun findAll(): List<Contract> = contractRepository.findAll()

    fun findById(id: UUID): Contract? = contractRepository.findByIdOrNull(id)

    fun findByContractNumber(contractNumber: String): Contract? =
        contractRepository.findByContractNumber(contractNumber).orElse(null)

    fun findByFarmerId(farmerId: UUID): List<Contract> = contractRepository.findByFarmerId(farmerId)

    fun findActiveContractsByFarmerId(farmerId: UUID): List<Contract> =
        contractRepository.findByFarmerIdAndActive(farmerId, true)

    fun findByRegionId(regionId: UUID): List<Contract> = contractRepository.findByRegionId(regionId)

    fun findByAeoId(aeoId: UUID): List<Contract> = contractRepository.findByAeoId(aeoId)

    fun findByType(type: ContractType): List<Contract> = contractRepository.findByType(type)

    fun findByRepaymentStatus(status: RepaymentStatus): List<Contract> =
        contractRepository.findByRepaymentStatus(status)

    fun findExpiredContracts(): List<Contract> =
        contractRepository.findByEndDateBefore(LocalDate.now())

    fun findExpiredContractsWithUnmetDeliveries(): List<Contract> =
        contractRepository.findExpiredContractsWithUnmetDeliveries(LocalDate.now())

    fun findAtRiskContracts(): List<Contract> {
        val today = LocalDate.now()
        val thirtyDaysLater = today.plusDays(30)
        return contractRepository.findAtRiskContracts(today, thirtyDaysLater)
    }

    fun searchContracts(
        farmerId: UUID? = null,
        type: ContractType? = null,
        active: Boolean? = null,
        repaymentStatus: RepaymentStatus? = null
    ): List<Contract> = contractRepository.searchContracts(farmerId, type, active, repaymentStatus)

    @Transactional
    fun createContract(contract: Contract, farmerId: UUID): Contract {
        val farmer = farmerRepository.findByIdOrNull(farmerId)
            ?: throw IllegalArgumentException("Farmer with ID $farmerId not found")

        contract.farmer = farmer

        // Generate a unique contract number if not provided
        if (contract.contractNumber.isBlank()) {
            val timestamp = System.currentTimeMillis().toString().takeLast(8)
            val farmerInitials = farmer.name.split(" ")
                .filter { it.isNotBlank() }
                .joinToString("") { it.first().toString() }

            contract.contractNumber = "CTR-$farmerInitials-$timestamp"
        }

        // Calculate total owed amount if loan/advance components exist
        var totalOwed = 0.0

        if (contract.advancePayment != null && contract.advancePayment!! > 0) {
            totalOwed += contract.advancePayment!!
        }

        if (contract.inputSupportValue != null && contract.inputSupportValue!! > 0) {
            totalOwed += contract.inputSupportValue!!
            contract.hasLoanComponent = true
        }

        contract.totalOwedAmount = totalOwed

        return contractRepository.save(contract)
    }

    @Transactional
    fun updateContract(id: UUID, updatedContract: Contract): Contract {
        val existingContract = contractRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Contract with ID $id not found")

        with(existingContract) {
            // Don't update farmer or contract number - these are fixed
            startDate = updatedContract.startDate
            endDate = updatedContract.endDate
            type = updatedContract.type
            expectedDeliveryKg = updatedContract.expectedDeliveryKg
            pricePerKg = updatedContract.pricePerKg
            advancePayment = updatedContract.advancePayment
            inputSupportValue = updatedContract.inputSupportValue
            signingBonus = updatedContract.signingBonus
            repaymentStatus = updatedContract.repaymentStatus
            totalRepaidAmount = updatedContract.totalRepaidAmount
            challengesMeetingTerms = updatedContract.challengesMeetingTerms
            hasLoanComponent = updatedContract.hasLoanComponent
            active = updatedContract.active

            // Recalculate total owed if needed
            var totalOwed = 0.0

            if (advancePayment != null && advancePayment!! > 0) {
                totalOwed += advancePayment!!
            }

            if (inputSupportValue != null && inputSupportValue!! > 0) {
                totalOwed += inputSupportValue!!
            }

            totalOwedAmount = totalOwed
        }

        return contractRepository.save(existingContract)
    }

    @Transactional
    fun addDelivery(contractId: UUID, delivery: Delivery): Contract {
        val contract = contractRepository.findByIdOrNull(contractId)
            ?: throw IllegalArgumentException("Contract with ID $contractId not found")

        delivery.contract = contract
        contract.deliveries.add(delivery)

        // Update repayment status based on deliveries
        updateRepaymentStatus(contract)

        return contractRepository.save(contract)
    }

    @Transactional
    fun updateRepaymentStatus(contract: Contract): Contract {
        // Calculate total delivered amount
        val totalDeliveredKg = contract.getTotalDeliveredKg()

        // Update repayment status based on percentage of expected delivery
        val deliveryPercentage = if (contract.expectedDeliveryKg > 0) {
            (totalDeliveredKg / contract.expectedDeliveryKg) * 100
        } else {
            0.0
        }

        // Update repayment status based on delivery percentage and other factors
        contract.repaymentStatus = when {
            deliveryPercentage >= 100 -> RepaymentStatus.COMPLETED
            deliveryPercentage > 0 -> RepaymentStatus.IN_PROGRESS
            contract.endDate.isBefore(LocalDate.now()) && deliveryPercentage < 100 -> RepaymentStatus.DEFAULTED
            else -> RepaymentStatus.NOT_STARTED
        }

        return contractRepository.save(contract)
    }

    @Transactional
    fun deactivateContract(id: UUID): Contract {
        val contract = contractRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Contract with ID $id not found")

        contract.active = false
        return contractRepository.save(contract)
    }

    @Transactional
    fun deleteContract(id: UUID): Boolean {
        val contract = contractRepository.findByIdOrNull(id) ?: return false

        // Check if contract has deliveries
        if (contract.deliveries.isNotEmpty()) {
            throw IllegalStateException("Cannot delete contract with recorded deliveries")
        }

        contractRepository.delete(contract)
        return true
    }

    fun getContractSummary(contractId: UUID): Map<String, Any> {
        val contract = contractRepository.findByIdOrNull(contractId)
            ?: throw IllegalArgumentException("Contract with ID $contractId not found")

        val totalDeliveredKg = contract.getTotalDeliveredKg()
        val completionPercentage = contract.getDeliveryCompletionPercentage()
        val isBehindSchedule = contract.isBehindSchedule()

        return mapOf(
            "id" to contract.id,
            "contractNumber" to contract.contractNumber,
            "farmerName" to contract.farmer.name,
            "startDate" to contract.startDate,
            "endDate" to contract.endDate,
            "type" to contract.type,
            "expectedDeliveryKg" to contract.expectedDeliveryKg,
            "actualDeliveryKg" to totalDeliveredKg,
            "completionPercentage" to completionPercentage,
            "active" to contract.active,
            "repaymentStatus" to contract.repaymentStatus,
            "atRisk" to isBehindSchedule,
            "remainingDays" to if (contract.endDate.isAfter(LocalDate.now())) {
                java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), contract.endDate)
            } else 0,
            "deliveriesCount" to contract.deliveries.size
        )
    }

    /**
     * Find contracts that are behind their expected delivery schedule
     */
    fun findContractsBehindSchedule(): List<Contract> {
        val today = LocalDate.now()
        val potentialContracts = contractRepository.findPotentialContractsBehindSchedule(today)
        return potentialContracts.filter { it.isBehindSchedule() }
    }

    /**
     * Get contract completion trends by month
     */
    fun getContractCompletionTrends(months: Int = 12): List<Map<String, Any>> {
        val today = LocalDate.now()
        val startDate = today.minusMonths(months.toLong())

        val trends = contractRepository.getContractTrendByMonth(startDate, today)

        return trends.map { result ->
            val year = result[0] as Int
            val month = result[1] as Int
            val count = result[2] as Long
            val expectedTotal = result[3] as Double
            val deliveredTotal = result[4] as Double

            val completionRate = if (expectedTotal > 0) {
                (deliveredTotal / expectedTotal) * 100
            } else {
                0.0
            }

            mapOf(
                "year" to year,
                "month" to month,
                "periodLabel" to "$year-${month.toString().padStart(2, '0')}",
                "contractCount" to count,
                "expectedDeliveryKg" to expectedTotal,
                "actualDeliveryKg" to deliveredTotal,
                "completionRate" to completionRate
            )
        }
    }

    /**
     * Get contract insights for decision making
     */
    fun getContractInsights(): Map<String, Any> {
        val today = LocalDate.now()
        val allContracts = contractRepository.findAll()
        val activeContracts = allContracts.filter { it.active && it.endDate.isAfter(today) }

        // Find contracts by their performance
        val contractsByPerformance = activeContracts.groupBy { contract ->
            val completionPercent = contract.getDeliveryCompletionPercentage()
            val timeElapsedPercent = calculateTimeElapsedPercentage(contract)

            when {
                completionPercent >= timeElapsedPercent + 10 -> "AHEAD_OF_SCHEDULE"
                completionPercent >= timeElapsedPercent - 10 -> "ON_SCHEDULE"
                completionPercent < timeElapsedPercent - 30 -> "SEVERELY_BEHIND"
                else -> "BEHIND_SCHEDULE"
            }
        }

        // Calculate performance by contract type
        val performanceByContractType = ContractType.values().associateWith { type ->
            val typeContracts = activeContracts.filter { it.type == type }
            if (typeContracts.isEmpty()) {
                mapOf(
                    "count" to 0,
                    "totalExpectedKg" to 0.0,
                    "totalDeliveredKg" to 0.0,
                    "avgCompletionRate" to 0.0,
                    "onTrackPercentage" to 0.0
                )
            } else {
                val avgCompletionRate = typeContracts.map { it.getDeliveryCompletionPercentage() }.average()
                val onTrackCount = typeContracts.count {
                    val completionPercent = it.getDeliveryCompletionPercentage()
                    val timeElapsedPercent = calculateTimeElapsedPercentage(it)
                    completionPercent >= timeElapsedPercent - 10
                }

                mapOf(
                    "count" to typeContracts.size,
                    "totalExpectedKg" to typeContracts.sumOf { it.expectedDeliveryKg },
                    "totalDeliveredKg" to typeContracts.sumOf { it.getTotalDeliveredKg() },
                    "avgCompletionRate" to avgCompletionRate,
                    "onTrackPercentage" to (onTrackCount.toDouble() / typeContracts.size) * 100
                )
            }
        }

        // Find soon-to-expire contracts that are behind
        val criticalContracts = activeContracts.filter { contract ->
            val daysToExpiry = ChronoUnit.DAYS.between(today, contract.endDate)
            val completionPercent = contract.getDeliveryCompletionPercentage()

            daysToExpiry <= 30 && completionPercent < 80
        }.map { contract ->
            mapOf(
                "id" to contract.id,
                "contractNumber" to contract.contractNumber,
                "farmerName" to contract.farmer.name,
                "farmerId" to contract.farmer.id,
                "endDate" to contract.endDate,
                "daysRemaining" to ChronoUnit.DAYS.between(today, contract.endDate),
                "completionPercentage" to contract.getDeliveryCompletionPercentage(),
                "region" to contract.farmer.region.name
            )
        }

        return mapOf(
            "totalActiveContracts" to activeContracts.size,
            "contractsByPerformance" to contractsByPerformance.mapValues { it.value.size },
            "performanceByContractType" to performanceByContractType,
            "criticalContracts" to criticalContracts,
            "generatedAt" to LocalDate.now()
        )
    }

    /**
     * Calculate what percentage of contract time has elapsed
     */
    private fun calculateTimeElapsedPercentage(contract: Contract): Double {
        val today = LocalDate.now()

        // If contract hasn't started yet, return 0
        if (today.isBefore(contract.startDate)) {
            return 0.0
        }

        // If contract has ended, return 100
        if (today.isAfter(contract.endDate)) {
            return 100.0
        }

        val totalDays = ChronoUnit.DAYS.between(contract.startDate, contract.endDate)
        val elapsedDays = ChronoUnit.DAYS.between(contract.startDate, today)

        return if (totalDays > 0) {
            (elapsedDays.toDouble() / totalDays) * 100
        } else {
            // Same day contract (unusual case)
            50.0
        }
    }

    /**
     * Recommend contract adjustments based on farmer performance
     */
    fun recommendContractAdjustments(farmerId: UUID): Map<String, Any> {
        val farmer = farmerRepository.findByIdOrNull(farmerId)
            ?: throw IllegalArgumentException("Farmer with ID $farmerId not found")

        val contracts = contractRepository.findByFarmerId(farmerId)
        val activeContracts = contracts.filter { it.active }
        val completedContracts = contracts.filter { !it.active && it.getDeliveryCompletionPercentage() >= 100 }

        // If no completed contracts, not enough data for recommendations
        if (completedContracts.isEmpty()) {
            return mapOf(
                "farmerId" to farmerId,
                "farmerName" to farmer.name,
                "hasRecommendation" to false,
                "message" to "Not enough historical contract data for recommendations",
                "generatedAt" to LocalDate.now()
            )
        }

        // Calculate average historical delivery performance
        val avgDeliveryRate = completedContracts.map { it.getDeliveryCompletionPercentage() / 100 }.average()

        // Check if farmer performs better with certain contract types
        val performanceByType = completedContracts
            .groupBy { it.type }
            .mapValues { (_, contracts) ->
                contracts.map { it.getDeliveryCompletionPercentage() / 100 }.average()
            }

        val bestPerformingType = performanceByType.maxByOrNull { it.value }

        // Determine optimal contract size based on historical data
        val avgDeliveryKg = completedContracts
            .map { it.getTotalDeliveredKg() }
            .average()

        // Recommendations to build
        val recommendations = mutableListOf<String>()
        val adjustments = mutableMapOf<String, Any>()

        // Recommend contract type
        if (bestPerformingType != null && bestPerformingType.value > 0.95) {
            recommendations.add("Farmer performs well with ${bestPerformingType.key} contracts (${bestPerformingType.value * 100}% completion rate)")
            adjustments["recommendedType"] = bestPerformingType.key
        }

        // Recommend contract size
        if (avgDeliveryRate < 0.8) {
            // Farmer struggling to meet targets, recommend smaller contracts
            val recommendedSize = avgDeliveryKg * 1.1 // 10% above proven capacity
            recommendations.add("Farmer historically delivers an average of $avgDeliveryKg kg. Consider reducing contract size to ${recommendedSize.toInt()} kg.")
            adjustments["recommendedSizeKg"] = recommendedSize
        } else if (avgDeliveryRate > 0.95 && activeContracts.isNotEmpty()) {
            // Farmer consistently meeting targets, could handle more
            val currentSize = activeContracts.maxOfOrNull { it.expectedDeliveryKg } ?: 0.0
            val recommendedSize = currentSize * 1.2 // 20% increase
            recommendations.add("Farmer consistently meets targets. Consider increasing contract size to ${recommendedSize.toInt()} kg.")
            adjustments["recommendedSizeKg"] = recommendedSize
        }

        // Consider payment structure adjustments
        if (farmer.needsSupport && farmer.socialVulnerability == com.edhumeni.domain.model.VulnerabilityLevel.HIGH) {
            recommendations.add("Consider increasing advance payment percentage to support farmer cash flow")
            adjustments["recommendedAdvancePaymentIncrease"] = true
        }

        return mapOf(
            "farmerId" to farmerId,
            "farmerName" to farmer.name,
            "hasRecommendation" to recommendations.isNotEmpty(),
            "historicalPerformance" to mapOf(
                "avgDeliveryRate" to avgDeliveryRate,
                "avgDeliveryKg" to avgDeliveryKg,
                "completedContractCount" to completedContracts.size,
                "performanceByType" to performanceByType
            ),
            "recommendations" to recommendations,
            "adjustments" to adjustments,
            "generatedAt" to LocalDate.now()
        )
    }
}