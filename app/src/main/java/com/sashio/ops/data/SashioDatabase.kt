package com.sashio.ops.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// --- Room Type Converters ---
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return Json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// --- Data Access Object ---
@Dao
interface SashioDao {
    // --- User Profiles ---
    @Query("SELECT * FROM user_profiles ORDER BY id ASC")
    fun getAllProfilesFlow(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE role = 'staff' ORDER BY name ASC")
    fun getStaffProfiles(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getProfileByEmail(email: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    @Update
    suspend fun updateProfile(profile: UserProfile)

    // --- Bookings ---
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookingsFlow(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getBookingsForClientFlow(clientId: Long): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE assignedStaffId = :staffId ORDER BY createdAt DESC")
    fun getBookingsForStaffFlow(staffId: Long): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: Long): Booking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Query("UPDATE bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: Long, status: String)

    @Query("UPDATE bookings SET assignedStaffId = :staffId, assignedStaffName = :staffName, status = 'Staff Assigned' WHERE id = :bookingId")
    suspend fun assignBookingToStaff(bookingId: Long, staffId: Long, staffName: String)

    // --- Payments ---
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPaymentsFlow(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE reference = :ref LIMIT 1")
    suspend fun getPaymentByReference(ref: String): Payment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Query("UPDATE payments SET status = :status WHERE reference = :ref")
    suspend fun updatePaymentStatus(ref: String, status: String)

    // --- Consultations ---
    @Query("SELECT * FROM consultations ORDER BY createdAt DESC")
    fun getAllConsultationsFlow(): Flow<List<Consultation>>

    @Query("SELECT * FROM consultations WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getConsultationsForClientFlow(clientId: Long): Flow<List<Consultation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(consultation: Consultation): Long

    @Update
    suspend fun updateConsultation(consultation: Consultation)

    // --- Reviews ---
    @Query("SELECT * FROM reviews ORDER BY id DESC")
    fun getAllReviewsFlow(): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE category = :category ORDER BY id DESC")
    fun getReviewsByCategory(category: String): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

    // --- Catalog Items ---
    @Query("SELECT * FROM catalog_items ORDER BY id ASC")
    fun getAllCatalogItemsFlow(): Flow<List<CatalogItem>>

    @Query("SELECT COUNT(*) FROM catalog_items")
    suspend fun getCatalogItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogItem(item: CatalogItem): Long

    @Update
    suspend fun updateCatalogItem(item: CatalogItem)

    @Query("DELETE FROM catalog_items WHERE id = :id")
    suspend fun deleteCatalogItem(id: Long)

    // --- Favorite Locations ---
    @Query("SELECT * FROM favorite_locations WHERE clientId = :clientId ORDER BY id DESC")
    fun getFavoriteLocationsForClientFlow(clientId: Long): Flow<List<FavoriteLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteLocation(location: FavoriteLocation): Long

    @Update
    suspend fun updateFavoriteLocation(location: FavoriteLocation)

    @Query("DELETE FROM favorite_locations WHERE id = :id")
    suspend fun deleteFavoriteLocation(id: Long)
}

// --- Room Database Configuration ---
@Database(
    entities = [
        UserProfile::class,
        Booking::class,
        Payment::class,
        Consultation::class,
        Review::class,
        CatalogItem::class,
        FavoriteLocation::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SashioDatabase : RoomDatabase() {
    abstract fun sashioDao(): SashioDao

    companion object {
        @Volatile
        private var INSTANCE: SashioDatabase? = null

        fun getDatabase(context: Context): SashioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SashioDatabase::class.java,
                    "sashio_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
