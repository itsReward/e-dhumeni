package com.edhumeni.web.controller

import com.edhumeni.application.dto.response.AlertSummaryResponse
import com.edhumeni.application.dto.response.FarmerResponse
import com.edhumeni.application.mapper.FarmerMapper
import com.edhumeni.domain.service.AlertService
import com.edhumeni.domain.service.FarmerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Support Alerts", description = "Endpoints for managing farmer support alerts")
class AlertController(
    private val alertService: AlertService,
    private val farmerMapper: FarmerMapper,
    private val farmerService: FarmerService,
) {

    @GetMapping("/farmers")
    @Operation(
        summary = "Get all farmers needing support",
        description = "Retrieves all farmers who have been flagged as needing e-Dhumeni support"
    )
    fun getAllFarmersNeedingSupport(): ResponseEntity<List<FarmerResponse>> {
        val farmers = alertService.findAllFarmersNeedingSupport()
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @GetMapping("/farmers/region/{regionId}")
    @Operation(
        summary = "Get farmers needing support by region",
        description = "Retrieves farmers who need support in a specific region"
    )
    fun getFarmersNeedingSupportByRegion(@PathVariable regionId: UUID): ResponseEntity<List<FarmerResponse>> {
        val farmers = alertService.findFarmersNeedingSupportByRegion(regionId)
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @GetMapping("/farmers/repayment-issues")
    @Operation(
        summary = "Get farmers with repayment issues",
        description = "Retrieves farmers who have issues with contract repayments"
    )
    fun getFarmersWithRepaymentIssues(): ResponseEntity<List<FarmerResponse>> {
        val farmers = alertService.findFarmersWithRepaymentIssues()
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @GetMapping("/farmers/high-priority")
    @Operation(
        summary = "Get high priority farmers",
        description = "Retrieves farmers that need immediate attention based on multiple criteria"
    )
    fun getHighPriorityFarmers(): ResponseEntity<List<FarmerResponse>> {
        val farmers = alertService.getHighPriorityFarmers()
        return ResponseEntity.ok(farmers.map { farmerMapper.toResponse(it) })
    }

    @GetMapping("/contracts/at-risk")
    @Operation(
        summary = "Get at-risk contracts",
        description = "Retrieves contracts that are at risk of non-completion"
    )
    fun getAtRiskContracts(): ResponseEntity<List<Map<String, Any>>> {
        val contracts = alertService.findAtRiskContracts()

        // Map to a simple representation for the alert system
        val response = contracts.map { contract ->
            mapOf(
                "id" to contract.id,
                "contractNumber" to contract.contractNumber,
                "farmerName" to contract.farmer.name,
                "farmerId" to contract.farmer.id,
                "endDate" to contract.endDate,
                "expectedDeliveryKg" to contract.expectedDeliveryKg,
                "actualDeliveryKg" to contract.getTotalDeliveredKg(),
                "completionPercentage" to contract.getDeliveryCompletionPercentage(),
                "daysRemaining" to java.time.temporal.ChronoUnit.DAYS.between(
                    java.time.LocalDate.now(), contract.endDate
                )
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/summary")
    @Operation(
        summary = "Get alert summary statistics",
        description = "Retrieves summary statistics for all support alerts"
    )
    fun getAlertSummary(): ResponseEntity<Map<String, Any>> {
        val summary = alertService.getSupportAlertSummary()
        return ResponseEntity.ok(summary)
    }

    @PostMapping("/assess")
    @Operation(
        summary = "Run assessment for all farmers",
        description = "Runs the support needs assessment algorithm on all farmers"
    )
    fun runSupportAssessment(authentication: Authentication): ResponseEntity<Map<String, Any>> {
        alertService.assessAllFarmersForSupport(authentication.name)

        val summary = alertService.getSupportAlertSummary()
        return ResponseEntity.ok(mapOf(
            "status" to "completed",
            "message" to "Support assessment completed for all farmers",
            "summary" to summary
        ))
    }

    @PostMapping("/farmer/{id}/mark")
    @Operation(
        summary = "Mark farmer as needing support",
        description = "Manually marks a farmer as needing e-Dhumeni support"
    )
    fun markFarmerForSupport(
        @PathVariable id: UUID,
        @RequestParam reason: String,
        authentication: Authentication
    ): ResponseEntity<FarmerResponse> {
        val farmer = alertService.markFarmerNeedsSupport(id, reason, authentication.name)
        return ResponseEntity.ok(farmerMapper.toResponse(farmer))
    }

    @PostMapping("/farmer/{id}/resolve")
    @Operation(
        summary = "Resolve farmer support",
        description = "Marks a farmer's support issue as resolved"
    )
    fun resolveFarmerSupport(
        @PathVariable id: UUID,
        @RequestParam resolutionNotes: String,
        authentication: Authentication
    ): ResponseEntity<FarmerResponse> {
        val farmer = alertService.resolveSupport(id, resolutionNotes, authentication.name)
        return ResponseEntity.ok(farmerMapper.toResponse(farmer))
    }

    @PostMapping("/assess/{id}")
    @Operation(
        summary = "Assess individual farmer",
        description = "Runs the support needs assessment for a specific farmer"
    )
    fun assessIndividualFarmer(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<Map<String, Any>> {
        val farmer = farmerService.findById(id)
            ?: return ResponseEntity.notFound().build()

        val assessmentResult = alertService.assessIndividualFarmerForSupport(farmer)

        // Update the farmer's support status based on assessment
        if (assessmentResult.first) {
            alertService.markFarmerNeedsSupport(id, assessmentResult.second ?: "Unknown", authentication.name)
        } else if (farmer.needsSupport) {
            alertService.resolveSupport(id, "Automatically resolved through assessment", authentication.name)
        }

        return ResponseEntity.ok(mapOf(
            "farmerId" to farmer.id,
            "farmerName" to farmer.name,
            "needsSupport" to assessmentResult.first,
            "supportReason" to (assessmentResult.second ?: "N/A"),
            "assessedBy" to authentication.name,
            "assessedAt" to java.time.LocalDateTime.now()
        ))
    }

}
