package com.sashio.ops.data

import android.app.Application
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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

    // --- Extra Profile Fields for Settings ---
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
    private val _themePreference = MutableStateFlow("dark")
    val themePreference = _themePreference.asStateFlow()

    private val _accentColor = MutableStateFlow("Gold")
    val accentColor = _accentColor.asStateFlow()

    private val _fontSizeScale = MutableStateFlow("Standard")
    val fontSizeScale = _fontSizeScale.asStateFlow()

    private val _cardDensity = MutableStateFlow("Comfortable")
    val cardDensity = _cardDensity.asStateFlow()

    private val _mapTheme = MutableStateFlow("Standard Map")
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

    // Notifications Toggles
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
    private val _privacyProfilePicVisible = MutableStateFlow("Public")
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

    private val _securityScore = MutableStateFlow("Excellent")
    val securityScore = _securityScore.asStateFlow()

    // Languages
    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    // Support Request lists
    private val _ticketHistory = MutableStateFlow<List<SupportTicket>>(emptyList())
    val ticketHistory = _ticketHistory.asStateFlow()

    // Error and Screen Flow
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _signUpError = MutableStateFlow<String?>(null)
    val signUpError: StateFlow<String?> = _signUpError.asStateFlow()

    private val _currentScreen = MutableStateFlow("splash")
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

    val allProfiles: StateFlow<List<UserProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _suspendedProfileIds = MutableStateFlow<Set<Long>>(emptySet())
    val suspendedProfileIds: StateFlow<Set<Long>> = _suspendedProfileIds.asStateFlow()

    private val _incidents = MutableStateFlow<List<AdminIncident>>(emptyList())
    val incidents: StateFlow<List<AdminIncident>> = _incidents.asStateFlow()

    private val _attendanceLogs = MutableStateFlow<List<AdminStaffAttendance>>(emptyList())
    val attendanceLogs: StateFlow<List<AdminStaffAttendance>> = _attendanceLogs.asStateFlow()

    private val _expenseLogs = MutableStateFlow<List<AdminExpenseClaim>>(emptyList())
    val expenseLogs: StateFlow<List<AdminExpenseClaim>> = _expenseLogs.asStateFlow()

    // Live Supabase Expenses Stream
    private val _supabaseExpenses = MutableStateFlow<List<SupabaseExpense>>(emptyList())
    val supabaseExpenses: StateFlow<List<SupabaseExpense>> = _supabaseExpenses.asStateFlow()

    // Live Supabase Attendance Stream
    private val _supabaseAttendance = MutableStateFlow<List<SupabaseAttendance>>(emptyList())
    val supabaseAttendance: StateFlow<List<SupabaseAttendance>> = _supabaseAttendance.asStateFlow()

    // Role-Filtered Bookings
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

    // Active Live Dispatch Trackers
    private val _activeLiveTrackers = MutableStateFlow<Map<Long, LiveTrackingState>>(emptyMap())
    val activeLiveTrackers: StateFlow<Map<Long, LiveTrackingState>> = _activeLiveTrackers.asStateFlow()

    val staffBookings: StateFlow<List<Booking>> = _currentUser
        .flatMapLatest { user ->
            if (user != null && user.role == "staff") {
                repository.getBookingsForStaff(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Places Autocomplete
    private val _addressSearchQuery = MutableStateFlow("")
    val addressSearchQuery: StateFlow<String> = _addressSearchQuery.asStateFlow()

    private val _addressSuggestions = MutableStateFlow<List<SimulatedPlace>>(emptyList())
    val addressSuggestions: StateFlow<List<SimulatedPlace>> = _addressSuggestions.asStateFlow()

    // Real Supabase User Management
    private val _supabaseProfiles = MutableStateFlow<List<AdminProfileRow>>(emptyList())
    val supabaseProfiles: StateFlow<List<AdminProfileRow>> = _supabaseProfiles.asStateFlow()

    private val _roleManagementError = MutableStateFlow<String?>(null)
    val roleManagementError: StateFlow<String?> = _roleManagementError.asStateFlow()

    val realStaffProfiles: StateFlow<List<AdminProfileRow>> = _supabaseProfiles
        .map { rows -> rows.filter { it.role == "staff" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Staff Live Assigned Bookings (Supabase)
    private val _myAssignedBookings = MutableStateFlow<List<SupabaseBooking>>(emptyList())
    val myAssignedBookings: StateFlow<List<SupabaseBooking>> = _myAssignedBookings.asStateFlow()

    // Admin Supabase Bookings
    private val _supabaseBookings = MutableStateFlow<List<SupabaseBooking>>(emptyList())
    val supabaseBookings: StateFlow<List<SupabaseBooking>> = _supabaseBookings.asStateFlow()

    // Catalog Image Upload
    private val _supabaseCatalogItems = MutableStateFlow<List<SupabaseCatalogItem>>(emptyList())
    val supabaseCatalogItems: StateFlow<List<SupabaseCatalogItem>> = _supabaseCatalogItems.asStateFlow()

    private val _isUploadingCatalogImage = MutableStateFlow(false)
    val isUploadingCatalogImage: StateFlow<Boolean> = _isUploadingCatalogImage.asStateFlow()

    private val storageHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val clientCatalogItems: StateFlow<List<CatalogItem>> = _supabaseCatalogItems
        .map { rows -> rows.filter { it.is_active }.map { it.toCatalogItem() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Password Reset
    private val _passwordResetError = MutableStateFlow<String?>(null)
    val passwordResetError: StateFlow<String?> = _passwordResetError.asStateFlow()

    private val _passwordResetSent = MutableStateFlow(false)
    val passwordResetSent: StateFlow<Boolean> = _passwordResetSent.asStateFlow()

    private val _newPasswordError = MutableStateFlow<String?>(null)
    val newPasswordError: StateFlow<String?> = _newPasswordError.asStateFlow()

    private val _newPasswordSetSuccessfully = MutableStateFlow(false)
    val newPasswordSetSuccessfully: StateFlow<Boolean> = _newPasswordSetSuccessfully.asStateFlow()

    private val _bookingSyncMessage = MutableStateFlow<String?>(null)
    val bookingSyncMessage: StateFlow<String?> = _bookingSyncMessage.asStateFlow()

    // Paystack Payments
    private val _activePaystackTransaction = MutableStateFlow<CreatePaymentResponse?>(null)
    val activePaystackTransaction: StateFlow<CreatePaymentResponse?> = _activePaystackTransaction.asStateFlow()

    private val _clientSupabaseBookings = MutableStateFlow<List<SupabaseBooking>>(emptyList())
    val clientSupabaseBookings: StateFlow<List<SupabaseBooking>> = _clientSupabaseBookings.asStateFlow()

    private val _lastPaymentStatus = MutableStateFlow<String?>(null)
    val lastPaymentStatus: StateFlow<String?> = _lastPaymentStatus.asStateFlow()

    // Staff Portal Specifics
    private val _staffExpenses = MutableStateFlow<List<StaffExpense>>(emptyList())
    val staffExpenses: StateFlow<List<StaffExpense>> = _staffExpenses.asStateFlow()

    private val _isClockedIn = MutableStateFlow(false)
    val isClockedIn: StateFlow<Boolean> = _isClockedIn.asStateFlow()

    private val _attendanceHistory = MutableStateFlow<List<AttendanceLog>>(emptyList())
    val attendanceHistory: StateFlow<List<AttendanceLog>> = _attendanceHistory.asStateFlow()

    private val _inspections = MutableStateFlow<List<DigitalInspection>>(emptyList())
    val inspections: StateFlow<List<DigitalInspection>> = _inspections.asStateFlow()

    private val _generalChats = MutableStateFlow<List<SupabaseMessage>>(emptyList())
    val generalChats: StateFlow<List<SupabaseMessage>> = _generalChats.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    fun getCurrentUserId(): String {
        return SupabaseProvider.client.auth.currentUserOrNull()?.id
            ?: _currentUser.value?.id?.toString()
            ?: "guest_user"
    }

    fun setScreen(screen: String) {
        _currentScreen.value = screen
        _loginError.value = null
        _signUpError.value = null
    }

    // --- Authentication ---
    fun loginWithEmail(email: String, requiredRole: String? = null, password: String = "") {
        viewModelScope.launch {
            _loginError.value = null

            if (SupabaseProvider.SUPABASE_URL == "YOUR_SUPABASE_URL") {
                val user = repository.getProfileByEmail(email.trim().lowercase())
                if (user == null) {
                    _loginError.value = "Account not found. Please check your email or register a new account."
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

            try {
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

                val dbProfile = SupabaseProvider.client.postgrest.from("profiles")
                    .select {
                        filter { eq("id", uid) }
                    }.decodeSingle<SupabaseProfile>()

                val mappedRole = dbProfile.role
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

                if (password.isNotBlank()) {
                    BiometricAuthHelper.saveCredentials(getApplication(), email.trim().lowercase(), password)
                }

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
        val safeRole = "client"
        viewModelScope.launch {
            val existing = repository.getProfileByEmail(email.trim().lowercase())
            if (existing != null) {
                _signUpError.value = "Email address already exists in the system"
                return@launch
            }

            try {
                SupabaseProvider.client.auth.signUpWith(Email) {
                    this.email = email.trim().lowercase()
                    this.password = password
                }
                val supaUser = SupabaseProvider.client.auth.currentUserOrNull()
                if (supaUser != null) {
                    SupabaseProvider.client.postgrest.from("profiles").update(
                        buildJsonObject {
                            put("full_name", name.trim())
                            put("phone_number", phone.trim())
                            put("company_name", company.trim())
                        }
                    ) {
                        filter { eq("id", supaUser.id) }
                    }
                }
            } catch (e: Exception) {
                _signUpError.value = "Could not create account: ${e.message}"
                return@launch
            }

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

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = "login"
    }

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
        _ticketHistory.update { list ->
            val ticket = SupportTicket(
                id = "SCH-${1000 + list.size}",
                subject = subject,
                category = category,
                description = description,
                status = "Open",
                date = SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.getDefault()).format(Date())
            )
            listOf(ticket) + list
        }
    }

    fun deactivateAccount() {
        _currentUser.value = null
        _currentScreen.value = "login"
    }

    fun deleteAccount() {
        _currentUser.value = null
        _currentScreen.value = "login"
    }

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

    fun adminSetUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("profiles").update(
                    buildJsonObject { put("role", newRole) }
                ) {
                    filter { eq("id", userId) }
                }
                loadSupabaseProfiles()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not update role: ${e.message}"
            }
        }
    }

    fun adminDeleteUserProfile(userId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("profiles").delete {
                    filter { eq("id", userId) }
                }
                loadSupabaseProfiles()
                onComplete()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not delete user: ${e.message}"
            }
        }
    }

    fun adminSetProfileSuspended(userId: String, suspended: Boolean) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("profiles").update(
                    buildJsonObject { put("suspended", suspended) }
                ) {
                    filter { eq("id", userId) }
                }
                loadSupabaseProfiles()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not update status: ${e.message}"
            }
        }
    }

    // --- Bookings & Quotation Workflows ---
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

    fun adminDeleteBooking(bookingId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("bookings").delete {
                    filter { eq("id", bookingId) }
                }
                loadSupabaseBookings()
                onComplete()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not delete quotation: ${e.message}"
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
                    buildJsonObject {
                        put("quoted_price", quotedPrice)
                        put("logistics_cost", logisticsCost)
                        put("vat_amount", vatAmount)
                        put("admin_notes", adminNotes)
                        put("status", "Quotation Generated")
                    }
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

    fun adminAssignStaffToSupabaseBooking(
        bookingId: String,
        staffId: String,
        staffName: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("bookings").update(
                    buildJsonObject {
                        put("assigned_staff_id", staffId)
                        put("assigned_staff_name", staffName)
                        put("status", "Staff Assigned")
                    }
                ) {
                    filter { eq("id", bookingId) }
                }
                loadSupabaseBookings()
                _roleManagementError.value = null
                onComplete(true)
            } catch (e: Exception) {
                // Fallback: assign staff without altering status if check constraint blocks "Staff Assigned"
                try {
                    SupabaseProvider.client.postgrest.from("bookings").update(
                        buildJsonObject {
                            put("assigned_staff_id", staffId)
                            put("assigned_staff_name", staffName)
                        }
                    ) {
                        filter { eq("id", bookingId) }
                    }
                    loadSupabaseBookings()
                    _roleManagementError.value = null
                    onComplete(true)
                } catch (fallbackError: Exception) {
                    _roleManagementError.value = "Could not assign staff: ${fallbackError.message}"
                    onComplete(false)
                }
            }
        }
    }

    fun loadMyAssignedBookings() {
        viewModelScope.launch {
            try {
                val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (uid == null) {
                    val localEmail = _currentUser.value?.email
                    if (localEmail != null) {
                        val rows = SupabaseProvider.client.postgrest.from("bookings")
                            .select {
                                filter { eq("assigned_staff_name", _currentUser.value?.name ?: "") }
                            }
                            .decodeList<SupabaseBooking>()
                        _myAssignedBookings.value = rows
                    }
                    return@launch
                }

                val rows = SupabaseProvider.client.postgrest.from("bookings")
                    .select {
                        filter { eq("assigned_staff_id", uid) }
                    }
                    .decodeList<SupabaseBooking>()
                _myAssignedBookings.value = rows
                _roleManagementError.value = null
            } catch (e: Exception) {
                _roleManagementError.value = "Could not load assigned jobs: ${e.message}"
            }
        }
    }

    fun staffUpdateBookingStatus(bookingId: String, newStatus: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("bookings").update(
                    buildJsonObject { put("status", newStatus) }
                ) {
                    filter { eq("id", bookingId) }
                }
                loadMyAssignedBookings()
                loadSupabaseBookings()
                onComplete()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not update job status: ${e.message}"
            }
        }
    }

    fun staffUpdateLocalBookingStatus(bookingId: Long, status: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, status)
        }
    }

    fun submitQuotationRequest(booking: Booking, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertBooking(booking)

            try {
                val supaUser = SupabaseProvider.client.auth.currentUserOrNull()
                if (supaUser != null) {
                    SupabaseProvider.client.postgrest.from("bookings").insert(
                        NewSupabaseBooking(
                            client_id = supaUser.id,
                            client_name = booking.clientName,
                            client_phone = booking.clientPhone,
                            client_email = booking.clientEmail,
                            company_name = booking.companyName,
                            service_mode = booking.serviceMode,
                            service_type = booking.serviceType,
                            variant_name = booking.variantName,
                            model_number = booking.modelNumber,
                            quantity = booking.quantity,
                            duration = booking.duration,
                            address = booking.address,
                            latitude = booking.latitude,
                            longitude = booking.longitude,
                            venue_name = booking.venueName,
                            event_type = booking.eventType,
                            guest_count = booking.guestCount,
                            event_date = booking.eventDate,
                            cleaning_support = booking.cleaningSupport,
                            on_site_attendant = booking.onSiteAttendant,
                            waste_evacuation = booking.wasteEvacuation,
                            maintenance_support = booking.maintenanceSupport,
                            special_instructions = booking.specialInstructions
                        )
                    )
                    _bookingSyncMessage.value = "synced"
                } else {
                    _bookingSyncMessage.value = "no_supabase_session"
                }
            } catch (e: Exception) {
                _bookingSyncMessage.value = "sync_failed: ${e.message}"
            }

            onComplete()
        }
    }

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

    fun clientRejectQuotation(bookingId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "Cancelled")
            onComplete()
        }
    }

    fun clientRequestRevision(bookingId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "Under Review")
            onComplete()
        }
    }

    fun adminAssignStaffToBooking(bookingId: Long, staffId: Long) {
        viewModelScope.launch {
            repository.assignBooking(bookingId, staffId)
        }
    }

    // --- Supabase Catalog Items ---
    fun loadSupabaseCatalogItems() {
        viewModelScope.launch {
            try {
                val rows = SupabaseProvider.client.postgrest.from("catalog_items")
                    .select()
                    .decodeList<SupabaseCatalogItem>()
                _supabaseCatalogItems.value = rows
                _roleManagementError.value = null
            } catch (e: Exception) {
                _roleManagementError.value = "Could not load catalog: ${e.message}"
            }
        }
    }

    fun uploadCatalogImage(uri: Uri, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _isUploadingCatalogImage.value = true
            try {
                val context = getApplication<Application>().applicationContext
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    _roleManagementError.value = "Could not read the selected image."
                    onResult(null)
                    return@launch
                }
                val mimeType = context.contentResolver.getType(uri)?.takeIf { it.isNotBlank() } ?: "image/jpeg"
                val ext = mimeType.substringAfter("/").takeIf { it.isNotBlank() } ?: "jpg"
                val path = "products/${UUID.randomUUID()}.$ext"

                val accessToken = SupabaseProvider.client.auth.currentAccessTokenOrNull()
                    ?: SupabaseProvider.SUPABASE_ANON_KEY

                val uploadUrl = "${SupabaseProvider.SUPABASE_URL}/storage/v1/object/${SupabaseProvider.CATALOG_IMAGES_BUCKET}/$path"
                val request = Request.Builder()
                    .url(uploadUrl)
                    .addHeader("apikey", SupabaseProvider.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .post(bytes.toRequestBody(mimeType.toMediaTypeOrNull()))
                    .build()

                val success = withContext(Dispatchers.IO) {
                    storageHttpClient.newCall(request).execute().use { it.isSuccessful }
                }

                if (!success) {
                    _roleManagementError.value = "Image upload failed. Verify storage bucket and RLS policies."
                    onResult(null)
                    return@launch
                }

                val publicUrl = "${SupabaseProvider.SUPABASE_URL}/storage/v1/object/public/${SupabaseProvider.CATALOG_IMAGES_BUCKET}/$path"
                _roleManagementError.value = null
                onResult(publicUrl)
            } catch (e: Exception) {
                _roleManagementError.value = "Image upload failed: ${e.message}"
                onResult(null)
            } finally {
                _isUploadingCatalogImage.value = false
            }
        }
    }

    fun adminAddSupabaseCatalogItem(
        title: String,
        description: String,
        modelNumber: String,
        variantName: String,
        basePrice: Double,
        imageUrl: String,
        imageUrls: List<String>,
        videoUrl: String,
        features: String,
        specifications: String,
        benefits: String,
        useCases: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("catalog_items").insert(
                    NewSupabaseCatalogItem(
                        title = title,
                        description = description,
                        model_number = modelNumber,
                        variant_name = variantName,
                        base_price = basePrice,
                        image_url = imageUrl,
                        image_urls = imageUrls,
                        video_url = videoUrl,
                        features = features,
                        specifications = specifications,
                        benefits = benefits,
                        use_cases = useCases
                    )
                )
                loadSupabaseCatalogItems()
                onComplete()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not add catalog item: ${e.message}"
            }
        }
    }

    fun adminUpdateSupabaseCatalogItem(
        id: String,
        title: String,
        description: String,
        modelNumber: String,
        variantName: String,
        basePrice: Double,
        imageUrl: String,
        imageUrls: List<String>,
        videoUrl: String,
        features: String,
        specifications: String,
        benefits: String,
        useCases: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("catalog_items").update(
                    buildJsonObject {
                        put("title", title)
                        put("description", description)
                        put("model_number", modelNumber)
                        put("variant_name", variantName)
                        put("base_price", basePrice)
                        put("image_url", imageUrl)
                        putJsonArray("image_urls") { imageUrls.forEach { add(it) } }
                        put("video_url", videoUrl)
                        put("features", features)
                        put("specifications", specifications)
                        put("benefits", benefits)
                        put("use_cases", useCases)
                    }
                ) {
                    filter { eq("id", id) }
                }
                loadSupabaseCatalogItems()
                onComplete()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not update catalog item: ${e.message}"
            }
        }
    }

    fun adminDeleteSupabaseCatalogItem(id: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("catalog_items").delete {
                    filter { eq("id", id) }
                }
                loadSupabaseCatalogItems()
                onComplete()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not delete catalog item: ${e.message}"
            }
        }
    }

    // --- Password Reset & Recovery ---
    fun resetPassword(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _passwordResetError.value = "Enter a valid email address."
            return
        }
        _passwordResetError.value = null
        _passwordResetSent.value = false
        viewModelScope.launch {
            try {
                SupabaseProvider.client.auth.resetPasswordForEmail(
                    email = email.trim(),
                    redirectUrl = "sachioexpress://reset-password"
                )
                _passwordResetSent.value = true
            } catch (e: Exception) {
                _passwordResetError.value = "Could not send reset email: ${e.message}"
            }
        }
    }

    fun beginPasswordRecoverySession(accessToken: String, refreshToken: String) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.auth.importAuthToken(accessToken, refreshToken, retrieveUser = true)
                _currentScreen.value = "set_new_password"
            } catch (e: Exception) {
                _passwordResetError.value = "This reset link is invalid or has expired. Please request a new one."
            }
        }
    }

    fun updateUserPassword(newPassword: String) {
        if (newPassword.length < 6) {
            _newPasswordError.value = "Password must be at least 6 characters."
            return
        }
        _newPasswordError.value = null
        viewModelScope.launch {
            try {
                SupabaseProvider.client.auth.updateUser {
                    password = newPassword
                }
                _newPasswordSetSuccessfully.value = true
            } catch (e: Exception) {
                _newPasswordError.value = "Could not update password: ${e.message}"
            }
        }
    }

    // --- Consultations & Reviews ---
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

    fun submitReview(rating: Int, comment: String, category: String, onComplete: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertReview(
                Review(
                    userName = user.name,
                    rating = rating,
                    comment = comment,
                    category = category,
                    date = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
                )
            )
            onComplete()
        }
    }

    // --- Paystack Payments & Webhook Status ---
    fun triggerPaystackPayment(bookingId: Long, totalAmount: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val response = repository.createPayment(bookingId, user.email, totalAmount)
            _activePaystackTransaction.value = response
        }
    }

    fun loadClientSupabaseBookings() {
        viewModelScope.launch {
            try {
                val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: return@launch
                val rows = SupabaseProvider.client.postgrest.from("bookings")
                    .select {
                        filter { eq("client_id", uid) }
                    }
                    .decodeList<SupabaseBooking>()
                _clientSupabaseBookings.value = rows
            } catch (e: Exception) {
                _roleManagementError.value = "Could not load your bookings: ${e.message}"
            }
        }
    }

    fun triggerPaystackPaymentForRealBooking(bookingId: String, totalAmount: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val response = repository.createPaymentForRealBooking(bookingId, user.email, totalAmount)
                _activePaystackTransaction.value = response
                _roleManagementError.value = null
            } catch (e: Exception) {
                _roleManagementError.value = "Payment could not start: ${e.message}"
            }
        }
    }

    fun clearActiveTransaction() {
        _activePaystackTransaction.value = null
    }

    // Handles user cancellation on Paystack checkout
    fun handlePaymentCancelled() {
        _activePaystackTransaction.value = null
        _lastPaymentStatus.value = "cancelled"
        loadClientSupabaseBookings()
    }

    // Handles Paystack completion with smart polling for webhook landing
    fun simWebpayComplete(ref: String) {
        _activePaystackTransaction.value = null
        viewModelScope.launch {
            var status = repository.checkPaymentStatus(ref)
            
            if (status == "not_found" || status == "pending") {
                delay(1500)
                status = repository.checkPaymentStatus(ref)
            }
            
            if (status == "not_found" || status == "pending") {
                delay(2000)
                status = repository.checkPaymentStatus(ref)
            }

            _lastPaymentStatus.value = status
            loadClientSupabaseBookings()
        }
    }

    // --- Live Geocoding & Address Suggestions ---
    fun updateAddressSearch(query: String) {
        _addressSearchQuery.value = query
        if (query.trim().length < 3) {
            _addressSuggestions.value = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val geocoder = Geocoder(context, Locale.getDefault())

                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        var resultList: List<android.location.Address> = emptyList()
                        geocoder.getFromLocationName(query, 5) { addresses ->
                            resultList = addresses
                        }
                        resultList
                    } catch (e: Exception) {
                        geocoder.getFromLocationName(query, 5) ?: emptyList()
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(query, 5) ?: emptyList()
                }

                if (!addresses.isNullOrEmpty()) {
                    val places = addresses.map { addr ->
                        val formatted = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }.joinToString(", ")
                        SimulatedPlace(
                            address = formatted.ifBlank { "${addr.featureName ?: query}, ${addr.locality ?: "Nigeria"}" },
                            lat = addr.latitude,
                            lng = addr.longitude
                        )
                    }
                    _addressSuggestions.value = places
                    return@launch
                }
            } catch (e: Exception) {
                // Fall through to pre-indexed landmarks
            }

            val lagosBase = "Lagos, Nigeria"
            val defaultPlaces = listOf(
                SimulatedPlace("Admiralty Way, Lekki Phase 1, $lagosBase", 6.4381, 3.4823),
                SimulatedPlace("Eko Atlantic City, Victoria Island, $lagosBase", 6.4253, 3.4243),
                SimulatedPlace("Allen Avenue, Ikeja, $lagosBase", 6.5982, 3.3541),
                SimulatedPlace("Broad Street, Marina, Lagos Island, $lagosBase", 6.4531, 3.3958),
                SimulatedPlace("Ozumba Mbadiwe Avenue, Victoria Island, $lagosBase", 6.4342, 3.4285),
                SimulatedPlace("Bourdillon Road, Ikoyi, $lagosBase", 6.4510, 3.4412),
                SimulatedPlace("Isaac John Street, GRA Ikeja, $lagosBase", 6.5891, 3.3592),
                SimulatedPlace("Ajose Adeogun Street, Victoria Island, $lagosBase", 6.4298, 3.4265),
                SimulatedPlace("Adetokunbo Ademola Crescent, Wuse II, Abuja, Nigeria", 9.0778, 7.4812),
                SimulatedPlace("Garki Area 11 Shopping Complex, Abuja, Nigeria", 9.0321, 7.4891),
                SimulatedPlace("Trans Amadi Industrial Estate, Port Harcourt, Rivers, Nigeria", 4.8152, 7.0392),
                SimulatedPlace("GRA Phase 2 Recreation Hub, Port Harcourt, Rivers, Nigeria", 4.8211, 7.0084),
                SimulatedPlace("Murtala Muhammed Way, Benin City, Edo, Nigeria", 6.3312, 5.6261),
                SimulatedPlace("Bompai Industrial Area, Kano, Nigeria", 12.0123, 8.5432)
            )

            _addressSuggestions.value = defaultPlaces.filter {
                it.address.contains(query, ignoreCase = true)
            }
        }
    }

    fun reverseGeocodeLocation(lat: Double, lng: Double, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val geocoder = Geocoder(context, Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val formatted = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }.joinToString(", ")
                    val finalAddr = formatted.ifBlank { "${addr.featureName ?: "Pinned Venue"}, ${addr.locality ?: "Lagos"}" }
                    withContext(Dispatchers.Main) { onResult(finalAddr) }
                    return@launch
                }
            } catch (e: Exception) {
                // Fallback
            }
            withContext(Dispatchers.Main) {
                onResult("Pinned Venue (%.4f, %.4f)".format(lat, lng))
            }
        }
    }

    fun generateWhatsAppText(booking: Booking): String {
        val inclusions = mutableListOf<String>()
        if (booking.cleaningSupport) inclusions.add("Cleaning Support")
        if (booking.onSiteAttendant) inclusions.add("On-Site Attendant")
        if (booking.wasteEvacuation) inclusions.add("Waste Evacuation")
        if (booking.maintenanceSupport) inclusions.add("Maintenance Support")

        val message = """
            *SACHIO MOBILE TOILETS LTD*
            *QUOTATION REQUEST DETAILS*
            ============================
            *Client:* ${booking.clientName}
            *Company:* ${booking.companyName.ifBlank { "N/A" }}
            *Service Mode:* ${booking.serviceMode}
            *Category:* ${booking.serviceType}
            *Product:* ${booking.variantName} (${booking.modelNumber})
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
            ${booking.specialInstructions.ifBlank { "None" }}
            ============================
            _Comfort. Hygiene. Dignity Anywhere._
        """.trimIndent()

        return try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            ""
        }
    }

    // --- Favorite Locations ---
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

    // --- Live Dispatch Tracking Simulation & Chat ---
    fun addActivityLog(bookingId: Long, title: String, description: String) {
        _activeLiveTrackers.update { currentMap ->
            val tracker = currentMap[bookingId] ?: return@update currentMap
            val updatedLogs = tracker.logs + ActivityLog(title, description)
            currentMap + (bookingId to tracker.copy(logs = updatedLogs, lastUpdated = System.currentTimeMillis()))
        }
    }

    fun addTrackingNotification(bookingId: Long, title: String, message: String, tag: String) {
        _activeLiveTrackers.update { currentMap ->
            val tracker = currentMap[bookingId] ?: return@update currentMap
            val updatedNotifs = tracker.notifications + TrackingNotification(
                id = "notif_${System.currentTimeMillis()}",
                title = title,
                message = message,
                tag = tag
            )
            currentMap + (bookingId to tracker.copy(notifications = updatedNotifs, lastUpdated = System.currentTimeMillis()))
        }
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
        _activeLiveTrackers.update { it + (bookingId to currentTracker.copy(messages = updatedMessages, lastUpdated = System.currentTimeMillis())) }

        viewModelScope.launch {
            delay(1500)
            val replyText = when {
                text.lowercase().contains("where") || text.lowercase().contains("eta") -> "Hi! We are currently en route on the bypass. Current ETA is about ${currentTracker.etaMinutes} minutes."
                text.lowercase().contains("gate") || text.lowercase().contains("parking") -> "Copy that, gate intelligence received! Adjusting maneuvers accordingly."
                text.lowercase().contains("issue") || text.lowercase().contains("help") -> "This is SACHIO Support Cockpit. We have logged your request. Dispatch engineer is on rapid alert."
                imageUri != null -> "Image attachment received successfully. Reviewing space clearance with dispatch team."
                locationShare != null -> "Location pin received: (%.4f, %.4f). Tuning routing vectors.".format(locationShare.first, locationShare.second)
                else -> "Prestige service acknowledged! We are dedicated to providing 'Comfort. Hygiene. Dignity' at your venue."
            }
            val trackerNow = _activeLiveTrackers.value[bookingId] ?: return@launch
            val finalizedMessages = trackerNow.messages + ChatMessage(
                id = "msg_reply_${System.currentTimeMillis()}",
                sender = if (text.lowercase().contains("help") || text.lowercase().contains("issue")) "Support Office" else "Obinna (Driver)",
                text = replyText
            )
            _activeLiveTrackers.update { it + (bookingId to trackerNow.copy(messages = finalizedMessages)) }
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
                boxWidthOffset = 3.3,
                etaMinutes = 25,
                isTrackingActive = true,
                journeyStatus = "Staff Assigned",
                staffName = "Obinna Okafor",
                staffState = "Standby"
            )

            _activeLiveTrackers.update { it + (bookingId to initialTracker) }

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

            _activeLiveTrackers.update { it + (bookingId to trackerState) }

            addActivityLog(bookingId, "Vehicle Dispatched", "SACHIO Restroom Hauler departed VI Operations Center.")
            addActivityLog(bookingId, "En Route", "Dispatch team is cruising along the expressway.")
            addTrackingNotification(
                bookingId = bookingId,
                title = "Vehicle Dispatched",
                message = "Your luxury cabins are en route! Hauler has departed depot.",
                tag = "VEHICLE_DISPATCHED"
            )

            launch {
                val steps = 10
                for (i in 1..steps) {
                    delay(3000)
                    val currentTracker = _activeLiveTrackers.value[bookingId] ?: break
                    if (currentTracker.journeyStatus != "In Transit") break

                    val pct = i.toDouble() / steps.toDouble()
                    val nextStaffLat = startLat + pct * (booking.latitude - startLat)
                    val nextStaffLng = startLng + pct * (booking.longitude - startLng)
                    val nextVehLat = vehicleStartLat + pct * (booking.latitude - vehicleStartLat)
                    val nextVehLng = vehicleStartLng + pct * (booking.longitude - vehicleStartLng)

                    val nextDist = (12.5 * (1.0 - pct)).coerceAtLeast(0.0)
                    val nextEta = (20 * (1.0 - pct)).toInt().coerceAtLeast(1)

                    val nextLogs = currentTracker.logs.toMutableList()
                    if (i == 3) {
                        nextLogs.add(ActivityLog("Toll Gate Bypass", "Cruising past Toll area. Optimizing alternate speed bypass."))
                    } else if (i == 7) {
                        nextLogs.add(ActivityLog("Vicinity Approaching", "Nearing venue gates. Attendant setting up toolkits."))
                    }

                    _activeLiveTrackers.update {
                        it + (bookingId to currentTracker.copy(
                            staffLat = nextStaffLat,
                            staffLng = nextStaffLng,
                            vehicleLat = nextVehLat,
                            vehicleLng = nextVehLng,
                            distanceRemainingKm = nextDist,
                            etaMinutes = if (i == steps) 0 else nextEta,
                            logs = nextLogs,
                            lastUpdated = System.currentTimeMillis()
                        ))
                    }
                }

                val finalTracker = _activeLiveTrackers.value[bookingId]
                if (finalTracker != null && finalTracker.journeyStatus == "In Transit") {
                    _activeLiveTrackers.update {
                        it + (bookingId to finalTracker.copy(
                            staffLat = booking.latitude,
                            staffLng = booking.longitude,
                            vehicleLat = booking.latitude,
                            vehicleLng = booking.longitude,
                            distanceRemainingKm = 0.0,
                            etaMinutes = 0,
                            journeyStatus = "Arrived",
                            staffState = "Standby"
                        ))
                    }
                    repository.updateBookingStatus(bookingId, "ARRIVED")
                    addActivityLog(bookingId, "Arrived at Venue", "Restroom hauler has safely parked at event coordinates.")
                }
            }
        }
    }

    fun staffMarkArrived(bookingId: Long) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "ARRIVED")
            val trackerState = _activeLiveTrackers.value[bookingId]
            if (trackerState != null) {
                val booking = database.sashioDao().getBookingById(bookingId)
                val targetLat = booking?.latitude ?: trackerState.staffLat
                val targetLng = booking?.longitude ?: trackerState.staffLng
                _activeLiveTrackers.update {
                    it + (bookingId to trackerState.copy(
                        staffLat = targetLat,
                        staffLng = targetLng,
                        vehicleLat = targetLat,
                        vehicleLng = targetLng,
                        distanceRemainingKm = 0.0,
                        etaMinutes = 0,
                        journeyStatus = "Arrived",
                        staffState = "Standby"
                    ))
                }
                addActivityLog(bookingId, "Arrived at Venue", "Staff marked arrival manually. Fleet parked at designated slot.")
            }
        }
    }

    fun staffStartSetup(bookingId: Long) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "SETUP IN PROGRESS")
            val trackerState = _activeLiveTrackers.value[bookingId]
            if (trackerState != null) {
                _activeLiveTrackers.update {
                    it + (bookingId to trackerState.copy(
                        journeyStatus = "Setup",
                        staffState = "Setting Up",
                        lastUpdated = System.currentTimeMillis()
                    ))
                }
                addActivityLog(bookingId, "Setup Started", "Chief Attendant is deploying high-pressure waste connectors and stabilizers.")
            }
        }
    }

    fun staffMarkActive(bookingId: Long) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "SERVICE ACTIVE")
            val trackerState = _activeLiveTrackers.value[bookingId]
            if (trackerState != null) {
                _activeLiveTrackers.update {
                    it + (bookingId to trackerState.copy(
                        journeyStatus = "Active",
                        staffState = "Active Monitoring",
                        lastUpdated = System.currentTimeMillis()
                    ))
                }
                addActivityLog(bookingId, "Service Active", "Sanitary cabins fully operational. Air fresheners, generators, and soaps active.")
            }
        }
    }

    fun staffPerformMaintenance(bookingId: Long) {
        viewModelScope.launch {
            val trackerState = _activeLiveTrackers.value[bookingId]
            if (trackerState != null) {
                _activeLiveTrackers.update {
                    it + (bookingId to trackerState.copy(
                        journeyStatus = "Maintenance",
                        staffState = "Cleaning & Upkeep",
                        lastUpdated = System.currentTimeMillis()
                    ))
                }
                addActivityLog(bookingId, "Maintenance Performed", "Attendant performed scheduled pressure wash and fluid refill.")
            }
        }
    }

    fun staffMarkCompleted(bookingId: Long) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "COMPLETED")
            val trackerState = _activeLiveTrackers.value[bookingId]
            if (trackerState != null) {
                _activeLiveTrackers.update {
                    it + (bookingId to trackerState.copy(
                        journeyStatus = "Completed",
                        staffState = "Standby",
                        isTrackingActive = false,
                        lastUpdated = System.currentTimeMillis()
                    ))
                }
                addActivityLog(bookingId, "Service Completed", "Sanitary units cleared, disinfected, packed and secured.")
            }
        }
    }

    fun calculateLogisticsMetrics(lat: Double, lng: Double): LogisticsMetrics {
        val depotLat = 6.4282
        val depotLng = 3.4211
        val dLat = lat - depotLat
        val dLng = lng - depotLng
        val distanceKm = Math.sqrt(dLat * dLat + dLng * dLng) * 111.0
        val travelTimeMinutes = (distanceKm * 2.5).toInt().coerceAtLeast(15)
        val fuelUsageLitres = (distanceKm * 0.15).coerceAtLeast(5.0)

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

    // --- Supabase-Backed Expense Management ---
    fun loadSupabaseExpenses() {
        viewModelScope.launch {
            try {
                val rows = SupabaseProvider.client.postgrest.from("expenses")
                    .select {
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<SupabaseExpense>()
                _supabaseExpenses.value = rows
                _roleManagementError.value = null
            } catch (e: Exception) {
                _roleManagementError.value = "Could not load expenses: ${e.message}"
            }
        }
    }

    fun submitStaffExpense(amount: Double, category: String, notes: String, date: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val supaUser = SupabaseProvider.client.auth.currentUserOrNull()
                val staffName = _currentUser.value?.name ?: supaUser?.email ?: "Field Operative"
                val staffId = supaUser?.id ?: _currentUser.value?.id?.toString()

                SupabaseProvider.client.postgrest.from("expenses").insert(
                    NewSupabaseExpense(
                        staff_id = staffId,
                        staff_name = staffName,
                        category = category,
                        amount = amount,
                        details = notes,
                        status = "Pending Approval",
                        expense_date = date
                    )
                )
                loadSupabaseExpenses()
                onComplete()
            } catch (e: Exception) {
                val newExp = StaffExpense(
                    id = "EXP-${Random().nextInt(9000) + 1000}",
                    amount = amount,
                    date = date,
                    category = category,
                    notes = notes,
                    imageUri = "",
                    status = "Pending Approval"
                )
                _staffExpenses.update { listOf(newExp) + it }
                onComplete()
            }
        }
    }

    fun submitExpenseFromAdmin(staffName: String, category: String, amount: Double, details: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                SupabaseProvider.client.postgrest.from("expenses").insert(
                    NewSupabaseExpense(
                        staff_name = staffName,
                        category = category,
                        amount = amount,
                        details = details,
                        status = "Approved",
                        expense_date = dateStr
                    )
                )
                loadSupabaseExpenses()
                onComplete()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not submit expense: ${e.message}"
            }
        }
    }

    fun approveExpenseClaim(id: String) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("expenses").update(
                    buildJsonObject { put("status", "Approved") }
                ) {
                    filter { eq("id", id) }
                }
                loadSupabaseExpenses()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not approve expense: ${e.message}"
            }
        }
    }

    fun rejectExpenseClaim(id: String) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.postgrest.from("expenses").update(
                    buildJsonObject { put("status", "Rejected") }
                ) {
                    filter { eq("id", id) }
                }
                loadSupabaseExpenses()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not reject expense: ${e.message}"
            }
        }
    }

    // --- Supabase-Backed Staff Attendance ---
    fun loadSupabaseAttendance() {
        viewModelScope.launch {
            try {
                val rows = SupabaseProvider.client.postgrest.from("staff_attendance")
                    .select {
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<SupabaseAttendance>()
                _supabaseAttendance.value = rows
                _roleManagementError.value = null
            } catch (e: Exception) {
                _roleManagementError.value = "Could not load attendance: ${e.message}"
            }
        }
    }

    fun clockInStaff(location: String, onComplete: () -> Unit = {}) {
        _isClockedIn.value = true
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            try {
                val supaUser = SupabaseProvider.client.auth.currentUserOrNull()
                val staffName = _currentUser.value?.name ?: supaUser?.email ?: "Field Operative"
                val staffId = supaUser?.id ?: _currentUser.value?.id?.toString()

                SupabaseProvider.client.postgrest.from("staff_attendance").insert(
                    NewSupabaseAttendance(
                        staff_id = staffId,
                        staff_name = staffName,
                        clock_in_time = nowIso,
                        location_verified = location,
                        is_verified = true
                    )
                )
                loadSupabaseAttendance()
                onComplete()
            } catch (e: Exception) {
                val nowStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val newLog = AttendanceLog(
                    id = "ATT-${Random().nextInt(900) + 100}",
                    clockInTime = nowStr,
                    clockOutTime = null,
                    date = dateStr,
                    location = location,
                    status = "Checked In"
                )
                _attendanceHistory.update { listOf(newLog) + it }
                onComplete()
            }
        }
    }

    fun clockOutStaff(onComplete: () -> Unit = {}) {
        _isClockedIn.value = false
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            try {
                val supaUser = SupabaseProvider.client.auth.currentUserOrNull()
                val staffId = supaUser?.id ?: _currentUser.value?.id?.toString()

                if (staffId != null) {
                    val openRecords = _supabaseAttendance.value.filter { 
                        it.staff_id == staffId && it.clock_out_time.isNullOrBlank() 
                    }
                    val latest = openRecords.firstOrNull()
                    if (latest != null) {
                        SupabaseProvider.client.postgrest.from("staff_attendance").update(
                            buildJsonObject { put("clock_out_time", nowIso) }
                        ) {
                            filter { eq("id", latest.id) }
                        }
                    }
                }
                loadSupabaseAttendance()
                onComplete()
            } catch (e: Exception) {
                val nowStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                _attendanceHistory.update { list ->
                    list.mapIndexed { idx, log ->
                        if (idx == 0 && log.clockOutTime == null) {
                            log.copy(clockOutTime = nowStr, status = "Completed")
                        } else {
                            log
                        }
                    }
                }
                onComplete()
            }
        }
    }

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
            id = "INSP-${Random().nextInt(9000) + 1000}",
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
        _inspections.update { listOf(newInsp) + it }
        addActivityLog(bookingId, "Digital Quality Check", "Field technician transmitted digital parameters: Cleanliness ($cleanlinessScore/5), Condition ($toiletCondition).")
    }

    fun loadGeneralChats() {
        viewModelScope.launch {
            try {
                val rows = SupabaseProvider.client.postgrest.from("messages")
                    .select {
                        order("created_at", Order.ASCENDING)
                    }
                    .decodeList<SupabaseMessage>()
                _generalChats.value = rows
            } catch (e: Exception) {
                _roleManagementError.value = "Could not load messages: ${e.message}"
            }
        }
    }

    fun sendGeneralChatMessage(text: String, channel: String = "admin", recipientId: String? = null) {
        viewModelScope.launch {
            try {
                val supaUser = SupabaseProvider.client.auth.currentUserOrNull() ?: run {
                    _roleManagementError.value = "You need to be signed in to send a message."
                    return@launch
                }

                val senderName = _currentUser.value?.name ?: supaUser.email ?: "User"
                val senderRole = _currentUser.value?.role ?: "client"

                val targetRole = when (channel.trim().lowercase()) {
                    "admin center", "admin", "field supervisor", "supervisor" -> "admin"
                    "staff", "field operative", "crews" -> "staff"
                    "active customer", "client", "clients" -> "client"
                    "all", "broadcast" -> "all"
                    else -> channel.trim().lowercase()
                }

                SupabaseProvider.client.postgrest.from("messages").insert(
                    buildJsonObject {
                        put("sender_id", supaUser.id)
                        put("sender_name", senderName)
                        put("sender_role", senderRole)
                        if (recipientId != null) {
                            put("recipient_id", recipientId)
                        } else {
                            put("recipient_role", targetRole)
                        }
                        put("content", text.trim())
                    }
                )
                loadGeneralChats()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not send message: ${e.message}"
            }
        }
    }

    fun toggleProfileSuspension(id: Long) {
        _suspendedProfileIds.update { if (it.contains(id)) it - id else it + id }
    }

    fun addNewStaffProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            repository.registerProfile(name = name, email = email, role = "staff", phone = phone)
        }
    }

    fun createIncident(title: String, reporter: String, priority: String, details: String) {
        _incidents.update { list ->
            val newId = "INC-${100 + list.size + 1}"
            val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
            listOf(AdminIncident(newId, title, reporter, priority, "Open", dateStr, details)) + list
        }
    }
}

