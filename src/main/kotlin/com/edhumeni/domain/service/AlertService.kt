package com.edhumeni.domain.service

import com.edhumeni.domain.model.Farmer
import com.edhumeni.domain.model.RepaymentStatus
import com.edhumeni.domain.repository.ContractRepository
import com.edhumeni.domain.repository.FarmerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class AlertService(
    private val farmerRepository: FarmerRepository,
    private val contractRepository: ContractRepository
) {
    /**
     * Find all farmers who need support
     */
    fun findAllFarmersNeedingSupport(): List<Farmer> =
        farmerRepository.findFarmersNeedingSupport()

    /**
     * Find farmers needing support in a specific region
     */
    fun findFarmersNeedingSupportByRegion(regionId: UUID): List<Farmer> =
        farmerRepository.findFarmersNeedingSupportByRegion(regionId)

    /**
     * Find farmers with repayment issues (at risk or defaulted)
     */
    fun findFarmersWithRepaymentIssues(): List<Farmer> =
        farmerRepository.findFarmersWithRepaymentIssues()

    /**
     * Find contracts at risk of non-completion
     */
    fun findAtRiskContracts() =
        contractRepository.findAtRiskContracts(LocalDate.now(), LocalDate.now().plusMonths(1))

    /**
     * Automatically assess all farmers to identify those who might need support
     * This could be scheduled to run periodically
     */
    @Transactional
    fun assessAllFarmersForSupport(updatedBy: String = "System") {
        val allFarmers = farmerRepository.findAll()
        val now = LocalDateTime.now()

        for (farmer in allFarmers) {
            val assessment = assessIndividualFarmerForSupport(farmer)
            if (assessment.first && farmer.needsSupport != assessment.first) {
                farmer.needsSupport = true
                farmer.supportReason = assessment.second
                farmer.lastUpdatedBy = updatedBy
                farmer.updatedAt = now
                farmerRepository.save(farmer)
            } else if (!assessment.first && farmer.needsSupport) {
                // If previously needed support but now doesn't, update the reason
                farmer.needsSupport = false
                farmer.supportReason = "Automatically resolved: No current issues detected"
                farmer.lastUpdatedBy = updatedBy
                farmer.updatedAt = now
                farmerRepository.save(farmer)
            }
        }
    }

    /**
     * Assess an individual farmer to determine if they need support
     * Returns a Pair containing (needsSupport, reasonIfAny)
     */
    fun assessIndividualFarmerForSupport(farmer: Farmer): Pair<Boolean, String?> {
        val reasons = mutableListOf<String>()

        // Check for significant yield decline
        if (farmer.previousSeasonYieldKg != null &&
            farmer.averageYieldPerSeasonKg != null &&
            farmer.previousSeasonYieldKg!! < (farmer.averageYieldPerSeasonKg!! * 0.7)) {
            reasons.add("Significant yield decline (>30% below average)")
        }

        // Check for contracts with repayment issues
        val activeContracts = contractRepository.findByFarmerIdAndActive(farmer.id, true)
        val hasContractsAtRisk = activeContracts.any { it.isBehindSchedule() }
        if (hasContractsAtRisk) {
            reasons.add("Contracts at risk of non-delivery")
        }

        // Check for defaulted contracts
        val defaultedContracts = activeContracts.filter {
            it.repaymentStatus == RepaymentStatus.DEFAULTED
        }
        if (defaultedContracts.isNotEmpty()) {
            reasons.add("Defaulted contracts: ${defaultedContracts.size}")
        }

        // Consider social vulnerability
        if (farmer.socialVulnerability == com.edhumeni.domain.model.VulnerabilityLevel.HIGH) {
            reasons.add("High social vulnerability")
        }

        // Check if farmer has reported multiple challenges
        if (farmer.challenges.size >= 3) {
            reasons.add("Multiple challenges reported: ${farmer.challenges.size}")
        }

        // Check AEO visit frequency
        if (farmer.aeoVisitFrequency == com.edhumeni.domain.model.VisitFrequency.NEVER ||
            farmer.aeoVisitFrequency == com.edhumeni.domain.model.VisitFrequency.YEARLY) {
            reasons.add("Insufficient AEO visits")
        }

        // Check for farm size to input cost ratio (potential financial stress)
        if (farmer.inputCostPerSeason != null &&
            farmer.inputCostPerSeason!! > 0 &&
            farmer.farmSizeHectares > 0) {
            val costPerHectare = farmer.inputCostPerSeason!! / farmer.farmSizeHectares
            // Threshold would depend on typical costs in the region
            if (costPerHectare > 1000) { // Example threshold
                reasons.add("High input costs per hectare")
            }
        }

        // Consider education level and farm size (potentially needs additional training)
        if (farmer.educationLevel == com.edhumeni.domain.model.EducationLevel.NONE &&
            farmer.farmSizeHectares > 5) {
            reasons.add("Large farm with limited education level")
        }

        val needsSupport = reasons.isNotEmpty()
        val reasonString = if (needsSupport) reasons.joinToString("; ") else null

        return Pair(needsSupport, reasonString)
    }

    /**
     * Mark a farmer as needing support
     */
    @Transactional
    fun markFarmerNeedsSupport(farmerId: UUID, reason: String, updatedBy: String): Farmer {
        val farmer = farmerRepository.findByIdOrNull(farmerId)
            ?: throw IllegalArgumentException("Farmer with ID $farmerId not found")

        farmer.needsSupport = true
        farmer.supportReason = reason
        farmer.lastUpdatedBy = updatedBy
        farmer.updatedAt = LocalDateTime.now()

        return farmerRepository.save(farmer)
    }

    /**
     * Mark a farmer as no longer needing support
     */
    @Transactional
    fun resolveSupport(farmerId: UUID, resolutionNotes: String, updatedBy: String): Farmer {
        val farmer = farmerRepository.findByIdOrNull(farmerId)
            ?: throw IllegalArgumentException("Farmer with ID $farmerId not found")

        if (!farmer.needsSupport) {
            throw IllegalStateException("Farmer is not currently marked as needing support")
        }

        farmer.needsSupport = false
        farmer.supportReason = "RESOLVED: $resolutionNotes"
        farmer.lastUpdatedBy = updatedBy
        farmer.updatedAt = LocalDateTime.now()

        return farmerRepository.save(farmer)
    }

    /**
     * Get summary statistics on farmers needing support
     */
    fun getSupportAlertSummary(): Map<String, Any> {
        val farmersNeedingSupport = farmerRepository.findFarmersNeedingSupport()
        val totalFarmers = farmerRepository.count()
        val atRiskContracts = contractRepository.findAtRiskContracts(
            LocalDate.now(),
            LocalDate.now().plusMonths(1)
        )

        // Get detailed support reasons
        val supportByReason = farmersNeedingSupport
            .filter { it.supportReason != null }
            .flatMap { farmer ->
                farmer.supportReason!!.split(";").map { it.trim() }
            }
            .groupBy { it }
            .mapValues { it.value.size }
            .filter { it.key.isNotBlank() }

        // Group farmers by region
        val supportByRegion = farmersNeedingSupport
            .groupBy { it.region.name }
            .mapValues { it.value.size }

        // Get support by vulnerability level
        val supportByVulnerability = farmersNeedingSupport
            .groupBy { it.socialVulnerability }
            .mapValues { it.value.size }

        // Get support by education level
        val supportByEducation = farmersNeedingSupport
            .groupBy { it.educationLevel }
            .mapValues { it.value.size }

        return mapOf(
            "totalFarmersNeedingSupport" to farmersNeedingSupport.size,
            "percentageNeedingSupport" to if (totalFarmers > 0)
                (farmersNeedingSupport.size.toDouble() / totalFarmers) * 100 else 0.0,
            "totalAtRiskContracts" to atRiskContracts.size,
            "supportByReason" to supportByReason,
            "supportByRegion" to supportByRegion,
            "supportByVulnerability" to supportByVulnerability,
            "supportByEducation" to supportByEducation,
            "mostCommonReason" to (supportByReason.maxByOrNull { it.value }?.key ?: "None"),
            "lastUpdated" to LocalDateTime.now()
        )
    }

    /**
     * Get farmers that need immediate attention based on multiple criteria
     */
    fun getHighPriorityFarmers(): List<Farmer> {
        val atRiskFarmers = farmerRepository.findFarmersNeedingSupport()

        // Filter to get only the highest priority cases
        return atRiskFarmers.filter { farmer ->
            // Has defaulted contracts
            val hasDefaultedContracts = contractRepository.findByFarmerId(farmer.id)
                .any { it.repaymentStatus == RepaymentStatus.DEFAULTED }

            // Has high vulnerability
            val isHighlyVulnerable = farmer.socialVulnerability ==
                    com.edhumeni.domain.model.VulnerabilityLevel.HIGH

            // Has significant yield decline
            val hasYieldDecline = farmer.previousSeasonYieldKg != null &&
                    farmer.averageYieldPerSeasonKg != null &&
                    farmer.previousSeasonYieldKg!! < (farmer.averageYieldPerSeasonKg!! * 0.5) // 50% decline

            // Return farmers meeting at least two of the high priority criteria
            val criteriaMet = listOf(hasDefaultedContracts, isHighlyVulnerable, hasYieldDecline)
                .count { it } >= 2

            criteriaMet
        }
    }

    /**
     * Scheduled task to assess all farmers for support needs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    fun scheduledFarmerAssessment() {
        assessAllFarmersForSupport("System (Scheduled)")
    }

    /**
     * Scheduled task to identify high-risk contracts weekly on Monday at 1 AM
     */
    @Scheduled(cron = "0 0 1 * * MON") // Run at 1 AM every Monday
    @Transactional
    fun identifyHighRiskContracts() {
        val today = LocalDate.now()
        val nextMonth = today.plusMonths(1)

        // Find contracts ending in the next month with low completion
        val atRiskContracts = contractRepository.findAtRiskContracts(today, nextMonth)

        // Update farmers with at-risk contracts to need support
        atRiskContracts.forEach { contract ->
            val farmer = contract.farmer
            if (!farmer.needsSupport) {
                farmer.needsSupport = true
                farmer.supportReason = "Contract ${contract.contractNumber} at risk of non-completion"
                farmer.lastUpdatedBy = "System (Scheduled Risk Assessment)"
                farmer.updatedAt = LocalDateTime.now()
                farmerRepository.save(farmer)
            }
        }
    }
}