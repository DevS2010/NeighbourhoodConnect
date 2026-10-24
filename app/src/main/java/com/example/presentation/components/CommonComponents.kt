package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.domain.model.ProfessionGroup
import com.example.domain.model.ServiceBooking
import com.example.domain.model.ServiceProvider
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.TrustScoreBreakdown
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmergencyCrimson
import com.example.ui.theme.TrustIndigo
import com.example.ui.theme.VerifiedEmerald

fun professionGroupAccentColor(group: ProfessionGroup): Color = when (group) {
    ProfessionGroup.ALL -> VerifiedEmerald
    ProfessionGroup.HOME_REPAIR -> Color(0xFF2563EB)
    ProfessionGroup.TECH_REPAIR -> Color(0xFF7C3AED)
    ProfessionGroup.HEALTH_LEGAL -> Color(0xFF059669)
    ProfessionGroup.EDUCATION_MEDIA -> Color(0xFFD97706)
    ProfessionGroup.DOMESTIC_DELIVERY -> Color(0xFF0891B2)
    ProfessionGroup.AUTOMOTIVE_EMERGENCY -> Color(0xFFDC2626)
}

@Composable
fun TrustScoreBadge(
    score: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val badgeColor = when {
        score >= 90 -> VerifiedEmerald
        score >= 80 -> TrustIndigo
        else -> AmberGold
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = badgeColor.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f)),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = "Verified Trust Score",
                tint = badgeColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Trust $score/100",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }
    }
}

