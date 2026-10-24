package com.example.presentation.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ProfessionCategory
import com.example.domain.model.ServiceProvider
import com.example.ui.theme.EmergencyCrimson
import com.example.ui.theme.VerifiedEmerald

@Composable
fun EmergencySosScreen(
    neighborhood: String,
    allProviders: List<ServiceProvider>,
    onTriggerEmergencyDispatch: (ProfessionCategory, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emergencyCategories = remember {
        ProfessionCategory.entries.filter { it.supportsEmergency }
    }
    var selectedEmergencyCategory by remember { mutableStateOf(ProfessionCategory.ELECTRICIAN) }
    var emergencyNote by remember { mutableStateOf("") }

    val matchingResponders = remember(allProviders, selectedEmergencyCategory) {
        allProviders.filter { it.category == selectedEmergencyCategory && it.acceptsEmergency }
            .ifEmpty { allProviders.filter { it.acceptsEmergency } }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sos_radar")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Sonar Radar Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Surface(
                        color = EmergencyCrimson,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "24x7 PRIORITY EMERGENCY SOS RADAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animated Radar Canvas
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxRadius = size.minDimension / 2f
                            drawCircle(
                                color = Color(0xFFEF4444).copy(alpha = 1f - radarPulse * 0.8f),
                                radius = maxRadius * radarPulse,
                                center = center
                            )
                            drawCircle(
                                color = Color(0xFFF87171).copy(alpha = 0.35f),
                                radius = maxRadius * 0.7f,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = Color(0xFFF87171).copy(alpha = 0.5f),
                                radius = maxRadius * 0.4f,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            // Simulated Nearby Provider Pins on Radar
                            drawCircle(
                                color = Color(0xFF10B981),
                                radius = 7.dp.toPx(),
                                center = Offset(center.x + 54.dp.toPx(), center.y - 26.dp.toPx())
                            )
                            drawCircle(
                                color = Color(0xFF10B981),
                                radius = 7.dp.toPx(),
                                center = Offset(center.x - 48.dp.toPx(), center.y + 22.dp.toPx())
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = EmergencyCrimson,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Emergency,
                                    contentDescription = "SOS",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Scanning 3 km around $neighborhood",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${matchingResponders.size} KYC-Verified ${selectedEmergencyCategory.titleEn} Responders Active Now • Avg ETA 9 mins",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFECACA)
                    )
                }
            }
        }

        // 2. Select Urgent Profession
        item {
            Text(
                text = "1. Select Emergency Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                emergencyCategories.chunked(2).forEach { rowCats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCats.forEach { cat ->
                            val isSelected = selectedEmergencyCategory == cat
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) EmergencyCrimson else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) EmergencyCrimson else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedEmergencyCategory = cat }
                                    .testTag("sos_cat_${cat.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.titleEn,
                                        tint = if (isSelected) Color.White else EmergencyCrimson,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = cat.titleEn,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Base ₹${cat.baseVisitingFeeInr}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White.copy(alpha = 0.85f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        if (rowCats.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 3. Emergency Note & One-Tap Dispatch
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, EmergencyCrimson.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Optional Emergency Situation Note",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emergencyNote,
                        onValueChange = { emergencyNote = it },
                        placeholder = {
                            Text("e.g., Main pipe burst flooding kitchen, or sparking fuse box...")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sos_note_input")
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onTriggerEmergencyDispatch(selectedEmergencyCategory, emergencyNote) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyCrimson),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("trigger_sos_dispatch_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BROADCAST SOS & LOCK NEAREST ${selectedEmergencyCategory.titleEn.uppercase()}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Standby Responders List
        item {
            Text(
                text = "Standby Emergency Responders Within 3 km:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(matchingResponders) { responder ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = responder.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = VerifiedEmerald.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Trust ${responder.trustScore}/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VerifiedEmerald,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${responder.neighborhood} • ${responder.distanceKm} km away • ETA ${responder.responseTimeMinutes} mins",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { onTriggerEmergencyDispatch(responder.category, emergencyNote) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyCrimson),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Dispatch")
                    }
                }
            }
        }
    }
}
