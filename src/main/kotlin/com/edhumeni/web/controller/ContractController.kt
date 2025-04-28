package com.edhumeni.web.controller

import com.edhumeni.application.dto.request.ContractRequest
import com.edhumeni.application.dto.response.ContractResponse
import com.edhumeni.application.dto.response.ContractSummaryResponse
import com.edhumeni.application.mapper.ContractMapper
import com.edhumeni.domain.model.ContractType
import com.edhumeni.domain.model.RepaymentStatus
import com.edhumeni.domain.service.ContractService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contract Management", description = "Endpoints for managing farmer contracts")
class ContractController(
    private val contractService: ContractService,
    private val contractMapper: ContractMapper
) {

    @GetMapping
    @Operation(summary = "Get all contracts", description = "Retrieves all contracts with optional filtering")
    fun getAllContracts(
        @RequestParam(required = false) farmerId: UUID?,
        @RequestParam(required = false) type: ContractType?,
        @RequestParam(required = false) active: Boolean?,
        @RequestParam(required = false) repaymentStatus: RepaymentStatus?
    ): ResponseEntity<List<ContractResponse>> {
        val contracts = if (farmerId != null || type != null || active != null || repaymentStatus != null) {
            contractService.searchContracts(farmerId, type, active, repaymentStatus)
        } else {
            contractService.findAll()
        }
        return ResponseEntity.ok(contracts.map { contractMapper.toResponse(it) })
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contract by ID", description = "Retrieves a specific contract by its ID")
    fun getContractById(@PathVariable id: UUID): ResponseEntity<ContractResponse> {
        val contract = contractService.findById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(contractMapper.toResponse(contract))
    }

    @GetMapping("/farmer/{farmerId}")
    @Operation(summary = "Get contracts by farmer", description = "Retrieves all contracts for a specific farmer")
    fun getContractsByFarmer(@PathVariable farmerId: UUID): ResponseEntity<List<ContractResponse>> {
        val contracts = contractService.findByFarmerId(farmerId)
        return ResponseEntity.ok(contracts.map { contractMapper.toResponse(it) })
    }

    @GetMapping("/farmer/{farmerId}/active")
    @Operation(summary = "Get active contracts by farmer", description = "Retrieves active contracts for a specific farmer")
    fun getActiveContractsByFarmer(@PathVariable farmerId: UUID): ResponseEntity<List<ContractResponse>> {
        val contracts = contractService.findActiveContractsByFarmerId(farmerId)
        return ResponseEntity.ok(contracts.map { contractMapper.toResponse(it) })
    }

    @GetMapping("/region/{regionId}")
    @Operation(summary = "Get contracts by region", description = "Retrieves all contracts in a specific region")
    fun getContractsByRegion(@PathVariable regionId: UUID): ResponseEntity<List<ContractResponse>> {
        val contracts = contractService.findByRegionId(regionId)
        return ResponseEntity.ok(contracts.map { contractMapper.toResponse(it) })
    }

    @GetMapping("/aeo/{aeoId}")
    @Operation(summary = "Get contracts by AEO", description = "Retrieves all contracts managed by a specific AEO")
    fun getContractsByAeo(@PathVariable aeoId: UUID): ResponseEntity<List<ContractResponse>> {
        val contracts = contractService.findByAeoId(aeoId)
        return ResponseEntity.ok(contracts.map { contractMapper.toResponse(it) })
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get contracts by type", description = "Retrieves all contracts of a specific type")
    fun getContractsByType(@PathVariable type: ContractType): ResponseEntity<List<ContractResponse>> {
        val contracts = contractService.findByType(type)
        return ResponseEntity.ok(contracts.map { contractMapper.toResponse(it) })
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get contracts by repayment status", description = "Retrieves all contracts with a specific repayment status")
    fun getContractsByStatus(@PathVariable status: RepaymentStatus): ResponseEntity<List<ContractResponse>> {
        val contracts = contractService.findByRepaymentStatus(status)
        return ResponseEntity.ok(contracts.map { contractMapper.toResponse(it) })
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired contracts", description = "Retrieves all contracts that have expired")
    fun getExpiredContracts(): ResponseEntity<List<ContractResponse>> {
        val contracts = contractService.findExpiredContracts()
        return ResponseEntity.ok(contracts.map { contractMapper.toResponse(it) })
    }

    @GetMapping("/at-risk")
    @Operation(summary = "Get at-risk contracts", description = "Retrieves all contracts at risk of non-completion")
    fun getAtRiskContracts(): ResponseEntity<List<ContractResponse>> {
        val contracts = contractService.findAtRiskContracts()
        return ResponseEntity.ok(contracts.map { contractMapper.toResponse(it) })
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Get contract summary", description = "Retrieves a summary of a specific contract")
    fun getContractSummary(@PathVariable id: UUID): ResponseEntity<ContractSummaryResponse> {
        val summary = contractService.getContractSummary(id)
        return ResponseEntity.ok(ContractSummaryResponse(summary))
    }

    @PostMapping
    @Operation(summary = "Create a contract", description = "Creates a new contract for a farmer")
    fun createContract(
        @Valid @RequestBody request: ContractRequest
    ): ResponseEntity<ContractResponse> {
        val contract = contractMapper.toEntity(request)
        val createdContract = contractService.createContract(contract, request.farmerId)

        return ResponseEntity.status(HttpStatus.CREATED).body(contractMapper.toResponse(createdContract))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a contract", description = "Updates an existing contract")
    fun updateContract(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ContractRequest
    ): ResponseEntity<ContractResponse> {
        val contract = contractMapper.toEntity(request)
        val updatedContract = contractService.updateContract(id, contract)

        return ResponseEntity.ok(contractMapper.toResponse(updatedContract))
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a contract", description = "Marks a contract as inactive")
    fun deactivateContract(@PathVariable id: UUID): ResponseEntity<ContractResponse> {
        val deactivatedContract = contractService.deactivateContract(id)
        return ResponseEntity.ok(contractMapper.toResponse(deactivatedContract))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a contract", description = "Deletes a contract if it has no deliveries")
    fun deleteContract(@PathVariable id: UUID): ResponseEntity<Void> {
        try {
            val deleted = contractService.deleteContract(id)

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