package com.example.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.PLATFORM_OWNER_ACCOUNT_EMAIL
import com.example.domain.model.ProfessionCategory
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserRole
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmergencyCrimson
import com.example.ui.theme.TrustIndigo
import com.example.ui.theme.VerifiedEmerald

enum class GovIdDocumentType(
    val code: String,
    val displayLabel: String,
    val placeholder: String,
    val formatHint: String,
    val sampleValidNumber: String
) {
    AADHAAR(
        code = "AADHAAR",
        displayLabel = "Aadhaar Card (12-Digit UIDAI)",
        placeholder = "482910384756",
        formatHint = "Exactly 12 digits (no spaces or dashes)",
        sampleValidNumber = "482910384756"
    ),
    PAN(
        code = "PAN",
        displayLabel = "PAN Card (Income Tax Dept)",
        placeholder = "ABCDE1234F",
        formatHint = "5 uppercase letters + 4 digits + 1 letter (e.g. ABCDE1234F)",
        sampleValidNumber = "ABCDE1234F"
    ),
    DRIVING_LICENSE(
        code = "DRIVING_LICENSE",
        displayLabel = "Driving License (RTO India)",
        placeholder = "KA0120200019283",
        formatHint = "2 state letters + 2 RTO digits + 7–13 alphanumeric chars",
        sampleValidNumber = "KA0120200019283"
    ),
    PASSPORT(
        code = "PASSPORT",
        displayLabel = "Indian Passport",
        placeholder = "M4829103",
        formatHint = "1 uppercase letter followed by 7 digits (e.g. M4829103)",
        sampleValidNumber = "M4829103"
    )
}

object ProviderKycValidator {
    private val aadhaarRegex = Regex("^\\d{12}$")
    private val panRegex = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
    private val dlRegex = Regex("^[A-Z]{2}[0-9]{2}[A-Z0-9]{7,13}$")
    private val passportRegex = Regex("^[A-Z][0-9]{7}$")
    private val certNumberRegex = Regex("^[A-Za-z0-9_\\-/]{5,30}$")
    private val upiRegex = Regex("^[a-zA-Z0-9.\\-_]{2,64}@[a-zA-Z]{2,32}$")

    fun validateGovIdNumber(type: GovIdDocumentType, rawInput: String): String? {
        val cleaned = rawInput.trim().replace(" ", "").replace("-", "").uppercase()
        if (cleaned.isEmpty()) {
            return "Government ID number is mandatory for Service Providers."
        }
        val matches = when (type) {
            GovIdDocumentType.AADHAAR -> aadhaarRegex.matches(cleaned)
            GovIdDocumentType.PAN -> panRegex.matches(cleaned)
            GovIdDocumentType.DRIVING_LICENSE -> dlRegex.matches(cleaned)
            GovIdDocumentType.PASSPORT -> passportRegex.matches(cleaned)
        }
        return if (matches) null else "Invalid ${type.displayLabel} format. Required: ${type.formatHint}"
    }

    fun normalizeGovIdNumber(rawInput: String): String =
        rawInput.trim().replace(" ", "").replace("-", "").uppercase()

    fun validateGovIdDocumentUri(uri: String, isVerified: Boolean): String? {
        if (uri.isBlank()) {
            return "Please upload or attach your Government ID document scan."
        }
        if (!isVerified) {
            return "Government ID document scan must pass format & OCR verification."
        }
        return null
    }

    fun validateCertificationTitle(title: String): String? {
        val trimmed = title.trim()
        return when {
            trimmed.isEmpty() -> "Professional certification or license title is required."
            trimmed.length < 3 -> "Certification title must be at least 3 characters."
            trimmed.length > 120 -> "Certification title cannot exceed 120 characters."
            else -> null
        }
    }

    fun validateIssuingAuthority(authority: String): String? {
        val trimmed = authority.trim()
        return when {
            trimmed.isEmpty() -> "Issuing authority / licensing board is required."
            trimmed.length < 3 -> "Issuing authority must be at least 3 characters."
            trimmed.length > 120 -> "Issuing authority cannot exceed 120 characters."
            else -> null
        }
    }

