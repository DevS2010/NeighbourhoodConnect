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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import com.example.data.firebase.ConsumerProfileDocument
import com.example.data.firebase.PLATFORM_OWNER_ACCOUNT_EMAIL
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ai.AiEngine
import com.example.domain.model.AppLanguage
import com.example.domain.model.BookingStatus
import com.example.domain.model.ProfessionCategory
import com.example.domain.model.ProfessionGroup
import com.example.domain.model.ServiceBooking
import com.example.domain.model.ServiceProvider
import com.example.presentation.components.ProviderCard
import com.example.presentation.components.professionGroupAccentColor
import com.example.ui.theme.EmergencyCrimson
import com.example.ui.theme.TrustIndigo
import com.example.ui.theme.VerifiedEmerald
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    providers: List<ServiceProvider>,
    activeBookings: List<ServiceBooking>,
    selectedNeighborhood: String,
    selectedLanguage: AppLanguage,
    searchQuery: String,
    selectedGroup: ProfessionGroup,
    selectedCategory: ProfessionCategory?,
    minTrustScore: Int,
    onSearchQueryChange: (String) -> Unit,
    onSelectGroup: (ProfessionGroup) -> Unit,
    onSelectCategory: (ProfessionCategory?) -> Unit,
    onUpdateMinTrustScore: (Int) -> Unit,
    onSelectNeighborhood: (String) -> Unit,
    onOpenLanguageDialog: () -> Unit,
    onSwitchToProviderMode: () -> Unit,
    onNavigateToAiDiagnosis: () -> Unit,
    onNavigateToEmergencySos: () -> Unit,
    onOpenProviderDetail: (String) -> Unit,
    onQuickBookProvider: (ServiceProvider) -> Unit,
    onOpenBookingChat: (String) -> Unit,
    onReplayLogoSplash: () -> Unit = {},
    linkedConsumerProfile: ConsumerProfileDocument? = null,
    cloudBookingsCount: Int = 0,
    onSignOutConsumer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showNeighborhoodPicker by remember { mutableStateOf(false) }
    var showAllCategoriesDialog by remember { mutableStateOf(false) }
    var showConsumerAccountDialog by remember { mutableStateOf(false) }

    val aiDetectedCategory = remember(searchQuery) {
        AiEngine.detectBestCategory(searchQuery)
    }

    val visibleCategories = remember(selectedGroup) {
        if (selectedGroup == ProfessionGroup.ALL) {
            ProfessionCategory.entries
        } else {
            ProfessionCategory.entries.filter { it.group == selectedGroup }
        }
    }

    val liveEnRouteBooking = activeBookings.firstOrNull {
        it.status == BookingStatus.PROVIDER_EN_ROUTE || it.status == BookingStatus.QUOTE_SUBMITTED || it.status == BookingStatus.IN_PROGRESS
    }

    val heroTransition = rememberInfiniteTransition(label = "hero_canvas")
    val heroOrbit by heroTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_orbit"
    )
    val heroPulse by heroTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_pulse"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Gradient Brand Header with Interactive Crest Logo & Smart Search
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF07132B),
                                TrustIndigo,
                                Color(0xFF0B3B3C)
                            )
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Tappable Brand Crest Emblem (Replays Centerpiece Logo Splash)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { onReplayLogoSplash() }
                                    .testTag("header_brand_logo_btn")
                            ) {
                                NeighbourConnectCrestLogo(modifier = Modifier.fillMaxSize())
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showNeighborhoodPicker = true }
                                    .padding(vertical = 2.dp, horizontal = 4.dp)
                                    .testTag("neighborhood_selector")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.GpsFixed,
                                        contentDescription = "GPS Active",
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "NEIGHBOURCONNECT GPS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF6EE7B7),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = selectedNeighborhood,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.14f),
                                modifier = Modifier
                                    .clickable { onOpenLanguageDialog() }
                                    .testTag("language_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Language,
                                        contentDescription = "Language",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedLanguage.nativeName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = VerifiedEmerald,
                                modifier = Modifier
                                    .clickable { onSwitchToProviderMode() }
                                    .testTag("switch_provider_role_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SwapHoriz,
                                        contentDescription = "Switch to Provider Mode",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Provider Pro",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Linked Consumer Google Account Pill
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showConsumerAccountDialog = true }
                            .testTag("linked_consumer_account_bar")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = "Consumer Account",
                                    tint = Color(0xFF6EE7B7),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = linkedConsumerProfile?.displayName?.ifBlank { "Verified Consumer" }
                                            ?: "Verified Google Consumer",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Linked to $PLATFORM_OWNER_ACCOUNT_EMAIL • ${linkedConsumerProfile?.email ?: "Google Auth Active"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFA7F3D0),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF065F46)
                            ) {
                                Text(
                                    text = "Account",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Smart AI Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Search 25+ trades or describe issue (e.g. 'AC not cooling')...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Smart Search",
                                tint = Color(0xFF34D399)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear search",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.12f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
                            focusedBorderColor = Color(0xFF34D399),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("smart_search_input")
                    )

                    if (aiDetectedCategory != null && searchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = VerifiedEmerald.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCategory(aiDetectedCategory) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF6EE7B7),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Smart AI Intent Matched: ${aiDetectedCategory.localizedTitle(selectedLanguage)} (Base ₹${aiDetectedCategory.baseVisitingFeeInr})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Live En-Route Booking Tracker Banner (if active)
        if (liveEnRouteBooking != null) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VerifiedEmerald.copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(1.dp, VerifiedEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clickable { onOpenBookingChat(liveEnRouteBooking.id) }
                        .testTag("live_booking_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = VerifiedEmerald,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = "Live GPS",
                                        tint = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${liveEnRouteBooking.status.label} • OTP: ${liveEnRouteBooking.completionOtp}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VerifiedEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${liveEnRouteBooking.providerName} (${liveEnRouteBooking.providerCategory.titleEn})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap to open Live Chat, Quote & GPS Tracker",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { onOpenBookingChat(liveEnRouteBooking.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Track Live", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 3. Interactive Animated Hero Graphic Showcase Card with Centerpiece Crest
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.4f)),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF061024),
                                    Color(0xFF132C54),
                                    Color(0xFF064E3B)
                                )
                            )
                        )
                ) {
                    // Animated Procedural Neighborhood Constellation Graphic on Right Side
                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                    ) {
                        val focalX = size.width * 0.80f
                        val focalY = size.height * 0.36f
                        val r = size.minDimension * 0.35f

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF10B981).copy(alpha = 0.32f),
                                    Color.Transparent
                                ),
                                center = Offset(focalX, focalY),
                                radius = r * 1.6f
                            ),
                            radius = r * 1.6f,
                            center = Offset(focalX, focalY)
                        )

                        drawCircle(
                            color = Color(0xFF34D399).copy(alpha = (1f - heroPulse) * 0.45f),
                            radius = r * (0.5f + heroPulse * 0.8f),
                            center = Offset(focalX, focalY),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        for (idx in 0 until 6) {
                            val rad = ((heroOrbit + idx * 60f) * PI / 180.0).toFloat()
                            val px = focalX + cos(rad) * (r * 0.88f)
                            val py = focalY + sin(rad) * (r * 0.88f)
                            drawLine(
                                color = Color(0xFF6EE7B7).copy(alpha = 0.22f),
                                start = Offset(focalX, focalY),
                                end = Offset(px, py),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawCircle(
                                color = if (idx % 2 == 0) Color(0xFF34D399) else Color(0xFFFBBF24),
                                radius = 4.dp.toPx(),
                                center = Offset(px, py)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.45f)),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "2% FLAT COMMISSION • 100% ESCROW",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF6EE7B7),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Every Local Profession.\nVerified by AI & Trust.",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Glowing Crest Emblem inside Hero Banner
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clickable { onReplayLogoSplash() }
                            ) {
                                NeighbourConnectCrestLogo(modifier = Modifier.fillMaxSize())
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Snap a photo for instant AI fault diagnosis & INR cost estimate, or trigger 24x7 Emergency SOS.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.88f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onNavigateToAiDiagnosis,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerifiedEmerald,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_ai_diagnosis_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Fault Diagnosis", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = onNavigateToEmergencySos,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmergencyCrimson,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_emergency_sos_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Emergency,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Emergency SOS", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        // 4. Universal 25+ Profession Taxonomy Explorer with Color-Coded Gradient Orbs
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Explore 25+ Local Professions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap any trade to filter nearby KYC-verified specialists",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = { showAllCategoriesDialog = true },
                        modifier = Modifier.testTag("view_all_professions_btn")
                    ) {
                        Text("All 25+ Trades")
                    }
                }

                // Group Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ProfessionGroup.entries) { group ->
                        FilterChip(
                            selected = selectedGroup == group,
                            onClick = { onSelectGroup(group) },
                            label = {
                                Text(
                                    text = if (selectedLanguage == AppLanguage.HINDI) group.titleHi else group.titleEn,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Scroll of Eye-Catching Profession Cards
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visibleCategories) { category ->
                        val isSelected = selectedCategory == category
                        val catAccent = professionGroupAccentColor(category.group)

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) catAccent else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            shadowElevation = if (isSelected) 6.dp else 2.dp,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) catAccent else catAccent.copy(alpha = 0.30f)
                            ),
                            modifier = Modifier
                                .width(122.dp)
                                .clickable { onSelectCategory(category) }
                                .testTag("category_chip_${category.id}")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) {
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color.White.copy(alpha = 0.28f),
                                                        Color.White.copy(alpha = 0.12f)
                                                    )
                                                )
                                            } else {
                                                Brush.linearGradient(
                                                    listOf(
                                                        catAccent.copy(alpha = 0.22f),
                                                        catAccent.copy(alpha = 0.08f)
                                                    )
                                                )
                                            }
                                        )
                                ) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = category.titleEn,
                                        tint = if (isSelected) Color.White else catAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = category.localizedTitle(selectedLanguage),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "From ₹${category.baseVisitingFeeInr}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f)
                                    else VerifiedEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Trust Score Minimum Filter Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = VerifiedEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nearby Specialists (${providers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(80, 90, 95).forEach { threshold ->
                        FilterChip(
                            selected = minTrustScore == threshold,
                            onClick = { onUpdateMinTrustScore(threshold) },
                            label = {
                                Text(
                                    text = "Trust $threshold+",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VerifiedEmerald.copy(alpha = 0.18f),
                                selectedLabelColor = VerifiedEmerald
                            )
                        )
                    }
                }
            }
        }

        // 6. Active Category Filter Pill
        if (selectedCategory != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Filtered: ${selectedCategory.localizedTitle(selectedLanguage)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Clear ✕",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onSelectCategory(null) }
                            )
                        }
                    }
                }
            }
        }

        // 7. Provider Cards or Empty State
        if (providers.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "No specialists match your current filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try resetting the category filter, lowering the Trust Score threshold to 80+, or self-register as the first neighborhood specialist in this trade!",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = {
                                onSelectCategory(null)
                                onSelectGroup(ProfessionGroup.ALL)
                                onUpdateMinTrustScore(80)
                                onSearchQueryChange("")
                            }) {
                                Text("Reset All Filters")
                            }
                            Button(
                                onClick = onSwitchToProviderMode,
                                colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald)
                            ) {
                                Text("Register as Specialist")
                            }
                        }
                    }
                }
            }
        } else {
            items(providers, key = { it.id }) { provider ->
                ProviderCard(
                    provider = provider,
                    language = selectedLanguage,
                    onCardClick = { onOpenProviderDetail(provider.id) },
                    onQuickBookClick = { onQuickBookProvider(provider) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    if (showNeighborhoodPicker) {
        val neighborhoods = listOf(
            "Indiranagar 12th Main, Bengaluru",
            "Koramangala 4th Block, Bengaluru",
            "HSR Layout Sector 2, Bengaluru",
            "Bandra West, Mumbai",
            "Koregaon Park, Pune",
            "Connaught Place / Vasant Kunj, New Delhi"
        )
        AlertDialog(
            onDismissRequest = { showNeighborhoodPicker = false },
            title = { Text("Select GPS Service Neighborhood", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    neighborhoods.forEach { area ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (area == selectedNeighborhood) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectNeighborhood(area)
                                    showNeighborhoodPicker = false
                                }
                        ) {
                            Text(
                                text = area,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNeighborhoodPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAllCategoriesDialog) {
        AlertDialog(
            onDismissRequest = { showAllCategoriesDialog = false },
            title = { Text("All 25 Supported Local Professions", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.height(380.dp)
                ) {
                    items(ProfessionCategory.entries) { cat ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectGroup(ProfessionGroup.ALL)
                                    onSelectCategory(cat)
                                    showAllCategoriesDialog = false
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = null,
                                        tint = professionGroupAccentColor(cat.group),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${cat.titleEn} (${cat.titleHi})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = cat.group.titleEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "₹${cat.baseVisitingFeeInr}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = VerifiedEmerald
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllCategoriesDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showConsumerAccountDialog) {
        AlertDialog(
            onDismissRequest = { showConsumerAccountDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = VerifiedEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Linked Consumer Account", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Consumer: ${linkedConsumerProfile?.displayName ?: "Google Verified User"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Email: ${linkedConsumerProfile?.email ?: "Authenticated via Google"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Linked Platform Account: $PLATFORM_OWNER_ACCOUNT_EMAIL",
                        style = MaterialTheme.typography.bodySmall,
                        color = VerifiedEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Cloud-Synced Bookings: $cloudBookingsCount",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Active GPS Neighborhood: $selectedNeighborhood",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConsumerAccountDialog = false
                        onSignOutConsumer()
                    },
                    modifier = Modifier.testTag("consumer_sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = EmergencyCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sign Out", color = EmergencyCrimson, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                TextButton(onClick = { showConsumerAccountDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

