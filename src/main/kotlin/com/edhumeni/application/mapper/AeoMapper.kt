package com.edhumeni.application.mapper

import com.edhumeni.application.dto.request.AeoRequest
import com.edhumeni.application.dto.response.AeoResponse
import com.edhumeni.application.dto.response.RegionSummaryResponse
import com.edhumeni.domain.model.AgriculturalExtensionOfficer
import com.edhumeni.domain.repository.RegionRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AeoMapper(private val regionRepository: RegionRepository) {

    fun toEntity(request: AeoRequest): AgriculturalExtensionOfficer {
        val aeo = AgriculturalExtensionOfficer(
            name = request.name,
            contactNumber = request.contactNumber,
            email = request.email,
            employeeId = request.employeeId,
            qualification = request.qualification,
            yearsOfExperience = request.yearsOfExperience
        )

        // Add regions if provided
        if (request.assignedRegionIds.isNotEmpty()) {
            val regions = regionRepository.findAllById(request.assignedRegionIds)
            aeo.assignedRegions.addAll(regions)
        }

        return aeo
    }

    fun toResponse(entity: AgriculturalExtensionOfficer): AeoResponse {
        return AeoResponse(
            id = entity.id,
            name = entity.name,
            contactNumber = entity.contactNumber,
            email = entity.email,
            employeeId = entity.employeeId,
            qualification = entity.qualification,
            yearsOfExperience = entity.yearsOfExperience,
            assignedRegions = entity.assignedRegions.map { region ->
                RegionSummaryResponse(
                    id = region.id,
                    name = region.name,
                    province = region.province,
                    district = region.district
                )
            },
            farmerCount = entity.getTotalFarmers(),
            farmersNeedingSupportCount = entity.getFarmersNeedingSupport().size,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}