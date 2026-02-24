package com.bwire.keylock.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bwire.keylock.ui.screens.HomeScreen
import com.bwire.keylock.ui.screens.crypto.CryptoCalculatorScreen
import com.bwire.keylock.ui.screens.hsm.HSMCommanderScreen
import com.bwire.keylock.ui.screens.security.LockScreen

/**
 * Main navigation component for KeyLock
 * Handles navigation between major modules and screens
 */
@Composable
fun KeyLockNavigation() {
    val navController = rememberNavController()
    var isLocked by remember { mutableStateOf(false) }
    
    if (isLocked) {
        LockScreen(
            onUnlock = { isLocked = false }
        )
    } else {
        KeyLockNavHost(
            navController = navController,
            onLock = { isLocked = true }
        )
    }
}

@Composable
private fun KeyLockNavHost(
    navController: NavHostController,
    onLock: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToCrypto = { 
                    navController.navigate(Screen.CryptoCalculator.route)
                },
                onNavigateToHSM = {
                    navController.navigate(Screen.HSMCommander.route)
                },
                onNavigateToKeyVault = {
                    navController.navigate(Screen.KeyVault.route)
                },
                onNavigateToLogs = {
                    navController.navigate(Screen.AuditLogs.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onLock = onLock
            )
        }
        
        composable(Screen.CryptoCalculator.route) {
            CryptoCalculatorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.HSMCommander.route) {
            HSMCommanderScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.KeyVault.route) {
            // TODO: Implement Key Vault screen
            PlaceholderScreen(
                title = "Key Vault",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.AuditLogs.route) {
            // TODO: Implement Audit Logs screen
            PlaceholderScreen(
                title = "Audit Logs",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            // TODO: Implement Settings screen
            PlaceholderScreen(
                title = "Settings",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(
    title: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("$title - Coming Soon")
        }
    }
}
