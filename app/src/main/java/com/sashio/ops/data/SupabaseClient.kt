package com.sashio.ops.data

import io.github.jan.supabase.auth.providers.Google
import com.sashio.ops.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

@Serializable
data class SupabasePaymentStatus(
    val reference: String,
    val status: String
)

@Serializable
data class NewSupabaseCatalogItem(
    val title: String,
    val description: String,
    val model_number: String,
    val variant_name: String,
    val base_price: Double,
    val image_url: String,
    val image_urls: List<String> = emptyList(),
    val video_url: String,
    val features: String,
    val specifications: String,
    val benefits: String,
    val use_cases: String
)

@Serializable
data class SupabaseCatalogItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val model_number: String = "",
    val variant_name: String = "",
    val base_price: Double = 0.0,
    val image_url: String = "",
    val image_urls: List<String> = emptyList(),
    val video_url: String = "",
    val features: String = "",
    val specifications: String = "",
    val benefits: String = "",
    val use_cases: String = "",
    val is_active: Boolean = true
)

/**
 * Maps a remote Supabase catalog item into the local UI/Room model shape.
 */
fun SupabaseCatalogItem.toCatalogItem(): CatalogItem = CatalogItem(
    title = title,
    description = description,
    modelNumber = model_number,
    variantName = variant_name,
    basePrice = base_price,
    imageUrl = image_url,
    imageUrls = image_urls.ifEmpty { if (image_url.isNotBlank()) listOf(image_url) else emptyList() },
    videoUrl = video_url,
    features = features,
    specifications = specifications,
    benefits = benefits,
    useCases = use_cases
)

@Serializable
data class NewSupabaseBooking(
    val client_id: String,
    val client_name: String,
    val client_phone: String,
    val client_email: String,
    val company_name: String,
    val service_mode: String,
    val service_type: String,
    val variant_name: String,
    val model_number: String,
    val quantity: Int,
    val duration: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val venue_name: String,
    val event_type: String,
    val guest_count: Int,
    val event_date: String? = null,
    val cleaning_support: Boolean,
    val on_site_attendant: Boolean,
    val waste_evacuation: Boolean,
    val maintenance_support: Boolean,
    val special_instructions: String
)

@Serializable
data class SupabaseBooking(
    val id: String,
    val client_id: String,
    val client_name: String = "",
    val client_phone: String = "",
    val client_email: String = "",
    val company_name: String = "",
    val service_mode: String = "Rental",
    val service_type: String = "",
    val catalog_item_id: String? = null,
    val variant_name: String = "",
    val model_number: String = "",
    val quantity: Int = 1,
    val duration: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val place_id: String? = null,
    val venue_name: String = "",
    val event_type: String = "",
    val guest_count: Int = 0,
    val event_date: String? = null,
    val cleaning_support: Boolean = false,
    val on_site_attendant: Boolean = false,
    val waste_evacuation: Boolean = false,
    val maintenance_support: Boolean = false,
    val special_instructions: String = "",
    val quoted_price: Double = 0.0,
    val logistics_cost: Double = 0.0,
    val vat_amount: Double = 0.0,
    val admin_notes: String = "",
    val status: String = "Quotation Requested",
    val assigned_staff_id: String? = null,
    val assigned_staff_name: String? = null,
    val payment_status: String = "pending"
)

@Serializable
data class AdminProfileRow(
    val id: String,
    val email: String,
    val full_name: String? = null,
    val role: String = "client",
    val phone_number: String? = null,
    val company_name: String? = null,
    val suspended: Boolean = false
)

@Serializable
data class SupabaseMessage(
    val id: String = "",
    val sender_id: String = "",
    val sender_name: String = "",
    val sender_role: String = "client",
    val recipient_id: String? = null,
    val recipient_role: String? = null, // "staff" | "client" | "admin" | "all" | null
    val content: String = "",
    val created_at: String = ""
)

@Serializable
data class SupabaseProfile(
    val id: String,
    val email: String,
    val full_name: String? = null,
    val role: String = "client",
    val phone_number: String? = null,
    val company_name: String? = null
)

@Serializable
data class NewSupabaseExpense(
    val staff_id: String? = null,
    val staff_name: String,
    val category: String,
    val amount: Double,
    val details: String = "",
    val status: String = "Pending Approval",
    val receipt_url: String? = null,
    val expense_date: String? = null
)

@Serializable
data class SupabaseExpense(
    val id: String = "",
    val staff_id: String? = null,
    val staff_name: String = "",
    val category: String = "Fuel Costs",
    val amount: Double = 0.0,
    val details: String = "",
    val status: String = "Pending Approval",
    val receipt_url: String? = null,
    val expense_date: String? = null,
    val created_at: String? = null
)

@Serializable
data class NewSupabaseAttendance(
    val staff_id: String? = null,
    val staff_name: String,
    val clock_in_time: String,
    val clock_out_time: String? = null,
    val location_verified: String = "",
    val is_verified: Boolean = true
)

@Serializable
data class SupabaseAttendance(
    val id: String = "",
    val staff_id: String? = null,
    val staff_name: String = "",
    val clock_in_time: String = "",
    val clock_out_time: String? = null,
    val location_verified: String? = null,
    val is_verified: Boolean = true,
    val created_at: String = ""
)

object SupabaseProvider {
    const val CATALOG_IMAGES_BUCKET = "catalog-images"

    val SUPABASE_URL = BuildConfig.SUPABASE_URL
    val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            requestTimeout = 30.seconds
            install(Auth) {
                scheme = "sachioexpress"
                host = "reset-password"
            }
            install(Postgrest)
        }
    }
}
