package com.example.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

data class OtpSendRequest(val phoneNumber: String)
data class OtpVerifyRequest(val phoneNumber: String, val otpCode: String)
data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val role: String
)

data class NearbySearchRequest(
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double,
    val categoryId: String?,
    val minTrustScore: Int,
    val emergencyOnly: Boolean
)

data class AiDiagnoseRequestDto(
    val description: String,
    val mediaType: String,
    val mediaBase64OrUrl: String?,
    val latitude: Double,
    val longitude: Double
)

data class CreateBookingRequestDto(
    val providerId: String,
    val customerAddress: String,
    val issueSummary: String,
    val scheduledSlot: String,
    val isEmergency: Boolean,
    val quotedSubtotalInr: Int
)

data class RazorpayOrderRequestDto(
    val bookingId: String,
    val amountInPaise: Int,
    val currency: String = "INR"
)

data class RazorpayOrderResponseDto(
    val razorpayOrderId: String,
    val escrowStatus: String,
    val platformCommissionInr: Int,
    val providerNetPayoutInr: Int
)

data class ReleaseEscrowRequestDto(
    val bookingId: String,
    val completionOtp: String
)

interface FastApiService {
    @POST("/api/v1/auth/otp/send")
    suspend fun sendOtp(@Body request: OtpSendRequest): Map<String, String>

    @POST("/api/v1/auth/otp/verify")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): AuthTokenResponse

    @POST("/api/v1/search/nearby")
    suspend fun searchNearbyProviders(@Body request: NearbySearchRequest): List<Map<String, Any>>

    @POST("/api/v1/ai/diagnose")
    suspend fun diagnoseFault(@Body request: AiDiagnoseRequestDto): Map<String, Any>

    @POST("/api/v1/bookings/create")
    suspend fun createBooking(@Body request: CreateBookingRequestDto): Map<String, Any>

    @POST("/api/v1/bookings/emergency")
    suspend fun triggerEmergencySos(@Body request: CreateBookingRequestDto): Map<String, Any>

    @PATCH("/api/v1/bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Path("id") bookingId: String,
        @Body statusPayload: Map<String, String>
    ): Map<String, Any>

    @POST("/api/v1/payments/create-order")
    suspend fun createRazorpayOrder(@Body request: RazorpayOrderRequestDto): RazorpayOrderResponseDto

    @POST("/api/v1/payments/release-escrow")
    suspend fun releaseEscrow(@Body request: ReleaseEscrowRequestDto): Map<String, Any>
}
