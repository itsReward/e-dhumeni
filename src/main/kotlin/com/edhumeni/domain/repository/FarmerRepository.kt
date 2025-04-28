package com.edhumeni.domain.repository

import com.edhumeni.domain.model.Farmer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FarmerRepository : JpaRepository<Farmer, UUID> {
    fun findByRegionId(regionId: UUID): List<Farmer>

    fun findByAgriculturalExtensionOfficerId(aeoId: UUID): List<Farmer>

    @Query("SELECT f FROM Farmer f WHERE f.needsSupport = true")
    fun findFarmersNeedingSupport(): List<Farmer>

    @Query("SELECT f FROM Farmer f WHERE f.region.id = :regionId AND f.needsSupport = true")
    fun findFarmersNeedingSupportByRegion(@Param("regionId") regionId: UUID): List<Farmer>

    @Query("SELECT COUNT(f) FROM Farmer f WHERE f.region.id = :regionId")
    fun countFarmersByRegion(@Param("regionId") regionId: UUID): Long

    @Query("""
    SELECT DISTINCT f FROM Farmer f 
    JOIN Contract c ON c.farmer.id = f.id 
    WHERE c.repaymentStatus = 'DEFAULTED' 
    OR (c.repaymentStatus = 'IN_PROGRESS' AND c.endDate < CURRENT_DATE)
""")
    fun findFarmersWithRepaymentIssues(): List<Farmer>

    fun findByNameContainingIgnoreCase(name: String): List<Farmer>

    @Query("""
        SELECT f FROM Farmer f 
        WHERE (:name IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:region IS NULL OR LOWER(f.region.name) LIKE LOWER(CONCAT('%', :region, '%')))
        AND (:province IS NULL OR LOWER(f.province) LIKE LOWER(CONCAT('%', :province, '%')))
        AND (:ward IS NULL OR LOWER(f.ward) LIKE LOWER(CONCAT('%', :ward, '%')))
    """)
    fun searchFarmers(
        @Param("name") name: String?,
        @Param("region") region: String?,
        @Param("province") province: String?,
        @Param("ward") ward: String?
    ): List<Farmer>
}