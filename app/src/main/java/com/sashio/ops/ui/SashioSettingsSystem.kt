package com.sashio.ops.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LanguageSettingsSubScreen(
    viewModel: SashioViewModel,
    currentAccent: Color,
    themePref: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val langVal by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    val flags = mapOf(
        "English" to "🇬🇧 English - International Standard",
        "French" to "🇫🇷 Français - French Regional",
        "Spanish" to "🇪🇸 Español - Spain & Americas",
        "Swahili" to "🇰🇪 Kiswahili - East Africa Standard",
        "Arabic" to "🇦🇪 العربية - Middle East Standard"
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "SELECT SYSTEM INTERACTIVE LANGUAGE:",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Active language selection check",
                            tint = currentAccent,
                            modifier = Modifier.size(18.dp)
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("LOCALIZATION COMPLIANCE PREVIEW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)

                    val currencyLabel = when (langVal) {
                        "French" -> "€ - Euro (EUR)"
                        "Spanish" -> "$ - US Dollar (USD)"
                        "Arabic" -> "AED - UAE Dirham"
                        "Swahili" -> "KSh - Kenyan Shilling"
                        else -> "₦ - Nigerian Naira (NGN)"
                    }

                    val dateLabel = when (langVal) {
                        "French" -> "15 Juin 2026 (DD/MM/YYYY)"
                        "Spanish" -> "15 de Junio de 2026"
                        "Arabic" -> "١٥ يونيو ٢٠٢٦"
                        else -> "15 June 2026 (Default Prestige layout)"
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mapped Currency Standard:", fontSize = 11.sp, color = SashioTextMuted)
                        Text(currencyLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mapped Date Structure:", fontSize = 11.sp, color = SashioTextMuted)
                        Text(dateLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                    }
                }
            }
        }
    }
}

@Composable
fun AppearanceSettingsSubScreen(
    viewModel: SashioViewModel,
    currentAccent: Color,
    themePref: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accentColorVal by viewModel.accentColor.collectAsStateWithLifecycle()
    val fontSizeVal by viewModel.fontSizeScale.collectAsStateWithLifecycle()
    val cardDensityVal by viewModel.cardDensity.collectAsStateWithLifecycle()
    val mapThemeVal by viewModel.mapTheme.collectAsStateWithLifecycle()
    val accessibilityVal by viewModel.accessibilityMode.collectAsStateWithLifecycle()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("THEME ENVIRONMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("light", "dark").forEach { themeName ->
                        val isSel = themePref == themeName
                        Button(
                            onClick = {
                                viewModel.updateAppearanceToggles(themeName, accentColorVal, fontSizeVal, cardDensityVal, mapThemeVal, accessibilityVal)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) SashioGold else Color.LightGray.copy(alpha = 0.2f),
                                contentColor = if (isSel) SashioSlate else if (themePref == "dark") Color.White else SashioSlate
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

        item {
            Text("BRAND ACCENT SELECTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                        val isSel 
