package com.sashio.ops.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.AdminProfileRow
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*
import java.net.URLEncoder

@Composable
fun AdminHomeRouteHandler(
    viewModel: SashioViewModel,
    homeSubScreen: String,
    onSubScreenChange: (String) -> Unit,
    onOpenAddStaff: () -> Unit,
    onOpenAddCatalog: () -> Unit,
    onOpenLogExpense: () -> Unit,
    onLaunchQuotationEngine: (String) -> Unit
) {
    when (homeSubScreen) {
        "dashboard" -> AdminDashboardHomeView(viewModel, onSubScreenChange)
        "clients" -> AdminClientsCrmView(viewModel)
        "staff" -> AdminStaffView(viewModel, onOpenAddStaff)
        "manage_roles" -> ManageUserRolesPanel(viewModel)
        "quotations" -> AdminQuotationsView(viewModel, onLaunchQuotationEngine)
        "products" -> AdminProductsView(viewModel, onOpenAddCatalog)
        "payments" -> AdminPaymentsView(viewModel)
        "revenue" -> AdminRevenueView(viewModel, onOpenLogExpense)
        "reports" -> AdminReportsView(viewModel)
        "settings" -> AdminSettingsView(viewModel)
    }
}

@Composable
fun AdminDashboardHomeView(
    viewModel: SashioViewModel,
    onSubScreenChange: (String) -> Unit
) {
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val supabaseBookings by viewModel.supabaseBookings.collectAsStateWithLifecycle()
    val staffList by viewModel.staffProfiles.collectAsStateWithLifecycle()
    val realStaffProfiles by viewModel.realStaffProfiles.collectAsStateWithLifecycle()
    val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val supabaseProfiles by viewModel.supabaseProfiles.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("SACHIO CORPORATE COMMAND CENTER", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Real-time metadata, logistics control matrices, and commercial quotation compliance.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
        }

        item {
            val activeRentals = supabaseBookings.count { 
                it.status == "Booking Confirmed" || it.status == "SERVICE DELIVERED" || it.status == "IN TRANSIT" || it.status == "Staff Assigned" 
            } + bookings.count { it.status == "Booking Confirmed" || it.status == "SERVICE DELIVERED" || it.status == "IN TRANSIT" }

            val outstandingQuotes = supabaseBookings.count { 
                it.status == "Quotation Requested" || it.status == "Under Review" 
            }

            val totalRevenue = payments.filter { it.status == "success" }.sumOf { it.amount } + 
                supabaseBookings.filter { it.payment_status == "success" }.sumOf { it.quoted_price + it.logistics_cost + it.vat_amount }

            val outstandingInvoicesVal = supabaseBookings.filter { 
                it.status == "Quotation Generated" || it.status == "Client Accepted" || it.status == "Quotation Sent" 
            }.sumOf { it.quoted_price + it.logistics_cost + it.vat_amount }

            val totalClients = supabaseProfiles.count { it.role == "client" }.coerceAtLeast(profiles.count { it.role == "client" })
            val totalStaff = realStaffProfiles.size.coerceAtLeast(staffList.size)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = SashioSlate), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("SETTLED REVENUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SashioGold)
                            Text("₦%,.0f".format(totalRevenue), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Paystack Confirmed", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = SashioTealDark), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("OUTSTANDING BILLS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SashioGold)
                            Text("₦%,.0f".format(outstandingInvoicesVal), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Pending Settlement", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("$totalClients Total", "👤 Clients", SashioBlue),
                        Triple("$activeRentals Live", "🚚 Bookings", SashioGreen),
                        Triple("$outstandingQuotes Open", "📄 Pending Quotes", SashioGold),
                        Triple("$totalStaff Crew", "👷 Active Staff", SashioSlate)
                    ).forEach { (count, title, color) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
                                Text(count, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("EXECUTIVE COMMAND GRID", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            val menuItems = listOf(
                Triple("clients", "👥 Clients Management", "Manage profiles, customer invoices, history"),
                Triple("staff", "👷 Staff & Field Crews", "Performance trackers, add members, dispatch logs"),
                Triple("quotations", "📄 Quotations Console", "Automated smart price calculations & metrics"),
                Triple("products", "🚽 Product Catalog", "Configure mobile units, specifications & images"),
                Triple("payments", "💳 Paystack Payments", "Settlement journals, transaction verification"),
                Triple("revenue", "📈 Business Analytics", "Revenue growth charts with expense reports"),
                Triple("reports", "📊 Reports Export Hub", "Sachio-branded PDF and CSV for all reports"),
                Triple("manage_roles", "🔑 User Roles (Live)", "Promote or demote live Supabase accounts"),
                Triple("settings", "⚙ Operational Settings", "System configurations, activity audits logs")
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in menuItems.indices step 2) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (j in 0..1) {
                            if (i + j < menuItems.size) {
                                val item = menuItems[i + j]
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSubScreenChange(item.first) }
                                        .testTag("admin_grid_${item.first}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(item.second, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(item.third, fontSize = 9.sp, color = SashioTextMuted, lineHeight = 12.sp)
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminClientsCrmView(viewModel: SashioViewModel) {
    val supabaseProfiles by viewModel.supabaseProfiles.collectAsStateWithLifecycle()
    var clientSearchQuery by remember { mutableStateOf("") }
    var clientCategoryFilter by remember { mutableStateOf("All") }
    val localContext = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSupabaseProfiles()
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("CUSTOMER RELATIONSHIP MANAGEMENT (CRM) - LIVE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = clientSearchQuery,
            onValueChange = { clientSearchQuery = it },
            label = { Text("Search client by name or company...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Corporate", "Individual").forEach { cat ->
                FilterChip(
                    selected = clientCategoryFilter == cat,
                    onClick = { clientCategoryFilter = cat },
                    label = { Text(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val filteredClients = supabaseProfiles.filter {
            it.role == "client" &&
            ((it.full_name ?: "").contains(clientSearchQuery, ignoreCase = true) || (it.company_name ?: "").contains(clientSearchQuery, ignoreCase = true) || it.email.contains(clientSearchQuery, ignoreCase = true)) &&
            (clientCategoryFilter == "All" ||
             (clientCategoryFilter == "Corporate" && !it.company_name.isNullOrBlank()) ||
             (clientCategoryFilter == "Individual" && it.company_name.isNullOrBlank()))
        }

        if (filteredClients.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Client Profiles found matching constraints", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredClients) { c ->
                    val isSuspended = c.suspended
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isSuspended) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(c.full_name?.takeIf { it.isNotBlank() } ?: c.email, fontWeight = FontWeight.Bold)
                                    Text(if (!c.company_name.isNullOrBlank()) "Corporate: ${c.company_name}" else "Individual Account", fontSize = 10.sp, color = SashioGold, fontWeight = FontWeight.Bold)
                                }
                                Text(if (isSuspended) "SUSPENDED" else "ACTIVE", color = if (isSuspended) SashioRed else SashioGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Text("Email: ${c.email} | Phone: ${c.phone_number ?: "N/A"}", fontSize = 10.sp, color = SashioTextMuted)

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.adminSetProfileSuspended(c.id, !isSuspended) },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isSuspended) SashioGreen else SashioRed),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(if (isSuspended) "Activate Profile" else "Suspend Profile", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Button(
                                    onClick = {
                                        val whatsappText = "Hello ${c.full_name ?: "Valued Client"}, this is SACHIO Customer Service regarding your event sanitation arrangements."
                                        val encodedText = URLEncoder.encode(whatsappText, "UTF-8")
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${c.phone_number ?: ""}&text=$encodedText"))
                                        localContext.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("WhatsApp Direct", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
fun AdminStaffView(
    viewModel: SashioViewModel,
    onOpenAddStaff: () -> Unit
) {
    val realStaffProfiles by viewModel.realStaffProfiles.collectAsStateWithLifecycle()
    val supabaseBookings by viewModel.supabaseBookings.collectAsStateWithLifecycle()
    var staffSearchQuery by remember { mutableStateOf("") }
    val localContext = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSupabaseProfiles()
        viewModel.loadSupabaseBookings()
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("STAFF & DISPATCH CREWS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = onOpenAddStaff,
                colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Register Staff", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        OutlinedTextField(
            value = staffSearchQuery,
            onValueChange = { staffSearchQuery = it },
            label = { Text("Search crews by name or email...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        val filteredStaff = realStaffProfiles.filter {
            (it.full_name ?: "").contains(staffSearchQuery, ignoreCase = true) || it.email.contains(staffSearchQuery, ignoreCase = true)
        }

        if (filteredStaff.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No staff accounts found. Promote users in 'User Roles'.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredStaff) { s ->
                    val isSuspended = s.suspended
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isSuspended) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(s.full_name?.takeIf { it.isNotBlank() } ?: s.email, fontWeight = FontWeight.Bold)
                                    Text("ROLE: FIELD OPERATIVE", fontSize = 9.sp, color = SashioGold, fontWeight = FontWeight.Bold)
                                }
                                Text(if (isSuspended) "SUSPENDED" else "ACTIVE", color = if (isSuspended) SashioRed else SashioGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Phone: ${s.phone_number ?: "N/A"} | Email: ${s.email}", fontSize = 10.sp, color = SashioTextMuted)

                            val countJob = supabaseBookings.count { it.assigned_staff_id == s.id }
                            Text("Active Dispatches: $countJob", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.adminSetProfileSuspended(s.id, !isSuspended) },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isSuspended) SashioGreen else SashioRed),
                                    modifier = Modifier.weight(1f).height(30.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(if (isSuspended) "Activate Crew" else "Suspend Crew", fontSize = 9.sp, color = Color.White)
                                }
                                Button(
                                    onClick = {
                                        Toast.makeText(localContext, "Ping alert dispatched to ${s.full_name ?: s.email}'s device.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                    modifier = Modifier.weight(1f).height(30.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Ping Device", fontSize = 9.sp, color = Color.White)
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
fun ManageUserRolesPanel(viewModel: SashioViewModel) {
    val profiles by viewModel.supabaseProfiles.collectAsStateWithLifecycle()
    val error by viewModel.roleManagementError.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var profilePendingDelete by remember { mutableStateOf<AdminProfileRow?>(null) }
    val localContext = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSupabaseProfiles()
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("LIVE USER ROLES (SUPABASE)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Promote or demote real Supabase accounts. Changes take effect on next login.",
            style = MaterialTheme.typography.bodySmall,
            color = SashioTextMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by name or email...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("manage_roles_search")
        )

        error?.let { err ->
            Text(err, color = SashioRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        }

        val filtered = profiles.filter {
            (it.full_name ?: "").contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { p ->
                var roleMenuOpen by remember(p.id) { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.full_name?.takeIf { it.isNotBlank() } ?: p.email, fontWeight = FontWeight.Bold)
                                Text(p.email, fontSize = 10.sp, color = SashioTextMuted)
                            }
                            Text(
                                p.role.uppercase(),
                                color = when (p.role) {
                                    "admin" -> SashioRed
                                    "staff" -> SashioGold
                                    else -> SashioGreen
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { roleMenuOpen = true },
                                    modifier = Modifier.fillMaxWidth().height(36.dp).testTag("manage_role_trigger_${p.id}")
                                ) {
                                    Text("Change Role (${p.role.uppercase()})", fontSize = 11.sp)
                                }
                                DropdownMenu(
                                    expanded = roleMenuOpen,
                                    onDismissRequest = { roleMenuOpen = false }
                                ) {
                                    listOf("client", "staff", "admin").forEach { roleOption ->
                                        DropdownMenuItem(
                                            text = { Text(roleOption.uppercase()) },
                                            onClick = {
                                                roleMenuOpen = false
                                                if (roleOption != p.role) {
                                                    viewModel.adminSetUserRole(p.id, roleOption)
                                                    Toast.makeText(localContext, "Updated ${p.email} to $roleOption", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            val isSelf = currentUser?.email?.equals(p.email, ignoreCase = true) == true
                            OutlinedButton(
                                onClick = { profilePendingDelete = p },
                                enabled = !isSelf,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SashioRed),
                                border = BorderStroke(1.dp, if (isSelf) Color.LightGray else SashioRed),
                                modifier = Modifier.height(36.dp).testTag("delete_profile_${p.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete user", tint = if (isSelf) Color.LightGray else SashioRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        profilePendingDelete?.let { toDelete ->
            AlertDialog(
                onDismissRequest = { profilePendingDelete = null },
                title = { Text("Delete User Profile?") },
                text = {
                    Text(
                        "This permanently removes ${toDelete.full_name?.takeIf { it.isNotBlank() } ?: toDelete.email} " +
                        "(${toDelete.email}) from the app database. This cannot be undone."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.adminDeleteUserProfile(toDelete.id)
                            profilePendingDelete = null
                            Toast.makeText(localContext, "User profile deleted.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioRed)
                    ) { Text("Delete", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { profilePendingDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}
