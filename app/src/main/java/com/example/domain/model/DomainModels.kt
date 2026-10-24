package com.example.domain.model

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिंदी"),
    MARATHI("mr", "Marathi", "मराठी"),
    TAMIL("ta", "Tamil", "தமிழ்")
}

enum class UserRole {
    CUSTOMER,
    PROVIDER
}

enum class SubscriptionTier(
    val title: String,
    val monthlyFeeInr: Int,
    val platformCommissionPercent: Double,
    val badgeLabel: String
) {
    FREE("Standard Partner", 0, 2.0, "Verified Partner"),
    PRO("NeighbourPro", 499, 1.5, "Pro Verified"),
    FEATURED("Featured Neighborhood Leader", 1299, 1.0, "Top Featured")
}

data class TrustScoreBreakdown(
    val kycVerificationPoints: Int,      // Max 25 pts (Aadhaar + PAN + Trade Cert + Liveness)
    val jobCompletionPoints: Int,        // Max 35 pts (Completion velocity & zero-dispute record)
    val verifiedReviewPoints: Int,       // Max 25 pts (AI fraud-filtered genuine customer reviews)
    val punctualitySlaPoints: Int        // Max 15 pts (On-time GPS arrival & rapid acceptance)
) {
    val totalScore: Int
        get() = (kycVerificationPoints + jobCompletionPoints + verifiedReviewPoints + punctualitySlaPoints)
            .coerceIn(0, 100)

    val trustLevelLabel: String
        get() = when {
            totalScore >= 92 -> "Platinum Neighborhood Guardian"
            totalScore >= 85 -> "Gold Verified Specialist"
            totalScore >= 75 -> "Silver Trusted Professional"
            else -> "Verified Rising Partner"
        }
}

data class ServiceOffering(
    val id: String,
    val title: String,
    val description: String,
    val priceInr: Int,
    val durationMinutes: Int,
    val warrantyDays: Int = 30
)

data class ReviewItem(
    val id: String,
    val customerName: String,
    val customerNeighborhood: String,
    val rating: Int,
    val comment: String,
    val servicePerformed: String,
    val timestampLabel: String,
    val isVerifiedBooking: Boolean,
    val aiAuthenticityScore: Int,        // 0..100 confidence that review is genuine
    val isFlaggedSuspicious: Boolean = false,
    val aiFraudReason: String? = null
)

data class ServiceProvider(
    val id: String,
    val name: String,
    val category: ProfessionCategory,
    val customProfessionTitle: String? = null,
    val bio: String,
    val experienceYears: Int,
    val neighborhood: String,
    val city: String,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double,
    val isOnlineNow: Boolean,
    val acceptsEmergency: Boolean,
    val responseTimeMinutes: Int,
    val completedJobsCount: Int,
    val repeatCustomerRatePercent: Int,
    val languagesSpoken: List<String>,
    val aadhaarKycVerified: Boolean,
    val tradeLicenseVerified: Boolean,
    val policeBackgroundChecked: Boolean,
    val governmentIdMasked: String,      // e.g., "XXXX-XXXX-8492"
    val trustScoreBreakdown: TrustScoreBreakdown,
    val aiProfileQualityScore: Int,      // 0..100 AI profile completeness & clarity audit
    val subscriptionTier: SubscriptionTier,
    val baseVisitingFeeInr: Int,
    val services: List<ServiceOffering>,
    val reviews: List<ReviewItem>,
    val upiId: String
) {
    val trustScore: Int get() = trustScoreBreakdown.totalScore
    val averageRating: Double
        get() {
            val genuine = reviews.filter { !it.isFlaggedSuspicious }
            return if (genuine.isEmpty()) 4.8 else genuine.map { it.rating }.average()
        }
}

