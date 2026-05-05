package com.bwire.keylock.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bwire.keylock.domain.crypto.DESAlgorithm
import com.bwire.keylock.domain.crypto.DESCryptoEngine
import com.bwire.keylock.domain.crypto.DESMode
import com.bwire.keylock.domain.crypto.DesPadding
import com.bwire.keylock.domain.crypto.DataEncoding
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.*
import com.bwire.keylock.util.*
import javax.crypto.spec.SecretKeySpec

/**
 * DES / 3DES Panel
 * DES and Triple-DES encryption/decryption with multiple modes and padding options
 */
@Composable
fun DESPanel(
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var algorithm by remember { mutableStateOf(DESAlgorithm.DES) }
    var mode by remember { mutableStateOf(DESMode.ECB) }
    var padding by remember { mutableStateOf(DesPadding.PKCS5) }
    var dataEncoding by remember { mutableStateOf(DataEncoding.HEXADECIMAL) }
    var keyInput by remember { mutableStateOf("") }
    var dataInput by remember { mutableStateOf("") }
    var ivInput by remember { mutableStateOf("") }
    var algorithmExpanded by remember { mutableStateOf(false) }
    var modeExpanded by remember { mutableStateOf(false) }
    var paddingExpanded by remember { mutableStateOf(false) }

    val keyExpectedLength = algorithm.keyBytes * 2  // Key is always hex
    val ivExpectedLength = 16  // IV is always hex (8 bytes)

    Surface(
        modifier = modifier,
        color = DarkestGreen
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with info icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DES / 3DES",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonGreen
                )

                Surface(
                    color = MediumGreen,
                    shape = MaterialTheme.shapes.small
                ) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = DarkestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Divider(color = MediumGreen)

            // Algorithm
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Algorithm",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = algorithm.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { algorithmExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Algorithm dropdown"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = MediumGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { algorithmExpanded = true }
                    )

                    DropdownMenu(
                        expanded = algorithmExpanded,
                        onDismissRequest = { algorithmExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DESAlgorithm.entries.forEach { algo ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = algo.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    algorithm = algo
                                    algorithmExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Divider(color = MediumGreen.copy(alpha = 0.3f))

            // Mode
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Mode",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = mode.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { modeExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Mode dropdown"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = MediumGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { modeExpanded = true }
                    )

                    DropdownMenu(
                        expanded = modeExpanded,
                        onDismissRequest = { modeExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DESMode.entries.forEach { desMode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = desMode.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    mode = desMode
                                    modeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Divider(color = MediumGreen.copy(alpha = 0.3f))

            // Padding
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Padding",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = padding.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { paddingExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Padding dropdown"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = MediumGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { paddingExpanded = true }
                    )

                    DropdownMenu(
                        expanded = paddingExpanded,
                        onDismissRequest = { paddingExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DesPadding.entries.forEach { pad ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = pad.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    padding = pad
                                    paddingExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Divider(color = MediumGreen.copy(alpha = 0.3f))

            // Data Encoding
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Data encoding",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DataEncoding.entries.forEach { enc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = dataEncoding == enc,
                                onClick = { dataEncoding = enc },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeonGreen,
                                    unselectedColor = MediumGreen
                                )
                            )
                            Text(
                                text = enc.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Divider(color = MediumGreen)

            // Key
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Key (Hex)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "[${keyInput.replace("\\s".toRegex(), "").length}/$keyExpectedLength]",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumGreen
                    )
                }
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
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

            // Data
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Data",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "[${dataInput.replace("\\s".toRegex(), "").length}]",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumGreen
                    )
                }
                OutlinedTextField(
                    value = dataInput,
                    onValueChange = { dataInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
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

            // IV (only shown when required)
            if (mode.requiresIV) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "IV (Hex)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "[${ivInput.replace("\\s".toRegex(), "").length}/$ivExpectedLength]",
                            style = MaterialTheme.typography.bodySmall,
                            color = MediumGreen
                        )
                    }
                    OutlinedTextField(
                        value = ivInput,
                        onValueChange = { ivInput = it },
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
            }

            Divider(color = MediumGreen)

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                Button(
                    onClick = {
                        executeEncrypt(algorithm, mode, padding, dataEncoding, keyInput, dataInput, ivInput, onExecute)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = DarkestGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypt",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Encrypt")
                }

                Button(
                    onClick = {
                        executeDecrypt(algorithm, mode, padding, dataEncoding, keyInput, dataInput, ivInput, onExecute)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediumGreen,
                        contentColor = DarkestGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Decrypt",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decrypt")
                }
            }
        }
    }
}

