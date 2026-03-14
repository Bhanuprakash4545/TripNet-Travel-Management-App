package com.example.travelsapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInformationScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var travelsName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Modern Professional Colors
    val primaryDark = Color(0xFF0D1B2A)
    val accentTeal = Color(0xFF00BFA5)
    val cardBg = Color(0xFF1B263B)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profilePhotoUri = uri
            isEditing = true // Automatically enter edit mode when photo is selected
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            travelsName = it.travelsName
            phoneNumber = it.phoneNumber
            profilePhotoUri = it.profilePhotoUri?.let { uriString -> Uri.parse(uriString) }
            isEditing = false
        } ?: run {
            isEditing = true
        }
    }

    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Fleet Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
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
                        .size(350.dp)
                        .align(Alignment.TopStart)
                        .offset(x = (-100).dp, y = (-50).dp)
                        .alpha(0.08f)
                        .background(accentTeal, CircleShape)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Innovative Profile Image Section
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier
                                .size(140.dp)
                                .shadow(24.dp, CircleShape)
                                .clickable { launcher.launch("image/*") },
                            shape = CircleShape,
                            color = cardBg,
                            border = androidx.compose.foundation.BorderStroke(3.dp, accentTeal)
                        ) {
                            if (profilePhotoUri != null) {
                                AsyncImage(
                                    model = profilePhotoUri,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Default Profile",
                                    modifier = Modifier.size(80.dp).padding(20.dp),
                                    tint = Color.White.copy(alpha = 0.2f)
                                )
                            }
                        }
                        
                        // Pencil Edit Button - Always visible as requested
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(8.dp, CircleShape)
                                .clickable { launcher.launch("image/*") },
                            shape = CircleShape,
                            color = accentTeal,
                            tonalElevation = 8.dp
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Change Photo",
                                modifier = Modifier.padding(10.dp),
                                tint = primaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (isEditing) "Update Your Identity" else travelsName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    
                    Text(
                        text = if (currentUser?.role == "Driver") "Certified Driver" else "Fleet Operations Manager",
                        style = MaterialTheme.typography.bodyMedium,
                        color = accentTeal,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Information Cards
                    InfoSection(
                        isEditing = isEditing,
                        travelsName = travelsName,
                        onNameChange = { travelsName = it },
                        phoneNumber = phoneNumber,
                        accentTeal = accentTeal,
                        cardBg = cardBg
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Action Buttons
                    if (!isEditing) {
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = primaryDark)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("MODIFY PROFILE", fontWeight = FontWeight.Black, color = primaryDark, letterSpacing = 1.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                val userToSave = currentUser?.copy(
                                    travelsName = travelsName,
                                    profilePhotoUri = profilePhotoUri?.toString()
                                ) ?: User(
                                    phoneNumber = phoneNumber,
                                    travelsName = travelsName,
                                    state = "",
                                    city = "",
                                    profilePhotoUri = profilePhotoUri?.toString()
                                )
                                authViewModel.updateUser(userToSave)
                                isEditing = false
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                            enabled = travelsName.isNotBlank()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = primaryDark)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("SAVE CHANGES", fontWeight = FontWeight.Black, color = primaryDark, letterSpacing = 1.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        TextButton(
                            onClick = { 
                                isEditing = false
                                currentUser?.let {
                                    travelsName = it.travelsName
                                    profilePhotoUri = it.profilePhotoUri?.let { uri -> Uri.parse(uri) }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("DISCARD CHANGES", color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun InfoSection(
    isEditing: Boolean,
    travelsName: String,
    onNameChange: (String) -> Unit,
    phoneNumber: String,
    accentTeal: Color,
    cardBg: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Account Details",
                color = accentTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Phone Field (Always Read-Only)
            InfoItem(
                label = "Contact Identifier",
                value = phoneNumber,
                icon = Icons.Default.Phone,
                isEditing = false,
                onValueChange = {},
                accentTeal = accentTeal
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.White.copy(alpha = 0.05f)
            )

            // Travels Name Field
            InfoItem(
                label = "Operations Name",
                value = travelsName,
                icon = Icons.Default.BusinessCenter,
                isEditing = isEditing,
                onValueChange = onNameChange,
                accentTeal = accentTeal
            )
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    accentTeal: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = accentTeal.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accentTeal,
                modifier = Modifier.padding(10.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            if (isEditing) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = accentTeal,
                        focusedIndicatorColor = accentTeal,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().offset(x = (-16).dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
