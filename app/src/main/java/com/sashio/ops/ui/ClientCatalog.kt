package com.sashio.ops.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.sashio.ops.data.CatalogItem
import com.sashio.ops.ui.theme.*

@Composable
fun ProductDiscoveryTab(catalogItems: List<CatalogItem>, onRequestQuote: (String) -> Unit) {
    var selectedProduct by remember { mutableStateOf<CatalogItem?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("SACHIO ENTERPRISE CATALOG", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
        Text("Prestige in Every Flush. Explore premium, sanitary systems.", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)
        Spacer(modifier = Modifier.height(12.dp))

        if (catalogItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SashioGold, modifier = Modifier.size(32.dp))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                items(catalogItems) { cat ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().clickable { selectedProduct = cat }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SashioSlate, modifier = Modifier.weight(1f))
                                if (cat.variantName.isNotBlank()) {
                                    Card(colors = CardDefaults.cardColors(containerColor = SashioGold.copy(alpha = 0.15f)), shape = RoundedCornerShape(4.dp)) {
                                        Text(cat.variantName, color = SashioGoldDark, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            if (cat.videoUrl.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = "Video", tint = SashioRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Video Presentation Available", fontSize = 10.sp, color = SashioRed, fontWeight = FontWeight.Bold)
                                }
                            }

                            val displayImages = cat.imageUrls.ifEmpty { if (cat.imageUrl.isNotBlank()) listOf(cat.imageUrl) else emptyList() }
                            if (displayImages.isNotEmpty()) {
                                Box {
                                    AsyncImage(
                                        model = displayImages.first(),
                                        contentDescription = cat.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                    if (displayImages.size > 1) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
                                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${displayImages.size}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(cat.description, style = MaterialTheme.typography.bodySmall, color = SashioTextMuted, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Text("Tap to view Features, Specs, Use Cases & Details", fontSize = 10.sp, color = SashioTeal, fontStyle = FontStyle.Italic, modifier = Modifier.padding(top = 8.dp))

                            if (cat.basePrice > 0.0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Guide Price: ₦%,.2f".format(cat.basePrice), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SashioSlate)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onRequestQuote(cat.title) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SashioGoldDark),
                                    border = BorderStroke(1.dp, SashioGold),
                                    modifier = Modifier.weight(1.2f).height(36.dp)
                                ) {
                                    Text("Request Quotation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onRequestQuote(cat.title) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SashioSlate),
                                    modifier = Modifier.weight(0.8f).height(36.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Select Unit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedProduct?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { selectedProduct = null },
            onRequestQuote = {
                selectedProduct = null
                onRequestQuote(product.title)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductDetailDialog(product: CatalogItem, onDismiss: () -> Unit, onRequestQuote: () -> Unit) {
    val images = product.imageUrls.ifEmpty { if (product.imageUrl.isNotBlank()) listOf(product.imageUrl) else emptyList() }
    val pagerState = rememberPagerState(pageCount = { images.size })

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box {
                    if (images.isNotEmpty()) {
                        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(220.dp)) { page ->
                            AsyncImage(
                                model = images[page],
                                contentDescription = "Product photo ${page + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        if (images.size > 1) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)
                            ) {
                                images.indices.forEach { i ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (i == pagerState.currentPage) 8.dp else 6.dp)
                                            .background(
                                                if (i == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(SashioSlate.copy(alpha = 0.08f)))
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Text(product.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = SashioSlate, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = SashioGold.copy(alpha = 0.15f)), shape = RoundedCornerShape(4.dp), modifier = Modifier.wrapContentWidth()) {
                        Text("Variant: ${product.variantName.ifBlank { "Standard" }} | Model: ${product.modelNumber.ifBlank { "S-Series" }}", color = SashioGoldDark, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }

                    if (product.videoUrl.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                            Icon(Icons.Default.PlayCircle, contentDescription = "Video", tint = SashioRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Video Presentation Available", fontSize = 11.sp, color = SashioRed, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Description", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SashioSlate)
                    Text(product.description, fontSize = 13.sp, color = SashioTextMuted, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))

                    if (product.features.isNotBlank()) {
                        Text("Features", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SashioTeal)
                        Text(product.features, fontSize = 12.sp, color = SashioSlate, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
                    }
                    if (product.specifications.isNotBlank()) {
                        Text("Specifications", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SashioGoldDark)
                        Text(product.specifications, fontSize = 12.sp, color = SashioSlate, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
                    }
                    if (product.benefits.isNotBlank()) {
                        Text("Benefits", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SashioGreen)
                        Text(product.benefits, fontSize = 12.sp, color = SashioSlate, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
                    }
                    if (product.useCases.isNotBlank()) {
                        Text("Use Cases", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SashioBlue)
                        Text(product.useCases, fontSize = 12.sp, color = SashioSlate, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
                    }

                    if (product.basePrice > 0.0) {
                        Text("Guide Price: ₦%,.2f".format(product.basePrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SashioSlate)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SashioSlate),
                        border = BorderStroke(1.dp, SashioSlate.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) { Text("Close") }
                    Button(
                        onClick = onRequestQuote,
                        colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
                        modifier = Modifier.weight(1.4f).height(44.dp)
                    ) { Text("Request Quotation", fontWeight = FontWeight.Bold, color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun VipPresidentialTab(onRequestQuote: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SashioSlate),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Star, contentDescription = null, tint = SashioGold, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "PRESIDENTIAL LUXURY CABINS",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    "Experience Sovereign Comfort Anywhere",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SashioGold,
                    fontWeight = FontWeight.Light
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("VIRTUAL WALKTHROUGH & SPECIFICATIONS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text("Review elite features integrated inside presidential layouts:", style = MaterialTheme.typography.bodySmall, color = SashioTextMuted)

        Spacer(modifier = Modifier.height(12.dp))

        listOf(
            Triple(Icons.Default.AcUnit, "Climate Control Setup", "High duty quiet HVAC units maintaining stable 18 degree temperatures under any Lagos heat."),
            Triple(Icons.Default.VolumeUp, "Surround Audio System", "Integrated smart ceiling speakers playing ambient classical and zen rhythms."),
            Triple(Icons.Default.WaterDrop, "Automated Soft Flush", "Completely contactless hands-free automatic vacuum systems ensuring zero smells."),
            Triple(Icons.Default.SensorWindow, "LED Vanity Mirrors", "Sleek back-lit vanity touch control smart mirrors matching bridal makeup.")
        ).forEach { (icon, title, sDesc) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SashioGold.copy(alpha = 0.15f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = SashioGold)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SashioSlate)
                        Text(sDesc, fontSize = 11.sp, color = SashioTextMuted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onRequestQuote() },
            colors = ButtonDefaults.buttonColors(containerColor = SashioGold),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("REQUEST PRESIDENTIAL VIEW QUOTATION", fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
        }
    }
}