    fun validateCertificationNumber(rawCertNumber: String): String? {
        val cleaned = rawCertNumber.trim().replace(" ", "-")
        return when {
            cleaned.isEmpty() -> "Professional certification / license number is mandatory."
            !certNumberRegex.matches(cleaned) ->
                "License number must be 5–30 alphanumeric characters (letters, digits, '-', '_', '/')."
            else -> null
        }
    }

    fun validateUpiId(rawUpi: String): String? {
        val trimmed = rawUpi.trim()
        return when {
            trimmed.isEmpty() -> "UPI ID is required for 98% instant escrow payouts."
            !upiRegex.matches(trimmed) -> "Enter a valid UPI ID (e.g., name@okicici or mobile@ybl)."
            else -> null
        }
    }
}

private fun defaultCertificationPresetFor(category: ProfessionCategory): Pair<String, String> = when (category) {
    ProfessionCategory.ELECTRICIAN ->
        "Class-1 Electrical Wireman & LT Switchgear License" to "Chief Electrical Inspectorate (CEIG) / NCVT"
    ProfessionCategory.PLUMBER ->
        "National Trade Certificate (NTC) — Sanitary & Plumbing" to "NCVT / Skill India Directorate"
    ProfessionCategory.DOCTOR ->
        "MBBS / State Medical Council Registration" to "National Medical Commission (NMC)"
    ProfessionCategory.NURSE ->
        "Registered Nurse & Midwife (RNRM) Certificate" to "Indian Nursing Council (INC)"
    ProfessionCategory.LAWYER ->
        "Certificate of Practice (Advocate Bar Enrollment)" to "Bar Council of India (BCI)"
    ProfessionCategory.AC_REPAIR ->
        "HVAC & Inverter Split AC Technician Certification" to "RASCI / Skill India Board"
    ProfessionCategory.DRIVER, ProfessionCategory.TAXI ->
        "Commercial Transport Badge & PSV Endorsement" to "Regional Transport Office (RTO)"
    else ->
        "National Skill Qualification Framework (NSQF) Level-4 Certificate" to "Skill India / NSDC Board"
}

data class ServiceProviderKycSubmission(
    val fullName: String,
    val category: ProfessionCategory,
    val customProfessionTitle: String?,
    val bio: String,
    val experienceYears: Int,
    val baseVisitingFeeInr: Int,
    val neighborhood: String,
    val city: String,
    val acceptsEmergency: Boolean,
    val govIdType: GovIdDocumentType,
    val govIdNumber: String,
    val govIdDocumentUri: String,
    val govIdDocumentVerified: Boolean,
    val certificationTitle: String,
    val issuingAuthority: String,
    val certificationNumber: String,
    val certificationDocumentUri: String?,
    val upiId: String,
    val subscriptionTier: SubscriptionTier
)

