package com.sashio.ops.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SashioDao {
    // --- User Profiles ---
    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getProfileByEmail(email: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE role = 'staff' ORDER BY name ASC")
    fun getStaffProfiles(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    @Query("SELECT * FROM user_profiles")
    fun getAllProfilesFlow(): Flow<List<UserProfile>>

    // --- Bookings & Quotations ---
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookingsFlow(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getBookingsForClientFlow(clientId: Long): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE assignedStaffId = :staffId ORDER BY createdAt DESC")
    fun getBookingsForStaffFlow(staffId: Long): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Long, status: String)

    @Query("UPDATE bookings SET assignedStaffId = :staffId, assignedStaffName = :staffName, status = 'SERVICE DELIVERED' WHERE id = :id")
    suspend fun assignBookingToStaff(id: Long, staffId: Long, staffName: String)

    @Query("UPDATE bookings SET paymentStatus = :paymentStatus, paymentRef = :ref, status = 'Payment Confirmed' WHERE id = :id")
    suspend fun updateBookingPayment(id: Long, paymentStatus: String, ref: String)

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: Long): Booking?

    // --- Payments ---
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPaymentsFlow(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Query("UPDATE payments SET status = :status WHERE reference = :ref")
    suspend fun updatePaymentStatusByRef(ref: String, status: String)

    @Query("SELECT * FROM payments WHERE reference = :ref LIMIT 1")
    suspend fun getPaymentByRef(ref: String): Payment?

    // --- Consultations ---
    @Query("SELECT * FROM consultations ORDER BY createdAt DESC")
    fun getAllConsultationsFlow(): Flow<List<Consultation>>

    @Query("SELECT * FROM consultations WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getConsultationsForClientFlow(clientId: Long): Flow<List<Consultation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(consultation: Consultation): Long

    @Update
    suspend fun updateConsultation(consultation: Consultation)

    // --- Catalog Items ---
    @Query("SELECT * FROM catalog_items ORDER BY id ASC")
    fun getAllCatalogItemsFlow(): Flow<List<CatalogItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogItem(item: CatalogItem): Long

    @Query("DELETE FROM catalog_items WHERE id = :id")
    suspend fun deleteCatalogItem(id: Long)

    // --- Reviews ---
    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviewsFlow(): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE category = :category ORDER BY createdAt DESC")
    fun getReviewsByCategory(category: String): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

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
