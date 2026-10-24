package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.configureAppCheck
import com.example.domain.model.UserRole
import com.example.presentation.MainDestination
import com.example.presentation.NeighbourViewModel
import com.example.presentation.SubScreen
import com.example.presentation.components.LanguagePickerDialog
import com.example.presentation.screens.AiDiagnosisScreen
import com.example.presentation.screens.BookingsAndEscrowScreen
import com.example.presentation.screens.ChatAndQuoteScreen
import com.example.presentation.screens.ConsumerLoginScreen
import com.example.presentation.screens.EmergencySosScreen
import com.example.presentation.screens.HomeScreen
import com.example.presentation.screens.ProviderDetailScreen
import com.example.presentation.screens.ProviderProScreen
import com.example.presentation.screens.ServiceProviderRegistrationScreen
import com.example.presentation.screens.SplashScreen
import com.example.ui.theme.EmergencyCrimson
import com.example.ui.theme.NeighbourConnectTheme
import com.example.ui.theme.TrustIndigo
import com.example.ui.theme.VerifiedEmerald
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

private const val AUTH_TAG = "NeighbourConnectAuth"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        // STEP 2: Initialize UI
        setContent {
            NeighbourConnectTheme {
                NeighbourConnectRoot()
            }
        }
    }
}

private enum class RootStage {
    SPLASH,
    CONSUMER_LOGIN,
    MAIN_MARKETPLACE
}

