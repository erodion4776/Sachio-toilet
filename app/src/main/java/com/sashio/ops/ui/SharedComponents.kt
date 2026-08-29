package com.sashio.ops.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sashio.ops.R
import com.sashio.ops.data.CreatePaymentResponse
import com.sashio.ops.data.UserProfile
import com.sashio.ops.ui.theme.*
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
    val mapColor = when (mapStyle) {
        "Satellite" -> Color(0xFF0F172A)
        "Traffic" -> Color(0xFF1E293B)
        "Operations" -> Color(0xFF111827)
        else -> Color(0xFF1E293B)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(mapColor)
                .pointerInput(onMarkerChanged) {
                    if (onMarkerChanged != null) {
                        detectTapGestures { offset ->
                            val outLat = 6.4 + (offset.y / 25000)
                            val outLng = 3.3 + (offset.x / 25000)
                            onMarkerChanged(outLat, outLng)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            for (i in 1..8) {
                val progressX = width * (i.toFloat() / 9f)
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(progressX, 0f),
                    end = Offset(progressX, height),
                    strokeWidth = 1f
                )
            }
            for (i in 1..5) {
                val progressY = height * (i.toFloat() / 6f)
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(0f, progressY),
                    end = Offset(width, progressY),
                    strokeWidth = 1f
                )
            }

            if (mapStyle == "Traffic") {
                drawLine(color = Color(0xFFEF4444), start = Offset(0f, height * 0.35f), end = Offset(width, height * 0.35f), strokeWidth = 3f)
                drawLine(color = Color(0xFF22C55E), start = Offset(0f, height * 0.45f), end = Offset(width, height * 0.45f), strokeWidth = 4f)
                drawLine(color = Color(0xFFF59E0B), start = Offset(width * 0.4f, 0f), end = Offset(width * 0.4f, height), strokeWidth = 2f)
            } else if (mapStyle == "Operations") {
                drawCircle(color = SashioGold.copy(alpha = 0.05f), center = Offset(width / 2, height / 2), radius = 50.dp.toPx(), style = Stroke(width = 1.dp.toPx()))
                drawCircle(color = SashioGold.copy(alpha = 0.03f), center = Offset(width / 2, height / 2), radius = 100.dp.toPx(), style = Stroke(width = 1.dp.toPx()))
                drawCircle(color = SashioGold.copy(alpha = 0.02f), center = Offset(width / 2, height / 2), radius = 150.dp.toPx(), style = Stroke(width = 1.dp.toPx()))
            }

            drawLine(color = Color.DarkGray, start = Offset(0f, height * 0.4f), end = Offset(width, height * 0.45f), strokeWidth = 10f)
            drawCircle(color = SashioTeal.copy(alpha = 0.13f), center = Offset(width * 0.2f, height * 0.8f), radius = 35.dp.toPx())

            val markerX = (((longitude - 3.3) * 25000).toFloat()).coerceIn(30f, width - 30f)
            val markerY = (((latitude - 6.4) * 25000).toFloat()).coerceIn(30f, height - 30f)

            val vX = vehicleLat?.let { (((vehicleLng ?: 3.3) - 3.3) * 25000).toFloat().coerceIn(30f, width - 30f) }
            val vY = vehicleLat?.let { (((vehicleLat) - 6.4) * 25000).toFloat().coerceIn(30f, height - 30f) }

            val sX = staffLat?.let { (((staffLng ?: 3.3) - 3.3) * 25000).toFloat().coerceIn(30f, width - 30f) }
            val sY = staffLat?.let { (((staffLat) - 6.4) * 25000).toFloat().coerceIn(30f, height - 30f) }

            if (showRoute && vX != null && vY != null) {
                drawLine(
                    color = SashioGold,
                    start = Offset(vX, vY),
                    end = Offset(markerX, markerY),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            drawCircle(color = SashioGold.copy(alpha = 0.25f), center = Offset(markerX, markerY), radius = 18.dp.toPx())
            drawCircle(color = SashioGold, center = Offset(markerX, markerY), radius = 6.dp.toPx())
            drawCircle(color = Color.White, center = Offset(markerX, markerY), radius = 2.dp.toPx())

            if (vX != null && vY != null) {
                drawCircle(color = SashioTeal.copy(alpha = 0.3f), center = Offset(vX, vY), radius = 15.dp.toPx())
                drawCircle(color = SashioTeal, center = Offset(vX, vY), radius = 7.dp.toPx())
                drawRect(color = Color.White, topLeft = Offset(vX - 3.dp.toPx(), vY - 3.dp.toPx()), size = Size(6.dp.toPx(), 6.dp.toPx()))
            }

            if (sX != null && sY != null) {
                drawCircle(color = Color.White.copy(alpha = 0.4f), center = Offset(sX, sY), radius = 10.dp.toPx())
                drawCircle(color = SashioGreen, center = Offset(sX, sY), radius = 4.dp.toPx())
            }
        }

        if (label.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun strokeWidthLimit(width: Float): Stroke {
    return Stroke(width = width)
}

fun formatMessageTimestamp(timestampOrIso: String): String {
    if (timestampOrIso.isBlank()) return ""
    return try {
        // If it's a numeric millisecond timestamp
        val millis = timestampOrIso.toLongOrNull()
        if (millis != null) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
        } else {
            // ISO-8601 string parse with API fallback
            try {
                val instant = Instant.parse(
                    if (timestampOrIso.contains("+") || timestampOrIso.endsWith("Z")) timestampOrIso
                    else "${timestampOrIso}Z"
                )
                DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault()).format(instant)
            } catch (e: Throwable) {
                timestampOrIso
            }
        }
    } catch (e: Exception) {
        timestampOrIso
    }
}
