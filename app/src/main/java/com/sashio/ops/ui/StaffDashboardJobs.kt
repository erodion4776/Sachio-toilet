
package com.sashio.ops.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*

private enum class StaffJobFilter {
    ASSIGNED, ACTIVE, COMPLETED
}

private fun normalizedJobStatus(status: String): String {
    return status.trim()
        .uppercase()
        .replace("_", " ")
        .replace("-", " ")
        .replace(Regex("\\s+"), " ")
}

private fun isAssignedJob(status: String): Boolean {
    return normalizedJobStatus(status) in setOf(
        "STAFF ASSIGNED",
        "BOOKING CONFIRMED",
        "PAYMENT CONFIRMED",
        "ASSIGNED",
        "CONFIRMED",
        "ACCEPTED"
    )
}

private fun isActiveJob(status: String): Boolean {
    return normalizedJobStatus(status) in setOf(
        "IN TRANSIT",
        "ARRIVED",
        "SETUP",
        "ACTIVE",
        "SETUP IN PROGRESS",
        "IN PROGRESS",
        "SERVICE IN PROGRESS",
        "ONGOING"
    )
}

private fun isCompletedJob(status: String): Boolean {
    return normalizedJobStatus(status) in setOf(
        "SERVICE DELIVERED",
        "COMPLETED",
        "COMPLETE",
        "DELIVERED",
        "CLOSED"
    )
}

