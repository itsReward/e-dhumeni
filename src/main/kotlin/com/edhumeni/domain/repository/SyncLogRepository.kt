package com.edhumeni.domain.repository

import com.edhumeni.domain.model.SyncLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SyncLogRepository : JpaRepository<SyncLog, UUID> {
    fun findByEntityId(entityId: UUID): List<SyncLog>
    fun findByEntityTypeAndStatus(entityType: String, status: com.edhumeni.domain.model.SyncStatus): List<SyncLog>
    fun findBySyncedBy(syncedBy: String): List<SyncLog>
}