package com.sashio.ops.data

import android.content.Context
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SashioRepository(
    private val context: Context,
    private val database: SashioDatabase
) {
    private val dao = database.sashioDao()
    val networkProvider = NetworkProvider(database)

    // --- Users ---
    val allProfiles: Flow<List<UserProfile>> = dao.getAllProfilesFlow()
    val staffProfiles: Flow<List<UserProfile>> = dao.getStaffProfiles()

    suspend fun getProfileByEmail(email: String): UserProfile? = dao.getProfileByEmail(email)
    suspend fun getProfileById(id: Long): UserProfile? = dao.getProfileById(id)
    suspend fun insertProfile(profile: UserProfile): Long = dao.insertProfile(profile)
    suspend fun registerProfile(name: String, email: String, role: String, phone: String, company: String = ""): Long {
        val existing = dao.getProfileByEmail(email)
        if (existing != null) return existing.id
        return dao.insertProfile(
            UserProfile(name = name, email = email, role = role, phoneNumber = phone, companyName = company)
        )
    }

    // --- Bookings & Quotation Requests ---
    val allBookings: Flow<List<Booking>> = dao.getAllBookingsFlow()
    fun getBookingsForClient(clientId: Long): Flow<List<Booking>> = dao.getBookingsForClientFlow(clientId)
    fun getBookingsForStaff(staffId: Long): Flow<List<Booking>> = dao.getBookingsForStaffFlow(staffId)

    suspend fun insertBooking(booking: Booking): Long = dao.insertBooking(booking)
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
    // Supabase - until bookings themselves are migrated to Supabase (a
    // separate, larger step), this call will succeed in reaching Paystack
    // but fail the booking lookup for bookings that only exist locally.
    suspend fun createPayment(bookingId: Long, email: String, amount: Double): CreatePaymentResponse? {
        return try {
            val req = CreatePaymentRequest(bookingId.toString(), email, amount)
            networkProvider.apiService.createPayment(req)
        } catch (e: Exception) {
            null
        }
    }

    // For bookings that actually exist in Supabase (real uuid), unlike the
    // Long-based overload above which stringifies a local Room id that
    // never matches a real Supabase booking.
    suspend fun createPaymentForRealBooking(bookingId: String, email: String, amount: Double): CreatePaymentResponse {
        val req = CreatePaymentRequest(bookingId, email, amount)
        return networkProvider.apiService.createPayment(req)
    }

    // Real status check against Supabase Postgrest - reflects whatever the
    // paystack-webhook Edge Function actually recorded after a genuine
    // Paystack payment. There is no client-side "mark as paid" anymore:
    // the only way a payment becomes "success" is Paystack calling the
    // real webhook. Returns null if the row can't be found/read.
    suspend fun checkPaymentStatus(reference: String): String? {
        return try {
            SupabaseProvider.client.postgrest.from("payments")
                .select {
                    filter { eq("reference", reference) }
                }
                .decodeSingleOrNull<SupabasePaymentStatus>()
                ?.status
        } catch (e: Exception) {
            null
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
    suspend fun seedDatabaseIfEmpty() {
        val admin = dao.getProfileByEmail("admin@sashio.com")
        if (admin == null) {
            // Register default accounts
            val adminId = dao.insertProfile(
                    UserProfile(name = "Sachio Administrator", email = "admin@sashio.com", role = "admin", phoneNumber = "08111223344", companyName = "Sachio Logistics Ltd")
            )
            val staffAId = dao.insertProfile(
                UserProfile(name = "David Staff James", email = "staff@sashio.com", role = "staff", phoneNumber = "08022334455")
            )
            val staffBId = dao.insertProfile(
                UserProfile(name = "Blessing Staff Okon", email = "staff2@sashio.com", role = "staff", phoneNumber = "08033445566")
            )
            val clientId1 = dao.insertProfile(
                UserProfile(name = "Alhaji Farouk Bello", email = "client@sashio.com", role = "client", phoneNumber = "08044556677", companyName = "Belco Global Ltd")
            )
            val clientId2 = dao.insertProfile(
                UserProfile(name = "Premium Event Planner Ltd", email = "client2@sashio.com", role = "client", phoneNumber = "08055667788", companyName = "Eko Glitz Events")
            )

            // Seed favorite locations for client 1
            dao.insertFavoriteLocation(
                FavoriteLocation(
                    clientId = clientId1,
                    name = "Home Villa",
                    address = "Plot 12, Adetokunbo Ademola, Victoria Island, Lagos",
                    latitude = 6.4278,
                    longitude = 3.4251,
                    locationType = "Home"
                )
            )
            dao.insertFavoriteLocation(
                FavoriteLocation(
                    clientId = clientId1,
                    name = "Belco Headquarters",
                    address = "Kingsway Tower, Alfred Rewane Road, Ikoyi, Lagos",
                    latitude = 6.4522,
                    longitude = 3.4359,
                    locationType = "Office"
                )
            )
            dao.insertFavoriteLocation(
                FavoriteLocation(
                    clientId = clientId1,
                    name = "Victoria Island Event Palace",
                    address = "Ligali Ayorinde Street, Victoria Island, Lagos",
                    latitude = 6.4326,
                    longitude = 3.4412,
                    locationType = "Event Center"
                )
            )

            val now = System.currentTimeMillis()

            // 1. Quotation Requested Status
            dao.insertBooking(
                Booking(
                    clientId = clientId1,
                    clientName = "Alhaji Farouk Bello",
                    clientPhone = "08044556677",
                    clientEmail = "client@sashio.com",
                    companyName = "Belco Global Ltd",
                    serviceMode = "Rental",
                    serviceType = "Presidential Luxury Mobile Restrooms",
                    variantName = "Presidential Five-Cabin Restroom Suite",
                    modelNumber = "S-1000 Sovereign",
                    quantity = 2,
                    duration = "3 Days",
                    address = "Eko Atlantic Promenade, Victoria Island, Lagos",
                    latitude = 6.4253,
                    longitude = 3.4243,
                    eventType = "VIP Wedding Reception",
                    guestCount = 450,
                    eventDate = "2026-06-20",
                    cleaningSupport = true,
                    onSiteAttendant = true,
                    wasteEvacuation = true,
                    maintenanceSupport = true,
                    specialInstructions = "Requires ultra-premium gold hardware fittings and fragrance diffuser setups",
                    status = "Quotation Requested",
                    createdAt = now - 5 * 3600_000
                )
            )

            // 2. Under Review Status
            dao.insertBooking(
                Booking(
                    clientId = clientId1,
                    clientName = "Alhaji Farouk Bello",
                    clientPhone = "08044556677",
                    clientEmail = "client@sashio.com",
                    companyName = "Belco Global Ltd",
                    serviceMode = "Rental",
                    serviceType = "VIP Portable Toilets",
                    variantName = "Executive VIP Cabin",
                    modelNumber = "S-400 Executive",
                    quantity = 5,
                    duration = "1 Week",
                    address = "Allen Avenue, Ikeja, Lagos, Nigeria",
                    latitude = 6.5982,
                    longitude = 3.3541,
                    eventType = "Corporate Summit",
                    guestCount = 200,
                    eventDate = "2026-06-25",
                    cleaningSupport = true,
                    onSiteAttendant = false,
                    wasteEvacuation = true,
                    status = "Under Review",
                    createdAt = now - 24 * 3600_000
                )
            )

            // 3. Quotation Generated & Sent Status
            dao.insertBooking(
                Booking(
                    clientId = clientId2,
                    clientName = "Premium Event Planner Ltd",
                    clientPhone = "08055667788",
                    clientEmail = "client2@sashio.com",
                    companyName = "Eko Glitz Events",
                    serviceMode = "Rental",
                    serviceType = "Executive Portable Toilets",
                    variantName = "Luxury AC Dual cabin",
                    modelNumber = "E-200 Deluxe",
                    quantity = 3,
                    duration = "1 Day",
                    address = "Landmark Event Center, Oniru, Victoria Island",
                    latitude = 6.4282,
                    longitude = 3.4443,
                    eventType = "Tech Conference Launch",
                    guestCount = 800,
                    eventDate = "2026-06-15",
                    cleaningSupport = true,
                    onSiteAttendant = true,
                    wasteEvacuation = false,
                    specialInstructions = "Require branded outer body wrap designs",
                    quotedPrice = 550000.0,
                    logisticsCost = 85000.0,
                    vatAmount = 47625.0,
                    adminNotes = "High-density crowd planned. Maintenance team dispatched in standby van.",
                    quoteIssuedDate = now - 8 * 3600_000,
                    status = "Quotation Sent",
                    createdAt = now - 12 * 3600_000
                )
            )

            // 4. Client Accepted & Awaiting Payment
            dao.insertBooking(
                Booking(
                    clientId = clientId1,
                    clientName = "Alhaji Farouk Bello",
                    clientPhone = "08044556677",
                    clientEmail = "client@sashio.com",
                    companyName = "Belco Global Ltd",
                    serviceMode = "Purchase",
                    serviceType = "Standard Portable Toilets",
                    variantName = "Eco Single Cabin HD",
                    modelNumber = "S-100 Classic",
                    quantity = 10,
                    duration = "Custom", // N/A Purchase
                    address = "GRA Phase 2 Recreation Hub, Port Harcourt, Nigeria",
                    latitude = 4.8211,
                    longitude = 7.0084,
                    eventType = "Construction Site Setups",
                    guestCount = 120,
                    eventDate = "2026-07-01",
                    cleaningSupport = false,
                    onSiteAttendant = false,
                    wasteEvacuation = false,
                    quotedPrice = 4500000.0,
                    logisticsCost = 350000.0,
                    vatAmount = 363750.0,
                    adminNotes = "Direct delivery to warehouse depot. Customer handles site offloading.",
                    quoteIssuedDate = now - 18 * 3600_000,
                    status = "Client Accepted",
                    createdAt = now - 36 * 3600_000
                )
            )

            // 5. Booking Confirmed (Paid)
            val bookingPaidId = dao.insertBooking(
                Booking(
                    clientId = clientId1,
                    clientName = "Alhaji Farouk Bello",
                    clientPhone = "08044556677",
                    clientEmail = "client@sashio.com",
                    companyName = "Belco Global Ltd",
                    serviceMode = "Rental",
                    serviceType = "Presidential Luxury Mobile Restrooms",
                    variantName = "Presidential Sovereign Cabin Range",
                    modelNumber = "S-900 Ambassador",
                    quantity = 1,
                    duration = "1 Week",
                    address = "Adetokunbo Ademola Crescent, Wuse II, Abuja, Nigeria",
                    latitude = 9.0778,
                    longitude = 7.4812,
                    eventType = "Government Summit Gala",
                    guestCount = 150,
                    eventDate = "2026-06-12",
                    cleaningSupport = true,
                    onSiteAttendant = true,
                    wasteEvacuation = true,
                    maintenanceSupport = true,
                    quotedPrice = 1800000.0,
                    logisticsCost = 150000.0,
                    vatAmount = 146250.0,
                    adminNotes = "Security clearance required for the crew. Vehicle registration details generated.",
                    quoteIssuedDate = now - 30 * 3600_000,
                    status = "Booking Confirmed",
                    paymentStatus = "success",
                    paymentRef = "PAY-ABJ-SUM-990P",
                    createdAt = now - 48 * 3600_000
                )
            )
            dao.insertPayment(
                Payment(
                    bookingId = bookingPaidId,
                    amount = 2096250.0, // Subtotal + Logistics + VAT
                    reference = "PAY-ABJ-SUM-990P",
                    status = "success",
                    paymentDate = now - 28 * 3600_000
                )
            )

            // 6. Service Delivered
            dao.insertBooking(
                Booking(
                    clientId = clientId1,
                    clientName = "Alhaji Farouk Bello",
                    clientPhone = "08044556677",
                    clientEmail = "client@sashio.com",
                    companyName = "Belco Global Ltd",
                    serviceMode = "Rental",
                    serviceType = "Cleaning & Maintenance Services",
                    variantName = "Elite Site Sanitization",
                    modelNumber = "CMS-Daily",
                    quantity = 1,
                    duration = "3 Days",
                    address = "Broad Street, Marina, Lagos, Nigeria",
                    latitude = 6.4531,
                    longitude = 3.3958,
                    eventType = "Commercial Carnivals",
                    guestCount = 1200,
                    eventDate = "2026-06-05",
                    cleaningSupport = true,
                    onSiteAttendant = true,
                    wasteEvacuation = true,
                    quotedPrice = 300000.0,
                    logisticsCost = 45000.0,
                    vatAmount = 25875.0,
                    quoteIssuedDate = now - 50 * 3600_000,
                    status = "SERVICE DELIVERED",
                    paymentStatus = "success",
                    paymentRef = "PAY-MAR-CAR-771T",
                    assignedStaffId = staffAId,
                    assignedStaffName = "David Staff James",
                    createdAt = now - 72 * 3600_000
                )
            )

            // Seed default consultations
            dao.insertConsultation(
                Consultation(
                    clientId = clientId1,
                    clientName = "Alhaji Farouk Bello",
                    clientPhone = "08044556677",
                    clientEmail = "client@sashio.com",
                    type = "Schedule Site Inspection",
                    preferredDate = "2026-06-10",
                    status = "Scheduled",
                    notes = "Inspection of Eko Atlantic Wedding plot regarding weight capacity for restroom trailer tow vehicles."
                )
            )
            dao.insertConsultation(
                Consultation(
                    clientId = clientId1,
                    clientName = "Alhaji Farouk Bello",
                    clientPhone = "08044556677",
                    clientEmail = "client@sashio.com",
                    type = "Book Consultation",
                    preferredDate = "2026-06-08",
                    status = "Pending",
                    notes = "Virtual consultation to discuss Event Sanitation Packages for an upcoming 3-day musical concert."
                )
            )

            // Seed testimonials & reviews for catalog products
            dao.insertReview(
                Review(
                    userName = "Chief Adebayo Alao",
                    rating = 5,
                    comment = "The presidential bathroom was the talk of our daughter's wedding! Smelled purely like roses, air conditioning worked perfectly, and the lighting was top notch.",
                    category = "Presidential Luxury Mobile Restrooms",
                    date = "June 2, 2026"
                )
            )
            dao.insertReview(
                Review(
                    userName = "Dr. Linda Benson",
                    rating = 5,
                    comment = "Our corporate construction team expressed high satisfaction with these executive cabins. Excellent water pressure and easy maintenance.",
                    category = "Executive Portable Toilets",
                    date = "May 28, 2026"
                )
            )
            dao.insertReview(
                Review(
                    userName = "Senator Jude K.",
                    rating = 5,
                    comment = "Truly premium service. Their attendants on-site were incredibly professional, keeping the VIP toilets pristine throughout the 2-day summit.",
                    category = "VIP Portable Toilets",
                    date = "May 15, 2026"
                )
            )

            // Seed default catalog items
            val defaultCatalog = listOf(
                CatalogItem(
                    title = "Standard Portable Toilets", 
                    description = "Ideal for high-throughput construction, agricultural workspaces, and community events. Robust, ventilated single-cab units.", 
                    modelNumber = "S-100 Classic", 
                    variantName = "Classic Single HD Cabin",
                    features = "• Anti-slip floor\n• High capacity waste tank\n• Urinal inside",
                    specifications = "Weight: ~75kg\nDimensions: 110x120x230cm\nMaterials: High-density polyethylene",
                    benefits = "Durable, easy to clean, scalable for large crowds.",
                    useCases = "Construction sites, Open concerts, Marathons, Farm events",
                    imageUrl = "https://images.unsplash.com/photo-1590435348301-49b4f0b0c609?q=80&w=600&auto=format&fit=crop"
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
                CatalogItem(title = "VIP Portable Toilets", description = "Ultra-high quality materials, integrated wash basins, luxury paper, deodorizers, and beautiful warm led lighting installations.", modelNumber = "V-400 Executive", variantName = "Premium VIP Flush Single"),
                CatalogItem(title = "Presidential Luxury Mobile Restrooms", description = "Multicabin presidential restrooms equipped with air conditioning, surround sound audio systems, and high-end mahogany counter trims.", modelNumber = "S-1000 Sovereign", variantName = "Presidential Trailing Suite", features = "• Mahogany Trims\n• Surround Sound\n• Fully Air-Conditioned", useCases = "High-society weddings, Government functions"),
                CatalogItem(title = "Mobile Shower Units", description = "Hot & cold hydro-pumping shower stalls. Excellent for mining layouts, military setups, and multi-day sports tournaments.", modelNumber = "MS-50 Shower", variantName = "Twin Restroom-Shower Suite"),
                CatalogItem(title = "Accessible Toilets", description = "Fully ADA wheelchair compliant and spacious floor-grade entries equipped with heavy robust handrail grips.", modelNumber = "A-500 Accessible", variantName = "Safe Cabin Comfort"),
                CatalogItem(title = "Event Sanitation Packages", description = "Complete modular packages: 5 Standard + 2 VIP + Onsite Standby pumpers and attendants for high scale gatherings.", modelNumber = "ESP-Multi", variantName = "Grand Wedding Special Package"),
                CatalogItem(title = "Corporate Site Sanitation Solutions", description = "Tailored packages with scheduled weekly extraction logs, servicing standard corporate expansion bases.", modelNumber = "CSS-Corp", variantName = "Industrial Workspace Unit"),
                CatalogItem(title = "Waste Evacuation Services", description = "High density quad-tank vacuum suction pump trucks for waste collection, transfer, and highly sanitized operations.", modelNumber = "WES-Truck", variantName = "4000L Quad-Tank Pumper-Truck"),
                CatalogItem(title = "Cleaning & Maintenance Services", description = "Uniformed standby sanitization crew dispatched to sites on-shift to scrub, empty, and refresh interiors constantly.", modelNumber = "CMS-Daily", variantName = "Standby Attendant Shift Crew")
            )
            for (c in defaultCatalog) {
                dao.insertCatalogItem(c)
            }
        }
    }
}
