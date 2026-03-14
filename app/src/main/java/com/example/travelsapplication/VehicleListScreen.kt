package com.example.travelsapplication

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    modifier: Modifier = Modifier,
    vehicleViewModel: VehicleViewModel,
    onBackClick: () -> Unit
) {
    var vehicleName by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf(TextFieldValue("")) }
    val vehicles by vehicleViewModel.vehicles.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }

    // Modern Professional Colors
    val primaryDark = Color(0xFF0D1B2A)
    val accentTeal = Color(0xFF00BFA5)
    
    // Animation for entrance
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    // Success Animation State
    var showSuccessAnimation by remember { mutableStateOf(false) }

    // Listen for success from ViewModel
    LaunchedEffect(Unit) {
        vehicleViewModel.addSuccessFlow.collect {
            showSuccessAnimation = true
            delay(2000)
            showSuccessAnimation = false
        }
    }

    fun formatVehicleNumber(input: String): String {
        val clean = input.uppercase().filter { it.isLetterOrDigit() }
        val sb = StringBuilder()
        var validCharCount = 0
        for (i in clean.indices) {
            val char = clean[i]
            val isCharValid = when (validCharCount) {
                0, 1 -> char.isLetter()
                2, 3 -> char.isDigit()
                4, 5 -> char.isLetter()
                in 6..9 -> char.isDigit()
                else -> false
            }

            if (isCharValid) {
                sb.append(char)
                validCharCount++
                if ((validCharCount == 2 || validCharCount == 4 || validCharCount == 6) && validCharCount < 10) {
                    sb.append("-")
                }
            }
        }
        return sb.toString()
    }

    if (showEditDialog && vehicleToEdit != null) {
        var editName by remember { mutableStateOf(vehicleToEdit!!.name) }
        var editNumber by remember { mutableStateOf(TextFieldValue(vehicleToEdit!!.vehicleNumber)) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = Color(0xFF1B263B),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.7f),
            title = { Text("Edit Vehicle", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Vehicle Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentTeal,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = accentTeal,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editNumber,
                        onValueChange = { input ->
                            if (input.text.length < editNumber.text.length) {
                                editNumber = input.copy(text = input.text.uppercase())
                            } else {
                                val formatted = formatVehicleNumber(input.text)
                                editNumber = TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(formatted.length)
                                )
                            }
                        },
                        label = { Text("Vehicle Number") },
                        placeholder = { Text("AA-00-AA-0000") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentTeal,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = accentTeal,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vehicleToEdit?.let {
                            vehicleViewModel.updateVehicle(it.copy(name = editName, vehicleNumber = editNumber.text))
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                ) {
                    Text("Save", color = primaryDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Fleet Inventory", fontWeight = FontWeight.Bold, color = Color.White) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = primaryDark,
        modifier = modifier
    ) { paddingValues ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { 50 })
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // Background Glow
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 150.dp, y = 150.dp)
                        .alpha(0.08f)
                        .background(accentTeal, CircleShape)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Modern Header Card with Live Preview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = accentTeal.copy(alpha = 0.15f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(Icons.Default.AddBusiness, contentDescription = null, tint = accentTeal, modifier = Modifier.padding(10.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Vehicle Onboarding",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            OutlinedTextField(
                                value = vehicleName,
                                onValueChange = { vehicleName = it },
                                label = { Text("Vehicle Name") },
                                modifier = Modifier.fillMaxWidth(),
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
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = vehicleNumber,
                                onValueChange = { input ->
                                    if (input.text.length < vehicleNumber.text.length) {
                                        vehicleNumber = input.copy(text = input.text.uppercase())
                                    } else {
                                        val formatted = formatVehicleNumber(input.text)
                                        vehicleNumber = TextFieldValue(
                                            text = formatted,
                                            selection = TextRange(formatted.length)
                                        )
                                    }
                                },
                                label = { Text("Registration Number") },
                                placeholder = { Text("AA-00-AA-0000") },
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                modifier = Modifier.fillMaxWidth(),
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
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = {
                                    if (vehicleName.isNotBlank() && vehicleNumber.text.isNotBlank()) {
                                        vehicleViewModel.addVehicle(Vehicle(name = vehicleName, vehicleNumber = vehicleNumber.text))
                                        vehicleName = ""
                                        vehicleNumber = TextFieldValue("")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                                enabled = vehicleName.isNotBlank() && vehicleNumber.text.length >= 10,
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = primaryDark)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("REGISTER TO FLEET", fontWeight = FontWeight.Black, color = primaryDark, letterSpacing = 1.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Fleet List Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Operational Fleet",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                            Text(
                                "Total managed vehicles: ${vehicles.size}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        
                        IconButton(onClick = { /* Refresh or Filter */ }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = accentTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (vehicles.isEmpty()) {
                        EmptyFleetSection()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            items(vehicles) { vehicle ->
                                VehicleItemCard(
                                    vehicle = vehicle,
                                    onEdit = {
                                        vehicleToEdit = vehicle
                                        showEditDialog = true
                                    },
                                    onDelete = { vehicleViewModel.deleteVehicle(vehicle) }
                                )
                            }
                        }
                    }
                }
                
                // Success Overlay Animation
                if (showSuccessAnimation) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.register_anim))
                        LottieAnimation(
                            composition = composition,
                            modifier = Modifier.size(250.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            "Vehicle Registered!",
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
}

@Composable
fun EmptyFleetSection() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.travel_anim))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(180.dp).alpha(0.6f),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No vehicles registered yet",
            color = Color.White.copy(alpha = 0.4f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun VehicleItemCard(
    vehicle: Vehicle,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accentTeal = Color(0xFF00BFA5)
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { isPressed = true },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentTeal.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsBus, // Changed to Bus for a variety
                    contentDescription = null,
                    tint = accentTeal,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(18.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = vehicle.vehicleNumber,
                        color = accentTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350).copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}
