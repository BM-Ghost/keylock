package com.bwire.keylock.ui.screens.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.bwire.keylock.ui.theme.*

/**
 * Lock Screen - Secure authentication for KeyLock
 * Implements master password/passphrase authentication
 * 
 * Future: Biometric authentication, hardware binding
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    onUnlock: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // TODO: Integrate with actual security layer
    val handleUnlock = {
        isLoading = true
        errorMessage = null
        
        // Temporary: Accept any non-empty password
        // In production: Verify against secure keystore
        if (password.isNotEmpty()) {
            if (password.length >= 8) {
                onUnlock()
            } else {
                errorMessage = "Password must be at least 8 characters"
                isLoading = false
            }
        } else {
            errorMessage = "Password required"
            isLoading = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceDark
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(64.dp)
                )
                
                Text(
                    text = "KeyLock",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                
                Text(
                    text = "Enter master password to unlock",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Master Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) 
                                    Icons.Default.VisibilityOff 
                                else 
                                    Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) 
                                    "Hide password" 
                                else 
                                    "Show password"
                            )
                        }
                    },
                    isError = errorMessage != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = MediumGreen,
                        focusedLabelColor = NeonGreen,
                        cursorColor = NeonGreen
                    )
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = handleUnlock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = DarkestGreen
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DarkestGreen
                        )
                    } else {
                        Text(
                            "UNLOCK",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "100% Offline • Zero Telemetry • Secure Local Storage",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}
