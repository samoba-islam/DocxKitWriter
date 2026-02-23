package com.samoba.docxkit.model

/**
 * Represents a single text element (word or symbol) with its bounding box.
 *
 * @param text The text content of this element
 * @param boundingBox The bounding box of this element in pixel coordinates
 * @param confidence Optional confidence score from OCR (0.0 to 1.0)
 */
data class DocxTextElement(
    val text: String,
    val boundingBox: DocxBoundingBox?,
    val confidence: Float? = null
)
