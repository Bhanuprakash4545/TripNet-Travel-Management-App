package com.example.travelsapplication

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DashboardTile(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

val tiles = listOf(
    DashboardTile("Fleet Registry", "Manage your vehicles", Icons.Default.DirectionsBus, "vehicleList", Color(0xFF00BFA5)),
    DashboardTile("Driver Roster", "Manage personnel", Icons.Default.Badge, "driverList", Color(0xFF2196F3)),
    DashboardTile("Trip Planner", "Schedule new journeys", Icons.Default.Route, "tripList", Color(0xFFFF9800)),
    DashboardTile("Trip Archives", "History and logs", Icons.Default.Analytics, "tripRecords", Color(0xFF9C27B0)),
    DashboardTile("Billing/Invoices", "Generate receipts", Icons.Default.ReceiptLong, "tripRecords", Color(0xFF607D8B)),
    DashboardTile("Intelligence", "Income analysis", Icons.Default.QueryStats, "incomeAnalysis", Color(0xFFE91E63)),
    DashboardTile("Live Ops", "Real-time tracking", Icons.Default.GpsFixed, "liveTracking", Color(0xFFF44336))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onTileClick: (String) -> Unit,
    tripViewModel: TripViewModel,
    authViewModel: AuthViewModel,
    vehicleViewModel: VehicleViewModel,
    driverViewModel: DriverViewModel,
    onNavigateToPersonalInformation: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Modern Professional Colors
    val primaryDark = Color(0xFF0D1B2A)
    val accentTeal = Color(0xFF00BFA5)
    
    val bgGradient = Brush.verticalGradient(
        colors = listOf(primaryDark, Color(0xFF1B263B))
    )
    
    val lastTrip by tripViewModel.lastTrip.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsState()
    val vehicles by vehicleViewModel.vehicles.collectAsStateWithLifecycle()
    val drivers by driverViewModel.drivers.collectAsStateWithLifecycle()

    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = primaryDark,
                drawerContentColor = Color.White,
                modifier = Modifier.width(300.dp),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(primaryDark, Color(0xFF1B263B))
                            )
                        )
                        .padding(top = 40.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
                ) {
                    Column {
                        Surface(
                            shape = CircleShape,
                            color = accentTeal.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, accentTeal),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).padding(8.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentUser?.travelsName ?: "TripNet Traveler",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.phoneNumber ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile Information", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToPersonalInformation()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("About TripNet", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToAbout()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    label = { Text("Sign Out", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            authViewModel.logout()
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = Color(0xFFEF5350),
                        unselectedTextColor = Color(0xFFEF5350)
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TripNet", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            Text("Fleet Management", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f)))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToPersonalInformation) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onTileClick("tripList") },
                    containerColor = accentTeal,
                    contentColor = primaryDark,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Trip")
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = primaryDark,
                    contentColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Dashboard") },
                        selected = true,
                        onClick = { },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentTeal,
                            selectedTextColor = accentTeal,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Timeline, contentDescription = null) },
                        label = { Text("Analysis") },
                        selected = false,
                        onClick = { onTileClick("incomeAnalysis") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DirectionsBus, contentDescription = null) },
                        label = { Text("Fleet") },
                        selected = false,
                        onClick = { onTileClick("vehicleList") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        ) { innerPadding ->
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { it / 10 })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgGradient)
                        .padding(innerPadding)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Stats Section
                        item(span = { GridItemSpan(2) }) {
                            FleetStatsHeader(vehicles.size, drivers.size)
                        }

                        // Active Trip Section
                        item(span = { GridItemSpan(2) }) {
                            if (lastTrip != null) {
                                ModernTripInfoCard(lastTrip!!, onTileClick)
                            } else {
                                TravelAnimationSection()
                            }
                        }

                        // Quick Actions Header
                        item(span = { GridItemSpan(2) }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            ) {
                                Surface(
                                    color = accentTeal.copy(alpha = 0.15f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = accentTeal, modifier = Modifier.padding(4.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "CONTROL CENTER",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }

                        // Innovative Dashboard Grid Tiles
                        items(tiles) { tile ->
                            ProfessionalActionTile(
                                tile = tile,
                                onClick = { onTileClick(tile.route) },
                                count = when(tile.route) {
                                    "vehicleList" -> vehicles.size
                                    "driverList" -> drivers.size
                                    else -> null
                                }
                            )
                        }
                        
                        // Bottom Padding for Grid
                        item(span = { GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FleetStatsHeader(vehicleCount: Int, driverCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            count = vehicleCount.toString(),
            label = "Vehicles",
            icon = Icons.Default.DirectionsCar,
            color = Color(0xFF00BFA5),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            count = driverCount.toString(),
            label = "Drivers",
            icon = Icons.Default.Badge,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(count: String, label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = count,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Surface(
                modifier = Modifier.size(40.dp).align(Alignment.TopEnd),
                shape = CircleShape,
                color = color.copy(alpha = 0.15f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TravelAnimationSection() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.travel_anim))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(130.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                "No Active Trips",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
            )
        }
    }
}

@Composable
fun ModernTripInfoCard(trip: Trip, onTrackClick: (String) -> Unit) {
    val accentTeal = Color(0xFF00BFA5)
    val cardBackground = Color(0xFF1B263B)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = accentTeal.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalShipping, 
                            contentDescription = null, 
                            tint = accentTeal,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "ACTIVE FLEET STATUS", 
                            fontWeight = FontWeight.Black, 
                            fontSize = 10.sp, 
                            color = accentTeal,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            "Vehicle En Route", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 18.sp, 
                            color = Color.White
                        )
                    }
                }
                
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                Surface(
                    color = Color(0xFFE53935).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .alpha(pulseAlpha)
                                .background(Color(0xFFE53935), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "LIVE", 
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DEPARTURE", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(trip.fromLocation, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.White)
                }
                
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward, 
                        contentDescription = null, 
                        tint = accentTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("DESTINATION", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(trip.toLocation, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.White, textAlign = TextAlign.End)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onTrackClick("liveTracking") },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF0D1B2A))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "TRACK REAL-TIME LOCATION", 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0D1B2A),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun ProfessionalActionTile(tile: DashboardTile, onClick: () -> Unit, count: Int? = null) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f)
    
    Card(
        modifier = Modifier
            .aspectRatio(1.05f)
            .scale(scale)
            .clickable { 
                isPressed = true
                onClick() 
            },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Decorative Icon
            Icon(
                tile.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp)
                    .alpha(0.05f)
                    .rotate(-15f),
                tint = tile.color
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = tile.color.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            tile.icon, 
                            contentDescription = tile.title, 
                            tint = tile.color, 
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    
                    if (count != null) {
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = count.toString(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Column {
                    Text(
                        text = tile.title, 
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 15.sp, 
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = tile.description, 
                        fontSize = 11.sp, 
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
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
