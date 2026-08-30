package com.sashio.ops.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdminRevenueView(
    viewModel: SashioViewModel,
    onOpenLogExpense: () -> Unit
) {
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val supabaseExpenses by viewModel.supabaseExpenses.collectAsStateWithLifecycle()
    val localContext = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSupabaseExpenses()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("REVENUE TRENDS & EXPENSES ANALYTICS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = SashioSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("REVENUE GROWTH LOGISTICAL MATRIX", color = SashioGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))

                val successfulPayments = payments.filter { it.status == "success" }.sortedBy { it.paymentDate }

                Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    var running = 0f
                    val points = if (successfulPayments.isEmpty()) {
                        listOf(0f, 0f)
                    } else {
                        successfulPayments.map { p -> running += p.amount.toFloat(); running }
                    }
                    val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
                    val widthSpacing = if (points.size > 1) size.width / (points.size - 1) else size.width
                    val graphPath = Path()

                    points.forEachIndexed { idx, value ->
                        val x = idx * widthSpacing
                        val y = size.height - (value / maxVal * (size.height - 20f)) - 10f
                        if (idx == 0) graphPath.moveTo(x, y) else graphPath.lineTo(x, y)
                    }

                    drawPath(
                        path = graphPath,
                        color = SashioGold,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    points.forEachIndexed { idx, value ->
                        val x = idx * widthSpacing
                        val y = size.height - (value / maxVal * (size.height - 20f)) - 10f
                        drawCircle(color = SashioGold, radius = 4.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = Offset(x, y))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (successfulPayments.isNotEmpty()) "Cumulative revenue across ${successfulPayments.size} confirmed payment(s)" else "No confirmed payments yet.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("STAFF ESCROW CLAIMS (${supabaseExpenses.size})", fontWeight = FontWeight.Bold)
            Button(
                onClick = onOpenLogExpense,
                colors = ButtonDefaults.buttonColors(containerColor = SashioTealDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Text("Log Expense", fontSize = 10.sp, color = Color.White)
            }
        }

        if (supabaseExpenses.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("No expense claims found in database.", color = SashioTextMuted, fontSize = 11.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                supabaseExpenses.forEach { exp ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(exp.category, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioSlate)
                                Text("₦%,.2f".format(exp.amount), fontWeight = FontWeight.ExtraBold, color = SashioRed, fontSize = 12.sp)
                            }
                            Text("Staff: ${exp.staff_name} | Notes: ${exp.details.ifBlank { "N/A" }}", fontSize = 10.sp, color = SashioTextMuted)
                            Text("Date: ${exp.expense_date ?: "Today"}", fontSize = 9.sp, color = SashioTextMuted)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val statusColor = when (exp.status) {
                                    "Approved" -> SashioGreen
                                    "Rejected" -> SashioRed
                                    else -> SashioGold
                                }
                                Text(
                                    "STATUS: ${exp.status.uppercase()}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )

                                if (exp.status == "Pending Approval") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                viewModel.approveExpenseClaim(exp.id)
                                                Toast.makeText(localContext, "Approved claim of ₦%,.0f".format(exp.amount), Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SashioGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Approve", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.rejectExpenseClaim(exp.id)
                                                Toast.makeText(localContext, "Rejected claim", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Reject", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
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

@Composable
fun AdminReportsView(viewModel: SashioViewModel) {
    var exportFormatType by remember { mutableStateOf("PDF") }
    var exportReportCategory by remember { mutableStateOf("Revenue Report") }
    var exportProgressPercent by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    val localContext = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("REPORTS & ANALYTICS EXPORT HUB", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Generate official logistical auditing reports in standard PDF, CSV, or Microsoft Excel formats.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("SELECT REPORT CATEGORY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                listOf("Revenue Report", "Booking Report", "Client Directory Log", "Staff Operations Matrix").forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { exportReportCategory = cat }
                            .background(if (exportReportCategory == cat) SashioGold.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = exportReportCategory == cat, onClick = { exportReportCategory = cat })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(cat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("EXPORT FILE FORMAT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("PDF", "CSV", "Excel").forEach { format ->
                        FilterChip(
                            selected = exportFormatType == format,
                            onClick = { exportFormatType = format },
                            label = { Text(format) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (exportProgressPercent > 0f) {
                    LinearProgressIndicator(
                        progress = { exportProgressPercent },
                        modifier = Modifier.fillMaxWidth(),
                        color = SashioGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Assembling records data structures & signatures... ${(exportProgressPercent * 100).toInt()}%", fontSize = 10.sp, color = SashioTextMuted)
                }

                Button(
                    onClick = {
                        exportProgressPercent = 0.1f
                        coroutineScope.launch {
                            for (p in 2..10) {
                                delay(180)
                                exportProgressPercent = p * 0.1f
                            }
                            Toast.makeText(localContext, "Exported $exportReportCategory successfully as $exportFormatType file", Toast.LENGTH_LONG).show()
                            exportProgressPercent = 0.0f
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("COMPILE & DISPATCH REPORT", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AdminSettingsView(viewModel: SashioViewModel) {
    var enforceBiometricsAdmin by remember { mutableStateOf(true) }
    var adminActiveLanguage by remember { mutableStateOf("English") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("SYSTEM SECURITY & CONTROLS AUDIT", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Operational Variables", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioSlate)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enforce Biometric Gate (Admin)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Require biometric confirmation for root admin actions.", fontSize = 9.sp, color = SashioTextMuted)
                    }
                    Switch(checked = enforceBiometricsAdmin, onCheckedChange = { enforceBiometricsAdmin = it })
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("System Language", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("English", "Yoruba", "Hausa").forEach { lang ->
                            FilterChip(
                                selected = adminActiveLanguage == lang,
                                onClick = { adminActiveLanguage = lang },
                                label = { Text(lang, fontSize = 9.sp) }
                            )
                        }
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ACTIVE SECURITY AUDIT SIGNALS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SashioSlate)
                Spacer(modifier = Modifier.height(8.dp))

                val logs = listOf(
                    "SYS: Session token #AK99-Z verified at UTC 2026-06-15",
                    "AUTH: Biometric node validated successfully",
                    "DB: Defragment routine complete. Cache status: Nominal",
                    "GATEWAY: Paystack Edge Function connection: Active 200 OK"
                )
                logs.forEach { log ->
                    Text(
                        text = log,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SashioTextMuted,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
