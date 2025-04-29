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
    SELECT DISTINCT c FROM Contract c 
    LEFT JOIN c.deliveries d
    WHERE c.endDate < :date
    GROUP BY c
    HAVING COALESCE(SUM(d.quantityKg), 0.0) < (c.expectedDeliveryKg * 0.8)
    """)
    fun findExpiredContractsWithUnmetDeliveries(@Param("date") date: LocalDate): List<Contract>

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

    fun findByEndDateAfterAndActive(endDate: LocalDate, active: Boolean): List<Contract>

    @Query("""
    SELECT DISTINCT c FROM Contract c 
    LEFT JOIN c.deliveries d
    WHERE c.active = true 
    AND c.endDate >= :today 
    AND c.endDate <= :thirtyDaysLater
    GROUP BY c
    HAVING COALESCE(SUM(d.quantityKg), 0.0) < (c.expectedDeliveryKg * 0.8)
    """)
    fun findAtRiskContracts(
        @Param("today") today: LocalDate,
        @Param("thirtyDaysLater") thirtyDaysLater: LocalDate
    ): List<Contract>


    //fun findByEndDateBeforeAndCompletedFalse(date: LocalDate): List<Contract>


    @Query("""
    SELECT
        FUNCTION('YEAR', c.startDate) as year,
        FUNCTION('MONTH', c.startDate) as month,
        COUNT(c) as count,
        SUM(c.expectedDeliveryKg) as expectedTotal,
        COALESCE(SUM(d.quantityKg), 0.0) as deliveredTotal
    FROM Contract c
    LEFT JOIN c.deliveries d
    WHERE c.startDate BETWEEN :startDate AND :endDate
    GROUP BY FUNCTION('YEAR', c.startDate), FUNCTION('MONTH', c.startDate)
    ORDER BY FUNCTION('YEAR', c.startDate), FUNCTION('MONTH', c.startDate)
    """)
    fun getContractTrendByMonth(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Array<Any>>

    @Query("""
SELECT c FROM Contract c
WHERE c.active = true
AND c.endDate >= :today
AND :today > c.startDate
""")
fun findPotentialContractsBehindSchedule(@Param("today") today: LocalDate): List<Contract>
}