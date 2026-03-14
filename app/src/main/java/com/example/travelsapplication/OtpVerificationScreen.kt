package com.example.travelsapplication

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelsapplication.ui.theme.TravelsApplicationTheme

@Composable
fun OtpVerificationScreen(
    modifier: Modifier = Modifier,
    onVerifyClick: (String) -> Unit,
    phoneNumber: String,
    authResult: AuthResult = AuthResult.Initial
) {
    var otp by remember { mutableStateOf("") }

    val darkBackgroundColor = Color(0xFF0D1B2A)
    val accentColor = Color(0xFF00BFA5)
    val textColor = Color.White
    val hintColor = Color.Gray

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.tripnet1),
                contentDescription = "App Logo",
                modifier = Modifier.height(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Travels Application",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter the 6-digit code sent to $phoneNumber",
                fontSize = 14.sp,
                color = hintColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = { Text("Enter 6-digit OTP") },
                leadingIcon = { Icon(Icons.Default.Password, contentDescription = "OTP Icon", tint = textColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    cursorColor = accentColor,
                    focusedIndicatorColor = accentColor,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = hintColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                )
            )

            if (authResult is AuthResult.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = authResult.message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { 
                    if (otp.length == 6) {
                        Log.d("OtpVerificationScreen", "Verify button clicked with OTP: $otp")
                        onVerifyClick(otp)
                    }
                },
                enabled = otp.length == 6 && authResult !is AuthResult.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = accentColor.copy(alpha = 0.5f)
                )
            ) {
                if (authResult is AuthResult.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = darkBackgroundColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Verify & Proceed", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = darkBackgroundColor)
                }
            }
        }

        Text(
            text = "By continuing, you agree to our Terms & Privacy Policy",
            color = hintColor,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OtpVerificationScreenPreview() {
    TravelsApplicationTheme {
        OtpVerificationScreen(onVerifyClick = {}, phoneNumber = "+91 1234567890")
    }
}
