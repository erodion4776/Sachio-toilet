
package com.sashio.ops.ui

import com.sashio.ops.data.SupabaseBooking
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.Booking
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.data.UserProfile
import com.sashio.ops.ui.theme.*

@Composable
fun ClientDashboard(viewModel: SashioViewModel, client: UserProfile?) {
    var activeTab by remember { mutableStateOf("home") }
    var isBookingFormOpen by remember { mutableStateOf(false) }
    var selectedCategoryForQuote by remember { mutableStateOf<String?>(null) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    val catalogItems by viewModel.clientCatalogItems.collectAsStateWithLifecycle()
    val bookings by viewModel.clientBookings.collectAsStateWithLifecycle()
    val liveBookings by viewModel.clientSupabaseBookings.collectAsStateWithLifecycle()
    val bookingLoadError by viewModel.clientBookingsLoadError.collectAsStateWithLifecycle()
    val bookingSyncMessage by viewModel.bookingSyncMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(bookingSyncMessage) {
        val message = bookingSyncMessage
        if (message != null && message.startsWith("sync_failed:")) {
            val detail = message.removePrefix("sync_failed:").trim()
            Toast.makeText(context, "Quotation was NOT submitted: $detail", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(client?.id) {
        viewModel.loadSupabaseCatalogItems()
        viewModel.loadClientSupabaseBookings()
    }

    Scaffold(
        topBar = {
            DashboardHeader(
                roleName = "Prestige Portal",
                user = client,
                onSettingsClick = { isSettingsOpen = true },
                onLogout = { viewModel.logout() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SashioSlate,
                contentColor = Color.White
            ) {
                listOf(
                    Triple("home", Icons.Default.Dashboard, "Dashboard"),
                    Triple("catalog", Icons.Default.MenuBook, "Catalog"),
                    Triple("vip", Icons.Default.Star, "VIP Suite"),
                    Triple("tracker", Icons.Default.History, "Quotes & Tech"),
                    Triple("consult", Icons.Default.Forum, "Community & More")
                ).forEach { (tabId, icon, label) ->
                    NavigationBarItem(
                        selected = activeTab == tabId,
                        onClick = { activeTab = tabId },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (activeTab == tabId) {
                                    SashioGold
                                } else {
                                    Color.White.copy(alpha = 0.6f)
                                }
                            )
                        },
                        label = {
                            Text(
                                label,
                                fontSize = 10.sp,
                                color = if (activeTab == tabId) {
                                    SashioGold
                                } else {
                                    Color.White.copy(alpha = 0.6f)
                                }
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isSettingsOpen) {
                SashioSettingsScreen(
                    viewModel = viewModel,
                    onDismiss = { isSettingsOpen = false }
                )
            } else if (isBookingFormOpen) {
                BookingFormSheet(
                    viewModel = viewModel,
                    initialCategory = selectedCategoryForQuote
                        ?: "Standard Portable Toilets",
                    onDismiss = { isBookingFormOpen = false }
                )
            } else {
                when (activeTab) {
                    "home" -> ClientOverviewTab(
    client = client,
    bookings = bookings,
    liveBookings = liveBookings,
    bookingLoadError = bookingLoadError,
    onRetryBookings = { viewModel.loadClientSupabaseBookings() },
                        onRequestQuote = {
                            selectedCategoryForQuote =
                                "Standard Portable Toilets"
                            isBookingFormOpen = true
                        },
                        onNavigate = { tab ->
                            activeTab = tab
                        }
                    )

                    "catalog" -> ProductDiscoveryTab(
                        catalogItems = catalogItems,
                        onRequestQuote = { category ->
                            selectedCategoryForQuote = category
                            isBookingFormOpen = true
                        }
                    )

                    "vip" -> VipPresidentialTab(
                        onRequestQuote = {
                            selectedCategoryForQuote =
                                "Presidential Luxury Mobile Restrooms"
                            isBookingFormOpen = true
                        }
                    )

                    "tracker" -> ClientQuotationTrackerTab(viewModel)

                    "consult" -> ConsultationAndReviewsTab(viewModel)
                }
            }
        }
    }
}

@Composable
fun ClientOverviewTab(
    client: UserProfile?,
    bookings: List<Booking>,
    liveBookings: List<SupabaseBooking>,
    bookingLoadError: String?,
    onRetryBookings: () -> Unit,
    onRequestQuote: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    fun normalizeStatus(value: String) =
        value.trim().uppercase().replace('_', ' ').replace('-', ' ')

    // Use Supabase as the source of truth, while retaining local-only requests
    // that have not synced yet. This prevents an empty remote response from
    // making a returning client's dashboard appear to have no activity.
    val localOnlyBookings = bookings.filter { local ->
        liveBookings.none { it.local_booking_id == local.id }
    }
    val allRequestCount = liveBookings.size + localOnlyBookings.size
    val pendingQuotes = liveBookings.count {
        normalizeStatus(it.status) in setOf(
            "QUOTATION REQUESTED", "UNDER REVIEW", "QUOTATION GENERATED",
            "QUOTE REQUESTED", "QUOTE GENERATED", "PENDING REVIEW"
        )
    } + localOnlyBookings.count {
        normalizeStatus(it.status) in setOf(
            "QUOTATION REQUESTED", "UNDER REVIEW", "QUOTATION GENERATED",
            "QUOTE REQUESTED", "QUOTE GENERATED", "PENDING REVIEW"
        )
    }

    val activeQuotes = liveBookings.count {
        normalizeStatus(it.status) in setOf(
            "QUOTATION SENT", "CLIENT ACCEPTED", "AWAITING PAYMENT",
            "QUOTATION APPROVED", "QUOTE SENT", "PAYMENT PENDING"
        )
    } + localOnlyBookings.count {
        normalizeStatus(it.status) in setOf(
            "QUOTATION SENT", "CLIENT ACCEPTED", "AWAITING PAYMENT",
            "QUOTATION APPROVED", "QUOTE SENT", "PAYMENT PENDING"
        )
    }

    val activeBookings = liveBookings.count {
        normalizeStatus(it.status) in setOf(
            "PAYMENT CONFIRMED", "BOOKING CONFIRMED", "IN TRANSIT",
            "ARRIVED", "SETUP IN PROGRESS", "SERVICE DELIVERED",
            "DISPATCHED", "IN PROGRESS"
        )
    } + localOnlyBookings.count {
        normalizeStatus(it.status) in setOf(
            "PAYMENT CONFIRMED", "BOOKING CONFIRMED", "IN TRANSIT",
            "ARRIVED", "SETUP IN PROGRESS", "SERVICE DELIVERED",
            "DISPATCHED", "IN PROGRESS"
        )
    }

    val completedServices = liveBookings.count {
        normalizeStatus(it.status) in setOf("COMPLETED", "CLOSED", "FULFILLED")
    } + localOnlyBookings.count {
        normalizeStatus(it.status) in setOf("COMPLETED", "CLOSED", "FULFILLED")
    }

    val outstandingPayments = liveBookings
        .filter {
            !it.payment_status.equals("success", ignoreCase = true) &&
                (it.quoted_price + it.logistics_cost + it.vat_amount) > 0.0
        }
        .sumOf { it.quoted_price + it.logistics_cost + it.vat_amount } +
        localOnlyBookings.filter {
            !it.paymentStatus.equals("success", ignoreCase = true) &&
                (it.quotedPrice + it.logisticsCost + it.vatAmount) > 0.0
        }.sumOf { it.quotedPrice + it.logisticsCost + it.vatAmount }

    val totalPaid = liveBookings.filter {
        it.payment_status.equals("success", ignoreCase = true)
    }.sumOf { it.quoted_price + it.logistics_cost + it.vat_amount } +
        localOnlyBookings.filter {
            it.paymentStatus.equals("success", ignoreCase = true)
        }.sumOf { it.quotedPrice + it.logisticsCost + it.vatAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Welcome back, ${client?.name ?: "Valued Client"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onBackground
            )

            Text(
                "Your SACHIO prestige dashboard",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }

        item {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.labelMedium,
                color = SashioGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onRequestQuote,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SashioTeal,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "New Quote",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { onNavigate("tracker") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SashioSlate,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Track Service",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onNavigate("catalog") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SashioGold,
                        contentColor = SashioSlate
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Book Service",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { onNavigate("consult") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surfaceVariant,
                        contentColor = colors.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.Default.Forum,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Contact Support",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (!bookingLoadError.isNullOrBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "We couldn't refresh your quotations: $bookingLoadError",
                            color = colors.onErrorContainer,
                            fontSize = 12.sp
                        )
                        TextButton(onClick = onRetryBookings) {
                            Text("Retry")
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Service Summary",
                style = MaterialTheme.typography.labelMedium,
                color = SashioGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    "Total Requests",
                    allRequestCount.toString(),
                    Icons.Default.ReceiptLong,
                    Modifier.weight(1f),
                    SashioBlue
                )
                StatCard(
                    "Pending Quotes",
                    pendingQuotes.toString(),
                    Icons.Default.HourglassEmpty,
                    Modifier.weight(1f),
                    SashioGold
                )

                StatCard(
                    "Active Quotes",
                    activeQuotes.toString(),
                    Icons.Default.Description,
                    Modifier.weight(1f),
                    SashioBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    "Active Bookings",
                    activeBookings.toString(),
                    Icons.Default.DirectionsRun,
                    Modifier.weight(1f),
                    SashioTeal
                )

                StatCard(
                    "Completed",
                    completedServices.toString(),
                    Icons.Default.CheckCircle,
                    Modifier.weight(1f),
                    SashioGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SashioGreen.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Payments", style = MaterialTheme.typography.labelSmall, color = SashioGreen)
                        Text("₦%,.2f".format(totalPaid), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    }
                    Icon(Icons.Default.Payments, contentDescription = null, tint = SashioGreen, modifier = Modifier.size(30.dp))
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SashioRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Outstanding Payments",
                            style = MaterialTheme.typography.labelSmall,
                            color = SashioRed
                        )

                        Text(
                            "₦%,.2f".format(outstandingPayments),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }

                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = SashioRed,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        item {
            Text(
                "Recent Activities & Notifications",
                style = MaterialTheme.typography.labelMedium,
                color = SashioGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (liveBookings.isEmpty()) {
    item {
        Text(
            "No recent activities or notifications found.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
} else {
    items(liveBookings.take(10)) { booking ->
        Card(
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            SashioGold.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = SashioGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Quotation for ${booking.service_type}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )

                    Text(
                        when (booking.status) {
                            "Quotation Requested",
                            "Under Review",
                            "Quotation Generated" ->
                                "Your quotation is pending admin review."

                            "Quotation Sent" ->
                                "Your quotation is ready for your review."

                            "Payment Confirmed" ->
                                "Your payment has been confirmed."

                            "IN TRANSIT" ->
                                "Your service is on the way."

                            "Completed" ->
                                "Your service has been completed."

                            else ->
                                "Status updated to: ${booking.status}"
                        },
                        fontSize = 10.sp,
                        color = colors.onSurfaceVariant
                    )
                }

             BookingStatusBadge(status = booking.status)
            }
        }
    }
}
}
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: Color = SashioTeal
) {
    val colors = MaterialTheme.colorScheme

    Card(
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onSurface
            )

            Text(
                label,
                fontSize = 11.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormSheet(
    viewModel: SashioViewModel,
    initialCategory: String,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val favorites by viewModel.clientFavoriteLocations.collectAsStateWithLifecycle()
    val suggestions by viewModel.addressSuggestions.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var serviceMode by remember { mutableStateOf("Rental") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedVariant by remember { mutableStateOf("VIP Luxury Cabin") }
    var selectedModel by remember { mutableStateOf("S-800 Elite") }
    var quantity by remember { mutableStateOf("1") }
    var duration by remember { mutableStateOf("1 Day") }

    var addressQuery by remember { mutableStateOf("") }
    var selectedAddressText by remember { mutableStateOf("") }
    var currentCoords by remember {
        mutableStateOf(Pair(6.4351, 3.4219))
    }
    // Coordinates must be explicitly confirmed for each new address.
    // Never submit the default Lagos map center as if it were the client's venue.
    var locationPinConfirmed by remember { mutableStateOf(false) }
    var showFullMapPicker by remember { mutableStateOf(false) }

    var eventType by remember { mutableStateOf("Wedding") }
    var guestCount by remember { mutableStateOf("100") }
    var eventDate by remember { mutableStateOf("") }

    var cleaningSupport by remember { mutableStateOf(false) }
    var onSiteAttendant by remember { mutableStateOf(false) }
    var wasteEvacuation by remember { mutableStateOf(false) }
    var maintenanceSupport by remember { mutableStateOf(false) }

    var specialInstructions by remember { mutableStateOf("") }
    var expCategory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(colors.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "DETAILED QUOTATION REQUEST",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close sheet",
                    tint = colors.onSurface
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 14.dp),
            color = colors.outlineVariant
        )

        Text(
            "1. Service Intent",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = SashioGold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Rental", "Purchase").forEach { mode ->
                Button(
                    onClick = { serviceMode = mode },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (serviceMode == mode) {
                            SashioGold
                        } else {
                            colors.surfaceVariant
                        },
                        contentColor = if (serviceMode == mode) {
                            SashioSlate
                        } else {
                            colors.onSurfaceVariant
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Text(
                        mode,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            "2. Select Catalog Range",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = SashioGold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box {
            OutlinedButton(
                onClick = { expCategory = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    selectedCategory,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            DropdownMenu(
                expanded = expCategory,
                onDismissRequest = { expCategory = false }
            ) {
                listOf(
                    "Standard Portable Toilets",
                    "Executive Portable Toilets",
                    "VIP Portable Toilets",
                    "Presidential Luxury Mobile Restrooms",
                    "Mobile Shower Units",
                    "Accessible Toilets",
                    "Event Sanitation Packages",
                    "Corporate Site Sanitation Solutions",
                    "Waste Evacuation Services",
                    "Cleaning & Maintenance Services",
                    "Sachio Clean and Detergents",
                    "Sachio Clothing"
                ).forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            selectedCategory = cat
                            expCategory = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = selectedVariant,
            onValueChange = { selectedVariant = it },
            label = { Text("Product Variant") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = selectedModel,
            onValueChange = { selectedModel = it },
            label = { Text("Model Number / Code") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Items Quantity Required") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Event Duration") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "3. Delivery Address & Map Pin Drop",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = SashioGold
        )

        Text(
            "Type location or tap anywhere on the map to pin your exact event venue.",
            fontSize = 10.sp,
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = if (selectedAddressText.isNotBlank()) {
                selectedAddressText
            } else {
                addressQuery
            },
            onValueChange = {
                selectedAddressText = ""
                addressQuery = it
                locationPinConfirmed = false
                viewModel.updateAddressSearch(it)
            },
            label = { Text("Search Street / Site Location...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = SashioGold
                )
            },
            trailingIcon = {
                if (selectedAddressText.isNotBlank() || addressQuery.isNotBlank()) {
                    IconButton(
                        onClick = {
                            selectedAddressText = ""
                            addressQuery = ""
                            locationPinConfirmed = false
                        }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (suggestions.isNotEmpty() && selectedAddressText.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .border(
                        1.dp,
                        colors.outlineVariant,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                LazyColumn(modifier = Modifier.padding(6.dp)) {
                    items(suggestions) { place ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAddressText = place.address
                                    addressQuery = place.address
                                    currentCoords = Pair(place.lat, place.lng)
                                    locationPinConfirmed = true
                                    focusManager.clearFocus()

                                    Toast.makeText(
                                        context,
                                        "Location pinned on map!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SashioGold,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                place.address,
                                fontSize = 11.sp,
                                color = colors.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        val metrics = viewModel.calculateLogisticsMetrics(
            currentCoords.first,
            currentCoords.second
        )

        if (favorites.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Select Saved Favorite Location:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(favorites) { fav ->
                    AssistChip(
                        onClick = {
                            selectedAddressText = fav.address
                            addressQuery = fav.address
                            currentCoords = Pair(
                                fav.latitude,
                                fav.longitude
                            )
                            locationPinConfirmed = true

                            Toast.makeText(
                                context,
                                "Loaded: ${fav.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        label = {
                            Text(
                                "${fav.locationType.uppercase()}: ${fav.name}",
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = SashioGold
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    1.dp,
                    colors.outlineVariant,
                    RoundedCornerShape(12.dp)
                )
        ) {
            SashioCanvasMap(
                latitude = currentCoords.first,
                longitude = currentCoords.second,
                onMarkerChanged = { newLat, newLng ->
                    currentCoords = Pair(newLat, newLng)
                    locationPinConfirmed = true

                    viewModel.reverseGeocodeLocation(
                        newLat,
                        newLng
                    ) { reverseAddr ->
                        selectedAddressText = reverseAddr
                        addressQuery = reverseAddr
                    }
                },
                mapStyle = "Standard",
                label = "Pinned Event Location"
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.75f)
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            ) {
                Text(
                    "GPS: %.4f, %.4f".format(
                        currentCoords.first,
                        currentCoords.second
                    ),
                    color = Color.White,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { showFullMapPicker = true },
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
            Icon(Icons.Default.Map, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("OPEN LARGE MAP TO PIN EXACT LOCATION", fontWeight = FontWeight.Bold)
        }

        Text(
            if (locationPinConfirmed) {
                "PIN CONFIRMED • The staff navigation will use this exact map point."
            } else {
                "PIN NOT CONFIRMED • Select a search result, choose a saved location, or tap the map at the exact venue before submitting."
            },
            color = if (locationPinConfirmed) SashioGreen else Color(0xFFB45309),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val finalAddr =
                        if (selectedAddressText.isNotBlank()) {
                            selectedAddressText
                        } else {
                            addressQuery
                        }

                    if (finalAddr.isNotBlank()) {
                        viewModel.saveFavoriteLocation(
                            name = "Event Location",
                            address = finalAddr,
                            lat = currentCoords.first,
                            lng = currentCoords.second,
                            type = "Venue"
                        )

                        Toast.makeText(
                            context,
                            "Saved to favorites!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Please set or pick an address first!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SashioTeal,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    "Save Favorite",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    "AUTOMATIC LOGISTICS MATRIX",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Logistics Zone: ${metrics.logisticsZone}",
                    fontSize = 10.sp,
                    color = colors.onSurfaceVariant
                )

                Text(
                    "Distance from Depot: %.2f km | Travel Time: ${metrics.travelTimeMinutes} mins"
                        .format(metrics.distanceKm),
                    fontSize = 10.sp,
                    color = colors.onSurfaceVariant
                )

                Text(
                    "Recommended Transport Fee: ₦%,.2f"
                        .format(metrics.recommendedLogisticsCost),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SashioGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            "4. Gathering & Event Specs",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = SashioGold
        )

        OutlinedTextField(
            value = eventType,
            onValueChange = { eventType = it },
            label = { Text("Event Type (e.g., Wedding, Banquet)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = guestCount,
            onValueChange = { guestCount = it },
            label = { Text("Projected Guest Capacity") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = eventDate,
            onValueChange = { eventDate = it },
            label = { Text("Target Event Date") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "5. Operations & Inclusions",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = SashioGold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    cleaningSupport = !cleaningSupport
                }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = cleaningSupport,
                onCheckedChange = { cleaningSupport = it }
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                "Onsite Cleaning Support Services",
                fontSize = 12.sp,
                color = colors.onSurface
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onSiteAttendant = !onSiteAttendant
                }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = onSiteAttendant,
                onCheckedChange = { onSiteAttendant = it }
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                "Standby Servicing Uniformed Attendant",
                fontSize = 12.sp,
                color = colors.onSurface
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    wasteEvacuation = !wasteEvacuation
                }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = wasteEvacuation,
                onCheckedChange = { wasteEvacuation = it }
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                "Scheduled Waste Evacuation Services",
                fontSize = 12.sp,
                color = colors.onSurface
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    maintenanceSupport = !maintenanceSupport
                }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = maintenanceSupport,
                onCheckedChange = { maintenanceSupport = it }
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                "Active Standby Plumber / Mechanical Support",
                fontSize = 12.sp,
                color = colors.onSurface
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = specialInstructions,
            onValueChange = { specialInstructions = it },
            label = {
                Text("Specific Instructions (Fragrance, hardware notes...)")
            },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val finalAddress =
                    if (selectedAddressText.isNotEmpty()) {
                        selectedAddressText
                    } else {
                        addressQuery
                    }

                val user = currentUser

                if (!locationPinConfirmed) {
                    Toast.makeText(
                        context,
                        "Please confirm the exact venue pin on the map before submitting.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (finalAddress.isNotEmpty() && user != null) {
                    val booking = Booking(
                        clientId = user.id,
                        clientName = user.name,
                        clientPhone = user.phoneNumber,
                        clientEmail = user.email,
                        companyName = user.companyName,
                        serviceMode = serviceMode,
                        serviceType = selectedCategory,
                        variantName = selectedVariant,
                        modelNumber = selectedModel,
                        quantity = quantity.toIntOrNull() ?: 1,
                        duration = duration,
                        address = finalAddress,
                        latitude = currentCoords.first,
                        longitude = currentCoords.second,
                        eventType = eventType,
                        guestCount = guestCount.toIntOrNull() ?: 100,
                        eventDate = eventDate,
                        cleaningSupport = cleaningSupport,
                        onSiteAttendant = onSiteAttendant,
                        wasteEvacuation = wasteEvacuation,
                        maintenanceSupport = maintenanceSupport,
                        specialInstructions = specialInstructions,
                        status = "Quotation Requested"
                    )

                    viewModel.submitQuotationRequest(booking) {
                        onDismiss()
                    }
                } else if (user == null) {
                    Toast.makeText(
                        context,
                        "Please sign in to submit a quote request.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = SashioGold,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("service_booking_submit"),
            enabled = (
                addressQuery.isNotEmpty() ||
                    selectedAddressText.isNotEmpty()
                ) && currentUser != null && locationPinConfirmed
        ) {
            Text(
                "TRANSMIT QUOTE REQUEST",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

    if (showFullMapPicker) {
        ClientFullMapPicker(
            initialLatitude = currentCoords.first,
            initialLongitude = currentCoords.second,
            onDismiss = { showFullMapPicker = false },
            onConfirm = { lat, lng ->
                currentCoords = Pair(lat, lng)
                locationPinConfirmed = true
                viewModel.reverseGeocodeLocation(lat, lng) { address ->
                    selectedAddressText = address
                    addressQuery = address
                }
                showFullMapPicker = false
                Toast.makeText(context, "Exact venue pin saved.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
}

@Composable
private fun ClientFullMapPicker(
    initialLatitude: Double,
    initialLongitude: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var pin by remember { mutableStateOf(Pair(initialLatitude, initialLongitude)) }
    var locating by remember { mutableStateOf(false) }

    fun fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Allow location access to find your current position.", Toast.LENGTH_LONG).show()
            return
        }
        locating = true
        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                locating = false
                if (location != null) {
                    pin = Pair(location.latitude, location.longitude)
                } else {
                    Toast.makeText(context, "Could not get GPS fix. Move outdoors and try again.", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                locating = false
                Toast.makeText(context, it.localizedMessage ?: "Unable to get GPS location.", Toast.LENGTH_LONG).show()
            }
        } catch (e: SecurityException) {
            locating = false
            Toast.makeText(context, "Location permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) fetchCurrentLocation()
        else Toast.makeText(context, "Location permission was not granted.", Toast.LENGTH_LONG).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.fillMaxSize().padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Pin your exact venue", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Move the map, then tap the exact entrance or venue.", fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close map")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (fine || coarse) fetchCurrentLocation()
                        else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    },
                    enabled = !locating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (locating) "FINDING YOUR GPS LOCATION…" else "USE MY CURRENT GPS LOCATION")
                }
                Text(
                    "You can also drag the map to the venue and tap the exact point. The pin coordinates are shown below.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                ) {
                    SashioCanvasMap(
                        latitude = pin.first,
                        longitude = pin.second,
                        onMarkerChanged = { lat, lng -> pin = Pair(lat, lng) },
                        label = "Tap exact venue location"
                    )
                }
                Text(
                    "PIN: %.6f, %.6f".format(pin.first, pin.second),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                Button(
                    onClick = { onConfirm(pin.first, pin.second) },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("CONFIRM THIS VENUE PIN", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
            }
        }
    }
}
