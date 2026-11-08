package com.sashio.ops.data

import android.content.Context
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow

class SashioRepository(
    private val context: Context,
    private val database: SashioDatabase
) {
    private val dao = database.sashioDao()
    val networkProvider = NetworkProvider(database)

    // --- Users ---
    val allProfiles: Flow<List<UserProfile>> = dao.getAllProfilesFlow()
    val staffProfiles: Flow<List<UserProfile>> = dao.getStaffProfiles()

    suspend fun getProfileByEmail(email: String): UserProfile? = dao.getProfileByEmail(email.trim().lowercase())
    suspend fun getProfileById(id: Long): UserProfile? = dao.getProfileById(id)
    suspend fun insertProfile(profile: UserProfile): Long = dao.insertProfile(profile)
    suspend fun registerProfile(name: String, email: String, role: String, phone: String, company: String = ""): Long {
        val existing = dao.getProfileByEmail(email.trim().lowercase())
        if (existing != null) return existing.id
        return dao.insertProfile(
            UserProfile(
                name = name.trim(),
                email = email.trim().lowercase(),
                role = role,
                phoneNumber = phone.trim(),
                companyName = company.trim()
            )
        )
    }

    // --- Bookings & Quotation Requests ---
    val allBookings: Flow<List<Booking>> = dao.getAllBookingsFlow()
    fun getBookingsForClient(clientId: Long, clientEmail: String): Flow<List<Booking>> =
        dao.getBookingsForClientFlow(clientId, clientEmail.trim().lowercase())
    fun getBookingsForStaff(staffId: Long): Flow<List<Booking>> = dao.getBookingsForStaffFlow(staffId)

    suspend fun insertBooking(booking: Booking): Long = dao.insertBooking(booking)
    suspend fun deleteBooking(id: Long) = dao.deleteBooking(id)
    suspend fun updateBooking(booking: Booking) = dao.updateBooking(booking)
    suspend fun updateBookingStatus(bookingId: Long, status: String) {
        dao.updateBookingStatus(bookingId, status)
    }

    suspend fun assignBooking(bookingId: Long, staffId: Long) {
        val staff = dao.getProfileById(staffId)
        if (staff != null) {
            dao.assignBookingToStaff(bookingId, staffId, staff.name)
        }
    }

    // --- Payments ---
    val allPayments: Flow<List<Payment>> = dao.getAllPaymentsFlow()

    // NOTE: bookingId here is the local Room Long id, stringified. The Edge
    // Function looks this up against the real public.bookings UUID in
    // Supabase.
    suspend fun createPayment(bookingId: Long, email: String, amount: Double): CreatePaymentResponse? {
        return try {
            val req = CreatePaymentRequest(bookingId.toString(), email, amount)
            networkProvider.apiService.createPayment(req)
        } catch (e: Exception) {
            null
        }
    }

    // For bookings that exist directly in Supabase (real UUID string).
    suspend fun createPaymentForRealBooking(bookingId: String, email: String, amount: Double): CreatePaymentResponse {
        val req = CreatePaymentRequest(bookingId, email, amount)
        return networkProvider.apiService.createPayment(req)
    }

    // Real status check against Supabase Postgrest - reflects whatever the
    // paystack-webhook Edge Function actually recorded after a genuine
    // Paystack payment.
    suspend fun checkPaymentStatus(reference: String): String {
        return try {
            SupabaseProvider.client.postgrest.from("payments")
                .select {
                    filter { eq("reference", reference) }
                }
                .decodeSingleOrNull<SupabasePaymentStatus>()
                ?.status ?: "not_found"
        } catch (e: Exception) {
            "check_failed"
        }
    }

    // --- Consultations ---
    val allConsultations: Flow<List<Consultation>> = dao.getAllConsultationsFlow()
    fun getConsultationsForClient(clientId: Long): Flow<List<Consultation>> = dao.getConsultationsForClientFlow(clientId)
    suspend fun insertConsultation(consultation: Consultation): Long = dao.insertConsultation(consultation)
    suspend fun updateConsultation(consultation: Consultation) = dao.updateConsultation(consultation)

    // --- Reviews ---
    val allReviews: Flow<List<Review>> = dao.getAllReviewsFlow()
    fun getReviewsByCategory(category: String): Flow<List<Review>> = dao.getReviewsByCategory(category)
    suspend fun insertReview(review: Review): Long = dao.insertReview(review)

    // --- Catalog Items ---
    val allCatalogItems: Flow<List<CatalogItem>> = dao.getAllCatalogItemsFlow()
    suspend fun insertCatalogItem(item: CatalogItem): Long = dao.insertCatalogItem(item)
    suspend fun deleteCatalogItem(id: Long) = dao.deleteCatalogItem(id)

    // --- Favorite Locations ---
    fun getFavoriteLocationsForClient(clientId: Long): Flow<List<FavoriteLocation>> = dao.getFavoriteLocationsForClientFlow(clientId)
    suspend fun insertFavoriteLocation(location: FavoriteLocation): Long = dao.insertFavoriteLocation(location)
    suspend fun updateFavoriteLocation(location: FavoriteLocation) = dao.updateFavoriteLocation(location)
    suspend fun deleteFavoriteLocation(id: Long) = dao.deleteFavoriteLocation(id)

    // --- Database Seeder ---
    // Seeds the 12 authentic SACHIO product catalog categories.
    suspend fun seedDatabaseIfEmpty() {
        if (dao.getCatalogItemCount() > 0) return

        val defaultCatalog = listOf(
            CatalogItem(
                title = "Standard Portable Toilets",
                description = "Ideal for high-throughput construction, agricultural workspaces, and community events. Robust, ventilated single-cab units.",
                modelNumber = "S-100 Classic",
                variantName = "Classic Single HD Cabin",
                features = "• Anti-slip floor\n• High capacity waste tank\n• Urinal inside",
                specifications = "Weight: ~75kg\nDimensions: 110x120x230cm\nMaterials: High-density polyethylene",
                benefits = "Durable, easy to clean, scalable for large crowds.",
                useCases = "Construction sites, Open concerts, Marathons, Farm events"
            ),
            CatalogItem(
                title = "Executive Portable Toilets",
                description = "Solar power air-conditioned single cabins, perfect for exclusive outdoor filming, private gatherings, and banquets.",
                modelNumber = "E-200 Deluxe",
                variantName = "Executive Solar Cabin",
                features = "• A/C Unit\n• Flushing System\n• Mirror and sink\n• Solar panel attached",
                specifications = "Power needed: Solar (12v system)\nWater capacity: 60L flush tank",
                benefits = "Premium comfort, cooling inside, VIP experience.",
                useCases = "Film sets, VIP banquets, Outdoor private weddings"
            ),
            CatalogItem(
                title = "VIP Portable Toilets",
                description = "Ultra-high quality materials, integrated wash basins, luxury paper, deodorizers, and beautiful warm led lighting installations.",
                modelNumber = "V-400 Executive",
                variantName = "Premium VIP Flush Single"
            ),
            CatalogItem(
                title = "Presidential Luxury Mobile Restrooms",
                description = "Multicabin presidential restrooms equipped with air conditioning, surround sound audio systems, and high-end mahogany counter trims.",
                modelNumber = "S-1000 Sovereign",
                variantName = "Presidential Trailing Suite",
                features = "• Mahogany Trims\n• Surround Sound\n• Fully Air-Conditioned",
                useCases = "High-society weddings, Government functions"
            ),
            CatalogItem(
                title = "Mobile Shower Units",
                description = "Hot & cold hydro-pumping shower stalls. Excellent for mining layouts, military setups, and multi-day sports tournaments.",
                modelNumber = "MS-50 Shower",
                variantName = "Twin Restroom-Shower Suite"
            ),
            CatalogItem(
                title = "Accessible Toilets",
                description = "Fully ADA wheelchair compliant and spacious floor-grade entries equipped with heavy robust handrail grips.",
                modelNumber = "A-500 Accessible",
                variantName = "Safe Cabin Comfort"
            ),
            CatalogItem(
                title = "Event Sanitation Packages",
                description = "Complete modular packages: Standard + VIP units plus onsite standby pumpers and attendants for high scale gatherings.",
                modelNumber = "ESP-Multi",
                variantName = "Grand Wedding Special Package"
            ),
            CatalogItem(
                title = "Corporate Site Sanitation Solutions",
                description = "Tailored packages with scheduled weekly extraction logs, servicing standard corporate expansion bases.",
                modelNumber = "CSS-Corp",
                variantName = "Industrial Workspace Unit"
            ),
            CatalogItem(
                title = "Waste Evacuation Services",
                description = "High density quad-tank vacuum suction pump trucks for waste collection, transfer, and highly sanitized operations.",
                modelNumber = "WES-Truck",
                variantName = "4000L Quad-Tank Pumper-Truck"
            ),
            CatalogItem(
                title = "Cleaning & Maintenance Services",
                description = "Uniformed standby sanitization crew dispatched to sites on-shift to scrub, empty, and refresh interiors constantly.",
                modelNumber = "CMS-Daily",
                variantName = "Standby Attendant Shift Crew"
            ),
            CatalogItem(
                title = "Sachio Clean and Detergents",
                description = "Sachio-branded cleaning agents, disinfectants, and detergents used for on-site sanitation and available for direct purchase.",
                modelNumber = "SCD-100",
                variantName = "Sachio Detergent & Disinfectant Range"
            ),
            CatalogItem(
                title = "Sachio Clothing",
                description = "Sachio-branded workwear and apparel, including staff uniforms and site-safety clothing.",
                modelNumber = "SCW-100",
                variantName = "Sachio Uniform & Apparel Range"
            )
        )
        for (c in defaultCatalog) {
            dao.insertCatalogItem(c)
        }
    }
}
