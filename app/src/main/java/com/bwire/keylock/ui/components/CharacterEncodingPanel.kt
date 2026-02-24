package com.bwire.keylock.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bwire.keylock.domain.crypto.CharacterEncodingEngine
import com.bwire.keylock.domain.crypto.CharacterEncodingType
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Character Encoding Panel
 * Handles character encoding conversions
 * 
 * Supported conversions:
 * - Binary <-> Hexadecimal
 * - ASCII <-> EBCDIC
 * - ASCII Text -> Hexadecimal
 * - ATM ASCII Decimal <-> Hexadecimal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterEncodingPanel(
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedEncoding by remember { mutableStateOf(CharacterEncodingType.HEX_TO_BINARY) }
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
                text = "Character Encoding",
                style = MaterialTheme.typography.titleLarge,
                color = NeonGreen
            )
            
            Divider(color = MediumGreen)
            
            // Encoding Type Selection
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Encoding Direction",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                var encodingExpanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = encodingExpanded,
                    onExpandedChange = { encodingExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedEncoding.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = encodingExpanded)
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
                        expanded = encodingExpanded,
                        onDismissRequest = { encodingExpanded = false }
                    ) {
                        CharacterEncodingType.entries.forEach { encodingType ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = encodingType.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedEncoding = encodingType
                                    encodingExpanded = false
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
                    onValueChange = { dataInput = it },
                    placeholder = { 
                        Text(
                            getPlaceholder(selectedEncoding),
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
                    executeConversion(
                        selectedEncoding,
                        dataInput,
                        onExecute
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = DarkestGreen
                ),
                enabled = dataInput.isNotBlank()
            ) {
                Text("CONVERT")
            }
        }
    }
}

/**
 * Get placeholder text based on encoding type
 */
private fun getPlaceholder(encodingType: CharacterEncodingType): String {
    return when (encodingType) {
        CharacterEncodingType.BINARY_TO_HEX -> "Enter text to convert to hex (e.g., Hello)"
        CharacterEncodingType.HEX_TO_BINARY -> "Enter hexadecimal string (e.g., 48656C6C6F)"
        CharacterEncodingType.ASCII_TO_EBCDIC -> "Enter ASCII text"
        CharacterEncodingType.EBCDIC_TO_ASCII -> "Enter EBCDIC hex string"
        CharacterEncodingType.ASCII_TO_HEX -> "Enter ASCII text"
        CharacterEncodingType.ATM_ASCII_DEC_TO_HEX -> "Enter ATM ASCII decimal (e.g., 065066067)"
        CharacterEncodingType.HEX_TO_ATM_ASCII_DEC -> "Enter hexadecimal string (e.g., 414243)"
    }
}

/**
 * Execute character encoding conversion
 */
private fun executeConversion(
    encodingType: CharacterEncodingType,
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
    
    val result = CharacterEncodingEngine.convert(encodingType, data)
    
    result.onSuccess { output ->
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        
        val message = buildString {
            appendLine("[$timestamp]")
            appendLine("Character Encoding: Encoding done")
            appendLine("Direction: ${encodingType.displayName}")
            appendLine("****************************************")
            appendLine("Data In:\t\t$data")
            appendLine("----------------------------------------")
            appendLine("Data Out:\t\t$output")
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
