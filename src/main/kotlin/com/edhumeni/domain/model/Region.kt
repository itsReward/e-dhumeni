package com.edhumeni.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.locationtech.jts.geom.Polygon
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "regions")
data class Region(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var province: String,

    @Column(nullable = false)
    var district: String,

    // Spatial data type for geographical boundary
    @Column(name = "boundary", columnDefinition = "geometry(Polygon,4326)")
    var boundary: Polygon? = null,

    @Column(name = "center_latitude")
    var centerLatitude: Double? = null,

    @Column(name = "center_longitude")
    var centerLongitude: Double? = null,

    @Column(name = "natural_region")
    var naturalRegion: String? = null,

    @Column(name = "avg_annual_rainfall_mm")
    var averageAnnualRainfallMm: Double? = null,

    @Column(name = "predominant_soil_type")
    var predominantSoilType: String? = null,

    @OneToMany(mappedBy = "region", cascade = [CascadeType.ALL], orphanRemoval = true)
    var farmers: MutableList<Farmer> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // Function to get farmer count for map display
    fun getFarmerCount(): Int = farmers.size
}