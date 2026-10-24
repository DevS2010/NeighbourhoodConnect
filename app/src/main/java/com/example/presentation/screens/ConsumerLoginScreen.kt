package com.example.presentation.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.data.firebase.PLATFORM_OWNER_ACCOUNT_EMAIL
import com.example.domain.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

private const val LOGIN_TAG = "ConsumerLoginScreen"

private val ForestGreenPrimary = Color(0xFF365336)
private val BrandPillText = Color(0xFF1F3522)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun ConsumerLoginScreen(
    isLoadingAuth: Boolean,
    authErrorMessage: String?,
    selectedLoginRole: UserRole = UserRole.CUSTOMER,
    onSelectLoginRole: (UserRole) -> Unit = {},
    onGoogleIdTokenReceived: (String) -> Unit,
    onAuthError: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isRequestingCredential by remember { mutableStateOf(false) }
    var showTermsInfoDialog by remember { mutableStateOf(false) }

    val webClientId = remember(context) {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) context.getString(resId) else ""
    }

    // Separate silent auto-sign-in request for returning users (never combined with GetSignInWithGoogleOption)
    LaunchedEffect(webClientId) {
        val activity = context.findActivity() ?: return@LaunchedEffect
        if (webClientId.isBlank()) return@LaunchedEffect
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()
            val silentRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = credentialManager.getCredential(
                request = silentRequest,
                context = activity
            )
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                onGoogleIdTokenReceived(googleIdTokenCredential.idToken)
            }
        } catch (unused: NoCredentialException) {
            // Expected on first visit when no authorized account is cached yet
        } catch (unused: GetCredentialCancellationException) {
            // User dismissed silent sheet
        } catch (e: GetCredentialException) {
            Log.d(LOGIN_TAG, "Silent auto-sign-in skipped: ${e.message}")
        } catch (e: Exception) {
            Log.d(LOGIN_TAG, "Silent auto-sign-in exception: ${e.message}")
        }
    }

    fun launchInteractiveGoogleSignIn() {
        val activity = context.findActivity()
        if (activity == null) {
            onAuthError("Unable to launch Google Sign-In: Activity context unavailable.")
            return
        }
        if (webClientId.isBlank()) {
            onAuthError("Google Sign-In configuration (default_web_client_id) is missing.")
            return
        }
        onDismissError()
        coroutineScope.launch {
            isRequestingCredential = true
            try {
                // Explicit interactive sign-in uses ONLY GetSignInWithGoogleOption in its own request
                val signInOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                val interactiveRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(signInOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = interactiveRequest,
                    context = activity
                )
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    onGoogleIdTokenReceived(googleIdTokenCredential.idToken)
                } else {
                    onAuthError("Unexpected credential type returned from Google Sign-In.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.i(LOGIN_TAG, "User canceled Google Sign-In prompt", e)
            } catch (e: NoCredentialException) {
                Log.w(LOGIN_TAG, "No Google account available on device", e)
                onAuthError("No Google account found on this device. Please add a Google account in Android Settings.")
            } catch (e: GetCredentialException) {
                Log.e(LOGIN_TAG, "CredentialManager error during Google Sign-In", e)
                onAuthError(e.errorMessage?.toString() ?: "Google Sign-In failed. Please try again.")
            } catch (e: Exception) {
                Log.e(LOGIN_TAG, "Unexpected exception during Google Sign-In", e)
                onAuthError(e.localizedMessage ?: "Failed to complete Google Sign-In.")
            } finally {
                isRequestingCredential = false
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "login_scene_fx")
    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_progress"
    )
    val ambientGlow by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07120B))
            .testTag("consumer_login_screen")
    ) {
        // 1. Custom Architectural Smart-Neighborhood & Emerald Twilight Artwork + Dark Vignette
        SmartNeighborhoodIllustrationBackdrop(
            orbitAngle = radarSweepAngle,
            pulseProgress = pulseProgress,
            ambientGlow = ambientGlow,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Foreground Content with Safe Drawing WindowInsets
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Floating White Brand Pill ("NeighbourConnect") + Live Trust Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF8FAF7),
                    shadowElevation = 10.dp,
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.testTag("consumer_login_brand_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.nc_brand_logo),
                            contentDescription = "NeighbourConnect Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(7.dp))
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = "NeighbourConnect",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp,
                                letterSpacing = (-0.2).sp
                            ),
                            color = BrandPillText
                        )
                    }
                }

                // Glassmorphic Live Verified Badge
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0B1F13).copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, Color(0xFF4ADE80).copy(alpha = 0.45f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE IN INDIA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color(0xFFD1FAE5)
                        )
                    }
                }
            }

            // Bottom Editorial Hero Copy + Enhanced Graphics Badges + Forest-Green "Continue with Google" Button
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Floating Glassmorphic Trust & Feature Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    LoginHighlightBadge(
                        icon = Icons.Filled.VerifiedUser,
                        label = "ID Verified",
                        accentColor = Color(0xFF4ADE80)
                    )
                    LoginHighlightBadge(
                        icon = Icons.Filled.Security,
                        label = "2% Escrow",
                        accentColor = Color(0xFF60A5FA)
                    )
                    LoginHighlightBadge(
                        icon = Icons.Filled.Bolt,
                        label = "9-Min SOS",
                        accentColor = Color(0xFFFBBF24)
                    )
                }

                Text(
                    text = "Trusted local\npros, on\ndemand.",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                        lineHeight = 50.sp,
                        letterSpacing = (-0.8).sp
                    ),
                    color = Color.White,
                    modifier = Modifier.testTag("consumer_login_headline")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Find verified electricians, plumbers, doctors, tutors and 50+ more services in your neighborhood.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.5.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFFE2E8E2),
                    modifier = Modifier.testTag("consumer_login_subtitle")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Role Selection During Login (Customer vs Service Provider)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF0A1810).copy(alpha = 0.82f),
                    border = BorderStroke(1.dp, Color(0xFF4ADE80).copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("login_role_selector_container")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "SELECT ACCOUNT TYPE (CAN ALSO SWITCH AFTER LOGIN)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.7.sp
                            ),
                            color = Color(0xFF86EFAC),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isCustomer = selectedLoginRole == UserRole.CUSTOMER
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCustomer) ForestGreenPrimary else Color.White.copy(alpha = 0.06f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCustomer) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.18f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectLoginRole(UserRole.CUSTOMER) }
                                    .testTag("login_role_customer_btn")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Customer",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Hire Pros • No ID needed",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        color = Color(0xFFD1FAE5)
                                    )
                                }
                            }

                            val isProvider = selectedLoginRole == UserRole.PROVIDER
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isProvider) Color(0xFF1E3A8A) else Color.White.copy(alpha = 0.06f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isProvider) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.18f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectLoginRole(UserRole.PROVIDER) }
                                    .testTag("login_role_provider_btn")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Service Provider",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Gov ID & Cert Required",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        color = Color(0xFFFDE68A)
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = !authErrorMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF7F1D1D).copy(alpha = 0.92f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .clickable { onDismissError() }
                            .testTag("consumer_login_error_banner")
                    ) {
                        Text(
                            text = authErrorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFECACA),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                // Forest-Green Pill Button ("Continue with Google") with Luminous Emerald Rim
                Button(
                    onClick = { launchInteractiveGoogleSignIn() },
                    enabled = !isLoadingAuth && !isRequestingCredential,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreenPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = ForestGreenPrimary.copy(alpha = 0.65f),
                        disabledContentColor = Color.White.copy(alpha = 0.85f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 4.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .border(
                            width = 1.2.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF86EFAC).copy(alpha = 0.55f),
                                    Color(0xFF4ADE80).copy(alpha = 0.25f),
                                    Color(0xFF86EFAC).copy(alpha = 0.55f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .testTag("continue_with_google_button")
                ) {
                    if (isLoadingAuth || isRequestingCredential) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Linking consumer account...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                    } else {
                        GoogleGLogoIcon(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                letterSpacing = 0.1.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Terms & Privacy Footer
                val footerText = buildAnnotatedString {
                    append("By continuing you accept our ")
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = Color(0xFFD1D5DB))) {
                        append("Terms")
                    }
                    append(" and\n")
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = Color(0xFFD1D5DB))) {
                        append("Privacy Policy")
                    }
                    append(".")
                }

                Text(
                    text = footerText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    ),
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTermsInfoDialog = true }
                        .padding(vertical = 4.dp)
                        .testTag("terms_privacy_footer")
                )

                if (showTermsInfoDialog) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable { showTermsInfoDialog = false }
                    ) {
                        Text(
                            text = "Consumer accounts are verified via Google Sign-In and linked to platform account $PLATFORM_OWNER_ACCOUNT_EMAIL with 2% escrow protection.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD1FAE5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginHighlightBadge(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color(0xFF0D1610).copy(alpha = 0.68f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.42f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun GoogleGLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.21f
        val radius = (size.minDimension - strokeWidth) / 2f
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(radius * 2f, radius * 2f)

        // Draw crisp white 'G' matching the reference screenshot
        drawArc(
            color = Color.White,
            startAngle = 42f,
            sweepAngle = 278f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Horizontal crossbar of 'G'
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.50f, size.height * 0.50f),
            end = Offset(size.width * 0.94f, size.height * 0.50f),
            strokeWidth = strokeWidth * 0.92f,
            cap = StrokeCap.Butt
        )
    }
}

