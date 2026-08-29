package com.sashio.ops.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ProfileSettingsSubScreen(
    viewModel: SashioViewModel,
    currentAccent: Color,
    themePref: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val altPhone by viewModel.userAltPhone.collectAsStateWithLifecycle()
    val bio by viewModel.userBio.collectAsStateWithLifecycle()
    val bizAddress by viewModel.userBusinessAddress.collectAsStateWithLifecycle()
    val profilePic by viewModel.userProfilePic.collectAsStateWithLifecycle()
    val location by viewModel.userLocation.collectAsStateWithLifecycle()

    var nameVal by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var emailVal by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    var phoneVal by remember(currentUser) { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var companyVal by remember(currentUser) { mutableStateOf(currentUser?.companyName ?: "") }
    var altPhoneVal by remember(altPhone) { mutableStateOf(altPhone) }
    var bioVal by remember(bio) { mutableStateOf(bio) }
    var addressVal by remember(bizAddress) { mutableStateOf(bizAddress) }
    var locationVal by remember(location) { mutableStateOf(location) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            viewModel.updateExtraProfileInfo(altPhoneVal, bioVal, addressVal, it.toString(), locationVal)
            Toast.makeText(context, "Custom avatar selected successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
                        "CHOOSE PRESET AVATAR OR SELECT FROM GALLERY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SashioTextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val presets = listOf(
                        "https://i.ibb.co/4ZM1G4WW/1000946646-removebg-preview.png",
                        "https://cdn-icons-png.flaticon.com/512/3135/3135715.png",
                        "https://cdn-icons-png.flaticon.com/512/4140/4140047.png",
                        "https://cdn-icons-png.flaticon.com/512/4140/4140037.png",
                        "https://cdn-icons-png.flaticon.com/512/1077/1077114.png"
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
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Gallery File", fontSize = 10.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.updateExtraProfileInfo(
                                    altPhoneVal,
                                    bioVal,
                                    addressVal,
                                    "https://i.ibb.co/4ZM1G4WW/1000946646-removebg-preview.png",
                                    locationVal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Default Avatar", fontSize = 10.sp, color = if (themePref == "dark") Color.White else SashioSlate)
                        }
                    }
                }
            }
        }

        item { OutlinedTextField(value = nameVal, onValueChange = { nameVal = it }, label = { Text("Patron Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = emailVal, onValueChange = { emailVal = it }, label = { Text("Corporate Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = phoneVal, onValueChange = { phoneVal = it }, label = { Text("Primary Hotline Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = companyVal, onValueChange = { companyVal = it }, label = { Text("Company Entity Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = altPhoneVal, onValueChange = { altPhoneVal = it }, label = { Text("Alternative Dispatch Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = locationVal, onValueChange = { locationVal = it }, label = { Text("State Coordinates / Location Zone") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = addressVal, onValueChange = { addressVal = it }, label = { Text("Principal Business Address") }, maxLines = 2, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = bioVal, onValueChange = { bioVal = it }, label = { Text("Corporate Biography / Notes") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = currentAccent.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("METRIC LOGS & META ARCHITECTURE:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                    Text("Account Reference ID: SCH-PREC-48201", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                    Text("Security Enlistment: Enrolled June, 2026", fontSize = 10.sp, color = SashioTextMuted)
                    Text("Network Node: Lagos Central Cluster Grid", fontSize = 10.sp, color = SashioTextMuted)
                }
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.updateUserProfileInfo(nameVal, emailVal, phoneVal, companyVal)
                    viewModel.updateExtraProfileInfo(altPhoneVal, bioVal, addressVal, profilePic, locationVal)
                    Toast.makeText(context, "Sashio Profile database updated successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("COMMIT PROFILE CHANGES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun NotificationSettingsSubScreen(
    viewModel: SashioViewModel,
    currentAccent: Color,
    themePref: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pushVal by viewModel.pushNotifications.collectAsStateWithLifecycle()
    val smsVal by viewModel.smsNotifications.collectAsStateWithLifecycle()
    val emailVal by viewModel.emailNotifications.collectAsStateWithLifecycle()
    val whatsappVal by viewModel.whatsappNotifications.collectAsStateWithLifecycle()
    val notifyBooking by viewModel.notifyBookingUpdates.collectAsStateWithLifecycle()
    val notifyQuote by viewModel.notifyQuotationUpdates.collectAsStateWithLifecycle()
    val notifyPay by viewModel.notifyPaymentConfirmations.collectAsStateWithLifecycle()
    val notifyAnnounce by viewModel.notifySystemAnnouncements.collectAsStateWithLifecycle()
    val pushSoundVal by viewModel.pushSound.collectAsStateWithLifecycle()

    var soundPreviewTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(soundPreviewTrigger) {
        if (soundPreviewTrigger) {
            delay(1000)
            soundPreviewTrigger = false
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
                    Text("CORE SERVICE DISPATCH CHANNELS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("App Push Banner Alerts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                            Text("System real-time tray notices", fontSize = 9.sp, color = SashioTextMuted)
                        }
                        Switch(
                            checked = pushVal,
                            onCheckedChange = {
                                viewModel.updateNotificationToggles(it, smsVal, emailVal, whatsappVal, notifyBooking, notifyQuote, notifyPay, notifyAnnounce, pushSoundVal)
                            }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Direct SMS Notifications", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                            Text("Offline cellular logistics alerts", fontSize = 9.sp, color = SashioTextMuted)
                        }
                        Switch(
                            checked = smsVal,
                            onCheckedChange = {
                                viewModel.updateNotificationToggles(pushVal, it, emailVal, whatsappVal, notifyBooking, notifyQuote, notifyPay, notifyAnnounce, pushSoundVal)
                            }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Prestige Email Digests", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                            Text("Detailed VAT invoices, PDF formal quotes", fontSize = 9.sp, color = SashioTextMuted)
                        }
                        Switch(
                            checked = emailVal,
                            onCheckedChange = {
                                viewModel.updateNotificationToggles(pushVal, smsVal, it, whatsappVal, notifyBooking, notifyQuote, notifyPay, notifyAnnounce, pushSoundVal)
                            }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("WhatsApp Direct Updates", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                            Text("Pre-filled quotations forms & live chat help", fontSize = 9.sp, color = SashioTextMuted)
                        }
                        Switch(
                            checked = whatsappVal,
                            onCheckedChange = {
                                viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, it, notifyBooking, notifyQuote, notifyPay, notifyAnnounce, pushSoundVal)
                            }
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("CATEGORY-SPECIFIC FILTER POLICIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Active Booking Status Alterations", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                        Switch(
                            checked = notifyBooking,
                            onCheckedChange = {
                                viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, whatsappVal, it, notifyQuote, notifyPay, notifyAnnounce, pushSoundVal)
                            }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Quotation Issued / Review Logs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                        Switch(
                            checked = notifyQuote,
                            onCheckedChange = {
                                viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, whatsappVal, notifyBooking, it, notifyPay, notifyAnnounce, pushSoundVal)
                            }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Payment Confirmations & Settlement", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                        Switch(
                            checked = notifyPay,
                            onCheckedChange = {
                                viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, whatsappVal, notifyBooking, notifyQuote, it, notifyAnnounce, pushSoundVal)
                            }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Sachio Global Announcements", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                        Switch(
                            checked = notifyAnnounce,
                            onCheckedChange = {
                                viewModel.updateNotificationToggles(pushVal, smsVal, emailVal, whatsappVal, notifyBooking, notifyQuote, notifyPay, it, pushSoundVal)
                            }
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
                    Text("SELECT AUDIO NOTIFICATION CHIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
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
                                Text(
                                    text = sound,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (themePref == "dark") Color.White else SashioSlate
                                )
                            }

                            if (pushSoundVal == sound && soundPreviewTrigger) {
                                Text("PLAYING PREVIEW...", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SashioGreen)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        viewModel.updateNotificationToggles(true, true, true, true, true, true, true, true, pushSoundVal)
                        Toast.makeText(context, "All notification channels enabled.", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(context, "All notification channels silenced.", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text("DISABLE ALL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (themePref == "dark") Color.White else SashioSlate)
                }
            }
        }
    }
}
