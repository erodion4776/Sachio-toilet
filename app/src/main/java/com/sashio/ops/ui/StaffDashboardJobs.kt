package com.sashio.ops.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.data.SupabaseBooking
import com.sashio.ops.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardJobsTab(
    viewModel: SashioViewModel,
    workOfflineToggle: Boolean
) {
    val myAssignedBookings by viewModel.myAssignedBookings.collectAsStateWithLifecycle()
    val localBookings by viewModel.staffBookings.collectAsStateWithLifecycle()
    var selectedJobDetailsId by remember { mutableStateOf<String?>(null) }
    var selectedFilterTab by remember { mutableStateOf("Assigned") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadMyAssignedBookings()
    }

    // Job Details Internal Checklist & Form States
    var activeChecklistBeforeDeparture by remember { mutableStateOf(setOf<String>()) }
    var activeChecklistUponArrival by remember { mutableStateOf(setOf<String>()) }
    var activeChecklistServiceActivities by remember { mutableStateOf(setOf<String>()) }

    var selectedInspectionCondition by remember { mutableStateOf("Pristine") }
    val selectedWaterLevel by remember { mutableStateOf("100%") }
    var cleanlinessScoreInput by remember { mutableStateOf(5f) }
    val equipmentConditionInput by remember { mutableStateOf("Good") }
    val damageAssessmentInput by remember { mutableStateOf("No noticeable exterior scratches or pipe dents.") }
    var inspectionCommentInput by remember { mutableStateOf("Excellent pre-commissioning standard. Sovereign vacuum manifold initialized perfectly.") }

    var beforePhotosList by remember { mutableStateOf(listOf("https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=500&auto=format&fit=crop")) }
    var afterPhotosList by remember { mutableStateOf(listOf("https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=500&auto=format&fit=crop")) }
    var showCameraSimulationBox by remember { mutableStateOf<String?>(null) }
    var cameraCapturingStatus by remember { mutableStateOf("Live camera lens online...") }
    var queuedOfflineInspections by remember { mutableStateOf(listOf<String>()) }

    if (selectedJobDetailsId != null) {
        val bkId = selectedJobDetailsId!!
        val b = myAssignedBookings.find { it.id == bkId }

        if (b == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Job data not found", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { selectedJobDetailsId = null }) {
                        Text("Back to Jobs")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { selectedJobDetailsId = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SashioGold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Job Deep-Dive: #QR-${b.id.take(8)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
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
                                Text(b.status.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = SashioGold)
                                Text("Date: ${b.event_date ?: "Pending"}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(b.service_type, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${b.variant_name} | ${b.model_number}", fontSize = 11.sp, color = SashioGold)
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("CLIENT NAME: ${b.client_name}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("PHONE: ${b.client_phone}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("BUSINESS: ${b.company_name.ifEmpty { "Private Client" }}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("ADDRESS: ${b.address}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                "SPECIAL NOTES: ${b.special_instructions.ifEmpty { "Standard setup protocol." }}",
                                fontSize = 12.sp,
                                color = SashioGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Step-by-step Quality Checklists", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("1. Pre-Departure Parameters", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            listOf("Trailer coupling pin secured", "Dual hydraulic brakes verified", "Bio-degradable chemical charged").forEach { item ->
                                val isDone = activeChecklistBeforeDeparture.contains(item)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isDone,
                                        onCheckedChange = {
                                            activeChecklistBeforeDeparture = if (isDone) activeChecklistBeforeDeparture - item else activeChecklistBeforeDeparture + item
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = SashioGold)
                                    )
                                    Text(item, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("2. Upon Arrival Alignment", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            listOf("Halt vehicle in spacious quadrant", "Deploy hydraulic support stands", "Integrate standard water supply line").forEach { item ->
                                val isDone = activeChecklistUponArrival.contains(item)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isDone,
                                        onCheckedChange = {
                                            activeChecklistUponArrival = if (isDone) activeChecklistUponArrival - item else activeChecklistUponArrival + item
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = SashioGold)
                                    )
                                    Text(item, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("3. On-Site Service Activities", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            listOf("Sanitize floor, seat, and vanity handles", "Refill luxury scent atomizer", "Refill paper towel and soap stocks").forEach { item ->
                                val isDone = activeChecklistServiceActivities.contains(item)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isDone,
                                        onCheckedChange = {
                                            activeChecklistServiceActivities = if (isDone) activeChecklistServiceActivities - item else activeChecklistServiceActivities + item
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = SashioGold)
                                    )
                                    Text(item, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("SANITY PHOTO & VIDEO REPORTING", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (showCameraSimulationBox != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black)
                                        .border(1.dp, SashioGold, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(32.dp), tint = SashioGold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(cameraCapturingStatus, color = Color.White, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                cameraCapturingStatus = "Capturing image frame..."
                                                val mockUrl = "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=500&auto=format&fit=crop"
                                                if (showCameraSimulationBox == "before") {
                                                    beforePhotosList = beforePhotosList + mockUrl
                                                } else {
                                                    afterPhotosList = afterPhotosList + mockUrl
                                                }
                                                cameraCapturingStatus = "Sanitation photograph captured successfully!"
                                                showCameraSimulationBox = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SashioGold)
                                        ) {
                                            Text("SNAP CAPTURE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            showCameraSimulationBox = "before"
                                            cameraCapturingStatus = "Resolving aperture lens. Aligning restroom exterior..."
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SashioGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Before Photo", fontSize = 10.sp, color = Color.White)
                                    }

                                    Button(
                                        onClick = {
                                            showCameraSimulationBox = "after"
                                            cameraCapturingStatus = "Focusing pristine interior fixtures..."
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SashioGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("After Photo", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Captured Files: ${beforePhotosList.size} Before, ${afterPhotosList.size} After", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("DIGITAL INSPECTION SPEC FORM", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Toilet Condition Status", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Pristine", "Fair", "Needs Repair").forEach { cond ->
                                    val isSelected = selectedInspectionCondition == cond
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedInspectionCondition = cond },
                                        label = { Text(cond, fontSize = 11.sp, color = if (isSelected) Color.Black else Color.White) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SashioGold,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Sanitation Rating: ${cleanlinessScoreInput.toInt()}/5", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Slider(
                                value = cleanlinessScoreInput,
                                onValueChange = { cleanlinessScoreInput = it },
                                valueRange = 1f..5f,
                                steps = 3,
                                colors = SliderDefaults.colors(thumbColor = SashioGold, activeTrackColor = SashioGold)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = inspectionCommentInput,
                                onValueChange = { inspectionCommentInput = it },
                                label = { Text("Sanitation remarks & field observations") },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SashioGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = SashioGold
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (workOfflineToggle) {
                                        queuedOfflineInspections = queuedOfflineInspections + "Booking #${b.id.take(6)}: Cond: $selectedInspectionCondition, Cln: ${cleanlinessScoreInput.toInt()}"
                                        Toast.makeText(context, "Inspection cached offline successfully.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val longId = b.id.hashCode().toLong()
                                        viewModel.submitInspectionReport(
                                            bookingId = longId,
                                            toiletCondition = selectedInspectionCondition,
                                            waterLevel = selectedWaterLevel,
                                            cleanlinessScore = cleanlinessScoreInput.toInt(),
                                            equipmentCondition = equipmentConditionInput,
                                            damageAssessment = damageAssessmentInput,
                                            comments = inspectionCommentInput,
                                            beforePhotos = beforePhotosList,
                                            afterPhotos = afterPhotosList
                                        )
                                        Toast.makeText(context, "Digital inspection report transmitted.", Toast.LENGTH_SHORT).show()
                                    }
                                    selectedJobDetailsId = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text("TRANSMIT DIGITAL SPECIFICATION REPORT", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Assigned", "Active", "Completed").forEach { fTab ->
                    val isSel = selectedFilterTab == fTab
                    Button(
                        onClick = { selectedFilterTab = fTab },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) SashioGold else SashioSlate.copy(alpha = 0.3f),
                            contentColor = if (isSel) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text(fTab, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val filteredList = myAssignedBookings.filter { b ->
                when (selectedFilterTab) {
                    "Active" -> b.status == "IN TRANSIT" || b.status == "ARRIVED" || b.status == "SETUP" || b.status == "ACTIVE" || b.status == "SETUP IN PROGRESS"
                    "Completed" -> b.status == "SERVICE DELIVERED" || b.status == "COMPLETED" || b.status == "Completed"
                    else -> b.status == "Staff Assigned" || b.status == "Booking Confirmed" || b.status == "Payment Confirmed"
                }
            }

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No $selectedFilterTab jobs found.", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { b ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(b.service_type, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    BookingStatusBadge(status = b.status)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Client: ${b.client_name} | Phone: ${b.client_phone}", fontSize = 11.sp, color = Color.LightGray)
                                Text("Location: ${b.address}", fontSize = 11.sp, color = SashioGold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (b.special_instructions.isNotBlank()) {
                                    Text("Notes: ${b.special_instructions}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { selectedJobDetailsId = b.id },
                                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    when (b.status) {
                                        "Staff Assigned" -> {
                                            Button(
                                                onClick = {
                                                    viewModel.staffUpdateBookingStatus(b.id, "IN TRANSIT") {
                                                        Toast.makeText(context, "Departure locked! In transit to venue.", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                                                modifier = Modifier.weight(1.3f).height(36.dp)
                                            ) {
                                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("START SHIFT", fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 10.sp)
                                            }
                                        }
                                        "IN TRANSIT" -> {
                                            Button(
                                                onClick = {
                                                    viewModel.staffUpdateBookingStatus(b.id, "ARRIVED") {
                                                        Toast.makeText(context, "Marked as Arrived at venue.", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SashioTeal),
                                                modifier = Modifier.weight(1.3f).height(36.dp)
                                            ) {
                                                Icon(Icons.Default.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("ARRIVED", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp)
                                            }
                                        }
                                        "ARRIVED" -> {
                                            Button(
                                                onClick = {
                                                    viewModel.staffUpdateBookingStatus(b.id, "SETUP IN PROGRESS") {
                                                        Toast.makeText(context, "Commencing cabin setup.", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                                                modifier = Modifier.weight(1.3f).height(36.dp)
                                            ) {
                                                Text("SETUP", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 10.sp)
                                            }
                                        }
                                        "SETUP IN PROGRESS", "SETUP" -> {
                                            Button(
                                                onClick = {
                                                    viewModel.staffUpdateBookingStatus(b.id, "SERVICE DELIVERED") {
                                                        Toast.makeText(context, "Service delivered and active!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SashioGreen),
                                                modifier = Modifier.weight(1.3f).height(36.dp)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("COMPLETE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp)
                                            }
                                        }
                                        else -> {
                                            Box(
                                                modifier = Modifier.weight(1.3f).height(36.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("COMPLETED", color = SashioGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
