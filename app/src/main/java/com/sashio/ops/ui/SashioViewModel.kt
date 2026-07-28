package com.sashio.ops.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sashio.ops.data.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLEncoder

data class AdminIncident(
    val id: String,
    val title: String,
    val reporter: String,
    val priority: String, // "Low", "Medium", "High", "Critical"
    val status: String, // "Open", "Investigating", "Resolved"
    val date: String,
    val details: String
)

data class AdminStaffAttendance(
    val staffId: Long,
    val staffName: String,
    val clockInTime: String,
    val clockOutTime: String,
    val workingHours: Double,
    val locationVerified: String,
    val isVerified: Boolean
)

data class AdminExpenseClaim(
    val id: String,
    val staffName: String,
    val category: String, // "Fuel Costs", "Maintenance Costs", "Staff Expenses"
    val amount: Double,
    val details: String,
    val status: String, // "Pending Approval", "Approved", "Rejected"
    val date: String
)

@OptIn(ExperimentalCoroutinesApi::class)
class SashioViewModel(application: Application) : AndroidViewModel(application) {
    private val database = SashioDatabase.getDatabase(application)
    private val repository = SashioRepository(application, database)

    // --- State Management ---
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // --- Extra Profile Fields for Premium Settings ---
    private val _userAltPhone = MutableStateFlow("")
    val userAltPhone = _userAltPhone.asStateFlow()

    private val _userBio = MutableStateFlow("Pioneering prestige event management logistics across major coordinates.")
    val userBio = _userBio.asStateFlow()

    private val _userBusinessAddress = MutableStateFlow("Suite A-18, Lekki Admiralty Way, Phase 1, Lagos")
    val userBusinessAddress = _userBusinessAddress.asStateFlow()

    private val _userProfilePic = MutableStateFlow("https://i.ibb.co/4ZM1G4WW/1000946646-removebg-preview.png")
    val userProfilePic = _userProfilePic.asStateFlow()

    private val _userLocation = MutableStateFlow("Victoria Island, Lagos, Nigeria")
    val userLocation = _userLocation.asStateFlow()

    // --- Settings Preferences ---
    private val _themePreference = MutableStateFlow("dark") // ui is dark by default
    val themePreference = _themePreference.asStateFlow()

    private val _accentColor = MutableStateFlow("Mustard") // Mustard (Logo Color), Golden, Charcoal, Slate
    val accentColor = _accentColor.asStateFlow()

    private val _fontSizeScale = MutableStateFlow("Standard") // Compact, Standard, Expanded, Large
    val fontSizeScale = _fontSizeScale.asStateFlow()

    private val _cardDensity = MutableStateFlow("Comfortable") // Comfortable, High Density, Minimal
    val cardDensity = _cardDensity.asStateFlow()

    private val _mapTheme = MutableStateFlow("Dark Map") // Standard, Satellite, Terrain, Dark Map
    val mapTheme = _mapTheme.asStateFlow()

    private val _accessibilityMode = MutableStateFlow(false)
    val accessibilityMode = _accessibilityMode.asStateFlow()

    private val _autoRefresh = MutableStateFlow(true)
    val autoRefresh = _autoRefresh.asStateFlow()

    private val _locationServicesEnabled = MutableStateFlow(true)
    val locationServicesEnabled = _locationServicesEnabled.asStateFlow()

    private val _backgroundTrackingEnabled = MutableStateFlow(true)
    val backgroundTrackingEnabled = _backgroundTrackingEnabled.asStateFlow()

    private val _offlineModeEnabled = MutableStateFlow(false)
    val offlineModeEnabled = _offlineModeEnabled.asStateFlow()

    private val _automaticDownloads = MutableStateFlow(true)
    val automaticDownloads = _automaticDownloads.asStateFlow()

    private val _cacheManagementSize = MutableStateFlow("32.4 MB")
    val cacheManagementSize = _cacheManagementSize.asStateFlow()

    private val _storageUsed = MutableStateFlow("118 MB")
    val storageUsed = _storageUsed.asStateFlow()

    // Notifications Enable Toggles
    private val _pushNotifications = MutableStateFlow(true)
    val pushNotifications = _pushNotifications.asStateFlow()

    private val _smsNotifications = MutableStateFlow(true)
    val smsNotifications = _smsNotifications.asStateFlow()

    private val _emailNotifications = MutableStateFlow(true)
    val emailNotifications = _emailNotifications.asStateFlow()

    private val _whatsappNotifications = MutableStateFlow(true)
    val whatsappNotifications = _whatsappNotifications.asStateFlow()

    private val _notifyBookingUpdates = MutableStateFlow(true)
    val notifyBookingUpdates = _notifyBookingUpdates.asStateFlow()

    private val _notifyQuotationUpdates = MutableStateFlow(true)
    val notifyQuotationUpdates = _notifyQuotationUpdates.asStateFlow()

    private val _notifyPaymentConfirmations = MutableStateFlow(true)
    val notifyPaymentConfirmations = _notifyPaymentConfirmations.asStateFlow()

    private val _notifySystemAnnouncements = MutableStateFlow(true)
    val notifySystemAnnouncements = _notifySystemAnnouncements.asStateFlow()

    private val _pushSound = MutableStateFlow("Premium Chime")
    val pushSound = _pushSound.asStateFlow()

    // Privacy Controls
    private val _privacyProfilePicVisible = MutableStateFlow("Public") // Public, Private, Custom
    val privacyProfilePicVisible = _privacyProfilePicVisible.asStateFlow()

    private val _privacyContactVisible = MutableStateFlow("Public")
    val privacyContactVisible = _privacyContactVisible.asStateFlow()

    private val _privacyTrackingShare = MutableStateFlow("During Active Dispatches")
    val privacyTrackingShare = _privacyTrackingShare.asStateFlow()

    private val _privacySupportAccess = MutableStateFlow(true)
    val privacySupportAccess = _privacySupportAccess.asStateFlow()

    private val _privacyAnalyticsAllowed = MutableStateFlow(true)
    val privacyAnalyticsAllowed = _privacyAnalyticsAllowed.asStateFlow()

    // Security Settings
    private val _securityBiometricsEnabled = MutableStateFlow(true)
    val securityBiometricsEnabled = _securityBiometricsEnabled.asStateFlow()

    private val _security2FAEnabled = MutableStateFlow(false)
    val security2FAEnabled = _security2FAEnabled.asStateFlow()

    private val _securityScore = MutableStateFlow("Excellent") // Low, Medium, High, Excellent
    val securityScore = _securityScore.asStateFlow()

    // Languages
    private val _selectedLanguage = MutableStateFlow("English") // English, French, Spanish, Swahili, Arabic
    val selectedLanguage = _selectedLanguage.asStateFlow()

    // Support Request lists
    private val _ticketHistory = MutableStateFlow<List<SupportTicket>>(emptyList())
    val ticketHistory = _ticketHistory.asStateFlow()

