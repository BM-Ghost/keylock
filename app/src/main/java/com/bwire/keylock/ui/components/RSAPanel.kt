package com.bwire.keylock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bwire.keylock.domain.crypto.*
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.DarkestGreen
import com.bwire.keylock.ui.theme.MediumGreen
import com.bwire.keylock.ui.theme.NeonGreen
import com.bwire.keylock.ui.theme.TextPrimary
import com.bwire.keylock.ui.theme.TextSecondary
import com.bwire.keylock.ui.theme.TextTertiary
import com.bwire.keylock.util.asciiToByteArray
import com.bwire.keylock.util.toHexString
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.RSAKeyGenParameterSpec
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.Cipher
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen

@Composable
fun RSAPanel(
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOperation by remember { mutableStateOf("Keys") }
    var expanded by remember { mutableStateOf(false) }
    val operations = listOf("Keys", "Encrypt", "Decrypt", "Sign", "Verify", "OAEP")
    var publicKey by remember { mutableStateOf<PublicKey?>(null) }
    var privateKey by remember { mutableStateOf<PrivateKey?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkestGreen)
            .padding(16.dp)
    ) {
        // Title
        Text(
            "RSA Operations",
            style = MaterialTheme.typography.titleLarge,
            color = NeonGreen,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Dropdown Selector
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeonGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Operation: $selectedOperation")
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown"
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MediumGreen)
            ) {
                operations.forEach { operation ->
                    DropdownMenuItem(
                        text = { Text(operation, color = DarkestGreen) },
                        onClick = {
                            selectedOperation = operation
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Operation Content - Scrollable area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedOperation) {
                "Keys" -> RSAKeysContent(
                    onExecute = onExecute,
                    publicKey = publicKey,
                    privateKey = privateKey,
                    onKeysGenerated = { pub, priv ->
                        publicKey = pub
                        privateKey = priv
                    }
                )
                "Encrypt" -> RSAEncryptContent(
                    onExecute = onExecute,
                    publicKey = publicKey,
                    privateKey = privateKey
                )
                "Decrypt" -> RSADecryptContent(
                    onExecute = onExecute,
                    publicKey = publicKey,
                    privateKey = privateKey
                )
                "Sign" -> RSASignContent(onExecute = onExecute)
                "Verify" -> RSAVerifyContent(onExecute = onExecute)
                "OAEP" -> RSAOAEPContent(onExecute = onExecute)
            }
        }
    }
}

