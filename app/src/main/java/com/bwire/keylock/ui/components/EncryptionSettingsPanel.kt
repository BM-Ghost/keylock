package com.bwire.keylock.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.*
import com.bwire.keylock.domain.crypto.AESAlgorithm
import com.bwire.keylock.domain.crypto.AESCryptoEngine
import com.bwire.keylock.domain.crypto.CipherMode
import com.bwire.keylock.domain.crypto.DataEncoding
import com.bwire.keylock.util.*

/**
 * Encryption Settings Panel
 * Primary input panel for cryptographic operations
 * 
 * Features:
 * - Algorithm selection (AES-128/192/256)
 * - Mode selection (ECB, CBC, CFB, OFB, KCV)
 * - Data encoding (ASCII, Hexadecimal)
 * - Key, Data, and IV input fields
 * - Execute and validate operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncryptionSettingsPanel(
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAlgorithm by remember { mutableStateOf(AESAlgorithm.AES_128) }
    var selectedMode by remember { mutableStateOf(CipherMode.ECB) }
    var selectedEncoding by remember { mutableStateOf(DataEncoding.HEXADECIMAL) }
    var keyInput by remember { mutableStateOf("") }
    var dataInput by remember { mutableStateOf("") }
    var ivInput by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf(CryptoOperation.ENCRYPT) }
    
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
            Text(
                text = "Encryption Settings",
                style = MaterialTheme.typography.titleLarge,
                color = NeonGreen
            )
            
            // Algorithm Selection
            Text(
                text = "Algorithm",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AESAlgorithm.entries.forEach { algo ->
                    FilterChip(
                        selected = selectedAlgorithm == algo,
                        onClick = { selectedAlgorithm = algo },
                        label = { Text(algo.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkGreen,
                            selectedLabelColor = NeonGreen
                        )
                    )
                }
            }
            
            // Mode Selection
            Text(
                text = "Mode",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CipherMode.entries.forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode },
                        label = { Text(mode.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkGreen,
                            selectedLabelColor = NeonGreen
                        )
                    )
                }
            }
            
            // Data Encoding
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
                        label = { Text(encoding.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkGreen,
                            selectedLabelColor = NeonGreen
                        )
                    )
                }
            }
            
            Divider(color = MediumGreen)
            
            // Key Input
            val expectedKeyLength = when (selectedAlgorithm) {
                AESAlgorithm.AES_128 -> if (selectedEncoding == DataEncoding.HEXADECIMAL) 32 else 16
                AESAlgorithm.AES_192 -> if (selectedEncoding == DataEncoding.HEXADECIMAL) 48 else 24
                AESAlgorithm.AES_256 -> if (selectedEncoding == DataEncoding.HEXADECIMAL) 64 else 32
            }
            
            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = { Text("Key (${expectedKeyLength} ${if (selectedEncoding == DataEncoding.HEXADECIMAL) "hex chars" else "bytes"})") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = MediumGreen,
                    focusedLabelColor = NeonGreen
                ),
                supportingText = {
                    Text("Current length: ${keyInput.length}")
                }
            )
            
            // Data Input
            OutlinedTextField(
                value = dataInput,
                onValueChange = { dataInput = it },
                label = { Text("Data Block") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = MediumGreen,
                    focusedLabelColor = NeonGreen
                )
            )
            
            // IV Input (conditional on mode)
            if (selectedMode.requiresIV) {
                OutlinedTextField(
                    value = ivInput,
                    onValueChange = { ivInput = it },
                    label = { Text("IV (Initialization Vector)") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = MediumGreen,
                        focusedLabelColor = NeonGreen
                    ),
                    supportingText = {
                        Text("16 bytes (32 hex chars for AES)")
                    }
                )
            }
            
            Divider(color = MediumGreen)
            
            // Operation Selection
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        operation = CryptoOperation.ENCRYPT
                        executeOperation(
                            selectedAlgorithm,
                            selectedMode,
                            selectedEncoding,
                            keyInput,
                            dataInput,
                            ivInput,
                            operation,
                            onExecute
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = DarkestGreen
                    )
                ) {
                    Text("ENCRYPT")
                }
                
                Button(
                    onClick = {
                        operation = CryptoOperation.DECRYPT
                        executeOperation(
                            selectedAlgorithm,
                            selectedMode,
                            selectedEncoding,
                            keyInput,
                            dataInput,
                            ivInput,
                            operation,
                            onExecute
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediumGreen,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("DECRYPT")
                }
            }
            
            if (selectedMode == CipherMode.KCV) {
                Button(
                    onClick = {
                        operation = CryptoOperation.KCV
                        executeOperation(
                            selectedAlgorithm,
                            selectedMode,
                            selectedEncoding,
                            keyInput,
                            dataInput,
                            ivInput,
                            operation,
                            onExecute
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen,
                        contentColor = NeonGreen
                    )
                ) {
                    Text("CALCULATE KCV")
                }
            }
        }
    }
}

enum class CryptoOperation {
    ENCRYPT,
    DECRYPT,
    KCV
}

private fun executeOperation(
    algorithm: AESAlgorithm,
    mode: CipherMode,
    encoding: DataEncoding,
    key: String,
    data: String,
    iv: String,
    operation: CryptoOperation,
    onExecute: (ConsoleMessage) -> Unit
) {
    try {
        // Validate inputs
        if (key.isEmpty()) {
            onExecute(ConsoleMessage(
                level = MessageLevel.ERROR,
                message = "Error: Key is required"
            ))
            return
        }
        
        if (data.isEmpty() && operation != CryptoOperation.KCV) {
            onExecute(ConsoleMessage(
                level = MessageLevel.ERROR,
                message = "Error: Data is required"
            ))
            return
        }
        
        if (mode.requiresIV && iv.isEmpty() && operation != CryptoOperation.KCV) {
            onExecute(ConsoleMessage(
                level = MessageLevel.ERROR,
                message = "Error: IV is required for ${mode.displayName} mode"
            ))
            return
        }
        
        // Log operation start
        onExecute(ConsoleMessage(
            level = MessageLevel.INFO,
            message = "═══ ${operation.name} Operation ═══"
        ))
        
        onExecute(ConsoleMessage(
            level = MessageLevel.INFO,
            message = "Algorithm: ${algorithm.displayName}, Mode: ${mode.displayName}, Encoding: ${encoding.displayName}"
        ))
        
        // Parse key based on encoding
        val keyBytes = when (encoding) {
            DataEncoding.HEXADECIMAL -> {
                if (!key.isValidHex()) {
                    onExecute(ConsoleMessage(
                        level = MessageLevel.ERROR,
                        message = "Error: Invalid hexadecimal key"
                    ))
                    return
                }
                key.hexToByteArray()
            }
            DataEncoding.ASCII -> key.asciiToByteArray()
        }
        
        // Validate key length
        if (keyBytes.size != algorithm.keyBytes) {
            onExecute(ConsoleMessage(
                level = MessageLevel.ERROR,
                message = "Error: Invalid key length. Expected ${algorithm.keyBytes} bytes, got ${keyBytes.size} bytes"
            ))
            return
        }
        
        onExecute(ConsoleMessage(
            level = MessageLevel.INFO,
            message = "Key: ${keyBytes.toHexString().formatHex()}"
        ))
        
        // Execute operation
        when (operation) {
            CryptoOperation.KCV -> {
                val result = AESCryptoEngine.generateKCV(keyBytes)
                result.onSuccess { kcv ->
                    onExecute(ConsoleMessage(
                        level = MessageLevel.SUCCESS,
                        message = "KCV: ${kcv.toHexString()}"
                    ))
                }.onFailure { error ->
                    onExecute(ConsoleMessage(
                        level = MessageLevel.ERROR,
                        message = "Error: ${error.message}"
                    ))
                }
            }
            
            CryptoOperation.ENCRYPT -> {
                val dataBytes = when (encoding) {
                    DataEncoding.HEXADECIMAL -> {
                        if (!data.isValidHex()) {
                            onExecute(ConsoleMessage(
                                level = MessageLevel.ERROR,
                                message = "Error: Invalid hexadecimal data"
                            ))
                            return
                        }
                        data.hexToByteArray()
                    }
                    DataEncoding.ASCII -> data.asciiToByteArray()
                }
                
                val ivBytes = if (mode.requiresIV) {
                    if (!iv.isValidHex()) {
                        onExecute(ConsoleMessage(
                            level = MessageLevel.ERROR,
                            message = "Error: Invalid hexadecimal IV"
                        ))
                        return
                    }
                    iv.hexToByteArray()
                } else null
                
                onExecute(ConsoleMessage(
                    level = MessageLevel.INFO,
                    message = "Plaintext (${dataBytes.size} bytes): ${dataBytes.toHexString().formatHex()}"
                ))
                
                if (ivBytes != null) {
                    onExecute(ConsoleMessage(
                        level = MessageLevel.INFO,
                        message = "IV: ${ivBytes.toHexString().formatHex()}"
                    ))
                }
                
                val result = AESCryptoEngine.encrypt(algorithm, mode, keyBytes, dataBytes, ivBytes)
                result.onSuccess { ciphertext ->
                    onExecute(ConsoleMessage(
                        level = MessageLevel.SUCCESS,
                        message = "Ciphertext (${ciphertext.size} bytes): ${ciphertext.toHexString().formatHex()}"
                    ))
                }.onFailure { error ->
                    onExecute(ConsoleMessage(
                        level = MessageLevel.ERROR,
                        message = "Encryption failed: ${error.message}"
                    ))
                }
            }
            
            CryptoOperation.DECRYPT -> {
                val dataBytes = when (encoding) {
                    DataEncoding.HEXADECIMAL -> {
                        if (!data.isValidHex()) {
                            onExecute(ConsoleMessage(
                                level = MessageLevel.ERROR,
                                message = "Error: Invalid hexadecimal data"
                            ))
                            return
                        }
                        data.hexToByteArray()
                    }
                    DataEncoding.ASCII -> data.asciiToByteArray()
                }
                
                val ivBytes = if (mode.requiresIV) {
                    if (!iv.isValidHex()) {
                        onExecute(ConsoleMessage(
                            level = MessageLevel.ERROR,
                            message = "Error: Invalid hexadecimal IV"
                        ))
                        return
                    }
                    iv.hexToByteArray()
                } else null
                
                onExecute(ConsoleMessage(
                    level = MessageLevel.INFO,
                    message = "Ciphertext (${dataBytes.size} bytes): ${dataBytes.toHexString().formatHex()}"
                ))
                
                if (ivBytes != null) {
                    onExecute(ConsoleMessage(
                        level = MessageLevel.INFO,
                        message = "IV: ${ivBytes.toHexString().formatHex()}"
                    ))
                }
                
                val result = AESCryptoEngine.decrypt(algorithm, mode, keyBytes, dataBytes, ivBytes)
                result.onSuccess { plaintext ->
                    val hexOutput = plaintext.toHexString().formatHex()
                    val asciiOutput = try {
                        plaintext.toAsciiString()
                    } catch (e: Exception) {
                        "[non-ASCII]"
                    }
                    
                    onExecute(ConsoleMessage(
                        level = MessageLevel.SUCCESS,
                        message = "Plaintext (${plaintext.size} bytes): $hexOutput"
                    ))
                    onExecute(ConsoleMessage(
                        level = MessageLevel.SUCCESS,
                        message = "ASCII: $asciiOutput"
                    ))
                }.onFailure { error ->
                    onExecute(ConsoleMessage(
                        level = MessageLevel.ERROR,
                        message = "Decryption failed: ${error.message}"
                    ))
                }
            }
        }
        
        onExecute(ConsoleMessage(
            level = MessageLevel.INFO,
            message = "═══ Operation Complete ═══"
        ))
        
        // Zeroize sensitive data
        keyBytes.zeroize()
        
    } catch (e: Exception) {
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Unexpected error: ${e.message}"
        ))
    }
}
