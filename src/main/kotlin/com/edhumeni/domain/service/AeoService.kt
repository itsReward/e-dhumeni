package com.edhumeni.domain.service

import com.edhumeni.domain.model.AgriculturalExtensionOfficer
import com.edhumeni.domain.model.Region
import com.edhumeni.domain.repository.AeoRepository
import com.edhumeni.domain.repository.RegionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AeoService(
    private val aeoRepository: AeoRepository,
    private val regionRepository: RegionRepository
) {

    fun findAll(): List<AgriculturalExtensionOfficer> = aeoRepository.findAll()

    fun findById(id: UUID): AgriculturalExtensionOfficer? = aeoRepository.findByIdOrNull(id)

    fun findByEmployeeId(employeeId: String): AgriculturalExtensionOfficer? =
        aeoRepository.findByEmployeeId(employeeId).orElse(null)

    fun findByRegionId(regionId: UUID): List<AgriculturalExtensionOfficer> =
        aeoRepository.findByAssignedRegionId(regionId)

    fun findWithFarmersNeedingSupport(): List<AgriculturalExtensionOfficer> =
        aeoRepository.findAeosWithFarmersNeedingSupport()

    fun searchAeos(searchTerm: String): List<AgriculturalExtensionOfficer> =
        aeoRepository.searchAeos(searchTerm)

    @Transactional
    fun createAeo(aeo: AgriculturalExtensionOfficer): AgriculturalExtensionOfficer {
        return aeoRepository.save(aeo)
    }

    @Transactional
    fun updateAeo(id: UUID, updatedAeo: AgriculturalExtensionOfficer): AgriculturalExtensionOfficer {
        val existingAeo = aeoRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("AEO with ID $id not found")

        with(existingAeo) {
            name = updatedAeo.name
            contactNumber = updatedAeo.contactNumber
            email = updatedAeo.email
            qualification = updatedAeo.qualification
            yearsOfExperience = updatedAeo.yearsOfExperience
        }

        return aeoRepository.save(existingAeo)
    }

    @Transactional
    fun assignRegion(aeoId: UUID, regionId: UUID): AgriculturalExtensionOfficer {
        val aeo = aeoRepository.findByIdOrNull(aeoId)
            ?: throw IllegalArgumentException("AEO with ID $aeoId not found")

        val region = regionRepository.findByIdOrNull(regionId)
            ?: throw IllegalArgumentException("Region with ID $regionId not found")

        aeo.assignedRegions.add(region)
        return aeoRepository.save(aeo)
    }

    @Transactional
    fun unassignRegion(aeoId: UUID, regionId: UUID): AgriculturalExtensionOfficer {
        val aeo = aeoRepository.findByIdOrNull(aeoId)
            ?: throw IllegalArgumentException("AEO with ID $aeoId not found")

        aeo.assignedRegions.removeIf { it.id == regionId }
        return aeoRepository.save(aeo)
    }

    @Transactional
    fun deleteAeo(id: UUID): Boolean {
        val aeo = aeoRepository.findByIdOrNull(id) ?: return false

        // Check if AEO has assigned farmers
        if (aeo.farmers.isNotEmpty()) {
            throw IllegalStateException("Cannot delete AEO with assigned farmers")
        }

        aeoRepository.delete(aeo)
        return true
    }

    fun getAeoStats(id: UUID): Map<String, Any> {
        val aeo = aeoRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("AEO with ID $id not found")

        val totalFarmers = aeo.getTotalFarmers()
        val farmersNeedingSupport = aeo.getFarmersNeedingSupport()

        return mapOf(
            "id" to aeo.id,
            "name" to aeo.name,
            "totalFarmers" to totalFarmers,
            "farmersNeedingSupport" to farmersNeedingSupport.size,
            "supportPercentage" to if (totalFarmers > 0) (farmersNeedingSupport.size.toDouble() / totalFarmers) * 100 else 0.0,
            "assignedRegions" to aeo.assignedRegions.size
        )
    }
}