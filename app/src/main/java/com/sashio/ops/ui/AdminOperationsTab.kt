package com.sashio.ops.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.Booking
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.data.SupabaseAttendance
import com.sashio.ops.data.SupabaseBooking
import com.sashio.ops.data.UserProfile
import com.sashio.ops.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOperationsTab(
    viewModel: SashioViewModel,
    onOpenIncidentLog: () -> Unit
) {
    val supabaseBookings by viewModel.supabaseBookings.collectAsStateWithLifecycle()
    val realStaffProfiles by viewModel.realStaffProfiles.collectAsStateWithLifecycle()
    val incidents by viewModel.incidents.collectAsStateWithLifecycle()
    val localContext = LocalContext.current
    var staffDropdownBookingId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSupabaseBookings()
        viewModel.loadSupabaseProfiles()
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FIELD OPERATIONS CONTROL CENTER", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = onOpenIncidentLog,
                colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Incident", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Text("Active dispatches, crew allocations, and field incident reporting.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
        Spacer(modifier = Modifier.height(10.dp))

        var opsFilterSelected by remember { mutableStateOf("Dispatches") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Dispatches", "Incident Logs").forEach { t ->
                FilterChip(
                    selected = opsFilterSelected == t,
                    onClick = { opsFilterSelected = t },
                    label = { Text(t) }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (opsFilterSelected == "Dispatches") {
            if (supabaseBookings.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No bookings in database.", color = SashioTextMuted, fontSize = 12.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(supabaseBookings) { b ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(b.service_type, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Client: ${b.client_name} | Event: ${b.event_type}", fontSize = 10.sp, color = SashioTextMuted)
                                    }
                                    BookingStatusBadge(status = b.status)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (b.assigned_staff_name != null) "Assigned Crew: ${b.assigned_staff_name}" else "Crew Allocation Required",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (b.assigned_staff_name != null) SashioTeal else SashioGold
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        Button(
                                            onClick = {
                                                viewModel.loadSupabaseProfiles()
                                                staffDropdownBookingId = b.id
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(30.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(if (b.assigned_staff_name != null) "Reassign Crew" else "Allocate Crew", fontSize = 10.sp, color = Color.White)
                                        }
                                        DropdownMenu(
                                            expanded = staffDropdownBookingId == b.id,
                                            onDismissRequest = { staffDropdownBookingId = null }
                                        ) {
                                            if (realStaffProfiles.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { Text("No staff available. Set roles in User Roles first.") },
                                                    onClick = { staffDropdownBookingId = null },
                                                    enabled = false
                                                )
                                            } else {
                                                realStaffProfiles.forEach { s ->
                                                    DropdownMenuItem(
                                                        text = { Text(s.full_name?.takeIf { it.isNotBlank() } ?: s.email) },
                                                        onClick = {
                                                            val name = s.full_name?.takeIf { it.isNotBlank() } ?: s.email
                                                            viewModel.adminAssignStaffToSupabaseBooking(b.id, s.id, name) { success ->
                                                                if (success) {
                                                                    Toast.makeText(localContext, "Assigned $name to job!", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                            staffDropdownBookingId = null
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.staffUpdateBookingStatus(b.id, "SERVICE DELIVERED") {
                                                Toast.makeText(localContext, "Completed dispatch #QR-${b.id.take(8)}.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SashioGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(30.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Complete Job", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (incidents.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No operational incidents logged.", color = SashioTextMuted, fontSize = 12.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(incidents) { inc ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (inc.priority == "Critical") Color(0xFFFEE2E2) else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(inc.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Reporter: ${inc.reporter} | Date: ${inc.date}", fontSize = 9.sp, color = SashioTextMuted)
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (inc.priority == "Critical") SashioRed else if (inc.priority == "High") SashioGold else SashioSlate
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            inc.priority,
                                            fontSize = 8.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(inc.details, fontSize = 10.sp, color = SashioSlate)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "STATUS: ${inc.status.uppercase()}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (inc.status == "Resolved") SashioGreen else SashioGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTrackingTab(viewModel: SashioViewModel) {
    val supabaseBookings by viewModel.supabaseBookings.collectAsStateWithLifecycle()
    val bookingLiveLocations by viewModel.bookingLiveLocations.collectAsStateWithLifecycle()
    val supabaseAttendance by viewModel.supabaseAttendance.collectAsStateWithLifecycle()
    val localContext = LocalContext.current

    var selectedTrackBookingId by remember { mutableStateOf<String?>(null) }
    var currentMapLayer by remember { mutableStateOf("Standard") }
    var trackSelectedTab by remember { mutableStateOf("Live Map") }

    LaunchedEffect(Unit) {
        viewModel.loadSupabaseBookings()
        viewModel.loadSupabaseAttendance()
        viewModel.loadSupabaseProfiles()
        while (true) {
            kotlinx.coroutines.delay(15_000)
            viewModel.loadSupabaseBookings()
        }
    }

    val activeBookings = remember(supabaseBookings) {
        supabaseBookings.filter { it.latitude != 0.0 && it.longitude != 0.0 }
    }
    
    val focusBooking = remember(activeBookings, selectedTrackBookingId) {
        activeBookings.find { it.id == selectedTrackBookingId } ?: activeBookings.firstOrNull()
    }

    LaunchedEffect(focusBooking?.id) {
        val bookingId = focusBooking?.id ?: return@LaunchedEffect
        while (true) {
            viewModel.loadBookingLiveLocation(bookingId)
            kotlinx.coroutines.delay(8_000)
        }
    }

    val focusLiveLocation = focusBooking?.let { bookingLiveLocations[it.id] }
    val hasLiveStaffGps = focusLiveLocation != null &&
        isFreshAdminGps(focusLiveLocation.updated_at) &&
        focusLiveLocation.latitude.isFinite() &&
        focusLiveLocation.longitude.isFinite() &&
        focusLiveLocation.latitude in -90.0..90.0 &&
        focusLiveLocation.longitude in -180.0..180.0 &&
        (focusLiveLocation.latitude != 0.0 || focusLiveLocation.longitude != 0.0)

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("LIVE ROUTE CONTROL & ATTENDANCE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            listOf("Live Map", "Attendance Logs").forEach { tTab ->
                FilterChip(
                    selected = trackSelectedTab == tTab,
                    onClick = { 
                        trackSelectedTab = tTab 
                        if (tTab == "Attendance Logs") {
                            viewModel.loadSupabaseAttendance()
                        } else {
                            viewModel.loadSupabaseBookings()
                        }
                    },
                    label = { Text(tTab) }
                )
            }
        }

        if (trackSelectedTab == "Live Map") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Map Layer:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                listOf("Standard", "Traffic", "Satellite", "Operations").forEach { lStyle ->
                    FilterChip(
                        selected = currentMapLayer == lStyle,
                        onClick = { currentMapLayer = lStyle },
                        label = { Text(lStyle, fontSize = 9.sp) }
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.5f).fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (focusBooking != null) {
                        val targetLat = focusBooking.latitude
                        val targetLng = focusBooking.longitude

                        SashioInteractiveCanvasMap(
                            latitude = targetLat,
                            longitude = targetLng,
                            staffLat = if (hasLiveStaffGps) focusLiveLocation!!.latitude else 0.0,
                            staffLng = if (hasLiveStaffGps) focusLiveLocation!!.longitude else 0.0,
                            vehicleLat = 0.0,
                            vehicleLng = 0.0,
                            mapStyle = currentMapLayer,
                            showRoute = hasLiveStaffGps,
                            label = "${focusBooking.client_name} (${focusBooking.service_type})"
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Dispatch: #QR-${focusBooking.id.take(8)}", color = SashioGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Client: ${focusBooking.client_name} | ${focusBooking.event_type}", color = Color.White, fontSize = 9.sp)
                                Text("Address: ${focusBooking.address}", color = Color.LightGray, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = if (focusBooking.assigned_staff_name != null) "Crew: ${focusBooking.assigned_staff_name}" else "Crew: Unassigned",
                                    color = if (focusBooking.assigned_staff_name != null) SashioTeal else SashioGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Status: ${focusBooking.status}", color = SashioGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    when {
                                        hasLiveStaffGps -> "LIVE GPS • ${focusLiveLocation?.updated_at}"
                                        focusBooking.assigned_staff_id == null -> "GPS unavailable • Awaiting staff assignment"
                                        else -> "GPS unavailable • Waiting for staff to start sharing"
                                    },
                                    color = if (hasLiveStaffGps) SashioTeal else SashioGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        launchExternalGpsNavigation(localContext, targetLat, targetLng, focusBooking.address)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SashioTeal),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().height(28.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("OPEN IN GOOGLE MAPS / WAZE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    } else {
                        SashioCanvasMap(
                            latitude = 6.4282, 
                            longitude = 3.4211, 
                            mapStyle = currentMapLayer, 
                            label = "SACHIO Victoria Island Central Depot"
                        )
                        Box(modifier = Modifier.align(Alignment.Center).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                            Text("No active event bookings to track. Create or assign jobs to view live routes.", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }

            if (activeBookings.isNotEmpty()) {
                Text("Select destination to focus radar:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeBookings) { b ->
                        val isFocused = focusBooking?.id == b.id
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isFocused) SashioSlate else Color.White),
                            border = BorderStroke(1.dp, if (isFocused) SashioGold else Color.LightGray.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.width(160.dp).clickable { selectedTrackBookingId = b.id }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    b.client_name, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 10.sp, 
                                    color = if (isFocused) Color.White else SashioSlate, 
                                    maxLines = 1, 
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${b.service_type} - ${b.status}", 
                                    fontSize = 8.sp, 
                                    color = if (isFocused) SashioGold else SashioTextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Text("GEO-FENCED ATTENDANCE LOGS (LIVE)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text("Verify clock-in durations, timestamps, and reporting depots of field personnel.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
            Spacer(modifier = Modifier.height(10.dp))

            if (supabaseAttendance.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("No staff attendance records logged in database yet.", color = SashioTextMuted, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(supabaseAttendance) { att ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(10.dp)) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(att.staff_name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioSlate)
                                    val clockInFormatted = formatMessageTimestamp(att.clock_in_time)
                                    val clockOutFormatted = if (!att.clock_out_time.isNullOrBlank()) formatMessageTimestamp(att.clock_out_time) else "On Duty (Active)"
                                    Text("In: $clockInFormatted | Out: $clockOutFormatted", fontSize = 10.sp, color = SashioTextMuted)
                                    Text("Reporting Depot: ${att.location_verified ?: "Central Yard"}", fontSize = 9.sp, color = SashioTeal, fontWeight = FontWeight.SemiBold)
                                }
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (att.is_verified) SashioGreen.copy(alpha = 0.12f) else SashioGold.copy(alpha = 0.12f)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        if (att.clock_out_time.isNullOrBlank()) "ACTIVE ON SHIFT" else "COMPLETED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (att.clock_out_time.isNullOrBlank()) SashioGold else SashioGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMessagesTab(viewModel: SashioViewModel) {
    val adminChats by viewModel.generalChats.collectAsStateWithLifecycle()
    var messageTabSelection by remember { mutableStateOf("Live Chats") }
    var msgChannel by remember { mutableStateOf("Staff") }
    var msgFormText by remember { mutableStateOf("") }
    var broadcastTarget by remember { mutableStateOf("All") }
    var broadcastMessageText by remember { mutableStateOf("") }
    val localContext = LocalContext.current
    val myAdminId = remember { viewModel.getCurrentUserId() }

    LaunchedEffect(Unit) {
        viewModel.loadGeneralChats()
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("MESSAGES & PUSH BROADCASTS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            listOf("Live Chats", "Corporate Broadcaster").forEach { t ->
                FilterChip(
                    selected = messageTabSelection == t,
                    onClick = { messageTabSelection = t },
                    label = { Text(t) }
                )
            }
        }

        if (messageTabSelection == "Live Chats") {
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                listOf("Staff", "Clients").forEach { ch ->
                    FilterChip(
                        selected = msgChannel == ch,
                        onClick = { msgChannel = ch },
                        label = { Text(ch, fontSize = 10.sp) },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    val targetRole = if (msgChannel == "Clients") "client" else "staff"

                    val filteredAdminChats = adminChats.filter { msg ->
                        val isRelevantToChannel = msg.sender_role == targetRole || msg.recipient_role == targetRole
                        val isBroadcast = msg.recipient_role == "all"
                        isRelevantToChannel || (msg.sender_id == myAdminId && msg.recipient_role == targetRole) || isBroadcast
                    }

                    if (filteredAdminChats.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No messages in $msgChannel channel yet.", color = SashioTextMuted, fontSize = 11.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(filteredAdminChats) { m ->
                                val isMe = m.sender_id == myAdminId
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                                ) {
                                    Text(
                                        "${m.sender_name} (${m.sender_role.uppercase()})",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMe) SashioGold else SashioSlate
                                    )
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isMe) SashioSlate else SashioGold.copy(alpha = 0.15f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(vertical = 2.dp).widthIn(max = 280.dp)
                                    ) {
                                        Text(
                                            m.content,
                                            fontSize = 11.sp,
                                            color = if (isMe) Color.White else SashioSlate,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    Text(formatMessageTimestamp(m.created_at), fontSize = 8.sp, color = SashioTextMuted)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = msgFormText,
                            onValueChange = { msgFormText = it },
                            placeholder = { Text("Reply to $msgChannel...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (msgFormText.isNotBlank()) {
                                    viewModel.sendGeneralChatMessage(msgFormText, targetRole)
                                    msgFormText = ""
                                }
                            },
                            modifier = Modifier.background(SashioGold, CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = SashioSlate)
                        }
                    }
                }
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("BROADCAST PUSH NOTIFICATION ENGINE", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Target Loop: ", fontSize = 11.sp)
                        listOf("All", "Clients", "Staff").forEach { tgt ->
                            FilterChip(
                                selected = broadcastTarget == tgt,
                                onClick = { broadcastTarget = tgt },
                                label = { Text(tgt, fontSize = 9.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = broadcastMessageText,
                        onValueChange = { broadcastMessageText = it },
                        label = { Text("Broadcast message text") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (broadcastMessageText.isNotBlank()) {
                                val targetRole = when (broadcastTarget) {
                                    "Clients" -> "client"
                                    "Staff" -> "staff"
                                    else -> "all"
                                }
                                viewModel.sendGeneralChatMessage(broadcastMessageText, targetRole)
                                Toast.makeText(localContext, "Broadcast sent to $broadcastTarget.", Toast.LENGTH_LONG).show()
                                broadcastMessageText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DISPATCH BROADCAST NOTIFICATION", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProfileTab(viewModel: SashioViewModel, admin: UserProfile?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = SashioGold
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = admin?.name?.take(2)?.uppercase() ?: "SA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SashioSlate
                )
            }
        }

        Text(admin?.name ?: "System Administrator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("ROLE: ENTERPRISE SYSTEM COMMANDER", fontSize = 10.sp, color = SashioTeal, fontWeight = FontWeight.Bold)
        Text("Email: ${admin?.email ?: "admin@sashio.com"}", fontSize = 10.sp, color = SashioTextMuted)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("REGISTRATION & NODE ACCESS TOKENS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Master Cryptographic Keys: SHA-256 Validated", fontSize = 10.sp, color = SashioTextMuted)
                Text("Session Active: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", fontSize = 10.sp, color = SashioTextMuted)
            }
        }

        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text("SECURE COCKPIT TERMINATION / LOGOUT", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminAddStaffDialog(
    viewModel: SashioViewModel,
    onDismiss: () -> Unit
) {
    var staffFormName by remember { mutableStateOf("") }
    var staffFormEmail by remember { mutableStateOf("") }
    var staffFormPhone by remember { mutableStateOf("") }
    val localContext = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Crew Operative", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = staffFormName,
                    onValueChange = { staffFormName = it },
                    label = { Text("Operative's Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = staffFormEmail,
                    onValueChange = { staffFormEmail = it },
                    label = { Text("Email ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = staffFormPhone,
                    onValueChange = { staffFormPhone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (staffFormName.isNotBlank() && staffFormEmail.isNotBlank()) {
                        viewModel.addNewStaffProfile(
                            name = staffFormName,
                            email = staffFormEmail,
                            phone = staffFormPhone
                        )
                        Toast.makeText(localContext, "Crew profile registered successfully.", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)
            ) {
                Text("Save Crew", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLogIncidentDialog(
    viewModel: SashioViewModel,
    onDismiss: () -> Unit
) {
    var incidentFormTitle by remember { mutableStateOf("") }
    var incidentFormReporter by remember { mutableStateOf("") }
    var incidentFormPriority by remember { mutableStateOf("Medium") }
    var incidentFormDetails by remember { mutableStateOf("") }
    val localContext = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Operational Incident", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = incidentFormTitle,
                    onValueChange = { incidentFormTitle = it },
                    label = { Text("Incident Title (e.g. Pump failure)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = incidentFormReporter,
                    onValueChange = { incidentFormReporter = it },
                    label = { Text("Reporter Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Priority Level: ", fontSize = 11.sp)
                    listOf("Low", "Medium", "High", "Critical").forEach { pri ->
                        FilterChip(
                            selected = incidentFormPriority == pri,
                            onClick = { incidentFormPriority = pri },
                            label = { Text(pri, fontSize = 9.sp) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = incidentFormDetails,
                    onValueChange = { incidentFormDetails = it },
                    label = { Text("Describe specific occurrence details...") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (incidentFormTitle.isNotBlank() && incidentFormReporter.isNotBlank()) {
                        viewModel.createIncident(
                            title = incidentFormTitle,
                            reporter = incidentFormReporter,
                            priority = incidentFormPriority,
                            details = incidentFormDetails
                        )
                        Toast.makeText(localContext, "Operational incident logged.", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)
            ) {
                Text("File Log", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLogExpenseDialog(
    viewModel: SashioViewModel,
    onDismiss: () -> Unit
) {
    var expenseFormStaff by remember { mutableStateOf("") }
    var expenseFormCategory by remember { mutableStateOf("Fuel Costs") }
    var expenseFormAmount by remember { mutableStateOf("") }
    var expenseFormDetails by remember { mutableStateOf("") }
    val localContext = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Corporate Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = expenseFormStaff,
                    onValueChange = { expenseFormStaff = it },
                    label = { Text("Staff / Operative Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Category: ", fontSize = 11.sp)
                    listOf("Fuel Costs", "Maintenance Costs", "Staff Expenses").forEach { cate ->
                        FilterChip(
                            selected = expenseFormCategory == cate,
                            onClick = { expenseFormCategory = cate },
                            label = { Text(cate.take(13), fontSize = 9.sp) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = expenseFormAmount,
                    onValueChange = { expenseFormAmount = it },
                    label = { Text("Amount Cost (₦)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = expenseFormDetails,
                    onValueChange = { expenseFormDetails = it },
                    label = { Text("Explanatory description details...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = expenseFormAmount.toDoubleOrNull()
                    if (expenseFormStaff.isNotBlank() && amt != null) {
                        viewModel.submitExpenseFromAdmin(
                            staffName = expenseFormStaff,
                            category = expenseFormCategory,
                            amount = amt,
                            details = expenseFormDetails
                        ) {
                            Toast.makeText(localContext, "Corporate expense logged.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)
            ) {
                Text("Log Expense", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


private fun isFreshAdminGps(updatedAt: String): Boolean {
    val ageSeconds = runCatching {
        java.time.Duration.between(
            java.time.Instant.parse(updatedAt),
            java.time.Instant.now()
        ).seconds
    }.getOrNull() ?: return false
    return ageSeconds in -30..90
}
