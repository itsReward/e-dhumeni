package com.edhumeni.application.mapper

import com.edhumeni.application.dto.request.FarmerRequest
import com.edhumeni.application.dto.response.AeoSummaryResponse
import com.edhumeni.application.dto.response.FarmerResponse
import com.edhumeni.application.dto.response.FarmerSummaryResponse
import com.edhumeni.application.dto.response.RegionSummaryResponse
import com.edhumeni.domain.model.Farmer
import com.edhumeni.domain.model.Region
import org.springframework.stereotype.Component

@Component
class FarmerMapper {

    fun toEntity(request: FarmerRequest): Farmer {
        // Create a placeholder Region that will be replaced in the service
        val placeholderRegion = Region(id = request.regionId, name = "", province = "", district = "")

        return Farmer(
            name = request.name,
            age = request.age,
            gender = request.gender,
            contactNumber = request.contactNumber ?: "",
            region = placeholderRegion,
            province = request.province,
            ward = request.ward,
            naturalRegion = request.naturalRegion ?: "",
            soilType = request.soilType ?: "",
            usesFertilizer = request.usesFertilizer,
            fertilizerType = request.fertilizerType,
            manureAvailability = request.manureAvailability,
            usesPloughing = request.usesPloughing,
            usesPfumvudza = request.usesPfumvudza,
            accessToCredit = request.accessToCredit,
            landOwnershipType = request.landOwnershipType,
            keepsFarmRecords = request.keepsFarmRecords,
            farmSizeHectares = request.farmSizeHectares,
            previousPlantedCrop = request.previousPlantedCrop,
            previousSeasonYieldKg = request.previousSeasonYieldKg,
            averageYieldPerSeasonKg = request.averageYieldPerSeasonKg,
            farmingPractices = request.farmingPractices,
            conservationPractices = request.conservationPractices,
            complianceLevel = request.complianceLevel,
            agronomicPractices = request.agronomicPractices,
            landPreparationType = request.landPreparationType,
            soilTestingDone = request.soilTestingDone,
            plantingDate = request.plantingDate,
            observedOffTypes = request.observedOffTypes,
            herbicidesUsed = request.herbicidesUsed,
            problematicPests = request.problematicPests,
            aeoVisitFrequency = request.aeoVisitFrequency,
            challenges = request.challenges,
            hasCropInsurance = request.hasCropInsurance,
            receivesGovtSubsidies = request.receivesGovtSubsidies,
            usesAgroforestry = request.usesAgroforestry,
            inputCostPerSeason = request.inputCostPerSeason,
            mainSourceOfInputs = request.mainSourceOfInputs,
            socialVulnerability = request.socialVulnerability,
            educationLevel = request.educationLevel,
            householdSize = request.householdSize,
            numberOfDependents = request.numberOfDependents,
            maritalStatus = request.maritalStatus,
            needsSupport = request.needsSupport,
            supportReason = request.supportReason
            // AEO will be set in the service
        )
    }

    fun toResponse(entity: Farmer): FarmerResponse {
        return FarmerResponse(
            id = entity.id,
            name = entity.name,
            age = entity.age,
            gender = entity.gender,
            contactNumber = entity.contactNumber,
            region = RegionSummaryResponse(
                id = entity.region.id,
                name = entity.region.name,
                province = entity.region.province,
                district = entity.region.district
            ),
            province = entity.province,
            ward = entity.ward,
            naturalRegion = entity.naturalRegion,
            soilType = entity.soilType,
            usesFertilizer = entity.usesFertilizer,
            fertilizerType = entity.fertilizerType,
            manureAvailability = entity.manureAvailability,
            usesPloughing = entity.usesPloughing,
            usesPfumvudza = entity.usesPfumvudza,
            accessToCredit = entity.accessToCredit,
            landOwnershipType = entity.landOwnershipType,
            keepsFarmRecords = entity.keepsFarmRecords,
            farmSizeHectares = entity.farmSizeHectares,
            previousPlantedCrop = entity.previousPlantedCrop,
            previousSeasonYieldKg = entity.previousSeasonYieldKg,
            averageYieldPerSeasonKg = entity.averageYieldPerSeasonKg,
            farmingPractices = entity.farmingPractices,
            conservationPractices = entity.conservationPractices,
            complianceLevel = entity.complianceLevel,
            agronomicPractices = entity.agronomicPractices,
            landPreparationType = entity.landPreparationType,
            soilTestingDone = entity.soilTestingDone,
            plantingDate = entity.plantingDate,
            observedOffTypes = entity.observedOffTypes,
            herbicidesUsed = entity.herbicidesUsed,
            problematicPests = entity.problematicPests,
            aeoVisitFrequency = entity.aeoVisitFrequency,
            challenges = entity.challenges,
            hasCropInsurance = entity.hasCropInsurance,
            receivesGovtSubsidies = entity.receivesGovtSubsidies,
            usesAgroforestry = entity.usesAgroforestry,
            inputCostPerSeason = entity.inputCostPerSeason,
            mainSourceOfInputs = entity.mainSourceOfInputs,
            socialVulnerability = entity.socialVulnerability,
            educationLevel = entity.educationLevel,
            householdSize = entity.householdSize,
            numberOfDependents = entity.numberOfDependents,
            maritalStatus = entity.maritalStatus,
            agriculturalExtensionOfficer = entity.agriculturalExtensionOfficer?.let {
                AeoSummaryResponse(
                    id = it.id,
                    name = it.name,
                    contactNumber = it.contactNumber,
                    email = it.email
                )
            },
            needsSupport = entity.needsSupport,
            supportReason = entity.supportReason,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            lastUpdatedBy = entity.lastUpdatedBy
        )
    }

    fun toSummaryResponse(entity: Farmer): FarmerSummaryResponse {
        return FarmerSummaryResponse(
            id = entity.id,
            name = entity.name,
            age = entity.age,
            gender = entity.gender,
            contactNumber = entity.contactNumber,
            region = entity.region.name,
            province = entity.province,
            ward = entity.ward,
            farmSizeHectares = entity.farmSizeHectares,
            landOwnershipType = entity.landOwnershipType,
            needsSupport = entity.needsSupport,
            aeoName = entity.agriculturalExtensionOfficer?.name
        )
    }
}