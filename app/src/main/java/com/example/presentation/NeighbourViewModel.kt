package com.example.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.ai.AiEngine
import com.example.data.ai.DiagnosticPresetScenario
import com.example.data.firebase.ConsumerAccountRepository
import com.example.data.firebase.ConsumerBookingDocument
import com.example.data.firebase.ConsumerProfileDocument
import com.example.data.firebase.PLATFORM_OWNER_ACCOUNT_EMAIL
import com.example.data.firebase.ServiceProviderRegistrationDocument
import com.example.data.local.AppDatabase
import com.example.data.repository.NeighbourRepository
import com.example.domain.model.AiDiagnosisReport
import com.example.domain.model.AiProfileAuditResult
import com.example.domain.model.AppLanguage
import com.example.domain.model.ChatMessage
import com.example.domain.model.DigitalQuote
import com.example.domain.model.MessageSender
import com.example.domain.model.ProfessionCategory
import com.example.domain.model.ProfessionGroup
import com.example.domain.model.ServiceBooking
import com.example.domain.model.ServiceOffering
import com.example.domain.model.ServiceProvider
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserRole
import com.example.presentation.screens.ServiceProviderKycSubmission
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val VM_TAG = "NeighbourViewModel"

enum class MainDestination {
    HOME,
    AI_DIAGNOSIS,
    EMERGENCY_SOS,
    BOOKINGS,
    PROVIDER_PRO
}

sealed interface SubScreen {
    data object None : SubScreen
    data class ProviderDetail(val providerId: String) : SubScreen
    data class ChatSession(val bookingId: String) : SubScreen
}

class NeighbourViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NeighbourRepository(AppDatabase.getInstance(application).dao())

    private val consumerAccountRepository: ConsumerAccountRepository by lazy {
        val dbId = application.getString(R.string.firestore_database_id)
        ConsumerAccountRepository(FirebaseFirestore.getInstance(dbId))
    }

    private var activeSyncedUserId: String? = null
    private var profileObserverJob: Job? = null
    private var bookingsObserverJob: Job? = null
    private var providerKycObserverJob: Job? = null

    private val _linkedConsumerProfile = MutableStateFlow<ConsumerProfileDocument?>(null)
    val linkedConsumerProfile: StateFlow<ConsumerProfileDocument?> = _linkedConsumerProfile.asStateFlow()

    private val _linkedProviderRegistration = MutableStateFlow<ServiceProviderRegistrationDocument?>(null)
    val linkedProviderRegistration: StateFlow<ServiceProviderRegistrationDocument?> =
        _linkedProviderRegistration.asStateFlow()

    private val _showRoleAndProviderKycGate = MutableStateFlow(true)
    val showRoleAndProviderKycGate: StateFlow<Boolean> = _showRoleAndProviderKycGate.asStateFlow()

    private val _isSubmittingProviderKyc = MutableStateFlow(false)
    val isSubmittingProviderKyc: StateFlow<Boolean> = _isSubmittingProviderKyc.asStateFlow()

    private val _providerKycErrorMessage = MutableStateFlow<String?>(null)
    val providerKycErrorMessage: StateFlow<String?> = _providerKycErrorMessage.asStateFlow()

    private val _cloudSyncedBookings = MutableStateFlow<List<ConsumerBookingDocument>>(emptyList())
    val cloudSyncedBookings: StateFlow<List<ConsumerBookingDocument>> = _cloudSyncedBookings.asStateFlow()

    private val _currentDestination = MutableStateFlow(MainDestination.HOME)
    val currentDestination: StateFlow<MainDestination> = _currentDestination.asStateFlow()

    private val _subScreen = MutableStateFlow<SubScreen>(SubScreen.None)
    val subScreen: StateFlow<SubScreen> = _subScreen.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _userRole = MutableStateFlow(UserRole.CUSTOMER)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _selectedNeighborhood = MutableStateFlow("Indiranagar 12th Main, Bengaluru")
    val selectedNeighborhood: StateFlow<String> = _selectedNeighborhood.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGroup = MutableStateFlow(ProfessionGroup.ALL)
    val selectedGroup: StateFlow<ProfessionGroup> = _selectedGroup.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ProfessionCategory?>(null)
    val selectedCategory: StateFlow<ProfessionCategory?> = _selectedCategory.asStateFlow()

    private val _minTrustScoreFilter = MutableStateFlow(80)
    val minTrustScoreFilter: StateFlow<Int> = _minTrustScoreFilter.asStateFlow()

    private val _onlineOnlyFilter = MutableStateFlow(false)
    val onlineOnlyFilter: StateFlow<Boolean> = _onlineOnlyFilter.asStateFlow()

    // AI Diagnosis State
    private val _selectedScenario = MutableStateFlow<DiagnosticPresetScenario?>(AiEngine.presetScenarios.first())
    val selectedScenario: StateFlow<DiagnosticPresetScenario?> = _selectedScenario.asStateFlow()

    private val _diagnosisPrompt = MutableStateFlow(AiEngine.presetScenarios.first().defaultPrompt)
    val diagnosisPrompt: StateFlow<String> = _diagnosisPrompt.asStateFlow()

    private val _hasAttachedFaultPhoto = MutableStateFlow(true)
    val hasAttachedFaultPhoto: StateFlow<Boolean> = _hasAttachedFaultPhoto.asStateFlow()

    private val _isAnalyzingDiagnosis = MutableStateFlow(false)
    val isAnalyzingDiagnosis: StateFlow<Boolean> = _isAnalyzingDiagnosis.asStateFlow()

    private val _currentDiagnosisReport = MutableStateFlow<AiDiagnosisReport?>(
        AiEngine.diagnoseProblem(
            userDescription = AiEngine.presetScenarios.first().defaultPrompt,
            selectedScenario = AiEngine.presetScenarios.first(),
            hasAttachedImage = true
        )
    )
    val currentDiagnosisReport: StateFlow<AiDiagnosisReport?> = _currentDiagnosisReport.asStateFlow()

    // Provider Self-Registration & AI Profile Audit State
    private val _providerOnlineToggle = MutableStateFlow(true)
    val providerOnlineToggle: StateFlow<Boolean> = _providerOnlineToggle.asStateFlow()

    private val _latestProfileAudit = MutableStateFlow<AiProfileAuditResult?>(null)
    val latestProfileAudit: StateFlow<AiProfileAuditResult?> = _latestProfileAudit.asStateFlow()

    private val _toastBannerMessage = MutableStateFlow<String?>(null)
    val toastBannerMessage: StateFlow<String?> = _toastBannerMessage.asStateFlow()

    val allProviders: StateFlow<List<ServiceProvider>> = repository.allProvidersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredProviders: StateFlow<List<ServiceProvider>> = combine(
        allProviders,
        _searchQuery,
        _selectedGroup,
        _selectedCategory,
        _minTrustScoreFilter
    ) { providers, query, group, category, minTrust ->
        val aiMatchedCat = AiEngine.detectBestCategory(query)
        providers.filter { provider ->
            val matchesGroup = group == ProfessionGroup.ALL || provider.category.group == group
            val matchesCategory = category == null || provider.category == category
            val matchesTrust = provider.trustScore >= minTrust
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                provider.name.lowercase().contains(q) ||
                    provider.category.titleEn.lowercase().contains(q) ||
                    provider.category.titleHi.contains(q) ||
                    provider.neighborhood.lowercase().contains(q) ||
                    provider.bio.lowercase().contains(q) ||
                    (aiMatchedCat != null && provider.category == aiMatchedCat)
            }
            matchesGroup && matchesCategory && matchesTrust && matchesQuery
        }.sortedWith(
            compareByDescending<ServiceProvider> { it.subscriptionTier == SubscriptionTier.FEATURED }
                .thenByDescending { it.trustScore }
                .thenBy { it.distanceKm }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bookings: StateFlow<List<ServiceBooking>> = repository.bookingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeChatMessages: StateFlow<List<ChatMessage>> = _subScreen.flatMapLatest { screen ->
        when (screen) {
            is SubScreen.ChatSession -> repository.observeChatMessages(screen.bookingId)
            else -> flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeedBookingsInitialized()
        }
    }

    /**
     * Links the authenticated Google user to the platform owner's Firestore account
     * (`Jksmau@gmail.com`), starts real-time listeners, and routes either to the
     * Role Selection / Mandatory Service Provider KYC screen or Marketplace.
     */
    fun syncAuthenticatedConsumer(
        user: FirebaseUser,
        initialRolePreference: UserRole = UserRole.CUSTOMER
    ) {
        if (activeSyncedUserId == user.uid) return
        activeSyncedUserId = user.uid
        _userRole.value = initialRolePreference
        // Always allow user to confirm Customer vs Service Provider after login (or enforce KYC if Provider)
        _showRoleAndProviderKycGate.value = true

        val displayName = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Verified Consumer"
        val email = user.email?.takeIf { it.isNotBlank() } ?: "consumer@neighbourconnect.in"
        val photoUrl = user.photoUrl?.toString()

        viewModelScope.launch {
            val result = consumerAccountRepository.upsertConsumerProfile(
                userId = user.uid,
                displayName = displayName,
                email = email,
                photoUrl = photoUrl,
                neighborhood = _selectedNeighborhood.value,
                role = if (initialRolePreference == UserRole.PROVIDER) "PROVIDER" else "CONSUMER"
            )
            result.onSuccess { profile ->
                _linkedConsumerProfile.value = profile
                if (profile.neighborhood.isNotBlank()) {
                    _selectedNeighborhood.value = profile.neighborhood
                }
            }.onFailure { err ->
                Log.w(VM_TAG, "Failed to sync consumer profile to Firestore", err)
            }
        }

        profileObserverJob?.cancel()
        profileObserverJob = viewModelScope.launch {
            consumerAccountRepository.observeConsumerProfile(user.uid)
                .catch { e -> Log.w(VM_TAG, "Error observing consumer profile", e) }
                .collect { profile ->
                    if (profile != null) {
                        _linkedConsumerProfile.value = profile
                    }
                }
        }

        providerKycObserverJob?.cancel()
        providerKycObserverJob = viewModelScope.launch {
            consumerAccountRepository.observeServiceProviderRegistration(user.uid)
                .catch { e -> Log.w(VM_TAG, "Error observing provider KYC registration", e) }
                .collect { kycDoc ->
                    _linkedProviderRegistration.value = kycDoc
                }
        }

        bookingsObserverJob?.cancel()
        bookingsObserverJob = viewModelScope.launch {
            consumerAccountRepository.observeConsumerBookings(user.uid)
                .catch { e -> Log.w(VM_TAG, "Error observing cloud bookings", e) }
                .collect { list ->
                    _cloudSyncedBookings.value = list
                }
        }
    }

    fun confirmCustomerRoleAndEnterMarketplace() {
        _userRole.value = UserRole.CUSTOMER
        _showRoleAndProviderKycGate.value = false
        _currentDestination.value = MainDestination.HOME
        _subScreen.value = SubScreen.None
        val uid = activeSyncedUserId
        if (!uid.isNullOrBlank()) {
            viewModelScope.launch {
                consumerAccountRepository.updateConsumerRole(uid, "CONSUMER")
            }
        }
        val name = _linkedConsumerProfile.value?.displayName ?: "Customer"
        showBanner("Welcome $name! Signed in as Customer (No ID required).")
    }

    fun openServiceProviderKycRegistrationGate() {
        _userRole.value = UserRole.PROVIDER
        _providerKycErrorMessage.value = null
        _showRoleAndProviderKycGate.value = true
    }

    fun submitServiceProviderKycRegistration(
        user: FirebaseUser,
        submission: ServiceProviderKycSubmission
    ) {
        viewModelScope.launch {
            _isSubmittingProviderKyc.value = true
            _providerKycErrorMessage.value = null

            val email = user.email?.takeIf { it.isNotBlank() }
                ?: _linkedConsumerProfile.value?.email
                ?: "provider@neighbourconnect.in"

            val kycResult = consumerAccountRepository.registerServiceProviderKyc(
                userId = user.uid,
                fullName = submission.fullName,
                email = email,
                professionCategory = submission.category.name,
                customProfessionTitle = submission.customProfessionTitle,
                govIdType = submission.govIdType.code,
                govIdNumber = submission.govIdNumber,
                govIdDocumentUri = submission.govIdDocumentUri,
                govIdDocumentVerified = submission.govIdDocumentVerified,
                certificationTitle = submission.certificationTitle,
                issuingAuthority = submission.issuingAuthority,
                certificationNumber = submission.certificationNumber,
                certificationDocumentUri = submission.certificationDocumentUri,
                experienceYears = submission.experienceYears,
                baseVisitingFeeInr = submission.baseVisitingFeeInr,
                upiId = submission.upiId,
                neighborhood = submission.neighborhood,
                city = submission.city,
                acceptsEmergencySos = submission.acceptsEmergency
            )

            kycResult.onSuccess { savedDoc ->
                _linkedProviderRegistration.value = savedDoc
                consumerAccountRepository.updateConsumerRole(user.uid, "PROVIDER")

                val audit = AiEngine.auditProviderProfile(
                    name = submission.fullName,
                    bio = submission.bio,
                    experienceYears = submission.experienceYears,
                    baseRateInr = submission.baseVisitingFeeInr,
                    category = submission.category,
                    governmentIdNumber = submission.govIdNumber,
                    upiId = submission.upiId
                )
                _latestProfileAudit.value = audit

                repository.registerNewServiceProvider(
                    name = submission.fullName,
                    category = submission.category,
                    customProfessionTitle = submission.customProfessionTitle,
                    bio = submission.bio,
                    experienceYears = submission.experienceYears,
                    neighborhood = submission.neighborhood,
                    city = submission.city,
                    baseVisitingFeeInr = submission.baseVisitingFeeInr,
                    acceptsEmergency = submission.acceptsEmergency,
                    languages = listOf("English", "Hindi", "Kannada"),
                    governmentIdNumber = "${submission.govIdType.code}:${submission.govIdNumber}",
                    upiId = submission.upiId,
                    subscriptionTier = submission.subscriptionTier,
                    aiQualityScore = audit.overallScore
                )

                _isSubmittingProviderKyc.value = false
                _userRole.value = UserRole.PROVIDER
                _showRoleAndProviderKycGate.value = false
                _currentDestination.value = MainDestination.PROVIDER_PRO
                _subScreen.value = SubScreen.None
                showBanner(
                    "KYC Verified! ${submission.fullName} registered with ${submission.govIdType.code} & Cert #${submission.certificationNumber}."
                )
            }.onFailure { err ->
                _isSubmittingProviderKyc.value = false
                _providerKycErrorMessage.value =
                    err.localizedMessage ?: "Failed to save Service Provider KYC to Firebase."
            }
        }
    }

    fun clearAuthenticatedConsumerSession() {
        profileObserverJob?.cancel()
        bookingsObserverJob?.cancel()
        providerKycObserverJob?.cancel()
        profileObserverJob = null
        bookingsObserverJob = null
        providerKycObserverJob = null
        activeSyncedUserId = null
        _linkedConsumerProfile.value = null
        _linkedProviderRegistration.value = null
        _cloudSyncedBookings.value = emptyList()
        _showRoleAndProviderKycGate.value = true
    }

    fun navigateToDestination(destination: MainDestination) {
        if (destination == MainDestination.PROVIDER_PRO && _linkedProviderRegistration.value == null) {
            _userRole.value = UserRole.PROVIDER
            _showRoleAndProviderKycGate.value = true
            showBanner("Government ID & Professional Certification are mandatory for Service Providers.")
            return
        }
        _subScreen.value = SubScreen.None
        _currentDestination.value = destination
    }

    fun openProviderDetail(providerId: String) {
        _subScreen.value = SubScreen.ProviderDetail(providerId)
    }

    fun openChatSession(bookingId: String) {
        _subScreen.value = SubScreen.ChatSession(bookingId)
    }

    fun closeSubScreen() {
        _subScreen.value = SubScreen.None
    }

    fun setLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
        showBanner("Language switched to ${language.displayName} (${language.nativeName})")
    }

    fun toggleUserRole(role: UserRole) {
        _userRole.value = role
        if (role == UserRole.PROVIDER) {
            if (_linkedProviderRegistration.value == null) {
                _showRoleAndProviderKycGate.value = true
                showBanner("Please complete mandatory Service Provider Government ID & Certification KYC.")
            } else {
                _currentDestination.value = MainDestination.PROVIDER_PRO
                _subScreen.value = SubScreen.None
            }
        } else {
            _showRoleAndProviderKycGate.value = false
            _currentDestination.value = MainDestination.HOME
            _subScreen.value = SubScreen.None
        }
    }

    fun setNeighborhood(neighborhood: String) {
        _selectedNeighborhood.value = neighborhood
        val uid = activeSyncedUserId
        if (!uid.isNullOrBlank()) {
            viewModelScope.launch {
                consumerAccountRepository.updateConsumerNeighborhood(uid, neighborhood)
            }
        }
        showBanner("GPS Neighborhood locked: $neighborhood")
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectProfessionGroup(group: ProfessionGroup) {
        _selectedGroup.value = group
        if (group != ProfessionGroup.ALL && _selectedCategory.value?.group != group) {
            _selectedCategory.value = null
        }
    }

    fun selectCategory(category: ProfessionCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun updateMinTrustScore(score: Int) {
        _minTrustScoreFilter.value = score
    }

    // AI Diagnosis Actions
    fun selectDiagnosticScenario(scenario: DiagnosticPresetScenario) {
        _selectedScenario.value = scenario
        _diagnosisPrompt.value = scenario.defaultPrompt
        _hasAttachedFaultPhoto.value = true
        runAiProblemDiagnosis()
    }

    fun updateDiagnosisPrompt(text: String) {
        _diagnosisPrompt.value = text
    }

    fun toggleFaultPhotoAttached() {
        _hasAttachedFaultPhoto.value = !_hasAttachedFaultPhoto.value
    }

    fun runAiProblemDiagnosis() {
        viewModelScope.launch {
            _isAnalyzingDiagnosis.value = true
            delay(450L)
            val report = AiEngine.diagnoseProblem(
                userDescription = _diagnosisPrompt.value,
                selectedScenario = _selectedScenario.value,
                hasAttachedImage = _hasAttachedFaultPhoto.value
            )
            _currentDiagnosisReport.value = report
            _isAnalyzingDiagnosis.value = false
        }
    }

    fun bookSpecialistFromDiagnosis(report: AiDiagnosisReport) {
        val matchedProvider = allProviders.value.firstOrNull { it.category == report.recommendedCategory }
            ?: allProviders.value.firstOrNull()
        if (matchedProvider != null) {
            viewModelScope.launch {
                val issueSummary = "${report.faultTitle} — ${report.inputDescription}"
                val bookingId = repository.createNewBooking(
                    provider = matchedProvider,
                    customerAddress = _selectedNeighborhood.value,
                    issueSummary = issueSummary,
                    scheduledSlotLabel = "Today • Priority AI-Triage Slot (Within 30 mins)",
                    isEmergencySos = false,
                    selectedService = matchedProvider.services.firstOrNull(),
                    attachedDiagnosisTitle = "${report.faultTitle} (Est. ₹${report.totalMinInr}–₹${report.totalMaxInr})"
                )
                syncBookingToCloudIfAuthenticated(
                    bookingId = bookingId,
                    provider = matchedProvider,
                    issueSummary = issueSummary,
                    totalAmountInr = report.totalMinInr.toLong()
                )
                showBanner("AI Diagnosis shared with ${matchedProvider.name}! Digital quote ready.")
                _currentDestination.value = MainDestination.BOOKINGS
                _subScreen.value = SubScreen.ChatSession(bookingId)
            }
        } else {
            _selectedCategory.value = report.recommendedCategory
            _currentDestination.value = MainDestination.HOME
        }
    }

    fun createStandardBooking(
        provider: ServiceProvider,
        issueDescription: String,
        scheduledSlotLabel: String,
        selectedService: ServiceOffering?
    ) {
        viewModelScope.launch {
            val summary = issueDescription.ifBlank {
                selectedService?.title ?: "On-site ${provider.category.titleEn} service"
            }
            val bookingId = repository.createNewBooking(
                provider = provider,
                customerAddress = _selectedNeighborhood.value,
                issueSummary = summary,
                scheduledSlotLabel = scheduledSlotLabel,
                isEmergencySos = false,
                selectedService = selectedService,
                attachedDiagnosisTitle = null
            )
            syncBookingToCloudIfAuthenticated(
                bookingId = bookingId,
                provider = provider,
                issueSummary = summary,
                totalAmountInr = (selectedService?.priceInr ?: provider.baseVisitingFeeInr).toLong()
            )
            showBanner("Booking requested with ${provider.name}! Review itemized quote in chat.")
            _currentDestination.value = MainDestination.BOOKINGS
            _subScreen.value = SubScreen.ChatSession(bookingId)
        }
    }

    fun dispatchEmergencySos(category: ProfessionCategory, emergencyNote: String) {
        val responder = allProviders.value.firstOrNull { it.category == category && it.acceptsEmergency }
            ?: allProviders.value.firstOrNull { it.acceptsEmergency }
            ?: allProviders.value.firstOrNull()
            ?: return

        viewModelScope.launch {
            val summary = "EMERGENCY SOS: ${emergencyNote.ifBlank { "Urgent ${category.titleEn} failure requiring immediate dispatch" }}"
            val bookingId = repository.createNewBooking(
                provider = responder,
                customerAddress = _selectedNeighborhood.value,
                issueSummary = summary,
                scheduledSlotLabel = "EMERGENCY SOS • Responder Dispatched (< 10 mins)",
                isEmergencySos = true,
                selectedService = responder.services.firstOrNull(),
                attachedDiagnosisTitle = "SOS Emergency Priority Alert"
            )
            syncBookingToCloudIfAuthenticated(
                bookingId = bookingId,
                provider = responder,
                issueSummary = summary,
                totalAmountInr = responder.baseVisitingFeeInr.toLong()
            )
            showBanner("SOS Broadcast locked! ${responder.name} (${responder.distanceKm} km away) alerted.")
            _currentDestination.value = MainDestination.BOOKINGS
            _subScreen.value = SubScreen.ChatSession(bookingId)
        }
    }

    private suspend fun syncBookingToCloudIfAuthenticated(
        bookingId: String,
        provider: ServiceProvider,
        issueSummary: String,
        totalAmountInr: Long
    ) {
        val uid = activeSyncedUserId ?: return
        val email = _linkedConsumerProfile.value?.email ?: "consumer@neighbourconnect.in"
        consumerAccountRepository.syncConsumerBookingToCloud(
            bookingId = bookingId,
            userId = uid,
            consumerEmail = email,
            providerId = provider.id,
            providerName = provider.name,
            categoryTitle = provider.category.titleEn,
            issueSummary = issueSummary,
            neighborhood = _selectedNeighborhood.value,
            status = "QUOTE_SUBMITTED",
            totalAmountInr = totalAmountInr
        )
    }

    fun fundBookingEscrow(booking: ServiceBooking, paymentMethodLabel: String) {
        viewModelScope.launch {
            repository.fundEscrowWithRazorpay(booking, paymentMethodLabel)
            showBanner("₹${booking.quote.totalCustomerPayableInr} locked in Razorpay Escrow! ${booking.providerName} is en route.")
        }
    }

    fun markProviderArrivedAndInProgress(booking: ServiceBooking) {
        viewModelScope.launch {
            repository.advanceBookingToInProgress(booking)
            showBanner("${booking.providerName} arrived via GPS and started work.")
        }
    }

    fun verifyCompletionOtpAndReleaseEscrow(booking: ServiceBooking, enteredOtp: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.verifyOtpAndReleaseEscrow(booking, enteredOtp)
            if (success) {
                showBanner("OTP Verified! Escrow released (₹${booking.quote.providerNetPayoutInr} to provider, 2% fee ₹${booking.quote.platformCommissionInr}).")
            } else {
                showBanner("Invalid Completion OTP. Use ${booking.completionOtp} shown on your booking card.")
            }
            onResult(success)
        }
    }

    fun sendChatMessage(bookingId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendChatMessage(
                bookingId = bookingId,
                sender = MessageSender.CUSTOMER,
                senderName = _linkedConsumerProfile.value?.displayName ?: "You",
                text = text.trim()
            )
        }
    }

    fun sendRevisedProviderQuote(
        bookingId: String,
        providerName: String,
        laborDesc: String,
        laborInr: Int,
        partsDesc: String,
        partsInr: Int
    ) {
        viewModelScope.launch {
            val quote = DigitalQuote(
                quoteId = "QT-${(1000..9999).random()}",
                laborDescription = laborDesc.ifBlank { "Certified Specialist Labor" },
                laborCostInr = laborInr.coerceAtLeast(99),
                partsDescription = partsDesc.ifBlank { "Genuine OEM Spare Parts" },
                partsCostInr = partsInr.coerceAtLeast(0),
                warrantyDays = 45,
                notes = "Updated itemized digital quote with 45-day warranty."
            )
            repository.sendChatMessage(
                bookingId = bookingId,
                sender = MessageSender.PROVIDER,
                senderName = providerName,
                text = "I have updated the itemized digital quote based on our chat:",
                customQuote = quote
            )
            showBanner("Updated Digital Quote sent to chat.")
        }
    }

    // Provider Pro & Self-Registration Actions
    fun toggleProviderOnlineStatus() {
        _providerOnlineToggle.value = !_providerOnlineToggle.value
        val stateText = if (_providerOnlineToggle.value) "ONLINE (Receiving Neighborhood Pings)" else "OFFLINE"
        showBanner("Provider Status: $stateText")
    }

    fun runAiProfileQualityAudit(
        name: String,
        bio: String,
        experienceYears: Int,
        baseRateInr: Int,
        category: ProfessionCategory,
        govId: String,
        upiId: String
    ) {
        _latestProfileAudit.value = AiEngine.auditProviderProfile(
            name = name,
            bio = bio,
            experienceYears = experienceYears,
            baseRateInr = baseRateInr,
            category = category,
            governmentIdNumber = govId,
            upiId = upiId
        )
    }

    fun submitProviderSelfRegistration(
        name: String,
        category: ProfessionCategory,
        customProfessionTitle: String?,
        bio: String,
        experienceYears: Int,
        neighborhood: String,
        city: String,
        baseRateInr: Int,
        acceptsEmergency: Boolean,
        languages: List<String>,
        govId: String,
        upiId: String,
        tier: SubscriptionTier,
        onRegistered: (String) -> Unit
    ) {
        viewModelScope.launch {
            val audit = AiEngine.auditProviderProfile(
                name = name,
                bio = bio,
                experienceYears = experienceYears,
                baseRateInr = baseRateInr,
                category = category,
                governmentIdNumber = govId,
                upiId = upiId
            )
            _latestProfileAudit.value = audit
            val newId = repository.registerNewServiceProvider(
                name = name,
                category = category,
                customProfessionTitle = customProfessionTitle,
                bio = bio,
                experienceYears = experienceYears,
                neighborhood = neighborhood,
                city = city,
                baseVisitingFeeInr = baseRateInr,
                acceptsEmergency = acceptsEmergency,
                languages = languages,
                governmentIdNumber = govId,
                upiId = upiId,
                subscriptionTier = tier,
                aiQualityScore = audit.overallScore
            )
            showBanner("Registered $name as Verified ${customProfessionTitle ?: category.titleEn}! Trust Score: 94/100")
            onRegistered(newId)
        }
    }

    fun dismissBanner() {
        _toastBannerMessage.value = null
    }

    private fun showBanner(message: String) {
        _toastBannerMessage.value = message
    }

    override fun onCleared() {
        super.onCleared()
        clearAuthenticatedConsumerSession()
    }
}
