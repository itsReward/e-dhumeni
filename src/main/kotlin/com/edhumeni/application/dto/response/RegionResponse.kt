package com.edhumeni.application.dto.response

import java.time.LocalDateTime
import java.util.UUID

data class RegionResponse(
    val id: UUID,
    val name: String,
    val province: String,
    val district: String,
    val centerLatitude: Double?,
    val centerLongitude: Double?,
    val naturalRegion: String?,
    val averageAnnualRainfallMm: Double?,
    val predominantSoilType: String?,
    val farmerCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class RegionMapResponse(
    val id: UUID,
    val name: String,
    val province: String,
    val district: String,
    val centerLatitude: Double?,
    val centerLongitude: Double?,
    val farmerCount: Long,
    val farmersNeedingSupportCount: Int
)

data class RegionStatsResponse(
    val id: UUID,
    val name: String,
    val province: String,
    val district: String,
    val farmerCount: Long,
    val farmersNeedingSupportCount: Int,
    val supportPercentage: Double
) {
    constructor(statsMap: Map<String, Any>) : this(
        id = statsMap["id"] as UUID,
        name = statsMap["name"] as String,
        province = statsMap["province"] as String,
        district = statsMap["district"] as String,
        farmerCount = statsMap["farmerCount"] as Long,
        farmersNeedingSupportCount = statsMap["farmersNeedingSupportCount"] as Int,
        supportPercentage = statsMap["supportPercentage"] as Double
    )
}