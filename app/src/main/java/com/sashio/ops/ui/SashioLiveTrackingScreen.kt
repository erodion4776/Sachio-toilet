package com.sashio.ops.ui

import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.Booking
import com.sashio.ops.data.LiveTrackingState
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SashioLiveTrackingScreen(
    viewModel: SashioViewModel,
    bookingId: Long,
    onBack: () -> Unit
) {
    val bookings by viewModel.clientBookings.collectAsStateWithLifecycle()
    val booking = bookings.find { it.id == bookingId }

    if (booking == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Error: Booking Reference #QR-$bookingId not found.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) {
                    Text("Back to Dashboard")
                }
            }
        }
        return
    }

    val trackers by viewModel.activeLiveTrackers.collectAsStateWithLifecycle()

    val tracker = trackers[bookingId] ?: LiveTrackingState(
        bookingId = bookingId,
        staffLat = booking.latitude - 0.05,
        staffLng = booking.longitude - 0.04,
        vehicleLat = booking.latitude - 0.048,
        vehicleLng = booking.longitude - 0.038
    )

    var currentTab by remember { mutableStateOf("radar") }
    var zoomLevel by remember { mutableStateOf(1f) }
    var showAlternateRoutes by remember { mutableStateOf(false) }
    var simulateRoadClosure by remember { mutableStateOf(false) }
    var selectedLayer by remember { mutableStateOf("Standard") }

    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SashioSlate)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "LIVE DISPATCH COCKPIT",
                        color = SashioGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        "Ref: #QR-${booking.id} | ${booking.serviceType}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                BookingStatusBadge(status = booking.status)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f)
                .clip(
                    RoundedCornerShape(
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .background(colors.surface)
        ) {
            SashioInteractiveCanvasMap(
                latitude = booking.latitude,
                longitude = booking.longitude,
                staffLat = tracker.staffLat,
                staffLng = tracker.staffLng,
                vehicleLat = tracker.vehicleLat,
                vehicleLng = tracker.vehicleLng,
                mapStyle = selectedLayer,
                showRoute = true,
                alternateRoute = showAlternateRoutes,
                roadClosureSelected = simulateRoadClosure,
                zoomFactor = zoomLevel,
                label = "Event: ${booking.address.split(",").firstOrNull() ?: "Venue"}"
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        selectedLayer = when (selectedLayer) {
                            "Standard" -> "Satellite"
                            "Satellite" -> "Traffic"
                            "Traffic" -> "Operations"
                            else -> "Standard"
                        }
                    },
                    containerColor = colors.surface.copy(alpha = 0.92f),
                    contentColor = colors.onSurface,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = "Change map layer",
                        modifier = Modifier.size(16.dp)
                    )
                }

                FloatingActionButton(
                    onClick = {
                        zoomLevel = (zoomLevel + 0.3f).coerceAtMost(2.5f)
                    },
                    containerColor = colors.surface.copy(alpha = 0.92f),
                    contentColor = colors.onSurface,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Zoom in",
                        modifier = Modifier.size(16.dp)
                    )
                }

                FloatingActionButton(
                    onClick = {
                        zoomLevel = (zoomLevel - 0.3f).coerceAtLeast(0.5f)
                    },
                    containerColor = colors.surface.copy(alpha = 0.92f),
                    contentColor = colors.onSurface,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Zoom out",
                        modifier = Modifier.size(16.dp)
                    )
                }

                FloatingActionButton(
                    onClick = {
                        zoomLevel = 1.0f
                        Toast.makeText(
                            context,
                            "Radar centered on SACHIO hauler.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    containerColor = colors.surface.copy(alpha = 0.92f),
                    contentColor = colors.onSurface,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Reset map",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        showAlternateRoutes = !showAlternateRoutes

                        val msg = if (showAlternateRoutes) {
                            "Alternative routes loaded. Bypass via Coastal road available."
                        } else {
                            "Alternate routes hidden."
                        }

                        Toast.makeText(
                            context,
                            msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAlternateRoutes) {
                            SashioGold
                        } else {
                            colors.surface.copy(alpha = 0.92f)
                        },
                        contentColor = if (showAlternateRoutes) {
                            Color.White
                        } else {
                            colors.onSurface
                        }
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "By-passes",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                                Button(
                    onClick = {
                        simulateRoadClosure = !simulateRoadClosure

                        val msg = if (simulateRoadClosure) {
                            "Simulated Road block detected. GPS has recalculated optimal bypass!"
                        } else {
                            "Route clear."
                        }

                        Toast.makeText(
                            context,
                            msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (simulateRoadClosure) {
                            SashioRed
                        } else {
                            colors.surface.copy(alpha = 0.92f)
                        },
                        contentColor = if (simulateRoadClosure) {
                            Color.White
                        } else {
                            colors.onSurface
                        }
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        Icons.Default.Traffic,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Traffic Alerts",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SashioSlate.copy(alpha = 0.92f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .width(170.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SashioGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "ESTIMATED ARRIVAL",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        if (
                            booking.status == "COMPLETED" ||
                            booking.status == "Completed"
                        ) {
                            "Delivered"
                        } else if (
                            booking.status in listOf(
                                "ARRIVED",
                                "SETUP IN PROGRESS",
                                "SERVICE ACTIVE"
                            )
                        ) {
                            "Arrived"
                        } else {
                            "${tracker.etaMinutes} MINS"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        if (
                            booking.status in listOf(
                                "ARRIVED",
                                "SETUP IN PROGRESS",
                                "SERVICE ACTIVE",
                                "COMPLETED",
                                "Completed"
                            )
                        ) {
                            "Setup active"
                        } else {
                            "Distance: %.1f km remaining".format(
                                tracker.distanceRemainingKm
                            )
                        },
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("radar", Icons.Default.Map, "Radar"),
                Triple("chat", Icons.Default.Chat, "Chat"),
                Triple("log", Icons.Default.History, "Activity"),
                Triple("crew", Icons.Default.Person, "Crew & Rig")
            ).forEach { (tabId, icon, label) ->

                val selected = currentTab == tabId

                Button(
                    onClick = {
                        currentTab = tabId
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) {
                            SashioSlate
                        } else {
                            colors.surface
                        },
                        contentColor = if (selected) {
                            SashioGold
                        } else {
                            colors.onSurface
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        Color.LightGray.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (selected) {
                                SashioGold
                            } else {
                                colors.onSurface
                            }
                        )

                        Text(
                            label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .background(colors.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (currentTab) {
                "radar" -> LiveRadarDetailTab(viewModel, booking, tracker)
                "chat" -> LiveChatInteractiveTab(viewModel, booking, tracker)
                "log" -> LiveActivityLogTab(viewModel, booking, tracker)
                "crew" -> AssignedCrewTab(viewModel, booking, tracker)
            }
        }
    }
}
@Composable
fun SashioInteractiveCanvasMap(
    latitude: Double,
    longitude: Double,
    staffLat: Double,
    staffLng: Double,
    vehicleLat: Double,
    vehicleLng: Double,
    mapStyle: String = "Standard",
    showRoute: Boolean = true,
    alternateRoute: Boolean = false,
    roadClosureSelected: Boolean = false,
    zoomFactor: Float = 1.0f,
    label: String = ""
) {
    val context = LocalContext.current

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(latitude, longitude))
        }
    }

    LaunchedEffect(latitude, longitude, zoomFactor) {
        mapView.controller.setCenter(GeoPoint(latitude, longitude))
        mapView.controller.setZoom(
            (15.0 * zoomFactor).coerceIn(3.0, 20.0)
        )
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize(),
        update = { map ->
            map.overlays.clear()

            val venueMarker = Marker(map).apply {
                position = GeoPoint(latitude, longitude)
                title = if (label.isNotBlank()) label else "Booking venue"
                snippet = "Booking location"
                setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
                )
            }

            map.overlays.add(venueMarker)
            map.invalidate()
        }
    )

    DisposableEffect(mapView) {
        mapView.onResume()

        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }
}
@Composable
fun LiveRadarDetailTab(
    viewModel: SashioViewModel,
    booking: Booking,
    tracker: LiveTrackingState
) {
    val colors = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "Active Rig Speed",
                            color = colors.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            if (booking.status in listOf(
                                    "ARRIVED",
                                    "SETUP IN PROGRESS",
                                    "SERVICE ACTIVE",
                                    "COMPLETED",
                                    "Completed"
                                )
                            ) "0 km/h (Parked)" else "42 km/h",
                            color = colors.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "Road Conditions",
                            color = colors.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            tracker.trafficLevel,
                            color = if (tracker.trafficLevel == "Heavy") {
                                SashioRed
                            } else {
                                colors.onSurface
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.9f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "Weather",
                            color = colors.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Cloudy, 29°C",
                            color = colors.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    colors.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "SERVICE DELIVERY STAGES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        "Prestige workflow progress track",
                        fontSize = 10.sp,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val stagesList = listOf(
                        "Quotation Requested" to "Client submitted initial sanitation parameters.",
                        "Quotation Approved" to "Quotation agreed. Logistics route vectors locked.",
                        "Payment Confirmed" to "Paystack transaction verified. Order released.",
                        "Booking Confirmed" to "Rig loading and sanitization check validated.",
                        "Staff Assigned" to "Supervising attendant allocated to field dispatch.",
                        "Vehicle Dispatched" to "VIP restraint cabin hauler has departed depot base.",
                        "En Route" to "Cruising to delivery pin optimal routes.",
                        "Arrived at Location" to "Landed at customer coordinates. Performing positioning.",
                        "Setup In Progress" to "Configuring waste pressure hoses & stabilizer brackets.",
                        "Service Active" to "Cabins are fully live, powered, scented and active.",
                        "Completed" to "Cabins serviced, packed and secured. Prestige accomplished."
                    )

                    val currentBookingStatus = booking.status

                    val activeIndex = when (currentBookingStatus) {
                        "Quotation Requested",
                        "Under Review",
                        "Quotation Generated" -> 0

                        "Quotation Sent",
                        "Client Accepted" -> 1

                        "Awaiting Payment",
                        "Payment Confirmed" -> 2

                        "Booking Confirmed" -> 3
                        "Staff Assigned" -> 4
                        "SERVICE DELIVERED" -> 5
                        "IN TRANSIT" -> 6
                        "ARRIVED" -> 7
                        "SETUP IN PROGRESS" -> 8
                        "SERVICE ACTIVE" -> 9
                        "COMPLETED",
                        "Completed" -> 10

                        else -> 3
                    }

                    stagesList.forEachIndexed { idx, (title, desc) ->
                        val isDone = idx <= activeIndex
                        val isCurrent = idx == activeIndex

                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDone) {
                                                if (isCurrent) SashioGold else SashioGreen
                                            } else {
                                                colors.outline.copy(alpha = 0.5f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone && !isCurrent) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(10.dp),
                                            tint = Color.White
                                        )
                                    } else if (isCurrent) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }

                                if (idx < stagesList.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(24.dp)
                                            .background(
                                                if (idx < activeIndex) {
                                                    SashioGreen
                                                } else {
                                                    colors.outline.copy(alpha = 0.4f)
                                                }
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent || isDone) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Medium
                                    },
                                    color = when {
                                        isCurrent -> SashioGold
                                        isDone -> colors.onSurface
                                        else -> colors.onSurfaceVariant
                                    }
                                )
                                Text(
                                    desc,
                                    fontSize = 9.sp,
                                    color = if (isDone) {
                                        colors.onSurfaceVariant
                                    } else {
                                        colors.onSurfaceVariant.copy(alpha = 0.6f)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
                                                
@Composable
fun LiveChatInteractiveTab(
    viewModel: SashioViewModel,
    booking: Booking,
    tracker: LiveTrackingState
) {
    var txtVal by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SashioGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Online Attendant Dispatch Support",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }
                Text(
                    "Live Link",
                    fontSize = 9.sp,
                    color = SashioGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tracker.messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No messages yet. Send a message to coordinate with the crew.",
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                items(tracker.messages) { msg ->
                    val isClient = msg.sender == "Client"

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isClient) {
                            Alignment.End
                        } else {
                            Alignment.Start
                        }
                    ) {
                        Text(
                            msg.sender,
                            fontSize = 9.sp,
                            color = if (isClient) SashioGold else SashioTeal,
                            fontWeight = FontWeight.Bold
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isClient) {
                                    SashioGold.copy(alpha = 0.15f)
                                } else {
                                    colors.surfaceVariant
                                }
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.widthIn(max = 260.dp)
                        ) {
                            Text(
                                msg.text,
                                fontSize = 12.sp,
                                color = colors.onSurface,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Text(
                            formatMessageTimestamp(msg.timestamp.toString()),
                            fontSize = 8.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = txtVal,
                onValueChange = { txtVal = it },
                placeholder = {
                    Text("Message attendant crew...", fontSize = 11.sp)
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (txtVal.isNotBlank()) {
                        viewModel.sendClientChatMessage(booking.id, txtVal.trim())
                        txtVal = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SashioGold)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = SashioSlate
                )
            }
        }
    }
}

@Composable
fun LiveActivityLogTab(
    viewModel: SashioViewModel,
    booking: Booking,
    tracker: LiveTrackingState
) {
    val colors = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (tracker.logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No dispatch activities recorded yet.",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            items(tracker.logs) { log ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SashioGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SashioGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                log.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = colors.onSurface
                            )
                            Text(
                                log.description,
                                fontSize = 10.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssignedCrewTab(
    viewModel: SashioViewModel,
    booking: Booking,
    tracker: LiveTrackingState
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "ASSIGNED SUPERVISING ATTENDANT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SashioGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SashioGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            tracker.staffName.firstOrNull()?.toString() ?: "O",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SashioSlate
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            tracker.staffName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.onSurface
                        )
                        Text(
                            "${tracker.staffPosition} | ${tracker.staffExperience} yrs experience",
                            fontSize = 10.sp,
                            color = colors.onSurfaceVariant
                        )
                        Text(
                            tracker.staffPhone,
                            fontSize = 10.sp,
                            color = SashioTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:${tracker.staffPhone}")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Unable to open the phone dialer.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SashioSlate
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Call Crew",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val phone = tracker.staffPhone.filter { it.isDigit() }
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://api.whatsapp.com/send?phone=$phone")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Unable to open WhatsApp.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Text("WhatsApp", fontSize = 10.sp)
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "DISPATCHED VEHICLE & RIG",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SashioGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    tracker.vehicleType,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.onSurface
                )
                Text(
                    "License Plate: ${tracker.vehicleNumber}",
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariant
                )
                Text(
                    "Traffic State: ${tracker.trafficLevel}",
                    fontSize = 10.sp,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}
