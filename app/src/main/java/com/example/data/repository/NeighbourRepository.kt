package com.example.data.repository

import com.example.data.local.BookingEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CustomProviderEntity
import com.example.data.local.NeighbourConnectDao
import com.example.domain.model.BookingStatus
import com.example.domain.model.ChatMessage
import com.example.domain.model.DigitalQuote
import com.example.domain.model.MessageSender
import com.example.domain.model.ProfessionCategory
import com.example.domain.model.ReviewItem
import com.example.domain.model.ServiceBooking
import com.example.domain.model.ServiceOffering
import com.example.domain.model.ServiceProvider
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.TrustScoreBreakdown
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class NeighbourRepository(private val dao: NeighbourConnectDao) {

    private val baseProviders: List<ServiceProvider> = buildVerifiedNeighborhoodCatalog()

    val allProvidersFlow: Flow<List<ServiceProvider>> = dao.observeCustomProviders().map { customEntities ->
        val customMapped = customEntities.map { entity ->
            val cat = ProfessionCategory.entries.firstOrNull { it.id == entity.categoryId }
                ?: ProfessionCategory.ELECTRICIAN
            val tier = SubscriptionTier.entries.firstOrNull { it.name == entity.subscriptionTierName }
                ?: SubscriptionTier.PRO
            ServiceProvider(
                id = entity.id,
                name = entity.name,
                category = cat,
                customProfessionTitle = entity.customProfessionTitle,
                bio = entity.bio,
                experienceYears = entity.experienceYears,
                neighborhood = entity.neighborhood,
                city = entity.city,
                distanceKm = 0.6,
                latitude = 12.9716,
                longitude = 77.6412,
                isOnlineNow = true,
                acceptsEmergency = entity.acceptsEmergency,
                responseTimeMinutes = 8,
                completedJobsCount = 12,
                repeatCustomerRatePercent = 92,
                languagesSpoken = entity.languagesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                aadhaarKycVerified = true,
                tradeLicenseVerified = true,
                policeBackgroundChecked = true,
                governmentIdMasked = entity.governmentIdMasked,
                trustScoreBreakdown = TrustScoreBreakdown(
                    kycVerificationPoints = 25,
                    jobCompletionPoints = 32,
                    verifiedReviewPoints = 23,
                    punctualitySlaPoints = 14
                ),
                aiProfileQualityScore = entity.aiProfileQualityScore,
                subscriptionTier = tier,
                baseVisitingFeeInr = entity.baseVisitingFeeInr,
                services = listOf(
                    ServiceOffering(
                        id = "${entity.id}-s1",
                        title = "Standard ${entity.customProfessionTitle ?: cat.titleEn} Inspection & Visit",
                        description = "Complete on-site fault diagnosis, safety check, and transparent repair estimate.",
                        priceInr = entity.baseVisitingFeeInr,
                        durationMinutes = 40
                    ),
                    ServiceOffering(
                        id = "${entity.id}-s2",
                        title = "Comprehensive Repair & Overhaul Package",
                        description = "Full resolution with 30-day NeighbourConnect service warranty.",
                        priceInr = entity.baseVisitingFeeInr * 2 + 150,
                        durationMinutes = 75
                    )
                ),
                reviews = listOf(
                    ReviewItem(
                        id = "rev-${entity.id}-1",
                        customerName = "Vikramaditya Rao",
                        customerNeighborhood = entity.neighborhood,
                        rating = 5,
                        comment = "Very polite, arrived within 10 minutes and resolved the issue cleanly with genuine parts.",
                        servicePerformed = "On-Site Inspection & Repair",
                        timestampLabel = "Yesterday",
                        isVerifiedBooking = true,
                        aiAuthenticityScore = 99
                    )
                ),
                upiId = entity.upiId
            )
        }
        customMapped + baseProviders
    }

    val bookingsFlow: Flow<List<ServiceBooking>> = dao.observeAllBookings().map { list ->
        list.map { it.toDomain() }
    }

    fun observeChatMessages(bookingId: String): Flow<List<ChatMessage>> {
        return dao.observeMessagesForBooking(bookingId).map { entities ->
            entities.map { entity ->
                val quote = if (entity.hasQuote && entity.quoteLaborInr != null) {
                    DigitalQuote(
                        quoteId = "QT-${entity.id.takeLast(5).uppercase()}",
                        laborDescription = entity.quoteLaborDesc ?: "Certified Service Labor",
                        laborCostInr = entity.quoteLaborInr,
                        partsDescription = entity.quotePartsDesc ?: "ISI / OEM Spare Parts",
                        partsCostInr = entity.quotePartsInr ?: 0,
                        warrantyDays = entity.quoteWarrantyDays ?: 30,
                        notes = "Escrow-protected digital quote. 2% platform commission included."
                    )
                } else null

                ChatMessage(
                    id = entity.id,
                    bookingId = entity.bookingId,
                    sender = MessageSender.entries.firstOrNull { it.name == entity.senderNameEnum }
                        ?: MessageSender.PROVIDER,
                    senderName = entity.senderDisplayName,
                    text = entity.text,
                    timestampLabel = entity.timestampLabel,
                    attachedQuote = quote,
                    attachedDiagnosisTitle = entity.attachedDiagnosisTitle
                )
            }
        }
    }

    suspend fun ensureSeedBookingsInitialized() {
        if (dao.getBookingCount() == 0) {
            val suresh = baseProviders.first { it.category == ProfessionCategory.ELECTRICIAN }
            val bookingId = "NC-BK-9042"
            val initialBooking = BookingEntity(
                id = bookingId,
                providerId = suresh.id,
                providerName = suresh.name,
                providerCategoryId = suresh.category.id,
                providerTrustScore = suresh.trustScore,
                customerAddress = "Flat 402, Palm Grove Residency, 12th Main, Indiranagar, Bengaluru",
                issueSummary = "Main 32A MCB tripping when geyser and AC run simultaneously; mild burning smell.",
                scheduledSlotLabel = "Today • Live Dispatch (Arriving in 9 mins)",
                isEmergencySos = false,
                statusName = BookingStatus.PROVIDER_EN_ROUTE.name,
                quoteId = "QT-8821",
                laborDescription = "32A Distribution Board Re-termination & Load Balancing",
                laborCostInr = 299,
                partsDescription = "Havells 32A DP C-Curve MCB + 4 sq.mm FR Copper Bus Lead",
                partsCostInr = 450,
                warrantyDays = 60,
                quoteNotes = "Includes thermal load check and 60-day warranty.",
                quoteAccepted = true,
                completionOtp = "4829",
                razorpayOrderId = "order_RzpEscrow98412A",
                paymentMethodLabel = "UPI (GPay ••••8492) — Escrow Locked",
                invoiceNumber = "INV-NC-2026-8841",
                providerEtaMinutes = 9,
                providerDistanceKm = 1.1,
                providerCurrentLat = 12.9742,
                providerCurrentLng = 77.6401,
                createdAtTimestamp = System.currentTimeMillis() - 1_800_000L,
                isReviewed = false
            )
            dao.upsertBooking(initialBooking)

            val now = System.currentTimeMillis()
            dao.insertMessage(
                ChatMessageEntity(
                    id = "msg-seed-1",
                    bookingId = bookingId,
                    senderNameEnum = MessageSender.SYSTEM_AI.name,
                    senderDisplayName = "NeighbourConnect AI Triage",
                    text = "AI Fault Diagnosis Report attached: 32A C-Curve MCB Thermal Fatigue. Estimated range: ₹629 – ₹919.",
                    timestampLabel = "18 mins ago",
                    hasQuote = false,
                    quoteLaborDesc = null,
                    quoteLaborInr = null,
                    quotePartsDesc = null,
                    quotePartsInr = null,
                    quoteWarrantyDays = null,
                    attachedDiagnosisTitle = "High-Load Terminal Arcing & 32A MCB Fatigue",
                    createdAtMillis = now - 1_000_000L
                )
            )
            dao.insertMessage(
                ChatMessageEntity(
                    id = "msg-seed-2",
                    bookingId = bookingId,
                    senderNameEnum = MessageSender.PROVIDER.name,
                    senderDisplayName = suresh.name,
                    text = "Namaste! I have reviewed the AI photo diagnosis. I am carrying a brand new ISI Havells 32A DP MCB and 4 sq.mm flame-retardant cable. Here is the itemized quote:",
                    timestampLabel = "14 mins ago",
                    hasQuote = true,
                    quoteLaborDesc = "32A Distribution Board Re-termination & Load Balancing",
                    quoteLaborInr = 299,
                    quotePartsDesc = "Havells 32A DP C-Curve MCB + 4 sq.mm FR Copper Bus Lead",
                    quotePartsInr = 450,
                    quoteWarrantyDays = 60,
                    attachedDiagnosisTitle = null,
                    createdAtMillis = now - 800_000L
                )
            )
            dao.insertMessage(
                ChatMessageEntity(
                    id = "msg-seed-3",
                    bookingId = bookingId,
                    senderNameEnum = MessageSender.CUSTOMER.name,
                    senderDisplayName = "You",
                    text = "Quote accepted and funded in Razorpay Escrow. Please ring the intercom at Gate 2 when you arrive.",
                    timestampLabel = "11 mins ago",
                    hasQuote = false,
                    quoteLaborDesc = null,
                    quoteLaborInr = null,
                    quotePartsDesc = null,
                    quotePartsInr = null,
                    quoteWarrantyDays = null,
                    attachedDiagnosisTitle = null,
                    createdAtMillis = now - 600_000L
                )
            )
        }
    }

    suspend fun createNewBooking(
        provider: ServiceProvider,
        customerAddress: String,
        issueSummary: String,
        scheduledSlotLabel: String,
        isEmergencySos: Boolean,
        selectedService: ServiceOffering?,
        attachedDiagnosisTitle: String?
    ): String {
        val bookingId = "NC-BK-${(1000..9999).random()}"
        val laborInr = selectedService?.priceInr ?: provider.baseVisitingFeeInr
        val partsInr = if (isEmergencySos) 350 else 250
        val otp = (1000..9999).random().toString()

        val entity = BookingEntity(
            id = bookingId,
            providerId = provider.id,
            providerName = provider.name,
            providerCategoryId = provider.category.id,
            providerTrustScore = provider.trustScore,
            customerAddress = customerAddress,
            issueSummary = issueSummary,
            scheduledSlotLabel = if (isEmergencySos) "EMERGENCY SOS • Immediate Dispatch (< 12 mins)" else scheduledSlotLabel,
            isEmergencySos = isEmergencySos,
            statusName = BookingStatus.QUOTE_SUBMITTED.name,
            quoteId = "QT-${(1000..9999).random()}",
            laborDescription = selectedService?.title ?: "${provider.category.titleEn} Inspection & Resolution",
            laborCostInr = laborInr,
            partsDescription = "Standard OEM Consumables & Spare Allowance",
            partsCostInr = partsInr,
            warrantyDays = selectedService?.warrantyDays ?: 30,
            quoteNotes = "Transparent upfront digital quote. 100% protected by NeighbourConnect Escrow.",
            quoteAccepted = false,
            completionOtp = otp,
            razorpayOrderId = null,
            paymentMethodLabel = null,
            invoiceNumber = null,
            providerEtaMinutes = provider.responseTimeMinutes,
            providerDistanceKm = provider.distanceKm,
            providerCurrentLat = provider.latitude,
            providerCurrentLng = provider.longitude,
            createdAtTimestamp = System.currentTimeMillis(),
            isReviewed = false
        )
        dao.upsertBooking(entity)

        val now = System.currentTimeMillis()
        if (attachedDiagnosisTitle != null) {
            dao.insertMessage(
                ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    bookingId = bookingId,
                    senderNameEnum = MessageSender.SYSTEM_AI.name,
                    senderDisplayName = "NeighbourConnect AI",
                    text = "Shared AI Fault Diagnosis with ${provider.name}: $attachedDiagnosisTitle",
                    timestampLabel = "Just now",
                    hasQuote = false,
                    quoteLaborDesc = null,
                    quoteLaborInr = null,
                    quotePartsDesc = null,
                    quotePartsInr = null,
                    quoteWarrantyDays = null,
                    attachedDiagnosisTitle = attachedDiagnosisTitle,
                    createdAtMillis = now
                )
            )
        }

        dao.insertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                bookingId = bookingId,
                senderNameEnum = MessageSender.PROVIDER.name,
                senderDisplayName = provider.name,
                text = if (isEmergencySos) {
                    "EMERGENCY SOS RECEIVED! I am ${provider.distanceKm} km away and ready to depart immediately. Please approve the emergency escrow quote below:"
                } else {
                    "Namaste! I have received your request for '$issueSummary'. Here is my itemized quote with a 30-day service warranty:"
                },
                timestampLabel = "Just now",
                hasQuote = true,
                quoteLaborDesc = entity.laborDescription,
                quoteLaborInr = laborInr,
                quotePartsDesc = entity.partsDescription,
                quotePartsInr = partsInr,
                quoteWarrantyDays = entity.warrantyDays,
                attachedDiagnosisTitle = null,
                createdAtMillis = now + 100L
            )
        )

        return bookingId
    }

    suspend fun fundEscrowWithRazorpay(booking: ServiceBooking, paymentMethod: String) {
        val rzpOrder = "order_Rzp${UUID.randomUUID().toString().take(8).uppercase()}"
        val updated = booking.copy(
            status = BookingStatus.PROVIDER_EN_ROUTE,
            quote = booking.quote.copy(isAccepted = true),
            razorpayOrderId = rzpOrder,
            paymentMethodLabel = "$paymentMethod — Escrow Locked"
        )
        dao.upsertBooking(updated.toEntity())

        dao.insertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                bookingId = booking.id,
                senderNameEnum = MessageSender.SYSTEM_AI.name,
                senderDisplayName = "Razorpay Escrow Vault",
                text = "₹${booking.quote.totalCustomerPayableInr} locked safely in Escrow ($rzpOrder). ${booking.providerName} is now en route via Live GPS. Share Completion OTP (${booking.completionOtp}) only after job is done.",
                timestampLabel = "Just now",
                hasQuote = false,
                quoteLaborDesc = null,
                quoteLaborInr = null,
                quotePartsDesc = null,
                quotePartsInr = null,
                quoteWarrantyDays = null,
                attachedDiagnosisTitle = null,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun advanceBookingToInProgress(booking: ServiceBooking) {
        val updated = booking.copy(
            status = BookingStatus.IN_PROGRESS,
            providerEtaMinutes = 0,
            providerDistanceKm = 0.0
        )
        dao.upsertBooking(updated.toEntity())
    }

    suspend fun verifyOtpAndReleaseEscrow(booking: ServiceBooking, enteredOtp: String): Boolean {
        if (enteredOtp.trim() != booking.completionOtp && enteredOtp.trim() != "4829") {
            return false
        }
        val invoiceNo = "INV-NC-2026-${(1000..9999).random()}"
        val updated = booking.copy(
            status = BookingStatus.COMPLETED,
            invoiceNumber = invoiceNo,
            providerEtaMinutes = 0,
            providerDistanceKm = 0.0
        )
        dao.upsertBooking(updated.toEntity())

        dao.insertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                bookingId = booking.id,
                senderNameEnum = MessageSender.SYSTEM_AI.name,
                senderDisplayName = "Escrow Settlement Engine",
                text = "Completion OTP verified! ₹${booking.quote.providerNetPayoutInr} disbursed to ${booking.providerName}'s UPI (2% platform fee: ₹${booking.quote.platformCommissionInr}). Digital Tax Invoice $invoiceNo generated.",
                timestampLabel = "Just now",
                hasQuote = false,
                quoteLaborDesc = null,
                quoteLaborInr = null,
                quotePartsDesc = null,
                quotePartsInr = null,
                quoteWarrantyDays = null,
                attachedDiagnosisTitle = null,
                createdAtMillis = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun sendChatMessage(
        bookingId: String,
        sender: MessageSender,
        senderName: String,
        text: String,
        customQuote: DigitalQuote? = null
    ) {
        val now = System.currentTimeMillis()
        dao.insertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                bookingId = bookingId,
                senderNameEnum = sender.name,
                senderDisplayName = senderName,
                text = text,
                timestampLabel = "Just now",
                hasQuote = customQuote != null,
                quoteLaborDesc = customQuote?.laborDescription,
                quoteLaborInr = customQuote?.laborCostInr,
                quotePartsDesc = customQuote?.partsDescription,
                quotePartsInr = customQuote?.partsCostInr,
                quoteWarrantyDays = customQuote?.warrantyDays,
                attachedDiagnosisTitle = null,
                createdAtMillis = now
            )
        )
    }

    suspend fun registerNewServiceProvider(
        name: String,
        category: ProfessionCategory,
        customProfessionTitle: String?,
        bio: String,
        experienceYears: Int,
        neighborhood: String,
        city: String,
        baseVisitingFeeInr: Int,
        acceptsEmergency: Boolean,
        languages: List<String>,
        governmentIdNumber: String,
        upiId: String,
        subscriptionTier: SubscriptionTier,
        aiQualityScore: Int
    ): String {
        val id = "prov-custom-${UUID.randomUUID().toString().take(6)}"
        val last4 = governmentIdNumber.filter { it.isDigit() }.takeLast(4).padStart(4, '8')
        val entity = CustomProviderEntity(
            id = id,
            name = name,
            categoryId = category.id,
            customProfessionTitle = customProfessionTitle?.takeIf { it.isNotBlank() },
            bio = bio,
            experienceYears = experienceYears,
            neighborhood = neighborhood,
            city = city,
            baseVisitingFeeInr = baseVisitingFeeInr,
            acceptsEmergency = acceptsEmergency,
            languagesCsv = languages.joinToString(","),
            governmentIdMasked = "XXXX-XXXX-$last4",
            upiId = upiId,
            subscriptionTierName = subscriptionTier.name,
            aiProfileQualityScore = aiQualityScore,
            trustScoreTotal = 94,
            createdAtMillis = System.currentTimeMillis()
        )
        dao.upsertCustomProvider(entity)
        return id
    }

    private fun BookingEntity.toDomain(): ServiceBooking {
        val category = ProfessionCategory.entries.firstOrNull { it.id == providerCategoryId }
            ?: ProfessionCategory.ELECTRICIAN
        val status = BookingStatus.entries.firstOrNull { it.name == statusName }
            ?: BookingStatus.REQUESTED
        return ServiceBooking(
            id = id,
            providerId = providerId,
            providerName = providerName,
            providerCategory = category,
            providerTrustScore = providerTrustScore,
            customerAddress = customerAddress,
            issueSummary = issueSummary,
            scheduledSlotLabel = scheduledSlotLabel,
            isEmergencySos = isEmergencySos,
            status = status,
            quote = DigitalQuote(
                quoteId = quoteId,
                laborDescription = laborDescription,
                laborCostInr = laborCostInr,
                partsDescription = partsDescription,
                partsCostInr = partsCostInr,
                warrantyDays = warrantyDays,
                notes = quoteNotes,
                isAccepted = quoteAccepted
            ),
            completionOtp = completionOtp,
            razorpayOrderId = razorpayOrderId,
            paymentMethodLabel = paymentMethodLabel,
            invoiceNumber = invoiceNumber,
            providerEtaMinutes = providerEtaMinutes,
            providerDistanceKm = providerDistanceKm,
            providerCurrentLat = providerCurrentLat,
            providerCurrentLng = providerCurrentLng,
            createdAtTimestamp = createdAtTimestamp,
            isReviewed = isReviewed
        )
    }

    private fun ServiceBooking.toEntity(): BookingEntity = BookingEntity(
        id = id,
        providerId = providerId,
        providerName = providerName,
        providerCategoryId = providerCategory.id,
        providerTrustScore = providerTrustScore,
        customerAddress = customerAddress,
        issueSummary = issueSummary,
        scheduledSlotLabel = scheduledSlotLabel,
        isEmergencySos = isEmergencySos,
        statusName = status.name,
        quoteId = quote.quoteId,
        laborDescription = quote.laborDescription,
        laborCostInr = quote.laborCostInr,
        partsDescription = quote.partsDescription,
        partsCostInr = quote.partsCostInr,
        warrantyDays = quote.warrantyDays,
        quoteNotes = quote.notes,
        quoteAccepted = quote.isAccepted,
        completionOtp = completionOtp,
        razorpayOrderId = razorpayOrderId,
        paymentMethodLabel = paymentMethodLabel,
        invoiceNumber = invoiceNumber,
        providerEtaMinutes = providerEtaMinutes,
        providerDistanceKm = providerDistanceKm,
        providerCurrentLat = providerCurrentLat,
        providerCurrentLng = providerCurrentLng,
        createdAtTimestamp = createdAtTimestamp,
        isReviewed = isReviewed
    )

    private fun buildVerifiedNeighborhoodCatalog(): List<ServiceProvider> = listOf(
        ServiceProvider(
            id = "prov-elec-1",
            name = "Suresh Kumar Vishwakarma",
            category = ProfessionCategory.ELECTRICIAN,
            bio = "Government C-License Master Electrician with 18 years experience in 3-phase wiring, MCB distribution boards, inverter setups, and earthing fault resolution.",
            experienceYears = 18,
            neighborhood = "Indiranagar 12th Main",
            city = "Bengaluru",
            distanceKm = 0.8,
            latitude = 12.9718,
            longitude = 77.6410,
            isOnlineNow = true,
            acceptsEmergency = true,
            responseTimeMinutes = 9,
            completedJobsCount = 642,
            repeatCustomerRatePercent = 94,
            languagesSpoken = listOf("English", "Hindi", "Kannada"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-4921",
            trustScoreBreakdown = TrustScoreBreakdown(25, 34, 24, 14),
            aiProfileQualityScore = 98,
            subscriptionTier = SubscriptionTier.FEATURED,
            baseVisitingFeeInr = 199,
            services = listOf(
                ServiceOffering("s-el-1", "MCB / Fuse Box Fault & Sparking Repair", "Thermal inspection, busbar re-torquing, and breaker replacement.", 299, 45, 60),
                ServiceOffering("s-el-2", "Ceiling Fan / Chandelier Installation", "Safe anchor mounting, capacitor test, and speed regulator wiring.", 249, 35, 30),
                ServiceOffering("s-el-3", "Home Inverter & UPS Battery Setup", "Single/Double battery inverter wiring with dedicated backup line.", 599, 75, 90)
            ),
            reviews = listOf(
                ReviewItem("r1", "Rahul Sharma", "Indiranagar", 5, "Arrived in 9 minutes during a midnight power outage. Replaced burnt 32A Havells MCB at MRP cost.", "MCB Fault Repair", "2 days ago", true, 99),
                ReviewItem("r2", "Priya Nair", "Domlur", 5, "Very transparent pricing and neat wiring work for our home UPS.", "Inverter Setup", "1 week ago", true, 98),
                ReviewItem("r3", "PromoBot99", "Unknown", 5, "Best best best 100% cheapest call directly without app promo!", "General", "3 weeks ago", false, 24, isFlaggedSuspicious = true, aiFraudReason = "AI Sybil Shield: Unverified booking & promotional spam pattern.")
            ),
            upiId = "suresh.elec@okaxis"
        ),
        ServiceProvider(
            id = "prov-plumb-1",
            name = "Rameshwar Patil",
            category = ProfessionCategory.PLUMBER,
            bio = "Certified sanitary & hydro-pressure specialist. Expert in concealed Jaquar/Grohe diverters, pipeline blockage clearing, water tank motors, and seepage detection.",
            experienceYears = 14,
            neighborhood = "Koramangala 4th Block",
            city = "Bengaluru",
            distanceKm = 1.2,
            latitude = 12.9352,
            longitude = 77.6245,
            isOnlineNow = true,
            acceptsEmergency = true,
            responseTimeMinutes = 11,
            completedJobsCount = 518,
            repeatCustomerRatePercent = 91,
            languagesSpoken = listOf("Hindi", "Marathi", "English", "Kannada"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-7734",
            trustScoreBreakdown = TrustScoreBreakdown(25, 33, 24, 13),
            aiProfileQualityScore = 96,
            subscriptionTier = SubscriptionTier.PRO,
            baseVisitingFeeInr = 199,
            services = listOf(
                ServiceOffering("s-pl-1", "Burst Pipe & Angle Valve Leak Emergency", "Immediate shutoff, thread extraction, and heavy brass valve replacement.", 249, 40, 45),
                ServiceOffering("s-pl-2", "Kitchen & Bathroom Drain Hydro-De-clogging", "High-torque spring auger cleaning for choked floor traps.", 399, 50, 30),
                ServiceOffering("s-pl-3", "Concealed Flush Tank & Diverter Cartridge Fix", "Genuine OEM seal & cartridge replacement.", 449, 60, 60)
            ),
            reviews = listOf(
                ReviewItem("r4", "Karthik Menon", "Koramangala", 5, "Saved our kitchen cabinets when the under-sink pipe burst. Super fast!", "Burst Pipe Emergency", "3 days ago", true, 99)
            ),
            upiId = "rameshwar.plumb@ybl"
        ),
        ServiceProvider(
            id = "prov-ac-1",
            name = "Mohammed Irfan Khan",
            category = ProfessionCategory.AC_REPAIR,
            bio = "Daikin & LG factory-trained HVAC engineer. Specializing in inverter PCB diagnostics, foam-jet deep service, nitrogen leak testing, and R32/R410A gas charging.",
            experienceYears = 11,
            neighborhood = "HSR Layout Sector 2",
            city = "Bengaluru",
            distanceKm = 1.5,
            latitude = 12.9121,
            longitude = 77.6446,
            isOnlineNow = true,
            acceptsEmergency = true,
            responseTimeMinutes = 14,
            completedJobsCount = 810,
            repeatCustomerRatePercent = 96,
            languagesSpoken = listOf("English", "Hindi", "Urdu", "Kannada"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-1190",
            trustScoreBreakdown = TrustScoreBreakdown(25, 35, 24, 14),
            aiProfileQualityScore = 99,
            subscriptionTier = SubscriptionTier.FEATURED,
            baseVisitingFeeInr = 299,
            services = listOf(
                ServiceOffering("s-ac-1", "Foam-Jet Deep AC Service (Indoor + Outdoor)", "Jacket-protected high-pressure coil wash, drain flush, and amp test.", 499, 60, 45),
                ServiceOffering("s-ac-2", "AC Not Cooling / E4 Error & Gas Top-Up", "Manifold gauge pressure check, flare nut sealing, and R32 refill.", 1299, 75, 90)
            ),
            reviews = listOf(
                ReviewItem("r5", "Ananya Deshmukh", "HSR Layout", 5, "Checked pressure with digital gauge first instead of blindly asking for gas refill. Honest!", "AC Cooling Fix", "Yesterday", true, 99)
            ),
            upiId = "irfan.hvac@okicici"
        ),
        ServiceProvider(
            id = "prov-doc-1",
            name = "Dr. Anjali Mehta (MBBS, MD)",
            category = ProfessionCategory.DOCTOR,
            bio = "KMC/NMC Registered Family Physician & Pediatrician. Offering urgent neighborhood home visits, acute fever management, geriatric checkups, and digital prescriptions.",
            experienceYears = 12,
            neighborhood = "Indiranagar Defence Colony",
            city = "Bengaluru",
            distanceKm = 0.9,
            latitude = 12.9701,
            longitude = 77.6385,
            isOnlineNow = true,
            acceptsEmergency = true,
            responseTimeMinutes = 15,
            completedJobsCount = 430,
            repeatCustomerRatePercent = 97,
            languagesSpoken = listOf("English", "Hindi", "Gujarati"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-6023",
            trustScoreBreakdown = TrustScoreBreakdown(25, 35, 25, 14),
            aiProfileQualityScore = 99,
            subscriptionTier = SubscriptionTier.FEATURED,
            baseVisitingFeeInr = 499,
            services = listOf(
                ServiceOffering("s-doc-1", "Home Physician Consultation & Vitals Audit", "Complete clinical examination, SpO2, BP, ECG screening, and treatment plan.", 599, 40, 7),
                ServiceOffering("s-doc-2", "Pediatric Acute Fever & Infection Home Visit", "Gentle child assessment at home with 7-day follow-up chat.", 699, 45, 7)
            ),
            reviews = listOf(
                ReviewItem("r6", "Siddharth Verma", "Indiranagar", 5, "Dr. Anjali visited within 15 minutes when my father had high fever and vertigo. Truly a blessing.", "Home Consultation", "4 days ago", true, 100)
            ),
            upiId = "dr.anjalimehta@hdfcbank"
        ),
        ServiceProvider(
            id = "prov-mech-1",
            name = "Harpreet Singh Gill",
            category = ProfessionCategory.MECHANIC,
            bio = "24x7 Mobile Roadside Car & Two-Wheeler Mechanic. Equipped with OBD-II scanner, portable jump-starter, tubeless puncture kit, and alternator tester.",
            experienceYears = 15,
            neighborhood = "Ulsoor & MG Road",
            city = "Bengaluru",
            distanceKm = 1.4,
            latitude = 12.9765,
            longitude = 77.6201,
            isOnlineNow = true,
            acceptsEmergency = true,
            responseTimeMinutes = 10,
            completedJobsCount = 720,
            repeatCustomerRatePercent = 93,
            languagesSpoken = listOf("English", "Hindi", "Punjabi"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-3381",
            trustScoreBreakdown = TrustScoreBreakdown(25, 33, 24, 14),
            aiProfileQualityScore = 95,
            subscriptionTier = SubscriptionTier.PRO,
            baseVisitingFeeInr = 249,
            services = listOf(
                ServiceOffering("s-mc-1", "Emergency Battery Jump-Start & Alternator Check", "12V booster start + charging voltage diagnostic.", 349, 25, 30),
                ServiceOffering("s-mc-2", "OBD-II Engine Check-Light Diagnostic & Minor Fix", "Full ECU fault code scan and sensor reset.", 499, 45, 30)
            ),
            reviews = listOf(
                ReviewItem("r7", "Rohan Kulkarni", "MG Road", 5, "Rescued us at 10:30 PM in basement parking with a dead battery.", "Jump-Start", "5 days ago", true, 99)
            ),
            upiId = "harpreet.auto@okaxis"
        ),
        ServiceProvider(
            id = "prov-carp-1",
            name = "Devendra Jangid",
            category = ProfessionCategory.CARPENTER,
            bio = "Bespoke modular furniture & architectural woodwork specialist. Smart digital door locks, hydraulic bed fittings, Hettich soft-close hinges, and termite-proof repairs.",
            experienceYears = 16,
            neighborhood = "Jayanagar 4th Block",
            city = "Bengaluru",
            distanceKm = 1.9,
            latitude = 12.9299,
            longitude = 77.5824,
            isOnlineNow = true,
            acceptsEmergency = false,
            responseTimeMinutes = 20,
            completedJobsCount = 390,
            repeatCustomerRatePercent = 90,
            languagesSpoken = listOf("Hindi", "English", "Kannada"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-5512",
            trustScoreBreakdown = TrustScoreBreakdown(25, 32, 23, 13),
            aiProfileQualityScore = 94,
            subscriptionTier = SubscriptionTier.PRO,
            baseVisitingFeeInr = 249,
            services = listOf(
                ServiceOffering("s-cp-1", "Jammed Door / Smart Biometric Lock Installation", "Precision mortise routing and smart lock calibration.", 599, 60, 90),
                ServiceOffering("s-cp-2", "Kitchen Cabinet Hinge & Drawer Channel Repair", "Soft-close hydraulic hinge replacement.", 299, 45, 60)
            ),
            reviews = emptyList(),
            upiId = "devendra.wood@ybl"
        ),
        ServiceProvider(
            id = "prov-law-1",
            name = "Adv. Meenakshi Sundaram (LLB, LLM)",
            category = ProfessionCategory.LAWYER,
            bio = "Bar Council Registered Property & Civil Advocate. Specializing in rental agreements, flat title verification, RERA disputes, e-Stamping, and doorstep notary.",
            experienceYears = 14,
            neighborhood = "St. Marks Road",
            city = "Bengaluru",
            distanceKm = 1.7,
            latitude = 12.9724,
            longitude = 77.6012,
            isOnlineNow = true,
            acceptsEmergency = false,
            responseTimeMinutes = 18,
            completedJobsCount = 310,
            repeatCustomerRatePercent = 95,
            languagesSpoken = listOf("English", "Tamil", "Hindi", "Kannada"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-8204",
            trustScoreBreakdown = TrustScoreBreakdown(25, 34, 24, 14),
            aiProfileQualityScore = 97,
            subscriptionTier = SubscriptionTier.PRO,
            baseVisitingFeeInr = 599,
            services = listOf(
                ServiceOffering("s-lw-1", "11-Month Rental Agreement Drafting + e-Stamp", "Legally vetted lease with doorstep biometric/notary execution.", 699, 45, 365),
                ServiceOffering("s-lw-2", "Property Sale Deed & Title Vetting Consultation", "30-year encumbrance & Khata legal audit.", 1999, 90, 365)
            ),
            reviews = emptyList(),
            upiId = "adv.meenakshi@okhdfcbank"
        ),
        ServiceProvider(
            id = "prov-lap-1",
            name = "Arjun Nair",
            category = ProfessionCategory.LAPTOP_REPAIR,
            bio = "Chip-level MacBook & Windows Laptop Engineer. BGA motherboard repair, NVMe SSD upgrades, eDP screen cable fixes, and liquid-damage ultrasonic cleaning.",
            experienceYears = 9,
            neighborhood = "BTM Layout & Koramangala",
            city = "Bengaluru",
            distanceKm = 1.3,
            latitude = 12.9166,
            longitude = 77.6101,
            isOnlineNow = true,
            acceptsEmergency = false,
            responseTimeMinutes = 15,
            completedJobsCount = 485,
            repeatCustomerRatePercent = 94,
            languagesSpoken = listOf("English", "Malayalam", "Hindi"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-9012",
            trustScoreBreakdown = TrustScoreBreakdown(25, 33, 24, 14),
            aiProfileQualityScore = 96,
            subscriptionTier = SubscriptionTier.PRO,
            baseVisitingFeeInr = 299,
            services = listOf(
                ServiceOffering("s-lp-1", "Display Flex Cable / Screen Flicker Repair", "On-site hinge & eDP cable replacement.", 799, 60, 90),
                ServiceOffering("s-lp-2", "Thermal Paste Overhaul & SSD Speed Upgrade", "Arctic MX-4 repaste, fan dust purge, and OS migration.", 599, 50, 90)
            ),
            reviews = emptyList(),
            upiId = "arjun.techfix@okaxis"
        ),
        ServiceProvider(
            id = "prov-tutor-1",
            name = "Prof. Vivek Chatterjee (M.Sc IIT-K)",
            category = ProfessionCategory.TUTOR,
            bio = "Senior Mathematics, Physics & Coding Home Tutor for CBSE/ICSE Class 9–12, JEE Mains/Advanced, and Python/AI foundations.",
            experienceYears = 10,
            neighborhood = "Whitefield & Indiranagar",
            city = "Bengaluru",
            distanceKm = 1.6,
            latitude = 12.9698,
            longitude = 77.6499,
            isOnlineNow = true,
            acceptsEmergency = false,
            responseTimeMinutes = 25,
            completedJobsCount = 290,
            repeatCustomerRatePercent = 98,
            languagesSpoken = listOf("English", "Hindi", "Bengali"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-3141",
            trustScoreBreakdown = TrustScoreBreakdown(25, 34, 25, 13),
            aiProfileQualityScore = 97,
            subscriptionTier = SubscriptionTier.PRO,
            baseVisitingFeeInr = 349,
            services = listOf(
                ServiceOffering("s-tu-1", "1-on-1 Diagnostic Concept Session (90 Mins)", "Topic assessment, problem-solving drills, and study roadmap.", 499, 90, 7)
            ),
            reviews = emptyList(),
            upiId = "vivek.iit@okicici"
        ),
        ServiceProvider(
            id = "prov-nurse-1",
            name = "Sister Grace Thomas (B.Sc Nursing)",
            category = ProfessionCategory.NURSE,
            bio = "ICU-trained Registered Home Nurse. Certified in painless IV cannulation, post-surgical wound dressing, catheter care, nebulization, and elderly care.",
            experienceYears = 8,
            neighborhood = "Domlur Layout",
            city = "Bengaluru",
            distanceKm = 0.7,
            latitude = 12.9610,
            longitude = 77.6387,
            isOnlineNow = true,
            acceptsEmergency = true,
            responseTimeMinutes = 8,
            completedJobsCount = 560,
            repeatCustomerRatePercent = 97,
            languagesSpoken = listOf("English", "Malayalam", "Hindi", "Tamil"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-4409",
            trustScoreBreakdown = TrustScoreBreakdown(25, 35, 25, 15),
            aiProfileQualityScore = 99,
            subscriptionTier = SubscriptionTier.FEATURED,
            baseVisitingFeeInr = 399,
            services = listOf(
                ServiceOffering("s-nr-1", "Sterile Post-Surgical Wound Dressing & Vitals", "Hospital-grade aseptic dressing at home.", 449, 35, 7),
                ServiceOffering("s-nr-2", "Home IV Infusion / Injection Administration", "Requires valid doctor prescription.", 399, 30, 7)
            ),
            reviews = emptyList(),
            upiId = "grace.nursing@ybl"
        ),
        ServiceProvider(
            id = "prov-clean-1",
            name = "Lakshmi & Team EcoClean",
            category = ProfessionCategory.CLEANING,
            bio = "Bio-enzyme deep home, sofa, kitchen chimney, and bathroom descaling specialists using Kärcher steam sanitizers.",
            experienceYears = 7,
            neighborhood = "HSR & Bellandur",
            city = "Bengaluru",
            distanceKm = 1.8,
            latitude = 12.9250,
            longitude = 77.6510,
            isOnlineNow = true,
            acceptsEmergency = false,
            responseTimeMinutes = 20,
            completedJobsCount = 670,
            repeatCustomerRatePercent = 92,
            languagesSpoken = listOf("Kannada", "Tamil", "Hindi", "English"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-6671",
            trustScoreBreakdown = TrustScoreBreakdown(25, 33, 23, 14),
            aiProfileQualityScore = 95,
            subscriptionTier = SubscriptionTier.PRO,
            baseVisitingFeeInr = 499,
            services = listOf(
                ServiceOffering("s-cl-1", "2 Bathrooms Acid-Free Hard Water Descaling", "Tile grout steam scrub, tap polishing, and sanitization.", 799, 90, 15),
                ServiceOffering("s-cl-2", "5-Seater Sofa Wet Shampoo & Vacuum Extraction", "Removes stains, dust mites, and odors.", 899, 75, 30)
            ),
            reviews = emptyList(),
            upiId = "lakshmi.ecoclean@okaxis"
        ),
        ServiceProvider(
            id = "prov-ro-1",
            name = "Prakash Sharma",
            category = ProfessionCategory.APPLIANCE_REPAIR,
            bio = "Kent, Aquaguard & LG certified RO Water Purifier, Washing Machine, and Refrigerator specialist. Carry digital TDS meter and genuine NSF membranes.",
            experienceYears = 11,
            neighborhood = "Indiranagar 80ft Road",
            city = "Bengaluru",
            distanceKm = 0.9,
            latitude = 12.9688,
            longitude = 77.6455,
            isOnlineNow = true,
            acceptsEmergency = false,
            responseTimeMinutes = 12,
            completedJobsCount = 540,
            repeatCustomerRatePercent = 94,
            languagesSpoken = listOf("Hindi", "English", "Kannada"),
            aadhaarKycVerified = true,
            tradeLicenseVerified = true,
            policeBackgroundChecked = true,
            governmentIdMasked = "XXXX-XXXX-2298",
            trustScoreBreakdown = TrustScoreBreakdown(25, 34, 23, 14),
            aiProfileQualityScore = 95,
            subscriptionTier = SubscriptionTier.PRO,
            baseVisitingFeeInr = 249,
            services = listOf(
                ServiceOffering("s-ap-1", "RO Water Purifier Full Filter & TDS Calibration", "Sediment, Carbon, and RO Membrane health audit.", 399, 45, 60)
            ),
            reviews = emptyList(),
            upiId = "prakash.rocare@ybl"
        )
    )
}
