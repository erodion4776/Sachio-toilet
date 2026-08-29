package com.sashio.ops.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*

@Composable
fun SashioSettingsScreen(
    viewModel: SashioViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val profilePic by viewModel.userProfilePic.collectAsStateWithLifecycle()
    val themePref by viewModel.themePreference.collectAsStateWithLifecycle()
    val accentColorVal by viewModel.accentColor.collectAsStateWithLifecycle()

    var activeSubScreen by remember { mutableStateOf<String?>(null) }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SashioSlate)
                    .statusBarsPadding()
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
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (activeSubScreen == null) "SETTINGS INDEX" else activeSubScreen!!.uppercase().replace("_", " "),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = SashioRed.copy(alpha = 0.2f))
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Settings", tint = SashioRed)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                if (activeSubScreen == null) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.05f) else Color.White),
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
                                            currentUser?.name ?: "Sachio Patron",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (themePref == "dark") Color.White else SashioSlate
                                        )
                                        Text(
                                            currentUser?.email ?: "client@sashio.com",
                                            fontSize = 11.sp,
                                            color = SashioTextMuted
                                        )
                                        Text(
                                            "Phone: ${currentUser?.phoneNumber?.takeIf { it.isNotBlank() } ?: "080-SACHIO-M"}",
                                            fontSize = 10.sp,
                                            color = SashioTextMuted
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(SashioSlate)
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    (currentUser?.role ?: "Client").uppercase(),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SashioGold
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(currentAccent.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    "GOLD VIP STATUS",
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

                        item {
                            Text(
                                "APPLICATION CONFIGURATIONS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SashioTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }

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
                                colors = CardDefaults.cardColors(containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White),
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
                                            Icon(imageVector = icon, contentDescription = null, tint = currentAccent, modifier = Modifier.size(18.dp))
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
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = null,
                                        tint = SashioTextMuted,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .scale(scaleX = -1f, scaleY = 1f)
                                    )
                                }
                            }
                        }

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
                    when (activeSubScreen) {
                        "profile" -> ProfileSettingsSubScreen(viewModel, currentAccent, themePref) { activeSubScreen = null }
                        "notifications" -> NotificationSettingsSubScreen(viewModel, currentAccent, themePref) { activeSubScreen = null }
                        "privacy" -> PrivacySettingsSubScreen(viewModel, currentAccent, themePref) { activeSubScreen = null }
                        "security" -> SecuritySettingsSubScreen(viewModel, currentAccent, themePref) { activeSubScreen = null }
                        "language" -> LanguageSettingsSubScreen(viewModel, currentAccent, themePref) { activeSubScreen = null }
                        "appearance" -> AppearanceSettingsSubScreen(viewModel, currentAccent, themePref) { activeSubScreen = null }
                        "preferences" -> PreferencesSettingsSubScreen(viewModel, currentAccent, themePref) { activeSubScreen = null }
                        "support" -> SupportSettingsSubScreen(viewModel, currentAccent, themePref) { activeSubScreen = null }
                        "terms" -> TermsSettingsSubScreen(viewModel, currentAccent, themePref) { activeSubScreen = null }
                        "account" -> AccountSettingsSubScreen(viewModel, currentAccent, themePref) { activeSubScreen = null }
                    }
                }
            }
        }
    }
}
