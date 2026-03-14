package com.example.travelsapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.airbnb.lottie.compose.*
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripRecordsScreen(
    modifier: Modifier = Modifier,
    tripViewModel: TripViewModel,
    authViewModel: AuthViewModel,
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    onBackClick: () -> Unit
) {
    val trips by tripViewModel.trips.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    // Modern Professional Colors
    val primaryDark = Color(0xFF0D1B2A)
    val accentTeal = Color(0xFF00BFA5)
    val cardBg = Color(0xFF1B263B)

    var searchQuery by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val filteredTrips = remember(trips, searchQuery, startDate, endDate) {
        trips.filter { trip ->
            val vehicle = vehicles.find { it.id == trip.vehicleId }
            val driver = drivers.find { it.id == trip.driverId }
            
            val matchesSearch = trip.fromLocation.contains(searchQuery, ignoreCase = true) ||
                    trip.toLocation.contains(searchQuery, ignoreCase = true) ||
                    (vehicle?.name?.contains(searchQuery, ignoreCase = true) ?: false) ||
                    (driver?.name?.contains(searchQuery, ignoreCase = true) ?: false)
            
            val tripDate = try { LocalDate.parse(trip.date) } catch (e: Exception) { null }
            val inDateRange = if (tripDate != null) {
                (startDate == null || !tripDate.isBefore(startDate)) &&
                (endDate == null || !tripDate.isAfter(endDate))
            } else true

            matchesSearch && inDateRange
        }.reversed()
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            colors = DatePickerDefaults.colors(containerColor = primaryDark),
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showStartDatePicker = false
                }) { Text("OK", color = accentTeal) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            colors = DatePickerDefaults.colors(containerColor = primaryDark),
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        endDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showEndDatePicker = false
                }) { Text("OK", color = accentTeal) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Archives", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList, 
                            contentDescription = "Filter", 
                            tint = if (showFilters) accentTeal else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = primaryDark
    ) { paddingValues ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { 50 })
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // Background Glow
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 150.dp, y = 150.dp)
                        .alpha(0.05f)
                        .background(accentTeal, CircleShape)
                )

                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                    
                    // Innovative Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search routes, vehicles...", color = Color.White.copy(alpha = 0.3f)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentTeal) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.5f))
                                    }
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = accentTeal
                            ),
                            singleLine = true
                        )
                    }

                    // Date Filters Section
                    AnimatedVisibility(
                        visible = showFilters,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Filter by Period", color = accentTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    DateChip(
                                        label = startDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "Start Date",
                                        isSelected = startDate != null,
                                        onClick = { showStartDatePicker = true },
                                        accentTeal = accentTeal
                                    )
                                    DateChip(
                                        label = endDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "End Date",
                                        isSelected = endDate != null,
                                        onClick = { showEndDatePicker = true },
                                        accentTeal = accentTeal
                                    )
                                }
                                if (startDate != null || endDate != null) {
                                    TextButton(
                                        onClick = { startDate = null; endDate = null },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("RESET FILTERS", color = Color.Red.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Records List
                    if (filteredTrips.isEmpty()) {
                        EmptyRecordsView()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            items(filteredTrips) { trip ->
                                val vehicle = vehicles.find { it.id == trip.vehicleId }
                                val driver = drivers.find { it.id == trip.driverId }
                                ModernTripRecordCard(
                                    trip = trip, 
                                    vehicle = vehicle, 
                                    driver = driver,
                                    onDownload = {
                                        generateAndHandleInvoice(context, trip, vehicle, driver, currentUser?.travelsName ?: "Your Travels", isShare = false)
                                    },
                                    onShare = {
                                        generateAndHandleInvoice(context, trip, vehicle, driver, currentUser?.travelsName ?: "Your Travels", isShare = true)
                                    },
                                    accentTeal = accentTeal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateChip(label: String, isSelected: Boolean, onClick: () -> Unit, accentTeal: Color) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) accentTeal.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) accentTeal else Color.White.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isSelected) accentTeal else Color.White.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = if (isSelected) accentTeal else Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ModernTripRecordCard(
    trip: Trip, 
    vehicle: Vehicle?, 
    driver: Driver?, 
    onDownload: () -> Unit,
    onShare: () -> Unit,
    accentTeal: Color
) {
    val displayDate = try {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        LocalDate.parse(trip.date, inputFormatter).format(outputFormatter)
    } catch (e: Exception) {
        trip.date
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ROUTE LOG", color = accentTeal, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(trip.fromLocation, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = accentTeal, modifier = Modifier.padding(horizontal = 8.dp).size(14.dp))
                        Text(trip.toLocation, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(displayDate, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
                
                Surface(
                    color = accentTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "₹${String.format(Locale.getDefault(), "%,.0f", trip.income)}", 
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = accentTeal,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.05f))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    RecordDetail(icon = Icons.Default.DirectionsBus, label = vehicle?.name ?: "Unknown Vehicle", subLabel = vehicle?.vehicleNumber ?: "N/A")
                    Spacer(modifier = Modifier.height(10.dp))
                    RecordDetail(icon = Icons.Default.Badge, label = driver?.name ?: "Unassigned", subLabel = "Professional Driver")
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.size(44.dp).background(accentTeal, CircleShape)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color(0xFF0D1B2A), modifier = Modifier.size(18.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = (if (trip.profit >= 0) accentTeal else Color.Red).copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Operational Profit", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Text(
                        "₹${String.format(Locale.getDefault(), "%,.2f", trip.profit)}", 
                        color = if (trip.profit >= 0) accentTeal else Color.Red, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RecordDetail(icon: ImageVector, label: String, subLabel: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(subLabel, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
        }
    }
}

@Composable
fun EmptyRecordsView() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.travel_anim))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp).alpha(0.6f),
            contentScale = ContentScale.Fit
        )
        Text("No Trip Records Found", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
        Text("Try adjusting your filters or search query", color = Color.White.copy(alpha = 0.2f), fontSize = 12.sp)
    }
}

fun generateAndHandleInvoice(context: Context, trip: Trip, vehicle: Vehicle?, driver: Driver?, travelsName: String, isShare: Boolean) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()
    val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.tripnet1)
    val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, 100, 100, true)

    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 32f
    paint.isFakeBoldText = true
    paint.color = AndroidColor.BLACK
    canvas.drawText(travelsName.uppercase(Locale.getDefault()), 297f, 70f, paint)

    paint.textSize = 14f
    paint.isFakeBoldText = false
    paint.color = AndroidColor.DKGRAY
    canvas.drawText("OFFICIAL TRIP INVOICE", 297f, 95f, paint)

    canvas.drawBitmap(scaledLogo, 40f, 30f, null)

    paint.strokeWidth = 3f
    paint.color = AndroidColor.BLACK
    canvas.drawLine(40f, 120f, 555f, 120f, paint)

    paint.textAlign = Paint.Align.LEFT
    paint.textSize = 16f
    paint.isFakeBoldText = true
    paint.color = AndroidColor.BLACK
    canvas.drawText("CUSTOMER BILLING DETAILS", 50f, 160f, paint)
    
    paint.isFakeBoldText = false
    paint.textSize = 13f
    var y = 195f
    
    val tripDetails = listOf(
        "Invoice ID: #TN-REC-${trip.id}",
        "Booking Date: ${trip.date}",
        "From: ${trip.fromLocation}",
        "To: ${trip.toLocation}",
        "----------------------------------------------------------------",
        "Assigned Vehicle: ${vehicle?.name ?: "N/A"}",
        "Vehicle Registration: ${vehicle?.vehicleNumber ?: "N/A"}",
        "Professional Driver: ${driver?.name ?: "N/A"}"
    )

    for (detail in tripDetails) {
        canvas.drawText(detail, 60f, y, paint)
        y += 28f
    }

    y += 30f
    paint.isFakeBoldText = true
    paint.textSize = 16f
    canvas.drawText("PRICE SUMMARY", 50f, y, paint)
    y += 12f
    canvas.drawLine(50f, y, 545f, y, paint)
    y += 40f

    paint.textSize = 18f
    canvas.drawText("Total Trip Amount Payable", 60f, y, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Rs. ${String.format(Locale.getDefault(), "%,.2f", trip.income)}", 535f, y, paint)

    y += 100f
    paint.textAlign = Paint.Align.LEFT
    paint.textSize = 12f
    paint.isFakeBoldText = true
    canvas.drawText("Terms & Conditions:", 50f, y, paint)
    y += 20f
    paint.isFakeBoldText = false
    paint.textSize = 10f
    val terms = listOf(
        "1. All payments are to be made in favor of ${travelsName}.",
        "2. Please check all details before leaving the vehicle.",
        "3. This is a computer generated invoice and requires no signature."
    )
    for (term in terms) {
        canvas.drawText(term, 60f, y, paint)
        y += 18f
    }

    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 11f
    paint.color = AndroidColor.GRAY
    canvas.drawText("Thank you for choosing ${travelsName} for your journey!", 297f, 750f, paint)
    
    paint.color = AndroidColor.BLACK
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("Powered by TripNet App", 297f, 785f, paint)

    pdfDocument.finishPage(page)

    val fileName = "Invoice_${trip.id}.pdf"
    val file = File(context.cacheDir, fileName)

    try {
        pdfDocument.writeTo(FileOutputStream(file))
        if (isShare) {
            val uri: Uri = FileProvider.getUriForFile(context, "com.example.travelsapplication.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Invoice via"))
        } else {
            val downloadsFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            pdfDocument.writeTo(FileOutputStream(downloadsFile))
            Toast.makeText(context, "Invoice saved to Downloads", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}
