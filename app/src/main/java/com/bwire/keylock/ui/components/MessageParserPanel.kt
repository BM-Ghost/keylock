package com.bwire.keylock.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bwire.keylock.domain.crypto.MessageParserEngine
import com.bwire.keylock.domain.crypto.ParseMode
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Message Parser Panel
 * Parses and displays message data in various formats
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageParserPanel(
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(ParseMode.ATM_NDC) }
    var hexDataInput by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }
    
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
                text = "Message Parser",
                style = MaterialTheme.typography.titleLarge,
                color = NeonGreen
            )
            
            Divider(color = MediumGreen)
            
            // Parse Mode Selection
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Parse Mode",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedMode.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = MediumGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        ParseMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = mode.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selectedMode == mode) NeonGreen else TextPrimary
                                    )
                                },
                                onClick = {
                                    selectedMode = mode
                                    expandedDropdown = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = if (selectedMode == mode) NeonGreen else TextPrimary
                                )
                            )
                        }
                    }
                }
            }
            
            Divider(color = MediumGreen)
            
            // Hex Data Input
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Hex Data",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                OutlinedTextField(
                    value = hexDataInput,
                    onValueChange = { hexDataInput = it },
                    placeholder = {
                        Text(
                            "Enter hexadecimal data (e.g., 57652C206174204546544C61622C...)",
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
                    text = "[${hexDataInput.replace("\\s".toRegex(), "").length}]",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Divider(color = MediumGreen)
            
            // Parse Button
            Button(
                onClick = {
                    parseOperation(hexDataInput, selectedMode, onExecute)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = DarkestGreen
                ),
                enabled = hexDataInput.isNotBlank()
            ) {
                Text("PARSE")
            }
        }
    }
}

/**
 * Execute message parsing operation
 */
private fun parseOperation(
    hexData: String,
    mode: ParseMode,
    onExecute: (ConsoleMessage) -> Unit
) {
    if (hexData.isBlank()) {
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Error: Input data is empty"
        ))
        return
    }
    
    val result = MessageParserEngine.parse(hexData, mode)
    
    result.onSuccess { formattedOutput ->
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        
        val message = buildString {
            appendLine("[$timestamp]")
            appendLine("Message Parsing ${mode.displayName.replace(" ", "")}:")
            appendLine("****************************************")
            appendLine("Input Data:")
            append(formattedOutput)
        }
        
        onExecute(ConsoleMessage(
            level = MessageLevel.SUCCESS,
            message = message
        ))
    }.onFailure { error ->
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Error: ${error.message}"
        ))
    }
}
