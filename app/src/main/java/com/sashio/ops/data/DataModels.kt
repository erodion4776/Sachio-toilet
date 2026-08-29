package com.sashio.ops.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable

// --- Room Type Converters ---
// Room stores primitive-compatible columns. List<String> (used for
// CatalogItem.imageUrls) uses this converter, registered on SashioDatabase.
class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String = list?.joinToString("|||") ?: ""

    @TypeConverter
    fun toStringList(data: String?): List<String> =
        if (data.isNullOrBlank()) emptyList() else data.split("|||")
}

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val role: String, // "client", "staff", "admin"
    val phoneNumber: String = "",
    val companyName: String = ""
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val clientPhone: String = "",
    val clientEmail: String = "",
    val companyName: String = "",
    
    // Service & Catalog
    val serviceMode: String = "Rental", // "Rental" or "Purchase"
    val serviceType: String, // Catalog category (e.g., "Presidential Luxury Mobile Restrooms")
    val variantName: String = "VIP Luxury Cabin",
    val modelNumber: String = "S-800 Elite",
    val quantity: Int = 1,
    val duration: String = "1 Day",
    
    // Location Details
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val venueName: String = "",
    val gateInformation: String = "",
    val landmarks: String = "",
    val parkingInformation: String = "",
    
    // Event Details
    val eventType: String = "Wedding",
    val guestCount: Int = 100,
    val eventDate: String = "",
    
    // Inclusions Checklist
    val cleaningSupport: Boolean = false,
    val onSiteAttendant: Boolean = false,
    val wasteEvacuation: Boolean = false,
    val maintenanceSupport: Boolean = false,
    
    val specialInstructions: String = "",
    
    // Custom Admin pricing parameters
    val quotedPrice: Double = 0.0,
    val logisticsCost: Double = 0.0,
    val vatAmount: Double = 0.0,
    val adminNotes: String = "",
    val quoteIssuedDate: Long = 0L,
    
    // Status in workflow
    val status: String = "Quotation Requested",
    
    val assignedStaffId: Long? = null,
    val assignedStaffName: String? = null,
    val paymentStatus: String = "pending", // "pending", "success", "failed"
    val paymentRef: String = "",           // Required by SashioDao updateBookingPayment query
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val amount: Double,
    val reference: String,
    val status: String = "pending", // "pending", "success", "failed"
    val channel: String = "paystack",
    val paymentDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "consultations")
data class Consultation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val clientPhone: String = "",
    val clientEmail: String = "",
    val type: String = "Book Consultation",
    val preferredDate: String = "",
    val status: String = "Pending",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userName: String,
    val rating: Int = 5, // 1 to 5
    val comment: String,
    val category: String = "Presidential Luxury Mobile Restrooms",
    val date: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis() // Required by SashioDao getAllReviewsFlow query
)

@Entity(tableName = "catalog_items")
@TypeConverters(Converters::class)
data class CatalogItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val modelNumber: String = "",
    val variantName: String = "",
    val basePrice: Double = 0.0,
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String = "",
    val features: String = "",
    val specifications: String = "",
    val benefits: String = "",
    val useCases: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class SimulatedPlace(
    val address: String,
    val lat: Double,
    val lng: Double
)

@Entity(tableName = "favorite_locations")
data class FavoriteLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val locationType: String = "Other"
)