private fun executeEncrypt(
    algorithm: DESAlgorithm,
    mode: DESMode,
    padding: DesPadding,
    dataEncoding: DataEncoding,
    key: String,
    data: String,
    iv: String,
    onExecute: (ConsoleMessage) -> Unit
) {
    try {
        val keyChars = key.replace("\\s".toRegex(), "").length
        val expectedKeyChars = algorithm.keyBytes * 2
        if (keyChars != expectedKeyChars) {
            throw IllegalArgumentException(
                "Key length must be $expectedKeyChars hex chars for ${algorithm.displayName}"
            )
        }

        if (mode.requiresIV) {
            val ivChars = iv.replace("\\s".toRegex(), "").length
            if (ivChars != 16) {
                throw IllegalArgumentException(
                    "IV length must be 16 hex chars for ${mode.displayName}"
                )
            }
        }

        val keyBytes = parseInput(key, DataEncoding.HEXADECIMAL)
        val dataBytes = parseInput(data, dataEncoding)
        val ivBytes = if (mode.requiresIV) parseInput(iv, DataEncoding.HEXADECIMAL) else null

        val result = DESCryptoEngine.encrypt(algorithm, mode, padding, keyBytes, dataBytes, ivBytes)

        result.onSuccess { encrypted ->
            val message = buildString {
                appendLine("DES operation finished")
                appendLine("****************************************")
                appendLine("Key:\t\t\t${key.replace("\\s".toRegex(), "")}")
                appendLine("Algorithm:\t\t${algorithm.displayName}")
                appendLine("Mode:\t\t\t${mode.displayName}")
                appendLine("Padding:\t\t${padding.displayName}")
                if (ivBytes != null) {
                    appendLine("IV:\t\t\t${iv.replace("\\s".toRegex(), "")}")
                }
                appendLine("Crypto operation:\tEncryption")
                appendLine("Data:\t\t\t${data.replace("\\s".toRegex(), "")}")
                appendLine()
                appendLine("----------------------------------------")
                appendLine("Encrypted data:\t${encrypted.toHexString()}")
            }

            onExecute(ConsoleMessage(
                level = MessageLevel.SUCCESS,
                message = message
            ))
        }.onFailure { error ->
            onExecute(ConsoleMessage(
                level = MessageLevel.ERROR,
                message = "Encryption failed: ${error.message}"
            ))
        }
    } catch (e: Exception) {
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Error: ${e.message}"
        ))
    }
}

private fun executeDecrypt(
    algorithm: DESAlgorithm,
    mode: DESMode,
    padding: DesPadding,
    dataEncoding: DataEncoding,
    key: String,
    data: String,
    iv: String,
    onExecute: (ConsoleMessage) -> Unit
) {
    try {
        val keyChars = key.replace("\\s".toRegex(), "").length
        val expectedKeyChars = algorithm.keyBytes * 2
        if (keyChars != expectedKeyChars) {
            throw IllegalArgumentException(
                "Key length must be $expectedKeyChars hex chars for ${algorithm.displayName}"
            )
        }

        if (mode.requiresIV) {
            val ivChars = iv.replace("\\s".toRegex(), "").length
            if (ivChars != 16) {
                throw IllegalArgumentException(
                    "IV length must be 16 hex chars for ${mode.displayName}"
                )
            }
        }

        val keyBytes = parseInput(key, DataEncoding.HEXADECIMAL)
        val dataBytes = parseInput(data, dataEncoding)
        val ivBytes = if (mode.requiresIV) parseInput(iv, DataEncoding.HEXADECIMAL) else null

        val result = DESCryptoEngine.decrypt(algorithm, mode, padding, keyBytes, dataBytes, ivBytes)

        result.onSuccess { decrypted ->
            val message = buildString {
                appendLine("DES operation finished")
                appendLine("****************************************")
                appendLine("Key:\t\t\t${key.replace("\\s".toRegex(), "")}")
                appendLine("Algorithm:\t\t${algorithm.displayName}")
                appendLine("Mode:\t\t\t${mode.displayName}")
                appendLine("Padding:\t\t${padding.displayName}")
                if (ivBytes != null) {
                    appendLine("IV:\t\t\t${iv.replace("\\s".toRegex(), "")}")
                }
                appendLine("Crypto operation:\tDecoding")
                appendLine("Data:\t\t\t${data.replace("\\s".toRegex(), "")}")
                appendLine()
                appendLine("----------------------------------------")
                appendLine("Decoded data:\t\t${decrypted.toHexString()}")
            }

            onExecute(ConsoleMessage(
                level = MessageLevel.SUCCESS,
                message = message
            ))
        }.onFailure { error ->
            onExecute(ConsoleMessage(
                level = MessageLevel.ERROR,
                message = "Decryption failed: ${error.message}"
            ))
        }
    } catch (e: Exception) {
        onExecute(ConsoleMessage(
            level = MessageLevel.ERROR,
            message = "Error: ${e.message}"
        ))
    }
}

private fun parseInput(input: String, encoding: DataEncoding): ByteArray {
    val cleaned = input.replace("\\s".toRegex(), "")
    return when (encoding) {
        DataEncoding.ASCII -> cleaned.toByteArray(Charsets.US_ASCII)
        DataEncoding.HEXADECIMAL -> cleaned.hexToByteArray()
    }
}
