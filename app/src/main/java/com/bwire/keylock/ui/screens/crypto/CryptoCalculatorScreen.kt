package com.bwire.keylock.ui.screens.crypto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bwire.keylock.data.ConsoleLogRepository
import com.bwire.keylock.ui.navigation.*
import com.bwire.keylock.ui.theme.*
import com.bwire.keylock.ui.components.CryptoConsole
import com.bwire.keylock.ui.components.CryptoConsoleCollapsible
import com.bwire.keylock.ui.components.HashCalculatorPanel
import com.bwire.keylock.ui.components.CharacterEncodingPanel
import com.bwire.keylock.ui.components.BCDPanel
import com.bwire.keylock.ui.components.CheckDigitPanel
import com.bwire.keylock.ui.components.Base64Panel
import com.bwire.keylock.ui.components.Base94Panel
import com.bwire.keylock.ui.components.MessageParserPanel
import com.bwire.keylock.ui.components.RSADERPublicKeyPanel
import kotlinx.coroutines.launch

/**
 * Crypto Calculator Screen
 * Main screen for cryptographic operations
 * 
 * Menu structure:
 * - Generic (Hashes, Encoding, BCD, Base64, etc.)
 * - Cipher (AES, DES, RSA, ECC, ECDSA, FPE)
 * - Keys (DES/TDES/AES, HSM Keys, Key Blocks, etc.)
 * - Payments (CVV, MAC, PIN, DUKPT, etc.)
 * - EMV (Cryptograms, SDA, DDA, Secure Messaging, etc.)
 * - Development (Padding, Trace Parser, Bit Shift, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoCalculatorScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ConsoleLogRepository(context) }
    
    var selectedMenu by remember { mutableStateOf(CryptoMenu.GENERIC) }
    var selectedTool by remember { mutableStateOf<String?>(null) }
    val consoleMessages = remember { mutableStateListOf<ConsoleMessage>() }
    var isLogExpanded by remember { mutableStateOf(true) }
    
    // Load messages on first composition
    LaunchedEffect(Unit) {
        val savedMessages = repository.loadMessages()
        consoleMessages.clear()
        consoleMessages.addAll(savedMessages)
    }
    
    // Save messages whenever they change
    LaunchedEffect(consoleMessages.size) {
        if (consoleMessages.isNotEmpty()) {
            repository.saveMessages(consoleMessages.toList())
        }
    }
    
    Scaffold(
        topBar = {
            CryptoCalculatorTopBar(
                selectedMenu = selectedMenu,
                onMenuSelected = { 
                    selectedMenu = it
                    selectedTool = null
                },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tool selector dropdown
            ToolSelectorDropdown(
                menu = selectedMenu,
                selectedTool = selectedTool,
                onToolSelected = { selectedTool = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Divider(color = MediumGreen)
            
            // Main content area - Tool input panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (selectedTool != null) {
                    ToolConfigPanel(
                        menu = selectedMenu,
                        tool = selectedTool!!,
                        onExecute = { message ->
                            consoleMessages.add(message)
                            isLogExpanded = true // Auto-expand log on new message
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = MediumGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Select a tool from the dropdown above",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            Divider(color = MediumGreen)
            
            // Bottom: Collapsible Operation Log
            CryptoConsoleCollapsible(
                messages = consoleMessages,
                isExpanded = isLogExpanded,
                onToggleExpand = { isLogExpanded = !isLogExpanded },
                onClear = { 
                    consoleMessages.clear()
                    scope.launch {
                        repository.clearMessages()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CryptoCalculatorTopBar(
    selectedMenu: CryptoMenu,
    onMenuSelected: (CryptoMenu) -> Unit,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = { 
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    "KeyLock",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonGreen
                )
                Text(
                    "—",
                    style = MaterialTheme.typography.titleMedium,
                    color = MediumGreen
                )
                Text(
                    "Cryptographic Calculator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        actions = {
            // Menu navigation
            CryptoMenu.entries.forEach { menu ->
                FilterChip(
                    selected = selectedMenu == menu,
                    onClick = { onMenuSelected(menu) },
                    label = { 
                        Text(
                            menu.displayName,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonGreen,
                        selectedLabelColor = DarkestGreen,
                        containerColor = SurfaceMedium,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedMenu == menu,
                        borderColor = if (selectedMenu == menu) NeonGreen else MediumGreen,
                        selectedBorderColor = NeonGreen,
                        borderWidth = 1.dp
                    )
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolSelectorDropdown(
    menu: CryptoMenu,
    selectedTool: String?,
    onToolSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = when (menu) {
        CryptoMenu.GENERIC -> GenericTool.entries.map { it.displayName }
        CryptoMenu.CIPHER -> CipherTool.entries.map { it.displayName }
        CryptoMenu.KEYS -> KeysTool.entries.map { it.displayName }
        CryptoMenu.PAYMENTS -> PaymentsTool.entries.map { it.displayName }
        CryptoMenu.EMV -> EMVTool.entries.map { it.displayName }
        CryptoMenu.DEVELOPMENT -> DevelopmentTool.entries.map { it.displayName }
    }
    
    var expanded by remember { mutableStateOf(false) }
    
    // Reset expanded state when menu changes
    LaunchedEffect(menu) {
        expanded = false
    }
    
    Surface(
        modifier = modifier,
        color = SurfaceMedium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = MediumGreen,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = "${menu.displayName} Tools",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            
            Text(
                text = "(${tools.size})",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedTool ?: "Select a tool...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = MediumGreen,
                        focusedLabelColor = NeonGreen,
                        unfocusedTextColor = if (selectedTool == null) TextTertiary else TextPrimary,
                        focusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    tools.forEachIndexed { index, tool ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = tool,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (selectedTool == tool) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = NeonGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onToolSelected(tool)
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = if (selectedTool == tool) NeonGreen else TextPrimary
                            )
                        )
                        
                        if (index < tools.size - 1) {
                            HorizontalDivider(color = MediumGreen.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolConfigPanel(
    menu: CryptoMenu,
    tool: String,
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    // Route to specific tool panels based on menu and tool
    when (menu) {
        CryptoMenu.GENERIC -> {
            when (tool) {
                GenericTool.HASHES.displayName -> {
                    HashCalculatorPanel(
                        onExecute = onExecute,
                        modifier = modifier
                    )
                }
                GenericTool.CHARACTER_ENCODING.displayName -> {
                    CharacterEncodingPanel(
                        onExecute = onExecute,
                        modifier = modifier
                    )
                }
                GenericTool.BCD.displayName -> {
                    BCDPanel(
                        onExecute = onExecute,
                        modifier = modifier
                    )
                }
                GenericTool.CHECK_DIGITS.displayName -> {
                    CheckDigitPanel(
                        onExecute = onExecute,
                        modifier = modifier
                    )
                }
                GenericTool.BASE64.displayName -> {
                    Base64Panel(
                        onExecute = onExecute,
                        modifier = modifier
                    )
                }
                GenericTool.BASE94.displayName -> {
                    Base94Panel(
                        onExecute = onExecute,
                        modifier = modifier
                    )
                }
                GenericTool.MESSAGE_PARSER.displayName -> {
                    MessageParserPanel(
                        onExecute = onExecute,
                        modifier = modifier
                    )
                }
                GenericTool.RSA_DER_PUBLIC_KEY.displayName -> {
                    RSADERPublicKeyPanel(
                        onExecute = onExecute,
                        modifier = modifier
                    )
                }
                else -> PlaceholderPanel(tool, modifier)
            }
        }
        else -> PlaceholderPanel(tool, modifier)
    }
}

@Composable
private fun PlaceholderPanel(
    tool: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = DarkestGreen
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = tool,
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Configuration panel coming soon",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Console message data class
 */
data class ConsoleMessage(
    val timestamp: Long = System.currentTimeMillis(),
    val level: MessageLevel = MessageLevel.INFO,
    val message: String
)

enum class MessageLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}
