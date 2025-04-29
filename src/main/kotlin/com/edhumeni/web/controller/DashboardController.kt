package com.edhumeni.web.controller

import com.edhumeni.domain.service.DashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for dashboard analytics")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/summary")
    @Operation(
        summary = "Get dashboard summary",
        description = "Retrieves summary statistics for the dashboard"
    )
    fun getDashboardSummary(): ResponseEntity<Map<String, Any>> {
        val summary = dashboardService.getDashboardSummary()
        return ResponseEntity.ok(summary)
    }

    @GetMapping("/regions/stats")
    @Operation(
        summary = "Get region statistics",
        description = "Retrieves farmer statistics by region"
    )
    fun getRegionStats(): ResponseEntity<List<Map<String, Any>>> {
        val stats = dashboardService.getFarmerStatsByRegion()
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/contracts/completion")
    @Operation(
        summary = "Get contract completion statistics",
        description = "Retrieves statistics about contract completion rates"
    )
    fun getContractCompletionStats(): ResponseEntity<Map<String, Any>> {
        val stats = dashboardService.getContractCompletionStats()
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/deliveries/quality")
    @Operation(
        summary = "Get delivery quality statistics",
        description = "Retrieves statistics about delivery quality grades"
    )
    fun getDeliveryQualityStats(): ResponseEntity<Map<String, Any>> {
        val stats = dashboardService.getDeliveryQualityStats()
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/contracts/time")
    @Operation(
        summary = "Get contract time statistics",
        description = "Retrieves time-based statistics about contracts"
    )
    fun getContractTimeStats(): ResponseEntity<Map<String, Any>> {
        val stats = dashboardService.getContractTimeStats()
        return ResponseEntity.ok(stats)
    }
}