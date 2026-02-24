package com.bwire.keylock.ui.screens.hsm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.bwire.keylock.ui.navigation.HSMVendor
import com.bwire.keylock.ui.theme.*

/**
 * HSM Commander Screen
 * Offline HSM simulation engine
 * 
 * Supports:
 * - Atalla HSM commands
 * - SafeNet HSM commands  
 * - Thales HSM commands (complete command set)
 * 
 * Features:
 * - Command console with autocomplete
 * - Structured request/response parsing
 * - Encrypted audit logging
 * - Load testing capability
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HSMCommanderScreen(
    onNavigateBack: () -> Unit
) {
    var selectedVendor by remember { mutableStateOf(HSMVendor.THALES) }
    var selectedCommand by remember { mutableStateOf<HSMCommandInfo?>(null) }
    var commandInput by remember { mutableStateOf("") }
    val commandHistory = remember { mutableStateListOf<HSMCommand>() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "HSM Commander",
                        style = MaterialTheme.typography.titleMedium
                    )
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
                    IconButton(onClick = { /* TODO: Save log */ }) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { commandHistory.clear() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Vendor selection
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceMedium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Vendor:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                    )
                    
                    HSMVendor.entries.forEach { vendor ->
                        FilterChip(
                            selected = selectedVendor == vendor,
                            onClick = { selectedVendor = vendor },
                            label = { 
                                Text(
                                    vendor.displayName,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkGreen,
                                selectedLabelColor = NeonGreen
                            )
                        )
                    }
                }
            }
            
            Divider(color = MediumGreen)
            
            // Main content area - Console and Input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Command history/output
                HSMCommandConsole(
                    commands = commandHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                
                Divider(color = MediumGreen)
                
                // Command input area with dropdown
                HSMCommandInput(
                    vendor = selectedVendor,
                    selectedCommand = selectedCommand,
                    onCommandSelect = { 
                        selectedCommand = it
                        commandInput = it.code
                    },
                    command = commandInput,
                    onCommandChange = { commandInput = it },
                    onExecute = {
                        if (commandInput.isNotBlank()) {
                            val response = executeHSMCommand(selectedVendor, commandInput)
                            commandHistory.add(
                                HSMCommand(
                                    vendor = selectedVendor,
                                    request = commandInput,
                                    response = response
                                )
                            )
                            commandInput = ""
                            selectedCommand = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HSMCommandConsole(
    commands: List<HSMCommand>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ConsoleBackground
    ) {
        if (commands.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "HSM Command Console Ready\nEnter commands below",
                    style = ConsoleMediumTextStyle,
                    color = TextTertiary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(commands) { cmd ->
                    HSMCommandItem(cmd)
                }
            }
        }
    }
}

@Composable
private fun HSMCommandItem(command: HSMCommand) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = command.vendor.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = formatTimestamp(command.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = ConsoleTimestamp,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "→ ${command.request}",
                style = ConsoleMediumTextStyle,
                color = NeonGreen,
                fontFamily = FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "← ${command.response}",
                style = ConsoleMediumTextStyle,
                color = if (command.response.startsWith("ND") || command.response.startsWith("NE")) 
                    ConsoleError 
                else 
                    ConsoleSuccess,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HSMCommandInput(
    vendor: HSMVendor,
    selectedCommand: HSMCommandInfo?,
    onCommandSelect: (HSMCommandInfo) -> Unit,
    command: String,
    onCommandChange: (String) -> Unit,
    onExecute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val commands = when (vendor) {
        HSMVendor.THALES -> getThalesCommands()
        HSMVendor.ATALLA -> getAtallaCommands()
        HSMVendor.SAFENET -> getSafeNetCommands()
    }
    
    var expanded by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier,
        color = SurfaceMedium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Command dropdown selector
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCommand?.let { "${it.code} - ${it.description}" } ?: "Select command...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("${vendor.displayName} Commands") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = MediumGreen,
                        focusedLabelColor = NeonGreen
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    commands.forEach { cmd ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = cmd.code,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = NeonGreen,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = cmd.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            },
                            onClick = {
                                onCommandSelect(cmd)
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = TextPrimary
                            )
                        )
                    }
                }
            }
            
            // Command input and execute
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = command,
                    onValueChange = onCommandChange,
                    label = { Text("Command") },
                    placeholder = { Text("Enter or modify command...") },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = MediumGreen,
                        focusedLabelColor = NeonGreen
                    ),
                    singleLine = true
                )
                
                Button(
                    onClick = onExecute,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = DarkestGreen
                    ),
                    enabled = command.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Execute"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXECUTE")
                }
            }
        }
    }
}

