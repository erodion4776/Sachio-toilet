package com.sashio.ops.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.data.UserProfile
import com.sashio.ops.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StaffDashboardHomeTab(
    viewModel: SashioViewModel,
    staff: UserProfile?,
    clockedIn: Boolean,
    workOfflineToggle: Boolean,
    onClockInClick: () -> Unit,
    onSosClick: () -> Unit,
    onTabNavigate: (String) -> Unit
) {
    val myAssignedBookings by viewModel.myAssignedBookings.collectAsStateWithLifecycle()
    var checkedBeforeDeparture by remember { 
        mutableStateOf(setOf("Vehicle fluid metrics verified", "Restroom trailer towing lock pinned", "Fresh bio-blue fluid topped up")) 
    }
    var expandedSopIndex by remember { mutableStateOf<Int?>(0) }

    LaunchedEffect(Unit) {
        viewModel.loadMyAssignedBookings()
    }

    val activeCount = myAssignedBookings.count { 
        it.status == "Staff Assigned" || it.status == "IN TRANSIT" || it.status == "ARRIVED" || it.status == "SETUP IN PROGRESS" 
    }
    val completedCount = myAssignedBookings.count { 
        it.status == "SERVICE DELIVERED" || it.status == "COMPLETED" || it.status == "Completed" 
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (workOfflineToggle) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SashioGold.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, SashioGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.OfflineBolt, contentDescription = null, tint = SashioGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Offline Workspace Active", fontWeight = FontWeight.Bold, color = SashioGold, fontSize = 12.sp)
                            Text("Submissions are cached locally until synchronisation.", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (clockedIn) SashioGreen else SashioGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (clockedIn) "ON DUTY ACTIVE SHIFT" else "OFF-DUTY STANDBY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (clockedIn) SashioGreen else SashioGold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = if (clockedIn) "Status: Shift Active" else "No active shift",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (clockedIn) "Shift is currently running smoothly" else "Ready to commence daily sanitation schedules",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!clockedIn) {
                        Button(
                            onClick = onClockInClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LOCK IN ATTENDANCE / CLOCK IN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.clockOutStaff() },
                                border = BorderStroke(1.dp, SashioRed.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SashioRed),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CLOCK OUT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = onSosClick,
                                colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("EMERGENCY (SOS)", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Real-time Scorecard
        item {
            Column {
                Text("Performance Metrics Scorecard".uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("Total Assigned", "${myAssignedBookings.size}", SashioGold),
                        Triple("Active Jobs", "$activeCount", SashioTeal),
                        Triple("Completed", "$completedCount", SashioGreen)
                    ).forEach { (title, count, col) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(title, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(count, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = col)
                            }
                        }
                    }
                }
            }
        }

        // Live Assigned Jobs Preview on Home
        if (myAssignedBookings.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MY ASSIGNED DISPATCHES", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                    TextButton(onClick = { onTabNavigate("jobs") }) {
                        Text("View All (${myAssignedBookings.size})", color = SashioGold, fontSize = 10.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                myAssignedBookings.take(3).forEach { job ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, SashioGold.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(job.service_type, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                BookingStatusBadge(status = job.status)
                            }
                            Text("Client: ${job.client_name} | Event: ${job.event_type}", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("Address: ${job.address}", color = Color.LightGray, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        item {
            Column {
                Text("Quick Action Workspace".uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("Assigned Jobs", Icons.Default.Assignment, "jobs"),
                        Triple("Route Maps", Icons.Default.Map, "maps"),
                        Triple("Message Boards", Icons.Default.Chat, "messages")
                    ).forEach { (label, icon, tabTarget) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                            onClick = { onTabNavigate(tabTarget) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(icon, contentDescription = null, tint = SashioGold, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DISPATCH CHECKLIST PARAMETERS", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        "Confirm pre-departure fluid balance indicators",
                        "Log vehicle diesel status",
                        "Ensure tow-coupling safety steel pins locked",
                        "Synchronise offline client databases"
                    ).forEach { task ->
                        val isChecked = checkedBeforeDeparture.contains(task)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    checkedBeforeDeparture = if (isChecked) checkedBeforeDeparture - task else checkedBeforeDeparture + task
                                },
                                colors = CheckboxDefaults.colors(checkedColor = SashioGold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(task, color = if (isChecked) Color.LightGray.copy(alpha = 0.5f) else Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("SACHIO STAFF SOP", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.2.sp)
                Text("Prestige in Every Service", fontSize = 10.sp, fontStyle = FontStyle.Italic, color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                val sopList = listOf(
                    Triple("1. Be Ready", Icons.Default.AccessTime, listOf("Report on time.", "Wear full Sachio uniform.", "Be alert, fit, and ready for duty.")),
                    Triple("2. Respect the Brand", Icons.Default.Star, listOf("Be professional at all times.", "Treat clients, colleagues, and the public with respect.", "Protect the reputation of Sachio.")),
                    Triple("3. Before Leaving the Yard", Icons.Default.DirectionsCar, listOf("Inspect vehicle.", "Inspect toilets.", "Confirm job details.", "Ensure cleaning supplies and tools are available.")),
                    Triple("4. At the Client's Location", Icons.Default.Place, listOf("Greet the client professionally.", "Confirm setup location.", "Position toilets properly and safely.", "Take photos after setup.")),
                    Triple("5. Cleanliness First", Icons.Default.CheckCircle, listOf("Keep toilets clean and odor-free.", "Refill tissue, water, sanitizer, and chemicals.", "Never leave a dirty unit at a client site.")),
                    Triple("6. No Begging Policy", Icons.Default.Block, listOf("Salary is your entitlement.", "Tips, gifts, or support from Sachio are discretionary and not guaranteed.", "Never request money from clients.")),
                    Triple("7. Team Responsibility", Icons.Default.People, listOf("Follow instructions.", "Communicate issues immediately.", "Support team members when needed.")),
                    Triple("8. Safety", Icons.Default.Security, listOf("Handle equipment carefully.", "Report accidents, damage, or leaks immediately.", "Follow all safety procedures.")),
                    Triple("9. End of Duty", Icons.Default.DoneAll, listOf("Return all tools.", "Clean vehicle and equipment.", "Submit job report and photos.")),
                    Triple("10. The Sachio Standard", Icons.Default.Star, listOf("Every job must reflect:", "✅ Cleanliness", "✅ Professionalism", "✅ Reliability", "✅ Accountability", "✅ Respect"))
                )

                sopList.forEachIndexed { index, (title, icon, details) ->
                    val isExpanded = expandedSopIndex == index
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isExpanded) SashioSlate.copy(alpha = 0.35f) else SashioSlate.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, if (isExpanded) SashioGold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f)),
                        onClick = { expandedSopIndex = if (isExpanded) null else index },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = icon, contentDescription = null, tint = if (isExpanded) SashioGold else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = title, fontWeight = FontWeight.Bold, color = if (isExpanded) SashioGold else Color.White, fontSize = 12.sp)
                                }
                                Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = if (isExpanded) SashioGold else Color.White.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    details.forEach { detail ->
                                        Row(verticalAlignment = Alignment.Top) {
                                            if (!detail.startsWith("Every job must") && !detail.startsWith("✅")) {
                                                Text("• ", color = SashioGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(text = detail, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, lineHeight = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(colors = CardDefaults.cardColors(containerColor = SashioGold.copy(alpha = 0.05f)), border = BorderStroke(1.dp, SashioGold.copy(alpha = 0.2f)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SACHIO STAFF MOTTO", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "\"Prestige in Every Service\"", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun StaffClockInDialog(
    viewModel: SashioViewModel,
    onDismiss: () -> Unit
) {
    var clockInWorkplace by remember { mutableStateOf("SACHIO Victoria Island Depot") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFE111827)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).border(1.dp, SashioGold.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(48.dp), tint = SashioGold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("LOCK SHIFT ATTENDANCE", fontWeight = FontWeight.Bold, color = SashioGold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Select your reporting depot location to verify GPS checkout logs.", textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(12.dp))

                listOf("SACHIO Victoria Island Depot", "SACHIO Lekki Main Yard", "SACHIO Ikeja Depot Hub").forEach { depot ->
                    val isSel = clockInWorkplace == depot
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isSel) SashioGold.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, if (isSel) SashioGold else Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = { clockInWorkplace = depot }
                    ) {
                        Text(depot, modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("DISMISS", color = Color.White.copy(alpha = 0.5f))
                    }

                    Button(
                        onClick = {
                            viewModel.clockInStaff(clockInWorkplace)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("CLOCK IN", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StaffEmergencyDialog(onDismiss: () -> Unit) {
    var emergencyComments by remember { mutableStateOf("") }
    val localCtx = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFE1C0D0D)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).border(1.dp, SashioRed.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = SashioRed)
                Spacer(modifier = Modifier.height(14.dp))
                Text("EMERGENCY SECURE DIAL", fontWeight = FontWeight.Bold, color = SashioRed, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Immediately broadcast active location coordinates & alert central logistics.", textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = emergencyComments,
                    onValueChange = { emergencyComments = it },
                    label = { Text("Describe road incident or blockage", color = Color.LightGray) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SashioRed, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("CANCEL", color = Color.White.copy(alpha = 0.5f))
                    }

                    Button(
                        onClick = {
                            Toast.makeText(localCtx, "Emergency SOS alert dispatched to central cockpit!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("SOS REPORT", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StaffAddExpenseDialog(
    viewModel: SashioViewModel,
    onDismiss: () -> Unit
) {
    var expenseAmountInput by remember { mutableStateOf("") }
    var expenseCategorySelect by remember { mutableStateOf("Fuel Costs") }
    var expenseNotesInput by remember { mutableStateOf("") }
    val localCtx = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFE111827)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).border(1.dp, SashioGold.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(42.dp), tint = SashioGold)
                Spacer(modifier = Modifier.height(12.dp))
                Text("REGISTER EXPENSE SLIP", fontWeight = FontWeight.Bold, color = SashioGold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = expenseAmountInput,
                    onValueChange = { expenseAmountInput = it },
                    label = { Text("Expense Amount (₦)") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SashioGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = SashioGold
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Fuel Costs", "Toll", "Parking", "Maintenance").forEach { cat ->
                        val isSel = expenseCategorySelect == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { expenseCategorySelect = cat },
                            label = { Text(cat, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SashioGold, selectedLabelColor = Color.Black)
                        )
                    }
                }

                OutlinedTextField(
                    value = expenseNotesInput,
                    onValueChange = { expenseNotesInput = it },
                    label = { Text("Spent Reason / Receipt Notes") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SashioGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = SashioGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("DISMISS", color = Color.White.copy(alpha = 0.5f))
                    }

                    Button(
                        onClick = {
                            val amtNum = expenseAmountInput.toDoubleOrNull() ?: 0.0
                            if (amtNum > 0.0) {
                                val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                viewModel.submitStaffExpense(
                                    amount = amtNum,
                                    category = expenseCategorySelect,
                                    notes = expenseNotesInput,
                                    date = dateSdf.format(Date())
                                ) {
                                    Toast.makeText(localCtx, "Expense claim submitted to Admin for approval!", Toast.LENGTH_SHORT).show()
                                }
                                onDismiss()
                            } else {
                                Toast.makeText(localCtx, "Please enter a valid expense amount.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("TRANSMIT CLAIM", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
