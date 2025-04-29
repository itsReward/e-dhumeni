package com.edhumeni.domain.service

import com.edhumeni.domain.model.Farmer
import com.edhumeni.domain.model.SyncLog
import com.edhumeni.domain.model.SyncStatus
import com.edhumeni.domain.repository.FarmerRepository
import com.edhumeni.domain.repository.RegionRepository
import com.edhumeni.domain.repository.SyncLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class OfflineDataSyncService(
    private val farmerRepository: FarmerRepository,
    private val syncLogRepository: SyncLogRepository,
    private val regionRepository: RegionRepository,
) {

    /**
     * Synchronize offline farmer data changes with the server
     */
    @Transactional
    fun syncFarmers(offlineFarmers: List<Map<String, Any>>, userId: String): Map<String, Any> {
        val successfulSyncs = mutableListOf<UUID>()
        val failedSyncs = mutableListOf<Map<String, Any>>()

        for (offlineFarmer in offlineFarmers) {
            try {
                val farmerId = UUID.fromString(offlineFarmer["id"] as String)
                val syncTimestamp = LocalDateTime.parse(offlineFarmer["lastModified"] as String)

                val existingFarmer = farmerRepository.findById(farmerId)

                if (existingFarmer.isPresent()) {
                    val farmer = existingFarmer.get()

                    // Only update if offline change is newer than server data
                    if (syncTimestamp.isAfter(farmer.updatedAt)) {
                        updateFarmerFromOfflineData(farmer, offlineFarmer)
                        farmer.lastUpdatedBy = "$userId (Sync)"
                        farmer.updatedAt = LocalDateTime.now()

                        farmerRepository.save(farmer)

                        // Log the successful sync
                        logSync(farmerId, true, userId, "Farmer data synchronized successfully")

                        successfulSyncs.add(farmerId)
                    } else {
                        // Server data is newer, don't overwrite
                        logSync(farmerId, false, userId, "Server data is newer than offline changes")
                        failedSyncs.add(mapOf(
                            "id" to farmerId,
                            "reason" to "Server data is newer than offline changes"
                        ))
                    }
                } else {
                    // Farmer doesn't exist, log error
                    logSync(farmerId, false, userId, "Farmer not found")
                    failedSyncs.add(mapOf(
                        "id" to farmerId,
                        "reason" to "Farmer not found"
                    ))
                }
            } catch (e: Exception) {
                // Log the error
                failedSyncs.add(mapOf(
                    "id" to (offlineFarmer["id"] ?: "unknown"),
                    "reason" to "Error: ${e.message}"
                ))
            }
        }

        return mapOf(
            "success" to successfulSyncs.size,
            "failed" to failedSyncs.size,
            "successfulSyncs" to successfulSyncs,
            "failedSyncs" to failedSyncs,
            "timestamp" to LocalDateTime.now()
        )
    }

    /**
     * Prepare offline data package for client
     */
    fun prepareOfflineData(regionIds: List<UUID>? = null): Map<String, Any> {
        val farmers = if (regionIds != null && regionIds.isNotEmpty()) {
            regionIds.flatMap { farmerRepository.findByRegionId(it) }
        } else {
            farmerRepository.findAll()
        }

        // Create offline data package
        return mapOf(
            "farmers" to farmers,
            "regions" to (regionIds?.let { regionRepository.findAllById(it) } ?: regionRepository.findAll()),
            "timestamp" to LocalDateTime.now(),
            "expiresAt" to LocalDateTime.now().plusDays(7) // Offline data valid for 7 days
        )
    }

    /**
     * Log synchronization event
     */
    private fun logSync(entityId: UUID, success: Boolean, userId: String, message: String) {
        val syncLog = SyncLog(
            entityId = entityId,
            entityType = "FARMER",
            status = if (success) SyncStatus.SUCCESS else SyncStatus.FAILED,
            syncedBy = userId,
            message = message
        )

        syncLogRepository.save(syncLog)
    }

    /**
     * Update farmer entity from offline data map
     */
    private fun updateFarmerFromOfflineData(farmer: Farmer, offlineData: Map<String, Any>) {
        // Update basic properties
        offlineData["name"]?.let { farmer.name = it as String }
        offlineData["age"]?.let { farmer.age = (it as Number).toInt() }
        offlineData["contactNumber"]?.let { farmer.contactNumber = it as String }
        offlineData["province"]?.let { farmer.province = it as String }
        offlineData["ward"]?.let { farmer.ward = it as String }
        offlineData["usesFertilizer"]?.let { farmer.usesFertilizer = it as Boolean }
        offlineData["fertilizerType"]?.let { farmer.fertilizerType = it as String? }
        offlineData["manureAvailability"]?.let { farmer.manureAvailability = it as Boolean }
        offlineData["usesPloughing"]?.let { farmer.usesPloughing = it as Boolean }
        offlineData["usesPfumvudza"]?.let { farmer.usesPfumvudza = it as Boolean }
        offlineData["accessToCredit"]?.let { farmer.accessToCredit = it as Boolean }
        offlineData["keepsFarmRecords"]?.let { farmer.keepsFarmRecords = it as Boolean }
        offlineData["farmSizeHectares"]?.let { farmer.farmSizeHectares = (it as Number).toDouble() }
        offlineData["previousPlantedCrop"]?.let { farmer.previousPlantedCrop = it as String? }
        offlineData["soilTestingDone"]?.let { farmer.soilTestingDone = it as Boolean }
        offlineData["observedOffTypes"]?.let { farmer.observedOffTypes = it as Boolean }
        offlineData["herbicidesUsed"]?.let { farmer.herbicidesUsed = it as String? }
        offlineData["hasCropInsurance"]?.let { farmer.hasCropInsurance = it as Boolean }
        offlineData["receivesGovtSubsidies"]?.let { farmer.receivesGovtSubsidies = it as Boolean }
        offlineData["usesAgroforestry"]?.let { farmer.usesAgroforestry = it as Boolean }
        offlineData["inputCostPerSeason"]?.let { farmer.inputCostPerSeason = (it as Number).toDouble() }
        offlineData["mainSourceOfInputs"]?.let { farmer.mainSourceOfInputs = it as String? }
        offlineData["householdSize"]?.let { farmer.householdSize = (it as Number).toInt() }
        offlineData["numberOfDependents"]?.let { farmer.numberOfDependents = (it as Number).toInt() }
        offlineData["needsSupport"]?.let { farmer.needsSupport = it as Boolean }
        offlineData["supportReason"]?.let { farmer.supportReason = it as String? }

        // Handle collections if provided
        @Suppress("UNCHECKED_CAST")
        offlineData["farmingPractices"]?.let {
            farmer.farmingPractices = (it as List<String>).toSet()
        }

        @Suppress("UNCHECKED_CAST")
        offlineData["conservationPractices"]?.let {
            farmer.conservationPractices = (it as List<String>).toSet()
        }

        @Suppress("UNCHECKED_CAST")
        offlineData["agronomicPractices"]?.let {
            farmer.agronomicPractices = (it as List<String>).toSet()
        }

        @Suppress("UNCHECKED_CAST")
        offlineData["problematicPests"]?.let {
            farmer.problematicPests = it as List<String>
        }

        @Suppress("UNCHECKED_CAST")
        offlineData["challenges"]?.let {
            farmer.challenges = it as List<String>
        }

        // Handle enums
        offlineData["gender"]?.let {
            farmer.gender = com.edhumeni.domain.model.Gender.valueOf(it as String)
        }

        offlineData["landOwnershipType"]?.let {
            farmer.landOwnershipType = com.edhumeni.domain.model.LandOwnershipType.valueOf(it as String)
        }

        offlineData["complianceLevel"]?.let {
            farmer.complianceLevel = com.edhumeni.domain.model.ComplianceLevel.valueOf(it as String)
        }

        offlineData["landPreparationType"]?.let {
            farmer.landPreparationType = com.edhumeni.domain.model.LandPreparationType.valueOf(it as String)
        }

        offlineData["aeoVisitFrequency"]?.let {
            farmer.aeoVisitFrequency = com.edhumeni.domain.model.VisitFrequency.valueOf(it as String)
        }

        offlineData["socialVulnerability"]?.let {
            farmer.socialVulnerability = com.edhumeni.domain.model.VulnerabilityLevel.valueOf(it as String)
        }

        offlineData["educationLevel"]?.let {
            farmer.educationLevel = com.edhumeni.domain.model.EducationLevel.valueOf(it as String)
        }

        offlineData["maritalStatus"]?.let {
            farmer.maritalStatus = com.edhumeni.domain.model.MaritalStatus.valueOf(it as String)
        }
    }
}