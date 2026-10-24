package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val providerName: String,
    val providerCategoryId: String,
    val providerTrustScore: Int,
    val customerAddress: String,
    val issueSummary: String,
    val scheduledSlotLabel: String,
    val isEmergencySos: Boolean,
    val statusName: String,
    val quoteId: String,
    val laborDescription: String,
    val laborCostInr: Int,
    val partsDescription: String,
    val partsCostInr: Int,
    val warrantyDays: Int,
    val quoteNotes: String,
    val quoteAccepted: Boolean,
    val completionOtp: String,
    val razorpayOrderId: String?,
    val paymentMethodLabel: String?,
    val invoiceNumber: String?,
    val providerEtaMinutes: Int,
    val providerDistanceKm: Double,
    val providerCurrentLat: Double,
    val providerCurrentLng: Double,
    val createdAtTimestamp: Long,
    val isReviewed: Boolean
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val senderNameEnum: String,
    val senderDisplayName: String,
    val text: String,
    val timestampLabel: String,
    val hasQuote: Boolean,
    val quoteLaborDesc: String?,
    val quoteLaborInr: Int?,
    val quotePartsDesc: String?,
    val quotePartsInr: Int?,
    val quoteWarrantyDays: Int?,
    val attachedDiagnosisTitle: String?,
    val createdAtMillis: Long
)

@Entity(tableName = "custom_providers")
data class CustomProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categoryId: String,
    val customProfessionTitle: String?,
    val bio: String,
    val experienceYears: Int,
    val neighborhood: String,
    val city: String,
    val baseVisitingFeeInr: Int,
    val acceptsEmergency: Boolean,
    val languagesCsv: String,
    val governmentIdMasked: String,
    val upiId: String,
    val subscriptionTierName: String,
    val aiProfileQualityScore: Int,
    val trustScoreTotal: Int,
    val createdAtMillis: Long
)

@Dao
interface NeighbourConnectDao {
    @Query("SELECT * FROM bookings ORDER BY createdAtTimestamp DESC")
    fun observeAllBookings(): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooking(booking: BookingEntity)

    @Query("SELECT * FROM chat_messages WHERE bookingId = :bookingId ORDER BY createdAtMillis ASC")
    fun observeMessagesForBooking(bookingId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM custom_providers ORDER BY createdAtMillis DESC")
    fun observeCustomProviders(): Flow<List<CustomProviderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomProvider(provider: CustomProviderEntity)

    @Query("SELECT COUNT(*) FROM bookings")
    suspend fun getBookingCount(): Int
}

@Database(
    entities = [
        BookingEntity::class,
        ChatMessageEntity::class,
        CustomProviderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): NeighbourConnectDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "neighbour_connect.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