@Composable
private fun RSAKeysContent(
    onExecute: (ConsoleMessage) -> Unit,
    publicKey: PublicKey?,
    privateKey: PrivateKey?,
    onKeysGenerated: (PublicKey?, PrivateKey?) -> Unit
) {
    var keyLength by remember { mutableStateOf(RSAKeyLength.RSA_2048) }
    var publicExponentInput by remember { mutableStateOf("010001") }
    var modulusInput by remember { mutableStateOf("") }
    var privateExponentInput by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var expandedKeyLength by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Key Length Selector
        Text("Key Length:", color = TextPrimary)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedKeyLength = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                border = ButtonDefaults.outlinedButtonBorder()
            ) {
                Text(keyLength.displayName)
            }

            DropdownMenu(
                expanded = expandedKeyLength,
                onDismissRequest = { expandedKeyLength = false },
                modifier = Modifier.fillMaxWidth(0.9f).background(MediumGreen)
            ) {
                RSAKeyLength.values().forEach { length ->
                    DropdownMenuItem(
                        text = { Text(length.displayName, color = DarkestGreen) },
                        onClick = {
                            keyLength = length
                            expandedKeyLength = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = publicExponentInput,
            onValueChange = { publicExponentInput = it },
            label = { Text("Public Exponent (e)") },
            placeholder = { Text("010001") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                focusedLabelColor = NeonGreen,
                cursorColor = NeonGreen
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Load Existing Keys
        Text("Or Load Existing Keys:", color = TextPrimary)
        OutlinedTextField(
            value = modulusInput,
            onValueChange = { modulusInput = it.uppercase() },
            label = { Text("Modulus (n) in Hex") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                focusedLabelColor = NeonGreen,
                cursorColor = NeonGreen
            )
        )
        
        OutlinedTextField(
            value = privateExponentInput,
            onValueChange = { privateExponentInput = it.uppercase() },
            label = { Text("Private Exponent (d) in Hex") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                focusedLabelColor = NeonGreen,
                cursorColor = NeonGreen
            )
        )

        // Load Keys Button
        Button(
            onClick = {
                isGenerating = true
                loadRSAKeys(modulusInput, privateExponentInput, publicExponentInput) { output, pubKey, privKey ->
                    if (pubKey != null && privKey != null) {
                        onKeysGenerated(pubKey, privKey)
                    }
                    isGenerating = false
                    onExecute(ConsoleMessage(message = output))
                }
            },
            enabled = !isGenerating && modulusInput.isNotEmpty() && privateExponentInput.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
            Text(if (isGenerating) "Loading..." else "Load Keys", color = DarkestGreen)
        }

        // Generate Button
        Button(
            onClick = {
                isGenerating = true
                generateRSAKeyPair(keyLength, publicExponentInput) { output, pubKey, privKey ->
                    onKeysGenerated(pubKey, privKey)
                    isGenerating = false
                    onExecute(ConsoleMessage(message = output))
                }
            },
            enabled = !isGenerating,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
            Text(if (isGenerating) "Generating..." else "Generate Keys", color = DarkestGreen)
        }

        // Display Keys in Hex Format
        if (publicKey != null && privateKey != null) {
            val rsaPublicKey = publicKey as? java.security.interfaces.RSAPublicKey
            val rsaPrivateKey = privateKey as? java.security.interfaces.RSAPrivateKey

            if (rsaPublicKey != null && rsaPrivateKey != null) {
                Divider(color = MediumGreen, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                Text("Key Components (Hexadecimal):", color = NeonGreen, style = MaterialTheme.typography.titleMedium)

                val modulusHex = rsaPublicKey.modulus.toString(16).uppercase()
                val publicExpHex = rsaPublicKey.publicExponent.toString(16).uppercase()
                val privateExpHex = rsaPrivateKey.privateExponent.toString(16).uppercase()

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = DarkestGreen),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Modulus
                        HexKeyField("Modulus (n)", modulusHex)
                        Divider(color = MediumGreen, thickness = 1.dp)

                        // Public Exponent
                        HexKeyField("Public Exponent (e)", publicExpHex)
                        Divider(color = MediumGreen, thickness = 1.dp)

                        // Private Exponent
                        HexKeyField("Private Exponent (d)", privateExpHex)
                        Divider(color = MediumGreen, thickness = 1.dp)

                        // Key Length
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Key Length:", color = TextPrimary)
                            Text("${keyLength.bits} bits", color = NeonGreen, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HexKeyField(label: String, hexValue: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$label:", color = NeonGreen, modifier = Modifier.weight(1f))
            Text("[${hexValue.length}]", color = NeonGreen, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(4.dp))
        SelectionContainer {
            Text(
                hexValue,
                color = TextPrimary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RSAEncryptContent(
    onExecute: (ConsoleMessage) -> Unit,
    publicKey: PublicKey?,
    privateKey: PrivateKey?
) {
    var dataInput by remember { mutableStateOf("") }
    var encryptionMethod by remember { mutableStateOf(RSAEncryptionMethod.PRIVATE) }
    var dataEncoding by remember { mutableStateOf(DataEncoding.ASCII) }
    var padding by remember { mutableStateOf(RSAPadding.PKCS1) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Data Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Data:", color = TextPrimary)
            Text("[${dataInput.length}]", color = NeonGreen, style = MaterialTheme.typography.labelSmall)
        }
        OutlinedTextField(
            value = dataInput,
            onValueChange = { value ->
                dataInput = if (dataEncoding == DataEncoding.HEXADECIMAL) value.uppercase() else value
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = MediumGreen,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        GroupBox(title = "Encoding method") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RSAEncryptionMethod.values().forEach { method ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadioButton(
                            selected = encryptionMethod == method,
                            onClick = { encryptionMethod = method },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonGreen)
                        )
                        Text(method.displayName, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        GroupBox(title = "Input data format") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DataEncoding.values().forEach { encoding ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadioButton(
                            selected = dataEncoding == encoding,
                            onClick = { dataEncoding = encoding },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonGreen)
                        )
                        Text(encoding.displayName, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        GroupBox(title = "Padding method") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RSAPadding.values().forEach { pad ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadioButton(
                            selected = padding == pad,
                            onClick = { padding = pad },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonGreen)
                        )
                        Text(pad.displayName, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FilledIconButton(
                onClick = {
                    isProcessing = true
                    encryptRSAData(
                        dataInput = dataInput,
                        method = encryptionMethod,
                        padding = padding,
                        dataEncoding = dataEncoding,
                        publicKey = publicKey,
                        privateKey = privateKey
                    ) { output ->
                        isProcessing = false
                        onExecute(ConsoleMessage(message = output))
                    }
                },
                enabled = !isProcessing && dataInput.isNotEmpty(),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MediumGreen)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = if (isProcessing) "Encrypting" else "Encrypt",
                    tint = NeonGreen
                )
            }
        }
    }
}

@Composable
private fun RSADecryptContent(
    onExecute: (ConsoleMessage) -> Unit,
    publicKey: PublicKey?,
    privateKey: PrivateKey?
) {
    var encryptedData by remember { mutableStateOf("") }
    var decodingMethod by remember { mutableStateOf(RSAEncryptionMethod.PRIVATE) }
    var padding by remember { mutableStateOf(RSAPadding.PKCS1) }
    var outputFormat by remember { mutableStateOf(DataEncoding.ASCII) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Encrypted Data Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Data:", color = TextPrimary)
            Text("[${encryptedData.length}]", color = NeonGreen, style = MaterialTheme.typography.labelSmall)
        }
        OutlinedTextField(
            value = encryptedData,
            onValueChange = { encryptedData = it.uppercase() },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = MediumGreen,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        GroupBox(title = "Decoding method") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RSAEncryptionMethod.values().forEach { method ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadioButton(
                            selected = decodingMethod == method,
                            onClick = { decodingMethod = method },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonGreen)
                        )
                        Text(method.displayName, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        GroupBox(title = "Padding method") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RSAPadding.values().forEach { pad ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadioButton(
                            selected = padding == pad,
                            onClick = { padding = pad },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonGreen)
                        )
                        Text(pad.displayName, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        GroupBox(title = "Output data format") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DataEncoding.values().forEach { format ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadioButton(
                            selected = outputFormat == format,
                            onClick = { outputFormat = format },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonGreen)
                        )
                        Text(format.displayName, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FilledIconButton(
                onClick = {
                    isProcessing = true
                    decryptRSAData(
                        encryptedData = encryptedData,
                        method = decodingMethod,
                        padding = padding,
                        outputFormat = outputFormat,
                        publicKey = publicKey,
                        privateKey = privateKey
                    ) { output ->
                        isProcessing = false
                        onExecute(ConsoleMessage(message = output))
                    }
                },
                enabled = !isProcessing && encryptedData.isNotEmpty(),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MediumGreen)
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = if (isProcessing) "Decrypting" else "Decrypt",
                    tint = NeonGreen
                )
            }
        }
    }
}

@Composable
private fun RSASignContent(onExecute: (ConsoleMessage) -> Unit) {
    var dataInput by remember { mutableStateOf("") }
    var dataEncoding by remember { mutableStateOf(DataEncoding.ASCII) }
    var hashAlgorithm by remember { mutableStateOf(RSAHashAlgorithm.SHA256) }
    var consoleOutput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "RSA Sign",
            style = MaterialTheme.typography.titleLarge,
            color = NeonGreen
        )

        // Data Input
        Text("Data to Sign:", color = TextPrimary)
        OutlinedTextField(
            value = dataInput,
            onValueChange = { dataInput = if (dataEncoding == DataEncoding.ASCII) it else it.uppercase() },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = MediumGreen,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Data Encoding
        Text("Data Encoding:", color = TextPrimary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DataEncoding.values().forEach { encoding ->
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RadioButton(
                        selected = dataEncoding == encoding,
                        onClick = { dataEncoding = encoding },
                        colors = RadioButtonDefaults.colors(selectedColor = NeonGreen)
                    )
                    Text(encoding.name, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Hash Algorithm Selector
        Text("Hash Algorithm:", color = TextPrimary)
        var expandedHash by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedHash = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeonGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.foundation.BorderStroke(1.dp, MediumGreen).brush
                )
            ) {
                Text(hashAlgorithm.displayName)
            }

            DropdownMenu(
                expanded = expandedHash,
                onDismissRequest = { expandedHash = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MediumGreen)
            ) {
                RSAHashAlgorithm.values().forEach { algo ->
                    DropdownMenuItem(
                        text = { Text(algo.displayName, color = DarkestGreen) },
                        onClick = {
                            hashAlgorithm = algo
                            expandedHash = false
                        }
                    )
                }
            }
        }

        // Sign Button
        Button(
            onClick = {
                isProcessing = true
                signRSAData(dataInput, dataEncoding, hashAlgorithm) { output ->
                    consoleOutput = output
                    isProcessing = false
                    onExecute(ConsoleMessage(message = output))
                }
            },
            enabled = !isProcessing && dataInput.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
            Text(if (isProcessing) "Signing..." else "Sign", color = DarkestGreen)
        }

        // Console Output
        if (consoleOutput.isNotEmpty()) {
            Text("Output:", color = TextPrimary)
            ConsoleOutputBox(consoleOutput)
        }
    }
}

@Composable
private fun RSAVerifyContent(onExecute: (ConsoleMessage) -> Unit) {
    var dataInput by remember { mutableStateOf("") }
    var signatureInput by remember { mutableStateOf("") }
    var hashAlgorithm by remember { mutableStateOf(RSAHashAlgorithm.SHA256) }
    var consoleOutput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "RSA Verify",
            style = MaterialTheme.typography.titleLarge,
            color = NeonGreen
        )

        // Data Input
        Text("Original Data (Hex):", color = TextPrimary)
        OutlinedTextField(
            value = dataInput,
            onValueChange = { dataInput = it.uppercase() },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = MediumGreen,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Signature Input
        Text("Signature (Hex):", color = TextPrimary)
        OutlinedTextField(
            value = signatureInput,
            onValueChange = { signatureInput = it.uppercase() },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = MediumGreen,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Hash Algorithm Selector
        Text("Hash Algorithm:", color = TextPrimary)
        var expandedHash by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedHash = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeonGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.foundation.BorderStroke(1.dp, MediumGreen).brush
                )
            ) {
                Text(hashAlgorithm.displayName)
            }

            DropdownMenu(
                expanded = expandedHash,
                onDismissRequest = { expandedHash = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MediumGreen)
            ) {
                RSAHashAlgorithm.values().forEach { algo ->
                    DropdownMenuItem(
                        text = { Text(algo.displayName, color = DarkestGreen) },
                        onClick = {
                            hashAlgorithm = algo
                            expandedHash = false
                        }
                    )
                }
            }
        }

        // Verify Button
        Button(
            onClick = {
                isProcessing = true
                verifyRSASignature(dataInput, signatureInput, hashAlgorithm) { output ->
                    consoleOutput = output
                    isProcessing = false
                    onExecute(ConsoleMessage(message = output))
                }
            },
            enabled = !isProcessing && dataInput.isNotEmpty() && signatureInput.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
            Text(if (isProcessing) "Verifying..." else "Verify", color = DarkestGreen)
        }

        // Console Output
        if (consoleOutput.isNotEmpty()) {
            Text("Output:", color = TextPrimary)
            ConsoleOutputBox(consoleOutput)
        }
    }
}

@Composable
private fun RSAOAEPContent(onExecute: (ConsoleMessage) -> Unit) {
    var dataInput by remember { mutableStateOf("") }
    var labelInput by remember { mutableStateOf("") }
    var keyLength by remember { mutableStateOf(RSAKeyLength.RSA_2048) }
    var hashAlgorithm by remember { mutableStateOf(RSAHashAlgorithm.SHA256) }
    var method by remember { mutableStateOf("Encode") }
    var consoleOutput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "OAEP Padding",
            style = MaterialTheme.typography.titleLarge,
            color = NeonGreen
        )

        // Data Input
        Text("Data (Hex):", color = TextPrimary)
        OutlinedTextField(
            value = dataInput,
            onValueChange = { dataInput = it.uppercase() },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = MediumGreen,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Label Input
        Text("Label (Optional, Hex):", color = TextPrimary)
        OutlinedTextField(
            value = labelInput,
            onValueChange = { labelInput = it.uppercase() },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = MediumGreen,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Key Length Selector
        Text("Key Length:", color = TextPrimary)
        var expandedKeyLength by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedKeyLength = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeonGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.foundation.BorderStroke(1.dp, MediumGreen).brush
                )
            ) {
                Text(keyLength.displayName)
            }

            DropdownMenu(
                expanded = expandedKeyLength,
                onDismissRequest = { expandedKeyLength = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MediumGreen)
            ) {
                RSAKeyLength.values().forEach { length ->
                    DropdownMenuItem(
                        text = { Text(length.displayName, color = DarkestGreen) },
                        onClick = {
                            keyLength = length
                            expandedKeyLength = false
                        }
                    )
                }
            }
        }

        // Hash Algorithm Selector
        Text("Hash Algorithm:", color = TextPrimary)
        var expandedHash by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedHash = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeonGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.foundation.BorderStroke(1.dp, MediumGreen).brush
                )
            ) {
                Text(hashAlgorithm.displayName)
            }

            DropdownMenu(
                expanded = expandedHash,
                onDismissRequest = { expandedHash = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MediumGreen)
            ) {
                RSAHashAlgorithm.values().forEach { algo ->
                    DropdownMenuItem(
                        text = { Text(algo.displayName, color = DarkestGreen) },
                        onClick = {
                            hashAlgorithm = algo
                            expandedHash = false
                        }
                    )
                }
            }
        }

        // Method Selection
        Text("Method:", color = TextPrimary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("Encode", "Decode").forEach { m ->
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RadioButton(
                        selected = method == m,
                        onClick = { method = m },
                        colors = RadioButtonDefaults.colors(selectedColor = NeonGreen)
                    )
                    Text(m, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Process Button
        Button(
            onClick = {
                isProcessing = true
                processOAEPPadding(dataInput, labelInput, keyLength, hashAlgorithm, method) { output ->
                    consoleOutput = output
                    isProcessing = false
                    onExecute(ConsoleMessage(message = output))
                }
            },
            enabled = !isProcessing && dataInput.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
            Text(if (isProcessing) "Processing..." else method, color = DarkestGreen)
        }

        // Console Output
        if (consoleOutput.isNotEmpty()) {
            Text("Output:", color = TextPrimary)
            ConsoleOutputBox(consoleOutput)
        }
    }
}

@Composable
private fun ConsoleOutputBox(content: String) {
    val clipboardManager = LocalClipboardManager.current
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = DarkestGreen
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelectionContainer(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    content,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(
                onClick = { clipboardManager.setText(AnnotatedString(content)) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = NeonGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Crypto operations
private fun generateRSAKeyPair(
    keyLength: RSAKeyLength,
    publicExponentInput: String,
    onComplete: (output: String, pubKey: PublicKey?, privKey: PrivateKey?) -> Unit
) {
    try {
        val defaultExponentHex = "010001"
        val trimmedInput = publicExponentInput.trim()
        val cleanedInput = trimmedInput.replace("\\s".toRegex(), "")
        val exponentHexInput = if (cleanedInput.isEmpty()) defaultExponentHex else cleanedInput
        val isHexInputValid = exponentHexInput.matches(Regex("^[0-9A-Fa-f]+$"))
        val parsedExponent = if (isHexInputValid) {
            runCatching { BigInteger(exponentHexInput, 16) }.getOrNull()
        } else {
            null
        }
        val isExponentValid = parsedExponent != null && parsedExponent > BigInteger.ONE && parsedExponent.testBit(0)
        val publicExponent = if (isExponentValid) parsedExponent!! else BigInteger(defaultExponentHex, 16)
        val publicExponentDisplay = if (isExponentValid) {
            exponentHexInput.uppercase()
        } else {
            defaultExponentHex
        }

        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(RSAKeyGenParameterSpec(keyLength.bits, publicExponent))
        val keyPair = keyPairGenerator.generateKeyPair()

        val pubKey = keyPair.public
        val privKey = keyPair.private

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val timestamp = LocalDateTime.now().format(formatter)

        val rsaPublicKey = pubKey as? java.security.interfaces.RSAPublicKey
        val rsaPrivateKey = privKey as? java.security.interfaces.RSAPrivateKey

        val modulusHex = rsaPublicKey?.modulus?.toString(16)?.uppercase().orEmpty()
        val publicExpHexRaw = rsaPublicKey?.publicExponent?.toString(16)?.uppercase().orEmpty()
        val publicExpHex = if (publicExpHexRaw.length % 2 == 0) publicExpHexRaw else "0$publicExpHexRaw"
        val privateExpHex = rsaPrivateKey?.privateExponent?.toString(16)?.uppercase().orEmpty()

        val output = buildString {
            appendLine("[$timestamp]")
            appendLine(" RSA: Generate key operation finished")
            appendLine(" ****************************************")
            appendLine(" Key length:\t\t${keyLength.bits}")
            appendLine(" Public Exponent:\t$publicExponentDisplay")
            appendLine(" ----------------------------------------")
            appendLine(" Public modulus [n]:\t$modulusHex")
            appendLine(" Public exponent [e]:\t$publicExpHex")
            appendLine(" Private exponent [d]:\t$privateExpHex")
            if (!isExponentValid && trimmedInput.isNotEmpty()) {
                appendLine(" Debug:\t\t Invalid public exponent provided, using default 010001 (65537)")
            }
        }

        onComplete(output, pubKey, privKey)
    } catch (e: Exception) {
        onComplete("Error generating keys: ${e.message}", null, null)
    }
}

private fun loadRSAKeys(
    modulusHex: String,
    privateExpHex: String,
    publicExpHex: String,
    onComplete: (output: String, pubKey: PublicKey?, privKey: PrivateKey?) -> Unit
) {
    try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val timestamp = LocalDateTime.now().format(formatter)

        val cleanedModulus = modulusHex.replace("\\s".toRegex(), "")
        val cleanedPrivateExp = privateExpHex.replace("\\s".toRegex(), "")
        val cleanedPublicExp = publicExpHex.replace("\\s".toRegex(), "")

        if (!cleanedModulus.matches(Regex("^[0-9A-Fa-f]+$"))) {
            throw IllegalArgumentException("Modulus must be valid hexadecimal")
        }
        if (!cleanedPrivateExp.matches(Regex("^[0-9A-Fa-f]+$"))) {
            throw IllegalArgumentException("Private exponent must be valid hexadecimal")
        }

        val modulus = BigInteger(cleanedModulus, 16)
        val privateExp = BigInteger(cleanedPrivateExp, 16)
        val publicExp = BigInteger(cleanedPublicExp.ifEmpty { "010001" }, 16)

        val keyBits = modulus.bitLength()
        val keySpec = java.security.spec.RSAPrivateKeySpec(modulus, privateExp)
        val keyFactory = java.security.KeyFactory.getInstance("RSA")
        val privKey = keyFactory.generatePrivate(keySpec)

        val pubKeySpec = java.security.spec.RSAPublicKeySpec(modulus, publicExp)
        val pubKey = keyFactory.generatePublic(pubKeySpec)

        val modulusDisplay = cleanedModulus.uppercase()
        val publicExpDisplay = cleanedPublicExp.uppercase()

        val output = buildString {
            appendLine("[$timestamp]")
            appendLine(" RSA: Load key operation finished")
            appendLine(" ****************************************")
            appendLine(" Key length:\t\t$keyBits")
            appendLine(" Public Exponent:\t${publicExp.toString(16).uppercase()}")
            appendLine(" ----------------------------------------")
            appendLine(" Public modulus [n]:\t$modulusDisplay")
            appendLine(" Public exponent [e]:\t$publicExpDisplay")
            appendLine(" Private exponent [d]:\t${cleanedPrivateExp.uppercase()}")
        }

        onComplete(output, pubKey, privKey)
    } catch (e: Exception) {
        onComplete("Error loading keys: ${e.message}", null, null)
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun encryptRSAData(
    dataInput: String,
    method: RSAEncryptionMethod,
    padding: RSAPadding,
    dataEncoding: DataEncoding,
    publicKey: PublicKey?,
    privateKey: PrivateKey?,
    onComplete: (output: String) -> Unit
) {
    try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val timestamp = LocalDateTime.now().format(formatter)

        val rsaPublicKey = publicKey as? java.security.interfaces.RSAPublicKey
        val rsaPrivateKey = privateKey as? java.security.interfaces.RSAPrivateKey
        val encryptionKey = when (method) {
            RSAEncryptionMethod.PUBLIC -> publicKey
            RSAEncryptionMethod.PRIVATE -> privateKey
        }

        if (encryptionKey == null || rsaPublicKey == null || rsaPrivateKey == null) {
            throw IllegalStateException("No RSA keypair generated. Generate keys first.")
        }

        val inputBytes = when (dataEncoding) {
            DataEncoding.ASCII -> dataInput.asciiToByteArray()
            DataEncoding.HEXADECIMAL -> dataInput.hexToByteArray()
        }

        val transformation = when (padding) {
            RSAPadding.PKCS1 -> "RSA/ECB/PKCS1Padding"
            RSAPadding.NO_PADDING -> "RSA/ECB/NoPadding"
        }

        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey)
        val encrypted = cipher.doFinal(inputBytes)

        val modulusHex = rsaPublicKey.modulus.toString(16).uppercase()
        val publicExpHexRaw = rsaPublicKey.publicExponent.toString(16).uppercase()
        val publicExpHex = if (publicExpHexRaw.length % 2 == 0) publicExpHexRaw else "0$publicExpHexRaw"
        val privateExpHex = rsaPrivateKey.privateExponent.toString(16).uppercase()
        val keyLengthBits = rsaPublicKey.modulus.bitLength()
        val originalDataDisplay = if (dataEncoding == DataEncoding.HEXADECIMAL) {
            dataInput.replace("\\s".toRegex(), "").uppercase()
        } else {
            dataInput
        }

        val output = buildString {
            appendLine("[$timestamp]")
            appendLine(" RSA: Data encryption operation finished")
            appendLine(" ****************************************")
            appendLine(" Key length:\t\t$keyLengthBits")
            appendLine(" Encryption method:\t${method.displayName}")
            appendLine(" Original data:\t\t$originalDataDisplay")
            appendLine(" Public modulus [n]:\t$modulusHex")
            appendLine(" Public exponent [e]:\t$publicExpHex")
            appendLine(" Private exponent [d]:\t$privateExpHex")
            appendLine(" Padding Method:\t${padding.displayName}")
            appendLine(" ----------------------------------------")
            appendLine(" Encoded data:\t${encrypted.toHexString()}")
            appendLine(" Encoded data length:\t${encrypted.size}")
        }
        onComplete(output)
    } catch (e: Exception) {
        onComplete("Error: ${e.message}")
    }
}

@Composable
private fun GroupBox(title: String, content: @Composable () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = DarkestGreen),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, color = NeonGreen, style = MaterialTheme.typography.labelLarge)
            content()
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun decryptRSAData(
    encryptedData: String,
    method: RSAEncryptionMethod,
    padding: RSAPadding,
    outputFormat: DataEncoding,
    publicKey: PublicKey?,
    privateKey: PrivateKey?,
    onComplete: (output: String) -> Unit
) {
    try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val timestamp = LocalDateTime.now().format(formatter)

        // Validate that we have keys
        val key = when (method) {
            RSAEncryptionMethod.PRIVATE -> privateKey ?: throw IllegalStateException("Private key not loaded")
            RSAEncryptionMethod.PUBLIC -> publicKey ?: throw IllegalStateException("Public key not loaded")
        }

        // Convert encrypted hex data to bytes
        val encryptedBytes = encryptedData.hexToByteArray()
        
        // Get key parameters for output
        val keySize = when {
            publicKey is java.security.interfaces.RSAPublicKey -> publicKey.modulus.bitLength()
            privateKey is java.security.interfaces.RSAPrivateKey -> privateKey.modulus.bitLength()
            else -> 0
        }

        // Determine cipher transformation
        val cipherAlgorithm = when (padding) {
            RSAPadding.PKCS1 -> "RSA/ECB/PKCS1Padding"
            RSAPadding.NO_PADDING -> "RSA/ECB/NoPadding"
        }

        // Perform decryption
        val cipher = Cipher.getInstance(cipherAlgorithm)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        // Convert decrypted bytes to the requested output format
        val decryptedDataDisplay = when (outputFormat) {
            DataEncoding.ASCII -> {
                try {
                    String(decryptedBytes, Charsets.US_ASCII)
                } catch (e: Exception) {
                    // If ASCII conversion fails, show hex with note
                    "${decryptedBytes.toHexString()} (not valid ASCII)"
                }
            }
            DataEncoding.HEXADECIMAL -> decryptedBytes.toHexString()
        }

        // Extract key components for logging
        val modulus = when {
            publicKey is java.security.interfaces.RSAPublicKey -> publicKey.modulus.toString(16).uppercase()
            privateKey is java.security.interfaces.RSAPrivateKey -> privateKey.modulus.toString(16).uppercase()
            else -> "N/A"
        }

        val publicExponent = when {
            publicKey is java.security.interfaces.RSAPublicKey -> publicKey.publicExponent.toString(16).uppercase()
            privateKey is java.security.interfaces.RSAPrivateKey -> {
                // For private key, we need to get from the public part
                try {
                    val keyFactory = java.security.KeyFactory.getInstance("RSA")
                    val pubKeySpec = java.security.spec.RSAPublicKeySpec(
                        (privateKey as java.security.interfaces.RSAPrivateKey).modulus,
                        BigInteger("010001", 16) // Default public exponent
                    )
                    val pubKey = keyFactory.generatePublic(pubKeySpec) as java.security.interfaces.RSAPublicKey
                    pubKey.publicExponent.toString(16).uppercase()
                } catch (e: Exception) {
                    "010001" // Default if we can't derive it
                }
            }
            else -> "010001"
        }

        val privateExponent = when {
            privateKey is java.security.interfaces.RSAPrivateKey -> privateKey.privateExponent.toString(16).uppercase()
            else -> "N/A"
        }

        val output = buildString {
            appendLine("[$timestamp]")
            appendLine("RSA: Data decrypt operation finished")
            appendLine("Key length: $keySize")
            appendLine("Decoding method: ${method.displayName}")
            appendLine("Encoded data: $encryptedData")
            appendLine("Public modulus [n]: $modulus")
            appendLine("Public exponent [e]: $publicExponent")
            appendLine("Private exponent [d]: $privateExponent")
            appendLine("Padding Method: ${padding.displayName}")
            appendLine("Output format: ${outputFormat.displayName}")
            appendLine("Decoded data: $decryptedDataDisplay")
            appendLine("Decoded data length: ${decryptedBytes.size}")
        }

        onComplete(output)
    } catch (e: Exception) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val timestamp = LocalDateTime.now().format(formatter)
        
        val errorMessage = when {
            e.message?.contains("NoPadding") == true -> "pkcs decoding error"
            else -> e.message ?: "Unknown error"
        }

        val output = buildString {
            appendLine("[$timestamp]")
            appendLine("RSA: Error while decoding message")
            appendLine("Error: $errorMessage")
        }
        onComplete(output)
    }
}

private fun signRSAData(
    dataInput: String,
    encoding: DataEncoding,
    hashAlgorithm: RSAHashAlgorithm,
    onComplete: (output: String) -> Unit
) {
    try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val timestamp = LocalDateTime.now().format(formatter)

        val output = buildString {
            appendLine("=== RSA SIGNATURE ===")
            appendLine("Timestamp: $timestamp")
            appendLine("Hash Algorithm: ${hashAlgorithm.displayName}")
            appendLine("Encoding: ${encoding.name}")
            appendLine("Data: $dataInput")
            appendLine()
            appendLine("Status: No key loaded for signing")
        }
        onComplete(output)
    } catch (e: Exception) {
        onComplete("Error: ${e.message}")
    }
}

private fun verifyRSASignature(
    dataInput: String,
    signatureInput: String,
    hashAlgorithm: RSAHashAlgorithm,
    onComplete: (output: String) -> Unit
) {
    try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val timestamp = LocalDateTime.now().format(formatter)

        val output = buildString {
            appendLine("=== RSA VERIFY ===")
            appendLine("Timestamp: $timestamp")
            appendLine("Hash Algorithm: ${hashAlgorithm.displayName}")
            appendLine("Data: $dataInput")
            appendLine("Signature: $signatureInput")
            appendLine()
            appendLine("Status: No key loaded for verification")
        }
        onComplete(output)
    } catch (e: Exception) {
        onComplete("Error: ${e.message}")
    }
}

private fun processOAEPPadding(
    dataInput: String,
    labelInput: String,
    keyLength: RSAKeyLength,
    hashAlgorithm: RSAHashAlgorithm,
    method: String,
    onComplete: (output: String) -> Unit
) {
    try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val timestamp = LocalDateTime.now().format(formatter)

        val output = buildString {
            appendLine("=== OAEP PADDING ===")
            appendLine("Timestamp: $timestamp")
            appendLine("Method: $method")
            appendLine("Key Length: ${keyLength.displayName}")
            appendLine("Hash Algorithm: ${hashAlgorithm.displayName}")
            appendLine("Data: $dataInput")
            if (labelInput.isNotEmpty()) {
                appendLine("Label: $labelInput")
            }
            appendLine()
            appendLine("Status: OAEP padding simulation")
        }
        onComplete(output)
    } catch (e: Exception) {
        onComplete("Error: ${e.message}")
    }
}
