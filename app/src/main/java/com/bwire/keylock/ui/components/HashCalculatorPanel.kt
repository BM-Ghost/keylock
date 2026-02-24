package com.bwire.keylock.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bwire.keylock.domain.crypto.DataEncoding
import com.bwire.keylock.domain.crypto.HashAlgorithm
import com.bwire.keylock.domain.crypto.HashEngine
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.*
import com.bwire.keylock.util.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Hash Calculator Panel
 * Calculate cryptographic hashes: MD4, MD5, SHA-1, SHA-2, SHA-3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashCalculatorPanel(
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedEncoding by remember { mutableStateOf(DataEncoding.HEXADECIMAL) }
    var selectedHashType by remember { mutableStateOf(HashAlgorithm.MD5) }
    var dataInput by remember { mutableStateOf("") }
    
    Surface(
        modifier = modifier,
        color = DarkestGreen
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Hash Calculator",
                style = MaterialTheme.typography.titleLarge,
                color = NeonGreen
            )
            
            Divider(color = MediumGreen)
            
            // Data Encoding Selection
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Data Encoding",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DataEncoding.entries.forEach { encoding ->
                        FilterChip(
                            selected = selectedEncoding == encoding,
                            onClick = { selectedEncoding = encoding },
                            label = { 
                                Text(encoding.name)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkGreen,
                                selectedLabelColor = NeonGreen
                            )
                        )
                    }
                }
            }
            
            // Hash Type Selection
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Hash Type",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                var hashTypeExpanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = hashTypeExpanded,
                    onExpandedChange = { hashTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedHashType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = hashTypeExpanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = MediumGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = hashTypeExpanded,
                        onDismissRequest = { hashTypeExpanded = false }
                    ) {
                        HashAlgorithm.entries.forEach { hashType ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = hashType.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedHashType = hashType
                                    hashTypeExpanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
            
            // Data Input
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Input Data",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                OutlinedTextField(
                    value = dataInput,
                    onValueChange = { dataInput = it.uppercase() },
                    placeholder = { 
                        Text(
                            if (selectedEncoding == DataEncoding.HEXADECIMAL) 
                                "Enter hexadecimal data..." 
                            else 
                                "Enter ASCII text...",
                            color = TextTertiary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = MediumGreen,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    minLines = 4
                )
                
                // Character count
                Text(
                    text = when (selectedEncoding) {
                        DataEncoding.HEXADECIMAL -> "${dataInput.length} hex chars (${dataInput.length / 2} bytes)"
                        DataEncoding.ASCII -> "${dataInput.length} characters"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        executeHashOperation(
                            encoding = selectedEncoding,
                            hashType = selectedHashType,
                            dataInput = dataInput,
                            onExecute = onExecute
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = DarkestGreen
                    ),
                    enabled = dataInput.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CALCULATE HASH")
                }
                
                OutlinedButton(
                    onClick = {
                        dataInput = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CLEAR")
                }
            }
        }
    }
}

/**
 * Execute hash operation and generate formatted console output
 */
private fun executeHashOperation(
    encoding: DataEncoding,
    hashType: HashAlgorithm,
    dataInput: String,
    onExecute: (ConsoleMessage) -> Unit
) {
    try {
        // Validate and convert input
        val dataBytes = when (encoding) {
            DataEncoding.HEXADECIMAL -> {
                if (!dataInput.isValidHex()) {
                    onExecute(ConsoleMessage(
                        level = MessageLevel.ERROR,
                        message = "Hashes: Invalid hexadecimal input"
                    ))
                    return
                }
                dataInput.hexToByteArray()
            }
            DataEncoding.ASCII -> dataInput.toByteArray(Charsets.UTF_8)
        }
        
        // Calculate hash
        val result = HashEngine.hash(hashType, dataBytes)
        
        result.fold(
            onSuccess = { hash ->
                val hashHex = hash.toHexString()
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date())
                
                // Format output to match the user's expected format
                val formattedOutput = buildString {
                    appendLine("Hashes: Hashing operation finished")
                    appendLine("****************************************")
                    appendLine("Data:\t\t\t${dataInput.uppercase()}")
                    appendLine("Hash type:\t\t${hashType.displayName}")
                    appendLine("----------------------------------------")
                    appendLine("Hash:\t\t\t$hashHex")
                }
                
                onExecute(ConsoleMessage(
                    level = MessageLevel.SUCCESS,
                    message = formattedOutput
                ))
            },
            onFailure = { error ->
                onExecute(ConsoleMessage(
                    level = MessageLevel.ERROR,
                    message = "Hashes: Error - ${error.message}"
                ))
            }
        )
    } catch (e: Exception) {
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Hashes: Error - ${e.message}"
        ))
    }
}
