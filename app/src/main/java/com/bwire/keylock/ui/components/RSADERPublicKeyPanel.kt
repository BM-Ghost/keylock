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
import com.bwire.keylock.domain.crypto.RSADERPublicKeyEngine
import com.bwire.keylock.domain.crypto.RSADataEncoding
import com.bwire.keylock.domain.crypto.RSADEREncoding
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * RSA DER Public Key Panel
 * Handles encoding and decoding of RSA public keys in DER format
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RSADERPublicKeyPanel(
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0=Encode, 1=Decode
    
    // Encode tab state
    var modulusInput by remember { mutableStateOf("") }
    var modulusEncoding by remember { mutableStateOf(RSADataEncoding.EBCDIC_HEX) }
    var exponentInput by remember { mutableStateOf("") }
    var exponentEncoding by remember { mutableStateOf(RSADataEncoding.ASCII_BASE64) }
    var modulusNegative by remember { mutableStateOf(false) }
    var encodeDEREncoding by remember { mutableStateOf(RSADEREncoding.ENCODING_01_DER_ASN1_PUBLIC_KEY_UNSIGNED) }
    
    // Decode tab state
    var dataInput by remember { mutableStateOf("") }
    var dataEncoding by remember { mutableStateOf(RSADataEncoding.ASCII_HEX) }
    var decodeDEREncoding by remember { mutableStateOf(RSADEREncoding.ENCODING_01_DER_ASN1_PUBLIC_KEY_UNSIGNED) }
    
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
                text = "RSA DER Public Key",
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
            
            // Tab content
            if (selectedTab == 0) {
                // ENCODE TAB
                EncodeTab(
                    modulusInput = modulusInput,
                    onModulusChange = { modulusInput = it },
                    modulusEncoding = modulusEncoding,
                    onModulusEncodingChange = { modulusEncoding = it },
                    exponentInput = exponentInput,
                    onExponentChange = { exponentInput = it },
                    exponentEncoding = exponentEncoding,
                    onExponentEncodingChange = { exponentEncoding = it },
                    modulusNegative = modulusNegative,
                    onModulusNegativeChange = { modulusNegative = it },
                    derEncoding = encodeDEREncoding,
                    onDEREncodingChange = { encodeDEREncoding = it },
                    onExecute = onExecute
                )
            } else {
                // DECODE TAB
                DecodeTab(
                    dataInput = dataInput,
                    onDataChange = { dataInput = it },
                    dataEncoding = dataEncoding,
                    onDataEncodingChange = { dataEncoding = it },
                    derEncoding = decodeDEREncoding,
                    onDEREncodingChange = { decodeDEREncoding = it },
                    onExecute = onExecute
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EncodeTab(
    modulusInput: String,
    onModulusChange: (String) -> Unit,
    modulusEncoding: RSADataEncoding,
    onModulusEncodingChange: (RSADataEncoding) -> Unit,
    exponentInput: String,
    onExponentChange: (String) -> Unit,
    exponentEncoding: RSADataEncoding,
    onExponentEncodingChange: (RSADataEncoding) -> Unit,
    modulusNegative: Boolean,
    onModulusNegativeChange: (Boolean) -> Unit,
    derEncoding: RSADEREncoding,
    onDEREncodingChange: (RSADEREncoding) -> Unit,
    onExecute: (ConsoleMessage) -> Unit
) {
    var expandedModulusEncoding by remember { mutableStateOf(false) }
    var expandedExponentEncoding by remember { mutableStateOf(false) }
    var expandedDEREncoding by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Modulus Input
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Modulus",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            
            OutlinedTextField(
                value = modulusInput,
                onValueChange = onModulusChange,
                placeholder = { Text("Enter modulus...", color = TextTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = MediumGreen,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                minLines = 3
            )
        }
        
        // Modulus Encoding
        EncodingDropdown(
            label = "Modulus Encoding",
            selectedEncoding = modulusEncoding,
            onEncodingChange = onModulusEncodingChange,
            expanded = expandedModulusEncoding,
            onExpandedChange = { expandedModulusEncoding = it }
        )
        
        Divider(color = MediumGreen.copy(alpha = 0.3f))
        
        // Exponent Input
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Exponent",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            
            OutlinedTextField(
                value = exponentInput,
                onValueChange = onExponentChange,
                placeholder = { Text("Enter exponent...", color = TextTertiary) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = MediumGreen,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )
        }
        
        // Exponent Encoding
        EncodingDropdown(
            label = "Exponent Encoding",
            selectedEncoding = exponentEncoding,
            onEncodingChange = onExponentEncodingChange,
            expanded = expandedExponentEncoding,
            onExpandedChange = { expandedExponentEncoding = it }
        )
        
        // Modulus Negative Checkbox
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Checkbox(
                checked = modulusNegative,
                onCheckedChange = onModulusNegativeChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonGreen,
                    uncheckedColor = MediumGreen
                )
            )
            Text(
                text = "Modulus Negative",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
        
        Divider(color = MediumGreen.copy(alpha = 0.3f))
        
        // DER Encoding (bottom)
        DEREncodingDropdown(
            label = "Modulus Encoding",
            selectedEncoding = derEncoding,
            onEncodingChange = onDEREncodingChange,
            expanded = expandedDEREncoding,
            onExpandedChange = { expandedDEREncoding = it }
        )
        
        Divider(color = MediumGreen)
        
        // Encode Button
        Button(
            onClick = {
                encodeOperation(
                    modulusInput,
                    modulusEncoding,
                    exponentInput,
                    exponentEncoding,
                    modulusNegative,
                    derEncoding,
                    onExecute
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonGreen,
                contentColor = DarkestGreen
            ),
            enabled = modulusInput.isNotBlank() && exponentInput.isNotBlank()
        ) {
            Text("ENCODE")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DecodeTab(
    dataInput: String,
    onDataChange: (String) -> Unit,
    dataEncoding: RSADataEncoding,
    onDataEncodingChange: (RSADataEncoding) -> Unit,
    derEncoding: RSADEREncoding,
    onDEREncodingChange: (RSADEREncoding) -> Unit,
    onExecute: (ConsoleMessage) -> Unit
) {
    var expandedDataEncoding by remember { mutableStateOf(false) }
    var expandedDEREncoding by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                onValueChange = onDataChange,
                placeholder = { Text("Enter DER encoded data...", color = TextTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(
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
        }
        
        // Data Encoding
        EncodingDropdown(
            label = "Data Encoding",
            selectedEncoding = dataEncoding,
            onEncodingChange = onDataEncodingChange,
            expanded = expandedDataEncoding,
            onExpandedChange = { expandedDataEncoding = it }
        )
        
        // DER Encoding
        DEREncodingDropdown(
            label = "DER Encoding",
            selectedEncoding = derEncoding,
            onEncodingChange = onDEREncodingChange,
            expanded = expandedDEREncoding,
            onExpandedChange = { expandedDEREncoding = it }
        )
        
        Divider(color = MediumGreen)
        
        // Decode Button
        Button(
            onClick = {
                decodeOperation(dataInput, dataEncoding, derEncoding, onExecute)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonGreen,
                contentColor = DarkestGreen
            ),
            enabled = dataInput.isNotBlank()
        ) {
            Text("DECODE")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EncodingDropdown(
    label: String,
    selectedEncoding: RSADataEncoding,
    onEncodingChange: (RSADataEncoding) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedEncoding.displayName,
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
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                RSADataEncoding.entries.forEach { encoding ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = encoding.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedEncoding == encoding) NeonGreen else TextPrimary
                            )
                        },
                        onClick = {
                            onEncodingChange(encoding)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DEREncodingDropdown(
    label: String,
    selectedEncoding: RSADEREncoding,
    onEncodingChange: (RSADEREncoding) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedEncoding.displayName,
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
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                RSADEREncoding.entries.forEach { encoding ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = encoding.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedEncoding == encoding) NeonGreen else TextPrimary
                            )
                        },
                        onClick = {
                            onEncodingChange(encoding)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Execute RSA DER encoding operation
 */
private fun encodeOperation(
    modulus: String,
    modulusEncoding: RSADataEncoding,
    exponent: String,
    exponentEncoding: RSADataEncoding,
    modulusNegative: Boolean,
    derEncoding: RSADEREncoding,
    onExecute: (ConsoleMessage) -> Unit
) {
    val result = RSADERPublicKeyEngine.encode(
        modulus,
        modulusEncoding,
        exponent,
        exponentEncoding,
        modulusNegative,
        derEncoding
    )
    
    result.onSuccess { output ->
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        
        val message = buildString {
            appendLine("[$timestamp]")
            appendLine("RsaDerPublicKey: Encoding finished")
            appendLine("****************************************")
            appendLine("Modulus:\t\t$modulus")
            appendLine("Modulus Encoding:\t${modulusEncoding.displayName}")
            appendLine("Exponent:\t\t$exponent")
            appendLine("Exponent Encoding:\t${exponentEncoding.displayName}")
            appendLine("Modulus Negative:\t${if (modulusNegative) "Yes" else "No"}")
            appendLine("----------------------------------------")
            appendLine("Encoded As:\t\t${derEncoding.displayName}")
            appendLine("Data:\t\t\t$output")
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
 * Execute RSA DER decoding operation
 */
private fun decodeOperation(
    data: String,
    dataEncoding: RSADataEncoding,
    derEncoding: RSADEREncoding,
    onExecute: (ConsoleMessage) -> Unit
) {
    val result = RSADERPublicKeyEngine.decode(data, dataEncoding, derEncoding)
    
    result.onSuccess { components ->
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        
        val message = buildString {
            appendLine("[$timestamp]")
            appendLine("RsaDerPublicKey: Decoding finished")
            appendLine("****************************************")
            appendLine("Data:\t\t\t$data")
            appendLine("Encoded As:\t\t${derEncoding.displayName}")
            appendLine("----------------------------------------")
            appendLine("Encoding:\t\t${dataEncoding.displayName}")
            appendLine("Modulus:\t\t${components.modulus}")
            appendLine("Modulus Negative:\t${if (components.modulusNegative) "Yes" else "No"}")
            appendLine("Exponent:\t\t${components.exponent}")
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
