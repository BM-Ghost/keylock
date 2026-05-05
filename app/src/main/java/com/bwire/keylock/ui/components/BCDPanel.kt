package com.bwire.keylock.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bwire.keylock.domain.crypto.BCDEngine
import com.bwire.keylock.domain.crypto.BCDFormat
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Binary Coded Decimal (BCD) Panel
 * Handles BCD encoding and decoding operations
 * 
 * Features:
 * - Encode: Decimal -> BCD (Binary or Hex)
 * - Decode: BCD (Binary or Hex) -> Decimal
 */
@Composable
fun BCDPanel(
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0=Encode, 1=Decode
    var selectedFormat by remember { mutableStateOf(BCDFormat.BINARY) }
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
                text = "Binary Coded Decimal",
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
            
            // Format Selection (Radio Buttons)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Format",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BCDFormat.entries.forEach { format ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFormat == format,
                                onClick = { selectedFormat = format },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeonGreen,
                                    unselectedColor = MediumGreen
                                )
                            )
                            Text(
                                text = format.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
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
                    text = "Data",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                OutlinedTextField(
                    value = dataInput,
                    onValueChange = { dataInput = it },
                    placeholder = { 
                        Text(
                            getPlaceholder(selectedTab, selectedFormat),
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
                        encodeOperation(dataInput, selectedFormat, onExecute)
                    } else {
                        decodeOperation(dataInput, selectedFormat, onExecute)
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
 * Get placeholder text based on operation and format
 */
private fun getPlaceholder(tab: Int, format: BCDFormat): String {
    return if (tab == 0) {
        // Encode
        "Enter decimal number (e.g., 25)"
    } else {
        // Decode
        when (format) {
            BCDFormat.BINARY -> "Enter binary BCD (e.g., 0010 0101)"
            BCDFormat.HEXADECIMAL -> "Enter hex BCD (e.g., 0010 0101)"
        }
    }
}

/**
 * Execute BCD encoding operation
 */
private fun encodeOperation(
    data: String,
    format: BCDFormat,
    onExecute: (ConsoleMessage) -> Unit
) {
    if (data.isBlank()) {
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Error: Input data is empty"
        ))
        return
    }
    
    val result = BCDEngine.encode(data, format)
    
    result.onSuccess { output ->
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val formatName = format.displayName.lowercase()
        
        val message = buildString {
            appendLine("[$timestamp]")
            appendLine("BCD: Encoding finished")
            appendLine("****************************************")
            appendLine("Data (decimal):\t\t$data")
            appendLine("----------------------------------------")
            appendLine("Encoded Data ($formatName):\t$output")
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
 * Execute BCD decoding operation
 */
private fun decodeOperation(
    data: String,
    format: BCDFormat,
    onExecute: (ConsoleMessage) -> Unit
) {
    if (data.isBlank()) {
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Error: Input data is empty"
        ))
        return
    }
    
    val result = BCDEngine.decode(data, format)
    
    result.onSuccess { output ->
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val formatName = format.displayName.lowercase()
        
        val message = buildString {
            appendLine("[$timestamp]")
            appendLine("BCD: Decoding finished")
            appendLine("****************************************")
            appendLine("Data ($formatName):\t\t\t$data")
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