fun completeGoogleSignIn() {
    viewModelScope.launch {
        _loginError.value = null

        try {
            val authUser = SupabaseProvider.client.auth.currentUserOrNull()
            val uid = authUser?.id
            val email = authUser?.email

            if (uid.isNullOrBlank() || email.isNullOrBlank()) {
                _loginError.value = "Google sign-in did not return a valid account."
                return@launch
            }

            val profile = SupabaseProvider.client.postgrest
                .from("profiles")
                .select {
                    filter { eq("id", uid) }
                }
                .decodeSingle<SupabaseProfile>()

            val user = UserProfile(
                id = 1,
                name = profile.full_name ?: email,
                email = profile.email,
                role = profile.role,
                phoneNumber = profile.phone_number ?: "",
                companyName = profile.company_name ?: ""
            )

            _currentUser.value = user

            when (user.role) {
                "client" -> _currentScreen.value = "client_dashboard"
                "staff" -> _currentScreen.value = "staff_dashboard"
                "admin" -> _currentScreen.value = "admin_dashboard"
                else -> {
                    _currentUser.value = null
                    _loginError.value = "Your account has no valid application role."
                }
            }
        } catch (e: Exception) {
            _loginError.value =
                "Google account authenticated, but profile loading failed: ${e.localizedMessage}"
        }
    }
}

// --- Local Data Models for Attendance & Logs ---
data class StaffExpense(
    val id: String,
    val amount: Double,
    val date: String,
    val category: String,
    val notes: String,
    val imageUri: String,
    val status: String
)

data class AttendanceLog(
    val id: String,
    val clockInTime: String,
    val clockOutTime: String?,
    val date: String,
    val location: String,
    val status: String
)

data class DigitalInspection(
    val id: String,
    val bookingId: Long,
    val toiletCondition: String,
    val waterLevel: String,
    val cleanlinessScore: Int,
    val equipmentCondition: String,
    val damageAssessment: String,
    val comments: String,
    val beforePhotos: List<String>,
    val afterPhotos: List<String>,
    val timestamp: Long
)

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
    val sender: String,
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
    val boxWidthOffset: Double = 3.3,
    val etaMinutes: Int = 25,
    val isTrackingActive: Boolean = false,
    val journeyStatus: String = "Idle",
    val lastUpdated: Long = System.currentTimeMillis(),
    val staffName: String = "Obinna Okafor",
    val staffPhone: String = "+234 812 345 6789",
    val staffExperience: Int = 8,
    val staffState: String = "Driving",
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
