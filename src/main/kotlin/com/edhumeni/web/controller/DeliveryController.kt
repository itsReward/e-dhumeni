package com.edhumeni.web.controller

import com.edhumeni.application.dto.request.DeliveryRequest
import com.edhumeni.application.dto.response.DeliveryResponse
import com.edhumeni.application.mapper.DeliveryMapper
import com.edhumeni.domain.service.ContractService
import com.edhumeni.domain.service.DeliveryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/deliveries")
@Tag(name = "Delivery Management", description = "Endpoints for managing farmer deliveries")
class DeliveryController(
    private val deliveryService: DeliveryService,
    private val contractService: ContractService,
    private val deliveryMapper: DeliveryMapper
) {

    @GetMapping
    @Operation(summary = "Get all deliveries", description = "Retrieves all deliveries with optional filtering")
    fun getAllDeliveries(
        @RequestParam(required = false) contractId: UUID?,
        @RequestParam(required = false) farmerId: UUID?
    ): ResponseEntity<List<DeliveryResponse>> {
        val deliveries = when {
            contractId != null -> deliveryService.findByContractId(contractId)
            farmerId != null -> deliveryService.findByFarmerId(farmerId)
            else -> deliveryService.findAll()
        }
        return ResponseEntity.ok(deliveries.map { deliveryMapper.toResponse(it) })
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get delivery by ID", description = "Retrieves a specific delivery by its ID")
    fun getDeliveryById(@PathVariable id: UUID): ResponseEntity<DeliveryResponse> {
        val delivery = deliveryService.findById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(deliveryMapper.toResponse(delivery))
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Get deliveries by contract", description = "Retrieves all deliveries for a specific contract")
    fun getDeliveriesByContract(@PathVariable contractId: UUID): ResponseEntity<List<DeliveryResponse>> {
        val deliveries = deliveryService.findByContractId(contractId)
        return ResponseEntity.ok(deliveries.map { deliveryMapper.toResponse(it) })
    }

    @GetMapping("/farmer/{farmerId}")
    @Operation(summary = "Get deliveries by farmer", description = "Retrieves all deliveries for a specific farmer")
    fun getDeliveriesByFarmer(@PathVariable farmerId: UUID): ResponseEntity<List<DeliveryResponse>> {
        val deliveries = deliveryService.findByFarmerId(farmerId)
        return ResponseEntity.ok(deliveries.map { deliveryMapper.toResponse(it) })
    }

    @PostMapping
    @Operation(summary = "Record a delivery", description = "Records a new delivery for a contract")
    fun recordDelivery(
        @Valid @RequestBody request: DeliveryRequest,
        authentication: Authentication
    ): ResponseEntity<DeliveryResponse> {
        val delivery = deliveryMapper.toEntity(request)

        // Set the verifier information if available
        delivery.verifiedBy = authentication.name

        val recordedDelivery = deliveryService.recordDelivery(
            delivery = delivery,
            contractId = request.contractId
        )

        // Update the contract repayment status
        contractService.updateRepaymentStatus(recordedDelivery.contract)

        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryMapper.toResponse(recordedDelivery))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a delivery", description = "Updates an existing delivery record")
    fun updateDelivery(
        @PathVariable id: UUID,
        @Valid @RequestBody request: DeliveryRequest,
        authentication: Authentication
    ): ResponseEntity<DeliveryResponse> {
        val delivery = deliveryMapper.toEntity(request)

        // Set the verifier information
        delivery.verifiedBy = authentication.name

        val updatedDelivery = deliveryService.updateDelivery(id, delivery)

        // Update the contract repayment status
        contractService.updateRepaymentStatus(updatedDelivery.contract)

        return ResponseEntity.ok(deliveryMapper.toResponse(updatedDelivery))
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a delivery",
        description = "Deletes a delivery record (use with caution, as it affects contract calculations)"
    )
    fun deleteDelivery(@PathVariable id: UUID): ResponseEntity<Void> {
        // Get the contract before deleting the delivery
        val delivery = deliveryService.findById(id)
            ?: return ResponseEntity.notFound().build()

        val contract = delivery.contract

        val deleted = deliveryService.deleteDelivery(id)

        if (deleted) {
            // Update the contract repayment status
            contractService.updateRepaymentStatus(contract)
            return ResponseEntity.noContent().build()
        } else {
            return ResponseEntity.notFound().build()
        }
    }
}