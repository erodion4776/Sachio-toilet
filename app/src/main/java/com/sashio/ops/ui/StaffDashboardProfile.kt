package com.sashio.ops.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.data.UserProfile
import com.sashio.ops.ui.theme.*

@Composable
fun StaffDashboardProfileTab(
    viewModel: SashioViewModel,
    staff: UserProfile?,
    workOfflineToggle: Boolean,
    onWorkOfflineChange: (Boolean) -> Unit,
    onAddExpenseClick: () -> Unit
) {
    val supabaseExpenses by viewModel.supabaseExpenses.collectAsStateWithLifecycle()

    // Automatically load live expenses from Supabase on opening tab
    LaunchedEffect(Unit) {
        viewModel.loadSupabaseExpenses()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SashioGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = staff?.name?.firstOrNull()?.toString()?.uppercase() ?: "O",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = SashioSlate
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = staff?.name ?: "Obinna Okafor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "CHIEF SANITATION SPECIALIST",
                            fontSize = 10.sp,
                            color = SashioGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = staff?.phoneNumber?.takeIf { it.isNotBlank() } ?: "+234 812 345 6789",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = staff?.email ?: "staff@sashio.com",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Offline Work Module", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text("Cache job checklists and field inspections locally.", color = Color.LightGray, fontSize = 11.sp)
                    }

                    Switch(
                        checked = workOfflineToggle,
                        onCheckedChange = onWorkOfflineChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SashioGold,
                            checkedTrackColor = SashioGold.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }

        item {
            Column {
                Text(
                    "VERIFIED QUALIFICATIONS & ACCREDITATION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SashioGold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            "OSHA Health & Site Safety Certified - Level II",
                            "Premium Restroom Tow Rig Towing Hooking Licence",
                            "Eco-Blue Biocide Chemical Handling Accreditation",
                            "Lagos State Environmental Sanitation Operator Cleared"
                        ).forEach { cert ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SashioGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cert, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ESCROW EXPENSE CLAIMS & FUEL LOGS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SashioGold,
                        letterSpacing = 1.sp
                    )
                    Button(
                        onClick = onAddExpenseClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SUBMIT EXPENSE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (supabaseExpenses.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("No expense claims submitted yet.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        supabaseExpenses.forEach { exp ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SashioSlate.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exp.category.uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = SashioGold,
                                            fontSize = 12.sp
                                        )
                                        if (exp.details.isNotBlank()) {
                                            Text(exp.details, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                                        }
                                        Text(exp.expense_date ?: "Today", fontSize = 9.sp, color = Color.LightGray)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₦%,.2f".format(exp.amount), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val statusColor = when (exp.status) {
                                            "Approved" -> SashioGreen
                                            "Rejected" -> SashioRed
                                            else -> SashioGold
                                        }
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = exp.status.uppercase(),
                                                color = statusColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
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
        }
    }
}
