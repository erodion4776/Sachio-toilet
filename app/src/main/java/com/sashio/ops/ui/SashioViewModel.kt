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
                repository.getBookingsForClient(user.id, user.email)
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

private val _clientBookingsLoadError = MutableStateFlow<String?>(null)
val clientBookingsLoadError: StateFlow<String?> = _clientBookingsLoadError.asStateFlow()


    private val _bookingLiveLocations =
    MutableStateFlow<Map<String, BookingLiveLocation>>(emptyMap())

val bookingLiveLocations: StateFlow<Map<String, BookingLiveLocation>> =
    _bookingLiveLocations.asStateFlow()
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

                val normalizedEmail = dbProfile.email.trim().lowercase()
                val existingLocalProfile = repository.getProfileByEmail(normalizedEmail)
                val localProfileId = repository.insertProfile(
                    UserProfile(
                        id = existingLocalProfile?.id ?: 0L,
                        name = dbProfile.full_name ?: email,
                        email = normalizedEmail,
                        role = mappedRole,
                        phoneNumber = dbProfile.phone_number ?: "",
                        companyName = dbProfile.company_name ?: ""
                    )
                )
                val user = UserProfile(
                    id = localProfileId,
                    name = dbProfile.full_name ?: email,
                    email = normalizedEmail,
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
    val error = (e.message ?: "").lowercase()

    _loginError.value = when {
        error.contains("invalid login credentials") ->
            "Incorrect email or password. Please try again."

        error.contains("email not confirmed") ->
            "Please verify your email before logging in."

        error.contains("too many requests") ||
        error.contains("rate limit") ->
            "Too many login attempts. Please wait a moment and try again."

        error.contains("network") ||
        error.contains("timeout") ||
        error.contains("connect") ->
            "Connection problem. Please check your internet and try again."

                else ->
            "We couldn't log you in right now. Please try again."
    }
}
    }
}

