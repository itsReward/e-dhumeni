package com.edhumeni.web.controller

import com.edhumeni.domain.service.FarmerAssessmentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/assessments")
@Tag(name = "Farmer Assessments", description = "Endpoints for advanced farmer assessments")
class FarmerAssessmentController(
    private val farmerAssessmentService: FarmerAssessmentService
) {

    @GetMapping("/farmer/{farmerId}/risk-score")
    @Operation(
        summary = "Calculate farmer risk score",
        description = "Calculates a comprehensive risk score for a specific farmer"
    )
    fun calculateFarmerRiskScore(@PathVariable farmerId: UUID): ResponseEntity<Map<String, Any>> {
        val riskScore = farmerAssessmentService.calculateFarmerRiskScore(farmerId)
        return ResponseEntity.ok(riskScore)
    }

    @GetMapping("/region/{regionId}/analysis")
    @Operation(
        summary = "Analyze region farmers",
        description = "Analyzes farmers in a region to identify patterns and intervention needs"
    )
    fun analyzeRegionFarmers(@PathVariable regionId: UUID): ResponseEntity<Map<String, Any>> {
        val analysis = farmerAssessmentService.analyzeRegionFarmers(regionId)
        return ResponseEntity.ok(analysis)
    }
}