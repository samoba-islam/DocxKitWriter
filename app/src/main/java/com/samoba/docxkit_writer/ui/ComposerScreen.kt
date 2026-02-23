package com.samoba.docxkit_writer.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.samoba.docxkit_writer.ui.components.DraggableTextBox
import com.samoba.docxkit_writer.ui.components.PageSizeDialog
import com.samoba.docxkit_writer.ui.components.TextEditDialog
import com.samoba.docxkit_writer.viewmodel.ComposerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerScreen(
    viewModel: ComposerViewModel = viewModel()
) {
    val documentState by viewModel.documentState.collectAsState()
    val showTextEditDialog by viewModel.showTextEditDialog.collectAsState()
    val showPageSizeDialog by viewModel.showPageSizeDialog.collectAsState()
    val context = LocalContext.current
    
    // File picker for saving DOCX
    val saveDocxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    ) { uri ->
        uri?.let {
            val result = viewModel.exportToDocx(context, it)
            if (result.isSuccess) {
                Toast.makeText(context, "Document saved successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("DocxKit Composer") 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Page size button
                    IconButton(onClick = { viewModel.showPageSizeDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Page Size"
                        )
                    }
                    // Save button
                    IconButton(
                        onClick = { saveDocxLauncher.launch("document.docx") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Document"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                actions = {
                    // Edit selected text box
                    IconButton(
                        onClick = {
                            documentState.selectedBoxId?.let { id ->
                                documentState.textBoxes.find { it.id == id }?.let {
                                    viewModel.showEditDialog(it)
                                }
                            }
                        },
                        enabled = documentState.selectedBoxId != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Text"
                        )
                    }
                    
                    // Delete selected text box
                    IconButton(
                        onClick = { viewModel.deleteSelectedTextBox() },
                        enabled = documentState.selectedBoxId != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { viewModel.addTextBox() },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Text Box"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page info bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${documentState.pageSize.displayName} • ${documentState.textBoxes.size} elements",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Document canvas with scrolling
            // Document canvas with scrolling
            androidx.compose.foundation.layout.BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // Calculate scale factor to fit page width within screen
                // Use actual page dimensions from preset (in points/dp)
                val pageWidthPoints = documentState.pageSize.widthDp
                val pageHeightPoints = documentState.pageSize.heightDp
                
                // Calculate scale to fit width (with some visual padding considerations if needed)
                val availableWidth = maxWidth.value
                val scaleFactor = if (pageWidthPoints > 0) availableWidth / pageWidthPoints else 1f
                
                val scaledWidth = pageWidthPoints * scaleFactor
                val scaledHeight = pageHeightPoints * scaleFactor
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Page canvas - visually scaled
                    // Key forces recomposition when page size or scale changes
                    androidx.compose.runtime.key(documentState.pageSize, scaleFactor) {
                        Box(
                            modifier = Modifier
                                .size(
                                    width = scaledWidth.dp,
                                    height = scaledHeight.dp
                                )
                                .shadow(8.dp, RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            viewModel.selectTextBox(null)
                                        }
                                    )
                                }
                        ) {
                        // Text boxes - scaled for display
                        documentState.textBoxes.forEach { textBox ->
                            // Scale the text box for visual display
                            val scaledTextBox = textBox.copy(
                                x = textBox.x * scaleFactor,
                                y = textBox.y * scaleFactor,
                                width = textBox.width * scaleFactor,
                                height = textBox.height * scaleFactor,
                                fontSize = (textBox.fontSize * scaleFactor).toInt().coerceAtLeast(1)
                            )
                            
                            DraggableTextBox(
                                textBox = scaledTextBox,
                                isSelected = textBox.id == documentState.selectedBoxId,
                                pageWidthDp = scaledWidth,
                                pageHeightDp = scaledHeight,
                                onSelect = { viewModel.selectTextBox(textBox.id) },
                                onPositionChange = { x, y ->
                                    // Convert scaled coordinates back to page coordinates
                                    viewModel.updateTextBoxPosition(
                                        textBox.id, 
                                        x / scaleFactor, 
                                        y / scaleFactor
                                    )
                                },
                                onSizeChange = { width, height ->
                                    // Convert scaled size back to page size
                                    viewModel.updateTextBoxSize(
                                        textBox.id, 
                                        width / scaleFactor, 
                                        height / scaleFactor
                                    )
                                },
                                onDoubleClick = { viewModel.showEditDialog(textBox) }
                            )
                        }
                        
                        // Empty state hint
                        if (documentState.textBoxes.isEmpty()) {
                            Text(
                                text = "Tap + to add a text box",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        }
                    }
                }
            }
        }
    }
    
    // Text Edit Dialog
    showTextEditDialog?.let { textBox ->
        TextEditDialog(
            textBox = textBox,
            onDismiss = { viewModel.hideEditDialog() },
            onConfirm = { text, fontSize, isBold, isItalic ->
                viewModel.updateTextBox(
                    textBox.id,
                    text,
                    fontSize,
                    isBold,
                    isItalic
                )
                viewModel.hideEditDialog()
            }
        )
    }
    
    // Page Size Dialog
    if (showPageSizeDialog) {
        PageSizeDialog(
            currentSize = documentState.pageSize,
            onDismiss = { viewModel.hidePageSizeDialog() },
            onSelect = { viewModel.setPageSize(it) }
        )
    }
}
