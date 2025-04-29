package com.edhumeni.web.controller

import com.edhumeni.domain.service.ReportingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reporting", description = "Endpoints for generating reports")
class ReportController(
    private val reportingService: ReportingService
) {

    @GetMapping("/farmer/{farmerId}")
    @Operation(
        summary = "Generate farmer performance report",
        description = "Generates a performance report for a specific farmer"
    )
    fun generateFarmerReport(@PathVariable farmerId: UUID): ResponseEntity<Map<String, Any>> {
        val report = reportingService.generateFarmerPerformanceReport(farmerId)
        return ResponseEntity.ok(report)
    }

    @GetMapping("/region/{regionId}")
    @Operation(
        summary = "Generate region performance report",
        description = "Generates a performance report for a specific region"
    )
    fun generateRegionReport(@PathVariable regionId: UUID): ResponseEntity<Map<String, Any>> {
        val report = reportingService.generateRegionPerformanceReport(regionId)
        return ResponseEntity.ok(report)
    }

    @GetMapping("/system")
    @Operation(
        summary = "Generate system performance report",
        description = "Generates an overall system performance report"
    )
    fun generateSystemReport(): ResponseEntity<Map<String, Any>> {
        val report = reportingService.generateSystemPerformanceReport()
        return ResponseEntity.ok(report)
    }
}