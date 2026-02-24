package com.bwire.keylock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bwire.keylock.ui.theme.*

/**
 * Home Screen - Main dashboard for KeyLock Pro
 * Displays module cards for quick access to major functions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCrypto: () -> Unit,
    onNavigateToHSM: () -> Unit,
    onNavigateToKeyVault: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLock: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "KeyLock Pro",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    // Status indicators
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        tint = StatusOffline,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onLock) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = MutedGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(240.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(getModules(
                onNavigateToCrypto,
                onNavigateToHSM,
                onNavigateToKeyVault,
                onNavigateToLogs,
                onNavigateToSettings
            )) { module ->
                ModuleCard(module)
            }
        }
    }
}

@Composable
private fun StatusIndicator(
    icon: ImageVector,
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleCard(module: ModuleInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = module.onClick),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceMedium
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = module.icon,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(36.dp)
            )
            
            Column {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2
                )
            }
        }
    }
}

private data class ModuleInfo(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

private fun getModules(
    onNavigateToCrypto: () -> Unit,
    onNavigateToHSM: () -> Unit,
    onNavigateToKeyVault: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToSettings: () -> Unit
): List<ModuleInfo> = listOf(
    ModuleInfo(
        title = "Crypto Calculator",
        description = "Cryptographic operations, key derivation, and validation tools",
        icon = Icons.Default.Calculate,
        onClick = onNavigateToCrypto
    ),
    ModuleInfo(
        title = "HSM Commander",
        description = "Hardware Security Module command simulation and testing",
        icon = Icons.Default.Computer,
        onClick = onNavigateToHSM
    ),
    ModuleInfo(
        title = "Key Vault",
        description = "Secure key storage and management",
        icon = Icons.Default.VpnKey,
        onClick = onNavigateToKeyVault
    ),
    ModuleInfo(
        title = "Audit Logs",
        description = "Encrypted operation logs and audit trail",
        icon = Icons.Default.Description,
        onClick = onNavigateToLogs
    ),
    ModuleInfo(
        title = "Settings",
        description = "Application configuration and security settings",
        icon = Icons.Default.Settings,
        onClick = onNavigateToSettings
    )
)
