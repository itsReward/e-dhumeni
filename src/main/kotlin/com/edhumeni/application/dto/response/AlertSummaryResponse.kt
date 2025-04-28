package com.edhumeni.application.dto.response

import java.util.UUID

data class AlertSummaryResponse(
    val totalFarmersNeedingSupport: Int,
    val percentageNeedingSupport: Double,
    val totalAtRiskContracts: Int,
    val supportByReason: Map<String, Int>
) {
    constructor(summaryMap: Map<String, Any>) : this(
        totalFarmersNeedingSupport = summaryMap["totalFarmersNeedingSupport"] as Int,
        percentageNeedingSupport = summaryMap["percentageNeedingSupport"] as Double,
        totalAtRiskContracts = summaryMap["totalAtRiskContracts"] as Int,
        //@Suppress("UNCHECKED_CAST")
        supportByReason = summaryMap["supportByReason"] as Map<String, Int>
    )
}