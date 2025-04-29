package com.edhumeni.web.controller

import com.edhumeni.application.dto.request.AeoRequest
import com.edhumeni.application.dto.response.AeoResponse
import com.edhumeni.application.mapper.AeoMapper
import com.edhumeni.domain.service.AeoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/aeos")
@Tag(name = "Agricultural Extension Officer Management", description = "Endpoints for managing AEOs")
class AeoController(
    private val aeoService: AeoService,
    private val aeoMapper: AeoMapper
) {

    @GetMapping
    @Operation(summary = "Get all AEOs", description = "Retrieves all agricultural extension officers")
    fun getAllAeos(@RequestParam(required = false) search: String?): ResponseEntity<List<AeoResponse>> {
        val aeos = if (search != null) {
            aeoService.searchAeos(search)
        } else {
            aeoService.findAll()
        }
        return ResponseEntity.ok(aeos.map { aeoMapper.toResponse(it) })
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get AEO by ID", description = "Retrieves a specific AEO by their ID")
    fun getAeoById(@PathVariable id: UUID): ResponseEntity<AeoResponse> {
        val aeo = aeoService.findById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(aeoMapper.toResponse(aeo))
    }

    @GetMapping("/region/{regionId}")
    @Operation(summary = "Get AEOs by region", description = "Retrieves all AEOs assigned to a specific region")
    fun getAeosByRegion(@PathVariable regionId: UUID): ResponseEntity<List<AeoResponse>> {
        val aeos = aeoService.findByRegionId(regionId)
        return ResponseEntity.ok(aeos.map { aeoMapper.toResponse(it) })
    }

    @GetMapping("/support-needed")
    @Operation(summary = "Get AEOs with farmers needing support", description = "Retrieves all AEOs who have farmers needing support")
    fun getAeosWithFarmersNeedingSupport(): ResponseEntity<List<AeoResponse>> {
        val aeos = aeoService.findWithFarmersNeedingSupport()
        return ResponseEntity.ok(aeos.map { aeoMapper.toResponse(it) })
    }

    @PostMapping
    @Operation(summary = "Create an AEO", description = "Creates a new agricultural extension officer")
    fun createAeo(@Valid @RequestBody request: AeoRequest): ResponseEntity<AeoResponse> {
        val aeo = aeoMapper.toEntity(request)
        val createdAeo = aeoService.createAeo(aeo)

        return ResponseEntity.status(HttpStatus.CREATED).body(aeoMapper.toResponse(createdAeo))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an AEO", description = "Updates an existing agricultural extension officer")
    fun updateAeo(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AeoRequest
    ): ResponseEntity<AeoResponse> {
        val aeo = aeoMapper.toEntity(request)
        val updatedAeo = aeoService.updateAeo(id, aeo)

        return ResponseEntity.ok(aeoMapper.toResponse(updatedAeo))
    }

    @PostMapping("/{id}/regions/{regionId}")
    @Operation(summary = "Assign region to AEO", description = "Assigns a region to an agricultural extension officer")
    fun assignRegion(
        @PathVariable id: UUID,
        @PathVariable regionId: UUID
    ): ResponseEntity<AeoResponse> {
        val updatedAeo = aeoService.assignRegion(id, regionId)
        return ResponseEntity.ok(aeoMapper.toResponse(updatedAeo))
    }

    @DeleteMapping("/{id}/regions/{regionId}")
    @Operation(summary = "Unassign region from AEO", description = "Removes a region assignment from an agricultural extension officer")
    fun unassignRegion(
        @PathVariable id: UUID,
        @PathVariable regionId: UUID
    ): ResponseEntity<AeoResponse> {
        val updatedAeo = aeoService.unassignRegion(id, regionId)
        return ResponseEntity.ok(aeoMapper.toResponse(updatedAeo))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an AEO", description = "Deletes an agricultural extension officer")
    fun deleteAeo(@PathVariable id: UUID): ResponseEntity<Void> {
        try {
            val deleted = aeoService.deleteAeo(id)

            return if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalStateException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get AEO statistics", description = "Retrieves statistics for a specific AEO")
    fun getAeoStats(@PathVariable id: UUID): ResponseEntity<Map<String, Any>> {
        val stats = aeoService.getAeoStats(id)
        return ResponseEntity.ok(stats)
    }
}