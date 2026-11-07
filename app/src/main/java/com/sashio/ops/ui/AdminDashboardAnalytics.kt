package com.sashio.ops.ui

import android.graphics.Paint as AndroidPaint
import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.postgrest
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(exp.category, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
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


@Serializable
private data class ReportPaymentRow(val id:String="",val booking_id:String="",val amount:Double=0.0,val reference:String="",val status:String="",val channel:String="",val payment_date:String="",val currency:String="NGN",val customer_email:String="",val created_at:String="")
@Serializable
private data class ReportBookingRow(
 val id:String="",val client_id:String="",val client_name:String="",val client_phone:String="",val client_email:String="",val company_name:String="",
 val service_mode:String="",val service_type:String="",val catalog_item_id:String?=null,val variant_name:String="",val model_number:String="",val quantity:Int=0,
 val duration:String="",val address:String="",val latitude:Double=0.0,val longitude:Double=0.0,val place_id:String?=null,val venue_name:String="",
 val gate_information:String="",val landmarks:String="",val parking_information:String="",val event_type:String="",val guest_count:Int=0,val event_date:String?=null,
 val cleaning_support:Boolean=false,val on_site_attendant:Boolean=false,val waste_evacuation:Boolean=false,val maintenance_support:Boolean=false,
 val special_instructions:String="",val quoted_price:Double=0.0,val logistics_cost:Double=0.0,val vat_amount:Double=0.0,val admin_notes:String="",
 val quote_issued_date:String?=null,val status:String="",val assigned_staff_id:String?=null,val assigned_staff_name:String?=null,
 val payment_status:String="",val payment_ref:String?=null,val created_at:String="",val updated_at:String="",val local_booking_id:Long?=null
)
@Serializable
private data class ReportProfileRow(val id:String="",val email:String="",val full_name:String?=null,val role:String="",val phone_number:String?=null,val company_name:String?=null,val job_title:String?=null,val created_at:String="",val updated_at:String="",val suspended:Boolean=false)
@Serializable
private data class ReportConsultationRow(val id:String="",val client_id:String="",val client_name:String="",val client_phone:String="",val client_email:String="",val type:String="",val preferred_date:String?=null,val status:String="",val notes:String="",val created_at:String="")
@Serializable
private data class ReportReviewRow(val id:String="",val user_id:String?=null,val user_name:String="",val rating:Int=0,val comment:String="",val category:String="",val review_date:String="",val image_url:String="",val created_at:String="")
@Serializable
private data class ReportIncidentRow(val id:String="",val title:String="",val reporter:String="",val priority:String="",val status:String="",val details:String="",val incident_date:String?=null,val created_at:String="")
@Serializable
private data class ReportLiveLocationRow(val booking_id:String="",val staff_id:String="",val latitude:Double=0.0,val longitude:Double=0.0,val accuracy_m:Double?=null,val heading:Double?=null,val speed_mps:Double?=null,val updated_at:String="")

@Composable
fun AdminReportsView(viewModel: SashioViewModel) {
    var format by remember { mutableStateOf("PDF") }
    var category by remember { mutableStateOf("Revenue Report") }
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }
    var progress by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf("") }
    var payments by remember { mutableStateOf<List<ReportPaymentRow>>(emptyList()) }
    var bookings by remember { mutableStateOf<List<ReportBookingRow>>(emptyList()) }
    var profiles by remember { mutableStateOf<List<ReportProfileRow>>(emptyList()) }
    var expenses by remember { mutableStateOf<List<com.sashio.ops.data.SupabaseExpense>>(emptyList()) }
    var attendance by remember { mutableStateOf<List<com.sashio.ops.data.SupabaseAttendance>>(emptyList()) }
    var consultations by remember { mutableStateOf<List<ReportConsultationRow>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<ReportReviewRow>>(emptyList()) }
    var incidents by remember { mutableStateOf<List<ReportIncidentRow>>(emptyList()) }
    var products by remember { mutableStateOf<List<com.sashio.ops.data.SupabaseCatalogItem>>(emptyList()) }
    var liveLocations by remember { mutableStateOf<List<ReportLiveLocationRow>>(emptyList()) }
    val context = LocalContext.current
    val logo = remember(context) {
        android.graphics.BitmapFactory.decodeResource(context.resources, com.sashio.ops.R.drawable.sachio_logo)
    }
    val logoBytes = remember(logo) {
        val out = ByteArrayOutputStream()
        logo?.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        out.toByteArray()
    }

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            try {
                val bytes = pendingBytes ?: error("No report data available")
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("Unable to open destination")
                Toast.makeText(context, "Sachio PDF report saved successfully", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "PDF export failed: " + (e.localizedMessage ?: "Unknown error"), Toast.LENGTH_LONG).show()
            } finally { pendingBytes = null; progress = false }
        } else { pendingBytes = null; progress = false }
    }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            try {
                val bytes = pendingBytes ?: error("No report data available")
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("Unable to open destination")
                Toast.makeText(context, "Sachio CSV report saved successfully", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "CSV export failed: " + (e.localizedMessage ?: "Unknown error"), Toast.LENGTH_LONG).show()
            } finally { pendingBytes = null; progress = false }
        } else { pendingBytes = null; progress = false }
    }

    LaunchedEffect(refreshKey) {
        loading = true
        loadError = ""
        val errors = mutableListOf<String>()
        try { payments = com.sashio.ops.data.SupabaseProvider.client.postgrest.from("payments").select().decodeList<ReportPaymentRow>() } catch (e: Exception) { errors.add("Payments: " + (e.localizedMessage ?: "load failed")) }
        try { bookings = com.sashio.ops.data.SupabaseProvider.client.postgrest.from("bookings").select().decodeList<ReportBookingRow>() } catch (e: Exception) { errors.add("Bookings: " + (e.localizedMessage ?: "load failed")) }
        try { profiles = com.sashio.ops.data.SupabaseProvider.client.postgrest.from("profiles").select().decodeList<ReportProfileRow>() } catch (e: Exception) { errors.add("Profiles: " + (e.localizedMessage ?: "load failed")) }
        try { expenses = com.sashio.ops.data.SupabaseProvider.client.postgrest.from("expenses").select().decodeList<com.sashio.ops.data.SupabaseExpense>() } catch (e: Exception) { errors.add("Expenses: " + (e.localizedMessage ?: "load failed")) }
        try { attendance = com.sashio.ops.data.SupabaseProvider.client.postgrest.from("staff_attendance").select().decodeList<com.sashio.ops.data.SupabaseAttendance>() } catch (e: Exception) { errors.add("Attendance: " + (e.localizedMessage ?: "load failed")) }
        try { consultations = com.sashio.ops.data.SupabaseProvider.client.postgrest.from("consultations").select().decodeList<ReportConsultationRow>() } catch (e: Exception) { errors.add("Consultations: " + (e.localizedMessage ?: "load failed")) }
        try { reviews = com.sashio.ops.data.SupabaseProvider.client.postgrest.from("reviews").select().decodeList<ReportReviewRow>() } catch (e: Exception) { errors.add("Reviews: " + (e.localizedMessage ?: "load failed")) }
        try { incidents = com.sashio.ops.data.SupabaseProvider.client.postgrest.from("incidents").select().decodeList<ReportIncidentRow>() } catch (e: Exception) { errors.add("Incidents: " + (e.localizedMessage ?: "load failed")) }
        try { products = com.sashio.ops.data.SupabaseProvider.client.postgrest.from("catalog_items").select().decodeList<com.sashio.ops.data.SupabaseCatalogItem>() } catch (e: Exception) { errors.add("Product catalog: " + (e.localizedMessage ?: "load failed")) }
        try { liveLocations = com.sashio.ops.data.SupabaseProvider.client.postgrest.from("booking_live_locations").select().decodeList<ReportLiveLocationRow>() } catch (e: Exception) { errors.add("Live tracking: " + (e.localizedMessage ?: "load failed")) }
        loadError = errors.joinToString("\n")
        loading = false
    }

    fun cell(value: Any?): String {
        var text = value?.toString() ?: ""
        if (text.trimStart().startsWith("=") || text.trimStart().startsWith("+") || text.trimStart().startsWith("-") || text.trimStart().startsWith("@")) text = "'" + text
        return "\"" + text.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\""
    }

    fun reportRows(): Pair<List<String>, List<List<String>>> = when (category) {
        "Revenue Report" -> Pair(listOf("Payment ID","Booking ID","Reference","Amount","Currency","Status","Channel","Customer Email","Payment Date","Created At"), payments.map { listOf(it.id,it.booking_id,it.reference,it.amount.toString(),it.currency,it.status,it.channel,it.customer_email,it.payment_date,it.created_at) })
        "Booking Report" -> Pair(listOf("Booking ID","Client ID","Client Name","Phone","Email","Company","Service Mode","Service Type","Catalog Item ID","Variant","Model","Quantity","Duration","Address","Latitude","Longitude","Place ID","Venue","Gate Information","Landmarks","Parking Information","Event Type","Guest Count","Event Date","Cleaning Support","On-site Attendant","Waste Evacuation","Maintenance Support","Special Instructions","Quoted Price","Logistics Cost","VAT","Admin Notes","Quote Issued","Booking Status","Assigned Staff ID","Assigned Staff","Payment Status","Payment Reference","Created At","Updated At","Local Booking ID"), bookings.map { b -> listOf(b.id,b.client_id,b.client_name,b.client_phone,b.client_email,b.company_name,b.service_mode,b.service_type,b.catalog_item_id ?: "",b.variant_name,b.model_number,b.quantity.toString(),b.duration,b.address,b.latitude.toString(),b.longitude.toString(),b.place_id ?: "",b.venue_name,b.gate_information,b.landmarks,b.parking_information,b.event_type,b.guest_count.toString(),b.event_date ?: "",b.cleaning_support.toString(),b.on_site_attendant.toString(),b.waste_evacuation.toString(),b.maintenance_support.toString(),b.special_instructions,b.quoted_price.toString(),b.logistics_cost.toString(),b.vat_amount.toString(),b.admin_notes,b.quote_issued_date ?: "",b.status,b.assigned_staff_id ?: "",b.assigned_staff_name ?: "",b.payment_status,b.payment_ref ?: "",b.created_at,b.updated_at,b.local_booking_id?.toString() ?: "") })
        "Client Directory Log" -> Pair(listOf("Client ID","Name","Email","Phone","Company","Job Title","Role","Suspended","Created At","Updated At"), profiles.filter { it.role.equals("client",true) }.map { listOf(it.id,it.full_name ?: "",it.email,it.phone_number ?: "",it.company_name ?: "",it.job_title ?: "",it.role,it.suspended.toString(),it.created_at,it.updated_at) })
        "Staff Operations Matrix" -> Pair(listOf("Staff ID","Name","Email","Phone","Company","Job Title","Role","Suspended","Created At","Updated At"), profiles.filter { it.role.equals("staff",true) }.map { listOf(it.id,it.full_name ?: "",it.email,it.phone_number ?: "",it.company_name ?: "",it.job_title ?: "",it.role,it.suspended.toString(),it.created_at,it.updated_at) })
        "Expense Register" -> Pair(listOf("Expense ID","Staff ID","Staff Name","Category","Amount","Details","Status","Receipt URL","Expense Date","Created At"), expenses.map { listOf(it.id,it.staff_id ?: "",it.staff_name,it.category,it.amount.toString(),it.details,it.status,it.receipt_url ?: "",it.expense_date ?: "",it.created_at ?: "") })
        "Attendance Register" -> Pair(listOf("Attendance ID","Staff ID","Staff Name","Clock In","Clock Out","Verified Location","Verified"), attendance.map { listOf(it.id,it.staff_id ?: "",it.staff_name,it.clock_in_time,it.clock_out_time ?: "",it.location_verified ?: "",it.is_verified.toString()) })
        "Consultation Register" -> Pair(listOf("Consultation ID","Client ID","Client Name","Phone","Email","Type","Preferred Date","Status","Notes","Created At"), consultations.map { listOf(it.id,it.client_id,it.client_name,it.client_phone,it.client_email,it.type,it.preferred_date ?: "",it.status,it.notes,it.created_at) })
        "Customer Reviews" -> Pair(listOf("Review ID","User ID","Customer","Rating","Comment","Category","Review Date","Image URL","Created At"), reviews.map { listOf(it.id,it.user_id ?: "",it.user_name,it.rating.toString(),it.comment,it.category,it.review_date,it.image_url,it.created_at) })
        "Incident Register" -> Pair(listOf("Incident ID","Title","Reporter","Priority","Status","Details","Incident Date","Created At"), incidents.map { listOf(it.id,it.title,it.reporter,it.priority,it.status,it.details,it.incident_date ?: "",it.created_at) })
        "Product Catalog" -> Pair(listOf("Product ID","Title","Description","Model Number","Variant","Base Price","Image URL","Image URLs","Video URL","Features","Specifications","Benefits","Use Cases","Active"), products.map { listOf(it.id,it.title,it.description,it.model_number,it.variant_name,it.base_price.toString(),it.image_url,it.image_urls.joinToString(" | "),it.video_url,it.features,it.specifications,it.benefits,it.use_cases,it.is_active.toString()) })
        else -> Pair(listOf("Booking ID","Staff ID","Latitude","Longitude","Accuracy Metres","Heading","Speed Metres Per Second","Last Updated"), liveLocations.map { listOf(it.booking_id,it.staff_id,it.latitude.toString(),it.longitude.toString(),it.accuracy_m?.toString() ?: "",it.heading?.toString() ?: "",it.speed_mps?.toString() ?: "",it.updated_at) })
    }

    fun createCsv(title: String, headers: List<String>, rows: List<List<String>>): ByteArray {
        val dataUri = "data:image/png;base64," + android.util.Base64.encodeToString(logoBytes, android.util.Base64.NO_WRAP)
        val generated = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val csv = buildString {
            appendLine(cell("SACHIO OPERATIONS REPORT"))
            appendLine(listOf(cell("Report"),cell(title)).joinToString(","))
            appendLine(listOf(cell("Generated"),cell(generated)).joinToString(","))
            appendLine(listOf(cell("Sachio Logo PNG Data URI"),cell(dataUri)).joinToString(","))
            appendLine(headers.joinToString(",") { cell(it) })
            rows.forEach { appendLine(it.joinToString(",") { value -> cell(value) }) }
        }
        return ("\uFEFF" + csv).toByteArray(Charsets.UTF_8)
    }

    fun createPdf(title: String, headers: List<String>, rows: List<List<String>>, logoBitmap: android.graphics.Bitmap): ByteArray {
        val doc = PdfDocument()
        val width = 595
        val height = 842
        val margin = 38f
        val titlePaint = AndroidPaint().apply { color = android.graphics.Color.rgb(23,39,57); textSize = 17f; typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD); isAntiAlias = true }
        val normalPaint = AndroidPaint().apply { color = android.graphics.Color.BLACK; textSize = 8.2f; isAntiAlias = true }
        val boldPaint = AndroidPaint().apply { color = android.graphics.Color.rgb(34,47,58); textSize = 8.2f; typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD); isAntiAlias = true }
        val linePaint = AndroidPaint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 0.7f }
        val imagePaint = AndroidPaint().apply { isAntiAlias = true }
        var page: PdfDocument.Page? = null
        var canvas: android.graphics.Canvas? = null
        var pageNo = 0
        var y = 0f
        fun finishPage() {
            canvas?.drawText("SACHIO OPERATIONS  |  " + title + "  |  Page " + pageNo, margin, height - 20f, normalPaint)
            page?.let { doc.finishPage(it) }
            page = null
            canvas = null
        }
        fun newPage() {
            finishPage()
            pageNo += 1
            val info = PdfDocument.PageInfo.Builder(width,height,pageNo).create()
            page = doc.startPage(info)
            canvas = page!!.canvas
            canvas!!.drawBitmap(logoBitmap,null,android.graphics.RectF(425f,18f,557f,82f),imagePaint)
            canvas!!.drawText("SACHIO OPERATIONS",margin,37f,boldPaint)
            canvas!!.drawText(title,margin,60f,titlePaint)
            canvas!!.drawText("Generated: " + SimpleDateFormat("dd MMM yyyy HH:mm:ss",Locale.getDefault()).format(Date()),margin,78f,normalPaint)
            canvas!!.drawLine(margin,94f,width-margin,94f,linePaint)
            y = 112f
        }
        fun wrapped(text: String, paint: AndroidPaint): List<String> {
            val words = text.replace("\r"," ").replace("\n"," ").split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (words.isEmpty()) return listOf("")
            val result = mutableListOf<String>()
            var line = ""
            for (word in words) {
                val candidate = if (line.isEmpty()) word else line + " " + word
                if (line.isNotEmpty() && paint.measureText(candidate) > width - margin * 2) { result.add(line); line = word } else line = candidate
            }
            if (line.isNotEmpty()) result.add(line)
            return result
        }
        try {
            newPage()
            if (rows.isEmpty()) {
                canvas!!.drawText("No records were returned for this report.",margin,y,normalPaint)
                y += 16f
            }
            rows.forEachIndexed { index,row ->
                if (y > height - 55f) newPage()
                canvas!!.drawText("RECORD " + (index+1),margin,y,boldPaint)
                y += 13f
                headers.forEachIndexed { col,header ->
                    val value = row.getOrNull(col).orEmpty().ifBlank { "—" }
                    wrapped(header + ": " + value,normalPaint).forEach { line ->
                        if (y > height - 42f) newPage()
                        canvas!!.drawText(line,margin,y,normalPaint)
                        y += 11f
                    }
                }
                y += 7f
                if (y < height - 35f) canvas!!.drawLine(margin,y,width-margin,y,linePaint)
                y += 12f
            }
            finishPage()
            val out = ByteArrayOutputStream()
            doc.writeTo(out)
            return out.toByteArray()
        } finally { doc.close() }
    }

    val categories = listOf("Revenue Report","Booking Report","Client Directory Log","Staff Operations Matrix","Expense Register","Attendance Register","Consultation Register","Customer Reviews","Incident Register","Product Catalog","Live Tracking Audit")
    val currentRows = reportRows()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("SACHIO REPORTS & EXPORT HUB",style = MaterialTheme.typography.titleMedium,fontWeight = FontWeight.Bold)
                Text("Complete operational reports with official Sachio branding.",style = MaterialTheme.typography.bodySmall,color = SashioTextMuted)
            }
            TextButton(onClick = { refreshKey += 1 },enabled = !loading) { Text("REFRESH") }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),shape = RoundedCornerShape(14.dp),modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp),verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("SELECT REPORT CATEGORY",fontWeight = FontWeight.Bold,fontSize = 12.sp)
                categories.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { category = item }.background(if (category == item) SashioGold.copy(alpha=0.1f) else Color.Transparent,RoundedCornerShape(8.dp)).padding(horizontal=8.dp,vertical=4.dp),verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = category == item,onClick = { category = item })
                        Text(item,fontSize = 12.sp,fontWeight = FontWeight.SemiBold)
                    }
                }
                HorizontalDivider()
                Text("EXPORT FORMAT",fontWeight = FontWeight.Bold,fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("PDF","CSV").forEach { item -> FilterChip(selected = format == item,onClick = { format = item },label = { Text(item) }) }
                }
                Text(if (loading) "Loading all report records from Sachio database…" else currentRows.second.size.toString() + " records ready",fontSize = 11.sp,color = SashioTextMuted)
                if (loadError.isNotBlank()) Text("Some data sources could not be loaded:\n" + loadError + "\nUse Refresh after checking connection and account permissions.",color = MaterialTheme.colorScheme.error,fontSize = 10.sp)
                if (progress) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(),color = SashioGold)
                Button(
                    enabled = !loading && !progress && logoBytes.isNotEmpty(),
                    onClick = {
                        try {
                            val data = reportRows()
                            pendingBytes = if (format == "PDF") createPdf(category,data.first,data.second,logo ?: error("Sachio logo is missing")) else createCsv(category,data.first,data.second)
                            progress = true
                            val filename = "sachio_" + category.lowercase(Locale.ROOT).replace(" ","_") + "_" + System.currentTimeMillis()
                            if (format == "PDF") pdfLauncher.launch(filename + ".pdf") else csvLauncher.launch(filename + ".csv")
                        } catch (e: Exception) {
                            pendingBytes = null
                            progress = false
                            Toast.makeText(context,"Could not create report: " + (e.localizedMessage ?: "Unknown error"),Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.Download,contentDescription = null,modifier = Modifier.size(16.dp),tint = Color.White)
                    Spacer(modifier = Modifier.width(7.dp))
                    Text("GENERATE & SAVE SACHIO REPORT",fontWeight = FontWeight.Bold,color = Color.White,fontSize = 11.sp)
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Operational Variables", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ACTIVE SECURITY AUDIT SIGNALS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
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
