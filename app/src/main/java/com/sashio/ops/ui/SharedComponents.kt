package com.sashio.ops.ui

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.rememberNestedScrollInteropConnection
import coil.compose.AsyncImage
import com.sashio.ops.R
import com.sashio.ops.data.CreatePaymentResponse
import com.sashio.ops.data.UserProfile
import com.sashio.ops.ui.theme.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun DashboardHeader(
    roleName: String,
    user: UserProfile?,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SashioSlate),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                AsyncImage(
                    model = R.drawable.sachio_logo,
                    contentDescription = "Sachio Logo",
                    modifier = Modifier.size(40.dp).padding(end = 12.dp)
                )
                Column {
                    Text(roleName.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                    Text(user?.name ?: "Sachio Client", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(user?.email ?: "client@sashio.com", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onSettingsClick,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.testTag("settings_header_btn")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }

                IconButton(
                    onClick = onLogout,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.testTag("logout_header_btn")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sign out", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun BookingStatusBadge(status: String) {
    val (lbl, bg, fore) = when (status) {
        "Quotation Requested" -> Triple("PENDING REVIEW", SashioGold.copy(alpha = 0.15f), SashioGold)
        "Under Review" -> Triple("UNDER REVIEW", SashioGold.copy(alpha = 0.15f), SashioGold)
        "Quotation Generated" -> Triple("QUOTE READY", SashioTeal.copy(alpha = 0.15f), SashioTeal)
        "Quotation Sent" -> Triple("QUOTE ISSUED", SashioTeal.copy(alpha = 0.15f), SashioTeal)
        "Client Accepted" -> Triple("ACCEPTED / UNPAID", SashioBlue.copy(alpha = 0.15f), SashioBlue)
        "Awaiting Payment" -> Triple("AWAITING PAYMENT", SashioBlue.copy(alpha = 0.15f), SashioBlue)
        "Payment Confirmed" -> Triple("PAID", SashioGreen.copy(alpha = 0.15f), SashioGreen)
        "Booking Confirmed" -> Triple("BOOKED & CLEARED", SashioGreen.copy(alpha = 0.15f), SashioGreen)
        "Staff Assigned" -> Triple("STAFF ASSIGNED", SashioTeal.copy(alpha = 0.15f), SashioTeal)
        "IN TRANSIT" -> Triple("EN ROUTE", SashioGold.copy(alpha = 0.15f), SashioGold)
        "ARRIVED" -> Triple("ARRIVED ON SITE", SashioTeal.copy(alpha = 0.15f), SashioTeal)
        "SETUP IN PROGRESS", "SETUP" -> Triple("SETUP ACTIVE", SashioGold.copy(alpha = 0.15f), SashioGold)
        "SERVICE DELIVERED", "SERVICE ACTIVE", "ACTIVE" -> Triple("ACTIVE ON SITE", SashioGreen.copy(alpha = 0.15f), SashioGreen)
        "COMPLETED", "Completed", "completed" -> Triple("COMPLETED", SashioGreen.copy(alpha = 0.15f), SashioGreen)
        "Cancelled", "CANCELLED" -> Triple("CANCELLED", SashioRed.copy(alpha = 0.15f), SashioRed)
        else -> Triple(status.uppercase(), SashioTextMuted.copy(alpha = 0.15f), SashioTextMuted)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = lbl,
            color = fore,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PaystackCheckoutGatewayModal(
    transaction: CreatePaymentResponse,
    onSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var modeStep by remember { mutableStateOf("choices") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = SashioBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Secure Paystack Gateway", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("paystack_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("paystack_checkout_dialog")) {
                HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                when (modeStep) {
                    "choices" -> {
                        Text("Authorized Payment Verification Session:")
                        Text("Invoice Reference: ${transaction.reference}", fontSize = 11.sp, color = SashioTextMuted)
                        Spacer(modifier = Modifier.height(10.dp))

                        Card(colors = CardDefaults.cardColors(containerColor = SashioSlate), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SECURED TOTAL CHARGE", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                Text("₦%,.2f".format(transaction.amount), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(transaction.authorizationUrl))
                                context.startActivity(intent)
                                modeStep = "waiting"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioTeal),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("paystack_submit_gateway")
                        ) {
                            Text("PAY SECURELY WITH CARD", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    "waiting" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = SashioTeal, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Complete your payment in the browser page that opened.", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text("Once finished, tap below to check your verified payment status.", fontSize = 11.sp, color = SashioTextMuted, textAlign = TextAlign.Center)

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onSuccess(transaction.reference) },
                                colors = ButtonDefaults.buttonColors(containerColor = SashioGreen),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("paystack_finish")
                            ) {
                                Text("I've Paid - Check Status", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(transaction.authorizationUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reopen Payment Page")
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 100% FREE FOREVER OpenStreetMap Implementation for SACHIO
 * Supports interactive map gestures, tap-to-pin, auto-centering on typed/detected location, and live routing.
 */
@Composable
fun SashioCanvasMap(
    latitude: Double,
    longitude: Double,
    onMarkerChanged: ((Double, Double) -> Unit)? = null,
    staffLat: Double? = null,
    staffLng: Double? = null,
    vehicleLat: Double? = null,
    vehicleLng: Double? = null,
    mapStyle: String = "Standard",
    showRoute: Boolean = false,
    label: String = ""
) {
    val context = LocalContext.current
    val destLat = if (latitude != 0.0) latitude else 9.0820
    val destLng = if (longitude != 0.0) longitude else 8.6753
    var lastCenter by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(true)
                    isTilesScaledToDpi = true

                    val mapController = controller
                    mapController.setZoom(6.0)
                    mapController.setCenter(GeoPoint(destLat, destLng))

                    // Tap-to-pick pin drop gesture
                    if (onMarkerChanged != null) {
                        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                p?.let {
                                    onMarkerChanged(it.latitude, it.longitude)
                                }
                                return true
                            }
                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                        })
                        overlays.add(eventsOverlay)
                    }
                }
            },
            update = { mapView ->
                // Clear old overlays (excluding the tap events overlay)
                mapView.overlays.removeAll { it is Marker || it is Polyline }

                val destPoint = GeoPoint(destLat, destLng)

                // 1. Destination Marker (Event Venue Pin)
                val destMarker = Marker(mapView).apply {
                    position = destPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = label.ifBlank { "Event Venue Location" }
                    snippet = "GPS: %.4f, %.4f".format(destLat, destLng)
                }
                mapView.overlays.add(destMarker)

                // 2. Vehicle / Dispatch Hauler Marker
                if (vehicleLat != null && vehicleLng != null) {
                    val vehPoint = GeoPoint(vehicleLat, vehicleLng)
                    val vehMarker = Marker(mapView).apply {
                        position = vehPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "SACHIO VIP Hauler"
                    }
                    mapView.overlays.add(vehMarker)

                    // 3. Vector Route Polyline connecting Vehicle -> Venue
                    if (showRoute) {
                        val routeLine = Polyline(mapView).apply {
                            outlinePaint.color = android.graphics.Color.rgb(210, 204, 70) // SachioMustard / Gold
                            outlinePaint.strokeWidth = 10f
                            outlinePaint.strokeCap = Paint.Cap.ROUND
                            addPoint(vehPoint)
                            addPoint(destPoint)
                        }
                        mapView.overlays.add(routeLine)
                    }
                }

                // Recenter only when the selected coordinates actually change.
                // Do not fight the user's pan/zoom gestures on ordinary recompositions.
                val requestedCenter = Pair(destLat, destLng)
                if (lastCenter != requestedCenter) {
                    mapView.controller.animateTo(destPoint)
                    lastCenter = requestedCenter
                }
                mapView.invalidate()
            },
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(rememberNestedScrollInteropConnection())
        )

        if (label.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

fun strokeWidthLimit(width: Float): Stroke = Stroke(width = width)

fun formatMessageTimestamp(timestampOrIso: String): String {
    if (timestampOrIso.isBlank()) return ""
    return try {
        val millis = timestampOrIso.toLongOrNull()
        if (millis != null) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
        } else {
            val instant = Instant.parse(
                if (timestampOrIso.contains("+") || timestampOrIso.endsWith("Z")) timestampOrIso
                else "${timestampOrIso}Z"
            )
            DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault()).format(instant)
        }
    } catch (e: Exception) {
        timestampOrIso
    }
}
