package com.samoba.docxkit.model

/**
 * Represents a line of text containing multiple elements.
 *
 * @param elements List of text elements in this line
 * @param text The complete text of this line (concatenated elements)
 * @param boundingBox The bounding box of the entire line in pixel coordinates
 * @param confidence Optional confidence score from OCR (0.0 to 1.0)
 */
data class DocxTextLine(
    val elements: List<DocxTextElement>,
    val text: String,
    val boundingBox: DocxBoundingBox?,
    val confidence: Float? = null
)
