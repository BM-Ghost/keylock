package com.bwire.keylock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.bwire.keylock.ui.navigation.KeyLockNavigation
import com.bwire.keylock.ui.theme.KeyLockTheme

/**
 * KeyLock Pro - Professional Offline Cryptographic & HSM Simulation Application
 * 
 * Main Activity - Entry point for the application
 * 100% offline operation with zero telemetry or cloud connectivity
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            KeyLockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    KeyLockNavigation()
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // TODO: Implement auto-lock timer
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // TODO: Implement secure memory zeroization
    }
}