private fun validBookingCoordinates(
    latitude: Double,
    longitude: Double
): Boolean {
    return latitude.isFinite() &&
        longitude.isFinite() &&
        latitude in -90.0..90.0 &&
        longitude in -180.0..180.0 &&
        (latitude != 0.0 || longitude != 0.0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardJobsTab(
    viewModel: SashioViewModel,
    workOfflineToggle: Boolean
) {
    val bookings by viewModel.myAssignedBookings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedJobDetailsId by remember {
        mutableStateOf<String?>(null)
    }

    var selectedFilter by remember {
        mutableStateOf(StaffJobFilter.ASSIGNED)
    }

    var selectedInspectionCondition by remember {
        mutableStateOf("Pristine")
    }

    var selectedWaterLevel by remember {
        mutableStateOf("100%")
    }

    var cleanlinessScoreInput by remember {
        mutableFloatStateOf(5f)
    }

    var equipmentConditionInput by remember {
        mutableStateOf("Good")
    }

    var damageAssessmentInput by remember {
        mutableStateOf("")
    }

    var inspectionCommentInput by remember {
        mutableStateOf("")
    }

    var isSubmittingInspection by remember {
        mutableStateOf(false)
    }

    var checklistBefore by remember {
        mutableStateOf(setOf<String>())
    }

    var checklistArrival by remember {
        mutableStateOf(setOf<String>())
    }

    var checklistService by remember {
        mutableStateOf(setOf<String>())
    }

    LaunchedEffect(Unit) {
        viewModel.loadMyAssignedBookings()
    }

    val selectedBooking = bookings.find {
        it.id == selectedJobDetailsId
    }

    fun openGps(
        latitude: Double,
        longitude: Double,
        address: String
    ) {
        if (validBookingCoordinates(latitude, longitude)) {
            launchExternalGpsNavigation(
                context,
                latitude,
                longitude,
                address
            )
        } else {
            Toast.makeText(
                context,
                "This booking has no valid GPS coordinates. Please use the address.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun updateChecklist(
        current: Set<String>,
        item: String
    ): Set<String> {
        return if (item in current) {
            current - item
        } else {
            current + item
        }
    }

    if (selectedJobDetailsId != null) {
        val booking = selectedBooking

        if (booking == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Job data not found")
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            selectedJobDetailsId = null
                        }
                    ) {
                        Text("Back to Jobs")
                    }
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            selectedJobDetailsId = null
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Text(
                        text = "Job Details #${booking.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            booking.status.uppercase(),
                            color = SashioGold,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            booking.service_type,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "${booking.variant_name} | ${booking.model_number}"
                        )

                        HorizontalDivider()
                       
                        Text("ASSIGNED STAFF: ${booking.assigned_staff_name ?: "Not assigned"}")
                        Text("CLIENT: ${booking.client_name}")
                        Text("PHONE: ${booking.client_phone}")
                        Text("BUSINESS: ${booking.company_name.ifBlank { "Private Client" }}")
                        Text("ADDRESS: ${booking.address}")
                        Text("DATE: ${booking.event_date ?: "Pending"}")

                        if (booking.special_instructions.isNotBlank()) {
                            Text(
                                "SPECIAL NOTES: ${booking.special_instructions}",
                                color = SashioGold
                            )
                        }

                        Button(
                            onClick = {
                                openGps(
                                    booking.latitude,
                                    booking.longitude,
                                    booking.address
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Directions,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("OPEN DIRECTIONS")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "PRE-DEPARTURE CHECKLIST",
                            fontWeight = FontWeight.Bold,
                            color = SashioGold
                        )

                        listOf(
                            "Trailer coupling pin secured",
                            "Dual hydraulic brakes verified",
                            "Bio-degradable chemical charged"
                        ).forEach { task ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task in checklistBefore,
                                    onCheckedChange = {
                                        checklistBefore =
                                            updateChecklist(checklistBefore, task)
                                    }
                                )
                                Text(task)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Text(
                            "ARRIVAL CHECKLIST",
                            fontWeight = FontWeight.Bold,
                            color = SashioGold
                        )

                        listOf(
                            "Vehicle positioned safely",
                            "Hydraulic support stands deployed",
                            "Water supply line connected"
                        ).forEach { task ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task in checklistArrival,
                                    onCheckedChange = {
                                        checklistArrival =
                                            updateChecklist(checklistArrival, task)
                                    }
                                )
                                Text(task)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Text(
                            "SERVICE ACTIVITIES",
                            fontWeight = FontWeight.Bold,
                            color = SashioGold
                        )

                        listOf(
                            "Floor and fixtures sanitized",
                            "Scent atomizer checked",
                            "Paper towels and soap replenished"
                        ).forEach { task ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task in checklistService,
                                    onCheckedChange = {
                                        checklistService =
                                            updateChecklist(checklistService, task)
                                    }
                                )
                                Text(task)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "INSPECTION PHOTOGRAPHS",
                            fontWeight = FontWeight.Bold,
                            color = SashioGold
                        )

                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )

                        Text(
                            "Camera capture and photo upload are not connected yet. " +
                                "No sample photographs will be added to your inspection report."
                        )

                        Text(
                            "Do not submit an inspection as photo-verified until real photographs have been captured and uploaded."
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "DIGITAL INSPECTION REPORT",
                            fontWeight = FontWeight.Bold,
                            color = SashioGold
                        )

                        Text("Toilet condition")

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Pristine",
                                "Fair",
                                "Needs Repair"
                            ).forEach { condition ->
                                FilterChip(
                                    selected = selectedInspectionCondition == condition,
                                    onClick = {
                                        selectedInspectionCondition = condition
                                    },
                                    label = {
                                        Text(condition)
                                    }
                                )
                            }
                        }

                        Text("Water level")

                        var waterMenuExpanded by remember {
                            mutableStateOf(false)
                        }

                        ExposedDropdownMenuBox(
                            expanded = waterMenuExpanded,
                            onExpandedChange = {
                                waterMenuExpanded = !waterMenuExpanded
                            }
                        ) {
                            OutlinedTextField(
                                value = selectedWaterLevel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Water level") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = waterMenuExpanded
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = waterMenuExpanded,
                                onDismissRequest = {
                                    waterMenuExpanded = false
                                }
                            ) {
                                listOf(
                                    "0%",
                                    "25%",
                                    "50%",
                                    "75%",
                                    "100%"
                                ).forEach { level ->
                                    DropdownMenuItem(
                                        text = { Text(level) },
                                        onClick = {
                                            selectedWaterLevel = level
                                            waterMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            "Cleanliness rating: ${cleanlinessScoreInput.toInt()}/5",
                            fontWeight = FontWeight.Bold
                        )

                        Slider(
                            value = cleanlinessScoreInput,
                            onValueChange = {
                                cleanlinessScoreInput = it
                            },
                            valueRange = 1f..5f,
                            steps = 3
                        )

                        Text("Equipment condition")

                        var equipmentMenuExpanded by remember {
                            mutableStateOf(false)
                        }

                        ExposedDropdownMenuBox(
                            expanded = equipmentMenuExpanded,
                            onExpandedChange = {
                                equipmentMenuExpanded = !equipmentMenuExpanded
                            }
                        ) {
                            OutlinedTextField(
                                value = equipmentConditionInput,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Equipment condition") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = equipmentMenuExpanded
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = equipmentMenuExpanded,
                                onDismissRequest = {
                                    equipmentMenuExpanded = false
                                }
                            ) {
                                listOf(
                                    "Excellent",
                                    "Good",
                                    "Fair",
                                    "Needs Repair",
                                    "Not Working"
                                ).forEach { condition ->
                                    DropdownMenuItem(
                                        text = { Text(condition) },
                                        onClick = {
                                            equipmentConditionInput = condition
                                            equipmentMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = damageAssessmentInput,
                            onValueChange = {
                                damageAssessmentInput = it
                            },
                            label = {
                                Text("Damage assessment")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        OutlinedTextField(
                            value = inspectionCommentInput,
                            onValueChange = {
                                inspectionCommentInput = it
                            },
                            label = {
                                Text("Inspection remarks")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        if (workOfflineToggle) {
                            Text(
                                "Offline mode is enabled. This screen does not yet have persistent offline inspection storage. Your report will not be saved by pressing the button.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Button(
                            onClick = {
                                if (isSubmittingInspection) {
                                    return@Button
                                }

                                if (workOfflineToggle) {
                                    Toast.makeText(
                                        context,
                                        "Offline inspection storage is not connected. Report not saved.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }

                                // SupabaseBooking.id is a String.
                                // Never convert it using hashCode().
                                val numericBookingId =
                                    booking.id.toLongOrNull()

                                if (numericBookingId == null) {
                                    Toast.makeText(
                                        context,
                                        "This booking uses a non-numeric ID. The inspection ViewModel must accept the String booking ID before submission.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }

                                isSubmittingInspection = true

                                try {
                                    viewModel.submitInspectionReport(
                                        bookingId = numericBookingId,
                                        toiletCondition = selectedInspectionCondition,
                                        waterLevel = selectedWaterLevel,
                                        cleanlinessScore = cleanlinessScoreInput.toInt(),
                                        equipmentCondition = equipmentConditionInput,
                                        damageAssessment = damageAssessmentInput,
                                        comments = inspectionCommentInput,
                                        beforePhotos = emptyList(),
                                        afterPhotos = emptyList()
                                    )

                                    Toast.makeText(
                                        context,
                                        "Inspection request submitted to the current ViewModel. Backend persistence has not been confirmed.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    selectedJobDetailsId = null
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Inspection submission failed: ${e.message ?: "Unknown error"}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    isSubmittingInspection = false
                                }
                            },
                            enabled = !isSubmittingInspection,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isSubmittingInspection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("SUBMIT INSPECTION")
                            }
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StaffJobFilter.entries.forEach { filter ->
                    val selected = selectedFilter == filter

                    Button(
                        onClick = {
                            selectedFilter = filter
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) {
                                SashioGold
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (selected) {
                                Color.Black
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(
                            when (filter) {
                                StaffJobFilter.ASSIGNED -> "Assigned"
                                StaffJobFilter.ACTIVE -> "Active"
                                StaffJobFilter.COMPLETED -> "Completed"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            val filteredBookings = bookings.filter { booking ->
                when (selectedFilter) {
                    StaffJobFilter.ASSIGNED ->
                        isAssignedJob(booking.status)

                    StaffJobFilter.ACTIVE ->
                        isActiveJob(booking.status)

                    StaffJobFilter.COMPLETED ->
                        isCompletedJob(booking.status)
                }
            }

            if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AssignmentLate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "No ${selectedFilter.name.lowercase()} jobs found.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredBookings,
                        key = { it.id }
                    ) { booking ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        booking.service_type,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    BookingStatusBadge(
                                        status = booking.status
                                    )
                                }

                                Text(
                                    "Client: ${booking.client_name} | ${booking.client_phone}",
                                    fontSize = 11.sp
                                )

                                Text(
                                    "Location: ${booking.address}",
                                    fontSize = 11.sp,
                                    color = SashioGold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (booking.special_instructions.isNotBlank()) {
                                    Text(
                                        "Notes: ${booking.special_instructions}",
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedJobDetailsId = booking.id
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(4.dp)
                                    ) {
                                        Text("DETAILS", fontSize = 10.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            openGps(
                                                booking.latitude,
                                                booking.longitude,
                                                booking.address
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Directions,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text("GPS", fontSize = 10.sp)
                                    }
                                }

                                when (normalizedJobStatus(booking.status)) {
                                    "STAFF ASSIGNED",
                                    "ASSIGNED" -> {
                                        Button(
                                            onClick = {
                                                viewModel.staffUpdateBookingStatus(
                                                    booking.id,
                                                    "IN TRANSIT"
                                                ) {
                                                    Toast.makeText(
                                                        context,
                                                        "Departure status update requested.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("START SHIFT")
                                        }
                                    }

                                    "IN TRANSIT" -> {
                                        Button(
                                            onClick = {
                                                viewModel.staffUpdateBookingStatus(
                                                    booking.id,
                                                    "ARRIVED"
                                                ) {
                                                    Toast.makeText(
                                                        context,
                                                        "Arrival status update requested.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("MARK ARRIVED")
                                        }
                                    }

                                    "ARRIVED" -> {
                                        Button(
                                            onClick = {
                                                viewModel.staffUpdateBookingStatus(
                                                    booking.id,
                                                    "SETUP IN PROGRESS"
                                                ) {
                                                    Toast.makeText(
                                                        context,
                                                        "Setup status update requested.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("START SETUP")
                                        }
                                    }

                                    "SETUP",
                                    "SETUP IN PROGRESS" -> {
                                        Button(
                                            onClick = {
                                                viewModel.staffUpdateBookingStatus(
                                                    booking.id,
                                                    "SERVICE DELIVERED"
                                                ) {
                                                    Toast.makeText(
                                                        context,
                                                        "Service marked as delivered.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("COMPLETE JOB")
                                        }
                                    }

                                    else -> {
                                        Text(
                                            if (isCompletedJob(booking.status)) {
                                                "COMPLETED"
                                            } else {
                                                "STATUS: ${booking.status}"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
}
