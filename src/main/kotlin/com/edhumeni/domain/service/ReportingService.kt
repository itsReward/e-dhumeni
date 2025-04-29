package com.edhumeni.domain.service

import com.edhumeni.domain.model.Farmer
import com.edhumeni.domain.repository.ContractRepository
import com.edhumeni.domain.repository.DeliveryRepository
import com.edhumeni.domain.repository.FarmerRepository
import com.edhumeni.domain.repository.RegionRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class ReportingService(
    private val farmerRepository: FarmerRepository,
    private val contractRepository: ContractRepository,
    private val deliveryRepository: DeliveryRepository,
    private val regionRepository: RegionRepository
) {

    /**
     * Generate a farmer performance report
     */
    fun generateFarmerPerformanceReport(farmerId: UUID): Map<String, Any> {
        val farmer = farmerRepository.findById(farmerId)
            .orElseThrow { IllegalArgumentException("Farmer with ID $farmerId not found") }

        val contracts = contractRepository.findByFarmerId(farmerId)
        val deliveries = deliveryRepository.findByContractFarmerId(farmerId)

        // Calculate total contracted amount
        val totalContractedAmount = contracts.sumOf { it.expectedDeliveryKg }

        // Calculate total delivered amount
        val totalDeliveredAmount = deliveries.sumOf { it.quantityKg }

        // Calculate delivery percentage
        val deliveryPercentage = if (totalContractedAmount > 0) {
            (totalDeliveredAmount / totalContractedAmount) * 100
        } else {
            0.0
        }

        // Calculate average delivery quality
        val qualityDistribution = deliveries
            .groupBy { it.qualityGrade }
            .mapValues { (_, gradeDeliveries) ->
                gradeDeliveries.size.toDouble() / deliveries.size * 100
            }

        // Get active contracts
        val activeContracts = contracts.filter { it.active }

        // Get contract completion stats
        val contractCompletion = contracts.map { contract ->
            mapOf(
                "contractNumber" to contract.contractNumber,
                "expectedDeliveryKg" to contract.expectedDeliveryKg,
                "actualDeliveryKg" to contract.getTotalDeliveredKg(),
                "completionPercentage" to contract.getDeliveryCompletionPercentage(),
                "active" to contract.active,
                "behindSchedule" to contract.isBehindSchedule()
            )
        }

        return mapOf(
            "farmerId" to farmer.id,
            "farmerName" to farmer.name,
            "region" to farmer.region.name,
            "totalContracts" to contracts.size,
            "activeContracts" to activeContracts.size,
            "totalContractedAmount" to totalContractedAmount,
            "totalDeliveredAmount" to totalDeliveredAmount,
            "deliveryPercentage" to deliveryPercentage,
            "qualityDistribution" to qualityDistribution,
            "contractDetails" to contractCompletion,
            "generatedAt" to LocalDateTime.now()
        )
    }

    /**
     * Generate a region performance report
     */
    fun generateRegionPerformanceReport(regionId: UUID): Map<String, Any> {
        val region = regionRepository.findById(regionId)
            .orElseThrow { IllegalArgumentException("Region with ID $regionId not found") }

        val farmers = farmerRepository.findByRegionId(regionId)

        // Count farmers by support status
        val farmersNeedingSupport = farmers.count { it.needsSupport }

        // Get all contracts for farmers in this region
        val farmerIds = farmers.map { it.id }
        val contracts = farmerIds.flatMap { contractRepository.findByFarmerId(it) }

        // Calculate contract statistics
        val activeContracts = contracts.count { it.active }
        val completedContracts = contracts.count {
            it.getDeliveryCompletionPercentage() >= 100.0
        }
        val atRiskContracts = contracts.count { it.isBehindSchedule() }

        // Calculate total delivery statistics
        val totalContractedAmount = contracts.sumOf { it.expectedDeliveryKg }
        val totalDeliveredAmount = contracts.sumOf { it.getTotalDeliveredKg() }

        // Calculate overall delivery percentage
        val deliveryPercentage = if (totalContractedAmount > 0) {
            (totalDeliveredAmount / totalContractedAmount) * 100
        } else {
            0.0
        }

        // Top performing farmers in the region (by delivery percentage)
        val topFarmers = farmers
            .map { farmer ->
                val farmerContracts = contracts.filter { it.farmer.id == farmer.id }
                val contractedAmount = farmerContracts.sumOf { it.expectedDeliveryKg }
                val deliveredAmount = farmerContracts.sumOf { it.getTotalDeliveredKg() }
                val percentage = if (contractedAmount > 0) {
                    (deliveredAmount / contractedAmount) * 100
                } else {
                    0.0
                }

                mapOf(
                    "farmerId" to farmer.id,
                    "farmerName" to farmer.name,
                    "contractedAmount" to contractedAmount,
                    "deliveredAmount" to deliveredAmount,
                    "deliveryPercentage" to percentage
                )
            }
            .filter { (it["contractedAmount"] as Double) > 0 }
            .sortedByDescending { it["deliveryPercentage"] as Double }
            .take(5)

        // Farmers needing support
        val supportFarmers = farmers
            .filter { it.needsSupport }
            .map { farmer ->
                mapOf(
                    "farmerId" to farmer.id,
                    "farmerName" to farmer.name,
                    "supportReason" to (farmer.supportReason ?: "Unknown")
                )
            }

        return mapOf(
            "regionId" to region.id,
            "regionName" to region.name,
            "province" to region.province,
            "district" to region.district,
            "totalFarmers" to farmers.size,
            "farmersNeedingSupport" to farmersNeedingSupport,
            "supportPercentage" to if (farmers.isNotEmpty()) {
                (farmersNeedingSupport.toDouble() / farmers.size) * 100
            } else {
                0.0
            },
            "totalContracts" to contracts.size,
            "activeContracts" to activeContracts,
            "completedContracts" to completedContracts,
            "atRiskContracts" to atRiskContracts,
            "totalContractedAmount" to totalContractedAmount,
            "totalDeliveredAmount" to totalDeliveredAmount,
            "deliveryPercentage" to deliveryPercentage,
            "topPerformingFarmers" to topFarmers,
            "farmersNeedingSupport" to supportFarmers,
            "generatedAt" to LocalDateTime.now()
        )
    }

    /**
     * Generate an overall system performance report
     */
    fun generateSystemPerformanceReport(): Map<String, Any> {
        val allFarmers = farmerRepository.count()
        val allContracts = contractRepository.count()
        val allDeliveries = deliveryRepository.count()
        val allRegions = regionRepository.count()

        // Support statistics
        val farmersNeedingSupport = farmerRepository.findFarmersNeedingSupport().size

        // Contract statistics
        val contracts = contractRepository.findAll()
        val activeContracts = contracts.count { it.active && it.endDate.isAfter(LocalDate.now()) }
        val completedContracts = contracts.count { it.getDeliveryCompletionPercentage() >= 100.0 }
        val atRiskContracts = contracts.count { it.isBehindSchedule() }

        // Delivery statistics
        val totalContractedAmount = contracts.sumOf { it.expectedDeliveryKg }
        val totalDeliveredAmount = contracts.sumOf { it.getTotalDeliveredKg() }

        // Calculate overall delivery percentage
        val deliveryPercentage = if (totalContractedAmount > 0) {
            (totalDeliveredAmount / totalContractedAmount) * 100
        } else {
            0.0
        }

        // Region statistics
        val regionStats = regionRepository.findAllWithFarmerAndSupportCounts().map { result ->
            mapOf(
                "regionId" to result.getId(),
                "regionName" to result.getName(),
                "province" to result.getProvince(),
                "district" to result.getDistrict(),
                "farmerCount" to result.getFarmerCount(),
                "supportCount" to result.getSupportCount()
            )
        }

        return mapOf(
            "totalFarmers" to allFarmers,
            "totalContracts" to allContracts,
            "totalDeliveries" to allDeliveries,
            "totalRegions" to allRegions,
            "farmersNeedingSupport" to farmersNeedingSupport,
            "supportPercentage" to if (allFarmers > 0) {
                (farmersNeedingSupport.toDouble() / allFarmers) * 100
            } else {
                0.0
            },
            "activeContracts" to activeContracts,
            "completedContracts" to completedContracts,
            "atRiskContracts" to atRiskContracts,
            "totalContractedAmount" to totalContractedAmount,
            "totalDeliveredAmount" to totalDeliveredAmount,
            "deliveryPercentage" to deliveryPercentage,
            "regionStatistics" to regionStats,
            "generatedAt" to LocalDateTime.now()
        )
    }
}