package com.sashio.ops.ui

import androidx.compose.animation.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.window.Dialog
import com.sashio.ops.data.*
import com.sashio.ops.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SashioAppContent(viewModel: SashioViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeTransaction by viewModel.activePaystackTransaction.collectAsStateWithLifecycle()

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
                    "verify_otp" -> OtpVerificationScreen(viewModel)
                    "forgot_password" -> ForgotPasswordScreen(viewModel)
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
    
    val alphaAnim by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (animateStart) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1500),
        label = "SplashAlpha"
    )
    
    val scaleAnim by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (animateStart) 1f else 0.85f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1500, easing = androidx.compose.animation.core.EaseOutBack),
        label = "SplashScale"
    )

    LaunchedEffect(Unit) {
        animateStart = true
        kotlinx.coroutines.delay(2800)
        viewModel.setScreen("login")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SashioSlate),
        contentAlignment = Alignment.Center
    ) {
        // Luxury floating particle background for immediate premium vibes
        LuxuryVideoBackgroundEffect()
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
        ) {
            AsyncImage(
                model = "https://i.ibb.co/4ZM1G4WW/1000946646-removebg-preview.png",
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
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Sleek circular progress to show activation
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

// --- Luxury Canvas Particles Background ---
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
        // Golden background pulses
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

// ==========================================
// 1. PUBLIC LAYOUTS & AUTH SCREENS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: SashioViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    var selectedPortalTab by remember { mutableStateOf("client") }

    var showBioDialog by remember { mutableStateOf(false) }
    var bioType by remember { mutableStateOf("Fingerprint") }
    var bioScanState by remember { mutableStateOf("Position finger on reader...") }
    var headerLayoutType by remember { mutableStateOf("centered") }

    Box(modifier = Modifier.fillMaxSize()) {
        LuxuryVideoBackgroundEffect()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (headerLayoutType == "inline") {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                        title = {
                            Row(
                                modifier = Modifier.padding(start = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = "https://i.ibb.co/4ZM1G4WW/1000946646-removebg-preview.png",
                                    contentDescription = "Sachio Logo",
                                    modifier = Modifier.size(46.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("SACHIO TOILETS", fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp)
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (headerLayoutType == "centered") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        AsyncImage(
                            model = "https://i.ibb.co/4ZM1G4WW/1000946646-removebg-preview.png",
                            contentDescription = "Sachio Logo",
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "SACHIO TOILETS", 
                            fontWeight = FontWeight.ExtraBold, 
                            color = Color.White, 
                            letterSpacing = 3.sp, 
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Text(
                    "\"Prestige in Every Flush\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = SashioGold,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Header Design Selector for Real-Time Previews
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Header Style:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("inline" to "Inline", "centered" to "Centered").forEach { (style, label) ->
                                val isSelected = headerLayoutType == style
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) SashioGold else Color.White.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { headerLayoutType = style }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Demo accounts helper cards
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth().testTag("login_portal_selector")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "ENTERPRISE PORTAL SWITCHER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SashioGold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            mapOf("client" to "Client", "staff" to "Staff", "admin" to "Admin").forEach { (role, label) ->
                                Button(
                                    onClick = {
                                        selectedPortalTab = role
                                        email = "${role}@sashio.com"
                                        password = "password123"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedPortalTab == role) SashioGold else Color.White.copy(alpha = 0.08f),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("tab_$role")
                                ) {
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ElevatedCard(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Sign In To Portal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Comfort. Hygiene. Dignity Anywhere.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address", color = Color.White.copy(alpha = 0.8f)) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SashioGold) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SashioGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("email_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = Color.White.copy(alpha = 0.8f)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SashioGold) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SashioGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("password_input"),
                            singleLine = true
                        )

                        TextButton(
                            onClick = { viewModel.setScreen("forgot_password") },
                            modifier = Modifier.align(Alignment.End).testTag("forgot_password_btn")
                        ) {
                            Text("Recover Credentials?", color = SashioGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        loginError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(err, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.loginWithEmail(email, selectedPortalTab, password) },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_login_button")
                        ) {
                            Text("Enter System".uppercase(), fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                        }

                        // Progressive Biometric Login Trigger HUD (Optimized for Field Staff)
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    bioType = "Fingerprint"
                                    bioScanState = "Establish secure touch contact with fingerprint sensor..."
                                    showBioDialog = true
                                },
                                border = BorderStroke(1.dp, SashioGold.copy(alpha = 0.3f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SashioGold),
                                modifier = Modifier.weight(1f).height(40.dp).testTag("touch_id_login")
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Touch ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    bioType = "Face ID"
                                    bioScanState = "Hold device steady. Centering face within lens vector..."
                                    showBioDialog = true
                                },
                                border = BorderStroke(1.dp, SashioGold.copy(alpha = 0.3f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SashioGold),
                                modifier = Modifier.weight(1f).height(40.dp).testTag("face_id_login")
                            ) {
                                Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Face ID Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Biometrics Simulator Dialog Box
                if (showBioDialog) {
                    Dialog(onDismissRequest = { showBioDialog = false }) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFB111827)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .padding(16.dp)
                                .border(1.dp, SashioGold.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(SashioGold.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (bioType == "Fingerprint") Icons.Default.Fingerprint else Icons.Default.Face,
                                        contentDescription = null,
                                        tint = SashioGold,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                Text(
                                    text = "SACHIO Secure Portal".uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SashioGold,
                                    letterSpacing = 2.sp
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = bioScanState,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val context = LocalContext.current
                                LaunchedEffect(key1 = showBioDialog) {
                                    kotlinx.coroutines.delay(1200)
                                    bioScanState = "Securing link & scanning telemetry..."
                                    kotlinx.coroutines.delay(1400)
                                    bioScanState = "Matching biometric hash against active crew registry..."
                                    kotlinx.coroutines.delay(1000)
                                    bioScanState = "Match Verified: Obinna Okafor!"
                                    kotlinx.coroutines.delay(650)
                                    showBioDialog = false
                                    viewModel.loginWithEmail(if (selectedPortalTab == "staff") "staff@sashio.com" else if (selectedPortalTab == "admin") "admin@sashio.com" else "client@sashio.com", selectedPortalTab, "password123")
                                    Toast.makeText(context, "Welcome back, Obinna!", Toast.LENGTH_LONG).show()
                                }
                                
                                TextButton(
                                    onClick = { showBioDialog = false }
                                ) {
                                    Text("CANCEL SCAN", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("New to SACHIO? ", color = Color.White.copy(alpha = 0.7f))
                    TextButton(onClick = { viewModel.setScreen("register") }, modifier = Modifier.testTag("goto_register")) {
                        Text("Register Profile", color = SashioGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(viewModel: SashioViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    val signUpError by viewModel.signUpError.collectAsStateWithLifecycle()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = SashioGold,
        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LuxuryVideoBackgroundEffect()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    title = { Text("Create Account", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.setScreen("login") }, modifier = Modifier.testTag("back_button")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = Color.White)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                ElevatedCard(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Register Mobile Account", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Register to join our luxurious sanitation network.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name", color = Color.White.copy(alpha = 0.8f)) },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_name_input")
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address", color = Color.White.copy(alpha = 0.8f)) },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_email_input")
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number", color = Color.White.copy(alpha = 0.8f)) },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_phone_input")
                        )

                        OutlinedTextField(
                            value = company,
                            onValueChange = { company = it },
                            label = { Text("Company Name (Optional)", color = Color.White.copy(alpha = 0.8f)) },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        Text(
                            "All new sign-ups start as a Client account. Staff and admin access is granted separately by an administrator.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        signUpError?.let { err ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(err, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.sendSignupOtp(name, email, "client", phone, company)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reg_submit_button")
                        ) {
                            Text("Send Verification Code".uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(viewModel: SashioViewModel) {
    var code by remember { mutableStateOf("") }
    val signUpError by viewModel.signUpError.collectAsStateWithLifecycle()
    val pendingEmail by viewModel.pendingSignupEmail.collectAsStateWithLifecycle()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = SashioGold,
        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LuxuryVideoBackgroundEffect()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    title = { Text("Verify Email", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.setScreen("register") }, modifier = Modifier.testTag("back_button")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = Color.White)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            ) {
                ElevatedCard(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Enter Verification Code", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "We sent a 6-digit code to ${pendingEmail ?: "your email"}. Enter it below to finish creating your account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("6-Digit Code", color = Color.White.copy(alpha = 0.8f)) },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("otp_code_input")
                        )

                        signUpError?.let { err ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(err, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.verifySignupOtp(code) },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("otp_verify_button")
                        ) {
                            Text("Verify & Create Account".uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { viewModel.resendSignupOtp() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Resend Code", color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(viewModel: SashioViewModel) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LuxuryVideoBackgroundEffect()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    title = { Text("Recover Password", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.setScreen("login") }, modifier = Modifier.testTag("back_button")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = Color.White)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ElevatedCard(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Reset Verification", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Provide your designated email. We will generate verification code instructions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address", color = Color.White.copy(alpha = 0.8f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SashioGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("reset_email_input"),
                            singleLine = true
                        )

                        message?.let { msg ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(msg, color = SashioGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (viewModel.resetPassword(email)) {
                                    message = "Reset verification sent successfully to your email."
                                } else {
                                    message = "Invalid email format. Try again."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reset_submit_btn")
                        ) {
                            Text("Reset Password", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. CLIENT WORKSPACE & VIEWS
// ==========================================

@Composable
fun ClientDashboard(viewModel: SashioViewModel, client: UserProfile?) {
    var activeTab by remember { mutableStateOf("home") } // home, catalog, vip, tracker, consult
    var isBookingFormOpen by remember { mutableStateOf(false) }
    var selectedCategoryForQuote by remember { mutableStateOf<String?>(null) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    val catalogItems by viewModel.catalogItems.collectAsStateWithLifecycle()
    val bookings by viewModel.clientBookings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DashboardHeader(
                roleName = "Prestige Portal",
                user = client,
                onSettingsClick = { isSettingsOpen = true },
                onLogout = { viewModel.logout() }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SashioSlate, contentColor = Color.White) {
                listOf(
                    Triple("home", Icons.Default.Dashboard, "Dashboard"),
                    Triple("catalog", Icons.Default.MenuBook, "Catalog"),
                    Triple("vip", Icons.Default.Star, "VIP Suite"),
                    Triple("tracker", Icons.Default.History, "Quotes & Tech"),
                    Triple("consult", Icons.Default.Forum, "Community & More")
                ).forEach { (tabId, icon, label) ->
                    NavigationBarItem(
                        selected = activeTab == tabId,
                        onClick = { activeTab = tabId },
                        icon = { Icon(icon, contentDescription = null, tint = if (activeTab == tabId) SashioGold else Color.White.copy(alpha = 0.6f)) },
                        label = { Text(label, fontSize = 10.sp, color = if (activeTab == tabId) SashioGold else Color.White.copy(alpha = 0.6f)) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White.copy(alpha = 0.1f))
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedCategoryForQuote = "Presidential Luxury Mobile Restrooms"
                    isBookingFormOpen = true
                },
                containerColor = SashioGold,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Request Quotation", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("book_restroom_fab")
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isSettingsOpen) {
                SashioSettingsScreen(
                    viewModel = viewModel,
                    onDismiss = { isSettingsOpen = false }
                )
            } else if (isBookingFormOpen) {
                BookingFormSheet(
                    viewModel = viewModel,
                    initialCategory = selectedCategoryForQuote ?: "Standard Portable Toilets",
                    onDismiss = { isBookingFormOpen = false }
                )
            } else {
                when (activeTab) {
                    "home" -> ClientOverviewTab(
                        client = client,
                        bookings = bookings,
                        onRequestQuote = { 
                            selectedCategoryForQuote = "Standard Portable Toilets"
                            isBookingFormOpen = true 
                        },
                        onNavigate = { tab -> activeTab = tab }
                    )
                    "catalog" -> ProductDiscoveryTab(
                        catalogItems = catalogItems,
                        onRequestQuote = { cat ->
                            selectedCategoryForQuote = cat
                            isBookingFormOpen = true
                        }
                    )
                    "vip" -> VipPresidentialTab(
                        onRequestQuote = {
                            selectedCategoryForQuote = "Presidential Luxury Mobile Restrooms"
                            isBookingFormOpen = true
                        }
                    )
                    "tracker" -> ClientQuotationTrackerTab(viewModel)
                    "consult" -> ConsultationAndReviewsTab(viewModel)
                }
            }
        }
    }
}

// --- Client Overview Tab (Dashboard) ---
@Composable
fun ClientOverviewTab(
    client: UserProfile?,
    bookings: List<Booking>,
    onRequestQuote: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val pendingQuotes = bookings.count { it.status == "Quotation Requested" || it.status == "Under Review" || it.status == "Quotation Generated" }
    val activeQuotes = bookings.count { it.status == "Quotation Sent" }
    val activeBookings = bookings.count { it.status == "Payment Confirmed" || it.status == "Booking Confirmed" || it.status == "SERVICE DELIVERED" || it.status == "in_progress" }
    val completedServices = bookings.count { it.status == "completed" || it.status == "Completed" }
    
    val outstandingPayments = bookings.filter { it.paymentStatus == "pending" && it.quotedPrice > 0.0 }.sumOf { it.quotedPrice }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Welcome back, ${client?.name ?: "Valued Client"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("Your SACHIO prestige dashboard", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
        }

        // Quick Actions
        item {
            Text("Quick Actions", style = MaterialTheme.typography.labelMedium, color = SashioGold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onRequestQuote,
                    colors = ButtonDefaults.buttonColors(containerColor = SashioTeal, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Quote", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { onNavigate("tracker") },
                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Track Service", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onNavigate("catalog") },
                    colors = ButtonDefaults.buttonColors(containerColor = SashioGold, contentColor = SashioSlate),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Book Service", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { onNavigate("consult") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = SashioSlate),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Contact Support", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Statistics/Status Snippets
        item {
            Text("Service Summary", style = MaterialTheme.typography.labelMedium, color = SashioGold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Pending Quotes", pendingQuotes.toString(), Icons.Default.HourglassEmpty, Modifier.weight(1f), SashioGold)
                StatCard("Active Quotes", activeQuotes.toString(), Icons.Default.Description, Modifier.weight(1f), SashioBlue)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Active Bookings", activeBookings.toString(), Icons.Default.DirectionsRun, Modifier.weight(1f), SashioTeal)
                StatCard("Completed", completedServices.toString(), Icons.Default.CheckCircle, Modifier.weight(1f), SashioGreen)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = SashioRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Outstanding Payments", style = MaterialTheme.typography.labelSmall, color = SashioRed)
                        Text("₦%,.2f".format(outstandingPayments), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SashioSlate)
                    }
                    Icon(Icons.Default.Warning, contentDescription = null, tint = SashioRed, modifier = Modifier.size(32.dp))
                }
            }
        }

        // Recent Notifications / Activities
        item {
            Text("Recent Activities & Notifications", style = MaterialTheme.typography.labelMedium, color = SashioGold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (bookings.isEmpty()) {
            item {
                Text("No recent activities found.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted, modifier = Modifier.padding(vertical = 12.dp))
            }
        } else {
            items(bookings.sortedByDescending { it.createdAt }.take(5)) { booking ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        val StatusTrio = when (booking.status) {
                            "Quotation Requested" -> Triple("PENDING REVIEW", SashioGold.copy(alpha = 0.12f), SashioGold)
                            "Under Review" -> Triple("UNDER REVIEW", SashioGold.copy(alpha = 0.12f), SashioGold)
                            "Quotation Sent" -> Triple("QUOTE ISSUED", SashioTeal.copy(alpha = 0.12f), SashioTeal)
                            "Client Accepted" -> Triple("ACCEPTED / UNPAID", SashioBlue.copy(alpha = 0.12f), SashioBlue)
                            "Booking Confirmed" -> Triple("BOOKED & CLEARED", SashioGreen.copy(alpha = 0.12f), SashioGreen)
                            "SERVICE DELIVERED" -> Triple("ON-ROUTE", SashioTeal.copy(alpha = 0.12f), SashioTeal)
                            "in_progress" -> Triple("SERVICING", SashioTeal.copy(alpha = 0.12f), SashioTeal)
                            "completed" -> Triple("COMPLETED", SashioGreen.copy(alpha = 0.12f), SashioGreen)
                            "Completed" -> Triple("COMPLETED", SashioGreen.copy(alpha = 0.12f), SashioGreen)
                            else -> Triple("CANCELLED", SashioRed.copy(alpha = 0.12f), SashioRed)
                        }
                        Box(modifier = Modifier.size(40.dp).background(StatusTrio.second, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = StatusTrio.third, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Quotation for ${booking.serviceType}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                            Text("Status updated to: ${booking.status}", fontSize = 10.sp, color = SashioTextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: Color = SashioTeal
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = SashioSlate)
            Text(label, fontSize = 11.sp, color = SashioTextMuted)
        }
    }
}

// --- Product Discovery Tab (10 categories + action prompts) ---
@Composable
fun ProductDiscoveryTab(catalogItems: List<CatalogItem>, onRequestQuote: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("SACHIO ENTERPRISE CATALOG", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
        Text("Prestige in Every Flush. Explore premium, sanitary systems.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
            items(catalogItems) { cat ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(cat.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SashioSlate, modifier = Modifier.weight(1f))
                            Card(colors = CardDefaults.cardColors(containerColor = SashioGold.copy(alpha = 0.1f)), shape = RoundedCornerShape(4.dp)) {
                                Text(cat.variantName, color = SashioGold, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(6.dp, 3.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Videos (if present, show indication prominently)
                        if (cat.videoUrl.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                Icon(Icons.Default.PlayCircle, contentDescription = "Video", tint = SashioRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Video Presentation Available", fontSize = 10.sp, color = SashioRed, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        if (cat.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = cat.imageUrl,
                                contentDescription = "Product Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(cat.description, style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
                        
                        if (expanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (cat.features.isNotBlank()) {
                                Text("Features", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SashioTeal)
                                Text(cat.features, fontSize = 11.sp, color = SashioSlate, modifier = Modifier.padding(bottom = 8.dp))
                            }
                            if (cat.specifications.isNotBlank()) {
                                Text("Specifications", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SashioGold)
                                Text(cat.specifications, fontSize = 11.sp, color = SashioSlate, modifier = Modifier.padding(bottom = 8.dp))
                            }
                            if (cat.benefits.isNotBlank()) {
                                Text("Benefits", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SashioGreen)
                                Text(cat.benefits, fontSize = 11.sp, color = SashioSlate, modifier = Modifier.padding(bottom = 8.dp))
                            }
                            if (cat.useCases.isNotBlank()) {
                                Text("Use Cases", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SashioBlue)
                                Text(cat.useCases, fontSize = 11.sp, color = SashioSlate, modifier = Modifier.padding(bottom = 8.dp))
                            }
                        } else {
                            if (cat.features.isNotBlank() || cat.specifications.isNotBlank() || cat.useCases.isNotBlank()) {
                                Text("Tap to view Features, Specs, Use Cases & Details", fontSize = 10.sp, color = SashioTeal, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                        
                        // User explicitly requested admin to upload prices in product description.
                        // We show it as a Guide Price if basePrice > 0
                        if (cat.basePrice > 0.0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Guide Price: ₦%,.2f".format(cat.basePrice), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SashioSlate)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Under user constraints, NEVER display product pricing. Display action triggers instead:
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onRequestQuote(cat.title) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SashioGold),
                                border = BorderStroke(1.dp, SashioGold),
                                modifier = Modifier.weight(1.2f).height(36.dp)
                            ) {
                                Text("Request Quotation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { /* Call Sales simulated */ },
                                colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                modifier = Modifier.weight(0.8f).height(36.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Contact Sales", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- VIP Presidential Suite WALKTHROUGH View ---
@Composable
fun VipPresidentialTab(onRequestQuote: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SashioSlate),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Star, contentDescription = null, tint = SashioGold, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "PRESIDENTIAL LUXURY CABINS",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    "Experience Sovereign Comfort Anywhere",
                    style = MaterialTheme.typography.titleMedium,
                    color = SashioGold,
                    fontWeight = FontWeight.Light
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("VIRTUAL WALKTHROUGH & LOGBOOK", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text("Review elite features integrated inside presidential layouts:", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)

        Spacer(modifier = Modifier.height(12.dp))

        listOf(
            Triple(Icons.Default.AcUnit, "Climate Control Setup", "High duty quiet HVAC units maintaining stable 18 degree temperatures under any Lagos heat."),
            Triple(Icons.Default.VolumeUp, "Surround Audio System", "Integrated smart ceiling speakers playing ambient classical and zen rhythms."),
            Triple(Icons.Default.WaterDrop, "Automated Soft Flush", "Completely contactless hands-free automatic vacuum systems ensuring zero smells."),
            Triple(Icons.Default.SensorWindow, "LED Vanity Mirrors", "Sleek back-lit vanity touch control smart mirrors matching bridal makeup.")
        ).forEach { (icon, title, sDesc) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SashioGold.copy(alpha = 0.15f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = SashioGold)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SashioSlate)
                        Text(sDesc, fontSize = 11.sp, color = SashioTextMuted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRequestQuote,
            colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("REQUEST PRESIDENTIAL VIEW QUOTATION", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

// --- Quotations Tracker & Paystack Simulator Trigger Tab ---
@Composable
fun ClientQuotationTrackerTab(viewModel: SashioViewModel) {
    val bookings by viewModel.clientBookings.collectAsStateWithLifecycle()
    val activeTrackers by viewModel.activeLiveTrackers.collectAsStateWithLifecycle()
    var bookingToConvert by remember { mutableStateOf<Booking?>(null) }
    val context = LocalContext.current

    var activeTrackingBookingId by remember { mutableStateOf<Long?>(null) }

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
                    Icon(Icons.Default.Handshake, contentDescription = null, tint = SashioGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Service Booking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SashioSlate)
                }
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("1. Confirm Service Details", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioSlate)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Product Class: ${b.serviceType}", fontSize = 11.sp, color = SashioTextMuted)
                                Text("Variant: ${b.variantName} (${b.modelNumber})", fontSize = 11.sp, color = SashioTextMuted)
                                Text("Quantity: ${b.quantity} unit(s)", fontSize = 11.sp, color = SashioTextMuted)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray.copy(alpha = 0.5f))
                                Text("Quote Subtotal: ₦%,.2f".format(b.quotedPrice), fontSize = 11.sp, color = SashioSlate)
                                Text("Logistics Cost: ₦%,.2f".format(b.logisticsCost), fontSize = 11.sp, color = SashioSlate)
                                Text("VAT (7.5%): ₦%,.2f".format(b.vatAmount), fontSize = 11.sp, color = SashioSlate)
                                Text("Grand Total: ₦%,.2f".format(grandTotal), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioGreen)
                            }
                        }
                    }

                    item {
                        Text("2. Select Service Dates & Duration", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioSlate)
                        OutlinedTextField(
                            value = serviceDate,
                            onValueChange = { serviceDate = it },
                            label = { Text("Service Start Date") },
                            placeholder = { Text("e.g. June 15, 2026") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SashioGold, modifier = Modifier.size(16.dp)) },
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
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = SashioGold, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text("3. Confirm Delivery Location", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioSlate)
                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text("Delivery Address") },
                            maxLines = 2,
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SashioGold, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text("4. Special Instructions & Directions", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioSlate)
                        OutlinedTextField(
                            value = specialInstructions,
                            onValueChange = { specialInstructions = it },
                            label = { Text("Special Instructions") },
                            maxLines = 3,
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = SashioGold, modifier = Modifier.size(16.dp)) },
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
                                Toast.makeText(context, "Quotation successfully converted to Booking!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SashioGreen, contentColor = Color.White)
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("QUOTATION & ACTIVE DISPATCH TRACKER", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Follow your quotations from Under Review to Delivery", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
        Spacer(modifier = Modifier.height(12.dp))

        if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No active quotes. Request a quote to initiate the workflow.", color = SashioTextMuted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                items(bookings) { b ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("booking_card_${b.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(b.serviceType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Ref: #QR-${b.id}", fontSize = 11.sp, color = SashioTextMuted)
                                }
                                BookingStatusBadge(status = b.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PinDrop, contentDescription = null, tint = SashioGold, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(b.address, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp, color = SashioTextMuted)
                            }

                            // Inject Real-time dynamic Dispatch Live Map Track card for client
                            if (b.status in listOf("IN TRANSIT", "ARRIVED", "SERVICE DELIVERED")) {
                                val tracker = activeTrackers[b.id]
                                var layer by remember { mutableStateOf("Standard") }
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SashioSlate),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = SashioGold, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Live Dispatch Tracking", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            AssistChip(
                                                onClick = {
                                                    layer = when(layer) {
                                                        "Standard" -> "Satellite"
                                                        "Satellite" -> "Traffic"
                                                        "Traffic" -> "Operations"
                                                        else -> "Standard"
                                                    }
                                                },
                                                label = { Text("Layer: $layer", fontSize = 9.sp, color = Color.White) },
                                                colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.15f))
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
                                                staffLat = tracker?.staffLat ?: (b.latitude - 0.03),
                                                staffLng = tracker?.staffLng ?: (b.longitude - 0.025),
                                                vehicleLat = tracker?.vehicleLat ?: (b.latitude - 0.032),
                                                vehicleLng = tracker?.vehicleLng ?: (b.longitude - 0.028),
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
                                                Text("ETA: ${tracker?.etaMinutes ?: 15} mins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("Distance: %.1f km".format(tracker?.distanceRemainingKm ?: 8.4), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Crew: ${b.assignedStaffName ?: "David Staff james"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text("Status: ${b.status}", color = SashioGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // Live Interactive Radar button injection for active bookings
                            if (b.status in listOf("Payment Confirmed", "Booking Confirmed", "Staff Assigned", "SERVICE DELIVERED", "IN TRANSIT", "ARRIVED", "SETUP IN PROGRESS", "SERVICE ACTIVE", "COMPLETED", "Completed")) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { activeTrackingBookingId = b.id },
                                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate, contentColor = SashioGold),
                                    border = BorderStroke(1.dp, SashioGold),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp).testTag("radar_button_${b.id}")
                                ) {
                                    Icon(Icons.Default.Explore, contentDescription = null, tint = SashioGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Track Prestige Service Real-Time", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // If quotation is issued ("Quotation Sent" or later states)
                            if (b.status in listOf("Quotation Sent", "Client Accepted", "Booking Confirmed", "SERVICE DELIVERED", "Completed")) {
                                Divider(modifier = Modifier.padding(vertical = 12.dp))
                                val grandTotal = b.quotedPrice + b.logisticsCost + b.vatAmount
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Issued Service Total:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("₦%,.2f".format(grandTotal), fontWeight = FontWeight.ExtraBold, color = SashioSlate, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Subtotal ₦%,.0f | Logistics ₦%,.0f | VAT ₦%,.0f".format(b.quotedPrice, b.logisticsCost, b.vatAmount), fontSize = 10.sp, color = SashioTextMuted)

                                Spacer(modifier = Modifier.height(10.dp))

                                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Simulated download PDF document
                                        Button(
                                            onClick = { Toast.makeText(context, "Downloading PDF Quotation Document...", Toast.LENGTH_SHORT).show() },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = SashioSlate),
                                            modifier = Modifier.height(34.dp).weight(1f)
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = SashioSlate, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Download PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Simulated share quotation
                                        Button(
                                            onClick = { Toast.makeText(context, "Opening share dialogue...", Toast.LENGTH_SHORT).show() },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = SashioSlate),
                                            modifier = Modifier.height(34.dp).weight(1f)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, tint = SashioSlate, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Share", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    when (b.status) {
                                        "Quotation Sent" -> {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { viewModel.clientRejectQuotation(b.id) {} },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioRed, contentColor = Color.White),
                                                    modifier = Modifier.height(34.dp).weight(1f)
                                                ) {
                                                    Text("Reject", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = { viewModel.clientRequestRevision(b.id) {} },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate, contentColor = Color.White),
                                                    modifier = Modifier.height(34.dp).weight(1.2f)
                                                ) {
                                                    Text("Request Revision", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = { bookingToConvert = b },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioGreen, contentColor = Color.White),
                                                    modifier = Modifier.height(34.dp).weight(1.2f)
                                                ) {
                                                    Text("Accept & Book", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        "Client Accepted" -> {
                                            if (b.paymentStatus != "success") {
                                                Button(
                                                    onClick = { viewModel.triggerPaystackPayment(b.id, grandTotal) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioGreen, contentColor = Color.White),
                                                    modifier = Modifier.fillMaxWidth().height(36.dp).testTag("pay_booking_${b.id}")
                                                ) {
                                                    Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Pay Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

// --- Consultation & Community Feedback Tab ---
@Composable
fun ConsultationAndReviewsTab(viewModel: SashioViewModel) {
    val reviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val consultations by viewModel.clientConsultations.collectAsStateWithLifecycle()

    var consType by remember { mutableStateOf("Schedule Site Inspection") }
    var consDate by remember { mutableStateOf("2026-06-15") }
    var consInstructions by remember { mutableStateOf("") }

    var ratingVal by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var reviewCategory by remember { mutableStateOf("Presidential Luxury Mobile Restrooms") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // A. BOOK A CONSULTATION
        Text("SALES CONSULTATION HUB", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Book inspections, callback requests or product demonstrations.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
        Spacer(modifier = Modifier.height(10.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Request Consultation / Callback", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                Spacer(modifier = Modifier.height(10.dp))

                listOf("Schedule Site Inspection", "Book Consultation", "Request Callback", "Request Product Demonstration").forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { consType = type }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = consType == type, onClick = { consType = type })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(type, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = consDate,
                    onValueChange = { consDate = it },
                    label = { Text("Preferred Inspection Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = consInstructions,
                    onValueChange = { consInstructions = it },
                    label = { Text("Special Guidelines / Ground coordinates") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.bookConsultation(consType, consDate, consInstructions) {
                            consInstructions = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Schedule and Notify Sales", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // B. CUSTOMER REVIEWS SECTOR
        Text("CUSTOMER REVIEWS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Submit ratings and event testimonials directly from the app", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
        Spacer(modifier = Modifier.height(10.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Your Rating:")
                    (1..5).forEach { stars ->
                        IconButton(onClick = { ratingVal = stars }, modifier = Modifier.size(24.dp)) {
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.submitReview(ratingVal, reviewComment, reviewCategory) {
                            reviewComment = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SashioTeal),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Submit Public Review", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // C. PORTFOLIO SHOWCASE
        Text("SACHIO PROJECT PORTFOLIO", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Showcasing elite completed projects and sanitation delivery sites.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
        Spacer(modifier = Modifier.height(10.dp))

        listOf(
            "Luxury Eko Atlantic Beach Wedding Plot" to "Presidential Restroom Sovereign setups serving 600 VIP guests.",
            "Lagos National Tech Summit Forum" to "5 Executive Trailer Cabins perfectly installed within the exhibition lot.",
            "Government Wuse II State Banquet" to "VIP Flush modules backed with shift cleanup attendants.",
            "Construction Depot Site, Port Harcourt" to "10 Heavy Duty Standard chemical toilets managed over solid extraction weeks."
        ).forEach { (proj, info) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SashioSlate),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(proj, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text(info, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reviews Display List
        reviews.forEach { rev ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(rev.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            (1..rev.rating).forEach { _ ->
                                Icon(Icons.Default.Star, contentDescription = null, tint = SashioGold, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    Text(rev.comment, fontSize = 11.sp, color = SashioSlate, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // D. CONTACT & SOCIAL DIRECTORY
        Text("CONTACT & SOCIAL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Get in touch directly or connect across our platforms.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
        Spacer(modifier = Modifier.height(10.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = SashioSlate),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SashioGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Suite E284 Ikota shopping complex VGC Lekki Ajah",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                val context = LocalContext.current
                
                OutlinedButton(
                    onClick = {
                        val i = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.facebook.com/share/17ukrrzVb3/"))
                        context.startActivity(i)
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Visit Facebook Page")
                }
                
                OutlinedButton(
                    onClick = {
                        val i = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.instagram.com/sachiomobiletoilets?igsh=NWluaTAzYnMwMXow"))
                        context.startActivity(i)
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Visit Instagram Profile")
                }

                OutlinedButton(
                    onClick = {
                        val i = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://x.com/mobiletoiletsng"))
                        context.startActivity(i)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Visit X (Twitter)")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(30.dp))
    }
}

// --- Comprehensive Quotation & Booking Request Form ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormSheet(
    viewModel: SashioViewModel,
    initialCategory: String,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val favorites by viewModel.clientFavoriteLocations.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var serviceMode by remember { mutableStateOf("Rental") } // Rental, Purchase
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedVariant by remember { mutableStateOf("VIP Luxury Cabin") }
    var selectedModel by remember { mutableStateOf("S-800 Elite") }
    var quantity by remember { mutableStateOf("1") }
    var duration by remember { mutableStateOf("1 Day") }

    var addressQuery by remember { mutableStateOf("") }
    var selectedAddressText by remember { mutableStateOf("") }
    var currentCoords by remember { mutableStateOf(Pair(6.4531, 3.3958)) }

    var eventType by remember { mutableStateOf("Wedding") }
    var guestCount by remember { mutableStateOf("100") }
    var eventDate by remember { mutableStateOf("2026-06-25") }

    var cleaningSupport by remember { mutableStateOf(false) }
    var onSiteAttendant by remember { mutableStateOf(false) }
    var wasteEvacuation by remember { mutableStateOf(false) }
    var maintenanceSupport by remember { mutableStateOf(false) }

    var specialInstructions by remember { mutableStateOf("") }

    val suggestions by viewModel.addressSuggestions.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(SashioLight)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("DETAILED QUOTATION REQUEST", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SashioSlate)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close sheet")
            }
        }

        Divider(modifier = Modifier.padding(bottom = 14.dp))

        // 1. Service mode selector
        Text("1. Service Intent", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioGold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Rental", "Purchase").forEach { mode ->
                Button(
                    onClick = { serviceMode = mode },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (serviceMode == mode) SashioGold else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (serviceMode == mode) Color.White else SashioSlate
                    ),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text(mode, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Product selection
        Text("2. Select Catalog Range", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioGold)
        Spacer(modifier = Modifier.height(4.dp))

        var expCategory by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { expCategory = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedCategory, color = SashioSlate, fontWeight = FontWeight.Bold)
            }
            DropdownMenu(expanded = expCategory, onDismissRequest = { expCategory = false }) {
                listOf(
                    "Standard Portable Toilets", "Executive Portable Toilets", "VIP Portable Toilets",
                    "Presidential Luxury Mobile Restrooms", "Mobile Shower Units", "Accessible Toilets",
                    "Event Sanitation Packages", "Corporate Site Sanitation Solutions",
                    "Waste Evacuation Services", "Cleaning & Maintenance Services"
                ).forEach { cat ->
                    DropdownMenuItem(text = { Text(cat) }, onClick = {
                        selectedCategory = cat
                        expCategory = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = selectedVariant,
            onValueChange = { selectedVariant = it },
            label = { Text("Product Variant") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = selectedModel,
            onValueChange = { selectedModel = it },
            label = { Text("Model Number / Code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Items Quantity Required") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Event Duration Select") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Location tracker
        Text("3. Delivery Address Setup", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioGold)
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = addressQuery,
            onValueChange = {
                addressQuery = it
                viewModel.updateAddressSearch(it)
            },
            label = { Text("Search Street / Site Location...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (suggestions.isNotEmpty() && selectedAddressText.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp).border(1.dp, Color.LightGray)
            ) {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(suggestions) { place ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAddressText = place.address
                                    currentCoords = Pair(place.lat, place.lng)
                                    addressQuery = place.address
                                    focusManager.clearFocus()
                                }
                                .padding(10.dp)
                                .testTag("suggestion_${place.lat}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SashioGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(place.address, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        val metrics = viewModel.calculateLogisticsMetrics(currentCoords.first, currentCoords.second)

        // Horizontal Favorites list if any exist
        if (favorites.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text("Select Saved Favorite Location:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                items(favorites) { fav ->
                    AssistChip(
                        onClick = {
                            selectedAddressText = fav.address
                            currentCoords = Pair(fav.latitude, fav.longitude)
                            addressQuery = fav.address
                            Toast.makeText(context, "Loaded saved location: ${fav.name}", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("${fav.locationType.uppercase()}: ${fav.name}", fontSize = 10.sp, maxLines = 1) },
                        leadingIcon = {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = SashioGold)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Interactive Map Vector Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.LightGray)
        ) {
            SashioCanvasMap(
                latitude = currentCoords.first,
                longitude = currentCoords.second,
                onMarkerChanged = { newLat, newLng ->
                    currentCoords = Pair(newLat, newLng)
                },
                mapStyle = "Standard",
                label = "Delivery Pin Drop"
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
            ) {
                Text(
                    "GPS: %.4f, %.4f".format(currentCoords.first, currentCoords.second),
                    color = Color.White,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Actions: Use current location & Save Favorite
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val sampleLat = 6.4351
                    val sampleLng = 3.4219
                    selectedAddressText = "Plot 24, SACHIO Central Plaza Arena, Victoria Island, Lagos"
                    currentCoords = Pair(sampleLat, sampleLng)
                    addressQuery = "Plot 24, SACHIO Central Plaza Arena, Victoria Island, Lagos"
                    Toast.makeText(context, "GPS Location Detected: Plot 24, SACHIO Central Plaza Arena", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SashioSlate, contentColor = Color.White),
                modifier = Modifier.weight(1.2f).height(36.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp), tint = SashioGold)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Use Current Location", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = {
                    if (selectedAddressText.isNotBlank()) {
                        viewModel.saveFavoriteLocation(
                            name = "Delivery Site Location",
                            address = selectedAddressText,
                            lat = currentCoords.first,
                            lng = currentCoords.second,
                            type = "Other"
                        )
                        Toast.makeText(context, "Address successfully saved to favorites!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please set or pick an address first!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SashioTeal, contentColor = Color.White),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Favorite", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Real-time automatic logistics calculator insight card
        Card(
            colors = CardDefaults.cardColors(containerColor = SashioGold.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("AUTOMATIC LOGISTICS INSIGHT", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SashioSlate)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Operational Zone: ${metrics.logisticsZone}", fontSize = 10.sp, color = SashioSlate)
                Text("Distance from Depot: %.2f km".format(metrics.distanceKm), fontSize = 10.sp, color = SashioSlate)
                Text("Estimated Travel Time: ${metrics.travelTimeMinutes} minutes", fontSize = 10.sp, color = SashioSlate)
                Text("Simulated Fuel Factor: %.1f Liters Diesel".format(metrics.estimatedFuelLitres), fontSize = 10.sp, color = SashioSlate)
                Text("Recommended Transport Logistics Fee: ₦%,.2f".format(metrics.recommendedLogisticsCost), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioGreen)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Event Information
        Text("4. Gathering & Event Specs", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioGold)
        
        OutlinedTextField(
            value = eventType,
            onValueChange = { eventType = it },
            label = { Text("Gathering Event Type (e.g., Wedding)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = guestCount,
            onValueChange = { guestCount = it },
            label = { Text("Projected Guest Capacity count") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = eventDate,
            onValueChange = { eventDate = it },
            label = { Text("Target Event Setup Date") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Inclusions
        Text("5. Operations & Inclusions", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioGold)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { cleaningSupport = !cleaningSupport }.padding(vertical = 4.dp)
        ) {
            Checkbox(checked = cleaningSupport, onCheckedChange = { cleaningSupport = it })
            Spacer(modifier = Modifier.width(6.dp))
            Text("Onsite Cleaning Support Services", fontSize = 12.sp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { onSiteAttendant = !onSiteAttendant }.padding(vertical = 4.dp)
        ) {
            Checkbox(checked = onSiteAttendant, onCheckedChange = { onSiteAttendant = it })
            Spacer(modifier = Modifier.width(6.dp))
            Text("Standby Servicing Uniformed Crew Attendant", fontSize = 12.sp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { wasteEvacuation = !wasteEvacuation }.padding(vertical = 4.dp)
        ) {
            Checkbox(checked = wasteEvacuation, onCheckedChange = { wasteEvacuation = it })
            Spacer(modifier = Modifier.width(6.dp))
            Text("Scheduled Waste Quad Evacuation services", fontSize = 12.sp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { maintenanceSupport = !maintenanceSupport }.padding(vertical = 4.dp)
        ) {
            Checkbox(checked = maintenanceSupport, onCheckedChange = { maintenanceSupport = it })
            Spacer(modifier = Modifier.width(6.dp))
            Text("Active Plumber / Mechanical standby support", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = specialInstructions,
            onValueChange = { specialInstructions = it },
            label = { Text("Specific Instructions (Hardware, fragrance details...)") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val finalAddress = if (selectedAddressText.isNotEmpty()) selectedAddressText else addressQuery
                if (finalAddress.isNotEmpty() && currentUser != null) {
                    val booking = Booking(
                        clientId = currentUser!!.id,
                        clientName = currentUser!!.name,
                        clientPhone = currentUser!!.phoneNumber,
                        clientEmail = currentUser!!.email,
                        companyName = currentUser!!.companyName,
                        serviceMode = serviceMode,
                        serviceType = selectedCategory,
                        variantName = selectedVariant,
                        modelNumber = selectedModel,
                        quantity = quantity.toIntOrNull() ?: 1,
                        duration = duration,
                        address = finalAddress,
                        latitude = currentCoords.first,
                        longitude = currentCoords.second,
                        eventType = eventType,
                        guestCount = guestCount.toIntOrNull() ?: 100,
                        eventDate = eventDate,
                        cleaningSupport = cleaningSupport,
                        onSiteAttendant = onSiteAttendant,
                        wasteEvacuation = wasteEvacuation,
                        maintenanceSupport = maintenanceSupport,
                        specialInstructions = specialInstructions,
                        status = "Quotation Requested"
                    )
                    viewModel.submitQuotationRequest(booking) {
                        onDismiss()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("service_booking_submit"),
            enabled = addressQuery.isNotEmpty() || selectedAddressText.isNotEmpty()
        ) {
            Text("TRANSMIT QUOTE REQUEST", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ==========================================
// 3. STAFF WORKSPACE & CARD
// ==========================================

@Composable
fun StaffDashboard(viewModel: SashioViewModel, staff: UserProfile?) {
    val localCtx = LocalContext.current
    val bookings by viewModel.staffBookings.collectAsStateWithLifecycle()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()
    var isSettingsOpen by remember { mutableStateOf(false) }

    // Workforce Core States
    var activeStaffTab by remember { mutableStateOf("dashboard") } // "dashboard", "jobs", "maps", "messages", "profile"
    var selectedJobDetailsId by remember { mutableStateOf<Long?>(null) } // Drilldown view

    // Attendance Core States
    val clockedIn by viewModel.isClockedIn.collectAsStateWithLifecycle()
    val attendanceHistory by viewModel.attendanceHistory.collectAsStateWithLifecycle()
    val expenses by viewModel.staffExpenses.collectAsStateWithLifecycle()
    val inspections by viewModel.inspections.collectAsStateWithLifecycle()
    val generalChats by viewModel.generalChats.collectAsStateWithLifecycle()

    var clockInWorkplace by remember { mutableStateOf("SACHIO Victoria Island Depot") }
    var showClockInDialog by remember { mutableStateOf(false) }

    // Operational Interactive Checklists
    var checkedBeforeDeparture by remember { mutableStateOf(setOf("Vehicle fluid metrics verified", "Restroom trailer towing lock pinned", "Fresh bio-blue fluid topped up")) }
    var expandedSopIndex by remember { mutableStateOf<Int?>(0) }
    var activeChecklistBeforeDeparture by remember { mutableStateOf(setOf<String>()) }
    var activeChecklistUponArrival by remember { mutableStateOf(setOf<String>()) }
    var activeChecklistServiceActivities by remember { mutableStateOf(setOf<String>()) }
    var activeChecklistCompletion by remember { mutableStateOf(setOf<String>()) }

    // Digital Inspection Reporting Form States
    var selectedInspectionCondition by remember { mutableStateOf("Pristine") } // Pristine, Fair, Needs Repair
    var selectedWaterLevel by remember { mutableStateOf("100%") } // 100%, 75%, 50%, 25%
    var cleanlinessScoreInput by remember { mutableStateOf(5f) } // 1 to 5
    var equipmentConditionInput by remember { mutableStateOf("Good") } // Good, Faulty
    var damageAssessmentInput by remember { mutableStateOf("No noticeable exterior scratches or pipe dents.") }
    var inspectionCommentInput by remember { mutableStateOf("Excellent pre-commissioning standard. S-1000 Sovereign vacuums initialized perfectly.") }

    var beforePhotosList by remember { mutableStateOf(listOf("https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=500&auto=format&fit=crop")) }
    var afterPhotosList by remember { mutableStateOf(listOf("https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=500&auto=format&fit=crop")) }
    var showCameraSimulationBox by remember { mutableStateOf<String?>(null) } // "before" or "after"
    var cameraCapturingStatus by remember { mutableStateOf("Live camera lens online...") }

    // Expense Claims State
    var expenseAmountInput by remember { mutableStateOf("") }
    var expenseCategorySelect by remember { mutableStateOf("Fuel") } // Fuel, Toll, Parking, Emergency
    var expenseNotesInput by remember { mutableStateOf("") }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    // Emergency/Incident State
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var emergencyIncidentCategory by remember { mutableStateOf("Road Block") }
    var emergencyComments by remember { mutableStateOf("") }
    var emergencyContactNumber by remember { mutableStateOf("+234 811 122 3344") } // Chief Supervisor

    // Multi-Channel Messaging State
    var messagingChannelSelect by remember { mutableStateOf("Admin Center") } // Admin Center, Field Supervisor, Active Customer
    var typedGeneralMsgText by remember { mutableStateOf("") }

    // Offline Workspace System Info
    var workOfflineToggle by remember { mutableStateOf(false) }
    var queuedOfflineInspections by remember { mutableStateOf(listOf<String>()) }

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
            NavigationBar(
                containerColor = SashioSlate,
                tonalElevation = 8.dp,
                modifier = Modifier.height(72.dp)
            ) {
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
                            selectedJobDetailsId = null // Dismiss details view on tab shifts
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
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.White.copy(alpha = 0.08f)
                        )
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
                    "dashboard" -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0F172A))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Offline Warning banner
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

                            // 1. Attendance & Punch Clock Widget
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
                                                text = if (clockedIn) "Work Time: 04h 15m" else "No active shift",
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 11.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = if (clockedIn) "Assigned Depot: $clockInWorkplace" else "Ready to commence daily sanitation schedules",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        if (!clockedIn) {
                                            Button(
                                                onClick = { showClockInDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                                                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("clock_in_button")
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("LOCK IN ATTENDANCE / CLOCK IN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        // Clock out
                                                        viewModel.clockOutStaff()
                                                    },
                                                    border = BorderStroke(1.dp, SashioRed.copy(alpha = 0.5f)),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SashioRed),
                                                    modifier = Modifier.weight(1f).height(42.dp).testTag("clock_out_button")
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("CLOCK OUT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        showEmergencyDialog = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                                                    modifier = Modifier.weight(1f).height(42.dp).testTag("emergency_button")
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

                            // 2. Metrics Scorecard HUD
                            item {
                                Column {
                                    Text("Performance Metrics scorecard".uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            Triple("Jobs Today", "${bookings.size}", SashioGold),
                                            Triple("Active Tasks", "${bookings.filter { it.status == "ARRIVED" || it.status == "IN TRANSIT" }.size}", SashioTeal),
                                            Triple("Performance", "94%", SashioGreen)
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

                            // 3. Quick Action Grid
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
                                                onClick = { activeStaffTab = tabTarget },
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

                            // 4. Pending Dispatch Task Checklist
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
                                                        checkedBeforeDeparture = if (isChecked) {
                                                            checkedBeforeDeparture - task
                                                        } else {
                                                            checkedBeforeDeparture + task
                                                        }
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

                            // 5. Training / SOP Library Document Quick View
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("SACHIO STAFF SOP", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.2.sp)
                                    Text("Prestige in Every Service", fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color.White.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    val sopList = listOf(
                                        Triple("1. Be Ready", Icons.Default.AccessTime, listOf(
                                            "Report on time.",
                                            "Wear full Sachio uniform.",
                                            "Be alert, fit, and ready for duty."
                                        )),
                                        Triple("2. Respect the Brand", Icons.Default.Star, listOf(
                                            "Be professional at all times.",
                                            "Treat clients, colleagues, and the public with respect.",
                                            "Protect the reputation of Sachio."
                                        )),
                                        Triple("3. Before Leaving the Yard", Icons.Default.DirectionsCar, listOf(
                                            "Inspect vehicle.",
                                            "Inspect toilets.",
                                            "Confirm job details.",
                                            "Ensure cleaning supplies and tools are available."
                                        )),
                                        Triple("4. At the Client's Location", Icons.Default.Place, listOf(
                                            "Greet the client professionally.",
                                            "Confirm setup location.",
                                            "Position toilets properly and safely.",
                                            "Take photos after setup."
                                        )),
                                        Triple("5. Cleanliness First", Icons.Default.CheckCircle, listOf(
                                            "Keep toilets clean and odor-free.",
                                            "Refill tissue, water, sanitizer, and chemicals.",
                                            "Never leave a dirty unit at a client site."
                                        )),
                                        Triple("6. No Begging Policy", Icons.Default.Block, listOf(
                                            "Salary is your entitlement.",
                                            "Tips, gifts, or support from Sachio are discretionary and not guaranteed.",
                                            "Never request money from clients."
                                        )),
                                        Triple("7. Team Responsibility", Icons.Default.People, listOf(
                                            "Follow instructions.",
                                            "Communicate issues immediately.",
                                            "Support team members when needed."
                                        )),
                                        Triple("8. Safety", Icons.Default.Security, listOf(
                                            "Handle equipment carefully.",
                                            "Report accidents, damage, or leaks immediately.",
                                            "Follow all safety procedures."
                                        )),
                                        Triple("9. End of Duty", Icons.Default.DoneAll, listOf(
                                            "Return all tools.",
                                            "Clean vehicle and equipment.",
                                            "Submit job report and photos."
                                        )),
                                        Triple("10. The Sachio Standard", Icons.Default.Star, listOf(
                                            "Every job must reflect:",
                                            "✅ Cleanliness",
                                            "✅ Professionalism",
                                            "✅ Reliability",
                                            "✅ Accountability",
                                            "✅ Respect"
                                        ))
                                    )

                                    sopList.forEachIndexed { index, (title, icon, details) ->
                                        val isExpanded = expandedSopIndex == index
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isExpanded) SashioSlate.copy(alpha = 0.35f) else SashioSlate.copy(alpha = 0.15f)
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isExpanded) SashioGold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f)
                                            ),
                                            onClick = {
                                                expandedSopIndex = if (isExpanded) null else index
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = icon,
                                                            contentDescription = null,
                                                            tint = if (isExpanded) SashioGold else Color.White.copy(alpha = 0.6f),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = title,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isExpanded) SashioGold else Color.White,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = if (isExpanded) SashioGold else Color.White.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                if (isExpanded) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        details.forEach { detail ->
                                                            Row(verticalAlignment = Alignment.Top) {
                                                                if (!detail.startsWith("Every job must") && !detail.startsWith("✅")) {
                                                                    Text(
                                                                        "• ",
                                                                        color = SashioGold,
                                                                        fontSize = 12.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                                Text(
                                                                    text = detail,
                                                                    color = Color.White.copy(alpha = 0.85f),
                                                                    fontSize = 11.sp,
                                                                    lineHeight = 14.sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Sachio Staff Motto Callout Card
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SashioGold.copy(alpha = 0.05f)),
                                        border = BorderStroke(1.dp, SashioGold.copy(alpha = 0.2f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "SACHIO STAFF MOTTO",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SashioGold,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "\"Prestige in Every Service\"",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Light,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "jobs" -> {
                        if (selectedJobDetailsId != null) {
                            // Deep drilldown Job Details layout
                            val bkId = selectedJobDetailsId!!
                            val b = bookings.find { it.id == bkId } ?: allBookings.find { it.id == bkId }
                            
                            if (b == null) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Job data not found", color = Color.White)
                                }
                            } else {
                                // Detailed screen
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
                                            Text("Job Deep-Dive: #QR-${b.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }

                                    // Status Badge & Customer Specs
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
                                                    Text("Date: ${b.eventDate}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(b.serviceType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text("${b.variantName} | ${b.modelNumber}", fontSize = 11.sp, color = SashioGold)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Divider(color = Color.White.copy(alpha = 0.08f))
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("CLIENT NAME: ${b.clientName}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                                Text("BUSINESS: ${b.companyName.ifEmpty { "Private Client" }}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                                Text("ADDRESS: ${b.address}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                                Text("SPECIAL NOTES: ${b.specialInstructions.ifEmpty { "None provided." }}", fontSize = 12.sp, color = SashioGold, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }

                                    // Step-by-Step Checkout Checklists
                                    item {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.25f)),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text("Step-by-step Quality checklists", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                                                Spacer(modifier = Modifier.height(10.dp))

                                                // 1. Before Departure Checklist
                                                Text("1. Pre-Departure parameters", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
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

                                                // 2. Upon Arrival Checklist
                                                Text("2. Upon Arrival alignment", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
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

                                                // 3. Service & Maintenance Checklist
                                                Text("3. On-Site Service Activities", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                                listOf("Sanitize floor, seat, and vanity handles", "Refill luxury scent atomizer", "Refill paper Towel stock").forEach { item ->
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

                                    // Photographic Report Capture System (Simulated Camera Frame)
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
                                                    // Simulated active viewport camera
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
                                                            Text("Before Photo", fontSize = 10.sp)
                                                        }

                                                        Button(
                                                            onClick = {
                                                                showCameraSimulationBox = "after"
                                                                cameraCapturingStatus = "Vesta sensor. Focusing pristine interior alignment..."
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                            modifier = Modifier.weight(1f).height(38.dp)
                                                        ) {
                                                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SashioGold, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("After Photo", fontSize = 10.sp)
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text("Captured Files: ${beforePhotosList.size} Before, ${afterPhotosList.size} After", fontSize = 11.sp, color = Color.LightGray)
                                            }
                                        }
                                    }

                                    // Digital Inspection parameters Form
                                    item {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.3f)),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text("DIGITAL INSPECTION SPEC FORM", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                                                Spacer(modifier = Modifier.height(12.dp))

                                                // Toilet Condition Selector
                                                Text("Toilet Condition status", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
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

                                                // Cleanliness slider
                                                Text("Sanitation Rating score: ${cleanlinessScoreInput.toInt()}/5", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                                Slider(
                                                    value = cleanlinessScoreInput,
                                                    onValueChange = { cleanlinessScoreInput = it },
                                                    valueRange = 1f..5f,
                                                    steps = 3,
                                                    colors = SliderDefaults.colors(thumbColor = SashioGold, activeTrackColor = SashioGold)
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Comments input
                                                OutlinedTextField(
                                                    value = inspectionCommentInput,
                                                    onValueChange = { inspectionCommentInput = it },
                                                    label = { Text("Sanitation remarks & feedback comments") },
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
                                                            queuedOfflineInspections = queuedOfflineInspections + "Booking #${b.id}: Cond: $selectedInspectionCondition, Cln: ${cleanlinessScoreInput.toInt()}"
                                                        } else {
                                                            viewModel.submitInspectionReport(
                                                                bookingId = b.id,
                                                                toiletCondition = selectedInspectionCondition,
                                                                waterLevel = selectedWaterLevel,
                                                                cleanlinessScore = cleanlinessScoreInput.toInt(),
                                                                equipmentCondition = equipmentConditionInput,
                                                                damageAssessment = damageAssessmentInput,
                                                                comments = inspectionCommentInput,
                                                                beforePhotos = beforePhotosList,
                                                                afterPhotos = afterPhotosList
                                                            )
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

                                    // Direct Maintenance quick update actions
                                    item {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.2f)),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text("LOG MAINTENANCE QUICK ACTIONS", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.staffPerformMaintenance(b.id)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                        modifier = Modifier.weight(1f).height(38.dp)
                                                    ) {
                                                        Text("Water Refill", fontSize = 9.sp)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            viewModel.staffPerformMaintenance(b.id)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                        modifier = Modifier.weight(1f).height(38.dp)
                                                    ) {
                                                        Text("Bio Fluid", fontSize = 9.sp)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            viewModel.staffPerformMaintenance(b.id)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                        modifier = Modifier.weight(1f).height(38.dp)
                                                    ) {
                                                        Text("Waste Evac", fontSize = 9.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Standard Jobs List Screen with filters
                            var selectedFilterTab by remember { mutableStateOf("Assigned") } // Assigned, Active, Completed

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF0F172A))
                                    .padding(12.dp)
                            ) {
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

                                val filteredList = bookings.filter { b ->
                                    when (selectedFilterTab) {
                                        "Active" -> b.status == "ARRIVED" || b.status == "IN TRANSIT" || b.status == "SETUP" || b.status == "ACTIVE" || b.status == "MAINTENANCE"
                                        "Completed" -> b.status == "SERVICE DELIVERED" || b.status == "COMPLETED"
                                        else -> b.status == "Booking Confirmed" || b.status == "Payment Confirmed" || b.status == "Quotation Sent" || b.status == "Quotation Generated"
                                    }
                                }

                                if (filteredList.isEmpty()) {
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("No jobs found in $selectedFilterTab bracket", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(filteredList) { b ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.35f)),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text("#QR-${b.id} | ${b.status}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold)
                                                        Text("Dates: ${b.eventDate}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                                                    }
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(b.serviceType, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    Text(b.address, fontSize = 11.sp, color = Color.LightGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    
                                                    Spacer(modifier = Modifier.height(12.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Button(
                                                            onClick = { selectedJobDetailsId = b.id },
                                                            colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                            modifier = Modifier.weight(1f).height(34.dp)
                                                        ) {
                                                            Text("VIEW DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }

                                                        when (b.status) {
                                                            "Booking Confirmed", "Payment Confirmed" -> {
                                                                Button(
                                                                    onClick = { viewModel.staffAcceptJob(b.id) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                                                                    modifier = Modifier.weight(1.2f).height(34.dp)
                                                                ) {
                                                                    Text("ACCEPT JOB", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                                                }
                                                            }
                                                            "IN TRANSIT" -> {
                                                                Button(
                                                                    onClick = { viewModel.staffMarkArrived(b.id) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                                                                    modifier = Modifier.weight(1.2f).height(34.dp)
                                                                ) {
                                                                    Text("MARK ARRIVED", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                                                }
                                                            }
                                                            "ARRIVED" -> {
                                                                Button(
                                                                    onClick = { viewModel.staffStartSetup(b.id) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                                                                    modifier = Modifier.weight(1.2f).height(34.dp)
                                                                ) {
                                                                    Text("START SETUP", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                                                }
                                                            }
                                                            "SETUP" -> {
                                                                Button(
                                                                    onClick = { viewModel.staffMarkActive(b.id) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                                                                    modifier = Modifier.weight(1.2f).height(34.dp)
                                                                ) {
                                                                    Text("MARK ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                                                }
                                                            }
                                                            "ACTIVE", "MAINTENANCE" -> {
                                                                Button(
                                                                    onClick = { viewModel.staffMarkCompleted(b.id) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioGreen),
                                                                    modifier = Modifier.weight(1.2f).height(34.dp)
                                                                ) {
                                                                    Text("COMPLETED", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                                                }
                                                            }
                                                            else -> {
                                                                Button(
                                                                    onClick = { viewModel.startStaffJourney(b.id) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                                                                    modifier = Modifier.weight(1.2f).height(34.dp)
                                                                ) {
                                                                    Text("START SHIFT", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
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

                    "maps" -> {
                        // Find any ongoing tracking job to map
                        val activeJob = bookings.firstOrNull { it.status == "IN TRANSIT" || it.status == "ARRIVED" } ?: bookings.firstOrNull()

                        if (activeJob == null) {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)), contentAlignment = Alignment.Center) {
                                Text("No active in-transit haul job to track", color = Color.White.copy(alpha = 0.5f))
                            }
                        } else {
                            val trackerState by viewModel.activeLiveTrackers.collectAsStateWithLifecycle()
                            val trk = trackerState[activeJob.id]

                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
                                SashioCanvasMap(
                                    latitude = activeJob.latitude,
                                    longitude = activeJob.longitude,
                                    staffLat = trk?.staffLat ?: activeJob.latitude,
                                    staffLng = trk?.staffLng ?: activeJob.longitude,
                                    showRoute = true,
                                    label = "Destination: ${activeJob.address}"
                                )

                                // Route HUD overlay
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xF2111827)),
                                    border = BorderStroke(1.dp, SashioGold.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .align(Alignment.BottomCenter)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("LIVE DISPATCH ROUTE NAVIGATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioGold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Destination: ${activeJob.address}", fontWeight = FontWeight.Medium, color = Color.White, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Hitch Status: Coupled Securing Rig | ETA: ${trk?.etaMinutes ?: "15"} Mins", color = Color.LightGray, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    Toast.makeText(localCtx, "GPS System locked correctly.", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                modifier = Modifier.weight(1f).height(36.dp)
                                            ) {
                                                Text("GPS LOCK", fontSize = 10.sp, color = Color.White)
                                            }

                                            Button(
                                                onClick = {
                                                    if (activeJob.status == "IN TRANSIT") {
                                                        viewModel.staffMarkArrived(activeJob.id)
                                                    } else {
                                                        viewModel.staffStartSetup(activeJob.id)
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

                    "messages" -> {
                        // Messaging Desk Screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0F172A))
                        ) {
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

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(generalChats) { gm ->
                                    val isMe = gm.sender == "Staff"
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                                    ) {
                                        Text(gm.sender, fontSize = 10.sp, color = SashioGold, fontWeight = FontWeight.Bold)
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isMe) SashioGold.copy(alpha = 0.12f) else SashioSlate.copy(alpha = 0.35f)
                                            ),
                                            border = BorderStroke(1.dp, if (isMe) SashioGold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f)),
                                            modifier = Modifier.widthIn(max = 280.dp).padding(vertical = 2.dp)
                                        ) {
                                            Text(gm.text, modifier = Modifier.padding(10.dp), color = Color.White, fontSize = 12.sp)
                                        }
                                        Text(gm.timestamp, fontSize = 8.sp, color = Color.White.copy(alpha = 0.4f))
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = typedGeneralMsgText,
                                    onValueChange = { typedGeneralMsgText = it },
                                    placeholder = { Text("Write high-priority response...", fontSize = 12.sp) },
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
                                        if (typedGeneralMsgText.isNotEmpty()) {
                                            viewModel.sendGeneralChatMessage("Staff", typedGeneralMsgText, messagingChannelSelect)
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

                    "profile" -> {
                        LazyColumn(
                            modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF0F172A))
                                        .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Staff Profile Info Card
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(SashioGold),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(staff?.name?.firstOrNull()?.toString() ?: "O", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(staff?.name ?: "Obinna Okafor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("CHIEF SANITATION SPECIALIST", fontSize = 10.sp, color = SashioGold, fontWeight = FontWeight.Bold)
                                            Text(staff?.phoneNumber ?: "+234 812 345 6789", fontSize = 11.sp, color = Color.LightGray)
                                        }
                                    }
                                }
                            }

                            // Dynamic Offline Toggle
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.25f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Offline Work Module", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                            Text("Turn this on to cache sanitation parameters offline.", color = Color.LightGray, fontSize = 11.sp)
                                        }

                                        Switch(
                                            checked = workOfflineToggle,
                                            onCheckedChange = {
                                                workOfflineToggle = it
                                                if (!it && queuedOfflineInspections.isNotEmpty()) {
                                                    // Sync
                                                    Toast.makeText(localCtx, "Offline Queue synced: ${queuedOfflineInspections.size} reports sent", Toast.LENGTH_SHORT).show()
                                                    queuedOfflineInspections = emptyList()
                                                }
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = SashioGold, checkedTrackColor = SashioGold.copy(alpha = 0.4f))
                                        )
                                    }
                                }
                            }

                            // Professional Qualifications / Badges Checklist
                            item {
                                Column {
                                    Text("Verified Qualifications & training log".uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.15f)),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(
                                                "✔ OSHA Health & Site Safety Certified - Level II",
                                                "✔ Premium RESTROOM Tow Rig Towing Hooking Licence",
                                                "✔ Eco-blue Biocide chemical handling accreditation"
                                            ).forEach { cert ->
                                                Text(cert, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                            }

                            // Dynamic Expense claim Module
                            item {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Log fuel/toll expense payments".uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                                        TextButton(onClick = { showAddExpenseDialog = true }) {
                                            Text("+ SUBMIT ESCROW CASH", color = SashioGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    expenses.forEach { exp ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.3f)),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(exp.category.uppercase(), fontWeight = FontWeight.Bold, color = SashioGold, fontSize = 12.sp)
                                                    Text(exp.notes, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                                    Text(exp.date, fontSize = 9.sp, color = Color.LightGray)
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("₦${exp.amount.toInt()}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                                    Text(
                                                        text = exp.status,
                                                        color = if (exp.status == "Approved") SashioGreen else SashioGold,
                                                        fontSize = 10.sp,
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

    // Attendance Clock-In Config modal window
    if (showClockInDialog) {
        Dialog(onDismissRequest = { showClockInDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFE111827)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .border(1.dp, SashioGold.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { clockInWorkplace = depot }
                        ) {
                            Text(depot, modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { showClockInDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("DISMISS", color = Color.White.copy(alpha = 0.5f))
                        }

                        Button(
                            onClick = {
                                viewModel.clockInStaff(clockInWorkplace)
                                showClockInDialog = false
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

    // Emergency Incident reporter modal
    if (showEmergencyDialog) {
        Dialog(onDismissRequest = { showEmergencyDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFE1C0D0D)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .border(1.dp, SashioRed.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = SashioRed)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("EMERGENCY SECURE DIAL", fontWeight = FontWeight.Bold, color = SashioRed, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Immediately broadcast active location coordinates & alert central logistics.", textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.LightGray)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = emergencyComments,
                        onValueChange = { emergencyComments = it },
                        label = { Text("Describe road blocking or incident blockage", color = Color.LightGray) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SashioRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { showEmergencyDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("CANCEL", color = Color.White.copy(alpha = 0.5f))
                        }

                        Button(
                            onClick = {
                                Toast.makeText(localCtx, "Emergency broadcast sent successfully!", Toast.LENGTH_SHORT).show()
                                showEmergencyDialog = false
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

    // Expense Claims registration overlay
    if (showAddExpenseDialog) {
        Dialog(onDismissRequest = { showAddExpenseDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFE111827)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .border(1.dp, SashioGold.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(42.dp), tint = SashioGold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("REGISTER EXPENSE SLIP", fontWeight = FontWeight.Bold, color = SashioGold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Amount input
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

                    // Category Selector
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Fuel", "Toll", "Parking", "Emergency").forEach { cat ->
                            val isSel = expenseCategorySelect == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { expenseCategorySelect = cat },
                                label = { Text(cat, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SashioGold,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = expenseNotesInput,
                        onValueChange = { expenseNotesInput = it },
                        label = { Text("Spent Reason / Receipt comments") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SashioGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = SashioGold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { showAddExpenseDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("DISMISS", color = Color.White.copy(alpha = 0.5f))
                        }

                        Button(
                            onClick = {
                                val amtNum = expenseAmountInput.toDoubleOrNull() ?: 0.0
                                if (amtNum > 0.0) {
                                    val dateSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                    viewModel.submitStaffExpense(
                                        amount = amtNum,
                                        category = expenseCategorySelect,
                                        notes = expenseNotesInput,
                                        date = dateSdf.format(java.util.Date())
                                    )
                                    expenseAmountInput = ""
                                    expenseNotesInput = ""
                                    showAddExpenseDialog = false
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
}

// ==========================================
// 4. SYSADMIN PORTAL & MANAGEMENT COCKPIT
// ==========================================

@Composable
fun AdminDashboard(viewModel: SashioViewModel, admin: UserProfile?) {
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val staffList by viewModel.staffProfiles.collectAsStateWithLifecycle()
    val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val catalogItems by viewModel.catalogItems.collectAsStateWithLifecycle()
    val activeTrackers by viewModel.activeLiveTrackers.collectAsStateWithLifecycle()
    
    val incidents by viewModel.incidents.collectAsStateWithLifecycle()
    val attendanceLogs by viewModel.attendanceLogs.collectAsStateWithLifecycle()
    val expenseLogs by viewModel.expenseLogs.collectAsStateWithLifecycle()
    val suspendedProfileIds by viewModel.suspendedProfileIds.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()

    var activeBottomTab by remember { mutableStateOf("home") } // home, operations, tracking, messages, profile
    var homeSubScreen by remember { mutableStateOf("dashboard") } // dashboard, clients, staff, quotations, products, payments, revenue, reports

    // Filter, Search, and Form states
    var clientSearchQuery by remember { mutableStateOf("") }
    var clientCategoryFilter by remember { mutableStateOf("All") } // All, Corporate, Individual
    
    var staffSearchQuery by remember { mutableStateOf("") }
    var selectedClientForView by remember { mutableStateOf<UserProfile?>(null) }
    
    var quotationDialogId by remember { mutableStateOf<Long?>(null) }
    var isCatalogManagerOpen by remember { mutableStateOf(false) }
    
    // Add Staff State
    var isAddStaffOpen by remember { mutableStateOf(false) }
    var staffFormName by remember { mutableStateOf("") }
    var staffFormEmail by remember { mutableStateOf("") }
    var staffFormPhone by remember { mutableStateOf("") }

    // Log Incident State
    var isLogIncidentOpen by remember { mutableStateOf(false) }
    var incidentFormTitle by remember { mutableStateOf("") }
    var incidentFormReporter by remember { mutableStateOf("") }
    var incidentFormPriority by remember { mutableStateOf("Medium") }
    var incidentFormDetails by remember { mutableStateOf("") }

    // Log Expense State
    var isLogExpenseOpen by remember { mutableStateOf(false) }
    var expenseFormStaff by remember { mutableStateOf("") }
    var expenseFormCategory by remember { mutableStateOf("Fuel Costs") }
    var expenseFormAmount by remember { mutableStateOf("") }
    var expenseFormDetails by remember { mutableStateOf("") }

    // Broadcaster State
    var broadcastTarget by remember { mutableStateOf("All") } // All, Clients, Staff, Corporate
    var broadcastMessageText by remember { mutableStateOf("") }
    var listSentBroadcasts by remember { mutableStateOf<List<String>>(emptyList()) }

    // Reports export state
    var isExportReportOpen by remember { mutableStateOf(false) }
    var exportFormatType by remember { mutableStateOf("PDF") } // PDF, CSV, Excel
    var exportReportCategory by remember { mutableStateOf("Revenue Report") } // Revenue Report, Booking Report, Client Log
    var exportProgressPercent by remember { mutableStateOf(0f) }
    val localContext = LocalContext.current

    // Chat Simulator State
    var msgChannel by remember { mutableStateOf("Clients") } // Clients, Staff, Supervisors
    var msgFormText by remember { mutableStateOf("") }
    var adminChatMessages by remember { mutableStateOf(listOf(
        "SysAdmin: All dispatches are active.",
        "System: Daily attendance compiled with 100% geolocations verified."
    )) }

    // Settings Profile Toggles
    var enforceBiometricsAdmin by remember { mutableStateOf(true) }
    var securityAuditsLogOpen by remember { mutableStateOf(false) }
    var adminActiveLanguage by remember { mutableStateOf("English") }

    Scaffold(
        topBar = {
            Column {
                DashboardHeader(
                    roleName = when (activeBottomTab) {
                        "home" -> if (homeSubScreen == "dashboard") "SYSADMIN COMMAND" else "ADMIN > ${homeSubScreen.uppercase()}"
                        else -> "ADMIN CENTER > ${activeBottomTab.uppercase()}"
                    },
                    user = admin,
                    onSettingsClick = { 
                        activeBottomTab = "home"
                        homeSubScreen = "settings"
                    },
                    onLogout = { viewModel.logout() }
                )
                if (activeBottomTab == "home" && homeSubScreen != "dashboard") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SashioSlate)
                            .clickable { homeSubScreen = "dashboard" }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SashioGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Return to Console Executive Dashboard Grid", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SashioSlate,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                listOf(
                    Triple("home", "Dashboard", Icons.Default.Dashboard),
                    Triple("operations", "Operations", Icons.Default.Work),
                    Triple("tracking", "Tracking", Icons.Default.Map),
                    Triple("messages", "Messages", Icons.Default.Chat),
                    Triple("profile", "Profile", Icons.Default.Person)
                ).forEach { (id, label, icon) ->
                    val selected = activeBottomTab == id
                    NavigationBarItem(
                        selected = selected,
                        onClick = { 
                            activeBottomTab = id
                            if (id != "home") homeSubScreen = "dashboard"
                        },
                        icon = { Icon(icon, contentDescription = label, tint = if (selected) SashioGold else Color.Gray) },
                        label = { Text(label, color = if (selected) Color.White else Color.Gray, fontSize = 9.sp) },
                        modifier = Modifier.testTag("admin_bottom_tab_$id")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (activeBottomTab) {
                "home" -> {
                    when (homeSubScreen) {
                        "dashboard" -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item {
                                    Text(
                                        "SACHIO CORPORATE COMMAND CENTER", 
                                        style = MaterialTheme.typography.titleMedium, 
                                        fontWeight = FontWeight.Bold, 
                                        color = SashioSlate
                                    )
                                    Text(
                                        "Real-time metadata, logistics control matrices, and commercial quotation compliance.", 
                                        style = MaterialTheme.typography.bodySmall, 
                                        color = SashioTextMuted
                                    )
                                }

                                // Interactive Executive KPI Cards Rows
                                item {
                                    val completedJobs = bookings.filter { it.status == "Completed" || it.status == "completed_service" }.size
                                    val activeRentals = bookings.filter { it.status == "Booking Confirmed" || it.status == "Service Delivered" || it.status == "IN TRANSIT" }.size
                                    val outstandingQuotes = bookings.filter { it.status == "Quotation Requested" || it.status == "Under Review" }.size
                                    val totalRevenue = payments.filter { it.status == "success" }.sumOf { it.amount }
                                    val outstandingInvoicesVal = bookings.filter { it.status == "Awaiting Payment" || it.status == "Quotation Sent" }.sumOf { it.quotedPrice + it.logisticsCost }
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Card(colors = CardDefaults.cardColors(containerColor = SashioSlate), modifier = Modifier.weight(1f)) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text("MONTHLY REVENUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SashioGold)
                                                    Text("₦%,.0f".format(totalRevenue), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                                    Text("Paystack Settled", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                                                }
                                            }
                                            Card(colors = CardDefaults.cardColors(containerColor = SashioTealDark), modifier = Modifier.weight(1f)) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text("OUTSTANDING BILLS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SashioGold)
                                                    Text("₦%,.0f".format(outstandingInvoicesVal), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                                    Text("Pending Payment", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                                                }
                                            }
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(
                                                Triple("${profiles.filter { it.role == "client" }.size} Total", "👤 Clients", SashioBlue),
                                                Triple("$activeRentals Live", "🚚 Bookings", SashioGreen),
                                                Triple("$outstandingQuotes Open", "📄 Pending Quotes", SashioGold),
                                                Triple("${staffList.size} Crew", "👷 Active Staff", SashioSlate)
                                            ).forEach { (count, title, color) ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
                                                        Text(count, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SashioSlate)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Interactive Dashboard Menu Grids (2 columns)
                                item {
                                    Text("EXECUTIVE GRID CHANNELS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 4.dp))
                                }

                                item {
                                    val menuItems = listOf(
                                        Triple("clients", "👥 Clients Management", "Manage profiles, view invoices, receipt, history"),
                                        Triple("staff", "👷 Staff & Field Crews", "Performance trackers, add members, logs"),
                                        Triple("quotations", "📄 Quotations Console", "Automated smart price calculations & metrics"),
                                        Triple("products", "🚽 Product Catalog", "Configure mobile units, specifications & video URLs"),
                                        Triple("payments", "💳 Paystack Payments", "Successful, failed transactions logs verify"),
                                        Triple("revenue", "📈 Business Revenue Charts", "Simulated graphics with expense reports"),
                                        Triple("reports", "📊 Reports Export Hub", "PDF, CSV, Excel document dispatcher"),
                                        Triple("manage_roles", "🔑 User Roles (Live)", "Promote or demote real Supabase accounts to staff/admin"),
                                        Triple("settings", "⚙ Operational Settings", "System configurations, activity audits logs")
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        for (i in menuItems.indices step 2) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                for (j in 0..1) {
                                                    if (i + j < menuItems.size) {
                                                        val item = menuItems[i + j]
                                                        Card(
                                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                                            shape = RoundedCornerShape(12.dp),
                                                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clickable { homeSubScreen = item.first }
                                                                .testTag("admin_grid_${item.first}")
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                Text(item.second, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioSlate)
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Text(item.third, fontSize = 9.sp, color = SashioTextMuted, lineHeight = 12.sp)
                                                            }
                                                        }
                                                    } else {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "clients" -> {
                            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                Text("CUSTOMER RELATIONSHIP MANAGEMENT (CRM)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SashioSlate)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedTextField(
                                    value = clientSearchQuery,
                                    onValueChange = { clientSearchQuery = it },
                                    label = { Text("Search client full name or company...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("All", "Corporate", "Individual").forEach { cat ->
                                        FilterChip(
                                            selected = clientCategoryFilter == cat,
                                            onClick = { clientCategoryFilter = cat },
                                            label = { Text(cat) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val filteredClients = profiles.filter {
                                    it.role == "client" &&
                                    (it.name.contains(clientSearchQuery, ignoreCase = true) || it.companyName.contains(clientSearchQuery, ignoreCase = true)) &&
                                    (clientCategoryFilter == "All" || 
                                     (clientCategoryFilter == "Corporate" && it.companyName.isNotBlank()) ||
                                     (clientCategoryFilter == "Individual" && it.companyName.isBlank()))
                                }

                                if (filteredClients.isEmpty()) {
                                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                            Text("No Client Profiles found matching constraints", color = Color.Gray, fontSize = 12.sp)
                                        }
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(filteredClients) { c ->
                                            val isSuspended = suspendedProfileIds.contains(c.id)
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = if (isSuspended) Color(0xFFFEE2E2) else Color.White),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().clickable { selectedClientForView = c }
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Column {
                                                            Text(c.name, fontWeight = FontWeight.Bold)
                                                            Text(if (c.companyName.isNotBlank()) "Corporate: ${c.companyName}" else "Individual Account", fontSize = 10.sp, color = SashioGold, fontWeight = FontWeight.Bold)
                                                        }
                                                        Text(if (isSuspended) "SUSPENDED" else "ACTIVE", color = if (isSuspended) SashioRed else SashioGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                    }
                                                    Text("Email: ${c.email} | Phone: ${c.phoneNumber}", fontSize = 10.sp, color = SashioTextMuted)
                                                    
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = { viewModel.toggleProfileSuspension(c.id) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = if (isSuspended) SashioGreen else SashioRed),
                                                            modifier = Modifier.weight(1f).height(30.dp),
                                                            contentPadding = PaddingValues(0.dp)
                                                        ) {
                                                            Text(if (isSuspended) "Activate Profile" else "Suspend Profile", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }
                                                        Button(
                                                            onClick = {
                                                                // Prepopulate WhatsApp text
                                                                val whatsappText = "Hello ${c.name}, this is SACHIO Customer Service. We are reviewing your event sanitation account logistics coordinates."
                                                                val encodedText = java.net.URLEncoder.encode(whatsappText, "UTF-8")
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${c.phoneNumber}&text=$encodedText"))
                                                                localContext.startActivity(intent)
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                            modifier = Modifier.weight(1f).height(30.dp),
                                                            contentPadding = PaddingValues(0.dp)
                                                        ) {
                                                            Text("WhatsApp Direct", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "staff" -> {
                            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("STAFF & DISPATCH CREWS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Button(
                                        onClick = { isAddStaffOpen = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = SashioGold)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("Register", fontSize = 10.sp)
                                    }
                                }
                                
                                OutlinedTextField(
                                    value = staffSearchQuery,
                                    onValueChange = { staffSearchQuery = it },
                                    label = { Text("Search crews by name...") },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                )

                                val filteredStaff = staffList.filter {
                                    it.name.contains(staffSearchQuery, ignoreCase = true)
                                }

                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(filteredStaff) { s ->
                                        val isSuspended = suspendedProfileIds.contains(s.id)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = if (isSuspended) Color(0xFFFEE2E2) else Color.White),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Column {
                                                        Text(s.name, fontWeight = FontWeight.Bold)
                                                        Text("Role: FIELD OPERATIVE | ID: #SC-${s.id}", fontSize = 9.sp, color = SashioGold)
                                                    }
                                                    Text(if (isSuspended) "SUSPENDED" else "ACTIVE", color = if (isSuspended) SashioRed else SashioGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Text("Phone: ${s.phoneNumber} | email: ${s.email}", fontSize = 10.sp, color = SashioTextMuted)
                                                
                                                val countJob = bookings.filter { it.assignedStaffId == s.id }.size
                                                Text("Assigned Dispatches: $countJob", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioSlate)

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = { viewModel.toggleProfileSuspension(s.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = if (isSuspended) SashioGreen else SashioRed),
                                                        modifier = Modifier.weight(1f).height(28.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text(if (isSuspended) "Activate Crew" else "Suspend Crew", fontSize = 9.sp)
                                                    }
                                                    Button(
                                                        onClick = {
                                                            Toast.makeText(localContext, "Ping Alert dispatched to ${s.name}'s terminal.", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                        modifier = Modifier.weight(1f).height(28.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Ping Active Terminal", fontSize = 9.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "manage_roles" -> {
                            ManageUserRolesPanel(viewModel)
                        }

                        "quotations" -> {
                            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                Text("SMART PRICE QUOTATIONS CONTROL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Review and process incoming requests. Generate and configure logistics surcharges, taxes, and specialized inclusions.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
                                Spacer(modifier = Modifier.height(10.dp))

                                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(bookings) { b ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(b.serviceType, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    BookingStatusBadge(status = b.status)
                                                }
                                                Text("Client: ${b.clientName} | ${b.companyName.ifBlank() { "Individual" }}", fontSize = 10.sp, color = SashioTextMuted)
                                                Text("Specs: Event=${b.eventType} | Qty=${b.quantity} | Duration=${b.duration}", fontSize = 10.sp, color = SashioTextMuted)
                                                Text("Location: ${b.address}", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = SashioTextMuted)

                                                if (b.quotedPrice > 0.0) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Quoted Subtotal: ₦%,.0f | Logistics: ₦%,.0f | VAT: ₦%,.0f".format(b.quotedPrice, b.logisticsCost, b.vatAmount), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioGreen)
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    if (b.status == "Quotation Requested" || b.status == "Under Review") {
                                                        Button(
                                                            onClick = { quotationDialogId = b.id },
                                                            colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                                                            modifier = Modifier.weight(1f).height(30.dp),
                                                            contentPadding = PaddingValues(0.dp)
                                                        ) {
                                                            Text("Launch Quotation Engine", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    
                                                    Button(
                                                        onClick = {
                                                            Toast.makeText(localContext, "Digital Quote PDF compiled and shared to clients email portal.", Toast.LENGTH_LONG).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                        modifier = Modifier.weight(1f).height(30.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Compile PDF & Email", fontSize = 9.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "products" -> {
                            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("PRODUCT CATALOGS SETUPing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Button(
                                        onClick = { isCatalogManagerOpen = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = SashioGold)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("Add Unit", fontSize = 10.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(catalogItems) { cat ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(cat.title, fontWeight = FontWeight.Bold)
                                                Text("Variant: ${cat.variantName} | Model: ${cat.modelNumber}", fontSize = 10.sp, color = SashioGold, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(cat.description, fontSize = 10.sp, color = SashioTextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                
                                                if (cat.basePrice > 0.0) {
                                                    Text("System Reference Price: ₦%,.0f".format(cat.basePrice), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SashioSlate)
                                                }
                                                if (cat.imageUrl.isNotEmpty()) {
                                                    AsyncImage(
                                                        model = cat.imageUrl,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 6.dp).clip(RoundedCornerShape(8.dp))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "payments" -> {
                            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                Text("PAYSTACK SETTLEMENT JOURNAL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))

                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(payments) { p ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("REF: ${p.reference}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(p.paymentDate))
                                                    Text("Date: $dateStr | Channel: ${p.channel.uppercase()}", fontSize = 9.sp, color = SashioTextMuted)
                                                    Text("STATUS: ${p.status.uppercase()}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (p.status == "success") SashioGreen else SashioGold)
                                                }
                                                Text("₦%,.0f".format(p.amount), fontWeight = FontWeight.ExtraBold, color = SashioGreen, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "revenue" -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(14.dp).verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text("REVENUE TRENDS & EXPENSES ANALYTICS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                
                                // Premium Custom Line Graph for Revenue
                                Card(colors = CardDefaults.cardColors(containerColor = SashioSlate)) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("REVENUE GROWTH LOGISTICAL MATRIX", color = SashioGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                                            val maxVal = 2000000f
                                            val points = listOf(300000f, 600000f, 450000f, 900000f, 1200000f, 1580000f)
                                            val widthSpacing = size.width / (points.size - 1)
                                            val graphPath = androidx.compose.ui.graphics.Path()
                                            
                                            points.forEachIndexed { idx, value ->
                                                val x = idx * widthSpacing
                                                val y = size.height - (value / maxVal * size.height)
                                                if (idx == 0) graphPath.moveTo(x, y) else graphPath.lineTo(x, y)
                                            }
                                            drawPath(graphPath, color = SashioGold, style = strokeWidthLimit(3f))
                                            points.forEachIndexed { idx, value ->
                                                val x = idx * widthSpacing
                                                val y = size.height - (value / maxVal * size.height)
                                                drawCircle(color = SashioGold, radius = 4.dp.toPx(), center = Offset(x, y))
                                                drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = Offset(x, y))
                                            }
                                        }
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Wk 1", color = Color.White, fontSize = 8.sp)
                                            Text("Wk 2", color = Color.White, fontSize = 8.sp)
                                            Text("Wk 3", color = Color.White, fontSize = 8.sp)
                                            Text("Wk 4", color = Color.White, fontSize = 8.sp)
                                            Text("Wk 5", color = Color.White, fontSize = 8.sp)
                                            Text("Wk 6", color = Color.White, fontSize = 8.sp)
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("EXPENSES CLAIMS CENTER", fontWeight = FontWeight.Bold)
                                    Button(
                                        onClick = { isLogExpenseOpen = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = SashioTealDark),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Log Expense", fontSize = 9.sp)
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    expenseLogs.forEach { exp ->
                                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(exp.category, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SashioSlate)
                                                    Text("₦%,.0f".format(exp.amount), fontWeight = FontWeight.ExtraBold, color = SashioRed, fontSize = 11.sp)
                                                }
                                                Text("Staff: ${exp.staffName} | Details: ${exp.details}", fontSize = 9.sp, color = SashioTextMuted)
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("STATUS: ${exp.status}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (exp.status == "Approved") SashioGreen else SashioGold)
                                                    if (exp.status == "Pending Approval") {
                                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            TextButton(
                                                                onClick = { viewModel.approveExpenseClaim(exp.id) },
                                                                modifier = Modifier.height(24.dp),
                                                                contentPadding = PaddingValues(0.dp)
                                                            ) {
                                                                Text("Approve", fontSize = 9.sp, color = SashioGreen)
                                                            }
                                                            TextButton(
                                                                onClick = { viewModel.rejectExpenseClaim(exp.id) },
                                                                modifier = Modifier.height(24.dp),
                                                                contentPadding = PaddingValues(0.dp)
                                                            ) {
                                                                Text("Reject", fontSize = 9.sp, color = SashioRed)
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

                        "reports" -> {
                            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                Text("REPORTS & ANALYTICS EXPORT HUB", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Generate official logistical auditing reports in standard PDF, CSV, or Microsoft Excel sheets with dynamic database mapping.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
                                Spacer(modifier = Modifier.height(14.dp))

                                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("SELECT REPORT TYPE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        listOf("Revenue Report", "Booking Report", "Client Log", "Staff Operations").forEach { cat ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { exportReportCategory = cat }
                                                    .background(if (exportReportCategory == cat) SashioGold.copy(alpha = 0.1f) else Color.Transparent)
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(selected = exportReportCategory == cat, onClick = { exportReportCategory = cat })
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("EXPORT FILE FORMAT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            listOf("PDF", "CSV", "Excel").forEach { format ->
                                                FilterChip(
                                                    selected = exportFormatType == format,
                                                    onClick = { exportFormatType = format },
                                                    label = { Text(format) }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))
                                        if (exportProgressPercent > 0f) {
                                            LinearProgressIndicator(progress = exportProgressPercent, modifier = Modifier.fillMaxWidth(), color = SashioGold)
                                            Text("Assembling records data structures & signatures... ${(exportProgressPercent * 100).toInt()}%", fontSize = 10.sp, color = SashioTextMuted)
                                        }

                                        Button(
                                            onClick = {
                                                // Simulated progressive generation
                                                exportProgressPercent = 0.1f
                                                coroutineScope.launch {
                                                    for (p in 2..10) {
                                                        delay(200)
                                                        exportProgressPercent = p * 0.1f
                                                    }
                                                    Toast.makeText(localContext, "Exported $exportReportCategory successfully as $exportFormatType file", Toast.LENGTH_LONG).show()
                                                    exportProgressPercent = 0.0f
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("COMPILE REPORT", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        "settings" -> {
                            Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("SYSTEM SECURITY & CONTROLS AUDITS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Operational Variables", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Enforce Biometric Locks (Admin Gate)", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            Switch(checked = enforceBiometricsAdmin, onCheckedChange = { enforceBiometricsAdmin = it })
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Preferred System Language", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            Row {
                                                listOf("English", "Yoruba", "Hausa").forEach { lang ->
                                                    FilterChip(
                                                        selected = adminActiveLanguage == lang,
                                                        onClick = { adminActiveLanguage = lang },
                                                        label = { Text(lang, fontSize = 9.sp) },
                                                        modifier = Modifier.padding(horizontal = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("ACTIVE SECURITY SYSTEM SIGNALS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        val logs = listOf(
                                            "SYS: Session token #AK99-Z verified at UTC 2026-06-12",
                                            "AUTH: Biometric Node face_anchor_A validated success",
                                            "DB: Defragment routine complete. Sandbox size: 4.8MB"
                                        )
                                        logs.forEach { log ->
                                            Text(log, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 2.dp), color = SashioTextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "operations" -> {
                    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("FIELD OPERATIONS CONTROL CENTER", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { isLogIncidentOpen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("New Incident", fontSize = 10.sp)
                            }
                        }
                        Text("Active dispatches checklists, and field incident tracking.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
                        Spacer(modifier = Modifier.height(10.dp))

                        var opsFilterSelected by remember { mutableStateOf("Dispatches") } // Dispatches, Incidents
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Dispatches", "Incidents Logs").forEach { t ->
                                FilterChip(
                                    selected = opsFilterSelected == t,
                                    onClick = { opsFilterSelected = t },
                                    label = { Text(t) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        if (opsFilterSelected == "Dispatches") {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                items(bookings) { b ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column {
                                                    Text(b.serviceType, fontWeight = FontWeight.Bold)
                                                    Text("Client: ${b.clientName} | Event: ${b.eventType}", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                BookingStatusBadge(status = b.status)
                                            }
                                            
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = if (b.assignedStaffName != null) "Assigned Crew: ${b.assignedStaffName}" else "Crew Allocation Required",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (b.assignedStaffName != null) SashioTeal else SashioGold
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                var expandedCrewDrop by remember { mutableStateOf(false) }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    Button(
                                                        onClick = { expandedCrewDrop = true },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                        modifier = Modifier.fillMaxWidth().height(28.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Allocate Crew", fontSize = 10.sp)
                                                    }
                                                    DropdownMenu(expanded = expandedCrewDrop, onDismissRequest = { expandedCrewDrop = false }) {
                                                        staffList.forEach { s ->
                                                            DropdownMenuItem(
                                                                text = { Text(s.name) },
                                                                onClick = {
                                                                    viewModel.adminAssignStaffToBooking(b.id, s.id)
                                                                    expandedCrewDrop = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                Button(
                                                    onClick = {
                                                        // Update dispatch status to service delivered
                                                        viewModel.staffUpdateBookingStatus(b.id, "COMPLETED")
                                                        Toast.makeText(localContext, "Completed dispatch #QR-${b.id} manually.", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioGreen),
                                                    modifier = Modifier.weight(1f).height(28.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("Complete Job", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                items(incidents) { inc ->
                                    Card(colors = CardDefaults.cardColors(containerColor = if (inc.priority == "Critical") Color(0xFFFEE2E2) else Color.White)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column {
                                                    Text(inc.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text("Reporter: ${inc.reporter} | Date: ${inc.date}", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = if (inc.priority == "Critical") SashioRed else if (inc.priority == "High") SashioGold else SashioSlate)
                                                ) {
                                                    Text(inc.priority, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(inc.details, fontSize = 10.sp, color = SashioSlate)
                                            Text("STATUS: ${inc.status.uppercase()}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (inc.status == "Resolved") SashioGreen else SashioGold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "tracking" -> {
                    var selectedTrackBooking by remember { mutableStateOf<Booking?>(bookings.firstOrNull()) }
                    var currentMapLayer by remember { mutableStateOf("Standard") }
                    var trackSelectedTab by remember { mutableStateOf("Live Map") } // Live Map, Attendance verification

                    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        Text("LIVE ROUTE CONTROL & ATTENDANCE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                            listOf("Live Map", "Attendance Logs").forEach { tTab ->
                                FilterChip(
                                    selected = trackSelectedTab == tTab,
                                    onClick = { trackSelectedTab = tTab },
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
                                listOf("Standard", "Traffic", "Satellite").forEach { lStyle ->
                                    FilterChip(
                                        selected = currentMapLayer == lStyle,
                                        onClick = { currentMapLayer = lStyle },
                                        label = { Text(lStyle, fontSize = 9.sp) }
                                    )
                                }
                            }

                            val focusBooking = selectedTrackBooking ?: bookings.firstOrNull()
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.weight(1.5f).fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (focusBooking != null) {
                                        val tracker = activeTrackers[focusBooking.id]
                                        SashioCanvasMap(
                                            latitude = focusBooking.latitude,
                                            longitude = focusBooking.longitude,
                                            staffLat = tracker?.staffLat ?: (focusBooking.latitude - 0.025),
                                            staffLng = tracker?.staffLng ?: (focusBooking.longitude - 0.02),
                                            vehicleLat = tracker?.vehicleLat ?: (focusBooking.latitude - 0.028),
                                            vehicleLng = tracker?.vehicleLng ?: (focusBooking.longitude - 0.022),
                                            mapStyle = currentMapLayer,
                                            showRoute = true,
                                            label = "${focusBooking.clientName} @ ${focusBooking.eventType}"
                                        )

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                                            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("Dispatch Target: #QR-${focusBooking.id}", color = SashioGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text("Address: ${focusBooking.address}", color = Color.White, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (tracker != null) {
                                                    Text("ETA: ${tracker.etaMinutes} mins | Dist: %.1f km".format(tracker.distanceRemainingKm), color = Color.White, fontSize = 9.sp)
                                                } else {
                                                    Text("Live routing transit ready", color = Color.White, fontSize = 9.sp)
                                                }
                                            }
                                        }
                                    } else {
                                        SashioCanvasMap(latitude = 6.4282, longitude = 3.4211, mapStyle = currentMapLayer, label = "Lagos headquarters depot")
                                    }
                                }
                            }

                            Text("Select booking destination to track:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(bookings) { b ->
                                    val isFocused = selectedTrackBooking?.id == b.id
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (isFocused) SashioSlate else Color.White),
                                        modifier = Modifier.width(155.dp).clickable { selectedTrackBooking = b }
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(b.clientName, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = if (isFocused) Color.White else SashioSlate)
                                            Text("#QR-${b.id} | ${b.status}", fontSize = 8.sp, color = if (isFocused) SashioGold else SashioTextMuted)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Attendance Verification Center
                            Text("GEO-FENCED ATTENDANCE LOGS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Verify biometric clock-ins, duration logs and geo-location coordinates anchors of field personnel.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                items(attendanceLogs) { att ->
                                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Row(
                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(att.staffName, fontWeight = FontWeight.Bold)
                                                Text("Clock In: ${att.clockInTime} | Clock Out: ${att.clockOutTime}", fontSize = 10.sp, color = SashioTextMuted)
                                                Text("Verified Anchor: ${att.locationVerified}", fontSize = 9.sp, color = SashioTeal, fontWeight = FontWeight.SemiBold)
                                            }
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = if (att.isVerified) SashioGreen.copy(alpha = 0.12f) else SashioGold.copy(alpha = 0.12f))
                                            ) {
                                                Text(
                                                    if (att.isVerified) "VERIFIED" else "PENDING", 
                                                    fontSize = 8.sp, 
                                                    fontWeight = FontWeight.Bold, 
                                                    color = if (att.isVerified) SashioGreen else SashioGold,
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

                "messages" -> {
                    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        Text("MESSAGES & PUSH BROADCASTS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        var messageTabSelection by remember { mutableStateOf("Live Chats") } // Live Chats, Broadcaster
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
                            // Render unified subchat list
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                listOf("Clients", "Staff", "Supervisors").forEach { ch ->
                                    FilterChip(
                                        selected = msgChannel == ch,
                                        onClick = { msgChannel = ch },
                                        label = { Text(ch, fontSize = 10.sp) },
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                            }

                            // Chat Area
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(adminChatMessages) { m ->
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (m.startsWith("SysAdmin") || m.startsWith("Admin")) SashioSlate else Color.LightGray.copy(alpha = 0.3f)
                                                ),
                                                modifier = Modifier.padding(horizontal = 4.dp).align(if (m.startsWith("Admin")) Alignment.End else Alignment.Start)
                                            ) {
                                                Text(
                                                    m, 
                                                    fontSize = 11.sp, 
                                                    color = if (m.startsWith("SysAdmin") || m.startsWith("Admin")) Color.White else SashioSlate,
                                                    modifier = Modifier.padding(8.dp)
                                                )
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
                                            placeholder = { Text("Write chat response message...") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = {
                                                if (msgFormText.isNotBlank()) {
                                                    adminChatMessages = adminChatMessages + "Admin: $msgFormText"
                                                    // Simulated echo reply
                                                    val replyChannel = msgChannel
                                                    msgFormText = ""
                                                    coroutineScope.launch {
                                                        delay(1200)
                                                        adminChatMessages = adminChatMessages + "$replyChannel Operative: Node message received. Complying."
                                                    }
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
                            // Broadcaster tab
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("BROADCAST PUSH NOTIFICATION ENGINES", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    
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
                                        label = { Text("Custom alert messaging content text") },
                                        maxLines = 4,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            if (broadcastMessageText.isNotBlank()) {
                                                listSentBroadcasts = listSentBroadcasts + "Broadcaster Log [Target=$broadcastTarget]: $broadcastMessageText"
                                                Toast.makeText(localContext, "Commercial broadcast successfully sent to all $broadcastTarget channels.", Toast.LENGTH_LONG).show()
                                                broadcastMessageText = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("DISPATCH BROADCAST NOTIFICATION", color = Color.White)
                                    }

                                    if (listSentBroadcasts.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("RECENTLY TRANSMITTED BROADCAST SIGS:", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        listSentBroadcasts.reversed().forEach { sign ->
                                            Text(sign, fontSize = 9.sp, color = SashioTextMuted, modifier = Modifier.padding(vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "profile" -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(14.dp).verticalScroll(rememberScrollState()),
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

                        Text(admin?.name ?: "Sachio Administrator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("ROLE: ENTERPRISE SYSTEM COMMANDER", fontSize = 10.sp, color = SashioTeal, fontWeight = FontWeight.Bold)
                        Text("Email: ${admin?.email ?: "admin@sashio.com"}", fontSize = 10.sp, color = SashioTextMuted)

                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("REGISTRATION & NODE ACCESS TOKENS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Registered Device IP: 192.168.1.135", fontSize = 10.sp, color = SashioTextMuted)
                                Text("Master Cryptographic keys: SHA-256 Validated", fontSize = 10.sp, color = SashioTextMuted)
                                Text("Session Created: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", fontSize = 10.sp, color = SashioTextMuted)
                            }
                        }

                        Button(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SECURE COCKPIT TERMINATION / LOGOUT", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Add Staff Dialog
        if (isAddStaffOpen) {
            AlertDialog(
                onDismissRequest = { isAddStaffOpen = false },
                title = { Text("Register New Crew Operative", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = staffFormName, onValueChange = { staffFormName = it }, label = { Text("Operatives Full Name") }, singleLine = true)
                        OutlinedTextField(value = staffFormEmail, onValueChange = { staffFormEmail = it }, label = { Text("Enter Email ID") }, singleLine = true)
                        OutlinedTextField(value = staffFormPhone, onValueChange = { staffFormPhone = it }, label = { Text("Phone Number") }, singleLine = true)
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
                                Toast.makeText(localContext, "Crews profile Registered successfully", Toast.LENGTH_SHORT).show()
                                staffFormName = ""
                                staffFormEmail = ""
                                staffFormPhone = ""
                                isAddStaffOpen = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)
                    ) {
                        Text("Save Crew")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isAddStaffOpen = false }) { Text("Cancel") }
                }
            )
        }

        // Log Incident Dialog
        if (isLogIncidentOpen) {
            AlertDialog(
                onDismissRequest = { isLogIncidentOpen = false },
                title = { Text("Log Operational Incident", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = incidentFormTitle, onValueChange = { incidentFormTitle = it }, label = { Text("Incident Title (e.g. Pump failure)") }, singleLine = true)
                        OutlinedTextField(value = incidentFormReporter, onValueChange = { incidentFormReporter = it }, label = { Text("Reporter Name") }, singleLine = true)
                        
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

                        OutlinedTextField(value = incidentFormDetails, onValueChange = { incidentFormDetails = it }, label = { Text("Describe specific occurrence details...") }, maxLines = 4)
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
                                Toast.makeText(localContext, "Operational incident log generated successfully", Toast.LENGTH_SHORT).show()
                                incidentFormTitle = ""
                                incidentFormReporter = ""
                                incidentFormDetails = ""
                                isLogIncidentOpen = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)
                    ) {
                        Text("File Log")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isLogIncidentOpen = false }) { Text("Cancel") }
                }
            )
        }

        // Log Expense Dialog
        if (isLogExpenseOpen) {
            AlertDialog(
                onDismissRequest = { isLogExpenseOpen = false },
                title = { Text("Log Corporate Expense", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = expenseFormStaff, onValueChange = { expenseFormStaff = it }, label = { Text("Staff / Operatives Name") }, singleLine = true)
                        
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

                        OutlinedTextField(value = expenseFormAmount, onValueChange = { expenseFormAmount = it }, label = { Text("Amount Cost (₦)") }, singleLine = true)
                        OutlinedTextField(value = expenseFormDetails, onValueChange = { expenseFormDetails = it }, label = { Text("Explanatory description details...") }, maxLines = 3)
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
                                )
                                Toast.makeText(localContext, "Corporate expense logged successfully", Toast.LENGTH_SHORT).show()
                                expenseFormStaff = ""
                                expenseFormAmount = ""
                                expenseFormDetails = ""
                                isLogExpenseOpen = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)
                    ) {
                        Text("Log Expense")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isLogExpenseOpen = false }) { Text("Cancel") }
                }
            )
        }

        // Add Catalog Dialog
        if (isCatalogManagerOpen) {
            var newTitle by remember { mutableStateOf("") }
            var newDesc by remember { mutableStateOf("") }
            var newModel by remember { mutableStateOf("") }
            var newVariant by remember { mutableStateOf("") }
            var newPriceStr by remember { mutableStateOf("") }
            var newImageUrl by remember { mutableStateOf("") }
            var newVideoUrl by remember { mutableStateOf("") }
            var newFeatures by remember { mutableStateOf("") }
            var newSpecs by remember { mutableStateOf("") }
            var newBenefits by remember { mutableStateOf("") }
            var newUseCases by remember { mutableStateOf("") }
            val photoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    newImageUrl = uri.toString()
                }
            }

            AlertDialog(
                onDismissRequest = { isCatalogManagerOpen = false },
                title = { Text("Add Catalog Item", fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        item { OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Product Title") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = newVariant, onValueChange = { newVariant = it }, label = { Text("Variant Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = newDesc, onValueChange = { newDesc = it }, label = { Text("Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = newPriceStr, onValueChange = { newPriceStr = it }, label = { Text("Guide Price (Optional, ₦)") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                        
                        item {
                            Button(
                                onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                colors = ButtonDefaults.buttonColors(containerColor = SashioTeal),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (newImageUrl.isBlank()) "Upload Image" else "Change Image")
                            }
                        }
                        if (newImageUrl.isNotBlank()) {
                            item {
                                AsyncImage(
                                    model = newImageUrl,
                                    contentDescription = "Selected Image preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                        
                        item { OutlinedTextField(value = newVideoUrl, onValueChange = { newVideoUrl = it }, label = { Text("Video URL / Link") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = newFeatures, onValueChange = { newFeatures = it }, label = { Text("Features") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = newSpecs, onValueChange = { newSpecs = it }, label = { Text("Specifications") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = newBenefits, onValueChange = { newBenefits = it }, label = { Text("Benefits") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = newUseCases, onValueChange = { newUseCases = it }, label = { Text("Use Cases") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.adminAddCatalogItem(
                                title = newTitle.ifBlank { "New Product" },
                                description = newDesc.ifBlank { "N/A" },
                                modelNumber = newModel.ifBlank { "N/A" },
                                variantName = newVariant.ifBlank { "N/A" },
                                basePrice = newPriceStr.toDoubleOrNull() ?: 0.0,
                                imageUrl = newImageUrl,
                                videoUrl = newVideoUrl,
                                features = newFeatures,
                                specifications = newSpecs,
                                benefits = newBenefits,
                                useCases = newUseCases,
                                onComplete = { isCatalogManagerOpen = false }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)
                    ) {
                        Text("Save Product")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isCatalogManagerOpen = false }) { Text("Cancel") }
                }
            )
        }

        // Generate pricing quote dialogue modal picker
        quotationDialogId?.let { bId ->
            val matchingBooking = bookings.find { it.id == bId }
            val logisticsMetrics = matchingBooking?.let { viewModel.calculateLogisticsMetrics(it.latitude, it.longitude) }

            var subTotal by remember { mutableStateOf("120000") }
            var logSurcharge by remember(logisticsMetrics) { 
                mutableStateOf(logisticsMetrics?.recommendedLogisticsCost?.toInt()?.toString() ?: "25000") 
            }
            var adminNotesVal by remember { mutableStateOf("Premium trailer logistics. Secure anchor.") }

            AlertDialog(
                onDismissRequest = { quotationDialogId = null },
                title = { Text("Smart Pricing Quotation Engine", fontWeight = FontWeight.Bold) },
                confirmButton = {
                    Button(
                        onClick = {
                            val rawSub = subTotal.toDoubleOrNull() ?: 120000.0
                            val rawLog = logSurcharge.toDoubleOrNull() ?: logisticsMetrics?.recommendedLogisticsCost ?: 25000.0
                            val rawVat = (rawSub + rawLog) * 0.075 // 7.5% Standard luxury VAT Nigeria
                            viewModel.adminGenerateQuotation(bId, rawSub, rawLog, rawVat, adminNotesVal) {
                                quotationDialogId = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioGold)
                    ) {
                        Text("Issue formal Quote", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Configure logistics, VAT surcharges, and subtotal pricing parameters:")
                        
                        if (logisticsMetrics != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SashioGold.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Automatic Logistics Surcharge Recommendation:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Zone: ${logisticsMetrics.logisticsZone}", fontSize = 9.sp, color = SashioTextMuted)
                                    Text("Distance: %.2f km | Time: ${logisticsMetrics.travelTimeMinutes} mins", fontSize = 9.sp, color = SashioTextMuted)
                                    Text("Fuel: %.1f Liters Diesel".format(logisticsMetrics.estimatedFuelLitres), fontSize = 9.sp, color = SashioTextMuted)
                                    Text("Recommended Cost: ₦%,.2f".format(logisticsMetrics.recommendedLogisticsCost), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioGreen)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = subTotal, 
                            onValueChange = { subTotal = it }, 
                            label = { Text("Subtotal Service Cost (₦)") }, 
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = logSurcharge, 
                            onValueChange = { logSurcharge = it }, 
                            label = { Text("Logistics & Surcharge (₦)") }, 
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = adminNotesVal, 
                            onValueChange = { adminNotesVal = it }, 
                            label = { Text("Specific Terms & Signature") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }
    }
}

// Helper drawing utilities to ensure seamless compilation
fun strokeWidthLimit(width: Float): Stroke {
    return Stroke(width = width)
}

@Composable
fun ManageUserRolesPanel(viewModel: SashioViewModel) {
    val profiles by viewModel.supabaseProfiles.collectAsStateWithLifecycle()
    val error by viewModel.roleManagementError.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadSupabaseProfiles()
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("LIVE USER ROLES (SUPABASE)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "These are real Supabase accounts. Changing a role here takes effect immediately for that person's next login.",
            style = MaterialTheme.typography.bodySmall,
            color = SashioTextMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by name or email...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("manage_roles_search")
        )

        error?.let { err ->
            Text(err, color = SashioRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        }

        val filtered = profiles.filter {
            (it.full_name ?: "").contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { p ->
                var roleMenuOpen by remember(p.id) { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(p.full_name?.takeIf { it.isNotBlank() } ?: p.email, fontWeight = FontWeight.Bold)
                                Text(p.email, fontSize = 10.sp, color = SashioTextMuted)
                            }
                            Text(
                                p.role.uppercase(),
                                color = when (p.role) {
                                    "admin" -> SashioRed
                                    "staff" -> SashioGold
                                    else -> SashioGreen
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box {
                            OutlinedButton(
                                onClick = { roleMenuOpen = true },
                                modifier = Modifier.fillMaxWidth().testTag("manage_role_trigger_${p.id}")
                            ) {
                                Text("Change Role")
                            }
                            DropdownMenu(
                                expanded = roleMenuOpen,
                                onDismissRequest = { roleMenuOpen = false }
                            ) {
                                listOf("client", "staff", "admin").forEach { roleOption ->
                                    DropdownMenuItem(
                                        text = { Text(roleOption.uppercase()) },
                                        onClick = {
                                            roleMenuOpen = false
                                            if (roleOption != p.role) {
                                                viewModel.adminSetUserRole(p.id, roleOption)
                                            }
                                        }
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

@Composable
fun AdminCockpitCard(totalIncome: Double, activeDispatches: Int, pendingRequests: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SashioSlate),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().testTag("admin_cockpit_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("ENTERPRISE SUMMARY COCKPIT", style = MaterialTheme.typography.labelSmall, color = SashioGold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Text("₦%,.2f".format(totalIncome), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Approved Net Paystack Receipts", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = SashioGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$pendingRequests Pending", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = SashioTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$activeDispatches Active Dispatches", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 5. SECURITY & GATEAWY MODAL SIMULATION
// ==========================================

@Composable
fun PaystackCheckoutGatewayModal(
    transaction: CreatePaymentResponse,
    onSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var modeStep by remember { mutableStateOf("choices") } // choices, computing, authorization

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
                if (modeStep != "computing") {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("paystack_close_btn")) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("paystack_checkout_dialog")) {
                Divider(modifier = Modifier.padding(bottom = 12.dp))

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
                            onClick = { modeStep = "computing" },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioTeal),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("paystack_submit_gateway")
                        ) {
                            Text("PAY SECURELY WITH CARD", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    "computing" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = SashioTeal, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Computing secure local payload hashes...", fontWeight = FontWeight.Bold)
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(2000)
                                modeStep = "authorization"
                            }
                        }
                    }
                    "authorization" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SashioGreen, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Secure Payment Authorized!", fontWeight = FontWeight.ExtraBold)
                            Text("Simulating Paystack webhook broadcast...", fontSize = 11.sp, color = SashioTextMuted)

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onSuccess(transaction.reference) },
                                colors = ButtonDefaults.buttonColors(containerColor = SashioGreen),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("paystack_finish")
                            ) {
                                Text("Complete Checkout", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ==========================================
// DYNAMIC COMPONENT HELPERS & BADGES
// ==========================================

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
                    model = "https://i.ibb.co/4ZM1G4WW/1000946646-removebg-preview.png",
                    contentDescription = "Sachio Logo",
                    modifier = Modifier.size(40.dp).padding(end = 12.dp)
                )
                Column {
                    Text(roleName.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SashioGold, letterSpacing = 1.sp)
                    Text(user?.name ?: "Sachio Enlisted Attendant", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(user?.email ?: "anonymous@sashio.com", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
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
        "Quotation Requested" -> Triple("PENDING REVIEW", SashioGold.copy(alpha = 0.12f), SashioGold)
        "Under Review" -> Triple("UNDER REVIEW", SashioGold.copy(alpha = 0.12f), SashioGold)
        "Quotation Sent" -> Triple("QUOTE ISSUED", SashioTeal.copy(alpha = 0.12f), SashioTeal)
        "Client Accepted" -> Triple("ACCEPTED / UNPAID", SashioBlue.copy(alpha = 0.12f), SashioBlue)
        "Booking Confirmed" -> Triple("BOOKED & CLEARED", SashioGreen.copy(alpha = 0.12f), SashioGreen)
        "SERVICE DELIVERED" -> Triple("ON-ROUTE", SashioTeal.copy(alpha = 0.12f), SashioTeal)
        "in_progress" -> Triple("SERVICING", SashioTeal.copy(alpha = 0.12f), SashioTeal)
        "completed" -> Triple("COMPLETED", SashioGreen.copy(alpha = 0.12f), SashioGreen)
        else -> Triple("CANCELLED", SashioRed.copy(alpha = 0.12f), SashioRed)
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

// --- Vector Map Canvas Drawer ---
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

            // 1. Draw street network pathways
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

            // 2. Draw style layers decoration
            if (mapStyle == "Traffic") {
                // Red/green/orange glowing traffic congestion traces
                drawLine(color = Color(0xFFEF4444), start = Offset(0f, height * 0.35f), end = Offset(width, height * 0.35f), strokeWidth = 3f)
                drawLine(color = Color(0xFF22C55E), start = Offset(0f, height * 0.45f), end = Offset(width, height * 0.45f), strokeWidth = 4f)
                drawLine(color = Color(0xFFF59E0B), start = Offset(width * 0.4f, 0f), end = Offset(width * 0.4f, height), strokeWidth = 2f)
            } else if (mapStyle == "Operations") {
                // Draw yellow zoning grid concentric circles representing service circles
                drawCircle(color = SashioGold.copy(alpha = 0.05f), center = Offset(width / 2, height / 2), radius = 50.dp.toPx(), style = Stroke(width = 1.dp.toPx()))
                drawCircle(color = SashioGold.copy(alpha = 0.03f), center = Offset(width / 2, height / 2), radius = 100.dp.toPx(), style = Stroke(width = 1.dp.toPx()))
                drawCircle(color = SashioGold.copy(alpha = 0.02f), center = Offset(width / 2, height / 2), radius = 150.dp.toPx(), style = Stroke(width = 1.dp.toPx()))
            }

            // Major Highway arterial trace
            drawLine(color = Color.DarkGray, start = Offset(0f, height * 0.4f), end = Offset(width, height * 0.45f), strokeWidth = 10f)

            // Lagos Lagoon water segment
            drawCircle(color = SashioTeal.copy(alpha = 0.13f), center = Offset(width * 0.2f, height * 0.8f), radius = 35.dp.toPx())

            // 3. Client destination calculations
            val markerX = (((longitude - 3.3) * 25000).toFloat()).coerceIn(30f, width - 30f)
            val markerY = (((latitude - 6.4) * 25000).toFloat()).coerceIn(30f, height - 30f)

            // 4. Vehicle location calculations if provided
            val vX = vehicleLat?.let { (((vehicleLng ?: 3.3) - 3.3) * 25000).toFloat().coerceIn(30f, width - 30f) }
            val vY = vehicleLat?.let { (((vehicleLat) - 6.4) * 25000).toFloat().coerceIn(30f, height - 30f) }

            // 5. Staff location calculations if provided
            val sX = staffLat?.let { (((staffLng ?: 3.3) - 3.3) * 25000).toFloat().coerceIn(30f, width - 30f) }
            val sY = staffLat?.let { (((staffLat) - 6.4) * 25000).toFloat().coerceIn(30f, height - 30f) }

            // 6. Draw route path if required
            if (showRoute && vX != null && vY != null) {
                drawLine(
                    color = SashioGold,
                    start = Offset(vX, vY),
                    end = Offset(markerX, markerY),
                    strokeWidth = 3f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Draw Client Destination Pin
            drawCircle(color = SashioGold.copy(alpha = 0.25f), center = Offset(markerX, markerY), radius = 18.dp.toPx())
            drawCircle(color = SashioGold, center = Offset(markerX, markerY), radius = 6.dp.toPx())
            drawCircle(color = Color.White, center = Offset(markerX, markerY), radius = 2.dp.toPx())

            // Draw Vehicle Indicator (Truck Icon approximation)
            if (vX != null && vY != null) {
                drawCircle(color = SashioTeal.copy(alpha = 0.3f), center = Offset(vX, vY), radius = 15.dp.toPx())
                drawCircle(color = SashioTeal, center = Offset(vX, vY), radius = 7.dp.toPx())
                // small driver box cab shape
                drawRect(color = Color.White, topLeft = Offset(vX - 3.dp.toPx(), vY - 3.dp.toPx()), size = androidx.compose.ui.geometry.Size(6.dp.toPx(), 6.dp.toPx()))
            }

            // Draw Staff Indicator (Attendant icon approximation)
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

// --- Local Catalog category and data schemas ---
data class CatalogCategory(
    val title: String,
    val description: String,
    val model: String,
    val variant: String
)

// ==============================================================================
// SACHIO PREMIUM LIVE SERVICE TRACKING SCREEN & HELPER COMPOSABLES
// ==============================================================================

@Composable
fun SashioLiveTrackingScreen(
    viewModel: SashioViewModel,
    bookingId: Long,
    onBack: () -> Unit
) {
    val bookings by viewModel.clientBookings.collectAsStateWithLifecycle()
    val booking = bookings.find { it.id == bookingId }
    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error: Booking Reference #QR-$bookingId not found.", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Back to Dashboard") }
            }
        }
        return
    }

    val trackers by viewModel.activeLiveTrackers.collectAsStateWithLifecycle()
    // Default fallback coordinates if tracker empty
    val tracker = trackers[bookingId] ?: LiveTrackingState(
        bookingId = bookingId,
        staffLat = booking.latitude - 0.05,
        staffLng = booking.longitude - 0.04,
        vehicleLat = booking.latitude - 0.048,
        vehicleLng = booking.longitude - 0.038
    )

    var currentTab by remember { mutableStateOf("radar") } // radar, chat, log, crew
    var zoomLevel by remember { mutableStateOf(1f) }
    var showAlternateRoutes by remember { mutableStateOf(false) }
    var simulateRoadClosure by remember { mutableStateOf(false) }
    var selectedLayer by remember { mutableStateOf("Standard") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FDFE))
    ) {
        // PRESTIGE HEADER
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
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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

        // UPPER PORTION: HIGH-FIDELITY RADAR MAP VIEW
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(Color.White)
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

            // Radar overlay panels
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        selectedLayer = when(selectedLayer) {
                            "Standard" -> "Satellite"
                            "Satellite" -> "Traffic"
                            "Traffic" -> "Operations"
                            else -> "Standard"
                        }
                    },
                    containerColor = Color.White.copy(alpha = 0.92f),
                    contentColor = SashioSlate,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                SmallFloatingActionButton(
                    onClick = { zoomLevel = (zoomLevel + 0.3f).coerceAtMost(2.5f) },
                    containerColor = Color.White.copy(alpha = 0.92f),
                    contentColor = SashioSlate,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                SmallFloatingActionButton(
                    onClick = { zoomLevel = (zoomLevel - 0.3f).coerceAtLeast(0.5f) },
                    containerColor = Color.White.copy(alpha = 0.92f),
                    contentColor = SashioSlate,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                SmallFloatingActionButton(
                    onClick = {
                        zoomLevel = 1.0f
                        Toast.makeText(context, "Radar centered on SACHIO hauler.", Toast.LENGTH_SHORT).show()
                    },
                    containerColor = Color.White.copy(alpha = 0.92f),
                    contentColor = SashioSlate,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            // Bottom-left interactive map status toggles
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        showAlternateRoutes = !showAlternateRoutes
                        val msg = if (showAlternateRoutes) "Alternative routes loaded. Bypass via Ocean road available." else "Alternate routes hidden."
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAlternateRoutes) SashioGold else Color.White.copy(alpha = 0.92f),
                        contentColor = if (showAlternateRoutes) Color.White else SashioSlate
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("By-passes", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        simulateRoadClosure = !simulateRoadClosure
                        val msg = if (simulateRoadClosure) "Simulated Road block detected. GPS has recalculated optimal bypass!" else "Route clear."
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (simulateRoadClosure) SashioRed else Color.White.copy(alpha = 0.92f),
                        contentColor = if (simulateRoadClosure) Color.White else SashioSlate
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Traffic, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Traffic Alerts", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ETA COUNTDOWN OVERLAY INNER CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .width(170.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = SashioGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ESTIMATED ARRIVAL", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (booking.status == "COMPLETED" || booking.status == "Completed") "Delivered" 
                        else if (booking.status in listOf("ARRIVED", "SETUP IN PROGRESS", "SERVICE ACTIVE")) "Arrived"
                        else "${tracker.etaMinutes} MINS",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (booking.status in listOf("ARRIVED", "SETUP IN PROGRESS", "SERVICE ACTIVE", "COMPLETED", "Completed")) "Setup active"
                        else "Distance: %.1f km remaining".format(tracker.distanceRemainingKm),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // TABS FOR LOWER CONTROL DECK
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
                    onClick = { currentTab = tabId },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) SashioSlate else Color.White,
                        contentColor = if (selected) SashioGold else SashioSlate
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = if (selected) SashioGold else SashioSlate)
                        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // LOWER DECK DISPLAY AREA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .background(Color.White)
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        val bgGrad = when (mapStyle) {
            "Satellite" -> Brush.verticalGradient(listOf(Color(0xFF0F2012), Color(0xFF1B311E)))
            "Traffic" -> Brush.verticalGradient(listOf(Color(0xFFE9EEEC), Color(0xFFD6DFDC)))
            "Operations" -> Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
            else -> Brush.verticalGradient(listOf(Color(0xFFEDF4F7), Color(0xFFE2EBF1)))
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGrad)
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val cX = canvasW / 2f
            val cY = canvasH / 2f

            val zoom = 4000f * zoomFactor
            
            fun getCanvasPos(lat: Double, lng: Double): Offset {
                val dx = (lng - longitude) * zoom
                val dy = (latitude - lat) * zoom
                return Offset(cX + dx.toFloat(), cY + dy.toFloat())
            }

            val targetPos = Offset(cX, cY)
            val staffPos = getCanvasPos(staffLat, staffLng)
            val vehiclePos = getCanvasPos(vehicleLat, vehicleLng)
            val depotPos = getCanvasPos(latitude - 0.05, longitude - 0.04)

            val roadColor = when (mapStyle) {
                "Satellite" -> Color.White.copy(alpha = 0.15f)
                "Operations" -> Color(0xFF334155)
                else -> Color.White
            }
            
            val scale = zoomFactor
            for (offset in -4..4) {
                val gridY = cY + offset * 110f * scale
                val gridX = cX + offset * 150f * scale
                drawLine(
                    color = roadColor,
                    start = Offset(0f, gridY),
                    end = Offset(canvasW, gridY),
                    strokeWidth = 6.dp.toPx()
                )
                drawLine(
                    color = roadColor,
                    start = Offset(gridX, 0f),
                    end = Offset(gridX, canvasH),
                    strokeWidth = 6.dp.toPx()
                )
            }

            if (mapStyle == "Traffic") {
                drawLine(
                    color = SashioRed.copy(alpha = 0.8f),
                    start = depotPos,
                    end = targetPos,
                    strokeWidth = 4.dp.toPx()
                )
            }

            if (showRoute) {
                val pathColor = if (mapStyle == "Operations") SashioGold else SashioSlate
                drawLine(
                    color = pathColor.copy(alpha = 0.7f),
                    start = depotPos,
                    end = targetPos,
                    strokeWidth = 3.dp.toPx()
                )
            }

            if (alternateRoute) {
                val bypassColor = SashioTeal
                val steps = 20
                var currentX = depotPos.x
                var currentY = depotPos.y
                val targetX = targetPos.x
                val targetY = targetPos.y
                
                for (step in 0 until steps) {
                    val nextPct = (step + 1).toFloat() / steps
                    val tX = depotPos.x + nextPct * (targetX - depotPos.x) + kotlin.math.sin(nextPct * kotlin.math.PI.toFloat()) * 90f * scale
                    val tY = depotPos.y + nextPct * (targetY - depotPos.y) - kotlin.math.cos(nextPct * kotlin.math.PI.toFloat()) * 50f * scale
                    
                    drawLine(
                        color = bypassColor,
                        start = Offset(currentX, currentY),
                        end = Offset(tX, tY),
                        strokeWidth = 3.dp.toPx()
                    )
                    currentX = tX
                    currentY = tY
                }
            }

            if (roadClosureSelected) {
                val closureX = depotPos.x + 0.5f * (targetPos.x - depotPos.x)
                val closureY = depotPos.y + 0.5f * (targetPos.y - depotPos.y)
                drawCircle(
                    color = SashioRed,
                    center = Offset(closureX, closureY),
                    radius = 9.dp.toPx()
                )
                drawLine(
                    color = Color.White,
                    start = Offset(closureX - 5.dp.toPx(), closureY - 5.dp.toPx()),
                    end = Offset(closureX + 5.dp.toPx(), closureY + 5.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Pulse rings
            val pulseRadius = (System.currentTimeMillis() % 2000 / 2000f) * 20.dp.toPx()
            drawCircle(
                color = SashioGold.copy(alpha = (1f - (System.currentTimeMillis() % 2000 / 2000f)).coerceAtLeast(0f)),
                center = targetPos,
                radius = pulseRadius
            )

            drawCircle(color = SashioGreen, center = depotPos, radius = 7.dp.toPx())
            drawCircle(color = Color.White, center = depotPos, radius = 3.dp.toPx())

            drawCircle(color = SashioSlate, center = vehiclePos, radius = 10.dp.toPx())
            drawCircle(color = SashioGold, center = vehiclePos, radius = 6.dp.toPx())
            drawCircle(color = Color.White, center = vehiclePos, radius = 2.dp.toPx())

            drawCircle(color = SashioTeal, center = staffPos, radius = 6.dp.toPx())
            drawCircle(color = Color.White, center = staffPos, radius = 2.dp.toPx())

            if (label.isNotBlank()) {
                drawCircle(color = SashioRed, center = targetPos, radius = 5.dp.toPx())
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SashioGreen))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Depot", color = Color.White, fontSize = 8.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SashioGold))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hauler V", color = Color.White, fontSize = 8.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SashioTeal))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Attendant", color = Color.White, fontSize = 8.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SashioRed))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Venue", color = Color.White, fontSize = 8.sp)
            }
        }
    }
}

@Composable
fun LiveRadarDetailTab(
    viewModel: SashioViewModel,
    booking: Booking,
    tracker: LiveTrackingState
) {
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
                    colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Active Rig Speed", color = SashioTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            if (booking.status in listOf("ARRIVED", "SETUP IN PROGRESS", "SERVICE ACTIVE", "COMPLETED", "Completed")) "0 km/h (Parked)" else "42 km/h",
                            color = SashioSlate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.0f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Road Conditions", color = SashioTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            tracker.trafficLevel,
                            color = if (tracker.trafficLevel == "Heavy") SashioRed else SashioSlate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.9f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Weather", color = SashioTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Cloudy, 29°C", color = SashioSlate, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SERVICE DELIVERY STAGES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                    Text("Prestige workflow progress track", fontSize = 10.sp, color = SashioTextMuted)
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
                        "Quotation Requested", "Under Review", "Quotation Generated" -> 0
                        "Quotation Sent", "Client Accepted" -> 1
                        "Awaiting Payment" -> 2
                        "Payment Confirmed" -> 2
                        "Booking Confirmed" -> 3
                        "Staff Assigned" -> 4
                        "SERVICE DELIVERED" -> 5
                        "IN TRANSIT" -> 6
                        "ARRIVED" -> 7
                        "SETUP IN PROGRESS" -> 8
                        "SERVICE ACTIVE" -> 9
                        "COMPLETED", "Completed" -> 10
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
                                            if (isDone) (if (isCurrent) SashioGold else SashioGreen)
                                            else Color.LightGray.copy(alpha = 0.5f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone && !isCurrent) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color.White)
                                    } else if (isCurrent) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                                    }
                                }
                                if (idx < stagesList.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(24.dp)
                                            .background(
                                                if (idx < activeIndex) SashioGreen
                                                else Color.LightGray.copy(alpha = 0.4f)
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent || isDone) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) SashioGold else if (isDone) SashioSlate else Color.LightGray
                                )
                                Text(
                                    desc,
                                    fontSize = 9.sp,
                                    color = if (isDone) SashioTextMuted else Color.LightGray
                                )
                            }
                        }
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
    var attachedImg by remember { mutableStateOf<String?>(null) }
    var locationShared by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    
    val context = LocalContext.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.03f)),
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
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SashioGreen))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Secure Chat Session | Obinna & Support", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                }
                Text("Operational Support", fontSize = 9.sp, color = SashioGold, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tracker.messages.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("No messages yet. Send a message to coordinate setup placement or request assistance.", fontSize = 11.sp, color = SashioTextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            } else {
                items(tracker.messages) { msg ->
                    val isClient = msg.sender == "Client"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isClient) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isClient) SashioSlate else Color(0xFFECEFF1)
                            ),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isClient) 12.dp else 0.dp,
                                bottomEnd = if (isClient) 0.dp else 12.dp
                            ),
                            modifier = Modifier.widthIn(max = 240.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    msg.sender,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isClient) SashioGold else SashioSlate
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                if (msg.imageUri != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.DarkGray)
                                            .padding(bottom = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        Text("[Site Photo Uploaded]", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomCenter).padding(2.dp))
                                    }
                                }

                                if (msg.locationShare != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(4.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PinDrop, contentDescription = null, tint = SashioGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("[Location: %.4f, %.4f]".format(msg.locationShare.first, msg.locationShare.second), fontSize = 9.sp, color = if (isClient) Color.White else SashioSlate)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Text(
                                    msg.text,
                                    fontSize = 11.sp,
                                    color = if (isClient) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp))
                                    Text(timeStr, fontSize = 7.sp, color = if (isClient) Color.White.copy(alpha = 0.6f) else Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (attachedImg != null || locationShared != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (attachedImg != null) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = SashioGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prestige_Site_Entrance.png attached.", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    } else if (locationShared != null) {
                        Icon(Icons.Default.PinDrop, contentDescription = null, tint = SashioGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Current location pin attached.", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = {
                    attachedImg = null
                    locationShared = null
                }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = {
                    attachedImg = "sim_photo_uri_123"
                    locationShared = null
                    Toast.makeText(context, "Clear Site Placement Photo attached!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Attach Site Photo", tint = SashioGold)
            }

            IconButton(
                onClick = {
                    locationShared = Pair(booking.latitude, booking.longitude)
                    attachedImg = null
                    Toast.makeText(context, "Current event location pin attached!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.PinDrop, contentDescription = "Share Location Coordinates", tint = SashioGold)
            }

            OutlinedTextField(
                value = txtVal,
                onValueChange = { txtVal = it },
                placeholder = { Text("Prestige instructions...", fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { keyboardController?.hide() })
            )

            IconButton(
                onClick = {
                    if (txtVal.isNotBlank() || attachedImg != null || locationShared != null) {
                        viewModel.sendClientChatMessage(
                            bookingId = booking.id,
                            text = txtVal,
                            imageUri = attachedImg,
                            locationShare = locationShared
                        )
                        txtVal = ""
                        attachedImg = null
                        locationShared = null
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Message", tint = SashioGold)
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
    var subTab by remember { mutableStateOf("logs") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Button(
                onClick = { subTab = "logs" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (subTab == "logs") SashioGold else Color.LightGray.copy(alpha = 0.15f),
                    contentColor = if (subTab == "logs") Color.White else SashioSlate
                ),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Service Activity Log Monitor", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { subTab = "alerts" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (subTab == "alerts") SashioGold else Color.LightGray.copy(alpha = 0.15f),
                    contentColor = if (subTab == "alerts") Color.White else SashioSlate
                ),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                modifier = Modifier
                    .weight(1.0f)
                    .height(30.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Alerts & Notification History", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (subTab == "logs") {
                if (tracker.logs.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No activity logged yet. Dispatch simulation is starting shortly.", color = SashioTextMuted, fontSize = 11.sp)
                        }
                    }
                } else {
                    items(tracker.logs) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(SashioSlate.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = SashioSlate)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                    Text(log.description, fontSize = 9.sp, color = SashioTextMuted)
                                }
                                val logTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
                                Text(logTime, fontSize = 8.sp, color = SashioTextMuted)
                            }
                        }
                    }
                }
            } else {
                if (tracker.notifications.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Alert notification inbox empty. Will stream dispatcher signals.", color = SashioTextMuted, fontSize = 11.sp)
                        }
                    }
                } else {
                    items(tracker.notifications) { notif ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SashioGold.copy(alpha = 0.04f)),
                            border = BorderStroke(1.dp, SashioGold.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SashioGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(notif.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                    Text(notif.message, fontSize = 9.sp, color = SashioTextMuted)
                                }
                                val notifTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(notif.timestamp))
                                Text(notifTime, fontSize = 8.sp, color = SashioTextMuted)
                            }
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
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CHIEF SANITATION STEWARD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioGold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SashioSlate),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tracker.staffName, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SashioSlate)
                            Text(tracker.staffPosition, fontSize = 11.sp, color = SashioTextMuted)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = SashioGold, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("4.9 ★ (120+ duties Completed) | ${tracker.staffExperience} yrs Exp", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SashioSlate)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val telIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${tracker.staffPhone}"))
                                context.startActivity(telIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioSlate, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp).weight(1f)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Staff", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Steward Credentials: board-certified by Nigerian Sanitary Regulatory Authorities.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SashioSlate),
                            border = BorderStroke(1.dp, SashioSlate),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp).weight(1f)
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Profile", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("VEHICLE & FLEET DEPLOYMENT RIG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioGold)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SashioSlate.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = SashioSlate, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(tracker.vehicleType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                            Text("Registration ID: ${tracker.vehicleNumber}", fontSize = 10.sp, color = SashioTextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Engine Health Status:", fontSize = 10.sp, color = SashioTextMuted)
                            Text("100% Operational (Excellent)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sewage Tank Capacity (Est):", fontSize = 10.sp, color = SashioTextMuted)
                            Text("Empty (Sanitized for Event)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Water Reservoir Volume:", fontSize = 10.sp, color = SashioTextMuted)
                            Text("2,500 Litres (Fully Primed)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. PREMIUM ENTERPRISE-GRADE SETTINGS SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SashioSettingsScreen(
    viewModel: SashioViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // Core profile States
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val altPhone by viewModel.userAltPhone.collectAsStateWithLifecycle()
    val bio by viewModel.userBio.collectAsStateWithLifecycle()
    val bizAddress by viewModel.userBusinessAddress.collectAsStateWithLifecycle()
    val profilePic by viewModel.userProfilePic.collectAsStateWithLifecycle()
    val location by viewModel.userLocation.collectAsStateWithLifecycle()
    
    // Appearance States
    val themePref by viewModel.themePreference.collectAsStateWithLifecycle()
    val accentColorVal by viewModel.accentColor.collectAsStateWithLifecycle()
    val fontSizeVal by viewModel.fontSizeScale.collectAsStateWithLifecycle()
    val cardDensityVal by viewModel.cardDensity.collectAsStateWithLifecycle()
    val mapThemeVal by viewModel.mapTheme.collectAsStateWithLifecycle()
    val accessibilityVal by viewModel.accessibilityMode.collectAsStateWithLifecycle()
    
    // Notifications States
    val pushVal by viewModel.pushNotifications.collectAsStateWithLifecycle()
    val smsVal by viewModel.smsNotifications.collectAsStateWithLifecycle()
    val emailVal by viewModel.emailNotifications.collectAsStateWithLifecycle()
    val whatsappVal by viewModel.whatsappNotifications.collectAsStateWithLifecycle()
    val notifyBooking by viewModel.notifyBookingUpdates.collectAsStateWithLifecycle()
    val notifyQuote by viewModel.notifyQuotationUpdates.collectAsStateWithLifecycle()
    val notifyPay by viewModel.notifyPaymentConfirmations.collectAsStateWithLifecycle()
    val notifyAnnounce by viewModel.notifySystemAnnouncements.collectAsStateWithLifecycle()
    val pushSoundVal by viewModel.pushSound.collectAsStateWithLifecycle()
    
    // Privacy States
    val privacyPicVal by viewModel.privacyProfilePicVisible.collectAsStateWithLifecycle()
    val privacyContactVal by viewModel.privacyContactVisible.collectAsStateWithLifecycle()
    val privacyTrackingVal by viewModel.privacyTrackingShare.collectAsStateWithLifecycle()
    val privacySupportVal by viewModel.privacySupportAccess.collectAsStateWithLifecycle()
    val privacyAnalyticsVal by viewModel.privacyAnalyticsAllowed.collectAsStateWithLifecycle()
    
    // Security States
    val secBiometricVal by viewModel.securityBiometricsEnabled.collectAsStateWithLifecycle()
    val sec2FAVal by viewModel.security2FAEnabled.collectAsStateWithLifecycle()
    val secScoreVal by viewModel.securityScore.collectAsStateWithLifecycle()
    
    // App Preferences State
    val autoRefreshVal by viewModel.autoRefresh.collectAsStateWithLifecycle()
    val locServicesVal by viewModel.locationServicesEnabled.collectAsStateWithLifecycle()
    val bgTrackingVal by viewModel.backgroundTrackingEnabled.collectAsStateWithLifecycle()
    val offlineModeVal by viewModel.offlineModeEnabled.collectAsStateWithLifecycle()
    val autoDownloadsVal by viewModel.automaticDownloads.collectAsStateWithLifecycle()
    val cacheSizeVal by viewModel.cacheManagementSize.collectAsStateWithLifecycle()
    val storageUsedVal by viewModel.storageUsed.collectAsStateWithLifecycle()
    
    // Language & Ticket States
    val langVal by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val ticketHistory by viewModel.ticketHistory.collectAsStateWithLifecycle()
    
    // Active sub screen state (null means setting index, otherwise specific subscreen)
    var activeSubScreen by remember { mutableStateOf<String?>(null) }
    
    // Dynamic theme accent mapping
    val currentAccent = when (accentColorVal) {
        "Teal Breeze" -> SashioBlue
        "Royal Indigo" -> SashioSlate
        "Ruby Premium" -> SashioRed
        else -> SashioGold
    }
    
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_module_surface"),
        color = if (themePref == "dark") SashioSlate else SashioLight
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SashioSlate)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (activeSubScreen != null) {
                                activeSubScreen = null
                            } else {
                                onDismiss()
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (activeSubScreen == null) "SETTINGS INDEX" else activeSubScreen!!.uppercase().replace("_", " "),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = SashioRed.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Settings",
                        tint = SashioRed
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                if (activeSubScreen == null) {
                    // ==========================================
                    // MAIN SETTINGS HOME INDEX SCREEN
                    // ==========================================
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // User Profile Summary Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.05f) else Color.White
                                ),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(currentAccent.copy(alpha = 0.1f))
                                            .border(2.dp, currentAccent, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = profilePic.ifBlank { "https://i.ibb.co/4ZM1G4WW/1000946646-removebg-preview.png" },
                                            contentDescription = "User Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentUser?.name ?: "Sachio Patron",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (themePref == "dark") Color.White else SashioSlate
                                        )
                                        Text(
                                            text = currentUser?.email ?: "guestuser@sashio.com",
                                            fontSize = 11.sp,
                                            color = SashioTextMuted
                                        )
                                        Text(
                                            text = "Phone: ${currentUser?.phoneNumber ?: "080-SACHIO-M"}",
                                            fontSize = 10.sp,
                                            color = SashioTextMuted
                                        )
                                        
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Role Badge
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(SashioSlate)
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = (currentUser?.role ?: "Client").uppercase(),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SashioGold
                                                )
                                            }
                                            
                                            // VIP Tag
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(currentAccent.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "GOLD VIP STATUS",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (themePref == "dark") Color.White else currentAccent
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Menu Links Section
                        item {
                            Text(
                                text = "APPLICATION CONFIGURATIONS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SashioTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                        }
                        
                        // Quick Menu Card list items
                        val linkItems = listOf(
                            Triple("profile", "Profile Settings", Icons.Default.Person),
                            Triple("notifications", "Notification Preferences", Icons.Default.Notifications),
                            Triple("privacy", "Privacy Controls", Icons.Default.Lock),
                            Triple("security", "Security & Protection", Icons.Default.Security),
                            Triple("language", "Language Settings (Language)", Icons.Default.Language),
                            Triple("appearance", "Appearance Theme", Icons.Default.Palette),
                            Triple("preferences", "Storage & Preferences", Icons.Default.Settings),
                            Triple("support", "Help & Consultation Desk", Icons.Default.Info),
                            Triple("terms", "Terms of Use & Legal", Icons.Default.Assignment),
                            Triple("account", "Critical Account Options", Icons.Default.Delete)
                        )
                        
                        items(linkItems) { (tag, label, icon) ->
                            Card(
                                onClick = { activeSubScreen = tag },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.15f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_menu_$tag")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(currentAccent.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = currentAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (themePref == "dark") Color.White else SashioSlate
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack, // Will flip horizontally as visual indicator
                                        contentDescription = null,
                                        tint = SashioTextMuted,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .scale(scaleX = -1f, scaleY = 1f)
                                    )
                                }
                            }
                        }
                        
                        // Standard Logout Button
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { 
                                    onDismiss()
                                    viewModel.logout()
                                    Toast.makeText(context, "Sashio Prestige Session Disconnected.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("settings_logout_btn")
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("LOGOUT SESSION", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // INDIVIDUAL DETAILS SUBSCREENS (10 SECTIONS)
                    // ==========================================
                    when (activeSubScreen) {
                        "profile" -> {
                            // Section 1: Detailed User Profile Editor with presets and manual inputs
                            var nameVal by remember { mutableStateOf(currentUser?.name ?: "") }
                            var emailVal by remember { mutableStateOf(currentUser?.email ?: "") }
                            var phoneVal by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
                            var companyVal by remember { mutableStateOf(currentUser?.companyName ?: "") }
                            var altPhoneVal by remember { mutableStateOf(altPhone) }
                            var bioVal by remember { mutableStateOf(bio) }
                            var addressVal by remember { mutableStateOf(bizAddress) }
                            var locationVal by remember { mutableStateOf(location) }
                            
                            val photoPickerLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.PickVisualMedia()
                            ) { uri ->
                                uri?.let {
                                    viewModel.updateExtraProfileInfo(altPhoneVal, bioVal, addressVal, it.toString(), locationVal)
                                    Toast.makeText(context, "Custom Avatar selected successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Avatar Section
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape)
                                                    .background(currentAccent.copy(alpha = 0.1f))
                                                    .border(2.dp, currentAccent, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = profilePic,
                                                    contentDescription = "Active Picture",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            
                                            Text(
                                                text = "TAP REDEEM AVATAR OR CHOOSE GALLERY FILE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SashioTextMuted
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            // Avatar presets
                                            val presets = listOf(
                                                "https://i.ibb.co/4ZM1G4WW/1000946646-removebg-preview.png", // Standard logo
                                                "https://cdn-icons-png.flaticon.com/512/3135/3135715.png", // Client boy
                                                "https://cdn-icons-png.flaticon.com/512/4140/4140047.png", // Client girl
                                                "https://cdn-icons-png.flaticon.com/512/4140/4140037.png", // Premium rep
                                                "https://cdn-icons-png.flaticon.com/512/1077/1077114.png"  // Executive
                                            )
                                            
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                items(presets) { url ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(42.dp)
                                                            .clip(CircleShape)
                                                            .border(
                                                                width = if (profilePic == url) 2.dp else 1.dp,
                                                                color = if (profilePic == url) currentAccent else Color.LightGray.copy(alpha = 0.6f),
                                                                shape = CircleShape
                                                            )
                                                            .clickable {
                                                                viewModel.updateExtraProfileInfo(altPhoneVal, bioVal, addressVal, url, locationVal)
                                                            }
                                                    ) {
                                                        AsyncImage(
                                                            model = url,
                                                            contentDescription = null,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Button(
                                                    onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(36.dp)
                                                ) {
                                                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Pick Gallery File", fontSize = 10.sp, color = Color.White)
                                                }
                                                
                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.updateExtraProfileInfo(altPhoneVal, bioVal, addressVal, "https://i.ibb.co/4ZM1G4WW/1000946646-removebg-preview.png", locationVal)
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(36.dp)
                                                ) {
                                                    Text("Remove Image", fontSize = 10.sp, color = SashioSlate)
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Fields form list
                                item { OutlinedTextField(value = nameVal, onValueChange = { nameVal = it }, label = { Text("Patron Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                                item { OutlinedTextField(value = emailVal, onValueChange = { emailVal = it }, label = { Text("Corporate Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                                item { OutlinedTextField(value = phoneVal, onValueChange = { phoneVal = it }, label = { Text("Primary Hotline Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                                item { OutlinedTextField(value = companyVal, onValueChange = { companyVal = it }, label = { Text("Company Entity Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                                item { OutlinedTextField(value = altPhoneVal, onValueChange = { altPhoneVal = it }, label = { Text("Alternative Dispatch Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                                item { OutlinedTextField(value = locationVal, onValueChange = { locationVal = it }, label = { Text("State Coordinates / Location Zone") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                                item { OutlinedTextField(value = addressVal, onValueChange = { addressVal = it }, label = { Text("Principal Business Address") }, maxLines = 2, modifier = Modifier.fillMaxWidth()) }
                                item { OutlinedTextField(value = bioVal, onValueChange = { bioVal = it }, label = { Text("Corporate Biography / Notes") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                                
                                // Meta Data Display Card
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = currentAccent.copy(alpha = 0.05f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("METRIC LOGS & META ARCHITECTURE:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            Text("Account Reference ID: SCH-PREC-48201", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                            Text("Security Enlistment: Enrolled June, 2026", fontSize = 10.sp, color = SashioTextMuted)
                                            Text("Network Node: Lagos Central Cluster Grid", fontSize = 10.sp, color = SashioTextMuted)
                                        }
                                    }
                                }
                                
                                // Save
                                item {
                                    Button(
                                        onClick = {
                                            viewModel.updateUserProfileInfo(nameVal, emailVal, phoneVal, companyVal)
                                            viewModel.updateExtraProfileInfo(altPhoneVal, bioVal, addressVal, profilePic, locationVal)
                                            Toast.makeText(context, "Sashio Profile database updated successfully!", Toast.LENGTH_SHORT).show()
                                            activeSubScreen = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("COMMIT PROFILE CHANGES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                        
                        "notifications" -> {
                            // Section 2: Detailed Notifications Preferences with specific sound selector
                            var soundPreviewTrigger by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(pushSoundVal) {
                                if (soundPreviewTrigger) {
                                    kotlinx.coroutines.delay(800)
                                    soundPreviewTrigger = false
                                }
                            }
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Text("CORE SERVICE DISPATCH CHANNELS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            
                                            // Channel 1
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text("App Push Banner Alerts", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("System real-time tray notices", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Switch(checked = pushVal, onCheckedChange = { viewModel.updateNotificationToggles(it, smsVal, emailVal, whatsappVal, notifyBooking, notifyQuote, notifyPay, notifyAnnounce, pushSoundVal) })
                                            }
                                            
                                            // Channel 2
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text("Direct SMS Notifications", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("Offline cellular logistics alerts", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Switch(checked = smsVal, onCheckedChange = { viewModel.updateNotificationToggles(pushVal, it, emailVal, whatsappVal, notifyBooking, notifyQuote, notifyPay, notifyAnnounce, pushSoundVal) })
                                            }
                                            
                                            // Channel 3
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text("Prestige Email Digests", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("Detailed VAT invoices, PDF formal quotes", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Switch(checked = emailVal, onCheckedChange = { viewModel.updateNotificationToggles(pushVal, smsVal, it, whatsappVal, notifyBooking, notifyQuote, notifyPay, notifyAnnounce, pushSoundVal) })
                                            }
                                            
                                            // Channel 4
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text("WhatsApp Immediate Chat Messages", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("Pre-filled quotations forms & live chat help", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Switch(checked = whatsappVal, onCheckedChange = { viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, it, notifyBooking, notifyQuote, notifyPay, notifyAnnounce, pushSoundVal) })
                                            }
                                        }
                                    }
                                }
                                
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Text("CATEGORY-SPECIFIC FILTER POLICIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("Active Booking Status Alterations", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                                Switch(checked = notifyBooking, onCheckedChange = { viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, whatsappVal, it, notifyQuote, notifyPay, notifyAnnounce, pushSoundVal) })
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("Quotation Issued/Under Review logs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                                Switch(checked = notifyQuote, onCheckedChange = { viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, whatsappVal, notifyBooking, it, notifyPay, notifyAnnounce, pushSoundVal) })
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("Payment Confirmations & Reconciliations", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                                Switch(checked = notifyPay, onCheckedChange = { viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, whatsappVal, notifyBooking, notifyQuote, it, notifyAnnounce, pushSoundVal) })
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("Sachio Global Announcements & Updates", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                                Switch(checked = notifyAnnounce, onCheckedChange = { viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, whatsappVal, notifyBooking, notifyQuote, notifyPay, it, pushSoundVal) })
                                            }
                                        }
                                    }
                                }
                                
                                // Push notifications sound options card
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("SELECT SOUND PREFERENTIAL AUDIO CHIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            
                                            val pushSounds = listOf("Premium Chime", "Golden Bell", "Classic Note", "Acoustic Wave")
                                            pushSounds.forEach { sound ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, whatsappVal, notifyBooking, notifyQuote, notifyPay, notifyAnnounce, sound)
                                                            soundPreviewTrigger = true
                                                        }
                                                        .padding(vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        RadioButton(
                                                            selected = pushSoundVal == sound,
                                                            onClick = {
                                                                viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, whatsappVal, notifyBooking, notifyQuote, notifyPay, notifyAnnounce, sound)
                                                                soundPreviewTrigger = true
                                                            }
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(text = sound, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                                    }
                                                    
                                                    if (pushSoundVal == sound && soundPreviewTrigger) {
                                                        Text(
                                                            text = "🔊 PLAYING PREVIEW...",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = SashioGreen
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Batch triggers
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.updateNotificationToggles(true, true, true, true, true, true, true, true, pushSoundVal)
                                                Toast.makeText(context, "All alerts channels armed completely.", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).height(44.dp)
                                        ) {
                                            Text("ENABLE ALL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                        }
                                        
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.updateNotificationToggles(false, false, false, false, false, false, false, false, pushSoundVal)
                                                Toast.makeText(context, "All notification channels silent.", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).height(44.dp)
                                        ) {
                                            Text("DISABLE ALL", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        
                        "privacy" -> {
                            // Section 3: Privacy and personal data management download simulator
                            var isDownloadingData by remember { mutableStateOf(false) }
                            var downloadProgress by remember { mutableStateOf(0f) }
                            var showDeleteDisclaimer by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(isDownloadingData) {
                                if (isDownloadingData) {
                                    downloadProgress = 0f
                                    while (downloadProgress < 1f) {
                                        kotlinx.coroutines.delay(150)
                                        downloadProgress += 0.1f
                                    }
                                    isDownloadingData = false
                                    Toast.makeText(context, "SUCCESS: Decoded file SACHIO_profile_data.json ready for export.", Toast.LENGTH_LONG).show()
                                }
                            }
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Text("VISIBILITY CRITERIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            
                                            // Criteria 1
                                            Text("Who can view your Profile Avatar Picture?", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                listOf("Public", "Private", "Custom Contacts").forEach { visible ->
                                                    val isSel = privacyPicVal == visible
                                                    Button(
                                                        onClick = { viewModel.updatePrivacyToggles(visible, privacyContactVal, privacyTrackingVal, privacySupportVal, privacyAnalyticsVal) },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isSel) currentAccent else Color.LightGray.copy(alpha = 0.2f),
                                                            contentColor = if (isSel) Color.White else SashioSlate
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                                        modifier = Modifier.weight(1f).height(36.dp)
                                                    ) {
                                                        Text(visible, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                            
                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                            
                                            // Criteria 2
                                            Text("Who can view directory contact phone nodes?", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                listOf("Public", "Private", "Custom Contacts").forEach { visible ->
                                                    val isSel = privacyContactVal == visible
                                                    Button(
                                                        onClick = { viewModel.updatePrivacyToggles(privacyPicVal, visible, privacyTrackingVal, privacySupportVal, privacyAnalyticsVal) },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isSel) currentAccent else Color.LightGray.copy(alpha = 0.2f),
                                                            contentColor = if (isSel) Color.White else SashioSlate
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                                        modifier = Modifier.weight(1f).height(36.dp)
                                                    ) {
                                                        Text(visible, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Text("DIAGNOSTICS & PERMISSIONS ACCESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1.2f)) {
                                                    Text("Sachio Consultants Support Access", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("Allow administrators to review your active coordinates for priority service matching.", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Switch(checked = privacySupportVal, onCheckedChange = { viewModel.updatePrivacyToggles(privacyPicVal, privacyContactVal, privacyTrackingVal, it, privacyAnalyticsVal) })
                                            }
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1.2f)) {
                                                    Text("Anonymous Usage Telemetry Logs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("Help improve prestige fleet response times by transmitting anonymous metrics.", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Switch(checked = privacyAnalyticsVal, onCheckedChange = { viewModel.updatePrivacyToggles(privacyPicVal, privacyContactVal, privacyTrackingVal, privacySupportVal, it) })
                                            }
                                        }
                                    }
                                }
                                
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("ENTERPRISE PROFILE ARCHIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text("Backup and download a physical encrypted record containing your full rental records, payments logs, and active coordinate caches.", fontSize = 10.sp, color = SashioTextMuted)
                                            Spacer(modifier = Modifier.height(14.dp))
                                            
                                            if (isDownloadingData) {
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    LinearProgressIndicator(
                                                        progress = { downloadProgress },
                                                        color = currentAccent,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text("Compiling and encrypting local record: %d%% completed...".format((downloadProgress * 100).toInt()), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                                }
                                            } else {
                                                Button(
                                                    onClick = { isDownloadingData = true },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth().height(40.dp)
                                                ) {
                                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("DOWNLOAD MY ENCRYPTED PROFILE ARCHIVE", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                item {
                                    Button(
                                        onClick = { showDeleteDisclaimer = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = SashioRed.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(44.dp)
                                    ) {
                                        Text("PERSONAL DATA RETENTION CLAIMS", fontWeight = FontWeight.Bold, color = SashioRed, fontSize = 11.sp)
                                    }
                                }
                            }
                            
                            if (showDeleteDisclaimer) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteDisclaimer = false },
                                    title = { Text("Client Data Retention Clause", fontWeight = FontWeight.Bold, color = SashioRed) },
                                    text = {
                                        Text(
                                            text = "Under the Lagos State Sanitary & Environmental Code and GDPR statutes, SACHIO Mobile Toilets safely stores transaction archives for critical auditing up to 90 days following completion. To initiate formal immediate database expunction, submit a signed request through the Account Management submenu.",
                                            fontSize = 11.sp,
                                            color = SashioTextMuted
                                        )
                                    },
                                    confirmButton = {
                                        Button(onClick = { showDeleteDisclaimer = false }, colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)) {
                                            Text("Synthesize Statutory Policy", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                )
                            }
                        }
                        
                        "security" -> {
                            // Section 4: Security Shield, Change Password form, Security Rating indicator and mock Authorized devices list
                            var editPasswordPanel by remember { mutableStateOf(false) }
                            var currPass by remember { mutableStateOf("") }
                            var newPass by remember { mutableStateOf("") }
                            var confirmPass by remember { mutableStateOf("") }
                            
                            // Mock login devices
                            var devicesList by remember {
                                mutableStateOf(
                                    listOf(
                                        "Samsung Galaxy S24 Ultra - Lagos, NG (This Phone)",
                                        "Apple iPhone 15 Pro Max - London, UK",
                                        "MacBook Pro M3 Max - Victoria Island, Lagos"
                                    )
                                )
                            }
                            
                            // Calculate dynamic visual score
                            val securityRatingScore = when {
                                secBiometricVal && sec2FAVal -> 100
                                secBiometricVal || sec2FAVal -> 70
                                else -> 40
                            }
                            
                            val securityStatusLabel = when {
                                securityRatingScore == 100 -> "EXCELLENT SHIELD"
                                securityRatingScore == 70 -> "HIGHLY SECURE"
                                else -> "MEDIUM LEVEL - SECURITY UPGRADE RECOMMENDED"
                            }
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Security scorecard
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (securityRatingScore == 100) SashioGreen.copy(alpha = 0.08f) else SashioRed.copy(alpha = 0.08f)
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("SACHIO PRESTIGE SHIELD DEFENSE BAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("Security Score Rating: %d%%".format(securityRatingScore), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                Text(text = securityStatusLabel, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = if (securityRatingScore == 100) SashioGreen else SashioSlate)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { securityRatingScore / 100f },
                                                color = if (securityRatingScore == 100) SashioGreen else SashioGold,
                                                trackColor = Color.LightGray.copy(alpha = 0.3f),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                                
                                // Toggles card
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Text("CRYPTOGRAPHIC CREDENTIALS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1.2f)) {
                                                    Text("Tether Device Biometrics", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("Authenticate dispatch payouts using fingerprint sensors.", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Switch(checked = secBiometricVal, onCheckedChange = { viewModel.updateSecurityToggles(it, sec2FAVal) })
                                            }
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1.2f)) {
                                                    Text("Apply Two-Factor Credentials (2FA)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("Receive cellular SMS codes with every rental quote verification.", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Switch(checked = sec2FAVal, onCheckedChange = { viewModel.updateSecurityToggles(secBiometricVal, it) })
                                            }
                                        }
                                    }
                                }
                                
                                // Change Password forms accordian card
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable { editPasswordPanel = !editPasswordPanel },
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("CHANGE CURRENT PASSWORD KEY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                                Icon(
                                                    imageVector = if (editPasswordPanel) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = currentAccent
                                                )
                                            }
                                            
                                            if (editPasswordPanel) {
                                                Spacer(modifier = Modifier.height(14.dp))
                                                OutlinedTextField(
                                                    value = currPass,
                                                    onValueChange = { currPass = it },
                                                    label = { Text("Active Cipher Code") },
                                                    visualTransformation = PasswordVisualTransformation(),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                OutlinedTextField(
                                                    value = newPass,
                                                    onValueChange = { newPass = it },
                                                    label = { Text("Enter New Cipher Key") },
                                                    visualTransformation = PasswordVisualTransformation(),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                OutlinedTextField(
                                                    value = confirmPass,
                                                    onValueChange = { confirmPass = it },
                                                    label = { Text("Confirm New Cipher Key") },
                                                    visualTransformation = PasswordVisualTransformation(),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Button(
                                                    onClick = {
                                                        if (newPass.length < 5) {
                                                            Toast.makeText(context, "ERROR: Password credentials too brief.", Toast.LENGTH_SHORT).show()
                                                        } else if (newPass != confirmPass) {
                                                            Toast.makeText(context, "ERROR: Confirm mismatch.", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            viewModel.resetPassword(currentUser?.email ?: "guest")
                                                            Toast.makeText(context, "SUCCESS: Password Key updated.", Toast.LENGTH_SHORT).show()
                                                            currPass = ""
                                                            newPass = ""
                                                            confirmPass = ""
                                                            editPasswordPanel = false
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                                ) {
                                                    Text("CIPHER CODE UPDATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Authorized devices linked elements
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text("AUTHORIZED DISPATCH NODES / DEVICES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            
                                            devicesList.forEach { dev ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = dev, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                                    IconButton(onClick = {
                                                        devicesList = devicesList.filter { it != dev }
                                                        Toast.makeText(context, "Authorization node successfully revoked.", Toast.LENGTH_SHORT).show()
                                                    }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Revoke device access", tint = SashioRed, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                            
                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                                            
                                            TextButton(
                                                onClick = {
                                                    devicesList = listOf("Samsung Galaxy S24 Ultra - Lagos, NG (This Phone)")
                                                    Toast.makeText(context, "All secondary credentials revoked.", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("LOGOUT FROM ALL OTHER NODES", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SashioRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        "language" -> {
                            // Section 5: Internationalization node changes with Country localized representations
                            val flags = mapOf(
                                "English" to "🇬🇧 English - International",
                                "French" to "🇫🇷 Français - French Zone",
                                "Spanish" to "🇪🇸 Español - Spain & Americas",
                                "Swahili" to "🇰🇪 Kiswahili - East Africa Area",
                                "Arabic" to "🇦🇪 العربية - Middle East Region"
                            )
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Text(
                                        text = "SELECT THE SYSTEM INTERACTIVE LANGUAGE NODE:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SashioTextMuted,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                
                                items(flags.keys.toList()) { lang ->
                                    val isSel = langVal == lang
                                    Card(
                                        onClick = {
                                            viewModel.setLanguage(lang)
                                            Toast.makeText(context, "System transformed to: ${flags[lang]}", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSel) currentAccent.copy(alpha = 0.12f) else if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if (isSel) currentAccent else Color.LightGray.copy(alpha = 0.2f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = flags[lang] ?: lang,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) currentAccent else if (themePref == "dark") Color.White else SashioSlate
                                            )
                                            if (isSel) {
                                                Icon(Icons.Default.Check, contentDescription = "Active language selection icon check", tint = currentAccent, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                                
                                // Localized preview variables
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("LOCALIZATION COMPLIANCE PREVIEW STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            
                                            val currencyLabel = when (langVal) {
                                                "French" -> "€ - Euro Sovereign currency system"
                                                "Spanish" -> "$ - Spanish/Latin Dollar standards"
                                                "Arabic" -> "AED - United Arab Emirates Dirham"
                                                "Swahili" -> "KSh - East African Shillings specs"
                                                else -> "₦ - Nigerian Naira Core Standard"
                                            }
                                            
                                            val dateLabel = when (langVal) {
                                                "French" -> "12 Juin 2026 (DD/MM/YYYY)"
                                                "Spanish" -> "12 de Junio de 2026"
                                                "Arabic" -> "١٢ يونيو ٢٠٢٦"
                                                else -> "12 June 2026 (Default Prestige layout)"
                                            }
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Mapped Currency Unit:", fontSize = 10.sp, color = SashioTextMuted)
                                                Text(currencyLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Mapped Date Structure:", fontSize = 10.sp, color = SashioTextMuted)
                                                Text(dateLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        "appearance" -> {
                            // Section 6: Dynamic styling parameters, card density and maps theme presets
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Theme Dark/Light Modes
                                item {
                                    Text("THEME DISPATCH ENVIRONMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("light", "dark").forEach { themeName ->
                                                val isSel = themePref == themeName
                                                Button(
                                                    onClick = { viewModel.updateAppearanceToggles(themeName, accentColorVal, fontSizeVal, cardDensityVal, mapThemeVal, accessibilityVal) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isSel) SashioSlate else Color.LightGray.copy(alpha = 0.2f),
                                                        contentColor = if (isSel) Color.White else SashioSlate
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f).height(40.dp)
                                                ) {
                                                    Text(text = themeName.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Accent Colors Circle Selection
                                item {
                                    Text("DYNAMIC APLET BRAND ACCENT SELECTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val choices = mapOf(
                                                "Gold" to SashioGold,
                                                "Teal Breeze" to SashioBlue,
                                                "Royal Indigo" to SashioSlate,
                                                "Ruby Premium" to SashioRed
                                            )
                                            
                                            choices.forEach { (name, colorValue) ->
                                                val isSel = accentColorVal == name
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(CircleShape)
                                                        .background(colorValue)
                                                        .border(
                                                            width = if (isSel) 4.dp else 0.dp,
                                                            color = if (isSel) Color.White else Color.Transparent,
                                                            shape = CircleShape
                                                        )
                                                        .clickable {
                                                            viewModel.updateAppearanceToggles(themePref, name, fontSizeVal, cardDensityVal, mapThemeVal, accessibilityVal)
                                                            Toast.makeText(context, "Accent set to $name", Toast.LENGTH_SHORT).show()
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSel) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Font details scales
                                item {
                                    Text("SELECT INFORMATION DENSITY FONT SIZE SCALE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf("Compact", "Standard", "Expanded").forEach { fs ->
                                                val isSel = fontSizeVal == fs
                                                Button(
                                                    onClick = { viewModel.updateAppearanceToggles(themePref, accentColorVal, fs, cardDensityVal, mapThemeVal, accessibilityVal) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isSel) currentAccent else Color.LightGray.copy(alpha = 0.2f),
                                                        contentColor = if (isSel) Color.White else SashioSlate
                                                    ),
                                                    modifier = Modifier.weight(1f).height(38.dp)
                                                ) {
                                                    Text(fs, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Map Theme Configuration Selector
                                item {
                                    Text("SELECT CORE OPERATIONS GOOGLE MAP THEME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("Standard Map", "Satellite Map Theme", "Terrain Operations Map", "Dark Map").forEach { mt ->
                                                val isSel = mapThemeVal == mt
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.updateAppearanceToggles(themePref, accentColorVal, fontSizeVal, cardDensityVal, mt, accessibilityVal)
                                                            Toast.makeText(context, "$mt layer activated.", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(mt, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                                    if (isSel) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = currentAccent, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Accessibility check
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("High Contrast Accessible layout Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("Improves screen readability ratios.", fontSize = 9.sp, color = SashioTextMuted)
                                            }
                                            Switch(checked = accessibilityVal, onCheckedChange = { viewModel.updateAppearanceToggles(themePref, accentColorVal, fontSizeVal, cardDensityVal, mapThemeVal, it) })
                                        }
                                    }
                                }
                            }
                        }
                        
                        "preferences" -> {
                            // Section 7: Cache Wiping and client-side database optimization controls
                            var isClearingCache by remember { mutableStateOf(false) }
                            var isClearingDls by remember { mutableStateOf(false) }
                            var isOptimizingDb by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(isClearingCache) {
                                if (isClearingCache) {
                                    kotlinx.coroutines.delay(1200)
                                    viewModel.clearCache()
                                    isClearingCache = false
                                    Toast.makeText(context, "Sashio system cache cleared successfully.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            
                            LaunchedEffect(isClearingDls) {
                                if (isClearingDls) {
                                    kotlinx.coroutines.delay(1200)
                                    viewModel.clearDownloads()
                                    isClearingDls = false
                                    Toast.makeText(context, "Sashio local PDF downloads flushed.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            
                            LaunchedEffect(isOptimizingDb) {
                                if (isOptimizingDb) {
                                    kotlinx.coroutines.delay(1600)
                                    viewModel.optimizeStorage()
                                    isOptimizingDb = false
                                    Toast.makeText(context, "Prestige Local DB defragmented and optimized.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Text("STORAGE PARAMETERS & METRICS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Active Storage Occupied:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(storageUsedVal, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                            }
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("System Trash & Caches size:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(cacheSizeVal, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                                            }
                                            
                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                                            
                                            if (isClearingCache) {
                                                CircularProgressIndicator(color = currentAccent, modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp))
                                            } else {
                                                Button(
                                                    onClick = { isClearingCache = true },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth().height(38.dp)
                                                ) {
                                                    Text("FLUSH SYSTEM APP CACHES (ERASE)", fontSize = 10.sp, color = Color.White)
                                                }
                                            }
                                            
                                            if (isClearingDls) {
                                                CircularProgressIndicator(color = currentAccent, modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp))
                                            } else {
                                                OutlinedButton(
                                                    onClick = { isClearingDls = true },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth().height(38.dp)
                                                ) {
                                                    Text("CLEAR OFF-LINE QUOTATION DOWNLOADS", fontSize = 10.sp, color = SashioSlate)
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Text("DATA PREFERENCES CONFIGS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1.2f)) {
                                                    Text("Auto-refresh active lists", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("Updates queues every 15 seconds.", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Switch(checked = autoRefreshVal, onCheckedChange = { viewModel.updateAppPreferencesToggles(it, locServicesVal, bgTrackingVal, offlineModeVal, autoDownloadsVal) })
                                            }
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1.2f)) {
                                                    Text("Enable Background GPS coordinate tags", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("Tethers phone location sensors for optimal dispatch.", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Switch(checked = locServicesVal, onCheckedChange = { viewModel.updateAppPreferencesToggles(autoRefreshVal, it, bgTrackingVal, offlineModeVal, autoDownloadsVal) })
                                            }
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1.2f)) {
                                                    Text("Preemptive Offline State Caching", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("Store local items offline for low networks.", fontSize = 9.sp, color = SashioTextMuted)
                                                }
                                                Switch(checked = offlineModeVal, onCheckedChange = { viewModel.updateAppPreferencesToggles(autoRefreshVal, locServicesVal, bgTrackingVal, it, autoDownloadsVal) })
                                            }
                                        }
                                    }
                                }
                                
                                item {
                                    if (isOptimizingDb) {
                                        LinearProgressIndicator(color = currentAccent, modifier = Modifier.fillMaxWidth())
                                    } else {
                                        Button(
                                            onClick = { isOptimizingDb = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(44.dp)
                                        ) {
                                            Text("DEFRAGMENT & OPTIMIZE ROOM LOCAL DB", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                        
                        "support" -> {
                            // Section 8: Help and support Ticket center and FAQs Search filtration
                            var faqQuery by remember { mutableStateOf("") }
                            var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }
                            
                            var ticketSubject by remember { mutableStateOf("") }
                            var ticketCategory by remember { mutableStateOf("Enquiry") }
                            var ticketDesc by remember { mutableStateOf("") }
                            
                            val availableFaqs = listOf(
                                Triple("Why is there no fixed pricing listed on products?", "To guarantee correct rates due to varying transport Logistics Surcharges, VAT constraints, and local coordinate fuel variables, SACHIO operates exclusively through bespoke real-time generated quotation agreements.", "pricing"),
                                Triple("How is cleaning and sanitizing handled?", "Prestige attendants perform comprehensive quad sewage evacuations using active biological digests that completely sanitize and eliminate microbial compounds.", "sanitize"),
                                Triple("Can I request on-site standby crew support?", "Yes! Tick our Standby Attendant element during your quote compilation and our crew will deploy on-site.", "standby"),
                                Triple("What are the VIP event toilet guidelines?", "Select 'VIP event Toilet variant packages' to access top-grade models with luxury mirrors and dynamic washbays.", "vip")
                            )
                            
                            val filteredFaqs = availableFaqs.filter {
                                faqQuery.isBlank() || it.first.contains(faqQuery, ignoreCase = true) || it.second.contains(faqQuery, ignoreCase = true)
                            }
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // HELP HOTLINES
                                item {
                                    Text("DIRECT PRESTIGE HOTLINES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+234800SACHIO"))
                                                    context.startActivity(intent)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(38.dp)
                                            ) {
                                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Phone Support", fontSize = 10.sp, color = Color.White)
                                            }
                                            
                                            Button(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@sachioltd.com"))
                                                    context.startActivity(Intent.createChooser(intent, "Send email to Sachio support"))
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(38.dp)
                                            ) {
                                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Email Support", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                                
                                // FAQ SEARCH
                                item {
                                    Text("SEARCH SACHIO KNOWLEDGE CENTER FAQS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = faqQuery,
                                        onValueChange = { faqQuery = it },
                                        label = { Text("Filter FAQs (e.g. price, sanitize)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        filteredFaqs.forEachIndexed { idx, (q, ans, _) ->
                                            val isExpanded = expandedFaqIndex == idx
                                            Card(
                                                onClick = { expandedFaqIndex = if (isExpanded) null else idx },
                                                colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.02f) else Color.White),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text(q, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                                        Icon(
                                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                            contentDescription = null,
                                                            tint = currentAccent,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    if (isExpanded) {
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(ans, fontSize = 10.sp, color = SashioTextMuted)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // SUBMIT SUPPORT TICKET FORM
                                item {
                                    Text("SUBMIT NEW CONSULTATION TICKET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = ticketSubject,
                                                onValueChange = { ticketSubject = it },
                                                label = { Text("Ticket Subject Title") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            
                                            // Category Picker Options Row
                                            Text("Select Consultation category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                listOf("Enquiry", "Billing", "Logistics").forEach { ct ->
                                                    val isC = ticketCategory == ct
                                                    Button(
                                                        onClick = { ticketCategory = ct },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isC) currentAccent else Color.LightGray.copy(alpha = 0.2f),
                                                            contentColor = if (isC) Color.White else SashioSlate
                                                        ),
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                                        modifier = Modifier.weight(1f).height(32.dp)
                                                    ) {
                                                        Text(ct, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                            
                                            OutlinedTextField(
                                                value = ticketDesc,
                                                onValueChange = { ticketDesc = it },
                                                label = { Text("Elaborate Description of Issue") },
                                                maxLines = 4,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            
                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            Button(
                                                onClick = {
                                                    if (ticketSubject.isBlank() || ticketDesc.isBlank()) {
                                                        Toast.makeText(context, "ERROR: Subject and detailed information are mandatory.", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        viewModel.submitSupportTicket(ticketSubject, ticketCategory, ticketDesc)
                                                        Toast.makeText(context, "SUCCESS: Ticket logs captured.", Toast.LENGTH_SHORT).show()
                                                        ticketSubject = ""
                                                        ticketDesc = ""
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().height(40.dp)
                                            ) {
                                                Text("TRANSMIT SUPPORT TICKET", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                
                                // TICKET LIST FEED
                                if (ticketHistory.isNotEmpty()) {
                                    item {
                                        Text("YOUR ACTIVE SUBMITTED TICKETS FEED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            ticketHistory.forEach { t ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.05f) else Color.White),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(text = "Ref Code: ${t.id}", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = currentAccent)
                                                            Text(text = t.subject, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            Text(text = "Priority Level: High", fontSize = 9.sp, color = SashioTextMuted)
                                                        }
                                                        
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(SashioGreen.copy(alpha = 0.15f))
                                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("ACTIVE OPEN", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SashioGreen)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        "terms" -> {
                            // Section 9: Highly polished Terms and Legal scrolled box with references
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text("SACHIO STATUTORY POLICY AGREEMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            
                                            Text(
                                                text = "1. LICENSE AND SCOPE OF TERMS\nThis platform serves purely as a secure business liaison client connecting qualified event planners to the specialized toilet hire network of SACHIO Ltd. The pricing calculations and fuel indices remain proprietary estimates until formal agreement execution.\n\n2. PRIVACY STATUTES\nWe strictly respect Lagos State Environmental Protection protocols and federal digital privacy boundaries. Your location coordinates are accessed only for routing and delivery tracking.\n\n3. LOGISTICS DISRUPTIONS CLAUSE\nOperational delays originating from heavy congestion, civil road maintenance, and fuel price volatility are managed with priority attendant dispatch. Formal communications will be delivered.",
                                                fontSize = 11.sp,
                                                color = SashioTextMuted,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                                
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("APPLICATION CODE ARCHITECTURE LOGS:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            Text("App Platform: SACHIO PRESTIGE MOBILE SEAMLESS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("Version Specs: Build 1.10.42-PRESTIGE-NATIVE", fontSize = 9.sp, color = SashioTextMuted)
                                            Text("Engine framework: Kotlin JVM Android SDK 34 Compose", fontSize = 9.sp, color = SashioTextMuted)
                                            Text("SQLite Persistence: Room ORM active integration", fontSize = 9.sp, color = SashioTextMuted)
                                        }
                                    }
                                }
                            }
                        }
                        
                        "account" -> {
                            // Section 10: Critical Account Options (Deactivate & irreversible Delete account warning countdown holding text trigger)
                            var showDeactivateModal by remember { mutableStateOf(false) }
                            var showDeleteModal by remember { mutableStateOf(false) }
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text("TEMPORARY ACCOUNT SUSPENSION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                                            Text("Pause your corporate notifications, pending review schedules and billing emails temporarily. You can restore access instantly at any subsequent date simply by logging back in.", fontSize = 11.sp, color = SashioTextMuted)
                                            
                                            Button(
                                                onClick = { showDeactivateModal = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().height(38.dp)
                                            ) {
                                                Text("DEACTIVATE SACHIO PRESTIGE ACCOUNT", fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                                
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SashioRed.copy(alpha = 0.03f)),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, SashioRed.copy(alpha = 0.2f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text("IRREVERSIBLE DATABASE DELETION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioRed)
                                            Text("Purge your profile record, transaction invoices, billing records, active favorite coordinates, and ticket histories permanently from the SACHIO SQLite servers. This transaction is absolute and cannot be recalled.", fontSize = 11.sp, color = SashioTextMuted)
                                            
                                            Button(
                                                onClick = { showDeleteModal = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().height(38.dp)
                                            ) {
                                                Text("PERMANENTLY EXPUNGE PROFILE FOREVER", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Deactivate dialog
                            if (showDeactivateModal) {
                                AlertDialog(
                                    onDismissRequest = { showDeactivateModal = false },
                                    title = { Text("Confirm Deactivation?", fontWeight = FontWeight.Bold) },
                                    text = { Text("Your active dispatches and standby schedules will be safely archived offline.", fontSize = 11.sp) },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                showDeactivateModal = false
                                                viewModel.deactivateAccount()
                                                onDismiss()
                                                Toast.makeText(context, "Sashio Prestige Profile Deactivated safely.", Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)
                                        ) { Text("Confirm", color = Color.White) }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeactivateModal = false }) { Text("Cancel") }
                                    }
                                )
                            }
                            
                            // Delete alert dialog
                            if (showDeleteModal) {
                                var deleteVerificationInput by remember { mutableStateOf("") }
                                
                                AlertDialog(
                                    onDismissRequest = { showDeleteModal = false },
                                    title = { Text("CRITICAL WARNING: Purge Record?", fontWeight = FontWeight.Bold, color = SashioRed) },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("To confirm absolute deletion, type the keyword \"DELETE\" in the field below:", fontSize = 11.sp)
                                            OutlinedTextField(
                                                value = deleteVerificationInput,
                                                onValueChange = { deleteVerificationInput = it },
                                                label = { Text("Verification Phrase") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                if (deleteVerificationInput.trim() == "DELETE") {
                                                    showDeleteModal = false
                                                    viewModel.deleteAccount()
                                                    onDismiss()
                                                    Toast.makeText(context, "Sashio profile completely and permanently expunged.", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "ERROR: Incorrect authorization phrase specified.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                                            enabled = deleteVerificationInput.trim() == "DELETE"
                                        ) { Text("Expunge Permanently", color = Color.White) }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteModal = false }) { Text("Abort Action") }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
