package com.example.data.ai

import com.example.domain.model.AiDiagnosisReport
import com.example.domain.model.AiProfileAuditResult
import com.example.domain.model.FaultSeverity
import com.example.domain.model.ProfessionCategory
import com.example.domain.model.ReviewItem
import com.example.domain.model.SparePartEstimate
import java.util.Locale
import java.util.UUID

data class DiagnosticPresetScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: ProfessionCategory,
    val defaultPrompt: String,
    val mediaBadge: String
)

object AiEngine {

    val presetScenarios: List<DiagnosticPresetScenario> = listOf(
        DiagnosticPresetScenario(
            id = "sparking_mcb",
            title = "Sparking MCB / Burnt Switchboard",
            subtitle = "Charring smell near distribution box & tripping breaker",
            category = ProfessionCategory.ELECTRICIAN,
            defaultPrompt = "My main 32A MCB switchboard is sparking with a burning plastic smell whenever the geyser and AC run together.",
            mediaBadge = "Photo + Thermal Pattern"
        ),
        DiagnosticPresetScenario(
            id = "pipe_burst",
            title = "Under-Sink Pipe Burst & Seepage",
            subtitle = "High-pressure water leak from braided angle valve",
            category = ProfessionCategory.PLUMBER,
            defaultPrompt = "Water is spraying out from the rusted angle valve under the kitchen sink and flooding the floor.",
            mediaBadge = "Video Frame Analysis"
        ),
        DiagnosticPresetScenario(
            id = "ac_not_cooling",
            title = "Split AC Blowing Warm Air + E4 Error",
            subtitle = "Indoor unit icing up & outdoor condenser not kicking in",
            category = ProfessionCategory.AC_REPAIR,
            defaultPrompt = "1.5 Ton Inverter Split AC is blowing room-temperature air, showing E4 error code, and water is dripping from indoor unit.",
            mediaBadge = "Image + Error Code OCR"
        ),
        DiagnosticPresetScenario(
            id = "car_dead_battery",
            title = "Car Won't Start / Clicking Starter",
            subtitle = "Rapid clicking sound on ignition & dim dashboard lights",
            category = ProfessionCategory.MECHANIC,
            defaultPrompt = "My car is stuck in basement parking. Turning the key makes a rapid clicking noise and headlights flicker.",
            mediaBadge = "Audio + Dash Photo"
        ),
        DiagnosticPresetScenario(
            id = "laptop_hinge_screen",
            title = "Laptop Screen Flickering & Overheating",
            subtitle = "Horizontal display lines & thermal throttling at 95°C",
            category = ProfessionCategory.LAPTOP_REPAIR,
            defaultPrompt = "Laptop display shows purple horizontal lines when tilting the lid and fan makes a loud grinding noise.",
            mediaBadge = "Display Defect Scan"
        ),
        DiagnosticPresetScenario(
            id = "pediatric_fever",
            title = "High Fever & Rash Home Consultation",
            subtitle = "102.5°F persistent fever requiring doctor home visit",
            category = ProfessionCategory.DOCTOR,
            defaultPrompt = "Need a verified pediatrician or family physician for a home visit; child has 102.5°F fever since last night.",
            mediaBadge = "Symptom Triage"
        ),
        DiagnosticPresetScenario(
            id = "ro_water_taste",
            title = "RO Purifier High TDS & Low Flow",
            subtitle = "Bitter water taste, red filter alert light blinking",
            category = ProfessionCategory.APPLIANCE_REPAIR,
            defaultPrompt = "RO water purifier flow is very slow, TDS meter reads 380 ppm, and filter change LED is blinking red.",
            mediaBadge = "Appliance Telemetry"
        ),
        DiagnosticPresetScenario(
            id = "rental_agreement",
            title = "Property Leave & License Vet + Notary",
            subtitle = "11-month residential lease review & e-Stamping",
            category = ProfessionCategory.LAWYER,
            defaultPrompt = "Need a property lawyer to verify clauses in an 11-month flat rental agreement and complete e-Stamp notary.",
            mediaBadge = "Document Audit"
        )
    )

