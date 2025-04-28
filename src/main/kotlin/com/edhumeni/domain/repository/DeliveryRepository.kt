package com.edhumeni.domain.repository

import com.edhumeni.domain.model.Delivery
import com.edhumeni.domain.model.QualityGrade
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface DeliveryRepository : JpaRepository<Delivery, UUID> {

    fun findByContractId(contractId: UUID): List<Delivery>

    @Query("SELECT d FROM Delivery d WHERE d.contract.farmer.id = :farmerId")
    fun findByContractFarmerId(@Param("farmerId") farmerId: UUID): List<Delivery>

    @Query("SELECT d FROM Delivery d ORDER BY d.deliveryDate DESC LIMIT :limit")
    fun findTopNByOrderByDeliveryDateDesc(@Param("limit") limit: Int): List<Delivery>

    fun findByQualityGrade(qualityGrade: QualityGrade): List<Delivery>

    fun findByDeliveryDateBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Delivery>

    @Query("SELECT SUM(d.quantityKg) FROM Delivery d WHERE d.contract.id = :contractId")
    fun sumQuantityByContractId(@Param("contractId") contractId: UUID): Double?

    @Query("SELECT SUM(d.quantityKg) FROM Delivery d WHERE d.contract.farmer.id = :farmerId")
    fun sumQuantityByFarmerId(@Param("farmerId") farmerId: UUID): Double?

    @Query("SELECT d FROM Delivery d " +
            "WHERE d.contract.id = :contractId " +
            "ORDER BY d.deliveryDate DESC")
    fun findLatestDeliveryByContractId(@Param("contractId") contractId: UUID): List<Delivery>

    @Query("SELECT AVG(d.quantityKg) FROM Delivery d " +
            "WHERE d.contract.farmer.id = :farmerId")
    fun getAverageDeliveryQuantityByFarmerId(@Param("farmerId") farmerId: UUID): Double?

    @Query("SELECT d FROM Delivery d " +
            "WHERE d.contract.id = :contractId " +
            "AND (:startDate IS NULL OR d.deliveryDate >= :startDate) " +
            "AND (:endDate IS NULL OR d.deliveryDate <= :endDate) " +
            "AND (:qualityGrade IS NULL OR d.qualityGrade = :qualityGrade)")
    fun searchDeliveries(
        @Param("contractId") contractId: UUID,
        @Param("startDate") startDate: LocalDateTime?,
        @Param("endDate") endDate: LocalDateTime?,
        @Param("qualityGrade") qualityGrade: QualityGrade?
    ): List<Delivery>

    @Query("SELECT COUNT(d) FROM Delivery d " +
            "WHERE d.contract.farmer.region.id = :regionId " +
            "AND d.deliveryDate BETWEEN :startDate AND :endDate")
    fun countDeliveriesByRegionAndDateRange(
        @Param("regionId") regionId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long

    @Query("SELECT SUM(d.quantityKg) FROM Delivery d " +
            "WHERE d.contract.farmer.region.id = :regionId " +
            "AND d.deliveryDate BETWEEN :startDate AND :endDate")
    fun sumQuantityByRegionAndDateRange(
        @Param("regionId") regionId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Double?
}