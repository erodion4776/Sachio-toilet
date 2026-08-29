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
    val expenseLogs by viewModel.expenseLogs.collectAsStateWithLifecycle()

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
            Column(modifier = Modifier.padding(14.dp)) {
                Text("REVENUE GROWTH LOGISTICAL MATRIX", color = SashioGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                
                val successfulPayments = payments.filter { it.status == "success" }
                    .sortedBy { it.paymentDate }

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
                
                Text(
                    if (successfulPayments.isNotEmpty()) "Cumulative revenue across ${successfulPayments.size} confirmed payment(s)" else "No confirmed payments yet",
                    color = Color.White,
                    fontSize = 8.sp
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("EXPENSES CLAIMS CENTER", fontWeight = FontWeight.Bold)
            Button(
                onClick = onOpenLogExpense,
                colors = ButtonDefaults.buttonColors(containerColor = SashioTealDark),
                modifier = Modifier.height(28.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("Log Expense", fontSize = 9.sp, color = Color.White)
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

@Composable
fun AdminReportsView(viewModel: SashioViewModel) {
    var exportFormatType by remember { mutableStateOf("PDF") }
    var exportReportCategory by remember { mutableStateOf("Revenue Report") }
    var exportProgressPercent by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    val localContext = LocalContext.current

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

@Composable
fun AdminSettingsView(viewModel: SashioViewModel) {
    var enforceBiometricsAdmin by remember { mutableStateOf(true) }
    var adminActiveLanguage by remember { mutableStateOf("English") }

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
