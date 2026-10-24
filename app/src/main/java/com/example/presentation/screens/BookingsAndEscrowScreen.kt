package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.BookingStatus
import com.example.domain.model.ServiceBooking
import com.example.presentation.components.DigitalInvoiceDialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmergencyCrimson
import com.example.ui.theme.TrustIndigo
import com.example.ui.theme.VerifiedEmerald

@Composable
fun BookingsAndEscrowScreen(
    bookings: List<ServiceBooking>,
    onOpenChat: (String) -> Unit,
    onFundRazorpayEscrow: (ServiceBooking, String) -> Unit,
    onMarkProviderArrived: (ServiceBooking) -> Unit,
    onVerifyOtpAndRelease: (ServiceBooking, String, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var razorpayDialogBooking by remember { mutableStateOf<ServiceBooking?>(null) }
    var otpReleaseDialogBooking by remember { mutableStateOf<ServiceBooking?>(null) }
    var invoiceDialogBooking by remember { mutableStateOf<ServiceBooking?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TrustIndigo)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Escrow Vault",
                            tint = Color(0xFF34D399)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Bookings, Live GPS & Escrow Vault",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "2% Flat Platform Fee • 98% Direct Provider UPI Payout • GST Invoices",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF93C5FD)
                            )
                        }
                    }
                }
            }
        }

        items(bookings, key = { it.id }) { booking ->
            val isCompleted = booking.status == BookingStatus.COMPLETED
            val statusColor = when {
                booking.isEmergencySos && !isCompleted -> EmergencyCrimson
                isCompleted -> VerifiedEmerald
                else -> MaterialTheme.colorScheme.primary
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.2.dp, statusColor.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_card_${booking.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Booking ID & Status Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = statusColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${booking.status.label} • #${booking.id}",
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = VerifiedEmerald.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "OTP: ${booking.completionOtp}",
                                style = MaterialTheme.typography.labelMedium,
                                color = VerifiedEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${booking.providerName} (${booking.providerCategory.titleEn})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = booking.issueSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Slot: ${booking.scheduledSlotLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Live GPS Route Map Widget when provider is en route
                    if (booking.status == BookingStatus.PROVIDER_EN_ROUTE || booking.status == BookingStatus.ESCROW_FUNDED) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.GpsFixed,
                                            contentDescription = null,
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "LIVE GPS TRACKING • ${booking.providerDistanceKm} km away",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF34D399),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "ETA ${booking.providerEtaMinutes} mins",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Simulated Route Vector Line Canvas
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                ) {
                                    val start = Offset(28.dp.toPx(), size.height / 2f)
                                    val end = Offset(size.width - 28.dp.toPx(), size.height / 2f)
                                    drawLine(
                                        color = Color(0xFF334155),
                                        start = start,
                                        end = end,
                                        strokeWidth = 4.dp.toPx()
                                    )
                                    drawLine(
                                        color = Color(0xFF10B981),
                                        start = start,
                                        end = Offset(size.width * 0.65f, size.height / 2f),
                                        strokeWidth = 4.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f))
                                    )
                                    drawCircle(
                                        color = Color(0xFF38BDF8),
                                        radius = 8.dp.toPx(),
                                        center = start
                                    )
                                    drawCircle(
                                        color = Color(0xFF10B981),
                                        radius = 10.dp.toPx(),
                                        center = Offset(size.width * 0.65f, size.height / 2f)
                                    )
                                    drawCircle(
                                        color = Color(0xFFF59E0B),
                                        radius = 8.dp.toPx(),
                                        center = end
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Specialist Hub",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "En Route (${booking.providerCurrentLat}, ${booking.providerCurrentLng})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF34D399)
                                    )
                                    Text(
                                        text = "Your Home",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFBBF24)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    // Escrow Financial Breakdown Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Payable (Incl. 18% GST: ₹${booking.quote.gstAmountInr})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${booking.quote.totalCustomerPayableInr}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Provider Net (98%): ₹${booking.quote.providerNetPayoutInr} • Platform (2%): ₹${booking.quote.platformCommissionInr}",
                                style = MaterialTheme.typography.labelSmall,
                                color = VerifiedEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons Row depending on Booking Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onOpenChat(booking.id) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_chat_btn_${booking.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chat & Quote")
                        }

                        when (booking.status) {
                            BookingStatus.REQUESTED, BookingStatus.QUOTE_SUBMITTED -> {
                                Button(
                                    onClick = { razorpayDialogBooking = booking },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("fund_escrow_btn_${booking.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountBalanceWallet,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Fund Razorpay Escrow")
                                }
                            }

                            BookingStatus.ESCROW_FUNDED, BookingStatus.PROVIDER_EN_ROUTE, BookingStatus.IN_PROGRESS -> {
                                Button(
                                    onClick = { otpReleaseDialogBooking = booking },
                                    colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("release_escrow_otp_btn_${booking.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verify OTP & Release")
                                }
                            }

                            BookingStatus.COMPLETED -> {
                                Button(
                                    onClick = { invoiceDialogBooking = booking },
                                    colors = ButtonDefaults.buttonColors(containerColor = TrustIndigo),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("view_invoice_btn_${booking.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ReceiptLong,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("GST Invoice")
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    // Razorpay Escrow Checkout Modal
    razorpayDialogBooking?.let { booking ->
        var selectedPaymentMethod by remember { mutableStateOf("UPI Instant (Google Pay / PhonePe)") }
        val paymentMethods = listOf(
            "UPI Instant (Google Pay / PhonePe)",
            "Paytm UPI / BHIM",
            "HDFC / ICICI Credit & Debit Card",
            "NetBanking (SBI / Axis / Kotak)"
        )

        AlertDialog(
            onDismissRequest = { razorpayDialogBooking = null },
            title = {
                Column {
                    Text(
                        text = "Razorpay Escrow Checkout",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "100% Money-Back Protection Until Job Completion",
                        style = MaterialTheme.typography.labelSmall,
                        color = VerifiedEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Amount to Lock in Escrow: ₹${booking.quote.totalCustomerPayableInr}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Funds are held safely in NeighbourConnect Razorpay Escrow and only disbursed to ${booking.providerName} after you share the 4-digit Completion OTP (${booking.completionOtp}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    paymentMethods.forEach { method ->
                        FilterChip(
                            selected = selectedPaymentMethod == method,
                            onClick = { selectedPaymentMethod = method },
                            label = { Text(method, style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onFundRazorpayEscrow(booking, selectedPaymentMethod)
                        razorpayDialogBooking = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald),
                    modifier = Modifier.testTag("confirm_razorpay_pay_btn")
                ) {
                    Text("Pay ₹${booking.quote.totalCustomerPayableInr} into Escrow")
                }
            },
            dismissButton = {
                TextButton(onClick = { razorpayDialogBooking = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4-Digit Completion OTP & Escrow Release Modal
    otpReleaseDialogBooking?.let { booking ->
        var otpInput by remember { mutableStateOf(booking.completionOtp) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { otpReleaseDialogBooking = null },
            title = {
                Text(
                    text = "Verify Completion OTP & Release Escrow",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Confirm that ${booking.providerName} has satisfactorily completed the job. Entering the 4-digit OTP releases ₹${booking.quote.providerNetPayoutInr} (98%) to the provider's UPI and generates your GST Tax Invoice.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = {
                            otpInput = it
                            errorMsg = null
                        },
                        label = { Text("4-Digit Completion OTP (Hint: ${booking.completionOtp})") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("completion_otp_input")
                    )
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = EmergencyCrimson
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onVerifyOtpAndRelease(booking, otpInput) { ok ->
                            if (ok) {
                                otpReleaseDialogBooking = null
                                invoiceDialogBooking = booking.copy(status = BookingStatus.COMPLETED)
                            } else {
                                errorMsg = "Incorrect OTP. Please enter ${booking.completionOtp}."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald),
                    modifier = Modifier.testTag("confirm_otp_release_btn")
                ) {
                    Text("Verify & Release ₹${booking.quote.providerNetPayoutInr}")
                }
            },
            dismissButton = {
                TextButton(onClick = { otpReleaseDialogBooking = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Digital GST Invoice Modal
    invoiceDialogBooking?.let { booking ->
        DigitalInvoiceDialog(
            booking = booking,
            onDismiss = { invoiceDialogBooking = null }
        )
    }
}
