package com.edhumeni.application.dto.response

import com.edhumeni.domain.model.*
import java.time.LocalDateTime
import java.util.UUID

data class FarmerResponse(
    val id: UUID,
    val name: String,
    val age: Int,
    val gender: Gender,
    val contactNumber: String?,
    val region: RegionSummaryResponse,
    val province: String,
    val ward: String,
    val naturalRegion: String?,
    val soilType: String?,
    val usesFertilizer: Boolean,
    val fertilizerType: String?,
    val manureAvailability: Boolean,
    val usesPloughing: Boolean,
    val usesPfumvudza: Boolean,
    val accessToCredit: Boolean,
    val landOwnershipType: LandOwnershipType,
    val keepsFarmRecords: Boolean,
    val farmSizeHectares: Double,
    val previousPlantedCrop: String?,
    val previousSeasonYieldKg: Double?,
    val averageYieldPerSeasonKg: Double?,
    val farmingPractices: Set<String>,
    val conservationPractices: Set<String>,
    val complianceLevel: ComplianceLevel,
    val agronomicPractices: Set<String>,
    val landPreparationType: LandPreparationType?,
    val soilTestingDone: Boolean,
    val plantingDate: LocalDateTime?,
    val observedOffTypes: Boolean,
    val herbicidesUsed: String?,
    val problematicPests: List<String>,
    val aeoVisitFrequency: VisitFrequency,
    val challenges: List<String>,
    val hasCropInsurance: Boolean,
    val receivesGovtSubsidies: Boolean,
    val usesAgroforestry: Boolean,
    val inputCostPerSeason: Double?,
    val mainSourceOfInputs: String?,
    val socialVulnerability: VulnerabilityLevel,
    val educationLevel: EducationLevel,
    val householdSize: Int,
    val numberOfDependents: Int,
    val maritalStatus: MaritalStatus,
    val agriculturalExtensionOfficer: AeoSummaryResponse?,
    val needsSupport: Boolean,
    val supportReason: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val lastUpdatedBy: String?
)

data class FarmerSummaryResponse(
    val id: UUID,
    val name: String,
    val age: Int,
    val gender: Gender,
    val contactNumber: String?,
    val region: String,
    val province: String,
    val ward: String,
    val farmSizeHectares: Double,
    val landOwnershipType: LandOwnershipType,
    val needsSupport: Boolean,
    val aeoName: String?
)

data class RegionSummaryResponse(
    val id: UUID,
    val name: String,
    val province: String,
    val district: String
)

data class AeoSummaryResponse(
    val id: UUID,
    val name: String,
    val contactNumber: String,
    val email: String
)