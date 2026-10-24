package com.example.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.BookingStatus
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageSender
import com.example.domain.model.ServiceBooking
import com.example.ui.theme.TrustIndigo
import com.example.ui.theme.VerifiedEmerald

@Composable
fun ChatAndQuoteScreen(
    booking: ServiceBooking,
    messages: List<ChatMessage>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSubmitRevisedQuote: (String, Int, String, Int) -> Unit,
    onAcceptQuoteAndFundEscrow: (ServiceBooking) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var messageText by remember { mutableStateOf("") }
    var showReviseQuoteDialog by remember { mutableStateOf(false) }
    var showMaskedCallBanner by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Surface(
            color = TrustIndigo,
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chat_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = booking.providerName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${booking.providerCategory.titleEn} • Trust ${booking.providerTrustScore}/100 • #${booking.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6EE7B7)
                        )
                    }
                }

                Row {
                    IconButton(onClick = { showReviseQuoteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.RequestQuote,
                            contentDescription = "Revise Digital Quote",
                            tint = Color(0xFF93C5FD)
                        )
                    }
                    IconButton(onClick = { showMaskedCallBanner = !showMaskedCallBanner }) {
                        Icon(
                            imageVector = Icons.Filled.PhoneInTalk,
                            contentDescription = "Privacy Masked Call",
                            tint = Color(0xFF34D399)
                        )
                    }
                }
            }
        }

        if (showMaskedCallBanner) {
            Surface(
                color = VerifiedEmerald.copy(alpha = 0.14f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = VerifiedEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy Bridge Active: Calling via virtual relay (+91-80-4719-XXXX). Personal phone numbers are hidden.",
                        style = MaterialTheme.typography.labelSmall,
                        color = VerifiedEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Messages Stream
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isCustomer = msg.sender == MessageSender.CUSTOMER
                val isSystem = msg.sender == MessageSender.SYSTEM_AI

                Column(
                    horizontalAlignment = when {
                        isSystem -> Alignment.CenterHorizontally
                        isCustomer -> Alignment.End
                        else -> Alignment.Start
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSystem) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.senderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = VerifiedEmerald
                                )
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isCustomer) 16.dp else 4.dp,
                                bottomEnd = if (isCustomer) 4.dp else 16.dp
                            ),
                            color = if (isCustomer) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            contentColor = if (isCustomer) Color.White else MaterialTheme.colorScheme.onSurface,
                            border = if (!isCustomer) {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            } else null,
                            modifier = Modifier.fillMaxWidth(0.88f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = msg.senderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCustomer) Color(0xFF93C5FD) else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = msg.timestampLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCustomer) Color.White.copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                // Interactive Digital Quote Card inside Chat
                                msg.attachedQuote?.let { quote ->
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        border = BorderStroke(1.5.dp, VerifiedEmerald.copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "ITEMIZED DIGITAL QUOTE (${quote.quoteId})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = VerifiedEmerald,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${quote.warrantyDays}d Warranty",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            QuoteRow("Labor: ${quote.laborDescription}", "₹${quote.laborCostInr}")
                                            QuoteRow("Parts: ${quote.partsDescription}", "₹${quote.partsCostInr}")
                                            QuoteRow("GST (18%)", "₹${quote.gstAmountInr}")
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                            QuoteRow(
                                                "Total Escrow Amount",
                                                "₹${quote.totalCustomerPayableInr}",
                                                bold = true
                                            )
                                            Text(
                                                text = "Provider receives ₹${quote.providerNetPayoutInr} (98%) • Platform fee ₹${quote.platformCommissionInr} (2%)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            if (booking.status == BookingStatus.REQUESTED || booking.status == BookingStatus.QUOTE_SUBMITTED) {
                                                Button(
                                                    onClick = { onAcceptQuoteAndFundEscrow(booking) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("accept_quote_escrow_btn")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.AccountBalanceWallet,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Accept Quote & Lock ₹${quote.totalCustomerPayableInr} in Escrow")
                                                }
                                            } else {
                                                Surface(
                                                    color = VerifiedEmerald.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "✓ Escrow Funded & Locked (${booking.razorpayOrderId ?: "Razorpay Verified"})",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = VerifiedEmerald,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Message Composer Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Message ${booking.providerName} or negotiate quote...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("chat_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    }

    if (showReviseQuoteDialog) {
        var laborDesc by remember { mutableStateOf(booking.quote.laborDescription) }
        var laborInrText by remember { mutableStateOf(booking.quote.laborCostInr.toString()) }
        var partsDesc by remember { mutableStateOf(booking.quote.partsDescription) }
        var partsInrText by remember { mutableStateOf(booking.quote.partsCostInr.toString()) }

        AlertDialog(
            onDismissRequest = { showReviseQuoteDialog = false },
            title = {
                Text(
                    text = "Submit Itemized Digital Quote",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = laborDesc,
                        onValueChange = { laborDesc = it },
                        label = { Text("Service Labor Description") }
                    )
                    OutlinedTextField(
                        value = laborInrText,
                        onValueChange = { laborInrText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Labor Charge (₹)") }
                    )
                    OutlinedTextField(
                        value = partsDesc,
                        onValueChange = { partsDesc = it },
                        label = { Text("Spare Parts Description") }
                    )
                    OutlinedTextField(
                        value = partsInrText,
                        onValueChange = { partsInrText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Spare Parts MRP (₹)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitRevisedQuote(
                            laborDesc,
                            laborInrText.toIntOrNull() ?: 249,
                            partsDesc,
                            partsInrText.toIntOrNull() ?: 0
                        )
                        showReviseQuoteDialog = false
                    }
                ) {
                    Text("Send Quote to Chat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviseQuoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuoteRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
