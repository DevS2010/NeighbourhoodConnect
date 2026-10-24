package com.example.presentation.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ServiceOffering
import com.example.domain.model.ServiceProvider
import com.example.presentation.components.TrustScoreBadge
import com.example.presentation.components.TrustScoreBreakdownDialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmergencyCrimson
import com.example.ui.theme.TrustIndigo
import com.example.ui.theme.VerifiedEmerald

@Composable
fun ProviderDetailScreen(
    provider: ServiceProvider,
    onBack: () -> Unit,
    onConfirmBooking: (ServiceProvider, String, String, ServiceOffering?) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showTrustAuditDialog by remember { mutableStateOf(false) }
    var hideAiFlaggedFakeReviews by remember { mutableStateOf(true) }
    var selectedService by remember { mutableStateOf(provider.services.firstOrNull()) }
    var showBookingDialog by remember { mutableStateOf(false) }
    var bookingNotes by remember { mutableStateOf("") }
    var selectedTimeSlot by remember { mutableStateOf("Instant Dispatch (Arrives in ${provider.responseTimeMinutes} mins)") }

    val displayedReviews = remember(provider.reviews, hideAiFlaggedFakeReviews) {
        if (hideAiFlaggedFakeReviews) {
            provider.reviews.filter { !it.isFlaggedSuspicious }
        } else {
            provider.reviews
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Hero Header
        item {
            Surface(
                color = TrustIndigo,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("provider_detail_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        TrustScoreBadge(
                            score = provider.trustScore,
                            onClick = { showTrustAuditDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = provider.category.icon,
                                    contentDescription = provider.category.titleEn,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = provider.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "KYC Verified",
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "${provider.customProfessionTitle ?: provider.category.titleEn} • ${provider.experienceYears} Years Experience",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF93C5FD),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${provider.neighborhood}, ${provider.city} (${provider.distanceKm} km away)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Key Metrics Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatPill("Trust Score", "${provider.trustScore}/100")
                        ProfileStatPill("Jobs Done", "${provider.completedJobsCount}+")
                        ProfileStatPill("Repeat Rate", "${provider.repeatCustomerRatePercent}%")
                        ProfileStatPill("Live ETA", "${provider.responseTimeMinutes} mins")
                    }
                }
            }
        }

        // 2. Cryptographic KYC & Trust Score Audit Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, VerifiedEmerald.copy(alpha = 0.4f)),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clickable { showTrustAuditDialog = true }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = VerifiedEmerald
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = provider.trustScoreBreakdown.trustLevelLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = VerifiedEmerald
                                )
                                Text(
                                    text = "Govt ID (${provider.governmentIdMasked}) • Police & Trade Verified",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(onClick = { showTrustAuditDialog = true }) {
                            Text("Full Audit")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = provider.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Languages Spoken: ${provider.languagesSpoken.joinToString(", ")} • UPI: ${provider.upiId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Service Offerings & Rate Card
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Transparent Service Menu & Rate Card",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select a service package below (100% protected by Razorpay Escrow)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(provider.services) { service ->
            val isChosen = selectedService?.id == service.id
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChosen) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    width = if (isChosen) 2.dp else 1.dp,
                    color = if (isChosen) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clickable { selectedService = service }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = service.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = service.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Est. ${service.durationMinutes} mins • ${service.warrantyDays}-Day Service Warranty",
                            style = MaterialTheme.typography.labelSmall,
                            color = VerifiedEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${service.priceInr}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isChosen) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = VerifiedEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. AI Fake Review & Sybil Fraud Detection Section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Fake Review Shield Active",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automatically hides unverified or bot-generated reviews",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hideAiFlaggedFakeReviews,
                        onCheckedChange = { hideAiFlaggedFakeReviews = it }
                    )
                }
            }
        }

        items(displayedReviews) { review ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (review.isFlaggedSuspicious) {
                        EmergencyCrimson.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    if (review.isFlaggedSuspicious) EmergencyCrimson
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${review.customerName} (${review.customerNeighborhood})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = if (review.isFlaggedSuspicious) EmergencyCrimson.copy(alpha = 0.15f)
                            else VerifiedEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (review.isFlaggedSuspicious) {
                                    "SUSPICIOUS (${review.aiAuthenticityScore}% Genuine)"
                                } else {
                                    "Verified Escrow (${review.aiAuthenticityScore}% Authentic)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (review.isFlaggedSuspicious) EmergencyCrimson else VerifiedEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = review.comment,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (review.aiFraudReason != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.GppBad,
                                contentDescription = null,
                                tint = EmergencyCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = review.aiFraudReason,
                                style = MaterialTheme.typography.labelSmall,
                                color = EmergencyCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. Bottom Booking Action Button
        item {
            Button(
                onClick = { showBookingDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("open_booking_modal_btn")
            ) {
                Text(
                    text = "Book ${provider.name} • ₹${selectedService?.priceInr ?: provider.baseVisitingFeeInr}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showTrustAuditDialog) {
        TrustScoreBreakdownDialog(
            providerName = provider.name,
            breakdown = provider.trustScoreBreakdown,
            governmentIdMasked = provider.governmentIdMasked,
            aiProfileScore = provider.aiProfileQualityScore,
            onDismiss = { showTrustAuditDialog = false }
        )
    }

    if (showBookingDialog) {
        val slots = listOf(
            "Instant Dispatch (Arrives in ${provider.responseTimeMinutes} mins)",
            "Today • 4:00 PM – 5:00 PM",
            "Today • 6:30 PM – 7:30 PM",
            "Tomorrow • 10:00 AM – 11:00 AM"
        )
        AlertDialog(
            onDismissRequest = { showBookingDialog = false },
            title = {
                Text(
                    text = "Confirm Service & Slot",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selected Package: ${selectedService?.title ?: "Standard Inspection"} (₹${selectedService?.priceInr ?: provider.baseVisitingFeeInr})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Choose Availability Slot:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    slots.forEach { slot ->
                        FilterChip(
                            selected = selectedTimeSlot == slot,
                            onClick = { selectedTimeSlot = slot },
                            label = { Text(slot, style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = bookingNotes,
                        onValueChange = { bookingNotes = it },
                        label = { Text("Describe issue or landmark notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBookingDialog = false
                        onConfirmBooking(provider, bookingNotes, selectedTimeSlot, selectedService)
                    },
                    modifier = Modifier.testTag("confirm_booking_submit_btn")
                ) {
                    Text("Request Digital Quote & Book")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileStatPill(label: String, value: String) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF93C5FD)
            )
        }
    }
}
