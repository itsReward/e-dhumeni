package com.edhumeni.domain.repository

import com.edhumeni.domain.model.Contract
import com.edhumeni.domain.model.ContractType
import com.edhumeni.domain.model.RepaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@Repository
interface ContractRepository : JpaRepository<Contract, UUID> {

    fun findByContractNumber(contractNumber: String): Optional<Contract>

    fun findByFarmerId(farmerId: UUID): List<Contract>

    fun findByFarmerIdAndActive(farmerId: UUID, active: Boolean): List<Contract>

    @Query("SELECT c FROM Contract c " +
            "WHERE c.farmer.region.id = :regionId")
    fun findByRegionId(@Param("regionId") UUID: UUID): List<Contract>

    @Query("SELECT c FROM Contract c " +
            "WHERE c.farmer.agriculturalExtensionOfficer.id = :aeoId")
    fun findByAeoId(@Param("aeoId") aeoId: UUID): List<Contract>

    fun findByType(type: ContractType): List<Contract>

    fun findByRepaymentStatus(status: RepaymentStatus): List<Contract>

    fun findByEndDateBefore(date: LocalDate): List<Contract>

    @Query("""
    SELECT c FROM Contract c 
    WHERE c.endDate < :date 
    AND (SELECT COALESCE(SUM(d.quantityKg), 0) FROM Delivery d WHERE d.contract = c) < c.expectedDeliveryKg
""")
    fun findExpiredContractsWithUnmetDeliveries(@Param("date") date: LocalDate): List<Contract>

    @Query("""
    SELECT c FROM Contract c 
    WHERE c.active = true 
    AND c.endDate >= :today 
    AND (SELECT COALESCE(SUM(d.quantityKg), 0) FROM Delivery d WHERE d.contract = c) < (c.expectedDeliveryKg * 0.8) 
    AND c.endDate <= :thirtyDaysLater
""")
    fun findAtRiskContracts(
        @Param("today") today: LocalDate,
        @Param("thirtyDaysLater") thirtyDaysLater: LocalDate
    ): List<Contract>

    @Query("SELECT c FROM Contract c WHERE " +
            "(:farmerId IS NULL OR c.farmer.id = :farmerId) AND " +
            "(:type IS NULL OR c.type = :type) AND " +
            "(:active IS NULL OR c.active = :active) AND " +
            "(:repaymentStatus IS NULL OR c.repaymentStatus = :repaymentStatus)")
    fun searchContracts(
        @Param("farmerId") farmerId: UUID?,
        @Param("type") type: ContractType?,
        @Param("active") active: Boolean?,
        @Param("repaymentStatus") repaymentStatus: RepaymentStatus?
    ): List<Contract>
}