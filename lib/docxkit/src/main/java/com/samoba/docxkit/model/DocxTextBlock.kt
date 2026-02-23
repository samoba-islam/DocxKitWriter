package com.samoba.docxkit.model

/**
 * Represents a block of text containing multiple lines.
 * A block is typically a paragraph or a logically grouped section of text.
 *
 * @param lines List of text lines in this block
 * @param text The complete text of this block
 * @param boundingBox The bounding box of the entire block in pixel coordinates
 * @param cornerPoints Optional corner points for rotated text [topLeft, topRight, bottomRight, bottomLeft] each as [x, y]
 * @param confidence Optional confidence score from OCR (0.0 to 1.0)
 * @param pageIndex Page index for multi-page documents (0-indexed)
 */
data class DocxTextBlock(
    val lines: List<DocxTextLine>,
    val text: String,
    val boundingBox: DocxBoundingBox?,
    val cornerPoints: List<IntArray>? = null,
    val confidence: Float? = null,
    val pageIndex: Int = 0,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val fontSize: Int? = null
)