enum class FaultSeverity(val label: String, val urgencyNote: String) {
    LOW("Minor Maintenance", "Can be scheduled within 24–48 hours"),
    MEDIUM("Standard Fault", "Recommended repair within 6–12 hours"),
    HIGH("Urgent Hazard", "Requires attention within 1–2 hours"),
    CRITICAL_EMERGENCY("Critical Safety Emergency", "Immediate SOS dispatch recommended (< 20 mins)")
}

data class SparePartEstimate(
    val partName: String,
    val estimatedCostMinInr: Int,
    val estimatedCostMaxInr: Int,
    val isOptional: Boolean = false
)

data class AiDiagnosisReport(
    val id: String,
    val inputDescription: String,
    val inputScenarioTitle: String,
    val recommendedCategory: ProfessionCategory,
    val faultTitle: String,
    val rootCauseAnalysis: String,
    val severity: FaultSeverity,
    val spareParts: List<SparePartEstimate>,
    val estimatedLaborMinInr: Int,
    val estimatedLaborMaxInr: Int,
    val estimatedTimeMinutes: Int,
    val safetyPrecautions: List<String>,
    val aiConfidencePercent: Int
) {
    val totalMinInr: Int
        get() = estimatedLaborMinInr + spareParts.filter { !it.isOptional }.sumOf { it.estimatedCostMinInr }
    val totalMaxInr: Int
        get() = estimatedLaborMaxInr + spareParts.sumOf { it.estimatedCostMaxInr }
}

enum class BookingStatus(val label: String, val stepIndex: Int) {
    REQUESTED("Booking Requested", 1),
    QUOTE_SUBMITTED("Digital Quote Ready", 2),
    ESCROW_FUNDED("Escrow Locked & Dispatched", 3),
    PROVIDER_EN_ROUTE("Specialist En Route (Live GPS)", 4),
    IN_PROGRESS("Service In Progress", 5),
    COMPLETED("Completed & Escrow Released", 6),
    CANCELLED("Cancelled", 0)
}

data class DigitalQuote(
    val quoteId: String,
    val laborDescription: String,
    val laborCostInr: Int,
    val partsDescription: String,
    val partsCostInr: Int,
    val warrantyDays: Int,
    val notes: String,
    val isAccepted: Boolean = false
) {
    val subtotalInr: Int get() = laborCostInr + partsCostInr
    val gstAmountInr: Int get() = (subtotalInr * 0.18).toInt()
    val totalCustomerPayableInr: Int get() = subtotalInr + gstAmountInr
    val platformCommissionInr: Int get() = (subtotalInr * 0.02).toInt().coerceAtLeast(5)
    val providerNetPayoutInr: Int get() = subtotalInr - platformCommissionInr
}

data class ServiceBooking(
    val id: String,
    val providerId: String,
    val providerName: String,
    val providerCategory: ProfessionCategory,
    val providerTrustScore: Int,
    val customerAddress: String,
    val issueSummary: String,
    val scheduledSlotLabel: String,
    val isEmergencySos: Boolean,
    val status: BookingStatus,
    val quote: DigitalQuote,
    val completionOtp: String,
    val razorpayOrderId: String?,
    val paymentMethodLabel: String?,
    val invoiceNumber: String?,
    val providerEtaMinutes: Int,
    val providerDistanceKm: Double,
    val providerCurrentLat: Double,
    val providerCurrentLng: Double,
    val createdAtTimestamp: Long,
    val isReviewed: Boolean = false
)

enum class MessageSender {
    CUSTOMER,
    PROVIDER,
    SYSTEM_AI
}

data class ChatMessage(
    val id: String,
    val bookingId: String,
    val sender: MessageSender,
    val senderName: String,
    val text: String,
    val timestampLabel: String,
    val attachedQuote: DigitalQuote? = null,
    val attachedDiagnosisTitle: String? = null
)

data class AiProfileAuditResult(
    val overallScore: Int,
    val bioClarityScore: Int,
    val pricingCompetitivenessScore: Int,
    val kycReadinessScore: Int,
    val fraudRiskFlag: Boolean,
    val actionableSuggestions: List<String>,
    val improvedBioSuggestion: String
)
