package com.sashio.ops.ui

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.R
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SashioAppContent(viewModel: SashioViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeTransaction by viewModel.activePaystackTransaction.collectAsStateWithLifecycle()
    val lastPaymentStatus by viewModel.lastPaymentStatus.collectAsStateWithLifecycle()
    val bookingSyncMessage by viewModel.bookingSyncMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPaymentConfirmedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(lastPaymentStatus) {
        when (lastPaymentStatus) {
            "success" -> showPaymentConfirmedDialog = true
            "cancelled" -> Toast.makeText(context, "Payment was cancelled. You can complete it anytime.", Toast.LENGTH_LONG).show()
            "pending" -> Toast.makeText(context, "Payment is processing. We will confirm your booking shortly.", Toast.LENGTH_LONG).show()
            "failed" -> Toast.makeText(context, "Payment failed. Please check your card and try again.", Toast.LENGTH_LONG).show()
            "not_found" -> Toast.makeText(context, "We haven't received confirmation from Paystack yet. If you just paid, wait a few seconds and tap \"I've Paid - Check Status\" again.", Toast.LENGTH_LONG).show()
            "check_failed" -> Toast.makeText(context, "Couldn't reach the server to check payment status. Check your connection.", Toast.LENGTH_LONG).show()
            null -> {}
            else -> Toast.makeText(context, "Status: $lastPaymentStatus", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(bookingSyncMessage) {
        when {
            bookingSyncMessage == "synced" -> Toast.makeText(context, "Booking request sent successfully.", Toast.LENGTH_LONG).show()
            bookingSyncMessage == "no_supabase_session" -> Toast.makeText(context, "Booking saved on this device only - please sign out and sign back in to sync it to your account.", Toast.LENGTH_LONG).show()
            bookingSyncMessage?.startsWith("sync_failed") == true -> Toast.makeText(context, "Booking saved on this device, but couldn't reach the server: ${bookingSyncMessage?.removePrefix("sync_failed: ")}", Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    if (showPaymentConfirmedDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentConfirmedDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SashioGreen, modifier = Modifier.size(48.dp)) },
            title = { Text("Payment Confirmed", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Your payment was received and confirmed by Paystack. Your booking is now paid and confirmed.",
                        textAlign = TextAlign.Center,
                        color = SashioTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPaymentConfirmedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SashioLight
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    "splash" -> SplashScreen(viewModel)
                    "login" -> LoginScreen(viewModel)
                    "register" -> RegisterScreen(viewModel)
                    "forgot_password" -> ForgotPasswordScreen(viewModel)
                    "set_new_password" -> SetNewPasswordScreen(viewModel)
                    "client_dashboard" -> ClientDashboard(viewModel, currentUser)
                    "staff_dashboard" -> StaffDashboard(viewModel, currentUser)
                    "admin_dashboard" -> AdminDashboard(viewModel, currentUser)
                    else -> LoginScreen(viewModel)
                }
            }

            activeTransaction?.let { transaction ->
                PaystackCheckoutGatewayModal(
                    transaction = transaction,
                    onSuccess = { ref -> viewModel.simWebpayComplete(ref) },
                    onDismiss = { viewModel.clearActiveTransaction() }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(viewModel: SashioViewModel) {
    var animateStart by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (animateStart) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "SplashAlpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (animateStart) 1f else 0.85f,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutBack),
        label = "SplashScale"
    )

    LaunchedEffect(Unit) {
        animateStart = true
        delay(2800)
        viewModel.setScreen("login")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SashioSlate),
        contentAlignment = Alignment.Center
    ) {
        LuxuryVideoBackgroundEffect()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
        ) {
            Image(
                painter = painterResource(id = R.drawable.sachio_logo),
                contentDescription = "Sachio Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "SACHIO",
                style = MaterialTheme.typography.headlineMedium,
                color = SashioGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Prestige in Every Flush",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                fontStyle = FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = SashioGold,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "INITIALIZING PRESTIGE PROPORTIONS...",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun LuxuryVideoBackgroundEffect() {
    val particleOffsets = remember {
        List(25) {
            Offset(
                (100..900).random().toFloat(),
                (100..1600).random().toFloat()
            )
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(SashioSlate, Color(0xFF111827), Color(0xFF030712))
            )
        )
        particleOffsets.forEach { baseOffset ->
            drawCircle(
                color = SashioGold.copy(alpha = 0.04f),
                radius = 120f,
                center = baseOffset
            )
            drawCircle(
                color = SashioTeal.copy(alpha = 0.15f),
                radius = 4f,
                center = baseOffset
            )
        }
    }
}