    // Update handlers
    fun updateUserProfileInfo(name: String, email: String, phone: String, company: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(name = name, email = email, phoneNumber = phone, companyName = company)
            repository.insertProfile(updated)
            _currentUser.value = updated
        }
    }

    fun updateExtraProfileInfo(altPhone: String, bio: String, bizAddress: String, profilePic: String, location: String) {
        _userAltPhone.value = altPhone
        _userBio.value = bio
        _userBusinessAddress.value = bizAddress
        _userProfilePic.value = profilePic
        _userLocation.value = location
    }

    fun updateNotificationToggles(
        push: Boolean, sms: Boolean, email: Boolean, whatsapp: Boolean,
        bookings: Boolean, quotes: Boolean, payments: Boolean, system: Boolean,
        sound: String
    ) {
        _pushNotifications.value = push
        _smsNotifications.value = sms
        _emailNotifications.value = email
        _whatsappNotifications.value = whatsapp
        _notifyBookingUpdates.value = bookings
        _notifyQuotationUpdates.value = quotes
        _notifyPaymentConfirmations.value = payments
        _notifySystemAnnouncements.value = system
        _pushSound.value = sound
    }

    fun updatePrivacyToggles(
        profilePic: String, contact: String, tracking: String, support: Boolean, analytics: Boolean
    ) {
        _privacyProfilePicVisible.value = profilePic
        _privacyContactVisible.value = contact
        _privacyTrackingShare.value = tracking
        _privacySupportAccess.value = support
        _privacyAnalyticsAllowed.value = analytics
    }

    fun updateSecurityToggles(biometrics: Boolean, m2FA: Boolean) {
        _securityBiometricsEnabled.value = biometrics
        _security2FAEnabled.value = m2FA
        _securityScore.value = if (biometrics && m2FA) "Excellent" else if (biometrics || m2FA) "High" else "Medium"
    }

    fun updateAppearanceToggles(
        theme: String, accent: String, font: String, density: String, map: String, access: Boolean
    ) {
        _themePreference.value = theme
        _accentColor.value = accent
        _fontSizeScale.value = font
        _cardDensity.value = density
        _mapTheme.value = map
        _accessibilityMode.value = access
    }

    fun updateAppPreferencesToggles(
        refresh: Boolean, location: Boolean, bgTrack: Boolean, offline: Boolean, downloads: Boolean
    ) {
        _autoRefresh.value = refresh
        _locationServicesEnabled.value = location
        _backgroundTrackingEnabled.value = bgTrack
        _offlineModeEnabled.value = offline
        _automaticDownloads.value = downloads
    }

    fun clearCache() {
        _cacheManagementSize.value = "0.0 B"
        _storageUsed.value = "85.6 MB"
    }

    fun clearDownloads() {
        _storageUsed.value = "12.0 MB"
    }

    fun optimizeStorage() {
        _cacheManagementSize.value = "4.2 MB"
        _storageUsed.value = "48.1 MB"
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun submitSupportTicket(subject: String, category: String, description: String) {
        val list = _ticketHistory.value.toMutableList()
        val ticket = SupportTicket(
            id = "SCH-${1000 + list.size}",
            subject = subject,
            category = category,
            description = description,
            status = "Open",
            date = "June 12, 2026 at 13:30"
        )
        list.add(0, ticket)
        _ticketHistory.value = list
    }

    fun deactivateAccount() {
        _currentUser.value = null
        _currentScreen.value = "login"
    }

    fun deleteAccount() {
        val user = _currentUser.value
        if (user != null) {
            viewModelScope.launch {
                // Remove or deactivate profile in DB if needed; for now, log out and close
                _currentUser.value = null
                _currentScreen.value = "login"
            }
        }
    }

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _signUpError = MutableStateFlow<String?>(null)
    val signUpError: StateFlow<String?> = _signUpError.asStateFlow()

    // Screen state
    private val _currentScreen = MutableStateFlow("splash") // splash, login, register, forgot_password, client_dashboard, staff_dashboard, admin_dashboard
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Core Listings
    val allBookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffProfiles: StateFlow<List<UserProfile>> = repository.staffProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allConsultations: StateFlow<List<Consultation>> = repository.allConsultations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<Review>> = repository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val catalogItems: StateFlow<List<CatalogItem>> = repository.allCatalogItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Enterprise Admin Center Flows ---
    val allProfiles: StateFlow<List<UserProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _suspendedProfileIds = MutableStateFlow<Set<Long>>(emptySet())
    val suspendedProfileIds: StateFlow<Set<Long>> = _suspendedProfileIds.asStateFlow()

    private val _incidents = MutableStateFlow<List<AdminIncident>>(
        listOf(
            AdminIncident("INC-101", "Pressure Pump Leakage", "Blessing Okon", "High", "Open", "June 12, 2026", "Lekki event VIP Cabin water pressure drops."),
            AdminIncident("INC-102", "Driver Cabin AC Issue", "David James", "Medium", "Investigating", "June 11, 2026", "Secondary rest ventilation failure."),
            AdminIncident("INC-103", "Client clean dispute at Oniru", "Eko Glitz Events", "Low", "Resolved", "June 10, 2026", "Resolved by rapid team cleaning dispatch.")
        )
    )
    val incidents: StateFlow<List<AdminIncident>> = _incidents.asStateFlow()

    private val _attendanceLogs = MutableStateFlow<List<AdminStaffAttendance>>(
        listOf(
            AdminStaffAttendance(2, "David Staff James", "07:30 AM", "05:45 PM", 10.25, "Lekki Coordinates Verified (6.4381, 3.4823)", true),
            AdminStaffAttendance(3, "Blessing Staff Okon", "08:00 AM", "Pending", 8.0, "VI Central Depot Verified (6.4253, 3.4243)", true)
        )
    )
    val attendanceLogs: StateFlow<List<AdminStaffAttendance>> = _attendanceLogs.asStateFlow()

    private val _expenseLogs = MutableStateFlow<List<AdminExpenseClaim>>(
        listOf(
            AdminExpenseClaim("EXP-900", "David James", "Fuel Costs", 24000.0, "Diesel for Lekki VIP Dispatch Caravan", "Pending Approval", "June 12, 2026"),
            AdminExpenseClaim("EXP-901", "Blessing Okon", "Maintenance Costs", 15000.0, "Eco Blue deodorant liquid fluid refills", "Approved", "June 11, 2026"),
            AdminExpenseClaim("EXP-902", "David James", "Staff Expenses", 8000.0, "On-site attendant meals allowance", "Approved", "June 11, 2026")
        )
    )
    val expenseLogs: StateFlow<List<AdminExpenseClaim>> = _expenseLogs.asStateFlow()

    fun toggleProfileSuspension(id: Long) {
        _suspendedProfileIds.value = if (_suspendedProfileIds.value.contains(id)) {
            _suspendedProfileIds.value - id
        } else {
            _suspendedProfileIds.value + id
        }
    }

    fun addNewStaffProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            repository.registerProfile(name = name, email = email, role = "staff", phone = phone)
        }
    }

    fun createIncident(title: String, reporter: String, priority: String, details: String) {
        val current = _incidents.value.toMutableList()
        val newId = "INC-${100 + current.size + 1}"
        current.add(0, AdminIncident(newId, title, reporter, priority, "Open", "June 12, 2026", details))
        _incidents.value = current
    }

    fun approveExpenseClaim(id: String) {
        val current = _expenseLogs.value.map {
            if (it.id == id) it.copy(status = "Approved") else it
        }
        _expenseLogs.value = current
    }

    fun rejectExpenseClaim(id: String) {
        val current = _expenseLogs.value.map {
            if (it.id == id) it.copy(status = "Rejected") else it
        }
        _expenseLogs.value = current
    }

    fun submitExpenseFromAdmin(staffName: String, category: String, amount: Double, details: String) {
        val current = _expenseLogs.value.toMutableList()
        val newId = "EXP-${900 + current.size + 1}"
        current.add(0, AdminExpenseClaim(newId, staffName, category, amount, details, "Approved", "June 12, 2026"))
        _expenseLogs.value = current
    }

    // Role-Filtered Bookings & Consultations
    val clientBookings: StateFlow<List<Booking>> = _currentUser
        .flatMapLatest { user ->
            if (user != null && user.role == "client") {
                repository.getBookingsForClient(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clientFavoriteLocations: StateFlow<List<FavoriteLocation>> = _currentUser
        .flatMapLatest { user ->
            if (user != null && user.role == "client") {
                repository.getFavoriteLocationsForClient(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Real-time Active Location Tracking State Map ---
    private val _activeLiveTrackers = MutableStateFlow<Map<Long, LiveTrackingState>>(emptyMap())
    val activeLiveTrackers: StateFlow<Map<Long, LiveTrackingState>> = _activeLiveTrackers.asStateFlow()

    val clientConsultations: StateFlow<List<Consultation>> = _currentUser
        .flatMapLatest { user ->
            if (user != null && user.role == "client") {
                repository.getConsultationsForClient(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffBookings: StateFlow<List<Booking>> = _currentUser
        .flatMapLatest { user ->
            if (user != null && user.role == "staff") {
                repository.getBookingsForStaff(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Address Lookup Simulator State
    private val _addressSearchQuery = MutableStateFlow("")
    val addressSearchQuery: StateFlow<String> = _addressSearchQuery.asStateFlow()

    private val _addressSuggestions = MutableStateFlow<List<SimulatedPlace>>(emptyList())
    val addressSuggestions: StateFlow<List<SimulatedPlace>> = _addressSuggestions.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // --- Navigation & Auth Actions ---
    fun setScreen(screen: String) {
        _currentScreen.value = screen
        _loginError.value = null
        _signUpError.value = null
    }

    fun loginWithEmail(email: String, requiredRole: String? = null, password: String = "") {
        viewModelScope.launch {
            _loginError.value = null

            if (SupabaseProvider.SUPABASE_URL == "YOUR_SUPABASE_URL") {
                // FALLBACK LOGIN FOR PREVIEW / OFFLINE MODE
                val user = repository.getProfileByEmail(email.trim().lowercase())
                if (user == null) {
                    _loginError.value = "Account not found. Try client@sashio.com, staff@sashio.com, or admin@sashio.com"
                    return@launch
                }

                if (requiredRole != null && user.role != requiredRole) {
                    _loginError.value = "Access Denied: Highly secured portal. Privileges unmatched."
                    return@launch
                }

                _loginError.value = null
                _currentUser.value = user

                when (user.role) {
                    "client" -> _currentScreen.value = "client_dashboard"
                    "staff" -> _currentScreen.value = "staff_dashboard"
                    "admin" -> _currentScreen.value = "admin_dashboard"
                }
                return@launch
            }

            // REAL SUPABASE + POSTGRESQL FLOW
            try {
                _loginError.value = "Connecting to SACHIO Secure Shield Auth..."
                
                SupabaseProvider.client.auth.signInWith(Email) {
                    this.email = email.trim().lowercase()
                    this.password = password
                }

                val finalUser = SupabaseProvider.client.auth.currentUserOrNull()
                val uid = finalUser?.id
                if (uid == null) {
                    _loginError.value = "Authentication failed: No valid session returned from Supabase."
                    return@launch
                }

                _loginError.value = "Retrieving Enterprise Security profile..."
                
                // Fetch profile record
                val dbProfile = SupabaseProvider.client.postgrest.from("profiles")
                    .select {
                        filter {
                            eq("id", uid)
                        }
                    }.decodeSingle<SupabaseProfile>()

                val mappedRole = dbProfile.role // 'client' | 'staff' | 'admin'
                if (requiredRole != null && mappedRole != requiredRole) {
                    _loginError.value = "Access Denied: Highly secured portal. Privileges unmatched for role '$requiredRole'."
                    return@launch
                }

                val user = UserProfile(
                    id = 1,
                    name = dbProfile.full_name ?: email,
                    email = dbProfile.email,
                    role = mappedRole,
                    phoneNumber = dbProfile.phone_number ?: "",
                    companyName = dbProfile.company_name ?: ""
                )

                _loginError.value = null
                _currentUser.value = user

                when (mappedRole) {
                    "client" -> _currentScreen.value = "client_dashboard"
                    "staff" -> _currentScreen.value = "staff_dashboard"
                    "admin" -> _currentScreen.value = "admin_dashboard"
                }
            } catch (e: Exception) {
                _loginError.value = "Supabase Shield Connection: ${e.localizedMessage ?: e.message ?: "Invalid credentials or network failure"}"
            }
        }
    }


    fun signUpUser(name: String, email: String, role: String, phone: String, company: String = "", password: String = "") {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            _signUpError.value = "All fields except company are mandatory"
            return
        }
        if (password.length < 6) {
            _signUpError.value = "Password must be at least 6 characters"
            return
        }
        // Public sign-up always creates a client account - role is not
        // caller-controlled. Staff/admin are granted later via the
        // ManageUserRolesPanel by an existing admin (or the DB trigger
        // blocks it anyway).
        val safeRole = "client"
        viewModelScope.launch {
            val existing = repository.getProfileByEmail(email.trim().lowercase())
            if (existing != null) {
                _signUpError.value = "Email address already exists in the system"
                return@launch
            }

            // Creates the real Supabase account - this is what makes the user
            // actually show up in Supabase Authentication / the profiles table.
            // "Confirm email" should be OFF in Supabase for this to log the
            // user in immediately without needing an email verification step.
            try {
                SupabaseProvider.client.auth.signUpWith(Email) {
                    this.email = email.trim().lowercase()
                    this.password = password
                }
                val supaUser = SupabaseProvider.client.auth.currentUserOrNull()
                if (supaUser != null) {
                    SupabaseProvider.client.postgrest.from("profiles").update(
                        mapOf(
                            "full_name" to name.trim(),
                            "phone_number" to phone.trim(),
                            "company_name" to company.trim()
                        )
                    ) {
                        filter { eq("id", supaUser.id) }
                    }
                }
            } catch (e: Exception) {
                _signUpError.value = "Could not create account: ${e.message}"
                return@launch
            }

            // Mirrors into the local Room database, since the rest of the app
            // still runs on local Long ids, not Supabase uuids yet.
            val userId = repository.registerProfile(
                name = name.trim(),
                email = email.trim().lowercase(),
                role = safeRole,
                phone = phone.trim(),
                company = company.trim()
            )

            _signUpError.value = null
            val newUser = repository.getProfileById(userId)
            _currentUser.value = newUser

            if (newUser != null) {
                when (newUser.role) {
                    "client" -> _currentScreen.value = "client_dashboard"
                    "staff" -> _currentScreen.value = "staff_dashboard"
                    "admin" -> _currentScreen.value = "admin_dashboard"
                }
            }
        }
    }

    // --- Admin: real Supabase role management ---
    // Independent of the local Room-based staff list above - this talks
    // directly to the live profiles table, since that's what actually
    // controls access (the DB trigger blocks non-admins from changing role;
    // this lets an actual admin do it from within the app).
    private val _supabaseProfiles = MutableStateFlow<List<AdminProfileRow>>(emptyList())
    val supabaseProfiles: StateFlow<List<AdminProfileRow>> = _supabaseProfiles.asStateFlow()

    private val _roleManagementError = MutableStateFlow<String?>(null)
    val roleManagementError: StateFlow<String?> = _roleManagementError.asStateFlow()

    fun loadSupabaseProfiles() {
        viewModelScope.launch {
            try {
                val rows = SupabaseProvider.client.postgrest.from("profiles")
                    .select()
                    .decodeList<AdminProfileRow>()
                _supabaseProfiles.value = rows
                _roleManagementError.value = null
            } catch (e: Exception) {
                _roleManagementError.value = "Could not load users: ${e.message}"
            }
        }
    }

    // --- Admin: real Supabase bookings (quotations console) ---
    private val _supabaseBookings = MutableStateFlow<List<SupabaseBooking>>(emptyList())
    val supabaseBookings: StateFlow<List<SupabaseBooking>> = _supabaseBookings.asStateFlow()

    fun loadSupabaseBookings() {
        viewModelScope.launch {
            try {
                val rows = SupabaseProvider.client.postgrest.from("bookings")
                    .select()
                    .decodeList<SupabaseBooking>()
                _supabaseBookings.value = rows
                _roleManagementError.value = null
            } catch (e: Exception) {
                _roleManagementError.value = "Could not load bookings: ${e.message}"
            }
        }
    }

    fun adminIssueQuotation(
        bookingId: String,
        quotedPrice: Double,
        logisticsCost: Double,
        vatAmount: Double,
        adminNotes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("bookings").update(
                    mapOf(
                        "quoted_price" to quotedPrice,
                        "logistics_cost" to logisticsCost,
                        "vat_amount" to vatAmount,
                        "admin_notes" to adminNotes,
                        "status" to "Quotation Generated"
                    )
                ) {
                    filter { eq("id", bookingId) }
                }
                loadSupabaseBookings()
                onComplete()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not issue quotation: ${e.message}"
            }
        }
    }

    fun adminSetUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("profiles").update(
                    mapOf("role" to newRole)
                ) {
                    filter { eq("id", userId) }
                }
                loadSupabaseProfiles()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not update role: ${e.message}"
            }
        }
    }

    fun adminSetProfileSuspended(userId: String, suspended: Boolean) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("profiles").update(
                    mapOf("suspended" to suspended)
                ) {
                    filter { eq("id", userId) }
                }
                loadSupabaseProfiles()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not update status: ${e.message}"
            }
        }
    }

    fun resetPassword(email: String): Boolean {
        return email.isNotBlank() && email.contains("@")
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = "login"
    }

    // --- Bookings & Quotation Request Management ---
    fun submitQuotationRequest(booking: Booking, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertBooking(booking)
            onComplete()
        }
    }

    // Admin pricing and quotation generation
    fun adminGenerateQuotation(
        bookingId: Long,
        quotedPrice: Double,
        logisticsCost: Double,
        vatAmount: Double,
        adminNotes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val booking = database.sashioDao().getBookingById(bookingId)
            if (booking != null) {
                val updatedBooking = booking.copy(
                    quotedPrice = quotedPrice,
                    logisticsCost = logisticsCost,
                    vatAmount = vatAmount,
                    adminNotes = adminNotes,
                    quoteIssuedDate = System.currentTimeMillis(),
                    status = "Quotation Sent"
                )
                repository.updateBooking(updatedBooking)
                onComplete()
            }
        }
    }

    // Admin rejects request
    fun adminRejectQuotationRequest(bookingId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "CANCELLED")
            onComplete()
        }
    }

    // Admin catalog management
    fun adminAddCatalogItem(
        title: String,
        description: String,
        modelNumber: String,
        variantName: String,
        basePrice: Double,
        imageUrl: String,
        videoUrl: String = "",
        features: String = "",
        specifications: String = "",
        benefits: String = "",
        useCases: String = "",
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.insertCatalogItem(
                CatalogItem(
                    title = title,
                    description = description,
                    modelNumber = modelNumber,
                    variantName = variantName,
                    basePrice = basePrice,
                    imageUrl = imageUrl,
                    videoUrl = videoUrl,
                    features = features,
                    specifications = specifications,
                    benefits = benefits,
                    useCases = useCases
                )
            )
            onComplete()
        }
    }

    // Client accepts generated quotation
    fun clientAcceptQuotation(bookingId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "Client Accepted")
            onComplete()
        }
    }

    // Client converts quotation to booking, confirming service details and updating dates/dates/location/instructions
    fun clientConvertCopyToBooking(
        bookingId: Long,
        serviceDate: String,
        duration: String,
        deliveryAddress: String,
        specialInstructions: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val booking = database.sashioDao().getBookingById(bookingId)
            if (booking != null) {
                val updatedBooking = booking.copy(
                    eventDate = serviceDate,
                    duration = duration,
                    address = deliveryAddress,
                    specialInstructions = specialInstructions,
                    status = "Client Accepted"
                )
                repository.updateBooking(updatedBooking)
                onComplete()
            }
        }
    }

    // Client rejects generated quotation
    fun clientRejectQuotation(bookingId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "Cancelled")
            onComplete()
        }
    }

    // Client requests revision for generated quotation
    fun clientRequestRevision(bookingId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "Under Review")
            onComplete()
        }
    }

    // Assigning crew (admin)
    fun adminAssignStaffToBooking(bookingId: Long, staffId: Long) {
        viewModelScope.launch {
            repository.assignBooking(bookingId, staffId)
        }
    }

    // Updating status by Crew (staff)
    fun staffUpdateBookingStatus(bookingId: Long, status: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, status)
        }
    }

    // --- Consultation Actions ---
    fun bookConsultation(type: String, preferredDate: String, notes: String, onComplete: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertConsultation(
                Consultation(
                    clientId = user.id,
                    clientName = user.name,
                    clientPhone = user.phoneNumber,
                    clientEmail = user.email,
                    type = type,
                    preferredDate = preferredDate,
                    notes = notes,
                    status = "Pending"
                )
            )
            onComplete()
        }
    }

    // --- Reviews Actions ---
    fun submitReview(rating: Int, comment: String, category: String, onComplete: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertReview(
                Review(
                    userName = user.name,
                    rating = rating,
                    comment = comment,
                    category = category,
                    date = "Today"
                )
            )
            onComplete()
        }
    }

    // --- Paystack Secured Checkout Simulator ---
    private val _activePaystackTransaction = MutableStateFlow<CreatePaymentResponse?>(null)
    val activePaystackTransaction: StateFlow<CreatePaymentResponse?> = _activePaystackTransaction.asStateFlow()

    fun triggerPaystackPayment(bookingId: Long, totalAmount: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val response = repository.createPayment(bookingId, user.email, totalAmount)
            _activePaystackTransaction.value = response
        }
    }

    fun clearActiveTransaction() {
        _activePaystackTransaction.value = null
    }

    // Renamed in spirit from "simWebpayComplete": this no longer marks a
    // payment successful on tap. It closes the checkout sheet and asks
    // Supabase for the real status, which only becomes "success" once
    // Paystack's own webhook has actually confirmed the charge.
    private val _lastPaymentStatus = MutableStateFlow<String?>(null)
    val lastPaymentStatus: StateFlow<String?> = _lastPaymentStatus.asStateFlow()

    fun simWebpayComplete(ref: String) {
        _activePaystackTransaction.value = null
        viewModelScope.launch {
            _lastPaymentStatus.value = repository.checkPaymentStatus(ref)
        }
    }

    // --- Places Autocomplete Simulator ---
    fun updateAddressSearch(query: String) {
        _addressSearchQuery.value = query
        if (query.length < 3) {
            _addressSuggestions.value = emptyList()
            return
        }

        val lagosBase = "Lagos, Nigeria"
        val places = listOf(
            SimulatedPlace("Allen Avenue, Ikeja, $lagosBase", 6.5982, 3.3541),
            SimulatedPlace("Broad Street, Marina, $lagosBase", 6.4531, 3.3958),
            SimulatedPlace("Eko Atlantic Promenade, Victoria Island, $lagosBase", 6.4253, 3.4243),
            SimulatedPlace("Lekki Phase 1 Gatehouse, Lekki, $lagosBase", 6.4381, 3.4823),
            SimulatedPlace("Adetokunbo Ademola Crescent, Wuse II, Abuja, Nigeria", 9.0778, 7.4812),
            SimulatedPlace("Garki Area 11 Shopping Complex, Abuja, Nigeria", 9.0321, 7.4891),
            SimulatedPlace("Trans Amadi Industrial Estate, Port Harcourt, Nigeria", 4.8152, 7.0392),
            SimulatedPlace("GRA Phase 2 Recreation Hub, Port Harcourt, Nigeria", 4.8211, 7.0084),
            SimulatedPlace("Bompai Industrial Area, Kano, Nigeria", 12.0123, 8.5432),
            SimulatedPlace("Murtala Muhammed Way, Benin City, Edo, Nigeria", 6.3312, 5.6261)
        )

        _addressSuggestions.value = places.filter {
            it.address.contains(query, ignoreCase = true)
        }
    }

    // --- WhatsApp Prepopulate Generator ---
    fun generateWhatsAppText(booking: Booking): String {
        val inclusions = mutableListOf<String>()
        if (booking.cleaningSupport) inclusions.add("Cleaning Support")
        if (booking.onSiteAttendant) inclusions.add("On-Site Attendant")
        if (booking.wasteEvacuation) inclusions.add("Waste Evacuation")
        if (booking.maintenanceSupport) inclusions.add("Maintenance Support")

        val boldDivider = "============================"
        val message = """
            *SACHIO MOBILE TOILETS LTD*
            *QUOTATION REQUEST DETAILS*
            $boldDivider
            *Client:* ${booking.clientName}
            *Company:* ${booking.companyName.ifBlank() { "N/A" }}
            *Service Mode:* ${booking.serviceMode}
            *Category:* ${booking.serviceType}
            *Product:* ${booking.variantName}
            *Model:* ${booking.modelNumber}
            *Quantity:* ${booking.quantity}
            *Duration:* ${booking.duration}
            
            *EVENT INFO:*
            *Type:* ${booking.eventType}
            *Guests:* ${booking.guestCount}
            *Date:* ${booking.eventDate}
            
            *LOCATION DETAILS:*
            *Address:* ${booking.address}
            *GPS Pins:* ${booking.latitude}, ${booking.longitude}
            
            *ADDITIONAL INCLUSIONS:*
            ${if (inclusions.isNotEmpty()) inclusions.joinToString(", ") else "None selected"}
            
            *SPECIAL REQUESTS:*
            ${booking.specialInstructions.ifBlank() { "None" }}
            $boldDivider
            _Comfort. Hygiene. Dignity Anywhere._
        """.trimIndent()
        
        return try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            ""
        }
    }

    // --- FAVORITE LOCATIONS MANAGEMENT ---
    fun saveFavoriteLocation(name: String, address: String, lat: Double, lng: Double, type: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertFavoriteLocation(
                FavoriteLocation(
                    clientId = user.id,
                    name = name,
                    address = address,
                    latitude = lat,
                    longitude = lng,
                    locationType = type
                )
            )
        }
    }

    fun updateFavoriteLocation(id: Long, name: String, address: String, lat: Double, lng: Double, type: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateFavoriteLocation(
                FavoriteLocation(
                    id = id,
                    clientId = user.id,
                    name = name,
                    address = address,
                    latitude = lat,
                    longitude = lng,
                    locationType = type
                )
            )
        }
    }

    fun deleteFavoriteLocation(id: Long) {
        viewModelScope.launch {
            repository.deleteFavoriteLocation(id)
        }
    }

    // --- REAL-TIME LIVE TRACKING & DISPATCH SIMULATION ---
    fun addActivityLog(bookingId: Long, title: String, description: String) {
        val currentTracker = _activeLiveTrackers.value[bookingId] ?: return
        val updatedLogs = currentTracker.logs + ActivityLog(title, description)
        val updatedMap = _activeLiveTrackers.value.toMutableMap()
        updatedMap[bookingId] = currentTracker.copy(logs = updatedLogs, lastUpdated = System.currentTimeMillis())
        _activeLiveTrackers.value = updatedMap
    }

    fun addTrackingNotification(bookingId: Long, title: String, message: String, tag: String) {
        val currentTracker = _activeLiveTrackers.value[bookingId] ?: return
        val updatedNotifs = currentTracker.notifications + TrackingNotification(
            id = "notif_${System.currentTimeMillis()}",
            title = title,
            message = message,
            tag = tag
        )
        val updatedMap = _activeLiveTrackers.value.toMutableMap()
        updatedMap[bookingId] = currentTracker.copy(notifications = updatedNotifs, lastUpdated = System.currentTimeMillis())
        _activeLiveTrackers.value = updatedMap
    }

    fun sendClientChatMessage(bookingId: Long, text: String, imageUri: String? = null, locationShare: Pair<Double, Double>? = null) {
        val currentTracker = _activeLiveTrackers.value[bookingId] ?: return
        val updatedMessages = currentTracker.messages + ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = "Client",
            text = text,
            imageUri = imageUri,
            locationShare = locationShare
        )
        val updatedMap = _activeLiveTrackers.value.toMutableMap()
        updatedMap[bookingId] = currentTracker.copy(messages = updatedMessages, lastUpdated = System.currentTimeMillis())
        _activeLiveTrackers.value = updatedMap

        // Trigger real-time, context-aware automated reply from field staff or support
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            val replyText = when {
                text.lowercase().contains("where") || text.lowercase().contains("far") || text.lowercase().contains("eta") -> {
                    "Hi! We are currently en route on the bypass. Current ETA is about ${currentTracker.etaMinutes} minutes. Hold on for us!"
                }
                text.lowercase().contains("gate") || text.lowercase().contains("parking") || text.lowercase().contains("landmark") -> {
                    "Copy that, gate intelligence received! Adjusting maneuvers accordingly."
                }
                text.lowercase().contains("issue") || text.lowercase().contains("broken") || text.lowercase().contains("help") -> {
                    "This is SACHIO Support Cockpit. We have logged your request. Dispatch engineer is on rapid alert."
                }
                imageUri != null -> {
                    "Image attachment received successfully. Reviewing space clearance with dispatch team."
                }
                locationShare != null -> {
                    "Location pin received: (%.4f, %.4f). Tuning routing vectors.".format(locationShare.first, locationShare.second)
                }
                else -> {
                    "Prestige service acknowledged! We are strictly dedicated to providing 'Comfort. Hygiene. Dignity' at your venue."
                }
            }
            val trackerNow = _activeLiveTrackers.value[bookingId] ?: return@launch
            val finalizedMessages = trackerNow.messages + ChatMessage(
                id = "msg_reply_${System.currentTimeMillis()}",
                sender = if (text.lowercase().contains("help") || text.lowercase().contains("issue")) "Support Office" else "Obinna (Driver)",
                text = replyText
            )
            val finalMap = _activeLiveTrackers.value.toMutableMap()
            finalMap[bookingId] = trackerNow.copy(messages = finalizedMessages)
            _activeLiveTrackers.value = finalMap
        }
    }

    fun staffAcceptJob(bookingId: Long) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "Staff Assigned")
            val booking = database.sashioDao().getBookingById(bookingId) ?: return@launch
            
            val initialTracker = LiveTrackingState(
                bookingId = bookingId,
                staffLat = booking.latitude - 0.05,
                staffLng = booking.longitude - 0.04,
                vehicleLat = booking.latitude - 0.048,
                vehicleLng = booking.longitude - 0.038,
                distanceRemainingKm = 15.0,
                etaMinutes = 25,
                isTrackingActive = true,
                journeyStatus = "Staff Assigned",
                staffName = "Obinna Okafor",
                staffState = "Standby"
            )

            val currentMap = _activeLiveTrackers.value.toMutableMap()
            currentMap[bookingId] = initialTracker
            _activeLiveTrackers.value = currentMap

            addActivityLog(bookingId, "Task Confirmed", "Staff Obinna Okafor accepted service task #QR-$bookingId.")
            addActivityLog(bookingId, "Crew Assigned", "Attendant Obinna Okafor allocated and reviewing venue directions.")
            addTrackingNotification(
                bookingId = bookingId,
                title = "Chief Attendant Assigned",
                message = "Obinna Okafor (8 Years Exp) has been assigned to supervise your Event Sanitation.",
                tag = "STAFF_ASSIGNED"
            )
        }
    }

    fun startStaffJourney(bookingId: Long) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "IN TRANSIT")
            
            val booking = database.sashioDao().getBookingById(bookingId) ?: return@launch
            
            val startLat = booking.latitude - 0.05
            val startLng = booking.longitude - 0.04
            val vehicleStartLat = booking.latitude - 0.048
            val vehicleStartLng = booking.longitude - 0.038
            
            // Check if there is an existing tracker state, if so copy details, else initialize
            val existingTracker = _activeLiveTrackers.value[bookingId]
            val trackerState = (existingTracker ?: LiveTrackingState(bookingId = bookingId)).copy(
                staffLat = startLat,
                staffLng = startLng,
                vehicleLat = vehicleStartLat,
                vehicleLng = vehicleStartLng,
                distanceRemainingKm = 12.5,
                etaMinutes = 20,
                isTrackingActive = true,
                journeyStatus = "In Transit",
                staffState = "Driving"
            )
            
            val currentMap = _activeLiveTrackers.value.toMutableMap()
            currentMap[bookingId] = trackerState
            _activeLiveTrackers.value = currentMap
            
            addActivityLog(bookingId, "Vehicle Dispatched", "SACHIO Restroom Hauler departed VI Operations Center.")
            addActivityLog(bookingId, "En Route", "Dispatch team is cruising along Third Mainland bridge.")
            addTrackingNotification(
                bookingId = bookingId,
                title = "Vehicle Dispatched",
                message = "Your luxury cabins are en route! Super-DeLuxe Trailer has departed depot.",
                tag = "VEHICLE_DISPATCHED"
            )
            addTrackingNotification(
                bookingId = bookingId,
                title = "Booking En Route",
                message = "Driver Obinna is navigating optimally. Current traffic is moderate.",
                tag = "VEHICLE_EN_ROUTE"
            )
            
            // Start updating coordinates in real time
            launch {
                val steps = 10
                for (i in 1..steps) {
                    kotlinx.coroutines.delay(3000) // Ticks every 3 seconds for smooth visual representation
                    val activeMap = _activeLiveTrackers.value
                    val currentTracker = activeMap[bookingId] ?: break
                    if (currentTracker.journeyStatus != "In Transit") break
                    
                    val pct = i.toDouble() / steps.toDouble()
                    val nextStaffLat = startLat + pct * (booking.latitude - startLat)
                    val nextStaffLng = startLng + pct * (booking.longitude - startLng)
                    val nextVehLat = vehicleStartLat + pct * (booking.latitude - vehicleStartLat)
                    val nextVehLng = vehicleStartLng + pct * (booking.longitude - vehicleStartLng)
                    
                    val nextDist = (12.5 * (1.0 - pct)).coerceAtLeast(0.0)
                    val nextEta = (20 * (1.0 - pct)).toInt().coerceAtLeast(1)
                    
                    val updatedMap = _activeLiveTrackers.value.toMutableMap()
                    
                    // Add periodic activity updates
                    val nextLogs = currentTracker.logs.toMutableList()
                    if (i == 3) {
                        nextLogs.add(ActivityLog("Toll Gate Bypass", "Cruising past Lekki Toll area. Optimizing alternate speed bypass."))
                    } else if (i == 7) {
                        nextLogs.add(ActivityLog("Vicinity Approaching", "Nearing venue gates. Attendant setting up dynamic toolkits."))
                    }
                    
                    updatedMap[bookingId] = currentTracker.copy(
                        staffLat = nextStaffLat,
                        staffLng = nextStaffLng,
                        vehicleLat = nextVehLat,
                        vehicleLng = nextVehLng,
                        distanceRemainingKm = nextDist,
                        etaMinutes = if (i == steps) 0 else nextEta,
                        logs = nextLogs,
                        lastUpdated = System.currentTimeMillis()
                    )
                    _activeLiveTrackers.value = updatedMap
                }
                
                // Once steps complete, auto-transition to ARRIVED
                val finalMap = _activeLiveTrackers.value.toMutableMap()
                val finalTracker = finalMap[bookingId]
                if (finalTracker != null && finalTracker.journeyStatus == "In Transit") {
                    finalMap[bookingId] = finalTracker.copy(
                        staffLat = booking.latitude,
                        staffLng = booking.longitude,
                        vehicleLat = booking.latitude,
                        vehicleLng = booking.longitude,
                        distanceRemainingKm = 0.0,
                        etaMinutes = 0,
                        journeyStatus = "Arrived",
                        staffState = "Standby"
                    )
                    _activeLiveTrackers.value = finalMap
                    repository.updateBookingStatus(bookingId, "ARRIVED")
                    
                    addActivityLog(bookingId, "Arrived at Venue", "SACHIO Restroom Hauler has successfully parked at event location.")
                    addTrackingNotification(
                        bookingId = bookingId,
                        title = "Arrived at Venue",
                        message = "Your assigned sanitation team has arrived at the venue! Preparing cabin deploy.",
                        tag = "VEHICLE_ARRIVED"
                    )
                }
            }
        }
    }

    fun staffMarkArrived(bookingId: Long) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "ARRIVED")
            val currentMap = _activeLiveTrackers.value.toMutableMap()
            val trackerState = currentMap[bookingId]
            if (trackerState != null) {
                val booking = database.sashioDao().getBookingById(bookingId)
                val targetLat = booking?.latitude ?: trackerState.staffLat
                val targetLng = booking?.longitude ?: trackerState.staffLng
                currentMap[bookingId] = trackerState.copy(
                    staffLat = targetLat,
                    staffLng = targetLng,
                    vehicleLat = targetLat,
                    vehicleLng = targetLng,
                    distanceRemainingKm = 0.0,
                    etaMinutes = 0,
                    journeyStatus = "Arrived",
                    staffState = "Standby"
                )
                _activeLiveTrackers.value = currentMap
                
                addActivityLog(bookingId, "Arrived at Venue", "Staff marked arrival manually. Fleet parked at designated slot.")
                addTrackingNotification(
                    bookingId = bookingId,
                    title = "Arrived at Venue",
                    message = "assigned team has arrived at the venue safely.",
                    tag = "VEHICLE_ARRIVED"
                )
            }
        }
    }

    fun staffStartSetup(bookingId: Long) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "SETUP IN PROGRESS")
            val currentMap = _activeLiveTrackers.value.toMutableMap()
            val trackerState = currentMap[bookingId]
            if (trackerState != null) {
                currentMap[bookingId] = trackerState.copy(
                    journeyStatus = "Setup",
                    staffState = "Setting Up",
                    lastUpdated = System.currentTimeMillis()
                )
                _activeLiveTrackers.value = currentMap
                
                addActivityLog(bookingId, "Setup Started", "Chief Attendant is deploying high-pressure waste connectors and stabilizers.")
                addTrackingNotification(
                    bookingId = bookingId,
                    title = "Setup in Progress",
                    message = "Crew has started unpacking and placing the premium mobile cabins.",
                    tag = "SETUP_STARTED"
                )
            }
        }
    }

    fun staffMarkActive(bookingId: Long) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "SERVICE ACTIVE")
            val currentMap = _activeLiveTrackers.value.toMutableMap()
            val trackerState = currentMap[bookingId]
            if (trackerState != null) {
                currentMap[bookingId] = trackerState.copy(
                    journeyStatus = "Active",
                    staffState = "Active Monitoring",
                    lastUpdated = System.currentTimeMillis()
                )
                _activeLiveTrackers.value = currentMap
                
                addActivityLog(bookingId, "Service Active", "Sanitary cabins fully operational. Air fresheners, power generators, and premium soaps active.")
                addTrackingNotification(
                    bookingId = bookingId,
                    title = "Service Started",
                    message = "Your SACHIO prestige rest cabins are now LIVE and open for guests.",
                    tag = "SERVICE_STARTED"
                )
            }
        }
    }

    fun staffPerformMaintenance(bookingId: Long) {
        viewModelScope.launch {
            val currentMap = _activeLiveTrackers.value.toMutableMap()
            val trackerState = currentMap[bookingId]
            if (trackerState != null) {
                currentMap[bookingId] = trackerState.copy(
                    journeyStatus = "Maintenance",
                    staffState = "Cleaning & Upkeep",
                    lastUpdated = System.currentTimeMillis()
                )
                _activeLiveTrackers.value = currentMap
                
                addActivityLog(bookingId, "Maintenance Performed", "Attendant performed scheduled pressure wash and diesel generator level top-up.")
            }
        }
    }

    fun staffMarkCompleted(bookingId: Long) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "COMPLETED")
            val currentMap = _activeLiveTrackers.value.toMutableMap()
            val trackerState = currentMap[bookingId]
            if (trackerState != null) {
                currentMap[bookingId] = trackerState.copy(
                    journeyStatus = "Completed",
                    staffState = "Standby",
                    isTrackingActive = false,
                    lastUpdated = System.currentTimeMillis()
                )
                _activeLiveTrackers.value = currentMap
                
                addActivityLog(bookingId, "Service Completed", "Sanitary units cleared, disinfected, packed and secured. Mission accomplished.")
                addTrackingNotification(
                    bookingId = bookingId,
                    title = "Service Completed",
                    message = "Thank you for choosing SACHIO. Your service is completed successfully.",
                    tag = "SERVICE_COMPLETED"
                )
            }
        }
    }

    // --- LOGISTICS & LOCATION MATH MATRIX ---
    fun calculateLogisticsMetrics(lat: Double, lng: Double): LogisticsMetrics {
        // Central SACHIO Operations Depot Location (Victoria Island, Lagos)
        val depotLat = 6.4282
        val depotLng = 3.4211
        
        // Euclidean-to-KM approximation for West African region
        val dLat = lat - depotLat
        val dLng = lng - depotLng
        val distanceKm = Math.sqrt(dLat * dLat + dLng * dLng) * 111.0 // 1 degree ~ 111km
        
        val travelTimeMinutes = (distanceKm * 2.5).toInt().coerceAtLeast(15) // Traffic weighted
        val fuelUsageLitres = (distanceKm * 0.15).coerceAtLeast(5.0) // Average diesel burn per KM
        
        val zone: String
        val recommendedCost: Double
        
        when {
            distanceKm <= 5.0 -> {
                zone = "Zone 1 (Core Victoria Island / Ikoyi)"
                recommendedCost = 50000.0
            }
            distanceKm <= 15.0 -> {
                zone = "Zone 2 (Mid-Island & Ikeja Mainland)"
                recommendedCost = 85000.0
            }
            distanceKm <= 40.0 -> {
                zone = "Zone 3 (Outer Lagos Environs)"
                recommendedCost = 150000.0
            }
            else -> {
                zone = "Zone 4 (Inter-state / Long-haul Logistics)"
                recommendedCost = 350000.0
            }
        }
        
        return LogisticsMetrics(
            distanceKm = distanceKm,
            travelTimeMinutes = travelTimeMinutes,
            estimatedFuelLitres = fuelUsageLitres,
            logisticsZone = zone,
            recommendedLogisticsCost = recommendedCost
        )
    }

    // ==========================================
    // STAFF PORTAL WORKFORCE STATES & SERVICES
    // ==========================================

    // Expense Management Models & Flows
    private val _staffExpenses = MutableStateFlow<List<StaffExpense>>(listOf(
        StaffExpense("EXP-9821", 15000.0, "2026-06-12", "Fuel", "Refuelled restroom utility hauler truck at TotalEnergies VI", "https://images.unsplash.com/photo-1554415707-6e8cfc93fe23?w=500&auto=format&fit=crop", "Approved"),
        StaffExpense("EXP-9833", 4500.0, "2026-06-12", "Toll", "Lekki Link Bridge toll & emergency bypass fee for the Sovereign Restroom Trailer", "https://images.unsplash.com/photo-1554415707-6e8cfc93fe23?w=500&auto=format&fit=crop", "Pending Approval")
    ))
    val staffExpenses: StateFlow<List<StaffExpense>> = _staffExpenses.asStateFlow()

    fun submitStaffExpense(amount: Double, category: String, notes: String, date: String) {
        val newExp = StaffExpense(
            id = "EXP-${java.util.Random().nextInt(9000) + 1000}",
            amount = amount,
            date = date,
            category = category,
            notes = notes,
            imageUri = "https://images.unsplash.com/photo-1554415707-6e8cfc93fe23?w=500&auto=format&fit=crop",
            status = "Pending Approval"
        )
        _staffExpenses.value = listOf(newExp) + _staffExpenses.value
    }

    fun approveStaffExpense(id: String) {
        _staffExpenses.value = _staffExpenses.value.map {
            if (it.id == id) it.copy(status = "Approved") else it
        }
    }

    // Attendance System Logs & Tracking Flow
    private val _isClockedIn = MutableStateFlow(false)
    val isClockedIn: StateFlow<Boolean> = _isClockedIn.asStateFlow()

    private val _attendanceHistory = MutableStateFlow<List<AttendanceLog>>(listOf(
        AttendanceLog("ATT-087", "08:15 AM", "05:30 PM", "2026-06-11", "SACHIO VI Depot", "Completed"),
        AttendanceLog("ATT-068", "07:55 AM", "06:05 PM", "2026-06-10", "SACHIO VI Depot", "Completed")
    ))
    val attendanceHistory: StateFlow<List<AttendanceLog>> = _attendanceHistory.asStateFlow()

    fun clockInStaff(location: String) {
        _isClockedIn.value = true
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val dateSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val nowStr = sdf.format(java.util.Date())
        val dateStr = dateSdf.format(java.util.Date())
        val newLog = AttendanceLog(
            id = "ATT-${java.util.Random().nextInt(900) + 100}",
            clockInTime = nowStr,
            clockOutTime = null,
            date = dateStr,
            location = location,
            status = "Checked In"
        )
        _attendanceHistory.value = listOf(newLog) + _attendanceHistory.value
    }

    fun clockOutStaff() {
        _isClockedIn.value = false
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val nowStr = sdf.format(java.util.Date())
        val updated = _attendanceHistory.value.mapIndexed { idx, log ->
            if (idx == 0 && log.clockOutTime == null) {
                log.copy(clockOutTime = nowStr, status = "Completed")
            } else {
                log
            }
        }
        _attendanceHistory.value = updated
    }

    // Digital Inspection Tooling & Reporting System
    private val _inspections = MutableStateFlow<List<DigitalInspection>>(listOf(
        DigitalInspection("INSP-8110", 6L, "Pristine", "100%", 5, "Good", "Excelsior", "Pre-event commissioning checklist successfully validated.", listOf("https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=500&auto=format&fit=crop"), listOf("https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=500&auto=format&fit=crop"), System.currentTimeMillis() - 3600_000)
    ))
    val inspections: StateFlow<List<DigitalInspection>> = _inspections.asStateFlow()

    fun submitInspectionReport(
        bookingId: Long,
        toiletCondition: String,
        waterLevel: String,
        cleanlinessScore: Int,
        equipmentCondition: String,
        damageAssessment: String,
        comments: String,
        beforePhotos: List<String>,
        afterPhotos: List<String>
    ) {
        val newInsp = DigitalInspection(
            id = "INSP-${java.util.Random().nextInt(9000) + 1000}",
            bookingId = bookingId,
            toiletCondition = toiletCondition,
            waterLevel = waterLevel,
            cleanlinessScore = cleanlinessScore,
            equipmentCondition = equipmentCondition,
            damageAssessment = damageAssessment,
            comments = comments,
            beforePhotos = beforePhotos,
            afterPhotos = afterPhotos,
            timestamp = System.currentTimeMillis()
        )
        _inspections.value = listOf(newInsp) + _inspections.value
        
        // Push activity log to mapping center
        addActivityLog(bookingId, "Digital Quality Check", "Field technician successfully transmitted digital parameters: Cleanliness ($cleanlinessScore/5), Condition ($toiletCondition).")
    }

    // General Chat / Multi-channel Message Board Center
    private val _generalChats = MutableStateFlow<List<GeneralMessage>>(listOf(
        GeneralMessage("GM1", "Supervisor", "David, please ensure safety locks are tightly fitted around the Sovereign haul truck hitches.", "09:30 AM"),
        GeneralMessage("GM2", "Staff", "Understood. Re-checked. Torsion bar and safety chains secured.", "09:35 AM"),
        GeneralMessage("GM3", "Admin", "All field teams please note: Heavy rain forecasted for Victoria Island area around 15:00. Ensure non-slip mats are fully deployed on steps.", "11:10 AM")
    ))
    val generalChats: StateFlow<List<GeneralMessage>> = _generalChats.asStateFlow()

    fun sendGeneralChatMessage(sender: String, text: String, channel: String = "Admin") {
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val newMsg = GeneralMessage(
            id = "GM-${java.util.Random().nextInt(9000) + 1000}",
            sender = sender,
            text = text,
            timestamp = sdf.format(java.util.Date())
        )
        _generalChats.value = _generalChats.value + newMsg

        // Trigger context automatic response
        viewModelScope.launch {
            kotlinx.coroutines.delay(1800)
            val replyText = when {
                text.lowercase().contains("rain") || text.lowercase().contains("weather") -> {
                    "Understood. Non-slip vinyl mats are loaded in the trailer utility locker."
                }
                text.lowercase().contains("accident") || text.lowercase().contains("emergency") || text.lowercase().contains("support") -> {
                    "INCIDENT RESOLUTION ENGINE: Copy that. Supervisor notified. Please stay on site and wait for emergency response."
                }
                else -> {
                    "David, report noted. Keep up the prestige service standards. 'Comfort. Hygiene. Dignity Anywhere.'"
                }
            }
            val autoReply = GeneralMessage(
                id = "GM-${java.util.Random().nextInt(9000) + 1000}",
                sender = if (channel == "Supervisor") "Supervisor" else "Admin",
                text = replyText,
                timestamp = sdf.format(java.util.Date())
            )
            _generalChats.value = _generalChats.value + autoReply
        }
    }

    // Booking-Specific Staff Chat Router
    fun sendStaffChatMessage(bookingId: Long, text: String, imageUri: String? = null, locationShare: Pair<Double, Double>? = null) {
        val currentTracker = _activeLiveTrackers.value[bookingId] ?: return
        val updatedMessages = currentTracker.messages + ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = "Driver",
            text = text,
            imageUri = imageUri,
            locationShare = locationShare
        )
        val updatedMap = _activeLiveTrackers.value.toMutableMap()
        updatedMap[bookingId] = currentTracker.copy(messages = updatedMessages, lastUpdated = System.currentTimeMillis())
        _activeLiveTrackers.value = updatedMap

        // Trigger automated response from client or system admin
        viewModelScope.launch {
            kotlinx.coroutines.delay(1800)
            val replyText = when {
                text.lowercase().contains("arrive") || text.lowercase().contains("on site") || text.lowercase().contains("gate") -> {
                    "Excellent! Security on Broad Street has been informed. I will come out to meet you."
                }
                text.lowercase().contains("active") || text.lowercase().contains("setup") || text.lowercase().contains("ready") -> {
                    "Lovely! We appreciate the prestige setup and quick response. Tested water and vacuum flush, looks 5-star."
                }
                text.lowercase().contains("clog") || text.lowercase().contains("issue") || text.lowercase().contains("leak") -> {
                    "Thanks for reporting. Let us know if you need our on-site plumber or any additional water source hookups."
                }
                else -> {
                    "Acknowledge. Proceed with standard service activities according to SACHIO safety manuals."
                }
            }
            val trackerNow = _activeLiveTrackers.value[bookingId] ?: return@launch
            val finalizedMessages = trackerNow.messages + ChatMessage(
                id = "msg_reply_${System.currentTimeMillis()}",
                sender = "Client",
                text = replyText
            )
            val finalMap = _activeLiveTrackers.value.toMutableMap()
            finalMap[bookingId] = trackerNow.copy(messages = finalizedMessages)
            _activeLiveTrackers.value = finalMap
        }
    }
}