// Data models
data class HSMCommand(
    val timestamp: Long = System.currentTimeMillis(),
    val vendor: HSMVendor,
    val request: String,
    val response: String
)

data class HSMCommandInfo(
    val code: String,
    val description: String
)

// Helper functions
private fun formatTimestamp(timestamp: Long): String {
    val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
    return format.format(java.util.Date(timestamp))
}

private fun executeHSMCommand(vendor: HSMVendor, command: String): String {
    // TODO: Implement actual HSM command simulation
    // For now, return placeholder responses
    
    val cmdCode = command.take(2).uppercase()
    
    return when (vendor) {
        HSMVendor.THALES -> executeThalesCommand(cmdCode, command)
        HSMVendor.ATALLA -> "NE - Command simulation pending"
        HSMVendor.SAFENET -> "NE - Command simulation pending"
    }
}

private fun executeThalesCommand(code: String, fullCommand: String): String {
    // TODO: Implement full Thales command set
    return when (code) {
        "NO" -> "NP00${getSimulatedSerialNumber()}"
        "A0" -> "NE15" // Not implemented yet
        else -> "ND00" // Invalid command code
    }
}

private fun getSimulatedSerialNumber(): String {
    return "SIM-KEYLOCK-001"
}

private fun getThalesCommands(): List<HSMCommandInfo> {
    return listOf(
        HSMCommandInfo("NO", "Perform Diagnostics"),
        HSMCommandInfo("A0", "Generate Key"),
        HSMCommandInfo("A4", "Generate Key – EMV Method"),
        HSMCommandInfo("A6", "Import Key"),
        HSMCommandInfo("A8", "Export Key"),
        HSMCommandInfo("AE", "Generate Key Set"),
        HSMCommandInfo("AG", "Generate AES Key"),
        HSMCommandInfo("AQ", "Generate Hash"),
        HSMCommandInfo("AU", "Generate and Print Component"),
        HSMCommandInfo("AW", "Form AES Key from Components"),
        HSMCommandInfo("B0", "Generate CVV / CVC / CVV2"),
        HSMCommandInfo("B2", "Verify CVV / CVC / CVV2"),
        HSMCommandInfo("BA", "Generate ARQC"),
        HSMCommandInfo("BC", "Translate BDK (ZPK to LMK)"),
        HSMCommandInfo("BE", "Translate BDK (LMK to ZPK)"),
        HSMCommandInfo("BI", "Encrypt Data Block"),
        HSMCommandInfo("BK", "Generate MAC"),
        HSMCommandInfo("BU", "Generate MAC (EMV 4.0/4.1)"),
        HSMCommandInfo("C0", "Generate MAC – ISO 9797-1"),
        HSMCommandInfo("C2", "Verify MAC – ISO 9797-1 (Data)"),
        HSMCommandInfo("C4", "Verify MAC – ISO 9797-1 (PIN)"),
        HSMCommandInfo("C6", "Generate HMAC"),
        HSMCommandInfo("CA", "Translate PIN (ZPK to ZPK)"),
        HSMCommandInfo("CC", "Translate PIN (ZPK to LMK)"),
        HSMCommandInfo("CG", "Verify Interchange PIN"),
        HSMCommandInfo("CI", "Generate ARPC"),
        HSMCommandInfo("CK", "Verify ARQC"),
        HSMCommandInfo("CM", "Verify ARQC (EMV 4.0/4.1)"),
        HSMCommandInfo("CW", "Translate PIN (ZPK to ZPK) - Enhanced"),
        HSMCommandInfo("CY", "Verify PIN - Offset"),
        HSMCommandInfo("D0", "Verify Terminal PIN"),
        HSMCommandInfo("D2", "Generate Offset"),
        HSMCommandInfo("EI", "Verify CVV"),
        HSMCommandInfo("EW", "Generate PVV"),
        HSMCommandInfo("EY", "Verify PVV"),
        HSMCommandInfo("FC", "Verify Terminal MAC"),
        HSMCommandInfo("FE", "Generate DUKPT Key"),
        HSMCommandInfo("FG", "Verify DUKPT MAC"),
        HSMCommandInfo("FI", "Translate DUKPT PIN to ZPK"),
        HSMCommandInfo("FK", "Verify DUKPT Transaction MAC"),
        HSMCommandInfo("FM", "Verify DUKPT PIN"),
        HSMCommandInfo("FQ", "Generate DUKPT PIN Block"),
        HSMCommandInfo("FS", "Derive DUKPT Key"),
        HSMCommandInfo("FW", "Verify DUKPT PIN (Visa Method)"),
        HSMCommandInfo("G0", "RSA Encrypt"),
        HSMCommandInfo("GC", "RSA Sign Hash"),
        HSMCommandInfo("GI", "RSA Decrypt"),
        HSMCommandInfo("GK", "RSA Verify Signature"),
        HSMCommandInfo("GM", "Derive ECC Key Pair"),
        HSMCommandInfo("GO", "ECDSA Sign"),
        HSMCommandInfo("GQ", "ECDSA Verify"),
        HSMCommandInfo("GU", "ECC Diffie-Hellman"),
        HSMCommandInfo("GW", "Generate ECDSA Key Pair"),
        HSMCommandInfo("GY", "Import ECC Public Key"),
        HSMCommandInfo("H0", "Encrypt Data"),
        HSMCommandInfo("H8", "Decrypt Data"),
        HSMCommandInfo("HA", "Generate VISA CVV"),
        HSMCommandInfo("HC", "Verify VISA CVV"),
        HSMCommandInfo("IA", "Generate/Verify MAC (Retail)"),
        HSMCommandInfo("JA", "Translate PIN Block"),
        HSMCommandInfo("JC", "Translate PIN (LMK to ZPK)"),
        HSMCommandInfo("JE", "Translate PIN (ZPK to LMK)"),
        HSMCommandInfo("JG", "Translate PIN Block (AES)"),
        HSMCommandInfo("KA", "Import Key Block"),
        HSMCommandInfo("KQ", "Export Key Block"),
        HSMCommandInfo("KS", "Translate Key Scheme"),
        HSMCommandInfo("KW", "Generate TR-31 Key Block"),
        HSMCommandInfo("L0", "Generate Load File"),
        HSMCommandInfo("LG", "Generate Session Keys"),
        HSMCommandInfo("M0", "PIN Change/Unblock"),
        HSMCommandInfo("M2", "Verify Interchange PIN"),
        HSMCommandInfo("M4", "Verify Offset"),
        HSMCommandInfo("M6", "Derive ICC Master Key"),
        HSMCommandInfo("M8", "Derive ICC Session Key"),
        HSMCommandInfo("MG", "Generate Session Key"),
        HSMCommandInfo("MI", "Derive Intermediate BDK"),
        HSMCommandInfo("MS", "Perform Script Update"),
        HSMCommandInfo("MY", "Verify Password"),
        HSMCommandInfo("N0", "Perform Diagnostics"),
        HSMCommandInfo("NC", "Console Command"),
        HSMCommandInfo("NG", "Generate Random Number"),
        HSMCommandInfo("NK", "Export Key (TR-31)"),
        HSMCommandInfo("OI", "Generate Key (TR-31)"),
        HSMCommandInfo("OK", "Import Key (TR-31)"),
        HSMCommandInfo("OU", "Translate ZEK to ZPK"),
        HSMCommandInfo("OW", "Translate ZPK to ZEK"),
        HSMCommandInfo("PA", "Print Key Component"),
        HSMCommandInfo("PE", "Encrypt PIN"),
        HSMCommandInfo("PI", "Translate PIN"),
        HSMCommandInfo("PM", "Verify DES Key Check Value"),
        HSMCommandInfo("PO", "Generate Decimalization Table"),
        HSMCommandInfo("PU", "Encrypt Data (EMV)"),
        HSMCommandInfo("PW", "Decrypt Data (EMV)")
    )
}

