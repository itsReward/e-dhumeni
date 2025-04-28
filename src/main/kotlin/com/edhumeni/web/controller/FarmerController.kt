package com.edhumeni.web.controller

import com.edhumeni.application.dto.request.FarmerRequest
import com.edhumeni.application.dto.response.FarmerResponse
import com.edhumeni.application.mapper.FarmerMapper
import com.edhumeni.domain.service.FarmerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/farmers")
@Tag(name = "Farmer Management", description = "Endpoints for managing farmers")
class FarmerController(
    private val farmerService: FarmerService,
    private val farmerMapper: FarmerMapper
) {

    @GetMapping
    @Operation(summary = "Get all farmers", description = "Retrieves all farmers with optional filtering")
    fun getAllFarmers(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) province: String?,
        @RequestParam(required = false) ward: String?
    ): ResponseEntity<List<FarmerResponse>> {
        val farmers = if (name != null || region != null || province != null || ward != null) {
            farmerService.searchFarmers(name, region, province, ward)
        } else {
            farmerService.findAll()
        }
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get farmer by ID", description = "Retrieves a specific farmer by their ID")
    fun getFarmerById(@PathVariable id: UUID): ResponseEntity<FarmerResponse> {
        val farmer = farmerService.findById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(farmerMapper.toResponse(farmer))
    }

    @GetMapping("/region/{regionId}")
    @Operation(summary = "Get farmers by region", description = "Retrieves all farmers in a specific region")
    fun getFarmersByRegion(@PathVariable regionId: UUID): ResponseEntity<List<FarmerResponse>> {
        val farmers = farmerService.findByRegionId(regionId)
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @GetMapping("/aeo/{aeoId}")
    @Operation(summary = "Get farmers by AEO", description = "Retrieves all farmers assigned to a specific AEO")
    fun getFarmersByAeo(@PathVariable aeoId: UUID): ResponseEntity<List<FarmerResponse>> {
        val farmers = farmerService.findByAeoId(aeoId)
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @GetMapping("/support")
    @Operation(summary = "Get farmers needing support", description = "Retrieves all farmers who need e-Dhumeni support")
    fun getFarmersNeedingSupport(): ResponseEntity<List<FarmerResponse>> {
        val farmers = farmerService.findFarmersNeedingSupport()
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @GetMapping("/region/{regionId}/support")
    @Operation(summary = "Get farmers needing support by region", description = "Retrieves farmers who need support in a specific region")
    fun getFarmersNeedingSupportByRegion(@PathVariable regionId: UUID): ResponseEntity<List<FarmerResponse>> {
        val farmers = farmerService.findFarmersNeedingSupportByRegion(regionId)
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @GetMapping("/repayment-issues")
    @Operation(summary = "Get farmers with repayment issues", description = "Retrieves farmers who have issues with contract repayments")
    fun getFarmersWithRepaymentIssues(): ResponseEntity<List<FarmerResponse>> {
        val farmers = farmerService.findFarmersWithRepaymentIssues()
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @PostMapping
    @Operation(summary = "Create a new farmer", description = "Creates a new farmer record")
    fun createFarmer(
        @Valid @RequestBody request: FarmerRequest,
        authentication: Authentication
    ): ResponseEntity<FarmerResponse> {
        val farmer = farmerMapper.toEntity(request)
        val createdFarmer = farmerService.createFarmer(
            farmer = farmer,
            regionId = request.regionId,
            aeoId = request.aeoId
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(farmerMapper.toResponse(createdFarmer))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a farmer", description = "Updates an existing farmer record")
    fun updateFarmer(
        @PathVariable id: UUID,
        @Valid @RequestBody request: FarmerRequest,
        authentication: Authentication
    ): ResponseEntity<FarmerResponse> {
        val updatedFarmer = farmerMapper.toEntity(request)
        val savedFarmer = farmerService.updateFarmer(
            id = id,
            updatedFarmer = updatedFarmer,
            updatedBy = authentication.name
        )

        return ResponseEntity.ok(farmerMapper.toResponse(savedFarmer))
    }

    @PatchMapping("/{id}/support-status")
    @Operation(summary = "Update farmer support status", description = "Updates whether a farmer needs e-Dhumeni support")
    fun updateFarmerSupportStatus(
        @PathVariable id: UUID,
        @RequestParam(name = "needs_support") needsSupport: Boolean,
        @RequestParam(required = false) reason: String?,
        authentication: Authentication
    ): ResponseEntity<FarmerResponse> {
        val updatedFarmer = farmerService.updateFarmerSupportStatus(
            id = id,
            needsSupport = needsSupport,
            reason = reason,
            updatedBy = authentication.name
        )

        return ResponseEntity.ok(farmerMapper.toResponse(updatedFarmer))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a farmer", description = "Deletes a farmer record")
    fun deleteFarmer(@PathVariable id: UUID): ResponseEntity<Void> {
        val deleted = farmerService.deleteFarmer(id)

        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}