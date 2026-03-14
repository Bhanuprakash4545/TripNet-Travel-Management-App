package com.example.travelsapplication

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverListScreen(
    modifier: Modifier = Modifier,
    driverViewModel: DriverViewModel,
    onBackClick: () -> Unit
) {
    var driverName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    
    val drivers by driverViewModel.drivers.collectAsState()
    val errorMsg by driverViewModel.errorFlow.collectAsState(initial = "")
    val context = LocalContext.current

    // Modern Professional Colors
    val primaryDark = Color(0xFF0D1B2A)
    val accentTeal = Color(0xFF00BFA5)
    val warningRed = Color(0xFFEF5350)

    var showEditDialog by remember { mutableStateOf(false) }
    var driverToEdit by remember { mutableStateOf<Driver?>(null) }
    
    // Success Animation State
    var showSuccessAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(errorMsg) {
        if (errorMsg.isNotEmpty()) {
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    // Listen for success from ViewModel
    LaunchedEffect(Unit) {
        driverViewModel.addSuccessFlow.collect {
            showSuccessAnimation = true
            delay(2000)
            showSuccessAnimation = false
        }
    }

    if (showEditDialog && driverToEdit != null) {
        var editName by remember { mutableStateOf(driverToEdit!!.name) }
        var editPhone by remember { mutableStateOf(driverToEdit!!.phoneNumber) }
        var editAge by remember { mutableStateOf(driverToEdit!!.age.toString()) }
        var editPin by remember { mutableStateOf(driverToEdit!!.pin) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = primaryDark,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.7f),
            title = { Text("Edit Driver Details", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName, 
                        onValueChange = { editName = it }, 
                        label = { Text("Driver Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentTeal,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { if (it.length <= 10) editPhone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentTeal,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editAge,
                            onValueChange = { if (it.length <= 2) editAge = it },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = accentTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = editPin,
                            onValueChange = { if (it.length <= 4) editPin = it },
                            label = { Text("PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = accentTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        driverToEdit?.let {
                            driverViewModel.updateDriver(it.copy(
                                name = editName,
                                phoneNumber = editPhone,
                                age = editAge.toIntOrNull() ?: 0,
                                pin = editPin
                            ))
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                    enabled = editPhone.length == 10 && editPin.length == 4
                ) { Text("Save", color = primaryDark, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { 
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f)) 
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Roster", fontWeight = FontWeight.Bold, color = Color.White) },
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
            // Decorative Glow
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-50).dp, y = (-50).dp)
                    .alpha(0.05f)
                    .background(accentTeal, CircleShape)
            )

            Column(
                modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Professional Input Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Register New Driver",
                            color = accentTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = accentTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = accentTeal,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 10) phoneNumber = it },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = accentTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = accentTeal,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = age,
                                onValueChange = { if (it.length <= 2) age = it },
                                label = { Text("Age") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = accentTeal,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = accentTeal,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(
                                value = pin,
                                onValueChange = { if (it.length <= 4) pin = it },
                                label = { Text("Login PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = accentTeal,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = accentTeal,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = {
                                if (driverName.isNotBlank() && phoneNumber.length == 10 && pin.length == 4) {
                                    driverViewModel.addDriver(
                                        Driver(
                                            name = driverName,
                                            phoneNumber = phoneNumber,
                                            age = age.toIntOrNull() ?: 0,
                                            pin = pin
                                        )
                                    )
                                    driverName = ""; phoneNumber = ""; age = ""; pin = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                            enabled = phoneNumber.length == 10 && pin.length == 4 && age.isNotEmpty()
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = primaryDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ONBOARD DRIVER", fontWeight = FontWeight.ExtraBold, color = primaryDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // List Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Active Personnel",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Surface(
                        color = accentTeal.copy(alpha = 0.2f),
                        shape = CircleShape
                    ) {
                        Text(
                            "${drivers.size}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = accentTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(drivers) { driver ->
                        DriverItemCard(
                            driver = driver,
                            onEdit = { driverToEdit = driver; showEditDialog = true },
                            onDelete = { driverViewModel.deleteDriver(driver) },
                            onResetDevice = { driverViewModel.resetDriverDevice(driver) }
                        )
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
                        "Registration Successful!",
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
fun DriverItemCard(
    driver: Driver,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onResetDevice: () -> Unit
) {
    val accentTeal = Color(0xFF00BFA5)
    val warningRed = Color(0xFFEF5350)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (driver.deviceId != null) accentTeal.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.1f)
            ) {
                Icon(
                    if (driver.deviceId != null) Icons.Default.VerifiedUser else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (driver.deviceId != null) accentTeal else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = driver.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Phone: ${driver.phoneNumber}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                if (driver.deviceId != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(accentTeal, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Device Bound", color = accentTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Row {
                if (driver.deviceId != null) {
                    IconButton(onClick = onResetDevice, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.PhonelinkErase, contentDescription = "Reset", tint = warningRed, modifier = Modifier.size(20.dp))
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = warningRed.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
