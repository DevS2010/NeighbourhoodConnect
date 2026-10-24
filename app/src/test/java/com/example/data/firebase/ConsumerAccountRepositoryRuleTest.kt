package com.example.data.firebase

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ConsumerAccountRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun upsertConsumerProfile_authenticatedUser_createsAndLinksProfileToOwnerAccount() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ConsumerAccountRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.upsertConsumerProfile(
                userId = aliceUid,
                displayName = "Alice Sharma",
                email = ALICE_EMAIL,
                photoUrl = "https://example.com/alice.png",
                neighborhood = "Indiranagar 12th Main, Bengaluru",
                role = "CONSUMER"
            )
        }

        assertTrue("Expected upsertConsumerProfile to succeed, got: ${result.exceptionOrNull()}", result.isSuccess)
        val profile = result.getOrThrow()
        assertEquals(aliceUid, profile.userId)
        assertEquals("Alice Sharma", profile.displayName)
        assertEquals(PLATFORM_OWNER_ACCOUNT_EMAIL, profile.linkedAccountOwnerEmail)
    }

    @Test
    fun getConsumerProfile_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ConsumerAccountRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.upsertConsumerProfile(
                userId = aliceUid,
                displayName = "Alice Sharma",
                email = ALICE_EMAIL
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getConsumerProfile(aliceUid)
        }
        assertTrue(crossReadResult.isFailure)
        val fsError = crossReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsError)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
    }

    @Test
    fun syncAndObserveConsumerBookings_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ConsumerAccountRepository(firestore)
        val dynamicBookingId = "bk_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val syncResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.syncConsumerBookingToCloud(
                bookingId = dynamicBookingId,
                userId = aliceUid,
                consumerEmail = ALICE_EMAIL,
                providerId = "prov_101",
                providerName = "Rajesh Verma",
                categoryTitle = "Electrician",
                issueSummary = "Main breaker tripping",
                neighborhood = "Indiranagar 12th Main, Bengaluru",
                status = "QUOTE_SUBMITTED",
                totalAmountInr = 750L
            )
        }
        assertTrue("Expected booking sync to succeed, got: ${syncResult.exceptionOrNull()}", syncResult.isSuccess)

        val emittedBookings = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeConsumerBookings(aliceUid).first { list ->
                list.any { it.id == dynamicBookingId }
            }
        }
        assertTrue(emittedBookings.any { it.id == dynamicBookingId && it.linkedAccountOwnerEmail == PLATFORM_OWNER_ACCOUNT_EMAIL })
    }

    @Test
    fun observeConsumerBookings_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = ConsumerAccountRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeConsumerBookings("unauth_uid").first()
            }
            fail("Expected unauthenticated observeConsumerBookings to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val fsError = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError.code)
        }
    }

    @Test
    fun registerServiceProviderKyc_authenticatedProviderWithValidGovIdAndCertification_succeeds() = runBlocking {
        val providerUid = signInTestUser(ALICE_EMAIL)
        val repository = ConsumerAccountRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.registerServiceProviderKyc(
                userId = providerUid,
                fullName = "Vikramaditya Rao",
                email = ALICE_EMAIL,
                professionCategory = "ELECTRICIAN",
                customProfessionTitle = "Smart Home Master Electrician",
                govIdType = "AADHAAR",
                govIdNumber = "482910384756",
                govIdDocumentUri = "content://neighbourconnect.kyc/gov_id/aadhaar_4829.jpg",
                govIdDocumentVerified = true,
                certificationTitle = "Class-1 Electrical Wireman & LT Switchgear License",
                issuingAuthority = "Karnataka Electrical Inspectorate (CEIG)",
                certificationNumber = "KEI-BLR-2024-88412",
                certificationDocumentUri = "content://neighbourconnect.kyc/cert/kei_88412.pdf",
                experienceYears = 9,
                baseVisitingFeeInr = 249,
                upiId = "vikram.elec@okicici",
                neighborhood = "Indiranagar 12th Main, Bengaluru"
            )
        }

        assertTrue("Expected registerServiceProviderKyc to succeed, got: ${result.exceptionOrNull()}", result.isSuccess)
        val doc = result.getOrThrow()
        assertEquals(providerUid, doc.userId)
        assertEquals("AADHAAR", doc.govIdType)
        assertTrue(doc.govIdDocumentVerified)
        assertEquals("KEI-BLR-2024-88412", doc.certificationNumber)
        assertEquals(PLATFORM_OWNER_ACCOUNT_EMAIL, doc.linkedAccountOwnerEmail)
    }

    @Test
    fun registerServiceProviderKyc_unverifiedGovIdDocument_failsWithPermissionDenied() = runBlocking {
        val providerUid = signInTestUser(BOB_EMAIL)
        val repository = ConsumerAccountRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.registerServiceProviderKyc(
                userId = providerUid,
                fullName = "Bob Unverified",
                email = BOB_EMAIL,
                professionCategory = "PLUMBER",
                govIdType = "PAN",
                govIdNumber = "ABCDE1234F",
                govIdDocumentUri = "content://neighbourconnect.kyc/gov_id/pan.jpg",
                govIdDocumentVerified = false,
                certificationTitle = "National Trade Certificate - Plumbing",
                issuingAuthority = "NCVT India",
                certificationNumber = "NCVT-PLM-99201",
                experienceYears = 5,
                baseVisitingFeeInr = 199,
                upiId = "bob@okaxis",
                neighborhood = "Koramangala 4th Block, Bengaluru"
            )
        }

        assertTrue("Expected unverified Gov ID document to be rejected by rules", result.isFailure)
        val fsError = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsError)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