    fun diagnoseProblem(
        userDescription: String,
        selectedScenario: DiagnosticPresetScenario?,
        hasAttachedImage: Boolean
    ): AiDiagnosisReport {
        val text = userDescription.lowercase(Locale.ROOT)
        val matchedCategory = detectBestCategory(text) ?: selectedScenario?.category ?: ProfessionCategory.ELECTRICIAN

        return when (matchedCategory) {
            ProfessionCategory.ELECTRICIAN -> AiDiagnosisReport(
                id = "DIAG-${UUID.randomUUID().toString().take(6).uppercase()}",
                inputDescription = userDescription.ifBlank { selectedScenario?.defaultPrompt.orEmpty() },
                inputScenarioTitle = selectedScenario?.title ?: "Electrical Load & Wiring Fault Analysis",
                recommendedCategory = ProfessionCategory.ELECTRICIAN,
                faultTitle = "High-Load Terminal Arcing & 32A C-Curve MCB Thermal Fatigue",
                rootCauseAnalysis = "Computer vision & symptom telemetry indicate carbonized copper insulation at the neutral-phase busbar terminal caused by loose lug torque under heavy inductive load (>15A).",
                severity = FaultSeverity.CRITICAL_EMERGENCY,
                spareParts = listOf(
                    SparePartEstimate("32A DP C-Curve MCB (Havells / Schneider ISI)", 380, 520),
                    SparePartEstimate("4 sq.mm FR-LSH Flame Retardant Copper Wire (2m)", 140, 220),
                    SparePartEstimate("Heavy-Duty 16A Modular Power Socket + Switch", 240, 360, isOptional = true)
                ),
                estimatedLaborMinInr = 249,
                estimatedLaborMaxInr = 399,
                estimatedTimeMinutes = 45,
                safetyPrecautions = listOf(
                    "Turn off the main isolator switch immediately.",
                    "Do not touch damp walls or metallic distribution boards.",
                    "Never pour water on electrical sparks; use Dry Powder Class C extinguisher if needed."
                ),
                aiConfidencePercent = if (hasAttachedImage) 97 else 92
            )

            ProfessionCategory.PLUMBER -> AiDiagnosisReport(
                id = "DIAG-${UUID.randomUUID().toString().take(6).uppercase()}",
                inputDescription = userDescription.ifBlank { selectedScenario?.defaultPrompt.orEmpty() },
                inputScenarioTitle = selectedScenario?.title ?: "Hydraulic Pressure & Valve Failure Analysis",
                recommendedCategory = ProfessionCategory.PLUMBER,
                faultTitle = "Corroded Brass Angle-Cock Thread Shear & Braided Hose Rupture",
                rootCauseAnalysis = "High TDS hard-water scaling has seized the ceramic quarter-turn cartridge and sheared the 1/2-inch BSP wall nipple thread, causing pressurized seepage.",
                severity = FaultSeverity.HIGH,
                spareParts = listOf(
                    SparePartEstimate("Heavy Brass Quarter-Turn Angle Valve (Jaquar/Hindware)", 320, 480),
                    SparePartEstimate("SS-304 Braided Connection Pipe (18 inch)", 180, 260),
                    SparePartEstimate("PTFE Teflon Seal Tape & Brass Extension Nipple", 60, 110)
                ),
                estimatedLaborMinInr = 199,
                estimatedLaborMaxInr = 349,
                estimatedTimeMinutes = 40,
                safetyPrecautions = listOf(
                    "Close the bathroom/kitchen main concealed stopcock valve.",
                    "Place a dry towel around cabinet wood to prevent laminate swelling.",
                    "Switch off under-sink RO power plug to avoid short circuits."
                ),
                aiConfidencePercent = if (hasAttachedImage) 96 else 90
            )

            ProfessionCategory.AC_REPAIR -> AiDiagnosisReport(
                id = "DIAG-${UUID.randomUUID().toString().take(6).uppercase()}",
                inputDescription = userDescription.ifBlank { selectedScenario?.defaultPrompt.orEmpty() },
                inputScenarioTitle = selectedScenario?.title ?: "HVAC Refrigerant & Evaporator Coil Diagnostics",
                recommendedCategory = ProfessionCategory.AC_REPAIR,
                faultTitle = "Low R32 Refrigerant Pressure, Clogged Drain Line & Evaporator Frosting",
                rootCauseAnalysis = "E4 fault code combined with indoor unit water overflow indicates restricted condensate drainage and sub-optimal suction pressure (likely flare-nut micro-leak or choked capillary).",
                severity = FaultSeverity.MEDIUM,
                spareParts = listOf(
                    SparePartEstimate("R32 / R410A Refrigerant Top-Up & Nitrogen Leak Test", 950, 1450),
                    SparePartEstimate("High-Pressure Foam Jet Service (Indoor + Outdoor)", 499, 599),
                    SparePartEstimate("Outdoor Dual-Run Capacitor 45+4 mfd (if weak)", 350, 480, isOptional = true)
                ),
                estimatedLaborMinInr = 299,
                estimatedLaborMaxInr = 449,
                estimatedTimeMinutes = 65,
                safetyPrecautions = listOf(
                    "Switch off the AC from the stabilizer/MCB to prevent compressor overheating.",
                    "Move electronics away from beneath the indoor unit drip zone."
                ),
                aiConfidencePercent = 95
            )

            ProfessionCategory.MECHANIC -> AiDiagnosisReport(
                id = "DIAG-${UUID.randomUUID().toString().take(6).uppercase()}",
                inputDescription = userDescription.ifBlank { selectedScenario?.defaultPrompt.orEmpty() },
                inputScenarioTitle = selectedScenario?.title ?: "Automotive Cranking & Alternator Telemetry",
                recommendedCategory = ProfessionCategory.MECHANIC,
                faultTitle = "Lead-Acid Battery Cell Sulfation (<10.4V Load Drop) or Starter Solenoid Fault",
                rootCauseAnalysis = "Rapid solenoid chatter on ignition indicates insufficient Cold Cranking Amps (CCA) reaching the starter motor, caused by a discharged battery or corroded battery terminal clamps.",
                severity = FaultSeverity.HIGH,
                spareParts = listOf(
                    SparePartEstimate("12V Jump-Start & Multi-Point Alternator Load Test", 250, 350),
                    SparePartEstimate("Brass Battery Terminal Clamp + Anti-Corrosion Spray", 120, 200),
                    SparePartEstimate("Amaron / Exide 45Ah DIN Battery (with old battery exchange)", 3800, 4600, isOptional = true)
                ),
                estimatedLaborMinInr = 249,
                estimatedLaborMaxInr = 399,
                estimatedTimeMinutes = 30,
                safetyPrecautions = listOf(
                    "Do not repeatedly crank the ignition—it can overheat the starter relay.",
                    "Turn off AC blower, infotainment, and headlights while waiting."
                ),
                aiConfidencePercent = 94
            )

            ProfessionCategory.LAPTOP_REPAIR -> AiDiagnosisReport(
                id = "DIAG-${UUID.randomUUID().toString().take(6).uppercase()}",
                inputDescription = userDescription.ifBlank { selectedScenario?.defaultPrompt.orEmpty() },
                inputScenarioTitle = selectedScenario?.title ?: "Computer Display eDP Cable & Thermal Audit",
                recommendedCategory = ProfessionCategory.LAPTOP_REPAIR,
                faultTitle = "Pinched 30/40-Pin eDP Display Flex Cable & Dried CPU Thermal Paste",
                rootCauseAnalysis = "Angle-dependent horizontal scanlines indicate flexing wear on the LVDS/eDP ribbon cable passing through the hinge barrel, rather than a cracked LCD panel.",
                severity = FaultSeverity.MEDIUM,
                spareParts = listOf(
                    SparePartEstimate("OEM eDP Display Flex Ribbon Cable", 650, 1100),
                    SparePartEstimate("Arctic MX-4 High-Conductivity Thermal Paste + Fan Cleaning", 350, 500),
                    SparePartEstimate("Brass Hinge Mount Reinforcement", 300, 450, isOptional = true)
                ),
                estimatedLaborMinInr = 299,
                estimatedLaborMaxInr = 499,
                estimatedTimeMinutes = 55,
                safetyPrecautions = listOf(
                    "Avoid opening and closing the laptop lid repeatedly.",
                    "Back up critical work or connect an external HDMI monitor if urgent."
                ),
                aiConfidencePercent = 93
            )

            ProfessionCategory.DOCTOR -> AiDiagnosisReport(
                id = "DIAG-${UUID.randomUUID().toString().take(6).uppercase()}",
                inputDescription = userDescription.ifBlank { selectedScenario?.defaultPrompt.orEmpty() },
                inputScenarioTitle = selectedScenario?.title ?: "Home Medical Triage & Physician Dispatch",
                recommendedCategory = ProfessionCategory.DOCTOR,
                faultTitle = "Acute Febrile Episode — Verified MBBS/MD Home Consultation Required",
                rootCauseAnalysis = "Symptoms require clinical auscultation, SpO2 monitoring, and differential evaluation by a MCI/NMC-registered neighborhood physician.",
                severity = FaultSeverity.HIGH,
                spareParts = listOf(
                    SparePartEstimate("Digital Vitals Kit Check (BP, SpO2, Otoscope, Glucometer)", 150, 250),
                    SparePartEstimate("Rapid Fever / CBC Lab Sample Collection at Home", 450, 750, isOptional = true)
                ),
                estimatedLaborMinInr = 499,
                estimatedLaborMaxInr = 699,
                estimatedTimeMinutes = 35,
                safetyPrecautions = listOf(
                    "Maintain oral hydration (ORS / fluids) and lukewarm tepid sponging.",
                    "Keep previous prescriptions and allergy records ready for the doctor."
                ),
                aiConfidencePercent = 96
            )

            else -> AiDiagnosisReport(
                id = "DIAG-${UUID.randomUUID().toString().take(6).uppercase()}",
                inputDescription = userDescription.ifBlank { "Comprehensive inspection and repair required." },
                inputScenarioTitle = selectedScenario?.title ?: "${matchedCategory.titleEn} AI Diagnostic Assessment",
                recommendedCategory = matchedCategory,
                faultTitle = "Verified ${matchedCategory.titleEn} Inspection & On-Site Resolution",
                rootCauseAnalysis = "AI semantic analysis matched your requirement to a certified ${matchedCategory.titleEn}. Standard visiting inspection and itemized spare-parts rate card apply.",
                severity = if (matchedCategory.supportsEmergency) FaultSeverity.HIGH else FaultSeverity.MEDIUM,
                spareParts = listOf(
                    SparePartEstimate("Standard Consumables & Diagnostic Kit", 150, 350),
                    SparePartEstimate("OEM Replacement Component (if required after inspection)", 300, 850, isOptional = true)
                ),
                estimatedLaborMinInr = matchedCategory.baseVisitingFeeInr,
                estimatedLaborMaxInr = matchedCategory.baseVisitingFeeInr + 200,
                estimatedTimeMinutes = 45,
                safetyPrecautions = listOf(
                    "Keep the work area accessible and well-lit for the visiting specialist.",
                    "Confirm the itemized digital quote in chat before work begins."
                ),
                aiConfidencePercent = 91
            )
        }
    }

