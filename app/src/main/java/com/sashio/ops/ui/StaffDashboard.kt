package com.sashio.ops.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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

fun launchExternalGpsNavigation(context: Context, latitude: Double, longitude: Double, label: String) {
    try {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }
}

@Composable
fun StaffDashboard(viewModel: SashioViewModel, staff: UserProfile?) {
    var activeStaffTab by remember { mutableStateOf("dashboard") }
    var isSettingsOpen by remember { mutableStateOf(false) }

    val clockedIn by viewModel.isClockedIn.collectAsStateWithLifecycle()
    var workOfflineToggle by remember { mutableStateOf(false) }

    var showClockInDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadMyAssignedBookings()
        viewModel.loadGeneralChats()
    }

    Scaffold(
        topBar = {
            DashboardHeader(
                roleName = if (clockedIn) "Attendant Portal [ON SHIFT]" else "Attendant Portal [OFF-DUTY]",
                user = staff,
                onSettingsClick = { isSettingsOpen = true },
                onLogout = { viewModel.logout() }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SashioSlate, tonalElevation = 8.dp, modifier = Modifier.height(72.dp)) {
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
                            if (tabId == "jobs" || tabId == "dashboard") {
                                viewModel.loadMyAssignedBookings()
                            } else if (tabId == "messages") {
                                viewModel.loadGeneralChats()
                            }
                        },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) SashioGold else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) SashioGold else Color.White.copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White.copy(alpha = 0.08f))
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isSettingsOpen) {
                SashioSettingsScreen(viewModel = viewModel, onDismiss = { isSettingsOpen = false })
            } else {
                when (activeStaffTab) {
                    "dashboard" -> StaffDashboardHomeTab(
                        viewModel = viewModel,
                        staff = staff,
                        clockedIn = clockedIn,
                        workOfflineToggle = workOfflineToggle,
                        onClockInClick = { showClockInDialog = true },
                        onSosClick = { showEmergencyDialog = true },
                        onTabNavigate = { activeStaffTab = it }
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
                        onWorkOfflineChange = { workOfflineToggle = it },
                        onAddExpenseClick = { showAddExpenseDialog = true }
                    )
                }
            }
        }
    }

    if (showClockInDialog) {
        StaffClockInDialog(
            viewModel = viewModel,
            onDismiss = { showClockInDialog = false }
        )
    }

    if (showEmergencyDialog) {
        StaffEmergencyDialog(onDismiss = { showEmergencyDialog = false })
    }

    if (showAddExpenseDialog) {
        StaffAddExpenseDialog(
            viewModel = viewModel,
            onDismiss = { showAddExpenseDialog = false }
        )
    }
}

