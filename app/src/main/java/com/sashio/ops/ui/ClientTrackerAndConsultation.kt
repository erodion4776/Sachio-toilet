package com.sashio.ops.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.Booking
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*

@Composable
fun ClientQuotationTrackerTab(viewModel: SashioViewModel) {
    val bookings by viewModel.clientBookings.collectAsStateWithLifecycle()
    val activeTrackers by viewModel.activeLiveTrackers.collectAsStateWithLifecycle()
    val liveBookings by viewModel.clientSupabaseBookings.collectAsStateWithLifecycle()
    val bookingLiveLocations by viewModel.bookingLiveLocations.collectAsStateWithLifecycle()
    var bookingToConvert by remember { mutableStateOf<Booking?>(null) }
    val context = LocalContext.current
    var activeTrackingBookingId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadClientSupabaseBookings()
    }

    LaunchedEffect(liveBookings) {
        while (true) {
            liveBookings
                .filter { it.assigned_staff_id != null }
                .forEach { booking ->
                    viewModel.loadBookingLiveLocation(booking.id)
                }

            kotlinx.coroutines.delay(15000)
        }
    }

    if (activeTrackingBookingId != null) {
        SashioLiveTrackingScreen(
            viewModel = viewModel,
            bookingId = activeTrackingBookingId!!,
            onBack = { activeTrackingBookingId = null }
        )
        return
    }

    if (bookingToConvert != null) {
        val b = bookingToConvert!!
        val grandTotal = b.quotedPrice + b.logisticsCost + b.vatAmount
        var serviceDate by remember(b.id) { mutableStateOf(b.eventDate) }
        var serviceDuration by remember(b.id) { mutableStateOf(b.duration) }
        var deliveryAddress by remember(b.id) { mutableStateOf(b.address) }
        var specialInstructions by remember(b.id) { mutableStateOf(b.specialInstructions) }

        AlertDialog(
            onDismissRequest = { bookingToConvert = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Handshake,
                        contentDescription = null,
                        tint = SashioGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Confirm Service Booking",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SashioSlate
                    )
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = SashioSlate.copy(alpha = 0.05f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "1. Confirm Service Details",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SashioSlate
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Product Class: ${b.serviceType}",
                                    fontSize = 11.sp,
                                    color = SashioTextMuted
                                )
                                Text(
                                    "Variant: ${b.variantName} (${b.modelNumber})",
                                    fontSize = 11.sp,
                                    color = SashioTextMuted
                                )
                                Text(
                                    "Quantity: ${b.quantity} unit(s)",
                                    fontSize = 11.sp,
                                    color = SashioTextMuted
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    color = Color.LightGray.copy(alpha = 0.5f)
                                )
                                Text(
                                    "Quote Subtotal: ₦%,.2f".format(b.quotedPrice),
                                    fontSize = 11.sp,
                                    color = SashioSlate
                                )
                                Text(
                                    "Logistics Cost: ₦%,.2f".format(b.logisticsCost),
                                    fontSize = 11.sp,
                                    color = SashioSlate
                                )
                                Text(
                                    "VAT (7.5%): ₦%,.2f".format(b.vatAmount),
                                    fontSize = 11.sp,
                                    color = SashioSlate
                                )
                                Text(
                                    "Grand Total: ₦%,.2f".format(grandTotal),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SashioGreen
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            "2. Select Service Dates & Duration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SashioSlate
                        )
                        OutlinedTextField(
                            value = serviceDate,
                            onValueChange = { serviceDate = it },
                            label = { Text("Service Start Date") },
                            placeholder = { Text("e.g. June 15, 2026") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = SashioGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = serviceDuration,
                            onValueChange = { serviceDuration = it },
                            label = { Text("Service Duration") },
                            placeholder = { Text("e.g. 3 Days, 1 Week") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = SashioGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(
                            "3. Confirm Delivery Location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SashioSlate
                        )
                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text("Delivery Address") },
                            maxLines = 2,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = SashioGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(
                            "4. Special Instructions & Directions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SashioSlate
                        )
                        OutlinedTextField(
                            value = specialInstructions,
                            onValueChange = { specialInstructions = it },
                            label = { Text("Special Instructions") },
                            maxLines = 3,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = SashioGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clientConvertCopyToBooking(
                            bookingId = b.id,
                            serviceDate = serviceDate,
                            duration = serviceDuration,
                            deliveryAddress = deliveryAddress,
                            specialInstructions = specialInstructions,
                            onComplete = {
                                bookingToConvert = null
                                Toast.makeText(
                                    context,
                                    "Quotation successfully converted to Booking!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SashioGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Confirm Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToConvert = null }) {
                    Text("Cancel", color = SashioSlate)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "QUOTATION & ACTIVE DISPATCH TRACKER",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Follow your quotations from Under Review to Delivery",
            style = MaterialTheme.typography.bodySmall,
            color = SashioTextMuted
        )
        Spacer(modifier = Modifier.height(12.dp))
                val liveQuoted = liveBookings
            .filter { it.quoted_price > 0.0 && it.payment_status != "success" }
            .take(2)

        if (liveQuoted.isNotEmpty()) {
            Text(
                "LIVE QUOTATIONS READY FOR PAYMENT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SashioGold
            )
            val paymentError by viewModel.roleManagementError.collectAsStateWithLifecycle()
            paymentError?.let { err ->
                Text(
                    err,
                    color = SashioRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            liveQuoted.forEach { lb ->
                val grandTotal = lb.quoted_price + lb.logistics_cost + lb.vat_amount

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("live_quote_card_${lb.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(lb.service_type, fontWeight = FontWeight.Bold)
                        Text(
                            "${lb.event_type} | Qty ${lb.quantity} | ${lb.duration}",
                            fontSize = 10.sp,
                            color = SashioTextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Subtotal: ₦%,.0f".format(lb.quoted_price), fontSize = 11.sp)
                        Text("Logistics: ₦%,.0f".format(lb.logistics_cost), fontSize = 11.sp)
                        Text("VAT: ₦%,.0f".format(lb.vat_amount), fontSize = 11.sp)
                        Text(
                            "Total: ₦%,.0f".format(grandTotal),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SashioGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.triggerPaystackPaymentForRealBooking(
                                    lb.id,
                                    grandTotal
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Pay Now",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        val paidBookings = liveBookings.filter { it.payment_status == "success" }

        if (paidBookings.isNotEmpty()) {
            Text(
                "PAID & CONFIRMED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SashioGreen
            )
            Spacer(modifier = Modifier.height(6.dp))

            paidBookings.forEach { pb ->
                val grandTotal = pb.quoted_price + pb.logistics_cost + pb.vat_amount

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("paid_booking_card_${pb.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pb.service_type, fontWeight = FontWeight.Bold)
                            BookingStatusBadge(status = pb.status)
                        }
                        Text(
                            "${pb.event_type} | Qty ${pb.quantity} | ${pb.duration}",
                            fontSize = 10.sp,
                            color = SashioTextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SashioGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Paid: ₦%,.0f".format(grandTotal),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SashioGreen
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (bookings.isEmpty() && liveBookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No active quotes. Request a quote to initiate the workflow.",
                    color = SashioTextMuted
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(bookings) { b ->
                    val remoteBooking = liveBookings.firstOrNull {
                        it.local_booking_id == b.id
                    }
                    val displayStatus = remoteBooking?.status ?: b.status
val displayQuotedPrice = remoteBooking?.quoted_price ?: b.quotedPrice
val displayLogisticsCost = remoteBooking?.logistics_cost ?: b.logisticsCost
val displayVatAmount = remoteBooking?.vat_amount ?: b.vatAmount
val displayPaymentStatus = remoteBooking?.payment_status ?: b.paymentStatus

                    val liveLocation = remoteBooking?.let {
                        bookingLiveLocations[it.id]
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("booking_card_${b.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        b.serviceType,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Ref: #QR-${b.id}",
                                        fontSize = 11.sp,
                                        color = SashioTextMuted
                                    )
                                }
                                BookingStatusBadge(status = displayStatus)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PinDrop,
                                    contentDescription = null,
                                    tint = SashioGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    b.address,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 11.sp,
                                    color = SashioTextMuted
                                )
                            }

                            if (b.status in listOf(
                                    "IN TRANSIT",
                                    "ARRIVED",
                                    "SERVICE DELIVERED",
                                    "SETUP IN PROGRESS"
                                )
                            ) {
                                var layer by remember { mutableStateOf("Standard") }

                                Spacer(modifier = Modifier.height(12.dp))

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = SashioSlate
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.LocalShipping,
                                                    contentDescription = null,
                                                    tint = SashioGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "Live Dispatch Tracking",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }

                                            AssistChip(
                                                onClick = {
                                                    layer = when (layer) {
                                                        "Standard" -> "Satellite"
                                                        "Satellite" -> "Traffic"
                                                        "Traffic" -> "Operations"
                                                        else -> "Standard"
                                                    }
                                                },
                                                label = {
                                                    Text(
                                                        "Layer: $layer",
                                                        fontSize = 9.sp,
                                                        color = Color.White
                                                    )
                                                },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = Color.White.copy(alpha = 0.15f)
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            SashioCanvasMap(
                                                latitude = b.latitude,
                                                longitude = b.longitude,
                                                vehicleLat = liveLocation?.latitude,
                                                vehicleLng = liveLocation?.longitude,
                                                mapStyle = layer,
                                                showRoute = true,
                                                label = "My Event Location"
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                if (liveLocation != null) {
                                                    Text(
                                                        "Staff GPS updated: ${liveLocation.updated_at}",
                                                        color = Color.White,
                                                        fontSize = 11.sp
                                                    )

                                                    liveLocation.accuracy_m?.let {
                                                        Text(
                                                            "GPS accuracy: %.0f metres".format(it),
                                                            color = Color.White.copy(alpha = 0.8f),
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                } else {
                                                    Text(
                                                        "Waiting for staff GPS location…",
                                                        color = Color.White,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    "Crew: ${b.assignedStaffName ?: "Unassigned"}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    "Status: ${b.status}",
                                                    color = SashioGold,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                                                            if (b.status in listOf(
                                    "Payment Confirmed",
                                    "Booking Confirmed",
                                    "Staff Assigned",
                                    "SERVICE DELIVERED",
                                    "IN TRANSIT",
                                    "ARRIVED",
                                    "SETUP IN PROGRESS",
                                    "SERVICE ACTIVE",
                                    "COMPLETED",
                                    "Completed"
                                )
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { activeTrackingBookingId = b.id },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SashioSlate,
                                        contentColor = SashioGold
                                    ),
                                    border = BorderStroke(1.dp, SashioGold),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                        .testTag("radar_button_${b.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Explore,
                                        contentDescription = null,
                                        tint = SashioGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Track Prestige Service Real-Time",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (displayStatus in listOf(
                                    "Quotation Sent",
                                "Quotation Generated",
                                    "Client Accepted",
                                    "Booking Confirmed",
                                    "SERVICE DELIVERED",
                                    "Completed"
                                )
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                                val grandTotal =
    displayQuotedPrice + displayLogisticsCost + displayVatAmount

                                Text(
                                    "Commercial Breakdown",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SashioSlate
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    "Subtotal ₦%,.0f | Logistics ₦%,.0f | VAT ₦%,.0f".format(
                                        displayQuotedPrice,
displayLogisticsCost,
displayVatAmount
                                    ),
                                    fontSize = 10.sp,
                                    color = SashioTextMuted
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    when (displayStatus) {
                                        "Quotation Sent", "Quotation Generated" -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.clientRejectQuotation(b.id) {}
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = SashioRed,
                                                        contentColor = Color.White
                                                    ),
                                                    modifier = Modifier
                                                        .height(34.dp)
                                                        .weight(1f)
                                                ) {
                                                    Text(
                                                        "Reject",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.clientRequestRevision(b.id) {}
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = SashioSlate,
                                                        contentColor = Color.White
                                                    ),
                                                    modifier = Modifier
                                                        .height(34.dp)
                                                        .weight(1.2f)
                                                ) {
                                                    Text(
                                                        "Request Revision",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Button(
                                                    onClick = { bookingToConvert = b },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = SashioGreen,
                                                        contentColor = Color.White
                                                    ),
                                                    modifier = Modifier
                                                        .height(34.dp)
                                                        .weight(1.2f)
                                                ) {
                                                    Text(
                                                        "Accept & Book",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        "Client Accepted" -> {
                                            if (displayPaymentStatus != "success" && grandTotal > 0) {
                                                Button(
                                                    onClick = {
                                                        viewModel.triggerPaystackPayment(
                                                            b.id,
                                                            grandTotal
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = SashioGreen,
                                                        contentColor = Color.White
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(36.dp)
                                                        .testTag("pay_booking_${b.id}")
                                                ) {
                                                    Icon(
                                                        Icons.Default.Payment,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        "Pay Now",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
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
        }
    }
}
}
@Composable
fun ConsultationAndReviewsTab(viewModel: SashioViewModel) {
    val reviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    var consType by remember { mutableStateOf("Schedule Site Inspection") }
    var consDate by remember { mutableStateOf("2026-06-15") }
    var consInstructions by remember { mutableStateOf("") }

    var ratingVal by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    val reviewCategory by remember {
        mutableStateOf("Presidential Luxury Mobile Restrooms")
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.onSurface,
        unfocusedTextColor = colors.onSurface,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        focusedLabelColor = colors.primary,
        unfocusedLabelColor = colors.onSurfaceVariant,
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.outline,
        cursorColor = colors.primary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .padding(bottom = 112.dp)
    ) {
        Text(
            "SALES CONSULTATION HUB",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground
        )

        Text(
            "Book inspections, callback requests or product demonstrations.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Request Consultation / Callback",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                listOf(
                    "Schedule Site Inspection",
                    "Book Consultation",
                    "Request Callback",
                    "Request Product Demonstration"
                ).forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { consType = type }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = consType == type,
                            onClick = { consType = type },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.primary,
                                unselectedColor = colors.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            type,
                            fontSize = 12.sp,
                            color = colors.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = consDate,
                    onValueChange = { consDate = it },
                    label = { Text("Preferred Inspection Date") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = consInstructions,
                    onValueChange = { consInstructions = it },
                    label = { Text("Special Guidelines / Ground coordinates") },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.bookConsultation(
                            consType,
                            consDate,
                            consInstructions
                        ) {
                            consInstructions = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SashioGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        "Schedule and Notify Sales",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
        
                    Spacer(modifier = Modifier.height(24.dp))

        Text(
            "CUSTOMER REVIEWS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground
        )

        Text(
            "Submit ratings and event testimonials directly from the app",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Your Rating:", color = colors.onSurface)

                    (1..5).forEach { stars ->
                        IconButton(
                            onClick = { ratingVal = stars },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (stars <= ratingVal) SashioGold else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reviewComment,
                    onValueChange = { reviewComment = it },
                    label = { Text("Write your testimonial comments...") },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.submitReview(
                            ratingVal,
                            reviewComment,
                            reviewCategory
                        ) {
                            reviewComment = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SashioTeal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        "Submit Public Review",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "SACHIO PROJECT PORTFOLIO",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground
        )

        Text(
            "Showcasing elite completed projects and sanitation delivery sites.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        listOf(
            "Luxury Eko Atlantic Beach Wedding Plot" to
                "Presidential Restroom Sovereign setups serving 600 VIP guests.",
            "Lagos National Tech Summit Forum" to
                "5 Executive Trailer Cabins perfectly installed within the exhibition lot.",
            "Government Wuse II State Banquet" to
                "VIP Flush modules backed with shift cleanup attendants.",
            "Construction Depot Site, Port Harcourt" to
                "10 Heavy Duty Standard chemical toilets managed over solid extraction weeks."
        ).forEach { (proj, info) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SashioSlate),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        proj,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        info,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        reviews.forEach { rev ->
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            rev.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.onSurface
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            (1..rev.rating).forEach { _ ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = SashioGold,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Text(
                        rev.comment,
                        fontSize = 11.sp,
                        color = colors.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "CONTACT & SOCIAL",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground
        )

        Text(
            "Get in touch directly or connect across our platforms.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SashioSlate),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SashioGold,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        "Suite E284 Ikota shopping complex VGC Lekki Ajah",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val cContext = LocalContext.current

                OutlinedButton(
                    onClick = {
                        val i = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.facebook.com/share/17ukrrzVb3/")
                        )
                        cContext.startActivity(i)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Visit Facebook Page")
                }

                OutlinedButton(
                    onClick = {
                        val i = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.instagram.com/sachiomobiletoilets?igsh=NWluaTAzYnMwMXow")
                        )
                        cContext.startActivity(i)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Visit Instagram Profile")
                }

                OutlinedButton(
                    onClick = {
                        val i = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://x.com/mobiletoiletsng")
                        )
                        cContext.startActivity(i)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Visit X (Twitter)")
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}                

