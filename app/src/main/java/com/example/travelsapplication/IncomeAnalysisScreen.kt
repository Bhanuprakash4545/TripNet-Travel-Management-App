package com.example.travelsapplication

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeAnalysisScreen(
    tripViewModel: TripViewModel,
    onBackClick: () -> Unit
) {
    val trips by tripViewModel.trips.collectAsState()
    
    // Modern Professional Colors
    val primaryDark = Color(0xFF0D1B2A)
    val accentTeal = Color(0xFF00BFA5)
    val softRed = Color(0xFFEF5350)

    var selectedTab by remember { mutableIntStateOf(1) } // 0: Day, 1: Month
    val tabs = listOf("Daily View", "Monthly View")

    // Filtering states
    var selectedDayOption by remember { mutableStateOf("Today") }
    var selectedCustomDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedMonthOption by remember { mutableStateOf("This Month") }
    var selectedCustomMonth by remember { mutableStateOf<YearMonth?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    val filteredTrips = remember(trips, selectedTab, selectedDayOption, selectedCustomDate, selectedMonthOption, selectedCustomMonth) {
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        
        trips.filter { trip ->
            try {
                val tripDate = LocalDate.parse(trip.date, formatter)
                if (selectedTab == 0) { // Day
                    when (selectedDayOption) {
                        "Today" -> tripDate == now
                        "Yesterday" -> tripDate == now.minusDays(1)
                        "Custom" -> selectedCustomDate?.let { tripDate == it } ?: false
                        else -> false
                    }
                } else { // Month
                    when (selectedMonthOption) {
                        "This Month" -> tripDate.month == now.month && tripDate.year == now.year
                        "Last Month" -> {
                            val lastMonth = now.minusMonths(1)
                            tripDate.month == lastMonth.month && tripDate.year == lastMonth.year
                        }
                        "Select Month" -> selectedCustomMonth?.let { 
                            tripDate.month == it.month && tripDate.year == it.year 
                        } ?: false
                        else -> false
                    }
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    val totalIncome = filteredTrips.sumOf { it.income }
    val totalFuel = filteredTrips.sumOf { it.fuelCost }
    val totalSalary = filteredTrips.sumOf { it.driverSalary }
    val totalToll = filteredTrips.sumOf { it.tollCost }
    val totalOther = filteredTrips.sumOf { it.otherExpense }
    val totalExpenses = totalFuel + totalSalary + totalToll + totalOther
    val netProfit = totalIncome - totalExpenses

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            colors = DatePickerDefaults.colors(containerColor = primaryDark),
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        if (selectedTab == 0) {
                            selectedCustomDate = selectedDate
                            selectedDayOption = "Custom"
                        } else {
                            selectedCustomMonth = YearMonth.from(selectedDate)
                            selectedMonthOption = "Select Month"
                        }
                    }
                    showDatePicker = false
                }) { Text("OK", color = accentTeal) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.White.copy(alpha = 0.6f)) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            onDismissRequest = { showMonthPicker = false },
            onMonthYearSelected = { month, year ->
                selectedCustomMonth = YearMonth.of(year, month)
                selectedMonthOption = "Select Month"
                showMonthPicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Intelligence", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = primaryDark
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Background Decorative Element
            Box(
                modifier = Modifier
                    .size(350.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-100).dp, y = 100.dp)
                    .alpha(0.05f)
                    .background(accentTeal, CircleShape)
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Professional Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = accentTeal,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = accentTeal
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title, 
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                ) 
                            }
                        )
                    }
                }

                // Filtering Controls
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedTab == 0) {
                            ModernOptionChip("Today", selectedDayOption == "Today") { selectedDayOption = "Today" }
                            ModernOptionChip("Yesterday", selectedDayOption == "Yesterday") { selectedDayOption = "Yesterday" }
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = if (selectedDayOption == "Custom") accentTeal else Color.White.copy(alpha = 0.4f))
                            }
                        } else {
                            ModernOptionChip("Current", selectedMonthOption == "This Month") { selectedMonthOption = "This Month" }
                            ModernOptionChip("Previous", selectedMonthOption == "Last Month") { selectedMonthOption = "Last Month" }
                            IconButton(onClick = { showMonthPicker = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = if (selectedMonthOption == "Select Month") accentTeal else Color.White.copy(alpha = 0.4f))
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        // Main Financial Summary Card
                        ModernSummaryCard(totalIncome, totalExpenses, netProfit, accentTeal, softRed)
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // Charts Header
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(color = accentTeal.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.PieChart, contentDescription = null, tint = accentTeal, modifier = Modifier.padding(8.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Expense Allocation", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Graphical Bar Chart
                        ProfessionalGraphicalAnalysis(totalFuel, totalSalary, totalToll, totalOther)
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Detailed Breakdown List
                        Text(
                            "DETAILED BREAKDOWN", 
                            color = Color.White.copy(alpha = 0.4f), 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold, 
                            letterSpacing = 1.sp,
                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 12.dp)
                        )
                        
                        ModernExpenseList(totalFuel, totalSalary, totalToll, totalOther)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernOptionChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val accentTeal = Color(0xFF00BFA5)
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) accentTeal.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) accentTeal else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) accentTeal else Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun ModernSummaryCard(income: Double, expenses: Double, profit: Double, accent: Color, warning: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("TOTAL REVENUE", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("₹${String.format(Locale.getDefault(), "%,.0f", income)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
                Surface(color = accent.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = accent, modifier = Modifier.padding(12.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("EXPENSES", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("₹${String.format(Locale.getDefault(), "%,.0f", expenses)}", color = warning.copy(alpha = 0.8f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                VerticalDivider(modifier = Modifier.height(30.dp), color = Color.White.copy(alpha = 0.1f))
                Column(modifier = Modifier.weight(1f)) {
                    Text("NET PROFIT", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("₹${String.format(Locale.getDefault(), "%,.0f", profit)}", color = if (profit >= 0) accent else warning, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfessionalGraphicalAnalysis(totalFuel: Double, totalSalary: Double, totalToll: Double, totalOther: Double) {
    val maxVal = maxOf(totalFuel, totalSalary, totalToll, totalOther, 1.0).toFloat()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(220.dp).padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            ModernBar((totalFuel.toFloat() / maxVal), "Fuel", Color(0xFF4FC3F7))
            ModernBar((totalSalary.toFloat() / maxVal), "Salary", Color(0xFF81C784))
            ModernBar((totalToll.toFloat() / maxVal), "Toll", Color(0xFFFFB74D))
            ModernBar((totalOther.toFloat() / maxVal), "Other", Color(0xFFBA68C8))
        }
    }
}

@Composable
fun ModernBar(fraction: Float, label: String, color: Color) {
    val animatedHeight by animateFloatAsState(targetValue = fraction.coerceIn(0.05f, 1f), animationSpec = tween(1000, easing = FastOutSlowInEasing))
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight(animatedHeight)
                .background(
                    Brush.verticalGradient(listOf(color, color.copy(alpha = 0.3f))),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ModernExpenseList(totalFuel: Double, totalSalary: Double, totalToll: Double, totalOther: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ModernExpenseItem("Fuel Consumption", totalFuel, Color(0xFF4FC3F7), Icons.Default.LocalGasStation)
        ModernExpenseItem("Driver Remuneration", totalSalary, Color(0xFF81C784), Icons.Default.Payments)
        ModernExpenseItem("Highway Tolls", totalToll, Color(0xFFFFB74D), Icons.Default.Foundation)
        ModernExpenseItem("Miscellaneous", totalOther, Color(0xFFBA68C8), Icons.AutoMirrored.Filled.ReceiptLong)
    }
}

@Composable
fun ModernExpenseItem(label: String, amount: Double, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp).size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text("₹${String.format(Locale.getDefault(), "%,.0f", amount)}", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MonthYearPickerDialog(
    onDismissRequest: () -> Unit,
    onMonthYearSelected: (Int, Int) -> Unit
) {
    val currentYear = LocalDate.now().year
    val currentMonth = LocalDate.now().monthValue
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    val accentTeal = Color(0xFF00BFA5)

    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Select Billing Cycle", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedYear-- }) { Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White) }
                    Text("$selectedYear", color = accentTeal, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = { if (selectedYear < currentYear) selectedYear++ }, enabled = selectedYear < currentYear) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (selectedYear < currentYear) Color.White else Color.White.copy(alpha = 0.2f))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column {
                    for (row in 0 until 4) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            for (col in 0 until 3) {
                                val monthIdx = row * 3 + col
                                val monthNum = monthIdx + 1
                                val isSelected = selectedMonth == monthNum
                                val isEnabled = selectedYear < currentYear || monthNum <= currentMonth
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) accentTeal else if (isEnabled) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                        .clickable(enabled = isEnabled) { selectedMonth = monthNum }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        months[monthIdx], 
                                        color = if (isSelected) Color(0xFF0D1B2A) else if (isEnabled) Color.White else Color.White.copy(alpha = 0.2f),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissRequest) { Text("CANCEL", color = Color.White.copy(alpha = 0.6f)) }
                    Button(
                        onClick = { onMonthYearSelected(selectedMonth, selectedYear) },
                        colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("APPLY", color = Color(0xFF0D1B2A), fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
