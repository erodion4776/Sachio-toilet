
package com.sashio.ops.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sashio.ops.R
import com.sashio.ops.data.BiometricAuthHelper
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*
import kotlinx.coroutines.delay

@Composable
private fun PasswordVisibilityToggle(
    visible: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (visible) {
                Icons.Default.VisibilityOff
            } else {
                Icons.Default.Visibility
            },
            contentDescription = if (visible) {
                "Hide password"
            } else {
                "Show password"
            },
            tint = SashioGold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: SashioViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    val headerLayoutType by remember { mutableStateOf("centered") }

    Box(modifier = Modifier.fillMaxSize()) {
        LuxuryVideoBackgroundEffect()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (headerLayoutType == "inline") {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        title = {
                            Row(
                                modifier = Modifier.padding(start = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = R.drawable.sachio_logo,
                                    contentDescription = "Sachio Logo",
                                    modifier = Modifier.size(46.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "SACHIO EXPRESS",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 2.sp
                                )
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        AsyncImage(
                            model = R.drawable.sachio_logo,
                            contentDescription = "Sachio Logo",
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "SACHIO EXPRESS",
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

                Spacer(modifier = Modifier.height(24.dp))

                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Sign In To Portal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            "Comfort. Hygiene. Dignity Anywhere.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = {
                                Text(
                                    "Email Address",
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = SashioGold
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SashioGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = {
                                Text(
                                    "Password",
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = SashioGold
                                )
                            },
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                PasswordVisibilityToggle(
                                    visible = passwordVisible,
                                    onToggle = {
                                        passwordVisible = !passwordVisible
                                    }
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SashioGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            singleLine = true
                        )

                        TextButton(
                            onClick = {
                                viewModel.setScreen("forgot_password")
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("forgot_password_btn")
                        ) {
                            Text(
                                "Recover Credentials?",
                                color = SashioGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        loginError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                err,
                                color = SashioRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.loginWithEmail(email, null, password)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SashioGold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_login_button")
                        ) {
                            Text(
                                "Enter System".uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }

                        val bioContext = LocalContext.current
                        val bioActivity = bioContext as? FragmentActivity

                        var biometricAvailable by remember {
                            mutableStateOf(false)
                        }

                        var hasSavedBioLogin by remember {
                            mutableStateOf(false)
                        }

                        LaunchedEffect(Unit) {
                            biometricAvailable =
                                BiometricAuthHelper.isBiometricAvailable(bioContext)

                            hasSavedBioLogin =
                                BiometricAuthHelper.hasSavedCredentials(bioContext)
                        }

                        if (biometricAvailable) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (bioActivity == null) {
                                            Toast.makeText(
                                                bioContext,
                                                "Biometric authentication is unavailable.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            BiometricAuthHelper.authenticateAndGetCredentials(
                                                activity = bioActivity,
                                                title = "Biometric Verification",
                                                onSuccess = { savedEmail, savedPassword ->
                                                    viewModel.loginWithEmail(
                                                        savedEmail,
                                                        null,
                                                        savedPassword
                                                    )
                                                },
                                                onError = { message ->
                                                    Toast.makeText(
                                                        bioContext,
                                                        message,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            )
                                        }
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        SashioGold.copy(alpha = 0.3f)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = SashioGold
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("touch_id_login")
                                ) {
                                    Icon(
                                        Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        "Biometrics",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (bioActivity == null) {
                                            Toast.makeText(
                                                bioContext,
                                                "Biometric authentication is unavailable.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            BiometricAuthHelper.authenticateAndGetCredentials(
                                                activity = bioActivity,
                                                title = "Biometric Verification",
                                                onSuccess = { savedEmail, savedPassword ->
                                                    viewModel.loginWithEmail(
                                                        savedEmail,
                                                        null,
                                                        savedPassword
                                                    )
                                                },
                                                onError = { message ->
                                                    Toast.makeText(
                                                        bioContext,
                                                        message,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            )
                                        }
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        SashioGold.copy(alpha = 0.3f)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = SashioGold
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("face_id_login")
                                ) {
                                    Icon(
                                        Icons.Default.Face,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        "Face Scanner",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (!hasSavedBioLogin) {
                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    "Sign in with your password once to enable biometric sign-in.",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "New to SACHIO? ",
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    TextButton(
                        onClick = {
                            viewModel.setScreen("register")
                        },
                        modifier = Modifier.testTag("goto_register")
                    ) {
                        Text(
                            "Register Profile",
                            color = SashioGold,
                            fontWeight = FontWeight.Bold
                        )
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
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    title = {
                        Text(
                            "Create Account",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                viewModel.setScreen("login")
                            },
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Go back",
                                tint = Color.White
                            )
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
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.75f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Register Mobile Account",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Register to join our luxurious sanitation network.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = {
                                Text(
                                    "Full Name",
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("reg_name_input")
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = {
                                Text(
                                    "Email Address",
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("reg_email_input")
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = {
                                Text(
                                    "Phone Number",
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("reg_phone_input")
                        )

                        OutlinedTextField(
                            value = company,
                            onValueChange = { company = it },
                            label = {
                                Text(
                                    "Company Name (Optional)",
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = {
                                Text(
                                    "Password",
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            },
                            colors = fieldColors,
                            singleLine = true,
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                PasswordVisibilityToggle(
                                    visible = passwordVisible,
                                    onToggle = {
                                        passwordVisible = !passwordVisible
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("reg_password_input")
                        )

                        Text(
                            "All new sign-ups start as a Client account. Staff and admin access is granted separately by an administrator.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        signUpError?.let { err ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                err,
                                color = SashioRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.signUpUser(
                                    name,
                                    email,
                                    "client",
                                    phone,
                                    company,
                                    password
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SashioGold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("reg_submit_button")
                        ) {
                            Text(
                                "Create Account Plan".uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
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

    val resetSent by viewModel.passwordResetSent.collectAsStateWithLifecycle()
    val resetError by viewModel.passwordResetError.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        LuxuryVideoBackgroundEffect()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    title = {
                        Text(
                            "Recover Password",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                viewModel.setScreen("login")
                            },
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Go back",
                                tint = Color.White
                            )
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
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.75f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Reset Verification",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Enter your account email. We'll send you a link to reset your password.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = {
                                Text(
                                    "Email Address",
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SashioGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reset_email_input"),
                            singleLine = true
                        )

                        if (resetSent) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Reset link sent - check your email and tap the link to set a new password.",
                                color = SashioGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        resetError?.let { err ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                err,
                                color = SashioRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.resetPassword(email)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SashioGold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("reset_submit_btn")
                        ) {
                            Text(
                                "Send Reset Link",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetNewPasswordScreen(viewModel: SashioViewModel) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val error by viewModel.newPasswordError.collectAsStateWithLifecycle()
    val success by viewModel.newPasswordSetSuccessfully.collectAsStateWithLifecycle()

    LaunchedEffect(success) {
        if (success) {
            delay(1800)
            viewModel.logout()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LuxuryVideoBackgroundEffect()

        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.75f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        if (success) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SashioGreen,
                                modifier = Modifier.size(40.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "Password updated",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "Please sign in again with your new password.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                "Set New Password",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Choose a new password for your account.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = {
                                    Text(
                                        "New Password",
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                },
                                visualTransformation = if (newPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    PasswordVisibilityToggle(
                                        visible = newPasswordVisible,
                                        onToggle = {
                                            newPasswordVisible = !newPasswordVisible
                                        }
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = SashioGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_password_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = {
                                    Text(
                                        "Confirm Password",
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                },
                                visualTransformation = if (confirmPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    PasswordVisibilityToggle(
                                        visible = confirmPasswordVisible,
                                        onToggle = {
                                            confirmPasswordVisible = !confirmPasswordVisible
                                        }
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = SashioGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_password_input"),
                                singleLine = true
                            )

                            error?.let { err ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    err,
                                    color = SashioRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (
                                confirmPassword.isNotEmpty() &&
                                confirmPassword != newPassword
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Passwords don't match.",
                                    color = SashioRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    viewModel.updateUserPassword(newPassword)
                                },
                                enabled = newPassword.isNotBlank() &&
                                    newPassword == confirmPassword,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SashioGold
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("set_new_password_btn")
                            ) {
                                Text(
                                    "Update Password",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
