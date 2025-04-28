package com.edhumeni.domain.service

import com.edhumeni.domain.model.Region
import com.edhumeni.domain.repository.FarmerRepository
import com.edhumeni.domain.repository.RegionRepository
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.WKTReader
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RegionService(
    private val regionRepository: RegionRepository,
    private val farmerRepository: FarmerRepository
) {

    fun findAll(): List<Region> = regionRepository.findAll()

    fun findById(id: UUID): Region? = regionRepository.findByIdOrNull(id)

    fun findByProvince(province: String): List<Region> = regionRepository.findByProvinceIgnoreCase(province)

    fun findByDistrict(district: String): List<Region> = regionRepository.findByDistrictIgnoreCase(district)

    fun searchRegions(searchTerm: String): List<Region> = regionRepository.searchRegions(searchTerm)

    fun findRegionByCoordinates(latitude: Double, longitude: Double): Region? {
        val wktReader = WKTReader()
        val pointWkt = "POINT($longitude $latitude)"
        val point = wktReader.read(pointWkt) as Point

        return regionRepository.findRegionContainingPoint(point)
    }

    fun findRegionsNearCoordinates(latitude: Double, longitude: Double, distanceInMeters: Double): List<Region> {
        val wktReader = WKTReader()
        val pointWkt = "POINT($longitude $latitude)"
        val point = wktReader.read(pointWkt) as Point

        return regionRepository.findRegionsWithinDistance(point, distanceInMeters)
    }

    fun getAllRegionsWithFarmerCount(): List<Any> = regionRepository.findAllWithFarmerCount()

    fun getAllRegionsWithSupportStats(): List<Any> = regionRepository.findAllWithFarmerAndSupportCounts()

    @Transactional
    fun createRegion(region: Region): Region {
        return regionRepository.save(region)
    }

    @Transactional
    fun updateRegion(id: UUID, updatedRegion: Region): Region {
        val existingRegion = regionRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Region with ID $id not found")

        // Update the fields
        with(existingRegion) {
            name = updatedRegion.name
            province = updatedRegion.province
            district = updatedRegion.district
            naturalRegion = updatedRegion.naturalRegion
            averageAnnualRainfallMm = updatedRegion.averageAnnualRainfallMm
            predominantSoilType = updatedRegion.predominantSoilType

            // Update spatial data if provided
            updatedRegion.boundary?.let { boundary = it }
            updatedRegion.centerLatitude?.let { centerLatitude = it }
            updatedRegion.centerLongitude?.let { centerLongitude = it }
        }

        return regionRepository.save(existingRegion)
    }

    @Transactional
    fun updateRegionBoundary(id: UUID, wktBoundary: String): Region {
        val existingRegion = regionRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Region with ID $id not found")

        val wktReader = WKTReader()
        val geometry = wktReader.read(wktBoundary)

        if (geometry !is org.locationtech.jts.geom.Polygon) {
            throw IllegalArgumentException("The provided WKT must represent a polygon")
        }

        existingRegion.boundary = geometry

        // Also update the center coordinates
        val centroid = geometry.centroid
        existingRegion.centerLatitude = centroid.y
        existingRegion.centerLongitude = centroid.x

        return regionRepository.save(existingRegion)
    }

    @Transactional
    fun deleteRegion(id: UUID): Boolean {
        val region = regionRepository.findByIdOrNull(id) ?: return false

        // Check if there are farmers in this region
        val farmerCount = farmerRepository.countFarmersByRegion(id)
        if (farmerCount > 0) {
            throw IllegalStateException("Cannot delete region with associated farmers")
        }

        regionRepository.delete(region)
        return true
    }

    fun getRegionStats(id: UUID): Map<String, Any> {
        val region = regionRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Region with ID $id not found")

        val farmerCount = farmerRepository.countFarmersByRegion(id)
        val farmersNeedingSupport = farmerRepository.findFarmersNeedingSupportByRegion(id)

        return mapOf(
            "id" to region.id,
            "name" to region.name,
            "province" to region.province,
            "district" to region.district,
            "farmerCount" to farmerCount,
            "farmersNeedingSupportCount" to farmersNeedingSupport.size,
            "supportPercentage" to if (farmerCount > 0) (farmersNeedingSupport.size.toDouble() / farmerCount) * 100 else 0.0
        )
    }
}