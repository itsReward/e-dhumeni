package com.edhumeni.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "sync_logs")
data class SyncLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "entity_id")
    val entityId: UUID,

    @Column(name = "entity_type")
    val entityType: String,

    @Enumerated(EnumType.STRING)
    val status: SyncStatus,

    @Column(name = "synced_by")
    val syncedBy: String,

    val message: String? = null,

    @CreationTimestamp
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class SyncStatus {
    SUCCESS, FAILED, PENDING
}