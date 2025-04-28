package com.edhumeni.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "farmers")
data class Farmer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var age: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var gender: Gender,

    @Column(name = "contact_number")
    var contactNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    var region: Region,

    @Column(nullable = false)
    var province: String,

    @Column(nullable = false)
    var ward: String,

    @Column(name = "natural_region")
    var naturalRegion: String,

    @Column(name = "soil_type")
    var soilType: String,

    @Column(name = "uses_fertilizer")
    var usesFertilizer: Boolean = false,

    @Column(name = "fertilizer_type")
    var fertilizerType: String? = null,

    @Column(name = "manure_availability")
    var manureAvailability: Boolean = false,

    @Column(name = "uses_ploughing")
    var usesPloughing: Boolean = false,

    @Column(name = "uses_pfumvudza")
    var usesPfumvudza: Boolean = false,

    @Column(name = "access_to_credit")
    var accessToCredit: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "land_ownership_type")
    var landOwnershipType: LandOwnershipType,

    @Column(name = "keeps_farm_records")
    var keepsFarmRecords: Boolean = false,

    @Column(name = "farm_size_hectares")
    var farmSizeHectares: Double,

    @Column(name = "previous_planted_crop")
    var previousPlantedCrop: String? = null,

    @Column(name = "previous_season_yield_kg")
    var previousSeasonYieldKg: Double? = null,

    @Column(name = "average_yield_per_season_kg")
    var averageYieldPerSeasonKg: Double? = null,

    @ElementCollection
    @CollectionTable(
        name = "farmer_farming_practices",
        joinColumns = [JoinColumn(name = "farmer_id")]
    )
    @Column(name = "farming_practice")
    var farmingPractices: Set<String> = emptySet(),

    @ElementCollection
    @CollectionTable(
        name = "farmer_conservation_practices",
        joinColumns = [JoinColumn(name = "farmer_id")]
    )
    @Column(name = "conservation_practice")
    var conservationPractices: Set<String> = emptySet(),

    @Column(name = "compliance_level")
    @Enumerated(EnumType.STRING)
    var complianceLevel: ComplianceLevel = ComplianceLevel.MEDIUM,

    @ElementCollection
    @CollectionTable(
        name = "farmer_agronomic_practices",
        joinColumns = [JoinColumn(name = "farmer_id")]
    )
    @Column(name = "agronomic_practice")
    var agronomicPractices: Set<String> = emptySet(),

    @Enumerated(EnumType.STRING)
    @Column(name = "land_preparation_type")
    var landPreparationType: LandPreparationType? = null,

    @Column(name = "soil_testing_done")
    var soilTestingDone: Boolean = false,

    @Column(name = "planting_date")
    var plantingDate: LocalDateTime? = null,

    @Column(name = "observed_off_types")
    var observedOffTypes: Boolean = false,

    @Column(name = "herbicides_used")
    var herbicidesUsed: String? = null,

    @ElementCollection
    @CollectionTable(
        name = "farmer_problematic_pests",
        joinColumns = [JoinColumn(name = "farmer_id")]
    )
    @Column(name = "pest_name")
    var problematicPests: List<String> = emptyList(),

    @Column(name = "aeo_visit_frequency")
    @Enumerated(EnumType.STRING)
    var aeoVisitFrequency: VisitFrequency = VisitFrequency.MONTHLY,

    @ElementCollection
    @CollectionTable(
        name = "farmer_challenges",
        joinColumns = [JoinColumn(name = "farmer_id")]
    )
    @Column(name = "challenge")
    var challenges: List<String> = emptyList(),

    @Column(name = "has_crop_insurance")
    var hasCropInsurance: Boolean = false,

    @Column(name = "receives_govt_subsidies")
    var receivesGovtSubsidies: Boolean = false,

    @Column(name = "uses_agroforestry")
    var usesAgroforestry: Boolean = false,

    @Column(name = "input_cost_per_season")
    var inputCostPerSeason: Double? = null,

    @Column(name = "main_source_of_inputs")
    var mainSourceOfInputs: String? = null,

    @Column(name = "social_vulnerability")
    @Enumerated(EnumType.STRING)
    var socialVulnerability: VulnerabilityLevel = VulnerabilityLevel.LOW,

    @Column(name = "education_level")
    @Enumerated(EnumType.STRING)
    var educationLevel: EducationLevel = EducationLevel.PRIMARY,

    @Column(name = "household_size")
    var householdSize: Int = 0,

    @Column(name = "num_dependents")
    var numberOfDependents: Int = 0,

    @Column(name = "marital_status")
    @Enumerated(EnumType.STRING)
    var maritalStatus: MaritalStatus = MaritalStatus.SINGLE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aeo_id")
    var agriculturalExtensionOfficer: AgriculturalExtensionOfficer? = null,

    @Column(name = "needs_support", nullable = false)
    var needsSupport: Boolean = false,

    @Column(name = "support_reason")
    var supportReason: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_updated_by")
    var lastUpdatedBy: String? = null
)

enum class Gender {
    MALE, FEMALE, OTHER
}

enum class LandOwnershipType {
    OWNED, LEASED, COMMUNAL, RESETTLEMENT, OTHER
}

enum class ComplianceLevel {
    LOW, MEDIUM, HIGH
}

enum class LandPreparationType {
    MANUAL, MECHANIZED, CONSERVATION_TILLAGE, ZERO_TILLAGE, OTHER
}

enum class VisitFrequency {
    WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY, NEVER
}

enum class VulnerabilityLevel {
    LOW, MEDIUM, HIGH
}

enum class EducationLevel {
    NONE, PRIMARY, SECONDARY, TERTIARY
}

enum class MaritalStatus {
    SINGLE, MARRIED, DIVORCED, WIDOWED
}