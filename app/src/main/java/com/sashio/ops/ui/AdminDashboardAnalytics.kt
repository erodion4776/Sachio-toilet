package com.sashio.ops.ui

import android.graphics.Paint as AndroidPaint
import android.graphics.pdf.PdfDocument
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    var pendingExportBytes by remember { mutableStateOf<ByteArray?>(null) }

    val localContext = LocalContext.current

    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val clients by viewModel.allProfiles.collectAsStateWithLifecycle()
    val staff by viewModel.staffProfiles.collectAsStateWithLifecycle()

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            try {
                val bytes = pendingExportBytes
                    ?: throw IllegalStateException("No report data available")

                localContext.contentResolver.openOutputStream(uri)?.use {
                    it.write(bytes)
                } ?: throw IllegalStateException("Unable to open destination")

                Toast.makeText(
                    localContext,
                    "PDF report saved successfully",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    localContext,
                    "PDF export failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                pendingExportBytes = null
                exportProgressPercent = 0f
            }
        } else {
            pendingExportBytes = null
            exportProgressPercent = 0f
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val bytes = pendingExportBytes
                    ?: throw IllegalStateException("No report data available")

                localContext.contentResolver.openOutputStream(uri)?.use {
                    it.write(bytes)
                } ?: throw IllegalStateException("Unable to open destination")

                Toast.makeText(
                    localContext,
                    "CSV report saved successfully",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    localContext,
                    "CSV export failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                pendingExportBytes = null
                exportProgressPercent = 0f
            }
        } else {
            pendingExportBytes = null
            exportProgressPercent = 0f
        }
    }

    fun dateText(timestamp: Long): String {
        return SimpleDateFormat(
            "dd MMM yyyy HH:mm",
            Locale.getDefault()
        ).format(Date(timestamp))
    }

    fun csvCell(value: Any?): String {
        var text = value?.toString() ?: ""

        // Prevent spreadsheet formula injection.
        if (text.trimStart().startsWith(
                "="
            ) || text.trimStart().startsWith(
                "+"
            ) || text.trimStart().startsWith(
                "-"
            ) || text.trimStart().startsWith(
                "@"
            )
        ) {
            text = "'$text"
        }

        return "\"${text.replace("\"", "\"\"")}\""
    }

    fun createReportRows(): Pair<List<String>, List<List<String>>> {
        return when (exportReportCategory) {

            "Revenue Report" -> {
                val rows = payments.map { payment ->
                    listOf(
                        payment.id.toString(),
                        payment.bookingId.toString(),
                        payment.reference,
                        payment.amount.toString(),
                        payment.status,
                        payment.channel,
                        dateText(payment.paymentDate)
                    )
                }

                Pair(
                    listOf(
                        "Payment ID",
                        "Booking ID",
                        "Reference",
                        "Amount",
                        "Status",
                        "Channel",
                        "Payment Date"
                    ),
                    rows
                )
            }

            "Booking Report" -> {
                val rows = bookings.map { booking ->
                    listOf(
                        booking.id.toString(),
                        booking.clientName,
                        booking.clientPhone,
                        booking.clientEmail,
                        booking.serviceType,
                        booking.serviceMode,
                        booking.address,
                        booking.status,
                        booking.paymentStatus,
                        booking.quotedPrice.toString(),
                        dateText(booking.createdAt)
                    )
                }

                Pair(
                    listOf(
                        "Booking ID",
                        "Client",
                        "Phone",
                        "Email",
                        "Service",
                        "Service Mode",
                        "Address",
                        "Booking Status",
                        "Payment Status",
                        "Quoted Price",
                        "Created"
                    ),
                    rows
                )
            }

            "Client Directory Log" -> {
                val rows = clients
                    .filter { it.role.equals("client", ignoreCase = true) }
                    .map { client ->
                        listOf(
                            client.id.toString(),
                            client.name,
                            client.email,
                            client.phoneNumber,
                            client.companyName,
                            client.role
                        )
                    }

                Pair(
                    listOf(
                        "Client ID",
                        "Name",
                        "Email",
                        "Phone",
                        "Company",
                        "Role"
                    ),
                    rows
                )
            }

            else -> {
                val rows = staff.map { member ->
                    listOf(
                        member.id.toString(),
                        member.name,
                        member.email,
                        member.phoneNumber,
                        member.companyName,
                        member.role
                    )
                }

                Pair(
                    listOf(
                        "Staff ID",
                        "Name",
                        "Email",
                        "Phone",
                        "Company",
                        "Role"
                    ),
                    rows
                )
            }
        }
    }

    fun createCsvBytes(
        headers: List<String>,
        rows: List<List<String>>
    ): ByteArray {
        val output = buildString {
            appendLine(headers.joinToString(",") { csvCell(it) })

            rows.forEach { row ->
                appendLine(row.joinToString(",") { csvCell(it) })
            }
        }

        // UTF-8 BOM improves compatibility with spreadsheet applications.
        return ("\uFEFF$output").toByteArray(Charsets.UTF_8)
    }

    fun createPdfBytes(
        title: String,
        headers: List<String>,
        rows: List<List<String>>
    ): ByteArray {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val lineHeight = 18f
        val rowsPerPage = 35

        val titlePaint = AndroidPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = 17f
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }

        val normalPaint = AndroidPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = 8f
        }

        val headerPaint = AndroidPaint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 8f
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }

        val pages = rows.chunked(rowsPerPage).ifEmpty {
            listOf(emptyList())
        }

        pages.forEachIndexed { pageIndex, pageRows ->
            val pageInfo = PdfDocument.PageInfo.Builder(
                pageWidth,
                pageHeight,
                pageIndex + 1
            ).create()

            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = 48f

            canvas.drawText(title, margin, y, titlePaint)
            y += 22f

            canvas.drawText(
                "Generated: ${
                    SimpleDateFormat(
                        "dd MMM yyyy HH:mm",
                        Locale.getDefault()
                    ).format(Date())
                }",
                margin,
                y,
                normalPaint
            )

            y += 25f

            canvas.drawText(
                headers.joinToString(" | ").take(115),
                margin,
                y,
                headerPaint
            )

            y += 16f

            canvas.drawLine(
                margin,
                y,
                pageWidth - margin,
                y,
                normalPaint
            )

            y += 15f

            pageRows.forEach { row ->
                val line = row.joinToString(" | ")
                    .replace("\n", " ")
                    .take(125)

                canvas.drawText(line, margin, y, normalPaint)
                y += lineHeight
            }

            canvas.drawText(
                "Page ${pageIndex + 1} of ${pages.size}",
                margin,
                pageHeight - 25f,
                normalPaint
            )

            document.finishPage(page)
        }

        val output = ByteArrayOutputStream()
        document.writeTo(output)
        document.close()

        return output.toByteArray()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            "REPORTS & ANALYTICS EXPORT HUB",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Generate and save PDF or CSV reports using records available in the application.",
            style = MaterialTheme.typography.bodySmall,
            color = SashioTextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "SELECT REPORT CATEGORY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                listOf(
                    "Revenue Report",
                    "Booking Report",
                    "Client Directory Log",
                    "Staff Operations Matrix"
                ).forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                exportReportCategory = category
                            }
                            .background(
                                if (exportReportCategory == category) {
                                    SashioGold.copy(alpha = 0.1f)
                                } else {
                                    Color.Transparent
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = exportReportCategory == category,
                            onClick = {
                                exportReportCategory = category
                            }
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            category,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "EXPORT FILE FORMAT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("PDF", "CSV").forEach { format ->
                        FilterChip(
                            selected = exportFormatType == format,
                            onClick = {
                                exportFormatType = format
                            },
                            label = { Text(format) }
                        )
                    }
                }

                if (exportProgressPercent > 0f) {
                    LinearProgressIndicator(
                        progress = { exportProgressPercent },
                        modifier = Modifier.fillMaxWidth(),
                        color = SashioGold
                    )
                }

                Button(
                    enabled = exportProgressPercent == 0f,
                    onClick = {
                        try {
                            val (headers, rows) = createReportRows()

                            pendingExportBytes = if (
                                exportFormatType == "PDF"
                            ) {
                                createPdfBytes(
                                    exportReportCategory,
                                    headers,
                                    rows
                                )
                            } else {
                                createCsvBytes(headers, rows)
                            }

                            exportProgressPercent = 0.5f

                            val filename = exportReportCategory
                                .lowercase(Locale.ROOT)
                                .replace(" ", "_") +
                                "_${System.currentTimeMillis()}"

                            if (exportFormatType == "PDF") {
                                pdfLauncher.launch("$filename.pdf")
                            } else {
                                csvLauncher.launch("$filename.csv")
                            }
                        } catch (e: Exception) {
                            pendingExportBytes = null
                            exportProgressPercent = 0f

                            Toast.makeText(
                                localContext,
                                "Could not create report: ${e.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SashioSlate
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        "GENERATE & SAVE REPORT",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
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
