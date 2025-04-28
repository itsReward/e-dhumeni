package com.edhumeni.domain.repository

import com.edhumeni.domain.model.AgriculturalExtensionOfficer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface AeoRepository : JpaRepository<AgriculturalExtensionOfficer, UUID> {

    fun findByEmployeeId(employeeId: String): Optional<AgriculturalExtensionOfficer>

    fun findByEmail(email: String): Optional<AgriculturalExtensionOfficer>

    @Query("SELECT aeo FROM AgriculturalExtensionOfficer aeo JOIN aeo.assignedRegions r WHERE r.id = :regionId")
    fun findByAssignedRegionId(@Param("regionId") regionId: UUID): List<AgriculturalExtensionOfficer>

    @Query("SELECT aeo FROM AgriculturalExtensionOfficer aeo " +
            "WHERE LOWER(aeo.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(aeo.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(aeo.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    fun searchAeos(@Param("searchTerm") searchTerm: String): List<AgriculturalExtensionOfficer>

    @Query(value = "SELECT aeo.*, " +
            "(SELECT COUNT(*) FROM farmers f WHERE f.aeo_id = aeo.id) as farmer_count " +
            "FROM agricultural_extension_officers aeo", nativeQuery = true)
    fun findAllWithFarmerCount(): List<Any>

    @Query("SELECT aeo FROM AgriculturalExtensionOfficer aeo " +
            "WHERE (SELECT COUNT(f) FROM Farmer f WHERE f.agriculturalExtensionOfficer.id = aeo.id) > :minFarmers")
    fun findByFarmerCountGreaterThan(@Param("minFarmers") minFarmers: Long): List<AgriculturalExtensionOfficer>

    @Query("SELECT aeo FROM AgriculturalExtensionOfficer aeo " +
            "WHERE (SELECT COUNT(f) FROM Farmer f WHERE f.agriculturalExtensionOfficer.id = aeo.id AND f.needsSupport = true) > 0")
    fun findAeosWithFarmersNeedingSupport(): List<AgriculturalExtensionOfficer>
}