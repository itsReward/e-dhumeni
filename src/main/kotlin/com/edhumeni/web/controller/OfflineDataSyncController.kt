package com.edhumeni.web.controller

import com.edhumeni.domain.service.OfflineDataSyncService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/sync")
@Tag(name = "Offline Data Synchronization", description = "Endpoints for offline data synchronization")
class OfflineDataSyncController(
    private val offlineDataSyncService: OfflineDataSyncService
) {

    @GetMapping("/download")
    @Operation(
        summary = "Download offline data",
        description = "Retrieves data package for offline use"
    )
    fun downloadOfflineData(
        @RequestParam(required = false) regionIds: List<UUID>?
    ): ResponseEntity<Map<String, Any>> {
        val offlineData = offlineDataSyncService.prepareOfflineData(regionIds)
        return ResponseEntity.ok(offlineData)
    }

    @PostMapping("/upload")
    @Operation(
        summary = "Upload offline changes",
        description = "Synchronizes offline data changes with the server"
    )
    fun uploadOfflineChanges(
        @RequestBody offlineChanges: List<Map<String, Any>>,
        authentication: Authentication
    ): ResponseEntity<Map<String, Any>> {
        val result = offlineDataSyncService.syncFarmers(offlineChanges, authentication.name)
        return ResponseEntity.ok(result)
    }
}