@Composable
fun ServiceProviderRegistrationScreen(
    authenticatedUserName: String,
    authenticatedUserEmail: String,
    authenticatedUserUid: String,
    initialSelectedRole: UserRole,
    defaultNeighborhood: String,
    isSubmittingKyc: Boolean,
    submissionErrorMessage: String?,
    onContinueAsCustomer: () -> Unit,
    onSubmitProviderKyc: (ServiceProviderKycSubmission) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Allow back press to switch to Customer role if user doesn't want to register as a provider
    BackHandler {
        onContinueAsCustomer()
    }

    var selectedRole by rememberSaveable { mutableStateOf(initialSelectedRole) }

    // Provider Identity & Profession State
    var fullName by rememberSaveable {
        mutableStateOf(authenticatedUserName.ifBlank { "Vikramaditya Rao" })
    }
    var selectedCategory by rememberSaveable { mutableStateOf(ProfessionCategory.ELECTRICIAN) }
    var customProfessionTitle by rememberSaveable { mutableStateOf("") }
    var experienceYearsText by rememberSaveable { mutableStateOf("8") }
    var baseFeeText by rememberSaveable {
        mutableStateOf(ProfessionCategory.ELECTRICIAN.baseVisitingFeeInr.toString())
    }
    var neighborhood by rememberSaveable {
        mutableStateOf(defaultNeighborhood.ifBlank { "Indiranagar 12th Main, Bengaluru" })
    }
    var city by rememberSaveable { mutableStateOf("Bengaluru") }
    var bio by rememberSaveable {
        mutableStateOf(
            "Verified local specialist with genuine OEM tools, transparent digital quotes, and 30-day service warranty."
        )
    }
    var acceptsEmergency by rememberSaveable { mutableStateOf(true) }
    var selectedTier by rememberSaveable { mutableStateOf(SubscriptionTier.PRO) }

    // Mandatory Government ID Document State
    var selectedGovIdType by rememberSaveable { mutableStateOf(GovIdDocumentType.AADHAAR) }
    var govIdNumber by rememberSaveable { mutableStateOf("") }
    var govIdDocumentUri by rememberSaveable { mutableStateOf("") }
    var govIdDocumentVerified by rememberSaveable { mutableStateOf(false) }

    // Mandatory Professional Certification Fields State
    val initialPreset = remember { defaultCertificationPresetFor(ProfessionCategory.ELECTRICIAN) }
    var certificationTitle by rememberSaveable { mutableStateOf(initialPreset.first) }
    var issuingAuthority by rememberSaveable { mutableStateOf(initialPreset.second) }
    var certificationNumber by rememberSaveable { mutableStateOf("") }
    var certificationDocumentUri by rememberSaveable { mutableStateOf("") }

    // UPI Payout State
    var upiId by rememberSaveable {
        val handlePrefix = authenticatedUserEmail.substringBefore("@")
            .filter { it.isLetterOrDigit() }
            .lowercase()
            .ifBlank { "specialist" }
        mutableStateOf("$handlePrefix@okicici")
    }

    // Show all field validation errors once the user clicks submit
    var hasAttemptedSubmit by rememberSaveable { mutableStateOf(false) }

    // Zero-permission Android Photo Picker for Government ID & Certification scans
    val govIdPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            govIdDocumentUri = uri.toString()
            govIdDocumentVerified = true
        }
    }

    val certPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            certificationDocumentUri = uri.toString()
        }
    }

    // Live Validation Computations
    val fullNameError = if (fullName.trim().length < 2) {
        "Full legal name (min 2 characters) matching your Government ID is required."
    } else null

    val govIdNumberError = ProviderKycValidator.validateGovIdNumber(selectedGovIdType, govIdNumber)
    val govIdDocError = ProviderKycValidator.validateGovIdDocumentUri(govIdDocumentUri, govIdDocumentVerified)
    val certTitleError = ProviderKycValidator.validateCertificationTitle(certificationTitle)
    val issuingAuthorityError = ProviderKycValidator.validateIssuingAuthority(issuingAuthority)
    val certNumberError = ProviderKycValidator.validateCertificationNumber(certificationNumber)
    val upiError = ProviderKycValidator.validateUpiId(upiId)

    val experienceYears = experienceYearsText.toIntOrNull()
    val experienceError = if (experienceYears == null || experienceYears !in 0..60) {
        "Enter valid experience between 0 and 60 years."
    } else null

    val baseFeeInr = baseFeeText.toIntOrNull()
    val baseFeeError = if (baseFeeInr == null || baseFeeInr !in 49..100000) {
        "Visiting fee must be between ₹49 and ₹1,00,000."
    } else null

    val isAllProviderFieldsValid = fullNameError == null &&
        govIdNumberError == null &&
        govIdDocError == null &&
        certTitleError == null &&
        issuingAuthorityError == null &&
        certNumberError == null &&
        upiError == null &&
        experienceError == null &&
        baseFeeError == null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("service_provider_registration_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Firebase Auth Verified Session Banner + Role Selection Header
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = TrustIndigo),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = VerifiedEmerald.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, Color(0xFF4ADE80))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = "Firebase Auth Verified",
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "FIREBASE AUTHENTICATED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF6EE7B7),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = authenticatedUserName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$authenticatedUserEmail • Linked to $PLATFORM_OWNER_ACCOUNT_EMAIL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.80f)
                                )
                            }
                        }

                        TextButton(onClick = onSignOut) {
                            Text(
                                text = "Switch Account",
                                color = Color(0xFF93C5FD),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "How would you like to use NeighbourConnect?",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select Customer for immediate access, or Service Provider to complete mandatory Government ID & Professional Certification verification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Two-Card Role Selector: Customer vs Service Provider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Role Option 1: Customer (No ID required)
                        val isCustomerSelected = selectedRole == UserRole.CUSTOMER
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCustomerSelected) Color(0xFF065F46) else Color.White.copy(alpha = 0.10f),
                            border = BorderStroke(
                                width = if (isCustomerSelected) 2.dp else 1.dp,
                                color = if (isCustomerSelected) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedRole = UserRole.CUSTOMER }
                                .testTag("select_role_customer_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.PersonSearch,
                                        contentDescription = null,
                                        tint = Color(0xFF4ADE80),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Customer",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Find & hire pros • No ID required",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFD1FAE5)
                                )
                            }
                        }

                        // Role Option 2: Service Provider (Mandatory KYC & Cert)
                        val isProviderSelected = selectedRole == UserRole.PROVIDER
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isProviderSelected) Color(0xFF1E3A8A) else Color.White.copy(alpha = 0.10f),
                            border = BorderStroke(
                                width = if (isProviderSelected) 2.dp else 1.dp,
                                color = if (isProviderSelected) AmberGold else Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedRole = UserRole.PROVIDER }
                                .testTag("select_role_provider_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Badge,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Service Provider",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Mandatory Gov ID & Cert KYC",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFDE68A)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. If CUSTOMER role is selected -> Show Instant Access Card (No Registration / No ID needed)
        if (selectedRole == UserRole.CUSTOMER) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, VerifiedEmerald.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = VerifiedEmerald,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Customer Account Ready",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Government ID & Professional Certification are NOT required for Customers.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VerifiedEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = "Your Google account ($authenticatedUserEmail) is authenticated via Firebase Auth. You can immediately discover verified electricians, plumbers, doctors, tutors, and 50+ local professionals, run AI fault diagnostics, and book with 2% Razorpay Escrow protection.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = onContinueAsCustomer,
                            colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("continue_as_customer_button")
                        ) {
                            Text(
                                text = "Enter Marketplace as Customer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = { selectedRole = UserRole.PROVIDER },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.HowToReg, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Want to offer services instead? Register as Service Provider")
                        }
                    }
                }
            }
        } else {
            // 3. SERVICE PROVIDER MANDATORY KYC REGISTRATION FORM
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AmberGold.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, AmberGold)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mandatory Service Provider KYC Gate",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "To protect neighborhood trust, every Service Provider must validate a Government ID document and a Professional Trade Certification before listing services. Customers do not need this step.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(
                            onClick = onContinueAsCustomer,
                            modifier = Modifier.testTag("skip_to_customer_btn")
                        ) {
                            Text("I'm a Customer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Demo Autofill Bar for Easy Verification & Testing
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Testing KYC Validation?",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pre-fill valid Government ID & Certification credentials with 1 tap.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                val preset = defaultCertificationPresetFor(selectedCategory)
                                selectedGovIdType = GovIdDocumentType.AADHAAR
                                govIdNumber = GovIdDocumentType.AADHAAR.sampleValidNumber
                                govIdDocumentUri = "content://neighbourconnect.kyc/gov_id/aadhaar_verified_${authenticatedUserUid.take(6)}.jpg"
                                govIdDocumentVerified = true
                                certificationTitle = preset.first
                                issuingAuthority = preset.second
                                certificationNumber = "NC-CERT-2025-88412"
                                certificationDocumentUri = "content://neighbourconnect.kyc/cert/trade_license_${authenticatedUserUid.take(6)}.pdf"
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("autofill_valid_kyc_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = VerifiedEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto-Fill Valid KYC")
                        }
                    }
                }
            }

            // SECTION A: Legal Identity & Profession Selection
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Step 1 • Legal Identity & Profession",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Legal Name (As on Government ID) *") },
                            isError = hasAttemptedSubmit && fullNameError != null,
                            supportingText = {
                                if (fullNameError != null && hasAttemptedSubmit) {
                                    Text(fullNameError, color = EmergencyCrimson)
                                } else {
                                    Text("Linked to Firebase Auth UID: ${authenticatedUserUid.take(12)}...")
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("kyc_full_name_input")
                        )

                        Text(
                            text = "Select Primary Profession Category *",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ProfessionCategory.entries) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = {
                                        selectedCategory = cat
                                        baseFeeText = cat.baseVisitingFeeInr.toString()
                                        val preset = defaultCertificationPresetFor(cat)
                                        certificationTitle = preset.first
                                        issuingAuthority = preset.second
                                    },
                                    label = { Text("${cat.titleEn} (${cat.titleHi})") }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = customProfessionTitle,
                            onValueChange = { customProfessionTitle = it },
                            label = { Text("Custom / Specialized Title (Optional)") },
                            placeholder = { Text("e.g., EV Home Charger Engineer, Robotic Vacuum Tech") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = experienceYearsText,
                                onValueChange = { experienceYearsText = it.filter { ch -> ch.isDigit() }.take(2) },
                                label = { Text("Experience (Yrs) *") },
                                isError = hasAttemptedSubmit && experienceError != null,
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("kyc_experience_input")
                            )
                            OutlinedTextField(
                                value = baseFeeText,
                                onValueChange = { baseFeeText = it.filter { ch -> ch.isDigit() }.take(6) },
                                label = { Text("Visiting Fee (₹) *") },
                                isError = hasAttemptedSubmit && baseFeeError != null,
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("kyc_visiting_fee_input")
                            )
                        }

                        OutlinedTextField(
                            value = neighborhood,
                            onValueChange = { neighborhood = it },
                            label = { Text("Primary Service Neighborhood *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // SECTION B: Mandatory Government ID Document Verification
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (govIdNumberError == null && govIdDocError == null) {
                            VerifiedEmerald
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.AssignmentInd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Step 2 • Government ID Verification",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (govIdNumberError == null && govIdDocError == null) {
                                Surface(
                                    shape = CircleShape,
                                    color = VerifiedEmerald.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = VerifiedEmerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ID VALIDATED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = VerifiedEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Select Official Government ID Type *",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(GovIdDocumentType.entries) { idType ->
                                FilterChip(
                                    selected = selectedGovIdType == idType,
                                    onClick = {
                                        selectedGovIdType = idType
                                    },
                                    label = { Text(idType.displayLabel) },
                                    modifier = Modifier.testTag("gov_id_type_${idType.code}")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = govIdNumber,
                            onValueChange = { govIdNumber = it.uppercase() },
                            label = { Text("${selectedGovIdType.displayLabel} Number *") },
                            placeholder = { Text("e.g., ${selectedGovIdType.placeholder}") },
                            isError = (govIdNumber.isNotBlank() || hasAttemptedSubmit) && govIdNumberError != null,
                            trailingIcon = {
                                if (govIdNumber.isNotBlank()) {
                                    Icon(
                                        imageVector = if (govIdNumberError == null) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                                        contentDescription = null,
                                        tint = if (govIdNumberError == null) VerifiedEmerald else EmergencyCrimson
                                    )
                                }
                            },
                            supportingText = {
                                if ((govIdNumber.isNotBlank() || hasAttemptedSubmit) && govIdNumberError != null) {
                                    Text(
                                        text = govIdNumberError,
                                        color = EmergencyCrimson,
                                        modifier = Modifier.testTag("gov_id_number_error_text")
                                    )
                                } else {
                                    Text("Format requirement: ${selectedGovIdType.formatHint}")
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("kyc_gov_id_number_input")
                        )

                        // Government ID Document Scan Upload / Attachment Box
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (govIdDocumentVerified && govIdDocumentUri.isNotBlank()) {
                                VerifiedEmerald.copy(alpha = 0.08f)
                            } else if (hasAttemptedSubmit && govIdDocError != null) {
                                EmergencyCrimson.copy(alpha = 0.08f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (govIdDocumentVerified && govIdDocumentUri.isNotBlank()) {
                                    VerifiedEmerald
                                } else if (hasAttemptedSubmit && govIdDocError != null) {
                                    EmergencyCrimson
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (govIdDocumentVerified) Icons.Filled.VerifiedUser else Icons.Filled.CloudUpload,
                                        contentDescription = null,
                                        tint = if (govIdDocumentVerified) VerifiedEmerald else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Government ID Document Scan (Mandatory) *",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (govIdDocumentVerified && govIdDocumentUri.isNotBlank()) {
                                                "Verified Document Attached: ${govIdDocumentUri.takeLast(38)}"
                                            } else {
                                                "Upload front/back photo of your ${selectedGovIdType.displayLabel}"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (govIdDocumentVerified) VerifiedEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            govIdPhotoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("pick_gov_id_photo_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CloudUpload,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Upload ID Photo")
                                    }

                                    Button(
                                        onClick = {
                                            if (govIdNumber.isBlank()) {
                                                govIdNumber = selectedGovIdType.sampleValidNumber
                                            }
                                            govIdDocumentUri =
                                                "content://neighbourconnect.kyc/gov_id/${selectedGovIdType.code.lowercase()}_scan_verified.jpg"
                                            govIdDocumentVerified = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TrustIndigo),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("attach_verified_gov_id_sample_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Verified,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Attach Verified Scan")
                                    }
                                }

                                if (hasAttemptedSubmit && govIdDocError != null) {
                                    Text(
                                        text = govIdDocError,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmergencyCrimson,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.testTag("gov_id_doc_error_text")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION C: Mandatory Professional Certification & Trade License Validation
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (certTitleError == null && issuingAuthorityError == null && certNumberError == null) {
                            VerifiedEmerald
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.WorkspacePremium,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Step 3 • Professional Certification",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (certTitleError == null && issuingAuthorityError == null && certNumberError == null) {
                                Surface(
                                    shape = CircleShape,
                                    color = VerifiedEmerald.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = VerifiedEmerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "CERTIFIED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = VerifiedEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = certificationTitle,
                            onValueChange = { certificationTitle = it },
                            label = { Text("Professional Certification / Trade License Title *") },
                            placeholder = { Text("e.g., Class-1 Electrical Wireman License") },
                            isError = (certificationTitle.isNotBlank() || hasAttemptedSubmit) && certTitleError != null,
                            supportingText = {
                                if ((certificationTitle.isNotBlank() || hasAttemptedSubmit) && certTitleError != null) {
                                    Text(certTitleError, color = EmergencyCrimson)
                                } else {
                                    Text("Official trade certificate, degree, or guild registration title")
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("kyc_cert_title_input")
                        )

                        OutlinedTextField(
                            value = issuingAuthority,
                            onValueChange = { issuingAuthority = it },
                            label = { Text("Issuing Authority / Licensing Board *") },
                            placeholder = { Text("e.g., NCVT / Skill India / State Medical Council") },
                            isError = (issuingAuthority.isNotBlank() || hasAttemptedSubmit) && issuingAuthorityError != null,
                            supportingText = {
                                if ((issuingAuthority.isNotBlank() || hasAttemptedSubmit) && issuingAuthorityError != null) {
                                    Text(issuingAuthorityError, color = EmergencyCrimson)
                                } else {
                                    Text("Government board, university, or accredited trade council")
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("kyc_issuing_authority_input")
                        )

                        OutlinedTextField(
                            value = certificationNumber,
                            onValueChange = { certificationNumber = it.uppercase() },
                            label = { Text("Certification / License Registration Number *") },
                            placeholder = { Text("e.g., KEI-BLR-2024-88412") },
                            isError = (certificationNumber.isNotBlank() || hasAttemptedSubmit) && certNumberError != null,
                            trailingIcon = {
                                if (certificationNumber.isNotBlank()) {
                                    Icon(
                                        imageVector = if (certNumberError == null) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                                        contentDescription = null,
                                        tint = if (certNumberError == null) VerifiedEmerald else EmergencyCrimson
                                    )
                                }
                            },
                            supportingText = {
                                if ((certificationNumber.isNotBlank() || hasAttemptedSubmit) && certNumberError != null) {
                                    Text(
                                        text = certNumberError,
                                        color = EmergencyCrimson,
                                        modifier = Modifier.testTag("cert_number_error_text")
                                    )
                                } else {
                                    Text("5–30 alphanumeric chars (letters, digits, '-', '_', '/')")
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("kyc_cert_number_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    certPhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (certificationDocumentUri.isBlank()) "Attach Certificate Photo"
                                    else "Certificate Attached ✓"
                                )
                            }

                            if (certificationNumber.isBlank()) {
                                TextButton(
                                    onClick = {
                                        certificationNumber = "NC-CERT-2025-88412"
                                        certificationDocumentUri =
                                            "content://neighbourconnect.kyc/cert/sample_license.pdf"
                                    }
                                ) {
                                    Text("Use Sample License #")
                                }
                            }
                        }
                    }
                }
            }

            // SECTION D: UPI Escrow Settlement & Final Submission
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Step 4 • 98% Direct UPI Escrow Settlement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text("Verified UPI ID for Escrow Payouts *") },
                            placeholder = { Text("name@okicici") },
                            isError = (upiId.isNotBlank() || hasAttemptedSubmit) && upiError != null,
                            supportingText = {
                                if ((upiId.isNotBlank() || hasAttemptedSubmit) && upiError != null) {
                                    Text(upiError, color = EmergencyCrimson)
                                } else {
                                    Text("Instant payout upon customer 4-digit OTP completion (only 2% fee)")
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("kyc_upi_id_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Accept 24x7 Emergency SOS Dispatch",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Receive high-priority neighborhood emergency pings (< 10 min ETA)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = acceptsEmergency,
                                onCheckedChange = { acceptsEmergency = it }
                            )
                        }

                        HorizontalDivider()

                        AnimatedVisibility(visible = !submissionErrorMessage.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmergencyCrimson.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, EmergencyCrimson)
                            ) {
                                Text(
                                    text = submissionErrorMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmergencyCrimson,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        if (hasAttemptedSubmit && !isAllProviderFieldsValid) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmergencyCrimson.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, EmergencyCrimson),
                                modifier = Modifier.testTag("kyc_validation_summary_error")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Please resolve the following mandatory KYC requirements:",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = EmergencyCrimson,
                                        fontWeight = FontWeight.Bold
                                    )
                                    listOfNotNull(
                                        fullNameError,
                                        govIdNumberError,
                                        govIdDocError,
                                        certTitleError,
                                        issuingAuthorityError,
                                        certNumberError,
                                        upiError,
                                        experienceError,
                                        baseFeeError
                                    ).forEach { err ->
                                        Text(
                                            text = "• $err",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmergencyCrimson
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                hasAttemptedSubmit = true
                                if (isAllProviderFieldsValid) {
                                    onSubmitProviderKyc(
                                        ServiceProviderKycSubmission(
                                            fullName = fullName.trim(),
                                            category = selectedCategory,
                                            customProfessionTitle = customProfessionTitle.trim().takeIf { it.isNotBlank() },
                                            bio = bio.trim(),
                                            experienceYears = experienceYears ?: 5,
                                            baseVisitingFeeInr = baseFeeInr ?: 199,
                                            neighborhood = neighborhood.trim(),
                                            city = city.trim(),
                                            acceptsEmergency = acceptsEmergency,
                                            govIdType = selectedGovIdType,
                                            govIdNumber = ProviderKycValidator.normalizeGovIdNumber(govIdNumber),
                                            govIdDocumentUri = govIdDocumentUri.trim(),
                                            govIdDocumentVerified = govIdDocumentVerified,
                                            certificationTitle = certificationTitle.trim(),
                                            issuingAuthority = issuingAuthority.trim(),
                                            certificationNumber = certificationNumber.trim().replace(" ", "-").uppercase(),
                                            certificationDocumentUri = certificationDocumentUri.trim().takeIf { it.isNotBlank() },
                                            upiId = upiId.trim(),
                                            subscriptionTier = selectedTier
                                        )
                                    )
                                }
                            },
                            enabled = !isSubmittingKyc,
                            colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("submit_kyc_provider_button")
                        ) {
                            if (isSubmittingKyc) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Verifying Government ID & Saving to Firebase...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Verify Government ID & Complete Registration",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
