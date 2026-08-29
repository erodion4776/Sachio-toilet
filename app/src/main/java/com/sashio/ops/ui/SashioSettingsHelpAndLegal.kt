package com.sashio.ops.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.theme.*

@Composable
fun SupportSettingsSubScreen(
    viewModel: SashioViewModel,
    currentAccent: Color,
    themePref: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val ticketHistory by viewModel.ticketHistory.collectAsStateWithLifecycle()

    var faqQuery by remember { mutableStateOf("") }
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }

    var ticketSubject by remember { mutableStateOf("") }
    var ticketCategory by remember { mutableStateOf("Enquiry") }
    var ticketDesc by remember { mutableStateOf("") }

    val availableFaqs = listOf(
        Triple(
            "Why is there no fixed pricing listed on products?",
            "To guarantee accurate rates accounting for event duration, custom logistics surcharges, VAT, and location coordinates, SACHIO operates through bespoke real-time generated quotation agreements.",
            "pricing"
        ),
        Triple(
            "How is cleaning and sanitizing handled?",
            "Prestige attendants perform comprehensive quad sewage evacuations using active biological biocide digests that completely sanitize and eliminate odors and microbial compounds.",
            "sanitize"
        ),
        Triple(
            "Can I request on-site standby crew support?",
            "Yes. Select the 'On-Site Attendant' inclusion when submitting your quotation request and our uniformed personnel will be deployed to your venue.",
            "standby"
        ),
        Triple(
            "What are the VIP event toilet guidelines?",
            "Select 'VIP event Toilet variant packages' to access luxury cabins equipped with vanity mirrors, climate control, and dedicated wash hand basins.",
            "vip"
        )
    )

    val filteredFaqs = availableFaqs.filter {
        faqQuery.isBlank() || it.first.contains(faqQuery, ignoreCase = true) || it.second.contains(faqQuery, ignoreCase = true)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("DIRECT PRESTIGE HOTLINES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
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
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+234800SACHIO"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Phone Support", fontSize = 10.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@sachioltd.com"))
                            context.startActivity(Intent.createChooser(intent, "Send email to Sachio support"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Email Support", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }

        item {
            Text("SEARCH KNOWLEDGE BASE FAQS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = faqQuery,
                onValueChange = { faqQuery = it },
                label = { Text("Filter FAQs (e.g. price, sanitize)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredFaqs.forEachIndexed { idx, (q, ans, _) ->
                    val isExpanded = expandedFaqIndex == idx
                    Card(
                        onClick = { expandedFaqIndex = if (isExpanded) null else idx },
                        colors = CardDefaults.cardColors(
                            containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.02f) else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    q,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (themePref == "dark") Color.White else SashioSlate,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = currentAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(ans, fontSize = 10.sp, color = SashioTextMuted)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("SUBMIT CONSULTATION TICKET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.03f) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ticketSubject,
                        onValueChange = { ticketSubject = it },
                        label = { Text("Ticket Subject Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Consultation Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Enquiry", "Billing", "Logistics").forEach { ct ->
                            val isC = ticketCategory == ct
                            Button(
                                onClick = { ticketCategory = ct },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isC) currentAccent else Color.LightGray.copy(alpha = 0.2f),
                                    contentColor = if (isC) Color.White else if (themePref == "dark") Color.White else SashioSlate
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1f).height(32.dp)
                            ) {
                                Text(ct, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = ticketDesc,
                        onValueChange = { ticketDesc = it },
                        label = { Text("Description of issue or inquiry") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (ticketSubject.isBlank() || ticketDesc.isBlank()) {
                                Toast.makeText(context, "ERROR: Subject and description are mandatory.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.submitSupportTicket(ticketSubject, ticketCategory, ticketDesc)
                                Toast.makeText(context, "SUCCESS: Support ticket filed successfully.", Toast.LENGTH_SHORT).show()
                                ticketSubject = ""
                                ticketDesc = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("TRANSMIT SUPPORT TICKET", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (ticketHistory.isNotEmpty()) {
            item {
                Text("YOUR ACTIVE SUBMITTED TICKETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioTextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ticketHistory.forEach { t ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.05f) else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Ref Code: ${t.id}", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = currentAccent)
                                    Text(text = t.subject, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                                    Text(text = "Category: ${t.category} | ${t.date}", fontSize = 9.sp, color = SashioTextMuted)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SashioGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("ACTIVE OPEN", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SashioGreen)
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
fun TermsSettingsSubScreen(
    viewModel: SashioViewModel,
    currentAccent: Color,
    themePref: String,
    onBack: () -> Unit
) {
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SACHIO STATUTORY POLICY AGREEMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)

                    Text(
                        text = "1. LICENSE AND SCOPE OF TERMS\nThis platform serves as a secure business liaison client connecting event organizers and corporate entities to the specialized mobile sanitation network of SACHIO Ltd. The pricing calculations and fuel indices remain proprietary estimates until formal agreement execution.\n\n2. PRIVACY & COMPLIANCE STATUTES\nWe strictly adhere to Lagos State Environmental Protection Agency (LASEPA) protocols and statutory data protection frameworks. Your location coordinates are accessed only for routing and delivery tracking.\n\n3. LOGISTICS DISRUPTIONS CLAUSE\nOperational delays originating from heavy traffic congestion, civil road maintenance, and fuel logistics variables are mitigated through dynamic route re-centering. Formal status updates are broadcast in real-time.",
                        fontSize = 11.sp,
                        color = SashioTextMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (themePref == "dark") Color.White.copy(alpha = 0.05f) else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("APPLICATION ARCHITECTURE SPECIFICATIONS:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                    Text("App Platform: SACHIO PRESTIGE MOBILE SEAMLESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (themePref == "dark") Color.White else SashioSlate)
                    Text("Version: Build 1.10.42-PRESTIGE-NATIVE", fontSize = 9.sp, color = SashioTextMuted)
                    Text("Engine Framework: Kotlin Compose Android SDK 34", fontSize = 9.sp, color = SashioTextMuted)
                    Text("Backend Stack: Supabase PostgreSQL + Room ORM", fontSize = 9.sp, color = SashioTextMuted)
                }
            }
        }
    }
}

@Composable
fun AccountSettingsSubScreen(
    viewModel: SashioViewModel,
    currentAccent: Color,
    themePref: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showDeactivateModal by remember { mutableStateOf(false) }
    var showDeleteModal by remember { mutableStateOf(false) }

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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TEMPORARY ACCOUNT SUSPENSION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = currentAccent)
                    Text(
                        "Pause your notifications, quotation schedules, and dispatch alerts temporarily. You can restore access instantly at any time simply by logging back in.",
                        fontSize = 11.sp,
                        color = SashioTextMuted
                    )

                    Button(
                        onClick = { showDeactivateModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Text("DEACTIVATE SACHIO PRESTIGE ACCOUNT", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SashioRed.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SashioRed.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("IRREVERSIBLE DATABASE DELETION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SashioRed)
                    Text(
                        "Purge your profile record, transaction invoices, billing records, favorite coordinates, and ticket histories permanently from the local cache. This action cannot be undone.",
                        fontSize = 11.sp,
                        color = SashioTextMuted
                    )

                    Button(
                        onClick = { showDeleteModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Text("PERMANENTLY EXPUNGE PROFILE", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeactivateModal) {
        AlertDialog(
            onDismissRequest = { showDeactivateModal = false },
            title = { Text("Confirm Deactivation?", fontWeight = FontWeight.Bold) },
            text = { Text("Your active dispatches and standby schedules will be safely archived.", fontSize = 11.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeactivateModal = false
                        viewModel.deactivateAccount()
                        onBack()
                        Toast.makeText(context, "Sashio Prestige profile deactivated.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate)
                ) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateModal = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteModal) {
        var deleteVerificationInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDeleteModal = false },
            title = { Text("CRITICAL WARNING: Purge Record?", fontWeight = FontWeight.Bold, color = SashioRed) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("To confirm absolute deletion, type the keyword \"DELETE\" in the field below:", fontSize = 11.sp)
                    OutlinedTextField(
                        value = deleteVerificationInput,
                        onValueChange = { deleteVerificationInput = it },
                        label = { Text("Verification Phrase") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteVerificationInput.trim() == "DELETE") {
                            showDeleteModal = false
                            viewModel.deleteAccount()
                            onBack()
                            Toast.makeText(context, "Sashio profile expunged successfully.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "ERROR: Incorrect authorization phrase.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SashioRed),
                    enabled = deleteVerificationInput.trim() == "DELETE"
                ) {
                    Text("Expunge Permanently", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteModal = false }) { Text("Abort Action") }
            }
        )
    }
}