@Composable
fun StaffDashboardMapsTab(viewModel: SashioViewModel) {
    val localBookings by viewModel.staffBookings.collectAsStateWithLifecycle()
    val assignedSupabaseBookings by viewModel.myAssignedBookings.collectAsStateWithLifecycle()
    val localCtx = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadMyAssignedBookings()
    }

    val activeJob = assignedSupabaseBookings.firstOrNull { 
        it.status == "IN TRANSIT" || it.status == "Staff Assigned" || it.status == "ARRIVED" || it.status == "SETUP IN PROGRESS" 
    } ?: assignedSupabaseBookings.firstOrNull() ?: localBookings.firstOrNull { it.status == "IN TRANSIT" || it.status == "ARRIVED" }

    if (activeJob == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocationOff, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No active dispatch job assigned to your profile.", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            }
        }
    } else {
        val destLat = when (activeJob) {
            is SupabaseBooking -> if (activeJob.latitude != 0.0) activeJob.latitude else 6.4531
            is Booking -> if (activeJob.latitude != 0.0) activeJob.latitude else 6.4531
            else -> 6.4531
        }
        val destLng = when (activeJob) {
            is SupabaseBooking -> if (activeJob.longitude != 0.0) activeJob.longitude else 3.3958
            is Booking -> if (activeJob.longitude != 0.0) activeJob.longitude else 3.3958
            else -> 3.3958
        }
        val address = when (activeJob) {
            is SupabaseBooking -> activeJob.address.ifBlank { "Client Venue Location" }
            is Booking -> activeJob.address.ifBlank { "Client Venue Location" }
            else -> "Client Venue Location"
        }
        val status = when (activeJob) {
            is SupabaseBooking -> activeJob.status
            is Booking -> activeJob.status
            else -> "Staff Assigned"
        }

        val vehicleLat = when (status) {
            "Staff Assigned" -> 6.4282
            "IN TRANSIT" -> destLat - 0.015
            else -> destLat
        }
        val vehicleLng = when (status) {
            "Staff Assigned" -> 3.4211
            "IN TRANSIT" -> destLng - 0.012
            else -> destLng
        }

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
            SashioCanvasMap(
                latitude = destLat,
                longitude = destLng,
                staffLat = vehicleLat,
                staffLng = vehicleLng,
                vehicleLat = vehicleLat,
                vehicleLng = vehicleLng,
                showRoute = true,
                label = "Event Venue: $address"
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xF2111827)),
                border = BorderStroke(1.dp, SashioGold.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("LIVE DISPATCH ROUTE NAVIGATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioGold)
                        BookingStatusBadge(status = status)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Destination: $address", fontWeight = FontWeight.Medium, color = Color.White, fontSize = 13.sp)
                    if (activeJob is SupabaseBooking) {
                        Text("Client: ${activeJob.client_name} (${activeJob.client_phone})", color = Color.LightGray, fontSize = 11.sp)
                    }
                    Text("Rig Status: Coupled & Secured", color = SashioTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { 
                                launchExternalGpsNavigation(localCtx, destLat, destLng, address)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioTeal),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("NAVIGATE (GPS)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (activeJob is SupabaseBooking) {
                                    val nextStatus = when (status) {
                                        "Staff Assigned" -> "IN TRANSIT"
                                        "IN TRANSIT" -> "ARRIVED"
                                        "ARRIVED" -> "SETUP IN PROGRESS"
                                        else -> "SERVICE DELIVERED"
                                    }
                                    viewModel.staffUpdateBookingStatus(activeJob.id, nextStatus) {
                                        Toast.makeText(localCtx, "Status updated to $nextStatus", Toast.LENGTH_SHORT).show()
                                    }
                                } else if (activeJob is Booking) {
                                    if (activeJob.status == "IN TRANSIT") viewModel.staffMarkArrived(activeJob.id) else viewModel.staffStartSetup(activeJob.id)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                            modifier = Modifier.weight(1.2f).height(36.dp)
                        ) {
                            Text("SYNC STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffDashboardMessagesTab(viewModel: SashioViewModel) {
    val generalChats by viewModel.generalChats.collectAsStateWithLifecycle()
    var messagingChannelSelect by remember { mutableStateOf("Admin Center") }
    var typedGeneralMsgText by remember { mutableStateOf("") }
    val myId = remember { viewModel.getCurrentUserId() }

    LaunchedEffect(Unit) {
        viewModel.loadGeneralChats()
    }

    val filteredChats = remember(generalChats, messagingChannelSelect, myId) {
        generalChats.filter { msg ->
            val isFromMe = msg.sender_id == myId
            when (messagingChannelSelect) {
                "Admin Center", "Field Supervisor" -> {
                    (isFromMe && msg.recipient_role == "admin") ||
                    (msg.sender_role == "admin" && (msg.recipient_role == "staff" || msg.recipient_role == "all" || msg.recipient_id == myId))
                }
                "Active Customer" -> {
                    (isFromMe && msg.recipient_role == "client") ||
                    (msg.sender_role == "client" && (msg.recipient_role == "staff" || msg.recipient_id == myId))
                }
                else -> isFromMe || msg.recipient_role == "staff" || msg.recipient_role == "all"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SashioSlate)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Admin Center", "Field Supervisor", "Active Customer").forEach { chan ->
                val isSel = messagingChannelSelect == chan
                Button(
                    onClick = { messagingChannelSelect = chan },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) SashioGold else Color.White.copy(alpha = 0.08f),
                        contentColor = if (isSel) Color.Black else Color.White
                    ),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(chan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (filteredChats.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "No messages in $messagingChannelSelect yet.",
                    color = Color.White.copy(alpha = 0.4f),
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
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        Text(
                            "${gm.sender_name} (${gm.sender_role.uppercase()})",
                            fontSize = 10.sp,
                            color = if (isMe) SashioGold else SashioTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) SashioGold.copy(alpha = 0.15f) else SashioSlate.copy(alpha = 0.4f)
                            ),
                            border = BorderStroke(1.dp, if (isMe) SashioGold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .padding(vertical = 2.dp)
                        ) {
                            Text(gm.content, modifier = Modifier.padding(10.dp), color = Color.White, fontSize = 12.sp)
                        }
                        Text(formatMessageTimestamp(gm.created_at), fontSize = 8.sp, color = Color.White.copy(alpha = 0.4f))
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = typedGeneralMsgText,
                onValueChange = { typedGeneralMsgText = it },
                placeholder = { Text("Write to $messagingChannelSelect...", fontSize = 12.sp) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SashioGold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (typedGeneralMsgText.isNotBlank()) {
                        viewModel.sendGeneralChatMessage(typedGeneralMsgText, messagingChannelSelect)
                        typedGeneralMsgText = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SashioGold)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
            }
        }
    }
}
