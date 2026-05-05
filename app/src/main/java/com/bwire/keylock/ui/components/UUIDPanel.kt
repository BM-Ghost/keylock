package com.bwire.keylock.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bwire.keylock.domain.crypto.UUIDEngine
import com.bwire.keylock.domain.crypto.UUIDVariant
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * UUID Generator Panel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UUIDPanel(
    onExecute: (ConsoleMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedVariant by remember { mutableStateOf(UUIDVariant.VERSION_4_RANDOM) }
    var countText by remember { mutableStateOf("1") }
    var variantExpanded by remember { mutableStateOf(false) }

    val countValue = countText.toIntOrNull()?.coerceAtLeast(1) ?: 1

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UUID",
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

            // Variant
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Variant:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(120.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                ExposedDropdownMenuBox(
                    expanded = variantExpanded,
                    onExpandedChange = { variantExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedVariant.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = MediumGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = variantExpanded,
                        onDismissRequest = { variantExpanded = false }
                    ) {
                        UUIDVariant.entries.forEach { variant ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = variant.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedVariant = variant
                                    variantExpanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Count:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(120.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = countText,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() } && value.length <= 4) {
                                countText = value
                            } else if (value.isBlank()) {
                                countText = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
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

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val newValue = (countValue + 1).coerceAtMost(1000)
                                countText = newValue.toString()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Increase",
                                tint = NeonGreen
                            )
                        }
                        IconButton(
                            onClick = {
                                val newValue = (countValue - 1).coerceAtLeast(1)
                                countText = newValue.toString()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Decrease",
                                tint = NeonGreen
                            )
                        }
                    }
                }
            }

            Divider(color = MediumGreen)

            // Generate button
            Button(
                onClick = {
                    generateOperation(selectedVariant, countValue, onExecute)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = DarkestGreen
                )
            ) {
                Text("GENERATE")
            }
        }
    }
}

private fun generateOperation(
    variant: UUIDVariant,
    count: Int,
    onExecute: (ConsoleMessage) -> Unit
) {
    val result = UUIDEngine.generate(variant, count)

    result.onSuccess { values ->
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

        val message = buildString {
            appendLine("[$timestamp]")
            appendLine("UUID: Generate UUID ${variant.logLabel} finished")
            appendLine("****************************************")
            values.forEachIndexed { index, value ->
                appendLine("UUID #${index + 1}:\t\t$value")
            }
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
