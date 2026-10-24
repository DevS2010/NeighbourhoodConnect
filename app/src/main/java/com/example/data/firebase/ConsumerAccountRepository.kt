package com.example.data.firebase

import android.content.Context
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

const val PLATFORM_OWNER_ACCOUNT_EMAIL = "Jksmau@gmail.com"

@IgnoreExtraProperties
data class ConsumerProfileDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val neighborhood: String = "Indiranagar 12th Main, Bengaluru",
    val linkedAccountOwnerEmail: String = PLATFORM_OWNER_ACCOUNT_EMAIL,
    val role: String = "CONSUMER",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ConsumerBookingDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val consumerEmail: String = "",
    val linkedAccountOwnerEmail: String = PLATFORM_OWNER_ACCOUNT_EMAIL,
    val providerId: String = "",
    val providerName: String = "",
    val categoryTitle: String = "",
    val issueSummary: String = "",
    val neighborhood: String = "",
    val status: String = "QUOTE_SUBMITTED",
    val totalAmountInr: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ServiceProviderRegistrationDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val professionCategory: String = "",
    val customProfessionTitle: String = "",
    val govIdType: String = "AADHAAR",
    val govIdNumber: String = "",
    val govIdDocumentUri: String = "",
    val govIdDocumentVerified: Boolean = false,
    val certificationTitle: String = "",
    val issuingAuthority: String = "",
    val certificationNumber: String = "",
    val certificationDocumentUri: String? = null,
    val experienceYears: Long = 0L,
    val baseVisitingFeeInr: Long = 199L,
    val upiId: String = "",
    val neighborhood: String = "Indiranagar 12th Main, Bengaluru",
    val city: String = "Bengaluru",
    val acceptsEmergencySos: Boolean = true,
    val linkedAccountOwnerEmail: String = PLATFORM_OWNER_ACCOUNT_EMAIL,
    val kycStatus: String = "VERIFIED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class ConsumerAccountRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    suspend fun upsertConsumerProfile(
        userId: String,
        displayName: String,
        email: String,
        photoUrl: String? = null,
        neighborhood: String = "Indiranagar 12th Main, Bengaluru",
        role: String = "CONSUMER"
    ): Result<ConsumerProfileDocument> = runCatching {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val docRef = db.collection("consumers").document(userId)
        val existingSnap = try {
            docRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

        val normalizedRole = if (role == "PROVIDER") "PROVIDER" else "CONSUMER"

        if (existingSnap != null && existingSnap.exists()) {
            val updatePayload = mutableMapOf<String, Any>(
                "displayName" to displayName.ifBlank { "NeighbourConnect Consumer" }.take(120),
                "email" to email.ifBlank { "consumer@neighbourconnect.in" }.take(254),
                "neighborhood" to neighborhood.ifBlank { "Indiranagar 12th Main, Bengaluru" }.take(200),
                "role" to normalizedRole,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!photoUrl.isNullOrBlank()) {
                updatePayload["photoUrl"] = photoUrl.take(1024)
            }
            try {
                docRef.set(updatePayload, SetOptions.merge()).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        } else {
            val createPayload = mutableMapOf<String, Any>(
                "userId" to userId,
                "displayName" to displayName.ifBlank { "NeighbourConnect Consumer" }.take(120),
                "email" to email.ifBlank { "consumer@neighbourconnect.in" }.take(254),
                "neighborhood" to neighborhood.ifBlank { "Indiranagar 12th Main, Bengaluru" }.take(200),
                "linkedAccountOwnerEmail" to PLATFORM_OWNER_ACCOUNT_EMAIL,
                "role" to normalizedRole,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!photoUrl.isNullOrBlank()) {
                createPayload["photoUrl"] = photoUrl.take(1024)
            }
            try {
                docRef.set(createPayload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
        }

        val savedSnap = try {
            docRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
        savedSnap.toObject(
            ConsumerProfileDocument::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ) ?: ConsumerProfileDocument(
            id = userId,
            userId = userId,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl,
            neighborhood = neighborhood,
            linkedAccountOwnerEmail = PLATFORM_OWNER_ACCOUNT_EMAIL,
            role = normalizedRole
        )
    }

    suspend fun updateConsumerRole(userId: String, role: String): Result<Unit> = runCatching {
        val docRef = db.collection("consumers").document(userId)
        val normalizedRole = if (role == "PROVIDER") "PROVIDER" else "CONSUMER"
        try {
            docRef.update(
                mapOf(
                    "role" to normalizedRole,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun getConsumerProfile(userId: String): Result<ConsumerProfileDocument?> = runCatching {
        val docRef = db.collection("consumers").document(userId)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(
                    ConsumerProfileDocument::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    fun observeConsumerProfile(userId: String): Flow<ConsumerProfileDocument?> {
        val docRef = db.collection("consumers").document(userId)
        return docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(
                    ConsumerProfileDocument::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
        }
    }

    suspend fun updateConsumerNeighborhood(userId: String, neighborhood: String): Result<Unit> = runCatching {
        val docRef = db.collection("consumers").document(userId)
        try {
            docRef.update(
                mapOf(
                    "neighborhood" to neighborhood.take(200),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun registerServiceProviderKyc(
        userId: String,
        fullName: String,
        email: String,
        professionCategory: String,
        customProfessionTitle: String? = null,
        govIdType: String,
        govIdNumber: String,
        govIdDocumentUri: String,
        govIdDocumentVerified: Boolean,
        certificationTitle: String,
        issuingAuthority: String,
        certificationNumber: String,
        certificationDocumentUri: String? = null,
        experienceYears: Int,
        baseVisitingFeeInr: Int,
        upiId: String,
        neighborhood: String,
        city: String = "Bengaluru",
        acceptsEmergencySos: Boolean = true
    ): Result<ServiceProviderRegistrationDocument> = runCatching {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val docRef = db.collection("service_providers").document(userId)
        val existingSnap = try {
            docRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

        val cleanGovIdNumber = govIdNumber.trim().replace(" ", "").uppercase().take(24)
        val cleanCertNumber = certificationNumber.trim().replace(" ", "-").uppercase().take(30)

        if (existingSnap != null && existingSnap.exists()) {
            val updatePayload = mutableMapOf<String, Any>(
                "fullName" to fullName.trim().take(120),
                "email" to email.trim().ifBlank { "provider@neighbourconnect.in" }.take(254),
                "professionCategory" to professionCategory.trim().take(80),
                "govIdType" to govIdType.trim().uppercase(),
                "govIdNumber" to cleanGovIdNumber,
                "govIdDocumentUri" to govIdDocumentUri.trim().take(1024),
                "govIdDocumentVerified" to govIdDocumentVerified,
                "certificationTitle" to certificationTitle.trim().take(120),
                "issuingAuthority" to issuingAuthority.trim().take(120),
                "certificationNumber" to cleanCertNumber,
                "experienceYears" to experienceYears.coerceIn(0, 60).toLong(),
                "baseVisitingFeeInr" to baseVisitingFeeInr.coerceIn(49, 100000).toLong(),
                "upiId" to upiId.trim().take(100),
                "neighborhood" to neighborhood.trim().ifBlank { "Indiranagar 12th Main, Bengaluru" }.take(200),
                "city" to city.trim().ifBlank { "Bengaluru" }.take(100),
                "acceptsEmergencySos" to acceptsEmergencySos,
                "kycStatus" to "VERIFIED",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!customProfessionTitle.isNullOrBlank()) {
                updatePayload["customProfessionTitle"] = customProfessionTitle.trim().take(80)
            }
            if (!certificationDocumentUri.isNullOrBlank()) {
                updatePayload["certificationDocumentUri"] = certificationDocumentUri.trim().take(1024)
            }
            try {
                docRef.set(updatePayload, SetOptions.merge()).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        } else {
            val createPayload = mutableMapOf<String, Any>(
                "userId" to userId,
                "fullName" to fullName.trim().take(120),
                "email" to email.trim().ifBlank { "provider@neighbourconnect.in" }.take(254),
                "professionCategory" to professionCategory.trim().take(80),
                "govIdType" to govIdType.trim().uppercase(),
                "govIdNumber" to cleanGovIdNumber,
                "govIdDocumentUri" to govIdDocumentUri.trim().take(1024),
                "govIdDocumentVerified" to govIdDocumentVerified,
                "certificationTitle" to certificationTitle.trim().take(120),
                "issuingAuthority" to issuingAuthority.trim().take(120),
                "certificationNumber" to cleanCertNumber,
                "experienceYears" to experienceYears.coerceIn(0, 60).toLong(),
                "baseVisitingFeeInr" to baseVisitingFeeInr.coerceIn(49, 100000).toLong(),
                "upiId" to upiId.trim().take(100),
                "neighborhood" to neighborhood.trim().ifBlank { "Indiranagar 12th Main, Bengaluru" }.take(200),
                "city" to city.trim().ifBlank { "Bengaluru" }.take(100),
                "acceptsEmergencySos" to acceptsEmergencySos,
                "linkedAccountOwnerEmail" to PLATFORM_OWNER_ACCOUNT_EMAIL,
                "kycStatus" to "VERIFIED",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!customProfessionTitle.isNullOrBlank()) {
                createPayload["customProfessionTitle"] = customProfessionTitle.trim().take(80)
            }
            if (!certificationDocumentUri.isNullOrBlank()) {
                createPayload["certificationDocumentUri"] = certificationDocumentUri.trim().take(1024)
            }
            try {
                docRef.set(createPayload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
        }

        val savedSnap = try {
            docRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
        savedSnap.toObject(
            ServiceProviderRegistrationDocument::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ) ?: ServiceProviderRegistrationDocument(
            id = userId,
            userId = userId,
            fullName = fullName,
            email = email,
            professionCategory = professionCategory,
            customProfessionTitle = customProfessionTitle.orEmpty(),
            govIdType = govIdType,
            govIdNumber = cleanGovIdNumber,
            govIdDocumentUri = govIdDocumentUri,
            govIdDocumentVerified = govIdDocumentVerified,
            certificationTitle = certificationTitle,
            issuingAuthority = issuingAuthority,
            certificationNumber = cleanCertNumber,
            certificationDocumentUri = certificationDocumentUri,
            experienceYears = experienceYears.toLong(),
            baseVisitingFeeInr = baseVisitingFeeInr.toLong(),
            upiId = upiId,
            neighborhood = neighborhood,
            city = city,
            acceptsEmergencySos = acceptsEmergencySos,
            linkedAccountOwnerEmail = PLATFORM_OWNER_ACCOUNT_EMAIL,
            kycStatus = "VERIFIED"
        )
    }

    suspend fun getServiceProviderRegistration(userId: String): Result<ServiceProviderRegistrationDocument?> = runCatching {
        val docRef = db.collection("service_providers").document(userId)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(
                    ServiceProviderRegistrationDocument::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    fun observeServiceProviderRegistration(userId: String): Flow<ServiceProviderRegistrationDocument?> {
        val docRef = db.collection("service_providers").document(userId)
        return docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(
                    ServiceProviderRegistrationDocument::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
        }
    }

    suspend fun syncConsumerBookingToCloud(
        bookingId: String,
        userId: String,
        consumerEmail: String,
        providerId: String,
        providerName: String,
        categoryTitle: String,
        issueSummary: String,
        neighborhood: String,
        status: String = "QUOTE_SUBMITTED",
        totalAmountInr: Long
    ): Result<String> = runCatching {
        val safeId = bookingId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "bk_${System.currentTimeMillis()}" }
        val docRef = db.collection("consumer_bookings").document(safeId)
        val payload = mapOf(
            "userId" to userId,
            "consumerEmail" to consumerEmail.ifBlank { "consumer@neighbourconnect.in" }.take(254),
            "linkedAccountOwnerEmail" to PLATFORM_OWNER_ACCOUNT_EMAIL,
            "providerId" to providerId.take(128),
            "providerName" to providerName.take(120),
            "categoryTitle" to categoryTitle.take(100),
            "issueSummary" to issueSummary.take(1000),
            "neighborhood" to neighborhood.ifBlank { "Indiranagar 12th Main, Bengaluru" }.take(200),
            "status" to status,
            "totalAmountInr" to totalAmountInr.coerceIn(0L, 10000000L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            safeId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun getConsumerBookings(userId: String): Result<List<ConsumerBookingDocument>> = runCatching {
        val colRef = db.collection("consumer_bookings")
        try {
            val snap = colRef.whereEqualTo("userId", userId).get().await()
            snap.toObjects(
                ConsumerBookingDocument::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "consumer_bookings")
            throw e
        }
    }

    fun observeConsumerBookings(userId: String): Flow<List<ConsumerBookingDocument>> {
        return db.collection("consumer_bookings")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    ConsumerBookingDocument::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
    }
}