@Composable
fun NeighbourConnectRoot(
    auth: FirebaseAuth = remember { FirebaseAuth.getInstance() }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showSplashScreen by rememberSaveable { mutableStateOf(true) }
    var selectedLoginRole by rememberSaveable { mutableStateOf(UserRole.CUSTOMER) }
    var isSigningInWithGoogle by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    val rootStage = when {
        showSplashScreen -> RootStage.SPLASH
        currentUser == null -> RootStage.CONSUMER_LOGIN
        else -> RootStage.MAIN_MARKETPLACE
    }

    Crossfade(
        targetState = rootStage,
        animationSpec = tween(500),
        label = "root_stage_crossfade"
    ) { stage ->
        when (stage) {
            RootStage.SPLASH -> {
                SplashScreen(
                    onEnterApp = { showSplashScreen = false }
                )
            }

            RootStage.CONSUMER_LOGIN -> {
                ConsumerLoginScreen(
                    isLoadingAuth = isSigningInWithGoogle,
                    authErrorMessage = authErrorMessage,
                    selectedLoginRole = selectedLoginRole,
                    onSelectLoginRole = { selectedLoginRole = it },
                    onGoogleIdTokenReceived = { idToken ->
                        isSigningInWithGoogle = true
                        authErrorMessage = null
                        val credential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                isSigningInWithGoogle = false
                                if (!task.isSuccessful) {
                                    Log.e(AUTH_TAG, "Firebase Google signInWithCredential failed", task.exception)
                                    authErrorMessage = task.exception?.localizedMessage
                                        ?: "Firebase authentication failed. Please try again."
                                }
                            }
                    },
                    onAuthError = { msg ->
                        isSigningInWithGoogle = false
                        authErrorMessage = msg
                    },
                    onDismissError = { authErrorMessage = null }
                )
            }

            RootStage.MAIN_MARKETPLACE -> {
                val activeUser = currentUser
                if (activeUser != null) {
                    AuthenticatedMarketplaceContent(
                        authenticatedUser = activeUser,
                        initialRolePreference = selectedLoginRole,
                        onReplayLogoSplash = { showSplashScreen = true },
                        onSignOut = {
                            coroutineScope.launch {
                                try {
                                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                } catch (e: Exception) {
                                    Log.w(AUTH_TAG, "Failed to clear credential manager state", e)
                                }
                                auth.signOut()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedMarketplaceContent(
    authenticatedUser: com.google.firebase.auth.FirebaseUser,
    initialRolePreference: UserRole,
    onReplayLogoSplash: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: NeighbourViewModel = viewModel(key = authenticatedUser.uid)
) {
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(authenticatedUser.uid) {
        viewModel.syncAuthenticatedConsumer(authenticatedUser, initialRolePreference)
    }

    val linkedConsumerProfile by viewModel.linkedConsumerProfile.collectAsStateWithLifecycle()
    val linkedProviderRegistration by viewModel.linkedProviderRegistration.collectAsStateWithLifecycle()
    val showRoleAndProviderKycGate by viewModel.showRoleAndProviderKycGate.collectAsStateWithLifecycle()
    val isSubmittingProviderKyc by viewModel.isSubmittingProviderKyc.collectAsStateWithLifecycle()
    val providerKycErrorMessage by viewModel.providerKycErrorMessage.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val cloudSyncedBookings by viewModel.cloudSyncedBookings.collectAsStateWithLifecycle()
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val subScreen by viewModel.subScreen.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val selectedNeighborhood by viewModel.selectedNeighborhood.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedGroup by viewModel.selectedGroup.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val minTrustScore by viewModel.minTrustScoreFilter.collectAsStateWithLifecycle()

    val allProviders by viewModel.allProviders.collectAsStateWithLifecycle()
    val filteredProviders by viewModel.filteredProviders.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val activeChatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()

    val selectedScenario by viewModel.selectedScenario.collectAsStateWithLifecycle()
    val diagnosisPrompt by viewModel.diagnosisPrompt.collectAsStateWithLifecycle()
    val hasAttachedPhoto by viewModel.hasAttachedFaultPhoto.collectAsStateWithLifecycle()
    val isAnalyzingDiagnosis by viewModel.isAnalyzingDiagnosis.collectAsStateWithLifecycle()
    val currentDiagnosisReport by viewModel.currentDiagnosisReport.collectAsStateWithLifecycle()

    val providerOnlineToggle by viewModel.providerOnlineToggle.collectAsStateWithLifecycle()
    val latestProfileAudit by viewModel.latestProfileAudit.collectAsStateWithLifecycle()
    val toastBannerMessage by viewModel.toastBannerMessage.collectAsStateWithLifecycle()

    if (showRoleAndProviderKycGate) {
        val displayName = authenticatedUser.displayName?.takeIf { it.isNotBlank() }
            ?: linkedConsumerProfile?.displayName?.takeIf { it.isNotBlank() }
            ?: authenticatedUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Verified User"
        val email = authenticatedUser.email?.takeIf { it.isNotBlank() }
            ?: linkedConsumerProfile?.email
            ?: "user@neighbourconnect.in"

        ServiceProviderRegistrationScreen(
            authenticatedUserName = displayName,
            authenticatedUserEmail = email,
            authenticatedUserUid = authenticatedUser.uid,
            initialSelectedRole = userRole,
            defaultNeighborhood = selectedNeighborhood,
            isSubmittingKyc = isSubmittingProviderKyc,
            submissionErrorMessage = providerKycErrorMessage,
            onContinueAsCustomer = viewModel::confirmCustomerRoleAndEnterMarketplace,
            onSubmitProviderKyc = { submission ->
                viewModel.submitServiceProviderKycRegistration(authenticatedUser, submission)
            },
            onSignOut = {
                viewModel.clearAuthenticatedConsumerSession()
                onSignOut()
            }
        )
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (subScreen is SubScreen.None) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentDestination == MainDestination.HOME,
                        onClick = { viewModel.navigateToDestination(MainDestination.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (currentDestination == MainDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Discover") },
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentDestination == MainDestination.AI_DIAGNOSIS,
                        onClick = { viewModel.navigateToDestination(MainDestination.AI_DIAGNOSIS) },
                        icon = {
                            Icon(
                                imageVector = if (currentDestination == MainDestination.AI_DIAGNOSIS) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                contentDescription = "AI Diagnose"
                            )
                        },
                        label = { Text("AI Diagnose") },
                        modifier = Modifier.testTag("nav_ai_diagnosis")
                    )

                    NavigationBarItem(
                        selected = currentDestination == MainDestination.EMERGENCY_SOS,
                        onClick = { viewModel.navigateToDestination(MainDestination.EMERGENCY_SOS) },
                        icon = {
                            Icon(
                                imageVector = if (currentDestination == MainDestination.EMERGENCY_SOS) Icons.Filled.Emergency else Icons.Outlined.Emergency,
                                contentDescription = "SOS",
                                tint = EmergencyCrimson
                            )
                        },
                        label = {
                            Text(
                                text = "SOS 24x7",
                                color = EmergencyCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = EmergencyCrimson.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier.testTag("nav_emergency_sos")
                    )

                    NavigationBarItem(
                        selected = currentDestination == MainDestination.BOOKINGS,
                        onClick = { viewModel.navigateToDestination(MainDestination.BOOKINGS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (bookings.isNotEmpty()) {
                                        Badge(containerColor = VerifiedEmerald) {
                                            Text(bookings.size.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentDestination == MainDestination.BOOKINGS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                    contentDescription = "Escrow & Bookings"
                                )
                            }
                        },
                        label = { Text("Escrow") },
                        modifier = Modifier.testTag("nav_bookings")
                    )

                    NavigationBarItem(
                        selected = currentDestination == MainDestination.PROVIDER_PRO,
                        onClick = { viewModel.navigateToDestination(MainDestination.PROVIDER_PRO) },
                        icon = {
                            Icon(
                                imageVector = if (currentDestination == MainDestination.PROVIDER_PRO) Icons.Filled.Badge else Icons.Outlined.Badge,
                                contentDescription = "Provider Pro"
                            )
                        },
                        label = { Text("Provider Pro") },
                        modifier = Modifier.testTag("nav_provider_pro")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val activeSub = subScreen) {
                is SubScreen.ProviderDetail -> {
                    val provider = allProviders.firstOrNull { it.id == activeSub.providerId }
                        ?: allProviders.firstOrNull()
                    if (provider != null) {
                        ProviderDetailScreen(
                            provider = provider,
                            onBack = { viewModel.closeSubScreen() },
                            onConfirmBooking = { chosenProvider, notes, slot, service ->
                                viewModel.createStandardBooking(chosenProvider, notes, slot, service)
                            }
                        )
                    }
                }

                is SubScreen.ChatSession -> {
                    val booking = bookings.firstOrNull { it.id == activeSub.bookingId }
                        ?: bookings.firstOrNull()
                    if (booking != null) {
                        ChatAndQuoteScreen(
                            booking = booking,
                            messages = activeChatMessages,
                            onBack = { viewModel.closeSubScreen() },
                            onSendMessage = { text ->
                                viewModel.sendChatMessage(booking.id, text)
                            },
                            onSubmitRevisedQuote = { lDesc, lInr, pDesc, pInr ->
                                viewModel.sendRevisedProviderQuote(
                                    booking.id,
                                    booking.providerName,
                                    lDesc,
                                    lInr,
                                    pDesc,
                                    pInr
                                )
                            },
                            onAcceptQuoteAndFundEscrow = { bk ->
                                viewModel.fundBookingEscrow(bk, "UPI Instant (Google Pay)")
                            }
                        )
                    }
                }

                SubScreen.None -> {
                    when (currentDestination) {
                        MainDestination.HOME -> HomeScreen(
                            providers = filteredProviders,
                            activeBookings = bookings,
                            selectedNeighborhood = selectedNeighborhood,
                            selectedLanguage = selectedLanguage,
                            searchQuery = searchQuery,
                            selectedGroup = selectedGroup,
                            selectedCategory = selectedCategory,
                            minTrustScore = minTrustScore,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onSelectGroup = viewModel::selectProfessionGroup,
                            onSelectCategory = viewModel::selectCategory,
                            onUpdateMinTrustScore = viewModel::updateMinTrustScore,
                            onSelectNeighborhood = viewModel::setNeighborhood,
                            onOpenLanguageDialog = { showLanguageDialog = true },
                            onSwitchToProviderMode = { viewModel.toggleUserRole(UserRole.PROVIDER) },
                            onNavigateToAiDiagnosis = {
                                viewModel.navigateToDestination(MainDestination.AI_DIAGNOSIS)
                            },
                            onNavigateToEmergencySos = {
                                viewModel.navigateToDestination(MainDestination.EMERGENCY_SOS)
                            },
                            onOpenProviderDetail = { id -> viewModel.openProviderDetail(id) },
                            onQuickBookProvider = { provider ->
                                viewModel.createStandardBooking(
                                    provider = provider,
                                    issueDescription = "Urgent ${provider.category.titleEn} visit & inspection",
                                    scheduledSlotLabel = "Instant Dispatch (Arrives in ${provider.responseTimeMinutes} mins)",
                                    selectedService = provider.services.firstOrNull()
                                )
                            },
                            onOpenBookingChat = { bookingId -> viewModel.openChatSession(bookingId) },
                            onReplayLogoSplash = onReplayLogoSplash,
                            linkedConsumerProfile = linkedConsumerProfile,
                            cloudBookingsCount = cloudSyncedBookings.size,
                            onSignOutConsumer = {
                                viewModel.clearAuthenticatedConsumerSession()
                                onSignOut()
                            }
                        )

                        MainDestination.AI_DIAGNOSIS -> AiDiagnosisScreen(
                            selectedScenario = selectedScenario,
                            diagnosisPrompt = diagnosisPrompt,
                            hasAttachedPhoto = hasAttachedPhoto,
                            isAnalyzing = isAnalyzingDiagnosis,
                            report = currentDiagnosisReport,
                            onSelectScenario = viewModel::selectDiagnosticScenario,
                            onPromptChange = viewModel::updateDiagnosisPrompt,
                            onTogglePhotoAttached = viewModel::toggleFaultPhotoAttached,
                            onRunDiagnosis = viewModel::runAiProblemDiagnosis,
                            onBookFromDiagnosis = viewModel::bookSpecialistFromDiagnosis
                        )

                        MainDestination.EMERGENCY_SOS -> EmergencySosScreen(
                            neighborhood = selectedNeighborhood,
                            allProviders = allProviders,
                            onTriggerEmergencyDispatch = viewModel::dispatchEmergencySos
                        )

                        MainDestination.BOOKINGS -> BookingsAndEscrowScreen(
                            bookings = bookings,
                            onOpenChat = { id -> viewModel.openChatSession(id) },
                            onFundRazorpayEscrow = viewModel::fundBookingEscrow,
                            onMarkProviderArrived = viewModel::markProviderArrivedAndInProgress,
                            onVerifyOtpAndRelease = viewModel::verifyCompletionOtpAndReleaseEscrow
                        )

                        MainDestination.PROVIDER_PRO -> ProviderProScreen(
                            isOnline = providerOnlineToggle,
                            bookings = bookings,
                            latestAudit = latestProfileAudit,
                            onToggleOnline = viewModel::toggleProviderOnlineStatus,
                            onRunProfileAudit = viewModel::runAiProfileQualityAudit,
                            onRegisterProvider = viewModel::submitProviderSelfRegistration,
                            onSwitchToCustomerMode = { viewModel.toggleUserRole(UserRole.CUSTOMER) },
                            verifiedKycRegistration = linkedProviderRegistration,
                            onOpenFullKycRegistrationScreen = viewModel::openServiceProviderKycRegistrationGate
                        )
                    }
                }
            }

            // Floating Confirmation Toast Banner
            AnimatedVisibility(
                visible = toastBannerMessage != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TrustIndigo,
                    contentColor = Color.White,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.dismissBanner() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = toastBannerMessage.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF93C5FD),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = selectedLanguage,
            onSelectLanguage = viewModel::setLanguage,
            onDismiss = { showLanguageDialog = false }
        )
    }
}
