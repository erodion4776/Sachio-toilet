package com.sashio.ops.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.sashio.ops.location.StaffLocationService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.Booking
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.data.SupabaseBooking
import com.sashio.ops.data.UserProfile
import com.sashio.ops.ui.theme.*

// ------------------------------------------------------------
// EXTERNAL GPS NAVIGATION
// ------------------------------------------------------------

fun launchExternalGpsNavigation(
    context: Context,
    latitude: Double,
    longitude: Double,
    label: String
) {
    if (
        !latitude.isFinite() ||
        !longitude.isFinite() ||
        latitude !in -90.0..90.0 ||
        longitude !in -180.0..180.0 ||
        (latitude == 0.0 && longitude == 0.0)
    ) {
        Toast.makeText(
            context,
            "Valid destination coordinates are unavailable.",
            Toast.LENGTH_LONG
        ).show()
        return
    }

    val encodedLabel = Uri.encode(label)

    val geoUri = Uri.parse(
        "geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)"
    )

    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, geoUri)
        )
    } catch (e: Exception) {
        try {
            val browserUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
            )

            context.startActivity(
                Intent(Intent.ACTION_VIEW, browserUri)
            )
        } catch (fallbackError: Exception) {
            Toast.makeText(
                context,
                "Unable to open navigation. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

// ------------------------------------------------------------
// STAFF DASHBOARD
// ------------------------------------------------------------

@Composable
fun StaffDashboard(
    viewModel: SashioViewModel,
    staff: UserProfile?
) {
    var activeStaffTab by remember {
        mutableStateOf("dashboard")
    }

    var isSettingsOpen by remember {
        mutableStateOf(false)
    }

    val clockedIn by viewModel.isClockedIn.collectAsStateWithLifecycle()

    var workOfflineToggle by remember {
        mutableStateOf(false)
    }

    var showClockInDialog by remember {
        mutableStateOf(false)
    }

    var showEmergencyDialog by remember {
        mutableStateOf(false)
    }

    var showAddExpenseDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        viewModel.loadMyAssignedBookings()
        viewModel.loadGeneralChats()
    }

    Scaffold(
        topBar = {
            DashboardHeader(
                roleName = if (clockedIn) {
                    "Attendant Portal [ON SHIFT]"
                } else {
                    "Attendant Portal [OFF-DUTY]"
                },
                user = staff,
                onSettingsClick = {
                    isSettingsOpen = true
                },
                onLogout = {
                    viewModel.logout()
                }
            )
        },

        bottomBar = {
            NavigationBar(
                containerColor = SashioSlate,
                tonalElevation = 8.dp,
                modifier = Modifier.height(72.dp)
            ) {
                listOf(
                    Triple("dashboard", Icons.Default.Home, "Dashboard"),
                    Triple("jobs", Icons.Default.Assignment, "My Jobs"),
                    Triple("maps", Icons.Default.Map, "Live Route"),
                    Triple("messages", Icons.Default.Chat, "Team Desk"),
                    Triple("profile", Icons.Default.Person, "My Profile")
                ).forEach { (tabId, icon, label) ->

                    val isSelected = activeStaffTab == tabId

                    NavigationBarItem(
                        selected = isSelected,

                        onClick = {
                            activeStaffTab = tabId

                            when (tabId) {
                                "jobs", "dashboard" -> {
                                    viewModel.loadMyAssignedBookings()
                                }

                                "messages" -> {
                                    viewModel.loadGeneralChats()
                                }
                            }
                        },

                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) {
                                    SashioGold
                                } else {
                                    Color.White.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        },

                        label = {
                            Text(
                                label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) {
                                    SashioGold
                                } else {
                                    Color.White.copy(alpha = 0.5f)
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },

                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.White.copy(alpha = 0.08f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (isSettingsOpen) {

                SashioSettingsScreen(
                    viewModel = viewModel,
                    onDismiss = {
                        isSettingsOpen = false
                    }
                )

            } else {

                when (activeStaffTab) {

                    "dashboard" -> StaffDashboardHomeTab(
                        viewModel = viewModel,
                        staff = staff,
                        clockedIn = clockedIn,
                        workOfflineToggle = workOfflineToggle,
                        onClockInClick = {
                            showClockInDialog = true
                        },
                        onSosClick = {
                            showEmergencyDialog = true
                        },
                        onTabNavigate = {
                            activeStaffTab = it
                        }
                    )

                    "jobs" -> StaffDashboardJobsTab(
                        viewModel = viewModel,
                        workOfflineToggle = workOfflineToggle
                    )

                    "maps" -> StaffDashboardMapsTab(viewModel)

                    "messages" -> StaffDashboardMessagesTab(viewModel)

                    "profile" -> StaffDashboardProfileTab(
                        viewModel = viewModel,
                        staff = staff,
                        workOfflineToggle = workOfflineToggle,
                        onWorkOfflineChange = {
                            workOfflineToggle = it
                        },
                        onAddExpenseClick = {
                            showAddExpenseDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showClockInDialog) {
        StaffClockInDialog(
            viewModel = viewModel,
            onDismiss = {
                showClockInDialog = false
            }
        )
    }

    if (showEmergencyDialog) {
        StaffEmergencyDialog(
            onDismiss = {
                showEmergencyDialog = false
            }
        )
    }

    if (showAddExpenseDialog) {
        StaffAddExpenseDialog(
            viewModel = viewModel,
            onDismiss = {
                showAddExpenseDialog = false
            }
        )
    }
}

// ------------------------------------------------------------
// STAFF LIVE ROUTE MAP
// ------------------------------------------------------------

@Composable
fun StaffDashboardMapsTab(
    viewModel: SashioViewModel
) {
    val localBookings by viewModel.staffBookings.collectAsStateWithLifecycle()

    val assignedSupabaseBookings by viewModel.myAssignedBookings.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadMyAssignedBookings()
    }

    // Prefer the assigned Supabase job.
    val activeSupabaseJob = assignedSupabaseBookings.firstOrNull {
        it.status in listOf(
            "IN TRANSIT",
            "Staff Assigned",
            "ARRIVED",
            "SETUP IN PROGRESS"
        )
    }

    val activeLocalJob = localBookings.firstOrNull {
        it.status in listOf(
            "IN TRANSIT",
            "Staff Assigned",
            "ARRIVED",
            "SETUP IN PROGRESS"
        )
    }

    val activeJob = activeSupabaseJob ?: activeLocalJob

    if (activeJob == null) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.LocationOff,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "No active dispatch job assigned to your profile.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        return
    }

    val destLat: Double
    val destLng: Double
    val address: String
    val status: String

    when (activeJob) {

        is SupabaseBooking -> {
            destLat = activeJob.latitude
            destLng = activeJob.longitude

            address = activeJob.address.ifBlank {
                "Client Venue Location"
            }

            status = activeJob.status
        }

        is Booking -> {
            destLat = activeJob.latitude
            destLng = activeJob.longitude

            address = activeJob.address.ifBlank {
                "Client Venue Location"
            }

            status = activeJob.status
        }

        else -> {
            destLat = 0.0
            destLng = 0.0
            address = "Client Venue Location"
            status = "Staff Assigned"
        }
    }

    val hasValidCoordinates =
        destLat.isFinite() &&
        destLng.isFinite() &&
        destLat in -90.0..90.0 &&
        destLng in -180.0..180.0 &&
        !(destLat == 0.0 && destLng == 0.0)

    // Illustrative map positions only.
    // These are NOT actual staff GPS coordinates.

    val vehicleLat = if (hasValidCoordinates) {
        when (status) {
            "Staff Assigned" -> destLat
            "IN TRANSIT" -> destLat - 0.015
            else -> destLat
        }
    } else {
        0.0
    }

    val vehicleLng = if (hasValidCoordinates) {
        when (status) {
            "Staff Assigned" -> destLng
            "IN TRANSIT" -> destLng - 0.012
            else -> destLng
        }
    } else {
        0.0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {

        if (hasValidCoordinates) {

            SashioInteractiveCanvasMap(
    latitude = destLat,
    longitude = destLng,
    staffLat = destLat,
    staffLng = destLng,
    vehicleLat = destLat,
    vehicleLng = destLng,
    showRoute = false,
    label = "Event Venue: $address"
)

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = SashioGold,
                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Destination coordinates unavailable",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Please update the booking location.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xF2111827)
            ),

            border = BorderStroke(
                1.dp,
                SashioGold.copy(alpha = 0.4f)
            ),

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {

            Column(
                modifier = Modifier.padding(14.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        "LIVE DISPATCH ROUTE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SashioGold
                    )

                    BookingStatusBadge(status = status)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Destination: $address",
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    fontSize = 13.sp
                )

                if (activeJob is SupabaseBooking) {
                    Text(
                        "Client: ${activeJob.client_name} (${activeJob.client_phone})",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }

                Text(
                    if (hasValidCoordinates) {
                        "Map preview available. Vehicle position is illustrative."
                    } else {
                        "GPS navigation unavailable until coordinates are provided."
                    },
                    color = SashioTeal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = {
                            launchExternalGpsNavigation(
                                context,
                                destLat,
                                destLng,
                                address
                            )
                        },

                        enabled = hasValidCoordinates,

                        colors = ButtonDefaults.buttonColors(
                            containerColor = SashioTeal
                        ),

                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {

                        Icon(
                            Icons.Default.Directions,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            "NAVIGATE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {

                            if (activeJob is SupabaseBooking) {

                                val nextStatus = when (status) {
                                    "Staff Assigned" -> "IN TRANSIT"
                                    "IN TRANSIT" -> "ARRIVED"
                                    "ARRIVED" -> "SETUP IN PROGRESS"
                                    "SETUP IN PROGRESS" -> "SERVICE DELIVERED"
                                    else -> null
                                }

                                if (nextStatus == null) {

                                    Toast.makeText(
                                        context,
                                        "There is no next status for this job.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else {

                                    viewModel.staffUpdateBookingStatus(
                                        activeJob.id,
                                        nextStatus
                                    ) {

                                        Toast.makeText(
                                            context,
                                            "Status updated to $nextStatus",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        viewModel.loadMyAssignedBookings()
                                    }
                                }

                            } else if (activeJob is Booking) {

                                when (status) {

                                    "IN TRANSIT" -> {
                                        viewModel.staffMarkArrived(activeJob.id)
                                    }

                                    "ARRIVED" -> {
                                        viewModel.staffStartSetup(activeJob.id)
                                    }

                                    "Staff Assigned" -> {
                                        Toast.makeText(
                                            context,
                                            "Please start dispatch from My Jobs first.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    "SETUP IN PROGRESS" -> {
                                        Toast.makeText(
                                            context,
                                            "Complete the service using the job controls.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    else -> {
                                        Toast.makeText(
                                            context,
                                            "No status update is available.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        },

                        colors = ButtonDefaults.buttonColors(
                            containerColor = SashioGold
                        ),

                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp)
                    ) {

                        Text(
                            "SYNC STATUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------
// STAFF TEAM MESSAGES
// ------------------------------------------------------------

@Composable
fun StaffDashboardMessagesTab(
    viewModel: SashioViewModel
) {
    val generalChats by viewModel.generalChats.collectAsStateWithLifecycle()

    var messagingChannelSelect by remember {
        mutableStateOf("Admin Center")
    }

    var typedGeneralMsgText by remember {
        mutableStateOf("")
    }

    val myId = remember {
        viewModel.getCurrentUserId()
    }

    val colors = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        viewModel.loadGeneralChats()
    }

    val filteredChats = remember(
        generalChats,
        messagingChannelSelect,
        myId
    ) {

        generalChats.filter { msg ->

            val isFromMe = msg.sender_id == myId

            when (messagingChannelSelect) {

                "Admin Center", "Field Supervisor" -> {

                    (isFromMe && msg.recipient_role == "admin") ||
                    (
                        msg.sender_role == "admin" &&
                        (
                            msg.recipient_role == "staff" ||
                            msg.recipient_role == "all" ||
                            msg.recipient_id == myId
                        )
                    )
                }

                "Active Customer" -> {

                    (isFromMe && msg.recipient_role == "client") ||
                    (
                        msg.sender_role == "client" &&
                        (
                            msg.recipient_role == "staff" ||
                            msg.recipient_id == myId
                        )
                    )
                }

                else -> {
                    isFromMe ||
                    msg.recipient_role == "staff" ||
                    msg.recipient_role == "all"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        // MESSAGE CHANNEL SELECTOR

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SashioSlate)
                .padding(8.dp),

            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            listOf(
                "Admin Center",
                "Field Supervisor",
                "Active Customer"
            ).forEach { chan ->

                val isSel = messagingChannelSelect == chan

                Button(
                    onClick = {
                        messagingChannelSelect = chan
                    },

                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) {
                            SashioGold
                        } else {
                            Color.White.copy(alpha = 0.08f)
                        },

                        contentColor = if (isSel) {
                            Color.Black
                        } else {
                            Color.White
                        }
                    ),

                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),

                    contentPadding = PaddingValues(
                        horizontal = 4.dp,
                        vertical = 2.dp
                    )
                ) {

                    Text(
                        chan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // MESSAGE LIST

        if (filteredChats.isEmpty()) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    "No messages in $messagingChannelSelect yet.",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),

                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(filteredChats) { gm ->

                    val isMe = gm.sender_id == myId

                    Column(
                        modifier = Modifier.fillMaxWidth(),

                        horizontalAlignment = if (isMe) {
                            Alignment.End
                        } else {
                            Alignment.Start
                        }
                    ) {

                        Text(
                            "${gm.sender_name} (${gm.sender_role.uppercase()})",
                            fontSize = 10.sp,
                            color = if (isMe) {
                                SashioGold
                            } else {
                                SashioTeal
                            },
                            fontWeight = FontWeight.Bold
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) {
                                    SashioGold.copy(alpha = 0.15f)
                                } else {
                                    colors.surfaceVariant
                                }
                            ),

                            border = BorderStroke(
                                1.dp,
                                if (isMe) {
                                    SashioGold.copy(alpha = 0.4f)
                                } else {
                                    colors.outline.copy(alpha = 0.3f)
                                }
                            ),

                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .padding(vertical = 2.dp)
                        ) {

                            Text(
                                gm.content,
                                modifier = Modifier.padding(10.dp),
                                color = colors.onSurface,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            formatMessageTimestamp(gm.created_at),
                            fontSize = 8.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // MESSAGE INPUT

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = typedGeneralMsgText,

                onValueChange = {
                    typedGeneralMsgText = it
                },

                placeholder = {
                    Text(
                        "Write to $messagingChannelSelect...",
                        fontSize = 12.sp
                    )
                },

                singleLine = true,

                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface,

                    focusedBorderColor = SashioGold,
                    unfocusedBorderColor = colors.outline,

                    focusedPlaceholderColor = colors.onSurfaceVariant,
                    unfocusedPlaceholderColor = colors.onSurfaceVariant,

                    cursorColor = SashioGold
                ),

                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {

                    if (typedGeneralMsgText.isNotBlank()) {

                        viewModel.sendGeneralChatMessage(
                            typedGeneralMsgText.trim(),
                            messagingChannelSelect
                        )

                        typedGeneralMsgText = ""
                    }
                },

                modifier = Modifier
                    .clip(CircleShape)
                    .background(SashioGold)
            ) {

                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.Black
                )
            }
        }
    }
}