@Composable
private fun SmartNeighborhoodIllustrationBackdrop(
    orbitAngle: Float,
    pulseProgress: Float,
    ambientGlow: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val radarCenter = Offset(w * 0.52f, h * 0.30f)
        val baseRadius = size.minDimension * 0.28f

        // 1. Rich Emerald-Teal & Warm Amber Architectural Twilight Sky
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF0E291A),
                    0.26f to Color(0xFF19422A),
                    0.50f to Color(0xFF214A34),
                    0.72f to Color(0xFF112218),
                    1.0f to Color(0xFF070C08)
                )
            )
        )

        // 2. Warm Golden-Hour Sun & Emerald Aurora Glow Orbs
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFBBF24).copy(alpha = 0.28f * ambientGlow),
                    Color(0xFF34D399).copy(alpha = 0.18f * ambientGlow),
                    Color.Transparent
                ),
                center = radarCenter,
                radius = baseRadius * 2.4f
            ),
            center = radarCenter,
            radius = baseRadius * 2.4f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.16f * ambientGlow),
                    Color.Transparent
                ),
                center = Offset(w * 0.18f, h * 0.20f),
                radius = baseRadius * 1.6f
            ),
            center = Offset(w * 0.18f, h * 0.20f),
            radius = baseRadius * 1.6f
        )

        // 3. Expanding GPS Neighborhood Dispatch Radar Waves in the Sky
        val wave1R = baseRadius * (0.55f + pulseProgress * 1.35f)
        drawCircle(
            color = Color(0xFF4ADE80).copy(alpha = (1f - pulseProgress) * 0.38f),
            radius = wave1R,
            center = radarCenter,
            style = Stroke(width = 2.dp.toPx())
        )

        val wave2Phase = (pulseProgress + 0.5f) % 1f
        val wave2R = baseRadius * (0.55f + wave2Phase * 1.35f)
        drawCircle(
            color = Color(0xFFFDE68A).copy(alpha = (1f - wave2Phase) * 0.28f),
            radius = wave2R,
            center = radarCenter,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Rotating Segmented Orbital Compass Ring
        rotate(degrees = orbitAngle, pivot = radarCenter) {
            for (seg in 0 until 6) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF4ADE80).copy(alpha = 0.45f),
                            Color(0xFFFBBF24).copy(alpha = 0.35f),
                            Color(0xFF38BDF8).copy(alpha = 0.40f),
                            Color(0xFF4ADE80).copy(alpha = 0.45f)
                        ),
                        center = radarCenter
                    ),
                    startAngle = seg * 60f + 8f,
                    sweepAngle = 36f,
                    useCenter = false,
                    topLeft = Offset(radarCenter.x - baseRadius, radarCenter.y - baseRadius),
                    size = Size(baseRadius * 2f, baseRadius * 2f),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Orbiting Verified Professional Nodes & Connection Mesh
        val nodeCount = 6
        val nodePositions = mutableListOf<Offset>()
        for (i in 0 until nodeCount) {
            val rad = ((orbitAngle * 0.7f + i * 60f) * PI / 180.0).toFloat()
            val orbitR = baseRadius * (1.12f + (i % 2) * 0.22f)
            val nx = radarCenter.x + cos(rad) * orbitR
            val ny = radarCenter.y + sin(rad) * (orbitR * 0.75f)
            val pos = Offset(nx, ny)
            nodePositions.add(pos)

            drawLine(
                color = Color(0xFF6EE7B7).copy(alpha = 0.16f),
                start = radarCenter,
                end = pos,
                strokeWidth = 1.dp.toPx()
            )
            val dotColor = when (i % 3) {
                0 -> Color(0xFF4ADE80)
                1 -> Color(0xFFFBBF24)
                else -> Color(0xFF60A5FA)
            }
            drawCircle(
                color = dotColor.copy(alpha = 0.28f * ambientGlow),
                radius = 9.dp.toPx(),
                center = pos
            )
            drawCircle(
                color = dotColor,
                radius = 4.dp.toPx(),
                center = pos
            )
        }

        // Central Glowing Neighborhood Hub Pin
        drawCircle(
            color = Color(0xFF10B981).copy(alpha = 0.32f * ambientGlow),
            radius = 22.dp.toPx(),
            center = radarCenter
        )
        drawCircle(
            color = Color(0xFFECFDF5),
            radius = 7.dp.toPx(),
            center = radarCenter
        )

        // 4. Layered Modern Neighborhood Skyline & Warm-Lit Homes (Midground)
        // Distant Skyline Silhouette
        val distantBuildings = listOf(
            Triple(0.02f, 0.36f, 0.16f),
            Triple(0.16f, 0.31f, 0.15f),
            Triple(0.30f, 0.34f, 0.14f),
            Triple(0.46f, 0.28f, 0.17f),
            Triple(0.64f, 0.32f, 0.16f),
            Triple(0.81f, 0.29f, 0.18f)
        )
        distantBuildings.forEach { (startXFrac, topYFrac, widthFrac) ->
            val bx = w * startXFrac
            val by = h * topYFrac
            val bw = w * widthFrac
            val bh = h * 0.35f
            drawRoundRect(
                color = Color(0xFF132A1E).copy(alpha = 0.85f),
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
        }

        // Foreground Architectural Homes & Townhouses with Pitched Roofs & Warm Golden Windows
        val homes = listOf(
            Triple(0.05f, 0.43f, 0.26f),
            Triple(0.35f, 0.39f, 0.30f),
            Triple(0.68f, 0.42f, 0.27f)
        )
        homes.forEachIndexed { idx, (xFrac, yFrac, wFrac) ->
            val hx = w * xFrac
            val hy = h * yFrac
            val hw = w * wFrac
            val hh = h * 0.26f

            // Pitched Roof Triangle
            val roofPath = Path().apply {
                moveTo(hx - hw * 0.06f, hy)
                lineTo(hx + hw * 0.5f, hy - h * 0.055f)
                lineTo(hx + hw * 1.06f, hy)
                close()
            }
            drawPath(
                path = roofPath,
                color = if (idx == 1) Color(0xFF234B35) else Color(0xFF1B3B2A)
            )

            // Building Body
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1D3F2C),
                        Color(0xFF0F2418)
                    ),
                    startY = hy,
                    endY = hy + hh
                ),
                topLeft = Offset(hx, hy),
                size = Size(hw, hh),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Warm Amber Lit Windows inside each Home
            for (row in 0..2) {
                for (col in 0..1) {
                    val wx = hx + hw * (0.20f + col * 0.38f)
                    val wy = hy + hh * (0.12f + row * 0.22f)
                    val ww = hw * 0.22f
                    val wh = hh * 0.13f
                    val isLit = (idx + row + col) % 2 == 0
                    drawRoundRect(
                        color = if (isLit) {
                            Color(0xFFFDE68A).copy(alpha = 0.78f * ambientGlow)
                        } else {
                            Color(0xFF34D399).copy(alpha = 0.32f)
                        },
                        topLeft = Offset(wx, wy),
                        size = Size(ww, wh),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        // Lush Rolling Neighborhood Greenery Hills
        val hillPath = Path().apply {
            moveTo(0f, h * 0.56f)
            quadraticTo(w * 0.28f, h * 0.49f, w * 0.55f, h * 0.55f)
            quadraticTo(w * 0.82f, h * 0.60f, w, h * 0.53f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = hillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF163824),
                    Color(0xFF0A1910)
                )
            )
        )

        // 5. Cinematic Bottom-Up Dark Vignette Gradient (Preserves High-Contrast Typography Below)
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF050B07).copy(alpha = 0.18f),
                    0.28f to Color.Transparent,
                    0.48f to Color(0xFF09120C).copy(alpha = 0.45f),
                    0.64f to Color(0xFF08100A).copy(alpha = 0.82f),
                    0.80f to Color(0xFF070D09).copy(alpha = 0.94f),
                    1.0f to Color(0xFF050906).copy(alpha = 0.99f)
                )
            )
        )
    }
}
