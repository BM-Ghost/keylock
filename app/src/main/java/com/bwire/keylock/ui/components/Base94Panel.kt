package com.bwire.keylock.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bwire.keylock.domain.crypto.Base94Engine
import com.bwire.keylock.domain.crypto.DataEncoding
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Base94 Panel
 * Handles Base94 encoding and decoding operations
 * 
 * Features:
 * - Encode: ASCII/Hexadecimal -> Base94
 * - Decode: Base94 -> ASCII
 */
@Composable
fun Base94Panel(
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0=Encode, 1=Decode
    var selectedEncoding by remember { mutableStateOf(DataEncoding.ASCII) }
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
                text = "Base94",
                style = MaterialTheme.typography.titleLarge,
                color = NeonGreen
            )
            
            Divider(color = MediumGreen)
            
            // Tabs: Encode | Decode
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkestGreen,
                contentColor = NeonGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Encode") },
                    selectedContentColor = NeonGreen,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Decode") },
                    selectedContentColor = NeonGreen,
                    unselectedContentColor = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Input Encoding Selection (only for Encode tab)
            if (selectedTab == 0) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Input Encoding",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DataEncoding.entries.forEach { encoding ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedEncoding == encoding,
                                    onClick = { selectedEncoding = encoding },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = NeonGreen,
                                        unselectedColor = MediumGreen
                                    )
                                )
                                Text(
                                    text = encoding.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
            
            // Data Input
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Data",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                OutlinedTextField(
                    value = dataInput,
                    onValueChange = { dataInput = it },
                    placeholder = { 
                        Text(
                            getPlaceholder(selectedTab, selectedEncoding),
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
                    text = "${dataInput.length} characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Divider(color = MediumGreen)
            
            // Execute Button
            Button(
                onClick = {
                    if (selectedTab == 0) {
                        encodeOperation(dataInput, selectedEncoding, onExecute)
                    } else {
                        decodeOperation(dataInput, onExecute)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = DarkestGreen
                ),
                enabled = dataInput.isNotBlank()
            ) {
                Text(if (selectedTab == 0) "ENCODE" else "DECODE")
            }
        }
    }
}

/**
 * Get placeholder text based on operation and encoding
 */
private fun getPlaceholder(tab: Int, encoding: DataEncoding): String {
    return if (tab == 0) {
        // Encode
        when (encoding) {
            DataEncoding.ASCII -> "Enter ASCII text (e.g., My name is Bwire)"
            DataEncoding.HEXADECIMAL -> "Enter hexadecimal data (e.g., 57652C206174204546544C61622C)"
        }
    } else {
        // Decode
        "Enter Base94 encoded data (e.g., mZM^7uhCz&o-c3.3f.k5)"
    }
}

/**
 * Execute Base94 encoding operation
 */
private fun encodeOperation(
    data: String,
    encoding: DataEncoding,
    onExecute: (ConsoleMessage) -> Unit
) {
    if (data.isBlank()) {
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Error: Input data is empty"
        ))
        return
    }
    
    val result = Base94Engine.encode(data, encoding)
    
    result.onSuccess { output ->
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val encodingName = encoding.displayName.uppercase()
        
        val message = buildString {
            appendLine("[$timestamp]")
            appendLine("Base94: Encoding finished")
            appendLine("****************************************")
            appendLine("Input Data ($encodingName):\t$data")
            appendLine("----------------------------------------")
            appendLine("Encoded Data:\t\t$output")
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

/**
 * Execute Base94 decoding operation
 */
private fun decodeOperation(
    data: String,
    onExecute: (ConsoleMessage) -> Unit
) {
    if (data.isBlank()) {
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Error: Input data is empty"
        ))
        return
    }
    
    val result = Base94Engine.decode(data)
    
    result.onSuccess { output ->
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        
        val message = buildString {
            appendLine("[$timestamp]")
            appendLine("Base94: Decoding finished")
            appendLine("****************************************")
            appendLine("Data:\t\t\t$data")
            appendLine("----------------------------------------")
            appendLine("Decoded Data:\t\t$output")
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
