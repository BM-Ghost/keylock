package com.bwire.keylock.ui.screens.cryptography

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.bwire.keylock.ui.theme.DarkestGreen
import com.bwire.keylock.ui.theme.MediumGreen
import com.bwire.keylock.ui.theme.NeonGreen
import com.bwire.keylock.ui.theme.TextPrimary
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import javax.crypto.Cipher

@Composable
fun RSAPanel() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Keys", "Encrypt", "Decrypt", "Sign", "Verify", "OAEP")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkestGreen)
            .padding(8.dp)
    ) {
        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = DarkestGreen,
            contentColor = NeonGreen,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = NeonGreen
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            title,
                            color = if (selectedTabIndex == index) NeonGreen else MediumGreen
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkestGreen)
                .padding(12.dp)
        ) {
            when (selectedTabIndex) {
                0 -> RSAKeysTab()
                1 -> RSAEncryptTab()
                2 -> RSADecryptTab()
                3 -> RSASignTab()
                4 -> RSAVerifyTab()
                5 -> RSAOAEPTab()
            }
        }
    }
}

@Composable
private fun RSAKeysTab() {
    var keyLength by remember { mutableStateOf(RSAKeyLength.RSA_2048) }
    var publicKey by remember { mutableStateOf<PublicKey?>(null) }
    var privateKey by remember { mutableStateOf<PrivateKey?>(null) }
    var consoleOutput by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "RSA Key Generation",
            style = MaterialTheme.typography.titleLarge,
            color = NeonGreen
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

        // Generate Button
        Button(
            onClick = {
                isGenerating = true
                generateRSAKeyPair(keyLength) { output, pubKey, privKey ->
                    consoleOutput = output
                    publicKey = pubKey
                    privateKey = privKey
                    isGenerating = false
                }
            },
            enabled = !isGenerating,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
            Text(if (isGenerating) "Generating..." else "Generate Keys", color = DarkestGreen)
        }

        // Console Output
        if (consoleOutput.isNotEmpty()) {
            Text("Output:", color = TextPrimary)
            ConsoleOutputBox(consoleOutput)
        }

        // Display Keys if Generated
        if (publicKey != null && privateKey != null) {
            Divider(color = MediumGreen, thickness = 1.dp)

            Text("Generated Keys (Hexadecimal Format):", color = TextPrimary)
            
            // Extract key components
            val rsaPublicKey = publicKey as? java.security.interfaces.RSAPublicKey
            val rsaPrivateKey = privateKey as? java.security.interfaces.RSAPrivateKey
            
            if (rsaPublicKey != null && rsaPrivateKey != null) {
                val modulusHex = rsaPublicKey.modulus.toString(16).uppercase()
                val publicExpHex = rsaPublicKey.publicExponent.toString(16).uppercase()
                val privateExpHex = rsaPrivateKey.privateExponent.toString(16).uppercase()
                
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = DarkestGreen
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Modulus (n)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("Modulus (n):", color = NeonGreen, modifier = Modifier.weight(1f))
                            Text(
                                "[${modulusHex.length}]",
                                color = NeonGreen,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        SelectionContainer {
                            Text(
                                modulusHex,
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Divider(color = MediumGreen, thickness = 1.dp)
                        
                        // Public Exponent (e)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("Public Exponent (e):", color = NeonGreen, modifier = Modifier.weight(1f))
                            Text(
                                "[${publicExpHex.length}]",
                                color = NeonGreen,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        SelectionContainer {
                            Text(publicExpHex, color = TextPrimary)
                        }
                        
                        Divider(color = MediumGreen, thickness = 1.dp)
                        
                        // Private Exponent (d)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("Private Exponent (d):", color = NeonGreen, modifier = Modifier.weight(1f))
                            Text(
                                "[${privateExpHex.length}]",
                                color = NeonGreen,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        SelectionContainer {
                            Text(
                                privateExpHex,
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Divider(color = MediumGreen, thickness = 1.dp)
                        
                        // Key Length
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Key Length:", color = TextPrimary)
                            Text(
                                keyLength.bits.toString(),
                                color = NeonGreen,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RSAEncryptTab() {
    var dataInput by remember { mutableStateOf("") }
    var encryptionMethod by remember { mutableStateOf(RSAEncryptionMethod.PUBLIC) }
    var padding by remember { mutableStateOf(RSAPadding.PKCS1) }
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
            "RSA Encryption",
            style = MaterialTheme.typography.titleLarge,
            color = NeonGreen
        )

        // Data Input
        Text("Data to Encrypt (Hex):", color = TextPrimary)
        OutlinedTextField(
            value = dataInput,
            onValueChange = { dataInput = it.uppercase() },
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

        // Encryption Method
        Text("Encryption Method:", color = TextPrimary)
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

        // Padding Selector
        Text("Padding:", color = TextPrimary)
        var expandedPadding by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedPadding = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeonGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.foundation.BorderStroke(1.dp, MediumGreen).brush
                )
            ) {
                Text(padding.displayName)
            }

            DropdownMenu(
                expanded = expandedPadding,
                onDismissRequest = { expandedPadding = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MediumGreen)
            ) {
                RSAPadding.values().forEach { pad ->
                    DropdownMenuItem(
                        text = { Text(pad.displayName, color = DarkestGreen) },
                        onClick = {
                            padding = pad
                            expandedPadding = false
                        }
                    )
                }
            }
        }

        // Encrypt Button
        Button(
            onClick = {
                isProcessing = true
                encryptRSAData(dataInput, encryptionMethod, padding) { output ->
                    consoleOutput = output
                    isProcessing = false
                }
            },
            enabled = !isProcessing && dataInput.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
            Text(if (isProcessing) "Encrypting..." else "Encrypt", color = DarkestGreen)
        }

        // Console Output
        if (consoleOutput.isNotEmpty()) {
            Text("Output:", color = TextPrimary)
            ConsoleOutputBox(consoleOutput)
        }
    }
}

@Composable
private fun RSADecryptTab() {
    var encryptedData by remember { mutableStateOf("") }
    var padding by remember { mutableStateOf(RSAPadding.PKCS1) }
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
            "RSA Decryption",
            style = MaterialTheme.typography.titleLarge,
            color = NeonGreen
        )

        // Encrypted Data Input
        Text("Encrypted Data (Hex):", color = TextPrimary)
        OutlinedTextField(
            value = encryptedData,
            onValueChange = { encryptedData = it.uppercase() },
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

        // Padding Selector
        Text("Padding:", color = TextPrimary)
        var expandedPadding by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedPadding = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeonGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.foundation.BorderStroke(1.dp, MediumGreen).brush
                )
            ) {
                Text(padding.displayName)
            }

            DropdownMenu(
                expanded = expandedPadding,
                onDismissRequest = { expandedPadding = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MediumGreen)
            ) {
                RSAPadding.values().forEach { pad ->
                    DropdownMenuItem(
                        text = { Text(pad.displayName, color = DarkestGreen) },
                        onClick = {
                            padding = pad
                            expandedPadding = false
                        }
                    )
                }
            }
        }

        // Decrypt Button
        Button(
            onClick = {
                isProcessing = true
                decryptRSAData(encryptedData, padding) { output ->
                    consoleOutput = output
                    isProcessing = false
                }
            },
            enabled = !isProcessing && encryptedData.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
            Text(if (isProcessing) "Decrypting..." else "Decrypt", color = DarkestGreen)
        }

        // Console Output
        if (consoleOutput.isNotEmpty()) {
            Text("Output:", color = TextPrimary)
            ConsoleOutputBox(consoleOutput)
        }
    }
}

@Composable
private fun RSASignTab() {
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
private fun RSAVerifyTab() {
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
private fun RSAOAEPTab() {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
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

// Placeholders - these need to be implemented in CryptoEngine.kt
private fun generateRSAKeyPair(
    keyLength: RSAKeyLength,
    onComplete: (output: String, pubKey: PublicKey?, privKey: PrivateKey?) -> Unit
) {
    try {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(keyLength.bits)
        val keyPair = keyPairGenerator.generateKeyPair()

        val pubKey = keyPair.public
        val privKey = keyPair.private

        val output = buildString {
            appendLine("=== RSA KEY GENERATION ===")
            appendLine("Timestamp: ${java.time.LocalDateTime.now()}")
            appendLine("Key Length: ${keyLength.displayName}")
            appendLine("Algorithm: RSA")
            appendLine()
            appendLine("Public Key Generated: ${pubKey.algorithm}")
            appendLine("Private Key Generated: ${privKey.algorithm}")
            appendLine()
            appendLine("Status: Keys generated successfully")
        }

        onComplete(output, pubKey, privKey)
    } catch (e: Exception) {
        onComplete("Error: ${e.message}", null, null)
    }
}

private fun encryptRSAData(
    dataInput: String,
    method: RSAEncryptionMethod,
    padding: RSAPadding,
    onComplete: (output: String) -> Unit
) {
    try {
        val output = buildString {
            appendLine("=== RSA ENCRYPTION ===")
            appendLine("Timestamp: ${java.time.LocalDateTime.now()}")
            appendLine("Method: ${method.displayName}")
            appendLine("Padding: ${padding.displayName}")
            appendLine("Input Data: $dataInput")
            appendLine()
            appendLine("Status: Encryption simulated (requires key)")
        }
        onComplete(output)
    } catch (e: Exception) {
        onComplete("Error: ${e.message}")
    }
}

private fun decryptRSAData(
    encryptedData: String,
    padding: RSAPadding,
    onComplete: (output: String) -> Unit
) {
    try {
        val output = buildString {
            appendLine("=== RSA DECRYPTION ===")
            appendLine("Timestamp: ${java.time.LocalDateTime.now()}")
            appendLine("Padding: ${padding.displayName}")
            appendLine("Encrypted Data: $encryptedData")
            appendLine()
            appendLine("Status: Decryption simulated (requires key)")
        }
        onComplete(output)
    } catch (e: Exception) {
        onComplete("Error: ${e.message}")
    }
}

private fun signRSAData(
    dataInput: String,
    encoding: DataEncoding,
    hashAlgorithm: RSAHashAlgorithm,
    onComplete: (output: String) -> Unit
) {
    try {
        val hashAlgo = when (hashAlgorithm) {
            RSAHashAlgorithm.SHA1 -> "SHA1withRSA"
            RSAHashAlgorithm.SHA224 -> "SHA224withRSA"
            RSAHashAlgorithm.SHA256 -> "SHA256withRSA"
            RSAHashAlgorithm.SHA384 -> "SHA384withRSA"
            RSAHashAlgorithm.SHA512 -> "SHA512withRSA"
        }

        val output = buildString {
            appendLine("=== RSA SIGNATURE ===")
            appendLine("Timestamp: ${java.time.LocalDateTime.now()}")
            appendLine("Algorithm: $hashAlgo")
            appendLine("Encoding: ${encoding.name}")
            appendLine("Data: $dataInput")
            appendLine()
            appendLine("Status: Signature generated (simulated)")
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
        val output = buildString {
            appendLine("=== RSA VERIFY ===")
            appendLine("Timestamp: ${java.time.LocalDateTime.now()}")
            appendLine("Hash Algorithm: ${hashAlgorithm.displayName}")
            appendLine("Data: $dataInput")
            appendLine("Signature: $signatureInput")
            appendLine()
            appendLine("Status: Verification successful (simulated)")
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
        val output = buildString {
            appendLine("=== OAEP PADDING ===")
            appendLine("Timestamp: ${java.time.LocalDateTime.now()}")
            appendLine("Method: $method")
            appendLine("Key Length: ${keyLength.displayName}")
            appendLine("Hash Algorithm: ${hashAlgorithm.displayName}")
            appendLine("Data: $dataInput")
            if (labelInput.isNotEmpty()) {
                appendLine("Label: $labelInput")
            }
            appendLine()
            appendLine("Status: OAEP padding $method completed (simulated)")
        }
        onComplete(output)
    } catch (e: Exception) {
        onComplete("Error: ${e.message}")
    }
}
