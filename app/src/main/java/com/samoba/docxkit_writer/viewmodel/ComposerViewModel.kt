package com.samoba.docxkit_writer.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.samoba.docxkit.DocxWriter
import com.samoba.docxkit.config.DocxConfig
import com.samoba.docxkit.config.PageSize
import com.samoba.docxkit.ext.writeDirectToUri
import com.samoba.docxkit.model.DocxBoundingBox
import com.samoba.docxkit.model.DocxStructuredText
import com.samoba.docxkit.model.DocxTextBlock
import com.samoba.docxkit_writer.model.DocumentState
import com.samoba.docxkit_writer.model.PageSizeType
import com.samoba.docxkit_writer.model.TextBoxElement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ComposerViewModel : ViewModel() {
    
    private val _documentState = MutableStateFlow(DocumentState())
    val documentState: StateFlow<DocumentState> = _documentState.asStateFlow()
    
    private val _showTextEditDialog = MutableStateFlow<TextBoxElement?>(null)
    val showTextEditDialog: StateFlow<TextBoxElement?> = _showTextEditDialog.asStateFlow()
    
    private val _showPageSizeDialog = MutableStateFlow(false)
    val showPageSizeDialog: StateFlow<Boolean> = _showPageSizeDialog.asStateFlow()
    
    /**
     * Add a new text box to the canvas.
     */
    fun addTextBox() {
        val newBox = TextBoxElement(
            x = 50f + (_documentState.value.textBoxes.size * 20f) % 200f,
            y = 50f + (_documentState.value.textBoxes.size * 20f) % 300f
        )
        _documentState.update { state ->
            state.copy(
                textBoxes = state.textBoxes + newBox,
                selectedBoxId = newBox.id
            )
        }
    }
    
    /**
     * Select a text box.
     */
    fun selectTextBox(id: String?) {
        _documentState.update { it.copy(selectedBoxId = id) }
    }
    
    /**
     * Update text box position after drag.
     */
    fun updateTextBoxPosition(id: String, x: Float, y: Float) {
        _documentState.update { state ->
            state.copy(
                textBoxes = state.textBoxes.map { box ->
                    if (box.id == id) box.copy(x = x.coerceAtLeast(0f), y = y.coerceAtLeast(0f))
                    else box
                }
            )
        }
    }
    
    /**
     * Update text box size after resize.
     */
    fun updateTextBoxSize(id: String, width: Float, height: Float) {
        _documentState.update { state ->
            state.copy(
                textBoxes = state.textBoxes.map { box ->
                    if (box.id == id) box.copy(
                        width = width.coerceAtLeast(50f),
                        height = height.coerceAtLeast(30f)
                    )
                    else box
                }
            )
        }
    }
    
    /**
     * Update text box content and formatting.
     */
    fun updateTextBox(
        id: String,
        text: String,
        fontSize: Int,
        isBold: Boolean,
        isItalic: Boolean
    ) {
        _documentState.update { state ->
            state.copy(
                textBoxes = state.textBoxes.map { box ->
                    if (box.id == id) box.copy(
                        text = text,
                        fontSize = fontSize,
                        isBold = isBold,
                        isItalic = isItalic
                    )
                    else box
                }
            )
        }
    }
    
    /**
     * Delete the selected text box.
     */
    fun deleteSelectedTextBox() {
        val selectedId = _documentState.value.selectedBoxId ?: return
        _documentState.update { state ->
            state.copy(
                textBoxes = state.textBoxes.filter { it.id != selectedId },
                selectedBoxId = null
            )
        }
    }
    
    /**
     * Show text edit dialog for a text box.
     */
    fun showEditDialog(box: TextBoxElement) {
        _showTextEditDialog.value = box
    }
    
    /**
     * Hide text edit dialog.
     */
    fun hideEditDialog() {
        _showTextEditDialog.value = null
    }
    
    /**
     * Show page size selection dialog.
     */
    fun showPageSizeDialog() {
        _showPageSizeDialog.value = true
    }
    
    /**
     * Hide page size selection dialog.
     */
    fun hidePageSizeDialog() {
        _showPageSizeDialog.value = false
    }
    
    /**
     * Set the page size.
     */
    fun setPageSize(size: PageSizeType) {
        _documentState.update { it.copy(pageSize = size) }
        _showPageSizeDialog.value = false
    }
    
    /**
     * Export the document to DOCX format.
     */
    fun exportToDocx(context: Context, uri: Uri): Result<Unit> {
        return try {
            val state = _documentState.value
            
            // Configure DocxWriter with page size
            val pageSize = when (state.pageSize) {
                PageSizeType.A4 -> PageSize.A4
                PageSizeType.LETTER -> PageSize.Letter
                PageSizeType.LEGAL -> PageSize.Legal
                PageSizeType.A3 -> PageSize.A3
                PageSizeType.A5 -> PageSize.A5
            }
            
            // Convert text boxes to DocxKit models
            // Pass dp coordinates directly - DocxWriter.writeDirect converts dp to EMU
            val blocks = state.textBoxes.map { box ->
                DocxTextBlock(
                    lines = emptyList(),
                    text = box.text,
                    isBold = box.isBold,
                    isItalic = box.isItalic,
                    fontSize = box.fontSize,
                    boundingBox = DocxBoundingBox(
                        left = box.x.toInt(),
                        top = box.y.toInt(),
                        right = (box.x + box.width).toInt(),
                        bottom = (box.y + box.height).toInt()
                    )
                )
            }
            
            val content = DocxStructuredText.fromBlocks(blocks)
            
            val config = DocxConfig.builder()
                .pageSize(pageSize)
                .build()
            
            val writer = DocxWriter(config)
            
            // writeDirect uses dp coordinates directly (1 dp = 12700 EMU)
            writer.writeDirectToUri(context, content, uri)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
