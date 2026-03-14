package com.example.travelsapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: (String) -> Unit,
    onDriverLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    authResult: AuthResult
) {
    var phoneNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var isDriverMode by remember { mutableStateOf(false) }

    val darkBackgroundColor = Color(0xFF0D1B2A)
    val accentColor = Color(0xFF00BFA5)
    val textColor = Color.White
    val hintColor = Color.Gray
    val isLoading = authResult is AuthResult.Loading

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Box(modifier = Modifier.height(180.dp), contentAlignment = Alignment.Center) {
                    TripNetLoader()
                }
            } else {
                Spacer(modifier = Modifier.height(60.dp))
                Text(
                    text = "TripNet",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(60.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isDriverMode) "Driver PIN Login" else "Admin Secure Login",
                fontSize = 14.sp,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (authResult is AuthResult.Error) {
                Text(
                    text = authResult.message,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Mobile Number") },
                leadingIcon = { Text("+91 ", color = textColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = hintColor,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.Gray
                )
            )

            if (isDriverMode) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it },
                    label = { Text("4-Digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = accentColor,
                        focusedLabelColor = accentColor,
                        unfocusedLabelColor = hintColor,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { 
                    if (isDriverMode) {
                        onDriverLoginClick(phoneNumber, pin)
                    } else {
                        onLoginClick(phoneNumber) 
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading && phoneNumber.isNotEmpty() && (!isDriverMode || pin.length == 4),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(if (isDriverMode) "Login Now" else "Get OTP", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = darkBackgroundColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Switcher
            TextButton(onClick = { isDriverMode = !isDriverMode }) {
                Text(
                    text = if (isDriverMode) "Back to Admin Login" else "Login as Driver (Quick PIN)",
                    color = accentColor
                )
            }

            TextButton(onClick = onRegisterClick, enabled = !isLoading) {
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = hintColor)) {
                        append("Don\'t have an account? ")
                    }
                    withStyle(style = SpanStyle(color = accentColor, fontWeight = FontWeight.Bold)) {
                        append("Register here")
                    }
                })
            }
        }

        Text(
            text = "By continuing, you agree to our Terms & Privacy Policy",
            color = hintColor,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        )
    }
}
