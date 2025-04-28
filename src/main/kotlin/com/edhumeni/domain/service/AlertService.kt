package com.edhumeni.domain.service

import com.edhumeni.domain.model.Farmer
import com.edhumeni.domain.repository.ContractRepository
import com.edhumeni.domain.repository.FarmerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
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
    fun assessAllFarmersForSupport() {
        val allFarmers = farmerRepository.findAll()

        for (farmer in allFarmers) {
            val needsSupport = assessIndividualFarmerForSupport(farmer)
            if (needsSupport.first && farmer.needsSupport != needsSupport.first) {
                farmer.needsSupport = true
                farmer.supportReason = needsSupport.second
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
        val defaultedContracts = activeContracts.filter { it.repaymentStatus == com.edhumeni.domain.model.RepaymentStatus.DEFAULTED }
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

        val needsSupport = reasons.isNotEmpty()
        val reasonString = if (needsSupport) reasons.joinToString("; ") else null

        return Pair(needsSupport, reasonString)
    }

    /**
     * Mark a farmer as needing support
     */
    @Transactional
    fun markFarmerNeedsSupport(farmerId: UUID, reason: String): Farmer {
        val farmer = farmerRepository.findByIdOrNull(farmerId)
            ?: throw IllegalArgumentException("Farmer with ID $farmerId not found")

        farmer.needsSupport = true
        farmer.supportReason = reason

        return farmerRepository.save(farmer)
    }

    /**
     * Mark a farmer as no longer needing support
     */
    @Transactional
    fun resolveSupport(farmerId: UUID, resolutionNotes: String): Farmer {
        val farmer = farmerRepository.findByIdOrNull(farmerId)
            ?: throw IllegalArgumentException("Farmer with ID $farmerId not found")

        if (!farmer.needsSupport) {
            throw IllegalStateException("Farmer is not currently marked as needing support")
        }

        farmer.needsSupport = false
        farmer.supportReason = "RESOLVED: $resolutionNotes"

        return farmerRepository.save(farmer)
    }

    /**
     * Get summary statistics on farmers needing support
     */
    fun getSupportAlertSummary(): Map<String, Any> {
        val farmersNeedingSupport = farmerRepository.findFarmersNeedingSupport()
        val totalFarmers = farmerRepository.count()
        val atRiskContracts = contractRepository.findAtRiskContracts(LocalDate.now(), LocalDate.now().plusMonths(1))

        val supportByReason = farmersNeedingSupport
            .filter { it.supportReason != null }
            .groupBy {
                // Extract the main reason category from the support reason
                it.supportReason!!.split(";").firstOrNull()?.trim() ?: "Other"
            }
            .mapValues { it.value.size }

        return mapOf(
            "totalFarmersNeedingSupport" to farmersNeedingSupport.size,
            "percentageNeedingSupport" to if (totalFarmers > 0)
                (farmersNeedingSupport.size.toDouble() / totalFarmers) * 100 else 0.0,
            "totalAtRiskContracts" to atRiskContracts.size,
            "supportByReason" to supportByReason
        )
    }
}