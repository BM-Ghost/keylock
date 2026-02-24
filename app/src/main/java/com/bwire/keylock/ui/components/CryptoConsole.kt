package com.bwire.keylock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import com.bwire.keylock.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Collapsible Cryptographic Console Component
 * Displays timestamped operation logs at the bottom with expand/collapse
 */
@Composable
fun CryptoConsoleCollapsible(
    messages: List<ConsoleMessage>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    
    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && isExpanded) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Surface(
        modifier = modifier,
        color = ConsoleBackground
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Console header with collapse/expand button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Text(
                        text = "Operation Log",
                        style = MaterialTheme.typography.titleSmall,
                        color = ConsoleText
                    )
                    
                    if (messages.isNotEmpty()) {
                        Surface(
                            color = DarkGreen,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "${messages.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Copy all button
                    IconButton(
                        onClick = {
                            if (messages.isNotEmpty()) {
                                val allText = messages.joinToString("\n\n") { message ->
                                    val timestamp = dateFormat.format(Date(message.timestamp))
                                    "[$timestamp]\n${message.message}"
                                }
                                clipboardManager.setText(AnnotatedString(allText))
                            }
                        },
                        modifier = Modifier.size(28.dp),
                        enabled = messages.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy All",
                            tint = if (messages.isNotEmpty()) TextSecondary else TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // Clear button
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(28.dp),
                        enabled = messages.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = if (messages.isNotEmpty()) TextSecondary else TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // Expandable console content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Divider(color = MediumGreen)
                    
                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(16.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(
                                text = "Operation log ready. Execute tools to see output.",
                                style = ConsoleMediumTextStyle,
                                color = TextTertiary
                            )
                        }
                    } else {
                        // Wrap in SelectionContainer to enable text selection
                        SelectionContainer {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp),
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                items(messages) { message ->
                                    ConsoleMessageItem(message)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Cryptographic Console Component
 * Displays timestamped operation logs with color-coded messages
 * Supports copy, clear, and scrollable output
 */
@Composable
fun CryptoConsole(
    messages: List<ConsoleMessage>,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Surface(
        modifier = modifier,
        color = ConsoleBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Console header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Operation Log (Read-only)",
                    style = MaterialTheme.typography.titleSmall,
                    color = ConsoleText
                )
                
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Divider(color = MediumGreen)
            
            // Console messages
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "Operation log ready. Execute tools to see output.",
                        style = ConsoleMediumTextStyle,
                        color = TextTertiary
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(messages) { message ->
                        ConsoleMessageItem(message)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsoleMessageItem(message: ConsoleMessage) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val timestamp = dateFormat.format(Date(message.timestamp))
    
    val messageColor = when (message.level) {
        MessageLevel.INFO -> ConsoleText
        MessageLevel.SUCCESS -> ConsoleSuccess
        MessageLevel.WARNING -> ConsoleWarning
        MessageLevel.ERROR -> ConsoleError
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "[$timestamp]",
            style = ConsoleSmallTextStyle,
            color = ConsoleTimestamp,
            fontFamily = FontFamily.Monospace
        )
        
        Text(
            text = message.message,
            style = ConsoleMediumTextStyle,
            color = messageColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
