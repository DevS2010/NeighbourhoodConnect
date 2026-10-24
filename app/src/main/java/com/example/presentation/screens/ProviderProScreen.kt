package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.firebase.ServiceProviderRegistrationDocument
import com.example.domain.model.AiProfileAuditResult
import com.example.domain.model.ProfessionCategory
import com.example.domain.model.ServiceBooking
import com.example.domain.model.SubscriptionTier
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmergencyCrimson
import com.example.ui.theme.TrustIndigo
import com.example.ui.theme.VerifiedEmerald

@Composable
fun ProviderProScreen(
    isOnline: Boolean,
    bookings: List<ServiceBooking>,
    latestAudit: AiProfileAuditResult?,
    onToggleOnline: () -> Unit,
    onRunProfileAudit: (String, String, Int, Int, ProfessionCategory, String, String) -> Unit,
    onRegisterProvider: (
        String,
        ProfessionCategory,
        String?,
        String,
        Int,
        String,
        String,
        Int,
        Boolean,
        List<String>,
        String,
        String,
        SubscriptionTier,
        (String) -> Unit
    ) -> Unit,
    onSwitchToCustomerMode: () -> Unit,
    verifiedKycRegistration: ServiceProviderRegistrationDocument? = null,
    onOpenFullKycRegistrationScreen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Self-Registration & KYC, 1 = Pro Analytics & Leads

    // Self-Registration Form State
    var providerName by remember { mutableStateOf("Rajeshwari Electrical & Solar Works") }
    var selectedCategory by remember { mutableStateOf(ProfessionCategory.ELECTRICIAN) }
    var customProfessionTitle by remember { mutableStateOf("") }
    var experienceYearsText by remember { mutableStateOf("9") }
    var baseFeeText by remember { mutableStateOf("199") }
    var neighborhood by remember { mutableStateOf("Indiranagar 12th Main") }
    var city by remember { mutableStateOf("Bengaluru") }
    var bio by remember {
        mutableStateOf(
            "Certified Electrician with 9+ years of experience in residential MCB boards, inverter wiring, and appliance repairs. 30-day warranty on all work."
        )
    }
    var govIdNumber by remember { mutableStateOf("4829-9104-7732") }
    var upiId by remember { mutableStateOf("rajeshwari.solar@okaxis") }
    var acceptsEmergency by remember { mutableStateOf(true) }
    var selectedTier by remember { mutableStateOf(SubscriptionTier.PRO) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Provider Hub Header with Live Availability Switch
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = TrustIndigo)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PROVIDER PRO & ONBOARDING HUB",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6EE7B7),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Keep 98% of Every Job",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Only 2% flat platform commission • Instant UPI Escrow Payouts",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Switch(
                                checked = isOnline,
                                onCheckedChange = { onToggleOnline() },
                                modifier = Modifier.testTag("provider_online_switch")
                            )
                            Text(
                                text = if (isOnline) "ONLINE FOR SOS" else "OFFLINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOnline) Color(0xFF34D399) else Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { selectedTabIndex = 0 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTabIndex == 0) VerifiedEmerald else Color.White.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_self_register_btn")
                        ) {
                            Icon(imageVector = Icons.Filled.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Self-Register & KYC")
                        }

                        Button(
                            onClick = { selectedTabIndex = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTabIndex == 1) VerifiedEmerald else Color.White.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_pro_analytics_btn")
                        ) {
                            Icon(imageVector = Icons.Filled.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analytics & Tiers")
                        }
                    }
                }
            }
        }

        // Verified Firebase KYC & Professional Certification Status Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = VerifiedEmerald.copy(alpha = 0.10f)
                ),
                border = BorderStroke(1.5.dp, VerifiedEmerald)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = VerifiedEmerald,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firebase Auth & Gov ID KYC Verified",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = VerifiedEmerald
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenFullKycRegistrationScreen,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_full_kyc_screen_btn")
                        ) {
                            Text("Update KYC / Role")
                        }
                    }

                    if (verifiedKycRegistration != null) {
                        Text(
                            text = "Provider: ${verifiedKycRegistration.fullName} • ${verifiedKycRegistration.professionCategory}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Gov ID: ${verifiedKycRegistration.govIdType} (${verifiedKycRegistration.govIdNumber}) • Cert: ${verifiedKycRegistration.certificationTitle} (#${verifiedKycRegistration.certificationNumber})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (selectedTabIndex == 0) {
            // TAB 0: Self-Registration, Government ID KYC & AI Profile Quality Check
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "1. Select Your Profession (25+ Trades or Custom)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ProfessionCategory.entries) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = {
                                        selectedCategory = cat
                                        baseFeeText = cat.baseVisitingFeeInr.toString()
                                    },
                                    label = { Text("${cat.titleEn} (${cat.titleHi})") }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = customProfessionTitle,
                            onValueChange = { customProfessionTitle = it },
                            label = { Text("Optional Custom / Future Profession Title (e.g. Solar EV Charger Tech)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = providerName,
                            onValueChange = { providerName = it },
                            label = { Text("Full Name or Business Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_provider_name_input")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = experienceYearsText,
                                onValueChange = { experienceYearsText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Experience (Yrs)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = baseFeeText,
                                onValueChange = { baseFeeText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Visiting Fee (₹)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = neighborhood,
                                onValueChange = { neighborhood = it },
                                label = { Text("Neighborhood") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Service Bio, Tools & Warranty Details") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider()

                        Text(
                            text = "2. Government ID KYC & Instant UPI Escrow Settlement",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = govIdNumber,
                            onValueChange = { govIdNumber = it },
                            label = { Text("Aadhaar / PAN / Trade License Number (Masked at Storage)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text("Verified UPI ID for 98% Direct Escrow Payouts") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Accept 24x7 Emergency SOS Pings",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Earn 1.5x priority Surge bonus for rapid response",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = acceptsEmergency,
                                onCheckedChange = { acceptsEmergency = it }
                            )
                        }

                        // AI Profile Quality Check Button
                        OutlinedButton(
                            onClick = {
                                onRunProfileAudit(
                                    providerName,
                                    bio,
                                    experienceYearsText.toIntOrNull() ?: 5,
                                    baseFeeText.toIntOrNull() ?: 199,
                                    selectedCategory,
                                    govIdNumber,
                                    upiId
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("run_profile_audit_btn")
                        ) {
                            Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run AI Profile Quality & Fraud Check")
                        }

                        // Display AI Profile Audit Result if run
                        latestAudit?.let { audit ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (audit.fraudRiskFlag) {
                                        EmergencyCrimson.copy(alpha = 0.1f)
                                    } else {
                                        VerifiedEmerald.copy(alpha = 0.1f)
                                    }
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (audit.fraudRiskFlag) EmergencyCrimson else VerifiedEmerald
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "AI Profile Quality Score: ${audit.overallScore}/100",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (audit.fraudRiskFlag) EmergencyCrimson else VerifiedEmerald
                                        )
                                        Text(
                                            text = "KYC: ${audit.kycReadinessScore}% • Pricing: ${audit.pricingCompetitivenessScore}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    audit.actionableSuggestions.forEach { tip ->
                                        Text(
                                            text = "• $tip",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { bio = audit.improvedBioSuggestion },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Apply AI-Optimized Bio")
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onRegisterProvider(
                                    providerName,
                                    selectedCategory,
                                    customProfessionTitle,
                                    bio,
                                    experienceYearsText.toIntOrNull() ?: 5,
                                    neighborhood,
                                    city,
                                    baseFeeText.toIntOrNull() ?: 199,
                                    acceptsEmergency,
                                    listOf("English", "Hindi", "Kannada"),
                                    govIdNumber,
                                    upiId,
                                    selectedTier
                                ) {
                                    onSwitchToCustomerMode()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("submit_provider_registration_btn")
                        ) {
                            Icon(imageVector = Icons.Filled.VerifiedUser, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Complete KYC & Publish Live to Neighborhood",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // TAB 1: Business Analytics, Revenue Model & Subscription Tiers
            item {
                val totalGross = bookings.sumOf { it.quote.subtotalInr }.coerceAtLeast(18450)
                val platformFee2Percent = (totalGross * 0.02).toInt()
                val netEarnings = totalGross - platformFee2Percent

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Provider Earnings & 2% Commission Ledger",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AnalyticsMetricBox("Gross Job Value", "₹$totalGross", MaterialTheme.colorScheme.primary)
                            AnalyticsMetricBox("2% Platform Fee", "₹$platformFee2Percent", AmberGold)
                            AnalyticsMetricBox("98% Net UPI Payout", "₹$netEarnings", VerifiedEmerald)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Comparison vs Legacy Aggregators (25% Commission): You saved ₹${(totalGross * 0.23).toInt()} this month on NeighbourConnect!",
                            style = MaterialTheme.typography.labelMedium,
                            color = VerifiedEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Provider Subscription & Featured Listing Tiers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(SubscriptionTier.entries) { tier ->
                val isSelected = selectedTier == tier
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTier = tier }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${tier.title} (${tier.badgeLabel})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Platform Commission: ${tier.platformCommissionPercent}% • Priority Search Ranking & AI Lead Insights",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (tier.monthlyFeeInr == 0) "FREE" else "₹${tier.monthlyFeeInr}/mo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedEmerald
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsMetricBox(label: String, value: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
