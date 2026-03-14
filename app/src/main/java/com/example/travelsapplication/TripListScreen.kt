package com.example.travelsapplication

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    modifier: Modifier = Modifier,
    tripViewModel: TripViewModel,
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    onBackClick: () -> Unit
) {
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var selectedDriver by remember { mutableStateOf<Driver?>(null) }
    var date by remember { mutableStateOf("") }
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var tripPrice by remember { mutableStateOf("") }
    var fuelCost by remember { mutableStateOf("") }
    var driverSalary by remember { mutableStateOf("") }
    var tollCost by remember { mutableStateOf("") }
    var otherExpense by remember { mutableStateOf("") }

    var expensesVisible by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    // Modern Professional Colors
    val primaryDark = Color(0xFF0D1B2A)
    val accentTeal = Color(0xFF00BFA5)

    // Success Animation State
    var showSuccessAnimation by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                return utcTimeMillis >= today
            }
        }
    )

    // Listen for success from ViewModel
    LaunchedEffect(Unit) {
        tripViewModel.addSuccessFlow.collect {
            showSuccessAnimation = true
            delay(1500)
            showSuccessAnimation = false
            onBackClick() // Navigate back after animation
        }
    }

    val totalExpenses = (fuelCost.toDoubleOrNull() ?: 0.0) +
        (driverSalary.toDoubleOrNull() ?: 0.0) +
        (tollCost.toDoubleOrNull() ?: 0.0) +
        (otherExpense.toDoubleOrNull() ?: 0.0)
    val profit = (tripPrice.toDoubleOrNull() ?: 0.0) - totalExpenses

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            colors = DatePickerDefaults.colors(
                containerColor = primaryDark,
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                selectedDayContainerColor = accentTeal,
                selectedDayContentColor = primaryDark,
                todayContentColor = accentTeal,
                todayDateBorderColor = accentTeal
            ),
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        date = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDatePicker = false
                }) {
                    Text("SELECT", color = accentTeal, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("CANCEL", color = Color.White.copy(alpha = 0.6f))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan New Journey", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = primaryDark
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background Gradient Glow
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 100.dp, y = (-50).dp)
                    .alpha(0.05f)
                    .background(accentTeal, CircleShape)
            )

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Trip Essentials",
                            color = accentTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        ModernDropdownField(
                            label = "Select Vehicle",
                            items = vehicles,
                            selectedItem = selectedVehicle,
                            onItemSelected = { selectedVehicle = it },
                            itemToString = { "${it.name} (${it.vehicleNumber})" },
                            icon = Icons.Default.DirectionsBus
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ModernDropdownField(
                            label = "Assign Driver",
                            items = drivers,
                            selectedItem = selectedDriver,
                            onItemSelected = { selectedDriver = it },
                            itemToString = { it.name },
                            icon = Icons.Default.Badge
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                            OutlinedTextField(
                                value = date,
                                onValueChange = {},
                                label = { Text("Departure Date") },
                                readOnly = true,
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = accentTeal) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false, // Disable TextField to ensure Box receives the click
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.White,
                                    disabledBorderColor = Color.White.copy(alpha = 0.2f),
                                    disabledLabelColor = Color.White.copy(alpha = 0.5f),
                                    disabledLeadingIconColor = accentTeal,
                                    disabledPlaceholderColor = Color.White.copy(alpha = 0.3f),
                                    disabledContainerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Route & Price Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Route, contentDescription = null, tint = accentTeal)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Route & Pricing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = from, 
                                onValueChange = { from = it }, 
                                label = { Text("From") }, 
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = accentTeal,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(
                                value = to, 
                                onValueChange = { to = it }, 
                                label = { Text("To") }, 
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = accentTeal,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = tripPrice,
                            onValueChange = { input -> if (input.all { it.isDigit() || it == '.' }) tripPrice = input },
                            label = { Text("Revenue (Trip Price)") },
                            leadingIcon = { Text("₹", color = accentTeal, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = accentTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Financial Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { expensesVisible = !expensesVisible },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = accentTeal)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Operating Expenses", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Icon(
                                if (expensesVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = accentTeal
                            )
                        }
                        
                        AnimatedVisibility(visible = expensesVisible) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                ExpenseInput(label = "Fuel Cost", value = fuelCost, onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) fuelCost = it })
                                ExpenseInput(label = "Driver Salary", value = driverSalary, onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) driverSalary = it })
                                ExpenseInput(label = "Toll Cost", value = tollCost, onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) tollCost = it })
                                ExpenseInput(label = "Other Expenses", value = otherExpense, onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) otherExpense = it })
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color.White.copy(alpha = 0.1f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ESTIMATED PROFIT", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text(
                                    "₹${String.format(Locale.getDefault(), "%.2f", profit)}", 
                                    fontWeight = FontWeight.Black, 
                                    color = if (profit >= 0) accentTeal else Color(0xFFEF5350), 
                                    fontSize = 24.sp
                                )
                            }
                            Surface(
                                color = (if (profit >= 0) accentTeal else Color(0xFFEF5350)).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    if (profit >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (profit >= 0) accentTeal else Color(0xFFEF5350),
                                    modifier = Modifier.padding(12.dp).size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val newTrip = Trip(
                            vehicleId = selectedVehicle?.id ?: 0,
                            driverId = selectedDriver?.id ?: 0,
                            date = date,
                            fromLocation = from,
                            toLocation = to,
                            income = tripPrice.toDoubleOrNull() ?: 0.0,
                            fuelCost = fuelCost.toDoubleOrNull() ?: 0.0,
                            driverSalary = driverSalary.toDoubleOrNull() ?: 0.0,
                            tollCost = tollCost.toDoubleOrNull() ?: 0.0,
                            otherExpense = otherExpense.toDoubleOrNull() ?: 0.0,
                            profit = profit
                        )
                        tripViewModel.addTrip(newTrip)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                    enabled = selectedVehicle != null && selectedDriver != null && date.isNotBlank() && from.isNotBlank() && to.isNotBlank(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = primaryDark)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("CREATE TRIP RECORD", fontWeight = FontWeight.Black, color = primaryDark, letterSpacing = 1.sp)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Success Overlay Animation
            if (showSuccessAnimation) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.register_anim))
                    LottieAnimation(
                        composition = composition,
                        modifier = Modifier.size(250.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        "Trip Scheduled Successfully!",
                        color = accentTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center).padding(top = 220.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseInput(label: String, value: String, onValueChange: (String) -> Unit) {
    val accentTeal = Color(0xFF00BFA5)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Text("₹", color = accentTeal.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = accentTeal.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ModernDropdownField(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemToString: (T) -> String,
    icon: ImageVector
) {
    var expanded by remember { mutableStateOf(false) }
    val accentTeal = Color(0xFF00BFA5)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedItem?.let { itemToString(it) } ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            leadingIcon = { Icon(icon, contentDescription = null, tint = accentTeal) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = accentTeal,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedLabelColor = accentTeal,
                unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
            )
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1B263B))
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemToString(item), color = Color.White) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
