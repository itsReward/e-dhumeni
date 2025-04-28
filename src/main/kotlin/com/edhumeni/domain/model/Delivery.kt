package com.edhumeni.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "deliveries")
data class Delivery(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    var contract: Contract,

    @Column(name = "delivery_date", nullable = false)
    var deliveryDate: LocalDateTime,

    @Column(name = "quantity_kg", nullable = false)
    var quantityKg: Double,

    @Column(name = "quality_grade")
    @Enumerated(EnumType.STRING)
    var qualityGrade: QualityGrade,

    @Column(name = "moisture_content")
    var moistureContent: Double? = null,

    @Column(name = "price_paid_per_kg")
    var pricePaidPerKg: Double? = null,

    @Column(name = "total_amount_paid")
    var totalAmountPaid: Double? = null,

    @Column(name = "deduction_amount")
    var deductionAmount: Double = 0.0,

    @Column(name = "deduction_reason")
    var deductionReason: String? = null,

    @Column(name = "receipt_number")
    var receiptNumber: String? = null,

    @Column(name = "verified_by")
    var verifiedBy: String? = null,

    @Column(name = "verified_at")
    var verifiedAt: LocalDateTime? = null,

    @Column(name = "notes")
    var notes: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class QualityGrade {
    A_PLUS, A, B, C, REJECTED
}