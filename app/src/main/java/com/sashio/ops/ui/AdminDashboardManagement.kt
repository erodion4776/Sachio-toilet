package com.sashio.ops.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.data.SupabaseBooking
import com.sashio.ops.data.SupabaseCatalogItem
import com.sashio.ops.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminQuotationsView(
    viewModel: SashioViewModel,
    onLaunchQuotationEngine: (String) -> Unit
) {
    val supabaseBookings by viewModel.supabaseBookings.collectAsStateWithLifecycle()
    val realStaffProfiles by viewModel.realStaffProfiles.collectAsStateWithLifecycle()
    var staffPickerForBookingId by remember { mutableStateOf<String?>(null) }
    val localContext = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSupabaseBookings()
        viewModel.loadSupabaseProfiles()
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("SMART PRICE QUOTATIONS CONTROL - LIVE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Review and process incoming requests. Generate and configure logistics surcharges, taxes, and specialized inclusions.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
            }
            IconButton(onClick = { viewModel.loadSupabaseBookings() }, modifier = Modifier.testTag("refresh_quotations")) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh quotations", tint = SashioSlate)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        var bookingPendingDelete by remember { mutableStateOf<SupabaseBooking?>(null) }

        bookingPendingDelete?.let { toDelete ->
            AlertDialog(
                onDismissRequest = { bookingPendingDelete = null },
                title = { Text("Delete this quotation?", fontWeight = FontWeight.Bold) },
                text = { Text("This permanently removes \"${toDelete.service_type}\" for ${toDelete.client_name}. This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.adminDeleteBooking(toDelete.id) {
                                bookingPendingDelete = null
                                Toast.makeText(localContext, "Quotation deleted.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioRed)
                    ) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { bookingPendingDelete = null }) { Text("Cancel") }
                }
            )
        }

        if (supabaseBookings.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No quotation requests in queue.", color = SashioTextMuted, fontSize = 12.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(supabaseBookings) { b ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(b.service_type, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                BookingStatusBadge(status = b.status)
                            }
                            Text("Client: ${b.client_name} | ${b.company_name.ifBlank { "Individual" }}", fontSize = 10.sp, color = SashioTextMuted)
                            Text("Specs: Event=${b.event_type} | Qty=${b.quantity} | Duration=${b.duration}", fontSize = 10.sp, color = SashioTextMuted)
                            Text("Location: ${b.address}", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = SashioTextMuted)
                            Text(
                                if (b.assigned_staff_name != null) "Crew: ${b.assigned_staff_name}" else "Crew: Unassigned",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (b.assigned_staff_name != null) SashioTeal else SashioGold
                            )

                            if (b.quoted_price > 0.0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Quoted Subtotal: ₦%,.0f | Logistics: ₦%,.0f | VAT: ₦%,.0f".format(b.quoted_price, b.logistics_cost, b.vat_amount),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SashioGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (b.status == "Quotation Requested" || b.status == "Under Review") {
                                    Button(
                                        onClick = { onLaunchQuotationEngine(b.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                                        modifier = Modifier.weight(1f).height(30.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Launch Engine", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(localContext, "Digital Quote PDF compiled and shared to client's email portal.", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                    modifier = Modifier.weight(1f).height(30.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Compile PDF", fontSize = 9.sp, color = Color.White)
                                }

                                Box {
                                    Button(
                                        onClick = {
                                            viewModel.loadSupabaseProfiles()
                                            staffPickerForBookingId = b.id
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (b.assigned_staff_name != null) SashioSlate else SashioGold
                                        ),
                                        modifier = Modifier.height(30.dp).testTag("assign_staff_${b.id}"),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PersonAdd,
                                            contentDescription = "Assign staff",
                                            tint = if (b.assigned_staff_name != null) Color.White else Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            if (b.assigned_staff_name != null) "Reassign" else "Assign Crew",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (b.assigned_staff_name != null) Color.White else Color.Black
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = staffPickerForBookingId == b.id,
                                        onDismissRequest = { staffPickerForBookingId = null }
                                    ) {
                                        if (realStaffProfiles.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("No staff registered yet. Assign role in 'User Roles' first.") },
                                                onClick = { staffPickerForBookingId = null },
                                                enabled = false
                                            )
                                        } else {
                                            realStaffProfiles.forEach { s ->
                                                DropdownMenuItem(
                                                    text = { Text(s.full_name?.takeIf { it.isNotBlank() } ?: s.email) },
                                                    onClick = {
                                                        val name = s.full_name?.takeIf { it.isNotBlank() } ?: s.email
                                                        viewModel.adminAssignStaffToSupabaseBooking(b.id, s.id, name) { success ->
                                                            if (success) {
                                                                Toast.makeText(localContext, "Assigned $name to job #QR-${b.id}!", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                        staffPickerForBookingId = null
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { bookingPendingDelete = b },
                                    modifier = Modifier.testTag("delete_quotation_${b.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete quotation", tint = SashioRed)
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
fun AdminQuotationPricingEngineDialog(
    viewModel: SashioViewModel,
    bookingId: String,
    onDismiss: () -> Unit
) {
    val supabaseBookingsForDialog by viewModel.supabaseBookings.collectAsStateWithLifecycle()
    val matchingBooking = supabaseBookingsForDialog.find { it.id == bookingId }
    val logisticsMetrics = matchingBooking?.let { viewModel.calculateLogisticsMetrics(it.latitude, it.longitude) }

    var subTotal by remember { mutableStateOf("120000") }
    var logSurcharge by remember(logisticsMetrics) {
        mutableStateOf(logisticsMetrics?.recommendedLogisticsCost?.toInt()?.toString() ?: "25000")
    }
    var adminNotesVal by remember { mutableStateOf("Premium trailer logistics. Secure anchor.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Smart Pricing Quotation Engine", fontWeight = FontWeight.Bold) },
        confirmButton = {
            Button(
                onClick = {
                    val rawSub = subTotal.toDoubleOrNull() ?: 120000.0
                    val rawLog = logSurcharge.toDoubleOrNull() ?: (logisticsMetrics?.recommendedLogisticsCost ?: 25000.0)
                    val rawVat = (rawSub + rawLog) * 0.075
                    viewModel.adminIssueQuotation(bookingId, rawSub, rawLog, rawVat, adminNotesVal) {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SashioGold)
            ) {
                Text("Issue Formal Quote", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Configure logistics, VAT surcharges, and subtotal pricing parameters:")

                if (logisticsMetrics != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SashioGold.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Automatic Logistics Surcharge Recommendation:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioSlate)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Zone: ${logisticsMetrics.logisticsZone}", fontSize = 9.sp, color = SashioTextMuted)
                            Text("Distance: %.2f km | Travel Time: ${logisticsMetrics.travelTimeMinutes} mins".format(logisticsMetrics.distanceKm), fontSize = 9.sp, color = SashioTextMuted)
                            Text("Estimated Fuel Factor: %.1f Liters Diesel".format(logisticsMetrics.estimatedFuelLitres), fontSize = 9.sp, color = SashioTextMuted)
                            Text("Recommended Fee: ₦%,.2f".format(logisticsMetrics.recommendedLogisticsCost), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioGreen)
                        }
                    }
                }

                OutlinedTextField(
                    value = subTotal,
                    onValueChange = { subTotal = it },
                    label = { Text("Subtotal Service Cost (₦)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = logSurcharge,
                    onValueChange = { logSurcharge = it },
                    label = { Text("Logistics & Transport Fee (₦)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = adminNotesVal,
                    onValueChange = { adminNotesVal = it },
                    label = { Text("Specific Terms & Signature Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                val quotationError by viewModel.roleManagementError.collectAsStateWithLifecycle()
                quotationError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(err, color = SashioRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminProductsView(
    viewModel: SashioViewModel,
    onOpenAddCatalog: () -> Unit
) {
    val supabaseCatalogItems by viewModel.supabaseCatalogItems.collectAsStateWithLifecycle()
    var editingCatalogItem by remember { mutableStateOf<SupabaseCatalogItem?>(null) }
    var catalogItemPendingDelete by remember { mutableStateOf<SupabaseCatalogItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSupabaseCatalogItems()
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("PRODUCT CATALOGS - LIVE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = onOpenAddCatalog,
                colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Unit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (supabaseCatalogItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No catalog items found in database.", color = SashioTextMuted, fontSize = 12.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(supabaseCatalogItems) { cat ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cat.title, fontWeight = FontWeight.Bold)
                                    Text("Variant: ${cat.variant_name} | Model: ${cat.model_number}", fontSize = 10.sp, color = SashioGoldDark, fontWeight = FontWeight.Bold)
                                }
                                Row {
                                    IconButton(onClick = { editingCatalogItem = cat }, modifier = Modifier.testTag("edit_catalog_${cat.id}")) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit product", tint = SashioSlate)
                                    }
                                    IconButton(onClick = { catalogItemPendingDelete = cat }, modifier = Modifier.testTag("delete_catalog_${cat.id}")) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete product", tint = SashioRed)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cat.description, fontSize = 10.sp, color = SashioTextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)

                            if (cat.base_price > 0.0) {
                                Text("Guide Price: ₦%,.0f".format(cat.base_price), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SashioSlate)
                            }
                            val galleryImages = cat.image_urls.ifEmpty { if (cat.image_url.isNotBlank()) listOf(cat.image_url) else emptyList() }
                            if (galleryImages.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                                    items(galleryImages) { img ->
                                        AsyncImage(
                                            model = img,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        catalogItemPendingDelete?.let { toDelete ->
            AlertDialog(
                onDismissRequest = { catalogItemPendingDelete = null },
                title = { Text("Delete Product?") },
                text = { Text("This permanently removes \"${toDelete.title}\" from the catalog. Clients will no longer see it. This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.adminDeleteSupabaseCatalogItem(toDelete.id)
                            catalogItemPendingDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioRed)
                    ) { Text("Delete", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { catalogItemPendingDelete = null }) { Text("Cancel") }
                }
            )
        }

        editingCatalogItem?.let { catItem ->
            var editTitle by remember(catItem.id) { mutableStateOf(catItem.title) }
            var editDesc by remember(catItem.id) { mutableStateOf(catItem.description) }
            var editModel by remember(catItem.id) { mutableStateOf(catItem.model_number) }
            var editVariant by remember(catItem.id) { mutableStateOf(catItem.variant_name) }
            var editPriceStr by remember(catItem.id) { mutableStateOf(if (catItem.base_price > 0.0) catItem.base_price.toInt().toString() else "") }
            var editImageUrl by remember(catItem.id) { mutableStateOf(catItem.image_url) }
            val editImageUrls = remember(catItem.id) {
                mutableStateListOf<String>().apply {
                    addAll(catItem.image_urls.ifEmpty { if (catItem.image_url.isNotBlank()) listOf(catItem.image_url) else emptyList() })
                }
            }
            var editVideoUrl by remember(catItem.id) { mutableStateOf(catItem.video_url) }
            var editFeatures by remember(catItem.id) { mutableStateOf(catItem.features) }
            var editSpecs by remember(catItem.id) { mutableStateOf(catItem.specifications) }
            var editBenefits by remember(catItem.id) { mutableStateOf(catItem.benefits) }
            var editUseCases by remember(catItem.id) { mutableStateOf(catItem.use_cases) }
            var editIsUploading by remember { mutableStateOf(false) }

            val editPhotoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickMultipleVisualMedia()
            ) { uris ->
                if (uris.isNotEmpty()) {
                    editIsUploading = true
                    var remaining = uris.size
                    uris.forEach { uri ->
                        viewModel.uploadCatalogImage(uri) { publicUrl ->
                            if (publicUrl != null) {
                                editImageUrls.add(publicUrl)
                                if (editImageUrl.isBlank()) editImageUrl = publicUrl
                            }
                            remaining -= 1
                            if (remaining <= 0) editIsUploading = false
                        }
                    }
                }
            }

            AlertDialog(
                onDismissRequest = { editingCatalogItem = null },
                title = { Text("Edit Catalog Item", fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        item { OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Product Title") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = editVariant, onValueChange = { editVariant = it }, label = { Text("Variant Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = editModel, onValueChange = { editModel = it }, label = { Text("Model Number") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = editDesc, onValueChange = { editDesc = it }, label = { Text("Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = editPriceStr, onValueChange = { editPriceStr = it }, label = { Text("Guide Price (Optional, ₦)") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }

                        item {
                            Button(
                                onClick = { editPhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                enabled = !editIsUploading,
                                colors = ButtonDefaults.buttonColors(containerColor = SashioTeal),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (editIsUploading) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Uploading...")
                                } else {
                                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (editImageUrls.isEmpty()) "Add Images" else "Add More Images")
                                }
                            }
                        }
                        if (editImageUrls.isNotEmpty()) {
                            item {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(editImageUrls.toList()) { img ->
                                        Box(modifier = Modifier.size(80.dp)) {
                                            AsyncImage(
                                                model = img,
                                                contentDescription = "Product photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                            )
                                            IconButton(
                                                onClick = {
                                                    editImageUrls.remove(img)
                                                    if (editImageUrl == img) editImageUrl = editImageUrls.firstOrNull() ?: ""
                                                },
                                                modifier = Modifier.align(Alignment.TopEnd).size(22.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove image", tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item { OutlinedTextField(value = editVideoUrl, onValueChange = { editVideoUrl = it }, label = { Text("Video URL / Link") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = editFeatures, onValueChange = { editFeatures = it }, label = { Text("Features") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = editSpecs, onValueChange = { editSpecs = it }, label = { Text("Specifications") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = editBenefits, onValueChange = { editBenefits = it }, label = { Text("Benefits") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = editUseCases, onValueChange = { editUseCases = it }, label = { Text("Use Cases") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.adminUpdateSupabaseCatalogItem(
                                id = catItem.id,
                                title = editTitle.ifBlank { "New Product" },
                                description = editDesc.ifBlank { "N/A" },
                                modelNumber = editModel.ifBlank { "N/A" },
                                variantName = editVariant.ifBlank { "N/A" },
                                basePrice = editPriceStr.toDoubleOrNull() ?: 0.0,
                                imageUrl = editImageUrls.firstOrNull() ?: editImageUrl,
                                imageUrls = editImageUrls.toList(),
                                videoUrl = editVideoUrl,
                                features = editFeatures,
                                specifications = editSpecs,
                                benefits = editBenefits,
                                useCases = editUseCases,
                                onComplete = { editingCatalogItem = null }
                            )
                        },
                        enabled = !editIsUploading,
                        colors = ButtonDefaults.buttonColors(containerColor = SashioGold)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingCatalogItem = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun AdminAddCatalogDialog(
    viewModel: SashioViewModel,
    onDismiss: () -> Unit
) {
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newModel by remember { mutableStateOf("") }
    var newVariant by remember { mutableStateOf("") }
    var newPriceStr by remember { mutableStateOf("") }
    var newImageUrl by remember { mutableStateOf("") }
    val newImageUrls = remember { mutableStateListOf<String>() }
    var newVideoUrl by remember { mutableStateOf("") }
    var newFeatures by remember { mutableStateOf("") }
    var newSpecs by remember { mutableStateOf("") }
    var newBenefits by remember { mutableStateOf("") }
    var newUseCases by remember { mutableStateOf("") }
    var newIsUploading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            newIsUploading = true
            var remaining = uris.size
            uris.forEach { uri ->
                viewModel.uploadCatalogImage(uri) { publicUrl ->
                    if (publicUrl != null) {
                        newImageUrls.add(publicUrl)
                        if (newImageUrl.isBlank()) newImageUrl = publicUrl
                    }
                    remaining -= 1
                    if (remaining <= 0) newIsUploading = false
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Catalog Item", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                item { OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Product Title") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = newVariant, onValueChange = { newVariant = it }, label = { Text("Variant Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = newModel, onValueChange = { newModel = it }, label = { Text("Model Number") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = newDesc, onValueChange = { newDesc = it }, label = { Text("Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = newPriceStr, onValueChange = { newPriceStr = it }, label = { Text("Guide Price (Optional, ₦)") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }

                item {
                    Button(
                        onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        enabled = !newIsUploading,
                        colors = ButtonDefaults.buttonColors(containerColor = SashioTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (newIsUploading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uploading...")
                        } else {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (newImageUrls.isEmpty()) "Add Images" else "Add More Images")
                        }
                    }
                }
                if (newImageUrls.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(newImageUrls.toList()) { img ->
                                Box(modifier = Modifier.size(80.dp)) {
                                    AsyncImage(
                                        model = img,
                                        contentDescription = "Product photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                    )
                                    IconButton(
                                        onClick = {
                                            newImageUrls.remove(img)
                                            if (newImageUrl == img) newImageUrl = newImageUrls.firstOrNull() ?: ""
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd).size(22.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove image", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item { OutlinedTextField(value = newVideoUrl, onValueChange = { newVideoUrl = it }, label = { Text("Video URL / Link") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = newFeatures, onValueChange = { newFeatures = it }, label = { Text("Features") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = newSpecs, onValueChange = { newSpecs = it }, label = { Text("Specifications") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = newBenefits, onValueChange = { newBenefits = it }, label = { Text("Benefits") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = newUseCases, onValueChange = { newUseCases = it }, label = { Text("Use Cases") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.adminAddSupabaseCatalogItem(
                        title = newTitle.ifBlank { "New Product" },
                        description = newDesc.ifBlank { "N/A" },
                        modelNumber = newModel.ifBlank { "N/A" },
                        variantName = newVariant.ifBlank { "N/A" },
                        basePrice = newPriceStr.toDoubleOrNull() ?: 0.0,
                        imageUrl = newImageUrls.firstOrNull() ?: newImageUrl,
                        imageUrls = newImageUrls.toList(),
                        videoUrl = newVideoUrl,
                        features = newFeatures,
                        specifications = newSpecs,
                        benefits = newBenefits,
                        useCases = newUseCases,
                        onComplete = { onDismiss() }
                    )
                },
                enabled = !newIsUploading,
                colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)
            ) {
                Text("Save Product", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminPaymentsView(viewModel: SashioViewModel) {
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("PAYSTACK SETTLEMENT JOURNAL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        if (payments.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No payment transactions recorded yet.", color = SashioTextMuted, fontSize = 12.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(payments) { p ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("REF: ${p.reference}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(p.paymentDate))
                                Text("Date: $dateStr | Channel: ${p.channel.uppercase()}", fontSize = 9.sp, color = SashioTextMuted)
                                Text("STATUS: ${p.status.uppercase()}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (p.status == "success") SashioGreen else SashioGold)
                            }
                            Text("₦%,.0f".format(p.amount), fontWeight = FontWeight.ExtraBold, color = SashioGreen, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
