package com.edhumeni.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.locationtech.jts.geom.Polygon

data class RegionRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Province is required")
    val province: String,

    @field:NotBlank(message = "District is required")
    val district: String,

    // Optional WKT (Well-Known Text) representation of the polygon boundary
    val wktBoundary: String?,

    val centerLatitude: Double?,

    val centerLongitude: Double?,

    val naturalRegion: String?,

    val averageAnnualRainfallMm: Double?,

    val predominantSoilType: String?
)