// ==========================================
// STAFF PORTAL STRUCTURED DATA DEFS
// ==========================================

data class StaffExpense(
    val id: String,
    val amount: Double,
    val date: String,
    val category: String, // Fuel, Parking, Toll, Emergency
    val notes: String,
    val imageUri: String,
    val status: String // Pending Approval, Approved
)

data class AttendanceLog(
    val id: String,
    val clockInTime: String,
    val clockOutTime: String?,
    val date: String,
    val location: String,
    val status: String // Checked In, Completed
)

data class DigitalInspection(
    val id: String,
    val bookingId: Long,
    val toiletCondition: String, // Pristine, Fair, Needs Repair
    val waterLevel: String, // 100%, 75%, 50%, 25%
    val cleanlinessScore: Int, // 1 to 5
    val equipmentCondition: String, // Good, Faulty
    val damageAssessment: String,
    val comments: String,
    val beforePhotos: List<String>,
    val afterPhotos: List<String>,
    val timestamp: Long
)

data class GeneralMessage(
    val id: String,
    val sender: String, // Admin, Supervisor, Support Team, Staff
    val text: String,
    val timestamp: String
)

data class SOPDocument(
    val id: String,
    val title: String,
    val category: String, // Safety, Plumbing, Chemical, Customer Service
    val overview: String,
    val checklist: List<String>
)


