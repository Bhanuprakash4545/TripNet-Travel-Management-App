package com.example.travelsapplication

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelsapplication.ui.theme.TravelsApplicationTheme

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(database.userDao(), database.driverDao())
    }
    private val vehicleViewModel: VehicleViewModel by viewModels {
        VehicleViewModelFactory(database.vehicleDao())
    }
    private val driverViewModel: DriverViewModel by viewModels {
        DriverViewModelFactory(database.driverDao())
    }
    private val tripViewModel: TripViewModel by viewModels {
        TripViewModelFactory(database.tripDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"

        enableEdgeToEdge()
        setContent {
            TravelsApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    val authResult by authViewModel.authResult.collectAsStateWithLifecycle()
                    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        if (!permissions.values.all { it }) {
                            Log.e("MainActivity", "Permissions not granted")
                        }
                    }

                    LaunchedEffect(Unit) {
                        val permissions = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        locationPermissionLauncher.launch(permissions.toTypedArray())
                    }

                    LaunchedEffect(authResult) {
                        when (authResult) {
                            is AuthResult.Success -> {
                                val destination = if (currentUser?.role == "Driver") "driverDashboard" else "dashboard"
                                navController.navigate(destination) {
                                    popUpTo(0) { inclusive = true }
                                }
                                authViewModel.resetAuthResult()
                            }
                            is AuthResult.OtpSent -> {
                                currentUser?.phoneNumber?.let { phone ->
                                    navController.navigate("otpVerification/$phone")
                                }
                                authViewModel.resetAuthResult()
                            }
                            is AuthResult.LoggedOut -> {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                                authViewModel.resetAuthResult()
                            }
                            else -> {}
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(onNavigateNext = { route ->
                                val finalRoute = if (route == "dashboard" && currentUser?.role == "Driver") "driverDashboard" else route
                                navController.navigate(finalRoute) {
                                    popUpTo("splash") { inclusive = true }
                                }
                            })
                        }
                        composable("login") {
                            LoginScreen(
                                onLoginClick = { phoneNumber: String ->
                                    authViewModel.onLogin(phoneNumber, this@MainActivity)
                                },
                                onDriverLoginClick = { phone: String, pin: String ->
                                    authViewModel.onDriverLogin(phone, pin, deviceId)
                                },
                                onRegisterClick = { navController.navigate("registration") },
                                authResult = authResult
                            )
                        }
                        composable("registration") {
                            RegistrationScreen(
                                onRegisterClick = { phoneNumber, travelsName, state, city ->
                                    authViewModel.onRegister(phoneNumber, travelsName, state, city, this@MainActivity)
                                },
                                onLoginClick = { navController.navigate("login") },
                                authResult = authResult
                            )
                        }
                        composable("otpVerification/{phoneNumber}") { backStackEntry ->
                            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                            OtpVerificationScreen(
                                phoneNumber = phoneNumber,
                                onVerifyClick = { otp -> authViewModel.verifyOtp(otp) },
                                authResult = authResult
                            )
                        }
                        composable("dashboard") {
                            DashboardScreen(
                                onTileClick = { route -> navController.navigate(route) },
                                tripViewModel = tripViewModel,
                                authViewModel = authViewModel,
                                vehicleViewModel = vehicleViewModel,
                                driverViewModel = driverViewModel,
                                onNavigateToPersonalInformation = { navController.navigate("personalInformation") },
                                onNavigateToAbout = { navController.navigate("about") }
                            )
                        }
                        composable("driverDashboard") {
                            currentUser?.let { user ->
                                DriverDashboardScreen(authViewModel = authViewModel, user = user)
                            }
                        }
                        composable("liveTracking") {
                            LiveTrackingScreen(onBackClick = { navController.popBackStack() })
                        }
                        composable("personalInformation") {
                            PersonalInformationScreen(
                                authViewModel = authViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("vehicleList") {
                            VehicleListScreen(
                                vehicleViewModel = vehicleViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("driverList") {
                            DriverListScreen(
                                driverViewModel = driverViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("tripList") {
                            val vehicles by vehicleViewModel.vehicles.collectAsStateWithLifecycle()
                            val drivers by driverViewModel.drivers.collectAsStateWithLifecycle()
                            TripListScreen(
                                tripViewModel = tripViewModel,
                                vehicles = vehicles,
                                drivers = drivers,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("tripRecords") {
                            val vehicles by vehicleViewModel.vehicles.collectAsStateWithLifecycle()
                            val drivers by driverViewModel.drivers.collectAsStateWithLifecycle()
                            TripRecordsScreen(
                                tripViewModel = tripViewModel,
                                authViewModel = authViewModel,
                                vehicles = vehicles,
                                drivers = drivers,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("incomeAnalysis") {
                            IncomeAnalysisScreen(
                                tripViewModel = tripViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("about") {
                            AboutScreen(onBackClick = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
