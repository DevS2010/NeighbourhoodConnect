package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onEnterApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startEntrance by remember { mutableStateOf(false) }
    var showSubtitleAndFeatures by remember { mutableStateOf(false) }
    var loadingProgress by remember { mutableFloatStateOf(0.08f) }

    LaunchedEffect(Unit) {
        startEntrance = true
        delay(220L)
        showSubtitleAndFeatures = true
        // Smooth progress bar animation before auto-transition
        for (step in 1..25) {
            delay(110L)
            loadingProgress = (step / 25f).coerceIn(0f, 1f)
        }
        delay(350L)
        onEnterApp()
    }

    val logoScale by animateFloatAsState(
        targetValue = if (startEntrance) 1f else 0.35f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splash_graphics")

    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    val counterOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_orbit"
    )

    val pulseWave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_wave"
    )

    val glowBreath by infiniteTransition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_breath"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050B18),
                        Color(0xFF0D2142),
                        Color(0xFF0A2E36),
                        Color(0xFF04131D)
                    )
                )
            )
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Full-Screen Procedural Aurora & Constellation Network Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.40f
            val baseRadius = size.minDimension * 0.24f

            // Ambient Aurora Glow Orbs
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.26f * glowBreath),
                        Color(0xFF3B82F6).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = baseRadius * 2.5f
                ),
                radius = baseRadius * 2.5f,
                center = Offset(cx, cy)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF59E0B).copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.18f),
                    radius = baseRadius * 1.6f
                ),
                radius = baseRadius * 1.6f,
                center = Offset(size.width * 0.85f, size.height * 0.18f)
            )

            // Expanding Sonar Pulse Rings from Center Logo
            val wave1Radius = baseRadius * (0.9f + pulseWave * 1.35f)
            drawCircle(
                color = Color(0xFF34D399).copy(alpha = (1f - pulseWave) * 0.45f),
                radius = wave1Radius,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5.dp.toPx())
            )

            val wave2Phase = (pulseWave + 0.5f) % 1f
            val wave2Radius = baseRadius * (0.9f + wave2Phase * 1.35f)
            drawCircle(
                color = Color(0xFF60A5FA).copy(alpha = (1f - wave2Phase) * 0.35f),
                radius = wave2Radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.8.dp.toPx())
            )

            // Outer Constellation Orbit Ring & Connected Service Nodes
            val outerOrbitR = baseRadius * 1.42f
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = outerOrbitR,
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )

            val nodeCount = 8
            val nodeOffsets = mutableListOf<Offset>()
            for (i in 0 until nodeCount) {
                val angleRad = ((orbitAngle + i * (360f / nodeCount)) * PI / 180.0).toFloat()
                val nx = cx + cos(angleRad) * outerOrbitR
                val ny = cy + sin(angleRad) * outerOrbitR
                nodeOffsets.add(Offset(nx, ny))

                // Subtle energy beam connecting satellite node to center shield
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.35f),
                            Color(0xFF60A5FA).copy(alpha = 0.05f)
                        ),
                        start = Offset(cx, cy),
                        end = Offset(nx, ny)
                    ),
                    start = Offset(cx, cy),
                    end = Offset(nx, ny),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            // Connect adjacent satellite nodes in a neighborhood mesh
            for (i in 0 until nodeCount) {
                val p1 = nodeOffsets[i]
                val p2 = nodeOffsets[(i + 1) % nodeCount]
                drawLine(
                    color = Color(0xFF34D399).copy(alpha = 0.18f),
                    start = p1,
                    end = p2,
                    strokeWidth = 1.dp.toPx()
                )
                val nodeColor = when (i % 3) {
                    0 -> Color(0xFF10B981)
                    1 -> Color(0xFF60A5FA)
                    else -> Color(0xFFFBBF24)
                }
                drawCircle(
                    color = nodeColor.copy(alpha = 0.3f),
                    radius = 9.dp.toPx(),
                    center = p1
                )
                drawCircle(
                    color = nodeColor,
                    radius = 4.5.dp.toPx(),
                    center = p1
                )
            }

            // Counter-Rotating Segmented Tech Arc Ring around the Logo
            val innerArcRadius = baseRadius * 1.05f
            rotate(degrees = counterOrbitAngle, pivot = Offset(cx, cy)) {
                for (seg in 0 until 4) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF10B981),
                                Color(0xFF38BDF8),
                                Color(0xFFF59E0B),
                                Color(0xFF10B981)
                            ),
                            center = Offset(cx, cy)
                        ),
                        startAngle = seg * 90f + 12f,
                        sweepAngle = 55f,
                        useCenter = false,
                        topLeft = Offset(cx - innerArcRadius, cy - innerArcRadius),
                        size = Size(innerArcRadius * 2f, innerArcRadius * 2f),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // 2. Foreground Content Column with Centerpiece Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.6f))

            // CENTERPIECE NEIGHBOURCONNECT EMBLEM LOGO
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(168.dp)
                    .scale(logoScale)
            ) {
                NeighbourConnectCrestLogo(
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Animated Brand Title & Tagline Reveal
            AnimatedVisibility(
                visible = showSubtitleAndFeatures,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INDIA'S HYPER-LOCAL TRUST NETWORK",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6EE7B7),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Neighbour",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Connect",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Connecting every neighborhood with trusted professionals.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // 3 Feature Pillar Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SplashFeatureChip(
                            icon = Icons.Filled.AutoAwesome,
                            title = "AI Vision",
                            subtitle = "Fault Diagnosis",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                        SplashFeatureChip(
                            icon = Icons.Filled.Security,
                            title = "2% Fee",
                            subtitle = "Razorpay Escrow",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.weight(1f)
                        )
                        SplashFeatureChip(
                            icon = Icons.Filled.Bolt,
                            title = "24x7 SOS",
                            subtitle = "9m GPS Dispatch",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Interactive Launch Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { loadingProgress },
                    color = Color(0xFF10B981),
                    trackColor = Color.White.copy(alpha = 0.14f),
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onEnterApp,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color(0xFF042F2E)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("splash_enter_marketplace_btn")
                ) {
                    Text(
                        text = "Explore 25+ Verified Professions",
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
            }
        }
    }
}

@Composable
fun NeighbourConnectCrestLogo(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(26))
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4ADE80),
                        Color(0xFF34D399),
                        Color(0xFF059669)
                    )
                ),
                shape = RoundedCornerShape(26)
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.nc_brand_logo),
            contentDescription = "NeighbourConnect Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun SplashFeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = tint.copy(alpha = 0.2f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}
