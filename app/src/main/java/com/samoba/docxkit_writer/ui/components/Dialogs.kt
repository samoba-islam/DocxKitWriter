package com.samoba.docxkit_writer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.samoba.docxkit_writer.model.TextBoxElement

@Composable
fun TextEditDialog(
    textBox: TextBoxElement,
    onDismiss: () -> Unit,
    onConfirm: (text: String, fontSize: Int, isBold: Boolean, isItalic: Boolean) -> Unit
) {
    var text by remember { mutableStateOf(textBox.text) }
    var fontSize by remember { mutableFloatStateOf(textBox.fontSize.toFloat()) }
    var isBold by remember { mutableStateOf(textBox.isBold) }
    var isItalic by remember { mutableStateOf(textBox.isItalic) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Text") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Text Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Font size slider
                Text(
                    text = "Font Size: ${fontSize.toInt()}pt",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 8f..48f,
                    steps = 39,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Formatting options
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = isBold,
                        onClick = { isBold = !isBold },
                        label = { Text("Bold") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FormatBold,
                                contentDescription = "Bold"
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FilterChip(
                        selected = isItalic,
                        onClick = { isItalic = !isItalic },
                        label = { Text("Italic") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FormatItalic,
                                contentDescription = "Italic"
                            )
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text, fontSize.toInt(), isBold, isItalic) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PageSizeDialog(
    currentSize: com.samoba.docxkit_writer.model.PageSizeType,
    onDismiss: () -> Unit,
    onSelect: (com.samoba.docxkit_writer.model.PageSizeType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Page Size") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                com.samoba.docxkit_writer.model.PageSizeType.entries.forEach { size ->
                    FilterChip(
                        selected = size == currentSize,
                        onClick = { onSelect(size) },
                        label = { 
                            Text("${size.displayName} (${size.widthDp.toInt()} × ${size.heightDp.toInt()})") 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
