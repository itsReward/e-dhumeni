package com.edhumeni.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "contracts")
data class Contract(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    var farmer: Farmer,

    @Column(name = "contract_number", nullable = false, unique = true)
    var contractNumber: String,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: ContractType,

    @Column(name = "expected_delivery_kg", nullable = false)
    var expectedDeliveryKg: Double,

    @Column(name = "price_per_kg")
    var pricePerKg: Double? = null,

    @Column(name = "advance_payment")
    var advancePayment: Double? = null,

    @Column(name = "input_support_value")
    var inputSupportValue: Double? = null,

    @Column(name = "signing_bonus")
    var signingBonus: Double? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_status", nullable = false)
    var repaymentStatus: RepaymentStatus = RepaymentStatus.NOT_STARTED,

    @Column(name = "total_repaid_amount")
    var totalRepaidAmount: Double = 0.0,

    @Column(name = "total_owed_amount")
    var totalOwedAmount: Double = 0.0,

    @Column(name = "challenges_meeting_terms")
    var challengesMeetingTerms: String? = null,

    @Column(name = "has_loan_component")
    var hasLoanComponent: Boolean = false,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

    @OneToMany(mappedBy = "contract", cascade = [CascadeType.ALL], orphanRemoval = true)
    var deliveries: MutableList<Delivery> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // Calculate actual deliveries total amount in kg
    fun getTotalDeliveredKg(): Double = deliveries.sumOf { it.quantityKg }

    // Calculate delivery completion percentage
    fun getDeliveryCompletionPercentage(): Double {
        if (expectedDeliveryKg <= 0) return 0.0
        return (getTotalDeliveredKg() / expectedDeliveryKg) * 100
    }

    // Check if contract is behind on delivery schedule
    fun isBehindSchedule(): Boolean {
        // Logic to determine if contract is behind schedule
        // This is a simplistic approach - could be more sophisticated
        val today = LocalDate.now()
        if (today.isAfter(endDate)) {
            return getTotalDeliveredKg() < expectedDeliveryKg
        }

        // Calculate elapsed percentage of contract duration
        val totalDays = endDate.toEpochDay() - startDate.toEpochDay()
        val elapsedDays = today.toEpochDay() - startDate.toEpochDay()
        val elapsedPercentage = (elapsedDays.toDouble() / totalDays) * 100

        // Compare delivery percentage with elapsed percentage
        return getDeliveryCompletionPercentage() < (elapsedPercentage * 0.8) // 20% margin
    }
}

enum class ContractType {
    BASIC, PREMIUM, COOPERATIVE, CORPORATE, GOVERNMENT
}

enum class RepaymentStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED, DEFAULTED, RENEGOTIATED
}