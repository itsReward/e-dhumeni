package com.edhumeni.application.dto.request

import com.edhumeni.domain.model.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime
import java.util.UUID

data class FarmerRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:Min(value = 15, message = "Age must be at least 15")
    @field:Max(value = 120, message = "Age must be less than 120")
    val age: Int,

    @field:NotNull(message = "Gender is required")
    val gender: Gender,

    @field:Pattern(regexp = "^\\+?[0-9\\s\\-()]{8,20}$", message = "Invalid contact number format")
    val contactNumber: String?,

    @field:NotNull(message = "Region ID is required")
    val regionId: UUID,

    @field:NotBlank(message = "Province is required")
    val province: String,

    @field:NotBlank(message = "Ward is required")
    val ward: String,

    val naturalRegion: String?,

    val soilType: String?,

    val usesFertilizer: Boolean = false,

    val fertilizerType: String?,

    val manureAvailability: Boolean = false,

    val usesPloughing: Boolean = false,

    val usesPfumvudza: Boolean = false,

    val accessToCredit: Boolean = false,

    @field:NotNull(message = "Land ownership type is required")
    val landOwnershipType: LandOwnershipType,

    val keepsFarmRecords: Boolean = false,

    @field:DecimalMin(value = "0.01", message = "Farm size must be greater than 0")
    val farmSizeHectares: Double,

    val previousPlantedCrop: String?,

    val previousSeasonYieldKg: Double?,

    val averageYieldPerSeasonKg: Double?,

    val farmingPractices: Set<String> = emptySet(),

    val conservationPractices: Set<String> = emptySet(),

    val complianceLevel: ComplianceLevel = ComplianceLevel.MEDIUM,

    val agronomicPractices: Set<String> = emptySet(),

    val landPreparationType: LandPreparationType?,

    val soilTestingDone: Boolean = false,

    val plantingDate: LocalDateTime?,

    val observedOffTypes: Boolean = false,

    val herbicidesUsed: String?,

    val problematicPests: List<String> = emptyList(),

    val aeoVisitFrequency: VisitFrequency = VisitFrequency.MONTHLY,

    val challenges: List<String> = emptyList(),

    val hasCropInsurance: Boolean = false,

    val receivesGovtSubsidies: Boolean = false,

    val usesAgroforestry: Boolean = false,

    val inputCostPerSeason: Double?,

    val mainSourceOfInputs: String?,

    val socialVulnerability: VulnerabilityLevel = VulnerabilityLevel.LOW,

    val educationLevel: EducationLevel = EducationLevel.PRIMARY,

    @field:Min(value = 1, message = "Household size must be at least 1")
    val householdSize: Int = 1,

    @field:Min(value = 0, message = "Number of dependents cannot be negative")
    val numberOfDependents: Int = 0,

    val maritalStatus: MaritalStatus = MaritalStatus.SINGLE,

    val aeoId: UUID?,

    val needsSupport: Boolean = false,

    val supportReason: String?
)

