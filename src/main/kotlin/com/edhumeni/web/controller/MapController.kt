package com.edhumeni.web.controller

import com.edhumeni.application.dto.response.FarmerResponse
import com.edhumeni.application.dto.response.RegionMapResponse
import com.edhumeni.application.mapper.FarmerMapper
import com.edhumeni.domain.service.FarmerService
import com.edhumeni.domain.service.RegionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/map")
@Tag(name = "Map Integration", description = "Endpoints for spatial map integration")
class MapController(
    private val regionService: RegionService,
    private val farmerService: FarmerService,
    private val farmerMapper: FarmerMapper
) {

    @GetMapping("/regions")
    @Operation(
        summary = "Get all regions for map display",
        description = "Retrieves all regions with farmer counts for the interactive map"
    )
    fun getAllRegionsForMap(): ResponseEntity<List<RegionMapResponse>> {
        val regions = regionService.findAll()

        val response = regions.map { region ->
            val farmerCount = farmerService.countFarmersByRegion(region.id)
            val supportCount = farmerService.findFarmersNeedingSupportByRegion(region.id).size

            RegionMapResponse(
                id = region.id,
                name = region.name,
                province = region.province,
                district = region.district,
                centerLatitude = region.centerLatitude,
                centerLongitude = region.centerLongitude,
                farmerCount = farmerCount,
                farmersNeedingSupportCount = supportCount
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/regions/{regionId}/farmers")
    @Operation(
        summary = "Get farmers in a region",
        description = "Retrieves all farmers in a specific region for map display"
    )
    fun getFarmersInRegion(@PathVariable regionId: UUID): ResponseEntity<List<FarmerResponse>> {
        val farmers = farmerService.findByRegionId(regionId)
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @GetMapping("/coordinates")
    @Operation(
        summary = "Find region by coordinates",
        description = "Finds the region containing the specified coordinates"
    )
    fun findRegionByCoordinates(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double
    ): ResponseEntity<RegionMapResponse> {
        val region = regionService.findRegionByCoordinates(latitude, longitude)
            ?: return ResponseEntity.notFound().build()

        val farmerCount = farmerService.countFarmersByRegion(region.id)
        val supportCount = farmerService.findFarmersNeedingSupportByRegion(region.id).size

        val response = RegionMapResponse(
            id = region.id,
            name = region.name,
            province = region.province,
            district = region.district,
            centerLatitude = region.centerLatitude,
            centerLongitude = region.centerLongitude,
            farmerCount = farmerCount,
            farmersNeedingSupportCount = supportCount
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/regions/{regionId}/support-alert")
    @Operation(
        summary = "Get farmers needing support in a region",
        description = "Retrieves farmers who need support in a specific region for map alerts"
    )
    fun getFarmersNeedingSupportInRegion(@PathVariable regionId: UUID): ResponseEntity<List<FarmerResponse>> {
        val farmers = farmerService.findFarmersNeedingSupportByRegion(regionId)
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @GetMapping("/support-alerts")
    @Operation(
        summary = "Get all regions with support alerts",
        description = "Retrieves all regions that have farmers needing support for map alert display"
    )
    fun getRegionsWithSupportAlerts(): ResponseEntity<List<RegionMapResponse>> {
        val regions = regionService.findAll()

        val response = regions
            .map { region ->
                val supportCount = farmerService.findFarmersNeedingSupportByRegion(region.id).size
                val farmerCount = farmerService.countFarmersByRegion(region.id)

                RegionMapResponse(
                    id = region.id,
                    name = region.name,
                    province = region.province,
                    district = region.district,
                    centerLatitude = region.centerLatitude,
                    centerLongitude = region.centerLongitude,
                    farmerCount = farmerCount,
                    farmersNeedingSupportCount = supportCount
                )
            }
            .filter { it.farmersNeedingSupportCount > 0 }

        return ResponseEntity.ok(response)
    }
}