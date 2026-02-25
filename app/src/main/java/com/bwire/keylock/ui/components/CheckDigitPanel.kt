package com.bwire.keylock.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bwire.keylock.domain.crypto.CheckDigitEngine
import com.bwire.keylock.domain.crypto.CheckDigitMethod
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Check Digits Panel
 * Handles check digit generation and validation
 * 
 * Features:
 * - Generate: Calculate check digit for input data
 * - Check: Validate check digit in input data
 * - Methods: Luhn (MOD 10), Amex SE Number (MOD 9)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckDigitPanel(
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(1) } // 0=Check, 1=Generate
    var selectedMethod by remember { mutableStateOf(CheckDigitMethod.LUHN) }
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
                text = "Check Digits",
                style = MaterialTheme.typography.titleLarge,
                color = NeonGreen
            )
            
            Divider(color = MediumGreen)
            
            // Tabs: Check | Generate
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkestGreen,
                contentColor = NeonGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Check") },
                    selectedContentColor = NeonGreen,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Generate") },
                    selectedContentColor = NeonGreen,
                    unselectedContentColor = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Method Selection (Dropdown)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Hash type",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                var methodExpanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedMethod.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded)
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
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false }
                    ) {
                        CheckDigitMethod.entries.forEach { method ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = method.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedMethod = method
                                    methodExpanded = false
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
                    text = "Input",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                OutlinedTextField(
                    value = dataInput,
                    onValueChange = { 
                        // Only allow digits
                        if (it.all { char -> char.isDigit() || char.isWhitespace() }) {
                            dataInput = it
                        }
                    },
                    placeholder = { 
                        Text(
                            if (selectedTab == 1) 
                                "Enter number to generate check digit (e.g., 79927398713)"
                            else
                                "Enter number with check digit to validate",
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
                    if (selectedTab == 1) {
                        generateOperation(dataInput, selectedMethod, onExecute)
                    } else {
                        checkOperation(dataInput, selectedMethod, onExecute)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = DarkestGreen
                ),
                enabled = dataInput.isNotBlank()
            ) {
                Text(if (selectedTab == 1) "GENERATE" else "CHECK")
            }
        }
    }
}

/**
 * Execute check digit generation
 */
private fun generateOperation(
    data: String,
    method: CheckDigitMethod,
    onExecute: (ConsoleMessage) -> Unit
) {
    if (data.isBlank()) {
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Error: Input data is empty"
        ))
        return
    }
    
    val result = CheckDigitEngine.generate(data, method)
    
    result.onSuccess { digit ->
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        
        val message = buildString {
            appendLine("[$timestamp] Digit generation done")
            appendLine("****************************************")
            appendLine("Method:\t\t${method.displayName}")
            appendLine("Input:\t\t\t$data")
            appendLine("Digit:\t\t\t$digit")
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
 * Execute check digit validation
 */
private fun checkOperation(
    data: String,
    method: CheckDigitMethod,
    onExecute: (ConsoleMessage) -> Unit
) {
    if (data.isBlank()) {
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Error: Input data is empty"
        ))
        return
    }
    
    val result = CheckDigitEngine.check(data, method)
    
    result.onSuccess { isValid ->
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        
        val message = buildString {
            appendLine("[$timestamp] Digit check done")
            appendLine("****************************************")
            appendLine("Method:\t\t${method.displayName}")
            appendLine("Input:\t\t\t$data")
            appendLine("----------------------------------------")
            appendLine("Result: \t\t${if (isValid) "Check Passed" else "Check Failed"}")
        }
        
        onExecute(ConsoleMessage(
            level = if (isValid) MessageLevel.SUCCESS else MessageLevel.ERROR,
            message = message
        ))
    }.onFailure { error ->
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Error: ${error.message}"
        ))
    }
}
