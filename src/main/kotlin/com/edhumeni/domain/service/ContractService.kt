package com.edhumeni.domain.service

import com.edhumeni.domain.model.Contract
import com.edhumeni.domain.model.ContractType
import com.edhumeni.domain.model.Delivery
import com.edhumeni.domain.model.RepaymentStatus
import com.edhumeni.domain.repository.ContractRepository
import com.edhumeni.domain.repository.FarmerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class ContractService(
    private val contractRepository: ContractRepository,
    private val farmerRepository: FarmerRepository
) {

    fun findAll(): List<Contract> = contractRepository.findAll()

    fun findById(id: UUID): Contract? = contractRepository.findByIdOrNull(id)

    fun findByContractNumber(contractNumber: String): Contract? =
        contractRepository.findByContractNumber(contractNumber).orElse(null)

    fun findByFarmerId(farmerId: UUID): List<Contract> = contractRepository.findByFarmerId(farmerId)

    fun findActiveContractsByFarmerId(farmerId: UUID): List<Contract> =
        contractRepository.findByFarmerIdAndActive(farmerId, true)

    fun findByRegionId(regionId: UUID): List<Contract> = contractRepository.findByRegionId(regionId)

    fun findByAeoId(aeoId: UUID): List<Contract> = contractRepository.findByAeoId(aeoId)

    fun findByType(type: ContractType): List<Contract> = contractRepository.findByType(type)

    fun findByRepaymentStatus(status: RepaymentStatus): List<Contract> =
        contractRepository.findByRepaymentStatus(status)

    fun findExpiredContracts(): List<Contract> =
        contractRepository.findByEndDateBefore(LocalDate.now())

    fun findExpiredContractsWithUnmetDeliveries(): List<Contract> =
        contractRepository.findExpiredContractsWithUnmetDeliveries(LocalDate.now())

    fun findAtRiskContracts(): List<Contract> {
        val today = LocalDate.now()
        val thirtyDaysLater = today.plusDays(30)
        return contractRepository.findAtRiskContracts(today, thirtyDaysLater)
    }

    fun searchContracts(
        farmerId: UUID? = null,
        type: ContractType? = null,
        active: Boolean? = null,
        repaymentStatus: RepaymentStatus? = null
    ): List<Contract> = contractRepository.searchContracts(farmerId, type, active, repaymentStatus)

    @Transactional
    fun createContract(contract: Contract, farmerId: UUID): Contract {
        val farmer = farmerRepository.findByIdOrNull(farmerId)
            ?: throw IllegalArgumentException("Farmer with ID $farmerId not found")

        contract.farmer = farmer

        // Generate a unique contract number if not provided
        if (contract.contractNumber.isBlank()) {
            val timestamp = System.currentTimeMillis().toString().takeLast(8)
            val farmerInitials = farmer.name.split(" ")
                .filter { it.isNotBlank() }
                .joinToString("") { it.first().toString() }

            contract.contractNumber = "CTR-$farmerInitials-$timestamp"
        }

        // Calculate total owed amount if loan/advance components exist
        var totalOwed = 0.0

        if (contract.advancePayment != null && contract.advancePayment!! > 0) {
            totalOwed += contract.advancePayment!!
        }

        if (contract.inputSupportValue != null && contract.inputSupportValue!! > 0) {
            totalOwed += contract.inputSupportValue!!
            contract.hasLoanComponent = true
        }

        contract.totalOwedAmount = totalOwed

        return contractRepository.save(contract)
    }

    @Transactional
    fun updateContract(id: UUID, updatedContract: Contract): Contract {
        val existingContract = contractRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Contract with ID $id not found")

        with(existingContract) {
            // Don't update farmer or contract number - these are fixed
            startDate = updatedContract.startDate
            endDate = updatedContract.endDate
            type = updatedContract.type
            expectedDeliveryKg = updatedContract.expectedDeliveryKg
            pricePerKg = updatedContract.pricePerKg
            advancePayment = updatedContract.advancePayment
            inputSupportValue = updatedContract.inputSupportValue
            signingBonus = updatedContract.signingBonus
            repaymentStatus = updatedContract.repaymentStatus
            totalRepaidAmount = updatedContract.totalRepaidAmount
            challengesMeetingTerms = updatedContract.challengesMeetingTerms
            hasLoanComponent = updatedContract.hasLoanComponent
            active = updatedContract.active

            // Recalculate total owed if needed
            var totalOwed = 0.0

            if (advancePayment != null && advancePayment!! > 0) {
                totalOwed += advancePayment!!
            }

            if (inputSupportValue != null && inputSupportValue!! > 0) {
                totalOwed += inputSupportValue!!
            }

            totalOwedAmount = totalOwed
        }

        return contractRepository.save(existingContract)
    }

    @Transactional
    fun addDelivery(contractId: UUID, delivery: Delivery): Contract {
        val contract = contractRepository.findByIdOrNull(contractId)
            ?: throw IllegalArgumentException("Contract with ID $contractId not found")

        delivery.contract = contract
        contract.deliveries.add(delivery)

        // Update repayment status based on deliveries
        updateRepaymentStatus(contract)

        return contractRepository.save(contract)
    }

    @Transactional
    fun updateRepaymentStatus(contract: Contract): Contract {
        // Calculate total delivered amount
        val totalDeliveredKg = contract.getTotalDeliveredKg()

        // Update repayment status based on percentage of expected delivery
        val deliveryPercentage = if (contract.expectedDeliveryKg > 0) {
            (totalDeliveredKg / contract.expectedDeliveryKg) * 100
        } else {
            0.0
        }

        // Update repayment status based on delivery percentage and other factors
        contract.repaymentStatus = when {
            deliveryPercentage >= 100 -> RepaymentStatus.COMPLETED
            deliveryPercentage > 0 -> RepaymentStatus.IN_PROGRESS
            contract.endDate.isBefore(LocalDate.now()) && deliveryPercentage < 100 -> RepaymentStatus.DEFAULTED
            else -> RepaymentStatus.NOT_STARTED
        }

        return contractRepository.save(contract)
    }

    @Transactional
    fun deactivateContract(id: UUID): Contract {
        val contract = contractRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Contract with ID $id not found")

        contract.active = false
        return contractRepository.save(contract)
    }

    @Transactional
    fun deleteContract(id: UUID): Boolean {
        val contract = contractRepository.findByIdOrNull(id) ?: return false

        // Check if contract has deliveries
        if (contract.deliveries.isNotEmpty()) {
            throw IllegalStateException("Cannot delete contract with recorded deliveries")
        }

        contractRepository.delete(contract)
        return true
    }

    fun getContractSummary(contractId: UUID): Map<String, Any> {
        val contract = contractRepository.findByIdOrNull(contractId)
            ?: throw IllegalArgumentException("Contract with ID $contractId not found")

        val totalDeliveredKg = contract.getTotalDeliveredKg()
        val completionPercentage = contract.getDeliveryCompletionPercentage()
        val isBehindSchedule = contract.isBehindSchedule()

        return mapOf(
            "id" to contract.id,
            "contractNumber" to contract.contractNumber,
            "farmerName" to contract.farmer.name,
            "startDate" to contract.startDate,
            "endDate" to contract.endDate,
            "type" to contract.type,
            "expectedDeliveryKg" to contract.expectedDeliveryKg,
            "actualDeliveryKg" to totalDeliveredKg,
            "completionPercentage" to completionPercentage,
            "active" to contract.active,
            "repaymentStatus" to contract.repaymentStatus,
            "atRisk" to isBehindSchedule,
            "remainingDays" to if (contract.endDate.isAfter(LocalDate.now())) {
                java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), contract.endDate)
            } else 0,
            "deliveriesCount" to contract.deliveries.size
        )
    }
}