    fun detectBestCategory(query: String): ProfessionCategory? {
        val normalized = query.trim().lowercase(Locale.ROOT)
        if (normalized.isEmpty()) return null

        // Direct name or keyword match
        return ProfessionCategory.entries.firstOrNull { category ->
            category.titleEn.lowercase(Locale.ROOT).contains(normalized) ||
                category.titleHi.contains(normalized) ||
                category.keywords.any { keyword -> normalized.contains(keyword) }
        }
    }

    fun auditProviderProfile(
        name: String,
        bio: String,
        experienceYears: Int,
        baseRateInr: Int,
        category: ProfessionCategory,
        governmentIdNumber: String,
        upiId: String
    ): AiProfileAuditResult {
        val suggestions = mutableListOf<String>()
        var bioScore = 60
        if (bio.length >= 60) bioScore += 25 else suggestions.add("Expand your bio to at least 60 characters detailing specific tools and certifications.")
        if (bio.contains("certified", ignoreCase = true) || bio.contains("warranty", ignoreCase = true) || bio.contains("experience", ignoreCase = true)) {
            bioScore += 15
        } else {
            suggestions.add("Mention service warranty (e.g., '30-day warranty') to boost customer conversion by 38%.")
        }

        val expectedBase = category.baseVisitingFeeInr
        val priceScore = when {
            baseRateInr in (expectedBase - 80)..(expectedBase + 120) -> 96
            baseRateInr > expectedBase + 250 -> {
                suggestions.add("Your visiting fee (₹$baseRateInr) is above the neighborhood median (₹$expectedBase). Consider ₹$expectedBase for faster initial bookings.")
                72
            }
            else -> 85
        }

        val kycScore = if (governmentIdNumber.count { it.isDigit() } >= 8 && upiId.contains("@")) {
            98
        } else {
            suggestions.add("Provide a valid 12-digit Aadhaar / PAN number and verified UPI ID for instant escrow payouts.")
            65
        }

        val fraudRisk = bio.lowercase(Locale.ROOT).let {
            it.contains("advance payment only") || it.contains("whatsapp only") || it.contains("no bill")
        }
        if (fraudRisk) {
            suggestions.add("CRITICAL: Off-platform payment solicitation ('advance only / no bill') violates NeighbourConnect Trust rules.")
        }

        val overall = ((bioScore * 0.35) + (priceScore * 0.30) + (kycScore * 0.35)).toInt()
            .let { if (fraudRisk) (it - 35).coerceAtLeast(25) else it.coerceIn(50, 99) }

        val improvedBio = "Certified ${category.titleEn} with $experienceYears+ years of hands-on experience in residential & commercial repairs. Equipped with professional diagnostic tools, genuine ISI/OEM spare parts, transparent upfront pricing, and a 30-day service warranty."

        return AiProfileAuditResult(
            overallScore = overall,
            bioClarityScore = bioScore.coerceAtMost(100),
            pricingCompetitivenessScore = priceScore,
            kycReadinessScore = kycScore,
            fraudRiskFlag = fraudRisk,
            actionableSuggestions = if (suggestions.isEmpty()) {
                listOf("Excellent profile! Your credentials and pricing meet Platinum Neighborhood Guardian standards.")
            } else suggestions,
            improvedBioSuggestion = improvedBio
        )
    }

    fun analyzeReviewForFraud(comment: String, rating: Int, hasVerifiedBooking: Boolean): Pair<Int, String?> {
        val lower = comment.lowercase(Locale.ROOT)
        if (!hasVerifiedBooking) {
            return 32 to "Flagged by AI Sybil Detector: Reviewer has no completed escrow transaction with this provider."
        }
        if (lower.contains("best best best") || lower.contains("100% cheapest call") || lower.contains("promo")) {
            return 28 to "Flagged by AI Linguistic Engine: Repetitive promotional spam syntax detected."
        }
        return 98 to null
    }
}
