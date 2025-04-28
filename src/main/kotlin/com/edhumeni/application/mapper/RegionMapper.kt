package com.edhumeni.application.mapper

import com.edhumeni.application.dto.request.RegionRequest
import com.edhumeni.application.dto.response.RegionResponse
import com.edhumeni.domain.model.Region
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.io.WKTReader
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RegionMapper {

    fun toEntity(request: RegionRequest): Region {
        val region = Region(
            name = request.name,
            province = request.province,
            district = request.district,
            naturalRegion = request.naturalRegion,
            averageAnnualRainfallMm = request.averageAnnualRainfallMm,
            predominantSoilType = request.predominantSoilType,
            centerLatitude = request.centerLatitude,
            centerLongitude = request.centerLongitude
        )

        // Parse WKT boundary if provided
        if (!request.wktBoundary.isNullOrBlank()) {
            try {
                val wktReader = WKTReader()
                val geometry = wktReader.read(request.wktBoundary)

                if (geometry is Polygon) {
                    region.boundary = geometry

                    // Update center coordinates if they weren't provided
                    if (region.centerLatitude == null || region.centerLongitude == null) {
                        val centroid = geometry.centroid
                        region.centerLatitude = centroid.y
                        region.centerLongitude = centroid.x
                    }
                }
            } catch (e: Exception) {
                // Log the error but continue - boundary will remain null
                println("Error parsing WKT: ${e.message}")
            }
        }

        return region
    }

    fun toResponse(entity: Region): RegionResponse {
        return RegionResponse(
            id = entity.id,
            name = entity.name,
            province = entity.province,
            district = entity.district,
            centerLatitude = entity.centerLatitude,
            centerLongitude = entity.centerLongitude,
            naturalRegion = entity.naturalRegion,
            averageAnnualRainfallMm = entity.averageAnnualRainfallMm,
            predominantSoilType = entity.predominantSoilType,
            farmerCount = entity.farmers.size,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}