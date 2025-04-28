package com.edhumeni.web.controller

import com.edhumeni.application.dto.request.RegionRequest
import com.edhumeni.application.dto.response.RegionResponse
import com.edhumeni.application.dto.response.RegionStatsResponse
import com.edhumeni.application.mapper.RegionMapper
import com.edhumeni.domain.service.RegionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/regions")
@Tag(name = "Region Management", description = "Endpoints for managing geographical regions")
class RegionController(
    private val regionService: RegionService,
    private val regionMapper: RegionMapper
) {

    @GetMapping
    @Operation(summary = "Get all regions", description = "Retrieves all geographical regions")
    fun getAllRegions(@RequestParam(required = false) search: String?): ResponseEntity<List<RegionResponse>> {
        val regions = if (search != null) {
            regionService.searchRegions(search)
        } else {
            regionService.findAll()
        }
        return ResponseEntity.ok(regions.map { regionMapper.toResponse(it) })
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get region by ID", description = "Retrieves a specific region by its ID")
    fun getRegionById(@PathVariable id: UUID): ResponseEntity<RegionResponse> {
        val region = regionService.findById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(regionMapper.toResponse(region))
    }

    @GetMapping("/province/{province}")
    @Operation(summary = "Get regions by province", description = "Retrieves all regions in a specific province")
    fun getRegionsByProvince(@PathVariable province: String): ResponseEntity<List<RegionResponse>> {
        val regions = regionService.findByProvince(province)
        return ResponseEntity.ok(regions.map { regionMapper.toResponse(it) })
    }

    @GetMapping("/district/{district}")
    @Operation(summary = "Get regions by district", description = "Retrieves all regions in a specific district")
    fun getRegionsByDistrict(@PathVariable district: String): ResponseEntity<List<RegionResponse>> {
        val regions = regionService.findByDistrict(district)
        return ResponseEntity.ok(regions.map { regionMapper.toResponse(it) })
    }

    @GetMapping("/coordinates")
    @Operation(summary = "Find region by coordinates", description = "Finds the region containing the specified coordinates")
    fun findRegionByCoordinates(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double
    ): ResponseEntity<RegionResponse> {
        val region = regionService.findRegionByCoordinates(latitude, longitude)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(regionMapper.toResponse(region))
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find regions near coordinates", description = "Finds regions within a certain distance of the coordinates")
    fun findRegionsNearCoordinates(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam(defaultValue = "10000") distanceInMeters: Double
    ): ResponseEntity<List<RegionResponse>> {
        val regions = regionService.findRegionsNearCoordinates(latitude, longitude, distanceInMeters)
        return ResponseEntity.ok(regions.map { regionMapper.toResponse(it) })
    }

    @GetMapping("/map")
    @Operation(summary = "Get regions for map", description = "Retrieves regions with farmer counts for map display")
    fun getRegionsForMap(): ResponseEntity<List<Map<String, Any>>> {
        val regionsData = regionService.getAllRegionsWithFarmerCount()

        // We need to convert the native query results to a usable format
        val response = regionsData.map { result ->
            // The result object needs to be cast to an appropriate type based on the native query
            // This is a simplified mapping as the actual structure would depend on the query result
            @Suppress("UNCHECKED_CAST")
            when (result) {
                is Array<*> -> {
                    mapOf(
                        "id" to (result[0] as UUID).toString(),
                        "name" to (result[1] as String),
                        "province" to (result[2] as String),
                        "district" to (result[3] as String),
                        "centerLatitude" to (result[4] as Double?),
                        "centerLongitude" to (result[5] as Double?),
                        "farmerCount" to (result[6] as Long)
                    )
                }
                is Map<*, *> -> result as Map<String, Any>
                else -> mapOf<String, Any>() // Fallback case
            }
        }

        return ResponseEntity.ok(response) as ResponseEntity<List<Map<String, Any>>>
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get region statistics", description = "Retrieves statistics for a specific region")
    fun getRegionStats(@PathVariable id: UUID): ResponseEntity<RegionStatsResponse> {
        val stats = regionService.getRegionStats(id)
        return ResponseEntity.ok(RegionStatsResponse(stats))
    }

    @PostMapping
    @Operation(summary = "Create a region", description = "Creates a new geographical region")
    fun createRegion(@Valid @RequestBody request: RegionRequest): ResponseEntity<RegionResponse> {
        val region = regionMapper.toEntity(request)
        val createdRegion = regionService.createRegion(region)

        return ResponseEntity.status(HttpStatus.CREATED).body(regionMapper.toResponse(createdRegion))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a region", description = "Updates an existing geographical region")
    fun updateRegion(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RegionRequest
    ): ResponseEntity<RegionResponse> {
        val region = regionMapper.toEntity(request)
        val updatedRegion = regionService.updateRegion(id, region)

        return ResponseEntity.ok(regionMapper.toResponse(updatedRegion))
    }

    @PatchMapping("/{id}/boundary")
    @Operation(summary = "Update region boundary", description = "Updates the geographical boundary of a region")
    fun updateRegionBoundary(
        @PathVariable id: UUID,
        @RequestParam wkt: String
    ): ResponseEntity<RegionResponse> {
        val updatedRegion = regionService.updateRegionBoundary(id, wkt)
        return ResponseEntity.ok(regionMapper.toResponse(updatedRegion))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a region", description = "Deletes a geographical region")
    fun deleteRegion(@PathVariable id: UUID): ResponseEntity<Void> {
        try {
            val deleted = regionService.deleteRegion(id)

            return if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalStateException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }
}