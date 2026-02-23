package com.samoba.docxkit.model

/**
 * Container for structured text content to be written to DOCX.
 *
 * @param blocks List of text blocks representing the document content
 * @param fullText The complete text content (used as fallback when blocks have no positioning data)
 */
data class DocxStructuredText(
    val blocks: List<DocxTextBlock>,
    val fullText: String
) {
    companion object {
        /**
         * Creates a simple structured text with just plain text (no positioning).
         */
        fun fromPlainText(text: String): DocxStructuredText {
            return DocxStructuredText(
                blocks = emptyList(),
                fullText = text
            )
        }

        /**
         * Creates structured text from a list of blocks.
         */
        fun fromBlocks(blocks: List<DocxTextBlock>): DocxStructuredText {
            return DocxStructuredText(
                blocks = blocks,
                fullText = blocks.joinToString("\n") { it.text }
            )
        }
    }
}
