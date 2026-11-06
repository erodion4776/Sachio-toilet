package com.sashio.ops.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.data.UserProfile
import com.sashio.ops.ui.theme.*

@Composable
fun AdminDashboard(viewModel: SashioViewModel, admin: UserProfile?) {
    var adminDarkMode by rememberSaveable { mutableStateOf(false) }
    val adminColors = if (adminDarkMode) darkColorScheme(
        primary = SashioGold, onPrimary = SashioSlate, background = Color(0xFF101820),
        surface = Color(0xFF1B2732), onBackground = Color(0xFFF3F5F7),
        onSurface = Color(0xFFF3F5F7), surfaceVariant = Color(0xFF293744),
        onSurfaceVariant = Color(0xFFD0D8E0), outline = Color(0xFF647482)
    ) else lightColorScheme(
        primary = SashioGoldDark, onPrimary = Color.White, background = Color(0xFFF6F7F9),
        surface = Color.White, onBackground = SashioSlate, onSurface = SashioSlate,
        surfaceVariant = Color(0xFFECEFF2), onSurfaceVariant = SashioTextMuted
    )
    var activeBottomTab by remember { mutableStateOf("home") }
    var homeSubScreen by remember { mutableStateOf("dashboard") }

    // Dialog Toggle States
    var isAddStaffOpen by remember { mutableStateOf(false) }
    var isLogIncidentOpen by remember { mutableStateOf(false) }
    var isLogExpenseOpen by remember { mutableStateOf(false) }
    var isCatalogManagerOpen by remember { mutableStateOf(false) }
    var quotationDialogId by remember { mutableStateOf<String?>(null) }

    // Sync live Supabase data on entering admin dashboard
    LaunchedEffect(Unit) {
        viewModel.loadSupabaseBookings()
        viewModel.loadSupabaseProfiles()
        viewModel.loadSupabaseCatalogItems()
        viewModel.loadGeneralChats()
    }

    MaterialTheme(colorScheme = adminColors) {
    Scaffold(
        containerColor = adminColors.background,
        topBar = {
            Column {
                DashboardHeader(
                    roleName = when (activeBottomTab) {
                        "home" -> if (homeSubScreen == "dashboard") "SYSADMIN COMMAND" else "ADMIN > ${homeSubScreen.uppercase()}"
                        else -> "ADMIN CENTER > ${activeBottomTab.uppercase()}"
                    },
                    user = admin,
                    onSettingsClick = { 
                        activeBottomTab = "home"
                        homeSubScreen = "settings"
                    },
                    onLogout = { viewModel.logout() }
                )
                Row(
                    modifier = Modifier.fillMaxWidth().background(adminColors.background).padding(horizontal = 12.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (adminDarkMode) "Dark mode" else "Light mode", color = adminColors.onBackground, fontSize = 10.sp)
                    IconButton(onClick = { adminDarkMode = !adminDarkMode }, modifier = Modifier.testTag("admin_theme_toggle")) {
                        Icon(
                            if (adminDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (adminDarkMode) "Switch to light mode" else "Switch to dark mode",
                            tint = SashioGold
                        )
                    }
                }
                if (activeBottomTab == "home" && homeSubScreen != "dashboard") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SashioSlate)
                            .clickable { homeSubScreen = "dashboard" }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SashioGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Return to Console Executive Dashboard Grid", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SashioSlate,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                listOf(
                    Triple("home", "Dashboard", Icons.Default.Dashboard),
                    Triple("operations", "Operations", Icons.Default.Work),
                    Triple("tracking", "Tracking", Icons.Default.Map),
                    Triple("messages", "Messages", Icons.Default.Chat),
                    Triple("profile", "Profile", Icons.Default.Person)
                ).forEach { (id, label, icon) ->
                    val selected = activeBottomTab == id
                    NavigationBarItem(
                        selected = selected,
                        onClick = { 
                            activeBottomTab = id
                            if (id != "home") {
                                homeSubScreen = "dashboard"
                            }
                            if (id == "home") {
                                viewModel.loadSupabaseBookings()
                                viewModel.loadSupabaseProfiles()
                            } else if (id == "messages") {
                                viewModel.loadGeneralChats()
                            }
                        },
                        icon = { 
                            Icon(
                                icon, 
                                contentDescription = label, 
                                tint = if (selected) SashioGold else Color.White.copy(alpha = 0.5f)
                            ) 
                        },
                        label = { 
                            Text(
                                label, 
                                color = if (selected) SashioGold else Color.White.copy(alpha = 0.5f), 
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.testTag("admin_bottom_tab_$id")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (activeBottomTab) {
                "home" -> AdminHomeRouteHandler(
                    viewModel = viewModel,
                    homeSubScreen = homeSubScreen,
                    onSubScreenChange = { homeSubScreen = it },
                    onOpenAddStaff = { isAddStaffOpen = true },
                    onOpenAddCatalog = { isCatalogManagerOpen = true },
                    onOpenLogExpense = { isLogExpenseOpen = true },
                    onLaunchQuotationEngine = { quotationDialogId = it }
                )
                "operations" -> AdminOperationsTab(
                    viewModel = viewModel,
                    onOpenIncidentLog = { isLogIncidentOpen = true }
                )
                "tracking" -> AdminTrackingTab(viewModel = viewModel)
                "messages" -> AdminMessagesTab(viewModel = viewModel)
                "profile" -> AdminProfileTab(viewModel = viewModel, admin = admin)
            }
        }

        // Overlay dialogue definitions
        if (isAddStaffOpen) {
            AdminAddStaffDialog(
                viewModel = viewModel,
                onDismiss = { isAddStaffOpen = false }
            )
        }

        if (isLogIncidentOpen) {
            AdminLogIncidentDialog(
                viewModel = viewModel,
                onDismiss = { isLogIncidentOpen = false }
            )
        }

        if (isLogExpenseOpen) {
            AdminLogExpenseDialog(
                viewModel = viewModel,
                onDismiss = { isLogExpenseOpen = false }
            )
        }

        if (isCatalogManagerOpen) {
            AdminAddCatalogDialog(
                viewModel = viewModel,
                onDismiss = { isCatalogManagerOpen = false }
            )
        }

        quotationDialogId?.let { bId ->
            AdminQuotationPricingEngineDialog(
                viewModel = viewModel,
                bookingId = bId,
                onDismiss = { quotationDialogId = null }
            )
        }
    }
    }
}
