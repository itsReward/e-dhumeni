package com.edhumeni.domain.service

import com.edhumeni.domain.repository.ContractRepository
import com.edhumeni.domain.repository.DeliveryRepository
import com.edhumeni.domain.repository.FarmerRepository
import com.edhumeni.domain.repository.RegionRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class DashboardService(
    private val farmerRepository: FarmerRepository,
    private val contractRepository: ContractRepository,
    private val deliveryRepository: DeliveryRepository,
    private val regionRepository: RegionRepository
) {

    /**
     * Get summary statistics for the dashboard
     */
    fun getDashboardSummary(): Map<String, Any> {
        val totalFarmers = farmerRepository.count()
        val totalContracts = contractRepository.count()
        val activeContracts = contractRepository.findByEndDateAfterAndActive(LocalDate.now(), true).size
        val supportNeededCount = farmerRepository.findFarmersNeedingSupport().size

        val atRiskContracts = contractRepository.findAtRiskContracts(
            LocalDate.now(),
            LocalDate.now().plusMonths(1)
        ).size

        val totalRegions = regionRepository.count()

        // Calculate total delivery amount for active contracts
        val activeContractIds = contractRepository.findByEndDateAfterAndActive(LocalDate.now(), true)
            .map { it.id }

        val totalDeliveredKg = if (activeContractIds.isNotEmpty()) {
            deliveryRepository.sumQuantityByContractIds(activeContractIds) ?: 0.0
        } else {
            0.0
        }

        // Get regions with most farmers
        val topRegions = regionRepository.findTopRegionsByFarmerCount(5)
            .map { result ->
                mapOf(
                    "regionName" to (result[0] as String),
                    "farmerCount" to (result[1] as Long)
                )
            }

        // Get 30-day delivery trends
        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
        val deliveryTrend = deliveryRepository.findDeliveryTrendByDay(thirtyDaysAgo)
            .map { result ->
                mapOf(
                    "date" to (result[0] as LocalDate),
                    "totalQuantity" to (result[1] as Double)
                )
            }

        return mapOf(
            "totalFarmers" to totalFarmers,
            "totalContracts" to totalContracts,
            "activeContracts" to activeContracts,
            "farmersNeedingSupport" to supportNeededCount,
            "supportPercentage" to if (totalFarmers > 0) (supportNeededCount.toDouble() / totalFarmers) * 100 else 0.0,
            "atRiskContracts" to atRiskContracts,
            "totalRegions" to totalRegions,
            "totalDeliveredKg" to totalDeliveredKg,
            "topRegionsByFarmers" to topRegions,
            "deliveryTrend" to deliveryTrend,
            "lastUpdated" to LocalDateTime.now()
        )
    }

    /**
     * Get farmer statistics by region
     */
    fun getFarmerStatsByRegion(): List<Map<String, Any>> {
        return regionRepository.findAllWithFarmerAndSupportCounts().map { result ->
            mapOf(
                "regionId" to result.getId(),
                "regionName" to result.getName(),
                "province" to result.getProvince(),
                "district" to result.getDistrict(),
                "farmerCount" to result.getFarmerCount(),
                "supportCount" to result.getSupportCount(),
                "supportPercentage" to if (result.getFarmerCount() > 0)
                    (result.getSupportCount().toDouble() / result.getFarmerCount()) * 100 else 0.0
            )
        }
    }

    /**
     * Get contract completion statistics
     */
    fun getContractCompletionStats(): Map<String, Any> {
        val allContracts = contractRepository.findAll()
        val completedContracts = allContracts.filter {
            it.getDeliveryCompletionPercentage() >= 100.0
        }
        val partiallyCompletedContracts = allContracts.filter {
            val percentage = it.getDeliveryCompletionPercentage()
            percentage > 0.0 && percentage < 100.0
        }
        val notStartedContracts = allContracts.filter {
            it.getDeliveryCompletionPercentage() == 0.0
        }

        // Group by contract type
        val completionByType = allContracts
            .groupBy { it.type }
            .mapValues { (_, contracts) ->
                val completed = contracts.count { it.getDeliveryCompletionPercentage() >= 100.0 }
                val total = contracts.size

                mapOf(
                    "completed" to completed,
                    "total" to total,
                    "percentage" to if (total > 0) (completed.toDouble() / total) * 100 else 0.0
                )
            }

        return mapOf(
            "totalContracts" to allContracts.size,
            "completedContracts" to completedContracts.size,
            "completionPercentage" to if (allContracts.isNotEmpty())
                (completedContracts.size.toDouble() / allContracts.size) * 100 else 0.0,
            "partiallyCompletedContracts" to partiallyCompletedContracts.size,
            "notStartedContracts" to notStartedContracts.size,
            "completionByType" to completionByType
        )
    }

    /**
     * Get delivery quality statistics
     */
    fun getDeliveryQualityStats(): Map<String, Any> {
        val allDeliveries = deliveryRepository.findAll()

        // Group by quality grade
        val deliveriesByQuality = allDeliveries
            .groupBy { it.qualityGrade }
            .mapValues { it.value.size }

        // Calculate percentage by quality
        val qualityDistribution = deliveriesByQuality.mapValues { (_, count) ->
            if (allDeliveries.isNotEmpty()) {
                (count.toDouble() / allDeliveries.size) * 100
            } else {
                0.0
            }
        }

        // Calculate average delivery size
        val averageDeliverySize = if (allDeliveries.isNotEmpty()) {
            allDeliveries.sumOf { it.quantityKg } / allDeliveries.size
        } else {
            0.0
        }

        return mapOf(
            "totalDeliveries" to allDeliveries.size,
            "deliveriesByQuality" to deliveriesByQuality,
            "qualityDistribution" to qualityDistribution,
            "averageDeliverySize" to averageDeliverySize
        )
    }

    /**
     * Get time-based contract statistics
     */
    fun getContractTimeStats(): Map<String, Any> {
        val today = LocalDate.now()
        val allContracts = contractRepository.findAll()

        // Calculate average contract duration in days
        val avgDuration = if (allContracts.isNotEmpty()) {
            allContracts.sumOf { ChronoUnit.DAYS.between(it.startDate, it.endDate) } / allContracts.size
        } else {
            0L
        }

        // Count contracts by time status
        val activeContracts = allContracts.count {
            it.active && it.endDate.isAfter(today)
        }
        val expiredContracts = allContracts.count {
            it.endDate.isBefore(today)
        }
        val expiringSoonContracts = allContracts.count {
            it.active &&
                    it.endDate.isAfter(today) &&
                    it.endDate.isBefore(today.plusDays(30))
        }

        return mapOf(
            "averageContractDuration" to avgDuration,
            "activeContracts" to activeContracts,
            "expiredContracts" to expiredContracts,
            "expiringSoonContracts" to expiringSoonContracts,
            "contractsByType" to allContracts.groupBy { it.type }.mapValues { it.value.size }
        )
    }
}