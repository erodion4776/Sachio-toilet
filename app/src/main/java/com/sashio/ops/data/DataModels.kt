package com.sashio.ops.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val role: String, // "client" | "staff" | "admin"
    val phoneNumber: String = "",
    val companyName: String = ""
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val clientPhone: String,
    val clientEmail: String,
    val companyName: String = "",
    val serviceMode: String = "Rental",
    val serviceType: String,
    val variantName: String,
    val modelNumber: String,
    val quantity: Int = 1,
    val duration: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val venueName: String = "",
    val eventType: String,
    val guestCount: Int = 100,
    val eventDate: String,
    val cleaningSupport: Boolean = false,
    val onSiteAttendant: Boolean = false,
    val wasteEvacuation: Boolean = false,
    val maintenanceSupport: Boolean = false,
    val specialInstructions: String = "",
    val quotedPrice: Double = 0.0,
    val logisticsCost: Double = 0.0,
    val vatAmount: Double = 0.0,
    val adminNotes: String = "",
    val quoteIssuedDate: Long = 0L,
    val status: String = "Quotation Requested",
    val paymentStatus: String = "pending",
    val assignedStaffId: Long? = null,
    val assignedStaffName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingId: Long,
    val reference: String,
    val amount: Double,
    val status: String, // "success" | "pending" | "failed"
    val channel: String = "card",
    val paymentDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "consultations")
data class Consultation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val clientPhone: String,
    val clientEmail: String,
    val type: String,
    val preferredDate: String,
    val notes: String = "",
    val status: String = "Pending",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userName: String,
    val rating: Int,
    val comment: String,
    val category: String,
    val date: String
)

@Entity(tableName = "catalog_items")
@TypeConverters(StringListConverter::class)
data class CatalogItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val modelNumber: String,
    val variantName: String,
    val basePrice: Double = 0.0,
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String = "",
    val features: String = "",
    val specifications: String = "",
    val benefits: String = "",
    val useCases: String = ""
)

@Entity(tableName = "favorite_locations")
data class FavoriteLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val locationType: String // "Home" | "Office" | "Venue" | "Other"
)

data class SimulatedPlace(
    val address: String,
    val lat: Double,
    val lng: Double
)

@Serializable
data class CreatePaymentResponse(
    val authorizationUrl: String,
    val accessCode: String,
    val reference: String,
    val amount: Double
)

class StringListConverter {
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
