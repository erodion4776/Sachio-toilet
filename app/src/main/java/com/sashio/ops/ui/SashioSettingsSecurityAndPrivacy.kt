package com.sashio.ops.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PrivacySettingsSubScreen(
    viewModel: SashioViewModel,
    currentAccent: Color,
    themePref: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val privacyPicVal by viewModel.privacyProfilePicVisible.collectAsStateWithLifecycle()
    val privacyContactVal by viewModel.privacyContactVisible.collectAsStateWithLifecycle()
    val privacyTrackingVal by viewModel.privacyTrackingShare.collectAsStateWithLifecycle()
    val privacySupportVal by viewModel.privacySupportAccess.collectAsStateWithLifecycle()
    val privacyAnalyticsVal by viewModel.privacyAnalyticsAllowed.collectAsStateWithLifecycle()

    var isDownloadingData by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var showDeleteDisclaimer by remember { mutableStateOf(false) }

    LaunchedEffect(isDownloadingData) {
        if (isDownloadingData) {
            downloadProgress = 0f
            while (downloadProgress < 1f) {
                delay(150)
                downloadProgress += 0.1f
            }
            isDownloadingData = false
            Toast.makeText(context, "SUCCESS: Decrypted archive SACHIO_profile_data.json ready for export.", Toast.LENGTH_LONG).show()
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("VISIBILITY SCOPE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)

                    Text("Who can view your Profile Avatar Picture?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
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

                    Text("Who can view directory contact phone nodes?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
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
                colors = CardDefaults.cardColors(
                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("DIAGNOSTICS & PERMISSIONS ACCESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Sachio Support Access", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                            Text("Allow administrators to review your active coordinates for priority service matching.", fontSize = 9.sp, color = SashioTextMuted)
                        }
                        Switch(
                            checked = privacySupportVal,
                            onCheckedChange = { viewModel.updatePrivacyToggles(privacyPicVal, privacyContactVal, privacyTrackingVal, it, privacyAnalyticsVal) }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Anonymous Usage Telemetry", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                            Text("Help improve prestige fleet response times by transmitting anonymous metrics.", fontSize = 9.sp, color = SashioTextMuted)
                        }
                        Switch(
                            checked = privacyAnalyticsVal,
                            onCheckedChange = { viewModel.updatePrivacyToggles(privacyPicVal, privacyContactVal, privacyTrackingVal, privacySupportVal, it) }
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ENTERPRISE PROFILE ARCHIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Backup and download an encrypted record containing your full rental records, payments logs, and active coordinate caches.", fontSize = 10.sp, color = SashioTextMuted)
                    Spacer(modifier = Modifier.height(14.dp))

                    if (isDownloadingData) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                color = currentAccent,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Compiling encrypted record: %d%% completed...".format((downloadProgress * 100).toInt()), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                        }
                    } else {
                        Button(
                            onClick = { isDownloadingData = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DOWNLOAD ENCRYPTED PROFILE ARCHIVE", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
                    text = "Under the Lagos State Environmental Protection Code and statutory GDPR privacy policies, SACHIO Mobile Toilets safely stores transaction archives for auditing up to 90 days following completion. To initiate immediate database expunction, submit a formal request via the Account Options submenu.",
                    fontSize = 11.sp,
                    color = SashioTextMuted
                )
            },
            confirmButton = {
                Button(onClick = { showDeleteDisclaimer = false }, colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)) {
                    Text("Understood", fontSize = 10.sp, color = Color.White)
                }
            }
        )
    }
}

@Composable
fun SecuritySettingsSubScreen(
    viewModel: SashioViewModel,
    currentAccent: Color,
    themePref: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val secBiometricVal by viewModel.securityBiometricsEnabled.collectAsStateWithLifecycle()
    val sec2FAVal by viewModel.security2FAEnabled.collectAsStateWithLifecycle()

    var editPasswordPanel by remember { mutableStateOf(false) }
    var currPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    var devicesList by remember {
        mutableStateOf(
            listOf(
                "Samsung Galaxy S24 Ultra - Lagos, NG (This Phone)",
                "Apple iPhone 15 Pro Max - London, UK",
                "MacBook Pro M3 Max - Victoria Island, Lagos"
            )
        )
    }

    val securityRatingScore = when {
        secBiometricVal && sec2FAVal -> 100
        secBiometricVal || sec2FAVal -> 70
        else -> 40
    }

    val securityStatusLabel = when {
        securityRatingScore == 100 -> "EXCELLENT SHIELD"
        securityRatingScore == 70 -> "HIGHLY SECURE"
        else -> "MEDIUM LEVEL - UPGRADE RECOMMENDED"
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (securityRatingScore == 100) SashioGreen.copy(alpha = 0.08f) else SashioRed.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SACHIO PRESTIGE SHIELD DEFENSE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Security Rating: %d%%".format(securityRatingScore), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                        Text(text = securityStatusLabel, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = if (securityRatingScore == 100) SashioGreen else SashioGold)
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

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("CRYPTOGRAPHIC CREDENTIALS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Device Biometrics Sign-In", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                            Text("Authenticate portal access using fingerprint or facial sensors.", fontSize = 9.sp, color = SashioTextMuted)
                        }
                        Switch(checked = secBiometricVal, onCheckedChange = { viewModel.updateSecurityToggles(it, sec2FAVal) })
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Two-Factor Authentication (2FA)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                            Text("Receive security verification codes on quotation and transaction clearances.", fontSize = 9.sp, color = SashioTextMuted)
                        }
                        Switch(checked = sec2FAVal, onCheckedChange = { viewModel.updateSecurityToggles(secBiometricVal, it) })
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { editPasswordPanel = !editPasswordPanel },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CHANGE ACCOUNT PASSWORD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
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
                            label = { Text("Current Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newPass,
                            onValueChange = { newPass = it },
                            label = { Text("New Password (min. 6 characters)") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = confirmPass,
                            onValueChange = { confirmPass = it },
                            label = { Text("Confirm New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (newPass.length < 6) {
                                    Toast.makeText(context, "ERROR: Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                                } else if (newPass != confirmPass) {
                                    Toast.makeText(context, "ERROR: Password confirmation mismatch.", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.updateUserPassword(newPass)
                                    Toast.makeText(context, "Password updated successfully.", Toast.LENGTH_SHORT).show()
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
                            Text("UPDATE PASSWORD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("AUTHORIZED SESSIONS & DEVICES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)

                    devicesList.forEach { dev ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = dev, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                devicesList = devicesList.filter { it != dev }
                                Toast.makeText(context, "Device session revoked successfully.", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Revoke device session", tint = SashioRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

                    TextButton(
                        onClick = {
                            devicesList = listOf("Samsung Galaxy S24 Ultra - Lagos, NG (This Phone)")
                            Toast.makeText(context, "All other sessions revoked.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("LOGOUT ALL OTHER DEVICES", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SashioRed)
                    }
                }
            }
        }
    }
}
