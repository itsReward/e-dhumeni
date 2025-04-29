package com.edhumeni.domain.repository

import com.edhumeni.domain.model.Region
import com.edhumeni.domain.model.RegionStats
import org.locationtech.jts.geom.Point
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RegionRepository : JpaRepository<Region, UUID> {

    fun findByProvinceIgnoreCase(province: String): List<Region>

    fun findByDistrictIgnoreCase(district: String): List<Region>

    @Query("SELECT r FROM Region r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(r.province) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(r.district) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    fun searchRegions(@Param("searchTerm") searchTerm: String): List<Region>

    @Query(value = "SELECT r.* FROM regions r " +
            "WHERE ST_Contains(r.boundary, :point)", nativeQuery = true)
    fun findRegionContainingPoint(@Param("point") point: Point): Region?

    @Query(value = "SELECT r.* FROM regions r " +
            "WHERE ST_Distance(r.boundary, :point) <= :distanceMeters", nativeQuery = true)
    fun findRegionsWithinDistance(
        @Param("point") point: Point,
        @Param("distanceMeters") distanceInMeters: Double
    ): List<Region>

    @Query(value = "SELECT r.*, " +
            "(SELECT COUNT(*) FROM farmers f WHERE f.region_id = r.id) as farmer_count " +
            "FROM regions r", nativeQuery = true)
    fun findAllWithFarmerCount(): List<Any>

    @Query(value = "SELECT r.id as id, r.name as name, r.province as province, r.district as district, " +
            "(SELECT COUNT(*) FROM farmers f WHERE f.region_id = r.id) as farmerCount, " +
            "(SELECT COUNT(*) FROM farmers f WHERE f.region_id = r.id AND f.needs_support = true) as supportCount " +
            "FROM regions r", nativeQuery = true)
    fun findAllWithFarmerAndSupportCounts(): List<RegionStats>

    @Query(value = "SELECT r.name, COUNT(f.id) as farmer_count " +
            "FROM regions r " +
            "JOIN farmers f ON f.region_id = r.id " +
            "GROUP BY r.name " +
            "ORDER BY farmer_count DESC " +
            "LIMIT :limit", nativeQuery = true)
    fun findTopRegionsByFarmerCount(@Param("limit") limit: Int): List<Array<Any>>

}