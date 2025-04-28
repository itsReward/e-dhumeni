package com.edhumeni.domain.service

import com.edhumeni.domain.model.Farmer
import com.edhumeni.domain.repository.AeoRepository
import com.edhumeni.domain.repository.FarmerRepository
import com.edhumeni.domain.repository.RegionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class FarmerService(
    private val farmerRepository: FarmerRepository,
    private val regionRepository: RegionRepository,
    private val aeoRepository: AeoRepository
) {

    fun findAll(): List<Farmer> = farmerRepository.findAll()

    fun findById(id: UUID): Farmer? = farmerRepository.findByIdOrNull(id)

    fun findByRegionId(regionId: UUID): List<Farmer> = farmerRepository.findByRegionId(regionId)

    fun findByAeoId(aeoId: UUID): List<Farmer> = farmerRepository.findByAgriculturalExtensionOfficerId(aeoId)

    fun findFarmersNeedingSupport(): List<Farmer> = farmerRepository.findFarmersNeedingSupport()

    fun findFarmersNeedingSupportByRegion(regionId: UUID): List<Farmer> =
        farmerRepository.findFarmersNeedingSupportByRegion(regionId)

    fun countFarmersByRegion(regionId: UUID): Long = farmerRepository.countFarmersByRegion(regionId)

    fun findFarmersWithRepaymentIssues(): List<Farmer> = farmerRepository.findFarmersWithRepaymentIssues()

    fun searchFarmers(
        name: String? = null,
        region: String? = null,
        province: String? = null,
        ward: String? = null
    ): List<Farmer> = farmerRepository.searchFarmers(name, region, province, ward)

    @Transactional
    fun createFarmer(farmer: Farmer, regionId: UUID, aeoId: UUID?): Farmer {
        val region = regionRepository.findByIdOrNull(regionId)
            ?: throw IllegalArgumentException("Region with ID $regionId not found")

        farmer.region = region

        if (aeoId != null) {
            val aeo = aeoRepository.findByIdOrNull(aeoId)
                ?: throw IllegalArgumentException("AEO with ID $aeoId not found")
            farmer.agriculturalExtensionOfficer = aeo
        }

        return farmerRepository.save(farmer)
    }

    @Transactional
    fun updateFarmer(id: UUID, updatedFarmer: Farmer, updatedBy: String): Farmer {
        val existingFarmer = farmerRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Farmer with ID $id not found")

        // Update only the fields that are provided
        with(existingFarmer) {
            name = updatedFarmer.name
            age = updatedFarmer.age
            gender = updatedFarmer.gender
            contactNumber = updatedFarmer.contactNumber
            province = updatedFarmer.province
            ward = updatedFarmer.ward
            naturalRegion = updatedFarmer.naturalRegion
            soilType = updatedFarmer.soilType
            usesFertilizer = updatedFarmer.usesFertilizer
            fertilizerType = updatedFarmer.fertilizerType
            manureAvailability = updatedFarmer.manureAvailability
            usesPloughing = updatedFarmer.usesPloughing
            usesPfumvudza = updatedFarmer.usesPfumvudza
            accessToCredit = updatedFarmer.accessToCredit
            landOwnershipType = updatedFarmer.landOwnershipType
            keepsFarmRecords = updatedFarmer.keepsFarmRecords
            farmSizeHectares = updatedFarmer.farmSizeHectares
            previousPlantedCrop = updatedFarmer.previousPlantedCrop
            previousSeasonYieldKg = updatedFarmer.previousSeasonYieldKg
            averageYieldPerSeasonKg = updatedFarmer.averageYieldPerSeasonKg
            farmingPractices = updatedFarmer.farmingPractices
            conservationPractices = updatedFarmer.conservationPractices
            complianceLevel = updatedFarmer.complianceLevel
            agronomicPractices = updatedFarmer.agronomicPractices
            landPreparationType = updatedFarmer.landPreparationType
            soilTestingDone = updatedFarmer.soilTestingDone
            plantingDate = updatedFarmer.plantingDate
            observedOffTypes = updatedFarmer.observedOffTypes
            herbicidesUsed = updatedFarmer.herbicidesUsed
            problematicPests = updatedFarmer.problematicPests
            aeoVisitFrequency = updatedFarmer.aeoVisitFrequency
            challenges = updatedFarmer.challenges
            hasCropInsurance = updatedFarmer.hasCropInsurance
            receivesGovtSubsidies = updatedFarmer.receivesGovtSubsidies
            usesAgroforestry = updatedFarmer.usesAgroforestry
            inputCostPerSeason = updatedFarmer.inputCostPerSeason
            mainSourceOfInputs = updatedFarmer.mainSourceOfInputs
            socialVulnerability = updatedFarmer.socialVulnerability
            educationLevel = updatedFarmer.educationLevel
            householdSize = updatedFarmer.householdSize
            numberOfDependents = updatedFarmer.numberOfDependents
            maritalStatus = updatedFarmer.maritalStatus
            needsSupport = updatedFarmer.needsSupport
            supportReason = updatedFarmer.supportReason
            updatedAt = LocalDateTime.now()
            lastUpdatedBy = updatedBy
        }

        // Update region if provided
        if (updatedFarmer.region.id != existingFarmer.region.id) {
            val newRegion = regionRepository.findByIdOrNull(updatedFarmer.region.id)
                ?: throw IllegalArgumentException("Region with ID ${updatedFarmer.region.id} not found")
            existingFarmer.region = newRegion
        }

        // Update AEO if provided
        if (updatedFarmer.agriculturalExtensionOfficer?.id != existingFarmer.agriculturalExtensionOfficer?.id) {
            updatedFarmer.agriculturalExtensionOfficer?.let { updatedAeo ->
                val newAeo = aeoRepository.findByIdOrNull(updatedAeo.id)
                    ?: throw IllegalArgumentException("AEO with ID ${updatedAeo.id} not found")
                existingFarmer.agriculturalExtensionOfficer = newAeo
            } ?: run {
                existingFarmer.agriculturalExtensionOfficer = null
            }
        }

        return farmerRepository.save(existingFarmer)
    }

    @Transactional
    fun deleteFarmer(id: UUID): Boolean {
        val farmer = farmerRepository.findByIdOrNull(id)
            ?: return false

        farmerRepository.delete(farmer)
        return true
    }

    @Transactional
    fun updateFarmerSupportStatus(id: UUID, needsSupport: Boolean, reason: String?, updatedBy: String): Farmer {
        val farmer = farmerRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Farmer with ID $id not found")

        farmer.needsSupport = needsSupport
        farmer.supportReason = reason
        farmer.lastUpdatedBy = updatedBy
        farmer.updatedAt = LocalDateTime.now()

        return farmerRepository.save(farmer)
    }

    fun assessFarmersForSupport() {
        // This would contain business logic to automatically assess farmers who might need support
        // based on various criteria like yield decline, repayment issues, etc.
        // This could be scheduled to run periodically
    }
}