fun completeGoogleSignIn() {
    viewModelScope.launch {
        _loginError.value = null

        try {
            // Wait up to 10 seconds for Supabase to finish
            // processing the Google callback.
            var finalUser = SupabaseProvider.client.auth.currentUserOrNull()

            repeat(20) {
                if (finalUser != null) return@repeat
                delay(500)
                finalUser = SupabaseProvider.client.auth.currentUserOrNull()
            }

            val uid = finalUser?.id

            if (uid.isNullOrBlank()) {
                _loginError.value =
                    "Google sign-in failed: Supabase session was not received. Please try again."
                _currentScreen.value = "login"
                return@launch
            }

            val dbProfile = SupabaseProvider.client.postgrest
                .from("profiles")
                .select {
                    filter {
                        eq("id", uid)
                    }
                }
                .decodeSingle<SupabaseProfile>()

            val normalizedEmail = dbProfile.email.trim().lowercase()
            val existingLocalProfile = repository.getProfileByEmail(normalizedEmail)
            val localProfileId = repository.insertProfile(
                UserProfile(
                    id = existingLocalProfile?.id ?: 0L,
                    name = dbProfile.full_name ?: dbProfile.email,
                    email = normalizedEmail,
                    role = dbProfile.role,
                    phoneNumber = dbProfile.phone_number ?: "",
                    companyName = dbProfile.company_name ?: ""
                )
            )
            val user = UserProfile(
                id = localProfileId,
                name = dbProfile.full_name ?: dbProfile.email,
                email = normalizedEmail,
                role = dbProfile.role,
                phoneNumber = dbProfile.phone_number ?: "",
                companyName = dbProfile.company_name ?: ""
            )

            _currentUser.value = user
            _loginError.value = null

            when (user.role.lowercase()) {
                "client" -> _currentScreen.value = "client_dashboard"
                "staff" -> _currentScreen.value = "staff_dashboard"
                "admin" -> _currentScreen.value = "admin_dashboard"
                else -> {
                    _currentUser.value = null
                    _loginError.value =
                        "Your account has an unsupported role. Please contact the administrator."
                    _currentScreen.value = "login"
                }
            }

        } catch (e: Exception) {
            _currentUser.value = null
            _loginError.value =
                "Google sign-in failed: ${e.localizedMessage ?: "Please try again."}"
            _currentScreen.value = "login"
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
            SupabaseProvider.client.postgrest.rpc(
                function = "admin_set_user_role",
                parameters = buildJsonObject {
                    put("p_user_id", userId)
                    put("p_new_role", newRole)
                }
            )

            loadSupabaseProfiles()
            _roleManagementError.value = null
        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not update role: ${e.message}"
        }
    }
}


    fun adminDeleteUserProfile(
    userId: String,
    onComplete: () -> Unit = {}
) {
    viewModelScope.launch {
        try {
            SupabaseProvider.client.postgrest.rpc(
                function = "admin_delete_user_profile",
                parameters = buildJsonObject {
                    put("p_user_id", userId)
                }
            )

            loadSupabaseProfiles()
            _roleManagementError.value = null
            onComplete()
        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not delete user: ${e.message}"
        }
    }
}

    fun adminSetProfileSuspended(userId: String, suspended: Boolean) {
    viewModelScope.launch {
        try {
            SupabaseProvider.client.postgrest.rpc(
                function = "admin_set_profile_suspended",
                parameters = buildJsonObject {
                    put("p_user_id", userId)
                    put("p_suspended", suspended)
                }
            )

            loadSupabaseProfiles()
            _roleManagementError.value = null
        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not update status: ${e.message}"
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
            SupabaseProvider.client.postgrest.rpc(
                function = "admin_delete_booking",
                parameters = buildJsonObject {
                    put("p_booking_id", bookingId)
                }
            )

            loadSupabaseBookings()
            _roleManagementError.value = null
            onComplete()
        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not delete booking: ${e.message}"
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
            SupabaseProvider.client.postgrest.rpc(
                function = "admin_issue_booking_quotation",
                parameters = buildJsonObject {
                    put("p_booking_id", bookingId)
                    put("p_quoted_price", quotedPrice)
                    put("p_logistics_cost", logisticsCost)
                    put("p_vat_amount", vatAmount)
                    put("p_admin_notes", adminNotes)
                }
            )

            loadSupabaseBookings()
            _roleManagementError.value = null
            onComplete()
        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not issue quotation: ${e.message}"
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
            SupabaseProvider.client.postgrest.rpc(
                function = "admin_assign_booking_staff",
                parameters = buildJsonObject {
                    put("p_booking_id", bookingId)
                    put("p_staff_id", staffId)
                }
            )

            loadSupabaseBookings()
            _roleManagementError.value = null
            onComplete(true)
        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not assign staff: ${e.message}"
            onComplete(false)
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

fun staffUpdateBookingStatus(
    bookingId: String,
    newStatus: String,
    onError: (String) -> Unit = {},
    onComplete: () -> Unit = {}
) {
    viewModelScope.launch {
        try {
            SupabaseProvider.client.postgrest.rpc(
                function = "staff_update_booking_status",
                parameters = buildJsonObject {
                    put("p_booking_id", bookingId)
                    put("p_new_status", newStatus)
                }
            )

            loadMyAssignedBookings()
            loadSupabaseBookings()
            loadClientSupabaseBookings()
            _roleManagementError.value = null
            onComplete()
        } catch (e: Exception) {
            val message = "Could not update job status: ${e.message ?: "Unknown error"}"
            _roleManagementError.value = message
            onError(message)
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
            var localBookingId: Long? = null
            try {
                val supaUser = SupabaseProvider.client.auth.currentUserOrNull()
                    ?: throw Exception("Your login session has expired. Please sign in again.")
                localBookingId = repository.insertBooking(booking)

                // Room IDs are local to each device/install. A reinstall or a
                // second device can reuse an ID already stored for this client.
                // Preserve the mapping only when it is not already in use.
                val existingLocalId = SupabaseProvider.client.postgrest
                    .from("bookings")
                    .select {
                        filter {
                            eq("client_id", supaUser.id)
                            eq("local_booking_id", localBookingId)
                        }
                    }
                    .decodeList<SupabaseBooking>()
                    .isNotEmpty()

                val remoteLocalBookingId = if (existingLocalId) null else localBookingId
                val newBooking = NewSupabaseBooking(
                        client_id = supaUser.id,
                        local_booking_id = remoteLocalBookingId,
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
                        event_date = booking.eventDate.takeIf { it.isNotBlank() },
                        cleaning_support = booking.cleaningSupport,
                        on_site_attendant = booking.onSiteAttendant,
                        waste_evacuation = booking.wasteEvacuation,
                        maintenance_support = booking.maintenanceSupport,
                        special_instructions = booking.specialInstructions
                    )

                try {
                    SupabaseProvider.client.postgrest.from("bookings").insert(newBooking)
                } catch (insertError: Exception) {
                    // Handle a simultaneous request that claimed the same local
                    // ID after the pre-check. Retry once without the device-local ID.
                    if (!existingLocalId &&
                        insertError.message.orEmpty().contains(
                            "bookings_client_local_id_unique",
                            ignoreCase = true
                        )
                    ) {
                        SupabaseProvider.client.postgrest.from("bookings")
                            .insert(newBooking.copy(local_booking_id = null))
                    } else {
                        throw insertError
                    }
                }
                _bookingSyncMessage.value = "synced"
                loadClientSupabaseBookings()
                loadSupabaseBookings()
                onComplete()
            } catch (e: Exception) {
                localBookingId?.let {
                    try { repository.deleteBooking(it) } catch (_: Exception) { }
                }
                _bookingSyncMessage.value = "sync_failed: ${e.message ?: "Please check your internet connection and try again."}"
            }
        }
    }


    fun clientConvertCopyToBooking(
    bookingId: Long,
    serviceDate: String,
    duration: String,
    deliveryAddress: String,
    specialInstructions: String,
    onComplete: (String) -> Unit
) {
    viewModelScope.launch {
        try {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: throw Exception("Please sign in again.")

            val rows = SupabaseProvider.client.postgrest
                .from("bookings")
                .select {
                    filter {
                        eq("client_id", uid)
                        eq("local_booking_id", bookingId)
                    }
                }
                .decodeList<SupabaseBooking>()

            val remoteBooking = rows.firstOrNull()
                ?: throw Exception("Could not find your online quotation.")

            SupabaseProvider.client.postgrest.rpc(
                function = "client_accept_booking_quotation",
                parameters = buildJsonObject {
                    put("p_booking_id", remoteBooking.id)
                    put("p_service_date", serviceDate)
                    put("p_duration", duration)
                    put("p_delivery_address", deliveryAddress)
                    put("p_special_instructions", specialInstructions)
                }
            )

            val booking = database.sashioDao().getBookingById(bookingId)

            if (booking != null) {
                repository.updateBooking(
                    booking.copy(
                        eventDate = serviceDate,
                        duration = duration,
                        address = deliveryAddress,
                        specialInstructions = specialInstructions,
                        status = "Client Accepted"
                    )
                )
            }

            loadClientSupabaseBookings()
            _roleManagementError.value = null
            onComplete(remoteBooking.id)

        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not accept quotation: ${e.message}"
        }
    }
}
    fun clientAcceptRemoteQuotation(booking: SupabaseBooking, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Please sign in again.")
                if (booking.client_id != uid) throw Exception("This quotation does not belong to your account.")
                if (booking.payment_status.equals("success", ignoreCase = true)) {
                    throw Exception("This quotation has already been paid.")
                }
                SupabaseProvider.client.postgrest.rpc(
                    function = "client_accept_booking_quotation",
                    parameters = buildJsonObject {
                        put("p_booking_id", booking.id)
                        put("p_service_date", booking.event_date ?: "")
                        put("p_duration", booking.duration)
                        put("p_delivery_address", booking.address)
                        put("p_special_instructions", booking.special_instructions)
                    }
                )
                loadClientSupabaseBookings()
                _roleManagementError.value = null
                onComplete(true)
            } catch (e: Exception) {
                _roleManagementError.value = "Could not accept quotation: ${e.message}"
                onComplete(false)
            }
        }
    }

    fun clientCancelQuotation(bookingId: Long, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Please sign in again.")
                val remote = SupabaseProvider.client.postgrest.from("bookings")
                    .select {
                        filter {
                            eq("client_id", uid)
                            eq("local_booking_id", bookingId)
                        }
                    }
                    .decodeList<SupabaseBooking>()
                    .firstOrNull()

                if (remote != null) {
                    SupabaseProvider.client.postgrest.rpc(
                        function = "client_cancel_booking_quotation",
                        parameters = buildJsonObject { put("p_booking_id", remote.id) }
                    )
                }
                repository.updateBookingStatus(bookingId, "Cancelled")
                _roleManagementError.value = null
                loadClientSupabaseBookings()
                onComplete(true)
            } catch (e: Exception) {
                _roleManagementError.value = "Could not cancel this order: ${e.message}"
                onComplete(false)
            }
        }
    }

    fun clientCancelRemoteQuotation(booking: SupabaseBooking, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Please sign in again.")
                if (booking.client_id != uid) throw Exception("This order does not belong to your account.")
                SupabaseProvider.client.postgrest.rpc(
                    function = "client_cancel_booking_quotation",
                    parameters = buildJsonObject { put("p_booking_id", booking.id) }
                )
                loadClientSupabaseBookings()
                _roleManagementError.value = null
                onComplete(true)
            } catch (e: Exception) {
                _roleManagementError.value = "Could not cancel this order: ${e.message}"
                onComplete(false)
            }
        }
    }

    fun clientDeleteCancelledQuotation(bookingId: Long, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Please sign in again.")
                val remote = SupabaseProvider.client.postgrest.from("bookings")
                    .select {
                        filter {
                            eq("client_id", uid)
                            eq("local_booking_id", bookingId)
                        }
                    }
                    .decodeList<SupabaseBooking>()
                    .firstOrNull()
                if (remote != null) {
                    SupabaseProvider.client.postgrest.rpc(
                        function = "client_delete_cancelled_booking",
                        parameters = buildJsonObject { put("p_booking_id", remote.id) }
                    )
                }
                repository.deleteBooking(bookingId)
                _roleManagementError.value = null
                loadClientSupabaseBookings()
                onComplete(true)
            } catch (e: Exception) {
                _roleManagementError.value = "Could not delete this cancelled order: ${e.message}"
                onComplete(false)
            }
        }
    }

    fun clientRejectQuotation(bookingId: Long, onComplete: () -> Unit) {
        clientCancelQuotation(bookingId) { onComplete() }
    }

    fun clientDeleteCancelledRemoteQuotation(booking: SupabaseBooking, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Please sign in again.")
                if (booking.client_id != uid) throw Exception("This order does not belong to your account.")
                SupabaseProvider.client.postgrest.rpc(
                    function = "client_delete_cancelled_booking",
                    parameters = buildJsonObject { put("p_booking_id", booking.id) }
                )
                loadClientSupabaseBookings()
                _roleManagementError.value = null
                onComplete(true)
            } catch (e: Exception) {
                _roleManagementError.value = "This order cannot be deleted: ${e.message}"
                onComplete(false)
            }
        }
    }

    fun clientRequestRevision(bookingId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Please sign in again.")
                val remote = SupabaseProvider.client.postgrest.from("bookings")
                    .select {
                        filter {
                            eq("client_id", uid)
                            eq("local_booking_id", bookingId)
                        }
                    }
                    .decodeList<SupabaseBooking>()
                    .firstOrNull()
                if (remote != null) {
                    SupabaseProvider.client.postgrest.from("bookings").update(
                        mapOf("status" to "Under Review")
                    ) {
                        filter {
                            eq("id", remote.id)
                            eq("client_id", uid)
                            eq("payment_status", "pending")
                        }
                    }
                }
                repository.updateBookingStatus(bookingId, "Under Review")
                loadClientSupabaseBookings()
                _roleManagementError.value = null
                onComplete()
            } catch (e: Exception) {
                _roleManagementError.value = "Could not request a quote revision: ${e.message}"
            }
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
            SupabaseProvider.client.postgrest.rpc(
                function = "admin_update_catalog_item",
                parameters = buildJsonObject {
                    put("p_id", id)
                    put("p_title", title)
                    put("p_description", description)
                    put("p_model_number", modelNumber)
                    put("p_variant_name", variantName)
                    put("p_base_price", basePrice)
                    put("p_image_url", imageUrl)
                    putJsonArray("p_image_urls") {
                        imageUrls.forEach { add(it) }
                    }
                    put("p_video_url", videoUrl)
                    put("p_features", features)
                    put("p_specifications", specifications)
                    put("p_benefits", benefits)
                    put("p_use_cases", useCases)
                }
            )

            loadSupabaseCatalogItems()
            _roleManagementError.value = null
            onComplete()
        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not update catalog item: ${e.message}"
        }
    }
}


    fun adminDeleteSupabaseCatalogItem(
    id: String,
    onComplete: () -> Unit = {}
) {
    viewModelScope.launch {
        try {
            SupabaseProvider.client.postgrest.rpc(
                function = "admin_delete_catalog_item",
                parameters = buildJsonObject {
                    put("p_id", id)
                }
            )

            loadSupabaseCatalogItems()
            _roleManagementError.value = null
            onComplete()
        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not delete catalog item: ${e.message}"
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
        _clientSupabaseBookings.value = emptyList()
        _clientBookingsLoadError.value = null

        try {
            var uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
            var attempts = 0

            while (uid.isNullOrBlank() && attempts < 10) {
                delay(500)
                uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                attempts++
            }

            if (uid.isNullOrBlank()) {
                _clientBookingsLoadError.value =
                    "Your secure session is not ready. Please tap Retry."
                return@launch
            }

            val rows = SupabaseProvider.client.postgrest.from("bookings")
                .select {
                    filter { eq("client_id", uid!!) }
                }
                .decodeList<SupabaseBooking>()

            _clientSupabaseBookings.value = rows
            _clientBookingsLoadError.value = null

        } catch (e: Exception) {
            _clientBookingsLoadError.value =
                "Could not load your latest quotations: ${e.message ?: "Please check your connection."}"
        }
    }
}

    fun loadBookingLiveLocation(bookingId: String) {
    viewModelScope.launch {
        try {
            val rows = SupabaseProvider.client.postgrest.from("booking_live_locations")
                .select {
                    filter { eq("booking_id", bookingId) }
                }
                .decodeList<BookingLiveLocation>()

            val latest = rows.maxByOrNull { it.updated_at }

            if (latest != null) {
                _bookingLiveLocations.update {
                    it + (bookingId to latest)
                }
            } else {
                _bookingLiveLocations.update {
                    it - bookingId
                }
            }
        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not load staff GPS location: ${e.message}"
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
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < 3) {
            _addressSuggestions.value = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            // Debounce typing and ignore stale responses from earlier searches.
            delay(450)
            if (_addressSearchQuery.value.trim() != trimmedQuery) return@launch

            try {
                // Photon is an OpenStreetMap-based address search service. Unlike the
                // Android Geocoder callback, this returns results synchronously to this
                // IO coroutine and does not require a Google Maps API key.
                val encodedQuery = URLEncoder.encode(trimmedQuery, "UTF-8")
                val request = Request.Builder()
                    .url("https://photon.komoot.io/api/?q=$encodedQuery&limit=8&lang=en")
                    .header("User-Agent", "SashioOps/2.5 (address search)")
                    .get()
                    .build()

                val response = storageHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()
                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val features = org.json.JSONObject(responseBody)
                        .optJSONArray("features")
                    val places = mutableListOf<SimulatedPlace>()

                    if (features != null) {
                        for (index in 0 until features.length()) {
                            val feature = features.optJSONObject(index) ?: continue
                            val geometry = feature.optJSONObject("geometry") ?: continue
                            val coordinates = geometry.optJSONArray("coordinates") ?: continue
                            if (coordinates.length() < 2) continue

                            val longitude = coordinates.optDouble(0, Double.NaN)
                            val latitude = coordinates.optDouble(1, Double.NaN)
                            if (!latitude.isFinite() || !longitude.isFinite() ||
                                latitude !in -90.0..90.0 || longitude !in -180.0..180.0
                            ) continue

                            val properties = feature.optJSONObject("properties")
                                ?: org.json.JSONObject()
                            val addressParts = listOf(
                                properties.optString("housenumber").takeIf { it.isNotBlank() },
                                properties.optString("street").takeIf { it.isNotBlank() },
                                properties.optString("name").takeIf { it.isNotBlank() },
                                properties.optString("district").takeIf { it.isNotBlank() },
                                properties.optString("city").takeIf { it.isNotBlank() },
                                properties.optString("state").takeIf { it.isNotBlank() },
                                properties.optString("country").takeIf { it.isNotBlank() }
                            ).filterNotNull().distinct()

                            val formattedAddress = addressParts.joinToString(", ")
                            if (formattedAddress.isNotBlank()) {
                                places.add(
                                    SimulatedPlace(
                                        address = formattedAddress,
                                        lat = latitude,
                                        lng = longitude
                                    )
                                )
                            }
                        }
                    }

                    if (_addressSearchQuery.value.trim() == trimmedQuery) {
                        _addressSuggestions.value = places.distinctBy {
                            "${it.lat},${it.lng}"
                        }
                    }
                    return@launch
                }
            } catch (_: Exception) {
                // Keep the suggestions empty; never show unrelated hard-coded locations.
            }

            if (_addressSearchQuery.value.trim() == trimmedQuery) {
                _addressSuggestions.value = emptyList()
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
            SupabaseProvider.client.postgrest.rpc(
                function = "admin_set_expense_status",
                parameters = buildJsonObject {
                    put("p_id", id)
                    put("p_status", "Approved")
                }
            )

            loadSupabaseExpenses()
            _roleManagementError.value = null
        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not approve expense: ${e.message}"
        }
    }
}

fun rejectExpenseClaim(id: String) {
    viewModelScope.launch {
        try {
            SupabaseProvider.client.postgrest.rpc(
                function = "admin_set_expense_status",
                parameters = buildJsonObject {
                    put("p_id", id)
                    put("p_status", "Rejected")
                }
            )

            loadSupabaseExpenses()
            _roleManagementError.value = null
        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not reject expense: ${e.message}"
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
    viewModelScope.launch {
        try {
            val staffId = SupabaseProvider.client.auth
                .currentUserOrNull()?.id
                ?: throw Exception("User is not authenticated")

            val openRecords = _supabaseAttendance.value.filter {
                it.staff_id == staffId &&
                it.clock_out_time.isNullOrBlank()
            }

            val latest = openRecords.firstOrNull()
                ?: throw Exception("No open attendance record found")

            SupabaseProvider.client.postgrest.rpc(
                function = "staff_clock_out",
                parameters = buildJsonObject {
                    put("p_id", latest.id)
                }
            )

            _isClockedIn.value = false
            _roleManagementError.value = null

            loadSupabaseAttendance()
            onComplete()

        } catch (e: Exception) {
            _roleManagementError.value =
                "Could not clock out: ${e.message}"
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
   
    fun createIncident(
        title: String,
        reporter: String,
        priority: String,
        details: String
    ) {
        _incidents.update { list ->
            val newId = "INC-${100 + list.size + 1}"

            val dateStr = SimpleDateFormat(
                "MMM d, yyyy",
                Locale.getDefault()
            ).format(Date())

            listOf(
                AdminIncident(
                    id = newId,
                    title = title,
                    reporter = reporter,
                    priority = priority,
                    status = "Open",
                    date = dateStr,
                    details = details
                )
            ) + list
        }
    }
}

    
// KEEP ALL EXISTING DATA CLASSES BELOW THIS LINE UNCHANGED.

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

