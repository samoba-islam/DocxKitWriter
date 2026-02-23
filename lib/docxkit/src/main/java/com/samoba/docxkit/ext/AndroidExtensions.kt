@file:JvmName("AndroidExtensions")

package com.samoba.docxkit.ext

import android.content.ContentResolver
import android.content.Context
import android.graphics.Rect
import android.net.Uri
import com.samoba.docxkit.DocxPage
import com.samoba.docxkit.DocxWriter
import com.samoba.docxkit.model.DocxBoundingBox
import com.samoba.docxkit.model.DocxStructuredText
import com.samoba.docxkit.model.DocxTextBlock
import com.samoba.docxkit.model.DocxTextElement
import com.samoba.docxkit.model.DocxTextLine
import java.io.File
import java.io.FileOutputStream

/**
 * Converts an Android Rect to a DocxBoundingBox.
 */
fun Rect.toDocxBoundingBox(): DocxBoundingBox {
    return DocxBoundingBox(left, top, right, bottom)
}

/**
 * Converts a DocxBoundingBox to an Android Rect.
 */
fun DocxBoundingBox.toAndroidRect(): Rect {
    return Rect(left, top, right, bottom)
}

/**
 * Writes a DOCX document to an Android Uri.
 *
 * @param context Android context for accessing ContentResolver
 * @param content The structured text content to write
 * @param outputUri The output Uri (supports both file:// and content:// schemes)
 */
fun DocxWriter.writeToUri(
    context: Context,
    content: DocxStructuredText,
    outputUri: Uri
) {
    val outputStream = if (outputUri.scheme == ContentResolver.SCHEME_FILE) {
        val file = File(outputUri.path ?: throw IllegalArgumentException("Invalid file URI path"))
        file.parentFile?.mkdirs()
        FileOutputStream(file)
    } else {
        context.contentResolver.openOutputStream(outputUri)
            ?: throw java.io.IOException("Unable to open output stream for URI: $outputUri")
    }

    outputStream.use { stream ->
        write(content, stream)
    }
}

/**
 * Writes a positioned DOCX document to an Android Uri.
 *
 * @param context Android context for accessing ContentResolver
 * @param content The structured text content to write
 * @param outputUri The output Uri
 * @param contentWidth Original content width in pixels
 * @param contentHeight Original content height in pixels
 */
fun DocxWriter.writePositionedToUri(
    context: Context,
    content: DocxStructuredText,
    outputUri: Uri,
    contentWidth: Int,
    contentHeight: Int
) {
    val outputStream = if (outputUri.scheme == ContentResolver.SCHEME_FILE) {
        val file = File(outputUri.path ?: throw IllegalArgumentException("Invalid file URI path"))
        file.parentFile?.mkdirs()
        FileOutputStream(file)
    } else {
        context.contentResolver.openOutputStream(outputUri)
            ?: throw java.io.IOException("Unable to open output stream for URI: $outputUri")
    }

    outputStream.use { stream ->
        writePositioned(content, stream, contentWidth, contentHeight)
    }
}

/**
 * Writes a DOCX document with direct coordinate positioning to an Android Uri.
 * Use this for visual composers where coordinates are already in page-relative units.
 *
 * @param context Android context for accessing ContentResolver
 * @param content The structured text content to write
 * @param outputUri The output Uri
 */
fun DocxWriter.writeDirectToUri(
    context: Context,
    content: DocxStructuredText,
    outputUri: Uri
) {
    val outputStream = if (outputUri.scheme == ContentResolver.SCHEME_FILE) {
        val file = File(outputUri.path ?: throw IllegalArgumentException("Invalid file URI path"))
        file.parentFile?.mkdirs()
        FileOutputStream(file)
    } else {
        context.contentResolver.openOutputStream(outputUri)
            ?: throw java.io.IOException("Unable to open output stream for URI: $outputUri")
    }

    outputStream.use { stream ->
        writeDirect(content, stream)
    }
}

/**
 * Writes a multi-page DOCX document to an Android Uri.
 *
 * @param context Android context for accessing ContentResolver
 * @param pages List of pages to include in the document
 * @param outputUri The output Uri
 */
fun DocxWriter.writeMultiPageToUri(
    context: Context,
    pages: List<DocxPage>,
    outputUri: Uri
) {
    val outputStream = if (outputUri.scheme == ContentResolver.SCHEME_FILE) {
        val file = File(outputUri.path ?: throw IllegalArgumentException("Invalid file URI path"))
        file.parentFile?.mkdirs()
        FileOutputStream(file)
    } else {
        context.contentResolver.openOutputStream(outputUri)
            ?: throw java.io.IOException("Unable to open output stream for URI: $outputUri")
    }

    outputStream.use { stream ->
        writeMultiPage(pages, stream)
    }
}

/**
 * Builder helper to create DocxTextBlock from Android Rect.
 */
fun docxTextBlock(
    text: String,
    boundingBox: Rect?,
    lines: List<DocxTextLine> = emptyList(),
    cornerPoints: List<IntArray>? = null,
    confidence: Float? = null,
    pageIndex: Int = 0
): DocxTextBlock {
    return DocxTextBlock(
        lines = lines,
        text = text,
        boundingBox = boundingBox?.toDocxBoundingBox(),
        cornerPoints = cornerPoints,
        confidence = confidence,
        pageIndex = pageIndex
    )
}

/**
 * Builder helper to create DocxTextLine from Android Rect.
 */
fun docxTextLine(
    text: String,
    boundingBox: Rect?,
    elements: List<DocxTextElement> = emptyList(),
    confidence: Float? = null
): DocxTextLine {
    return DocxTextLine(
        elements = elements,
        text = text,
        boundingBox = boundingBox?.toDocxBoundingBox(),
        confidence = confidence
    )
}

/**
 * Builder helper to create DocxTextElement from Android Rect.
 */
fun docxTextElement(
    text: String,
    boundingBox: Rect?,
    confidence: Float? = null
): DocxTextElement {
    return DocxTextElement(
        text = text,
        boundingBox = boundingBox?.toDocxBoundingBox(),
        confidence = confidence
    )
}