// --- DATA TRANSFER OBJECTS FOR MAP SYSTEM ---
data class ActivityLog(
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TrackingNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String
)

data class ChatMessage(
    val id: String,
    val sender: String, // "Client", "Driver", "Support"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val locationShare: Pair<Double, Double>? = null
)

data class LiveTrackingState(
    val bookingId: Long,
    val staffLat: Double = 0.0,
    val staffLng: Double = 0.0,
    val vehicleLat: Double = 0.0,
    val vehicleLng: Double = 0.0,
    val distanceRemainingKm: Double = 15.0,
    val etaMinutes: Int = 25,
    val isTrackingActive: Boolean = false,
    val journeyStatus: String = "Idle", // "Idle", "In Transit", "Arrived", "Completed", "Setup", "Active", "Maintenance"
    val lastUpdated: Long = System.currentTimeMillis(),
    val staffName: String = "Obinna Okafor",
    val staffPhone: String = "+234 812 345 6789",
    val staffExperience: Int = 8,
    val staffState: String = "Driving", // "Driving", "Setting Up", "Active Monitoring", "Standby"
    val staffPosition: String = "Chief Sanitary Attendant",
    val staffPhotoUrl: String = "",
    val vehicleType: String = "SACHIO VIP Restroom Hauler Wagon - Series V",
    val vehicleNumber: String = "LA-949-SACHIO",
    val trafficLevel: String = "Moderate Traffic",
    val roadClosureStatus: String = "No major road closures reported",
    val alternativeRouteName: String = "Depot Coastal Bypass (Optimal)",
    val logs: List<ActivityLog> = emptyList(),
    val notifications: List<TrackingNotification> = emptyList(),
    val messages: List<ChatMessage> = emptyList()
)

data class LogisticsMetrics(
    val distanceKm: Double,
    val travelTimeMinutes: Int,
    val estimatedFuelLitres: Double,
    val logisticsZone: String,
    val recommendedLogisticsCost: Double
)

data class SupportTicket(
    val id: String,
    val subject: String,
    val category: String,
    val description: String,
    val status: String,
    val date: String
)
