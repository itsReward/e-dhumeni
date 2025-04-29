package com.edhumeni.domain.service

import com.edhumeni.domain.model.Farmer
import com.edhumeni.domain.model.VulnerabilityLevel
import com.edhumeni.domain.repository.ContractRepository
import com.edhumeni.domain.repository.DeliveryRepository
import com.edhumeni.domain.repository.FarmerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class FarmerAssessmentService(
    private val farmerRepository: FarmerRepository,
    private val contractRepository: ContractRepository,
    private val deliveryRepository: DeliveryRepository
) {

    /**
     * Calculate a composite risk score for a farmer (0-100)
     * Higher score means higher risk
     */
    fun calculateFarmerRiskScore(farmerId: UUID): Map<String, Any> {
        val farmer = farmerRepository.findByIdOrNull(farmerId)
            ?: throw IllegalArgumentException("Farmer with ID $farmerId not found")

        var riskScore = 0.0
        val riskFactors = mutableMapOf<String, Any>()

        // 1. Contract Delivery Performance (0-30 points)
        val contracts = contractRepository.findByFarmerId(farmerId)
        if (contracts.isNotEmpty()) {
            val totalContracted = contracts.sumOf { it.expectedDeliveryKg }
            val totalDelivered = contracts.sumOf { it.getTotalDeliveredKg() }

            val deliveryPercentage = if (totalContracted > 0) {
                (totalDelivered / totalContracted) * 100
            } else {
                0.0
            }

            // Lower delivery percentage = higher risk
            val deliveryRisk = when {
                deliveryPercentage >= 90 -> 0
                deliveryPercentage >= 80 -> 5
                deliveryPercentage >= 70 -> 10
                deliveryPercentage >= 60 -> 15
                deliveryPercentage >= 50 -> 20
                else -> 30
            }

            riskScore += deliveryRisk
            riskFactors["deliveryPerformance"] = mapOf(
                "score" to deliveryRisk,
                "percentage" to deliveryPercentage,
                "maxScore" to 30
            )
        }

        // 2. Yield Trend Risk (0-20 points)
        if (farmer.previousSeasonYieldKg != null && farmer.averageYieldPerSeasonKg != null) {
            val yieldRatio = farmer.previousSeasonYieldKg!! / farmer.averageYieldPerSeasonKg!!

            val yieldRisk = when {
                yieldRatio >= 1.1 -> 0 // yield improving
                yieldRatio >= 0.9 -> 5 // maintaining yield
                yieldRatio >= 0.8 -> 10
                yieldRatio >= 0.7 -> 15
                else -> 20 // significant yield drop
            }

            riskScore += yieldRisk
            riskFactors["yieldTrend"] = mapOf(
                "score" to yieldRisk,
                "ratio" to yieldRatio,
                "maxScore" to 20
            )
        }

        // 3. Social Vulnerability (0-15 points)
        val vulnerabilityRisk = when (farmer.socialVulnerability) {
            VulnerabilityLevel.LOW -> 0
            VulnerabilityLevel.MEDIUM -> 7
            VulnerabilityLevel.HIGH -> 15
        }

        riskScore += vulnerabilityRisk
        riskFactors["socialVulnerability"] = mapOf(
            "score" to vulnerabilityRisk,
            "level" to farmer.socialVulnerability,
            "maxScore" to 15
        )

        // 4. Agricultural Practices Risk (0-15 points)
        var practicesRisk = 0

        // Check for key agricultural practices
        if (!farmer.usesFertilizer) practicesRisk += 3
        if (!farmer.soilTestingDone) practicesRisk += 3
        if (!farmer.keepsFarmRecords) practicesRisk += 3
        if (farmer.conservationPractices.isEmpty()) practicesRisk += 3
        if (farmer.farmingPractices.isEmpty()) practicesRisk += 3

        // Cap at 15
        practicesRisk = minOf(practicesRisk, 15)

        riskScore += practicesRisk
        riskFactors["agriculturalPractices"] = mapOf(
            "score" to practicesRisk,
            "maxScore" to 15
        )

        // 5. Support Network Risk (0-10 points)
        var supportRisk = 0

        // Check AEO visit frequency
        supportRisk += when (farmer.aeoVisitFrequency) {
            com.edhumeni.domain.model.VisitFrequency.WEEKLY -> 0
            com.edhumeni.domain.model.VisitFrequency.BIWEEKLY -> 1
            com.edhumeni.domain.model.VisitFrequency.MONTHLY -> 3
            com.edhumeni.domain.model.VisitFrequency.QUARTERLY -> 5
            com.edhumeni.domain.model.VisitFrequency.YEARLY -> 8
            com.edhumeni.domain.model.VisitFrequency.NEVER -> 10
        }

        riskScore += supportRisk
        riskFactors["supportNetwork"] = mapOf(
            "score" to supportRisk,
            "visitFrequency" to farmer.aeoVisitFrequency,
            "maxScore" to 10
        )

        // 6. Financial Risk (0-10 points)
        var financialRisk = 0

        if (!farmer.accessToCredit) financialRisk += 3
        if (!farmer.hasCropInsurance) financialRisk += 3

        // Check if input costs are high relative to farm size
        if (farmer.inputCostPerSeason != null && farmer.inputCostPerSeason!! > 0 &&
            farmer.farmSizeHectares > 0) {
            val costPerHectare = farmer.inputCostPerSeason!! / farmer.farmSizeHectares
            if (costPerHectare > 1000) financialRisk += 4 // Example threshold
        }

        // Cap at 10
        financialRisk = minOf(financialRisk, 10)

        riskScore += financialRisk
        riskFactors["financialRisk"] = mapOf(
            "score" to financialRisk,
            "maxScore" to 10
        )

        // Calculate risk level based on score
        val riskLevel = when {
            riskScore >= 70 -> "VERY_HIGH"
            riskScore >= 50 -> "HIGH"
            riskScore >= 30 -> "MEDIUM"
            riskScore >= 15 -> "LOW"
            else -> "VERY_LOW"
        }

        return mapOf(
            "farmerId" to farmer.id,
            "farmerName" to farmer.name,
            "riskScore" to riskScore,
            "riskLevel" to riskLevel,
            "riskFactors" to riskFactors,
            "recommendedActions" to generateRecommendations(farmer, riskFactors),
            "assessedAt" to LocalDate.now()
        )
    }

    /**
     * Generate recommendations based on risk factors
     */
    private fun generateRecommendations(
        farmer: Farmer,
        riskFactors: Map<String, Any>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Check delivery performance
        val deliveryPerformance = riskFactors["deliveryPerformance"] as? Map<*, *>
        if (deliveryPerformance != null && (deliveryPerformance["score"] as Int) > 10) {
            recommendations.add("Schedule a meeting to discuss contract delivery challenges")
            recommendations.add("Provide additional support for meeting delivery targets")
        }

        // Check yield trend
        val yieldTrend = riskFactors["yieldTrend"] as? Map<*, *>
        if (yieldTrend != null && (yieldTrend["score"] as Int) > 10) {
            recommendations.add("Conduct soil testing and fertility assessment")
            recommendations.add("Provide training on improved farming practices")
        }

        // Check agricultural practices
        val practices = riskFactors["agriculturalPractices"] as? Map<*, *>
        if (practices != null && (practices["score"] as Int) > 7) {
            if (!farmer.usesFertilizer) {
                recommendations.add("Provide guidance on appropriate fertilizer usage")
            }
            if (!farmer.soilTestingDone) {
                recommendations.add("Arrange for soil testing")
            }
            if (!farmer.keepsFarmRecords) {
                recommendations.add("Provide training on farm record keeping")
            }
            if (farmer.conservationPractices.isEmpty()) {
                recommendations.add("Introduce conservation farming techniques")
            }
        }

        // Check support network
        val support = riskFactors["supportNetwork"] as? Map<*, *>
        if (support != null && (support["score"] as Int) > 5) {
            recommendations.add("Increase AEO visit frequency")
            recommendations.add("Consider enrolling in farmer support group")
        }

        // Check financial risk
        val financial = riskFactors["financialRisk"] as? Map<*, *>
        if (financial != null && (financial["score"] as Int) > 5) {
            if (!farmer.accessToCredit) {
                recommendations.add("Provide information about available credit facilities")
            }
            if (!farmer.hasCropInsurance) {
                recommendations.add("Educate on benefits of crop insurance")
            }
        }

        return recommendations
    }

    /**
     * Analyze a region's farmers to identify patterns and intervention needs
     */
    fun analyzeRegionFarmers(regionId: UUID): Map<String, Any> {
        val farmers = farmerRepository.findByRegionId(regionId)

        if (farmers.isEmpty()) {
            throw IllegalArgumentException("No farmers found in region with ID $regionId")
        }

        // Calculate high-risk farmer percentage
        val highRiskFarmers = farmers.count { farmer ->
            val riskFactors = calculateFarmerRiskScore(farmer.id)
            (riskFactors["riskLevel"] as String) == "HIGH" ||
                    (riskFactors["riskLevel"] as String) == "VERY_HIGH"
        }

        val highRiskPercentage = (highRiskFarmers.toDouble() / farmers.size) * 100

        // Analyze farming practices distribution
        val farmingPracticesDistribution = mutableMapOf<String, Int>()
        farmers.forEach { farmer ->
            farmer.farmingPractices.forEach { practice ->
                farmingPracticesDistribution[practice] =
                    farmingPracticesDistribution.getOrDefault(practice, 0) + 1
            }
        }

        // Analyze conservation practices
        val conservationPracticesDistribution = mutableMapOf<String, Int>()
        farmers.forEach { farmer ->
            farmer.conservationPractices.forEach { practice ->
                conservationPracticesDistribution[practice] =
                    conservationPracticesDistribution.getOrDefault(practice, 0) + 1
            }
        }

        // Analyze challenges reported
        val challengesDistribution = mutableMapOf<String, Int>()
        farmers.forEach { farmer ->
            farmer.challenges.forEach { challenge ->
                challengesDistribution[challenge] =
                    challengesDistribution.getOrDefault(challenge, 0) + 1
            }
        }

        // Find most common support reasons
        val supportReasons = farmers
            .filter { it.needsSupport && it.supportReason != null }
            .flatMap { it.supportReason!!.split(";").map { reason -> reason.trim() } }
            .groupBy { it }
            .mapValues { it.value.size }
            .filter { it.key.isNotBlank() }

        // Calculate average farm size
        val avgFarmSize = farmers.map { it.farmSizeHectares }.average()

        // Calculate average yield
        val avgYield = farmers
            .mapNotNull { it.averageYieldPerSeasonKg }
            .let { if (it.isNotEmpty()) it.average() else null }

        // Generate regional intervention recommendations
        val recommendations = mutableListOf<String>()

        // Check high risk percentage
        if (highRiskPercentage > 30) {
            recommendations.add("Conduct urgent regional assessment with increased AEO presence")
        }

        // Check for prevalent challenges
        val topChallenges = challengesDistribution.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        topChallenges.forEach { challenge ->
            recommendations.add("Address common challenge: $challenge through targeted training")
        }

        // Check for missing farming practices
        val lowAdoptionPractices = farmingPracticesDistribution.entries
            .filter { it.value < (farmers.size * 0.3) } // less than 30% adoption
            .map { it.key }

        if (lowAdoptionPractices.isNotEmpty()) {
            recommendations.add("Promote adoption of practices: ${lowAdoptionPractices.joinToString(", ")}")
        }

        return mapOf(
            "regionId" to regionId,
            "totalFarmers" to farmers.size,
            "highRiskFarmers" to highRiskFarmers,
            "highRiskPercentage" to highRiskPercentage,
            "farmingPracticesDistribution" to farmingPracticesDistribution,
            "conservationPracticesDistribution" to conservationPracticesDistribution,
            "challengesDistribution" to challengesDistribution,
            "supportReasons" to supportReasons,
            "averageFarmSize" to avgFarmSize,
            "averageYield" to avgYield,
            "recommendedInterventions" to recommendations,
            "analyzedAt" to LocalDate.now()
        ) as Map<String, Any>
    }
}