@Composable
fun TrustScoreCircularGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(52.dp)
    ) {
        Canvas(modifier = Modifier.size(52.dp)) {
            val strokeW = 5.dp.toPx()
            val arcSize = Size(size.width - strokeW, size.height - strokeW)
            val topLeft = Offset(strokeW / 2f, strokeW / 2f)

            drawArc(
                color = Color(0xFFCBD5E1).copy(alpha = 0.35f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF3B82F6),
                        Color(0xFF10B981),
                        Color(0xFF059669),
                        Color(0xFF3B82F6)
                    )
                ),
                startAngle = 135f,
                sweepAngle = 270f * (score.coerceIn(0, 100) / 100f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = VerifiedEmerald
            )
            Text(
                text = "TRUST",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProviderCard(
    provider: ServiceProvider,
    language: AppLanguage,
    onCardClick: () -> Unit,
    onQuickBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = professionGroupAccentColor(provider.category.group)
    val isFeatured = provider.subscriptionTier == SubscriptionTier.FEATURED

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = if (isFeatured) 1.5.dp else 1.dp,
            color = if (isFeatured) {
                AmberGold.copy(alpha = 0.65f)
            } else {
                accent.copy(alpha = 0.28f)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("provider_card_${provider.id}")
    ) {
        Column {
            // Eye-Catching Gradient Accent Ribbon at Top of Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isFeatured) {
                                listOf(AmberGold, Color(0xFF10B981), TrustIndigo)
                            } else {
                                listOf(accent, VerifiedEmerald)
                            }
                        )
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Top Row: Featured / Online Status + Trust Gauge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isFeatured) {
                            Surface(
                                color = AmberGold.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Featured",
                                        tint = AmberGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "TOP NEIGHBORHOOD LEADER",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold
                                    )
                                }
                            }
                        }

                        Surface(
                            color = if (provider.isOnlineNow) VerifiedEmerald.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (provider.isOnlineNow) VerifiedEmerald else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (provider.isOnlineNow) "Live GPS • ${provider.responseTimeMinutes}m ETA" else "Scheduled",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (provider.isOnlineNow) VerifiedEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    TrustScoreBadge(score = provider.trustScore)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Provider Identity Row with Gradient Icon Orb & Circular Trust Gauge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        accent.copy(alpha = 0.22f),
                                        VerifiedEmerald.copy(alpha = 0.14f)
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = provider.category.icon,
                            contentDescription = provider.category.titleEn,
                            tint = accent,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = provider.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (provider.aadhaarKycVerified) {
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Government ID Verified",
                                    tint = VerifiedEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "${provider.customProfessionTitle ?: provider.category.localizedTitle(language)} • ${provider.experienceYears} yrs exp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = accent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${provider.neighborhood} (${provider.distanceKm} km) • ${provider.completedJobsCount} jobs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    TrustScoreCircularGauge(score = provider.trustScore)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = provider.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Verification Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VerificationPill("Aadhaar KYC (${provider.governmentIdMasked.takeLast(4)})")
                    if (provider.tradeLicenseVerified) {
                        VerificationPill("Trade Certified")
                    }
                    if (provider.acceptsEmergency) {
                        Surface(
                            color = EmergencyCrimson.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = "SOS Ready",
                                    tint = EmergencyCrimson,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "24x7 SOS Ready",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmergencyCrimson,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Pricing & CTA Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Visiting & Inspection",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${provider.baseVisitingFeeInr}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• 2% Escrow Fee",
                                style = MaterialTheme.typography.labelSmall,
                                color = VerifiedEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onCardClick,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Trust Audit")
                        }
                        Button(
                            onClick = onQuickBookClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("book_provider_${provider.id}")
                        ) {
                            Text("Hire Now")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationPill(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = VerifiedEmerald,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TrustScoreBreakdownDialog(
    providerName: String,
    breakdown: TrustScoreBreakdown,
    governmentIdMasked: String,
    aiProfileScore: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Algorithmic Trust Score Audit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$providerName • ${breakdown.trustLevelLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VerifiedEmerald,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "NeighbourConnect replaces easily-gamed star ratings with a 4-pillar cryptographic & behavioral Trust Score (0–100):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TrustMetricBar(
                    label = "1. Government KYC & Police Check ($governmentIdMasked)",
                    points = breakdown.kycVerificationPoints,
                    maxPoints = 25
                )
                TrustMetricBar(
                    label = "2. Escrow Job Completion & Zero-Dispute Velocity",
                    points = breakdown.jobCompletionPoints,
                    maxPoints = 35
                )
                TrustMetricBar(
                    label = "3. AI Sybil-Filtered Verified Customer Reviews",
                    points = breakdown.verifiedReviewPoints,
                    maxPoints = 25
                )
                TrustMetricBar(
                    label = "4. Live GPS Punctuality & SLA Response Rate",
                    points = breakdown.punctualitySlaPoints,
                    maxPoints = 15
                )

                Surface(
                    color = VerifiedEmerald.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = VerifiedEmerald
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Profile Quality Score: $aiProfileScore/100 • Zero Fraud Flags",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedEmerald
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Verified & Understood")
            }
        }
    )
}

@Composable
private fun TrustMetricBar(label: String, points: Int, maxPoints: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$points / $maxPoints pts",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { points.toFloat() / maxPoints.toFloat() },
            color = VerifiedEmerald,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun DigitalInvoiceDialog(
    booking: ServiceBooking,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ReceiptLong,
                    contentDescription = "GST Tax Invoice",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Digital Tax Invoice",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = booking.invoiceNumber ?: "INV-NC-2026-${booking.id.takeLast(4)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Provider: ${booking.providerName} (${booking.providerCategory.titleEn})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Customer Location: ${booking.customerAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                InvoiceLine("Service Labor (${booking.quote.laborDescription})", "₹${booking.quote.laborCostInr}")
                InvoiceLine("Spare Parts (${booking.quote.partsDescription})", "₹${booking.quote.partsCostInr}")
                InvoiceLine("Subtotal", "₹${booking.quote.subtotalInr}", bold = true)
                InvoiceLine("GST (18% IGST/CGST+SGST)", "₹${booking.quote.gstAmountInr}")
                HorizontalDivider()
                InvoiceLine("Total Paid via Razorpay Escrow", "₹${booking.quote.totalCustomerPayableInr}", bold = true)
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Transparent Marketplace Split (2% Commission):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        InvoiceLine("• Provider Direct UPI Payout (98%)", "₹${booking.quote.providerNetPayoutInr}")
                        InvoiceLine("• NeighbourConnect Platform Fee (2%)", "₹${booking.quote.platformCommissionInr}")
                        Text(
                            text = "Warranty: ${booking.quote.warrantyDays} Days NeighbourConnect Protection",
                            style = MaterialTheme.typography.labelSmall,
                            color = VerifiedEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun InvoiceLine(label: String, amount: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
fun LanguagePickerDialog(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select App Language / भाषा चुनें",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { lang ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (lang == currentLanguage) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectLanguage(lang)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${lang.displayName} (${lang.nativeName})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (lang == currentLanguage) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
