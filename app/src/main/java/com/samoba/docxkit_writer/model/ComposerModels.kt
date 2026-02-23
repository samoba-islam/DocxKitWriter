package com.samoba.docxkit_writer.model

import java.util.UUID

/**
 * Represents a text box element on the composer canvas.
 */
data class TextBoxElement(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "Text",
    val x: Float = 50f,
    val y: Float = 50f,
    val width: Float = 200f,
    val height: Float = 60f,
    val fontSize: Int = 14,
    val fontFamily: String = "Arial",
    val isBold: Boolean = false,
    val isItalic: Boolean = false
)

/**
 * Available page size options for the document.
 */
enum class PageSizeType(val displayName: String, val widthDp: Float, val heightDp: Float) {
    A4("A4", 595f, 842f),
    LETTER("Letter", 612f, 792f),
    LEGAL("Legal", 612f, 1008f),
    A3("A3", 842f, 1191f),
    A5("A5", 420f, 595f)
}

/**
 * Represents the complete document state in the composer.
 */
data class DocumentState(
    val textBoxes: List<TextBoxElement> = emptyList(),
    val selectedBoxId: String? = null,
    val pageSize: PageSizeType = PageSizeType.A4
)