private fun getAtallaCommands(): List<HSMCommandInfo> {
    return listOf(
        HSMCommandInfo("ID", "Perform Diagnostics"),
        HSMCommandInfo("PE", "PIN Encrypt"),
        HSMCommandInfo("PD", "PIN Decrypt"),
        HSMCommandInfo("PT", "PIN Translate"),
        HSMCommandInfo("PV", "PIN Verify"),
        HSMCommandInfo("KG", "Key Generate"),
        HSMCommandInfo("KT", "Key Translate"),
        HSMCommandInfo("MG", "MAC Generate"),
        HSMCommandInfo("MV", "MAC Verify")
    )
}

private fun getSafeNetCommands(): List<HSMCommandInfo> {
    return listOf(
        HSMCommandInfo("EI", "Encrypt Initialization"),
        HSMCommandInfo("EK", "Encrypt Key"),
        HSMCommandInfo("EP", "Encrypt PIN"),
        HSMCommandInfo("KG", "Generate Key"),
        HSMCommandInfo("KI", "Import Key"),
        HSMCommandInfo("KE", "Export Key"),
        HSMCommandInfo("MG", "Generate MAC"),
        HSMCommandInfo("MV", "Verify MAC"),
        HSMCommandInfo("PV", "Verify PIN")
    )
}
