package com.edhumeni.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "agricultural_extension_officers")
data class AgriculturalExtensionOfficer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(name = "contact_number", nullable = false)
    var contactNumber: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(name = "employee_id", nullable = false, unique = true)
    var employeeId: String,

    @ManyToMany
    @JoinTable(
        name = "aeo_assigned_regions",
        joinColumns = [JoinColumn(name = "aeo_id")],
        inverseJoinColumns = [JoinColumn(name = "region_id")]
    )
    var assignedRegions: MutableSet<Region> = mutableSetOf(),

    @Column(name = "qualification")
    var qualification: String? = null,

    @Column(name = "years_of_experience")
    var yearsOfExperience: Int = 0,

    @OneToMany(mappedBy = "agriculturalExtensionOfficer")
    var farmers: MutableList<Farmer> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // Method to get total number of farmers assigned to this AEO
    fun getTotalFarmers(): Int = farmers.size

    // Method to get farmers who need support
    fun getFarmersNeedingSupport(): List<Farmer> = farmers.filter { it.needsSupport }
}