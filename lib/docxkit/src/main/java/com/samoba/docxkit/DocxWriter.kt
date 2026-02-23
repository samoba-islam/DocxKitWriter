package com.samoba.docxkit

import com.samoba.docxkit.config.DocxConfig
import com.samoba.docxkit.config.PageSize
import com.samoba.docxkit.model.DocxStructuredText
import com.samoba.docxkit.model.DocxTextBlock
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Main class for generating DOCX files.
 *
 * DocxWriter creates DOCX files using pure OOXML (Office Open XML) format,
 * which is a ZIP archive containing XML files. No external dependencies required.
 *
 * Features:
 * - Page size configuration (A4, Letter, custom sizes)
 * - Coordinate-based text positioning with bounding boxes
 * - Text rotation support
 * - Multi-page document generation
 * - Auto-scaling content to fit page
 *
 * Example usage:
 * ```kotlin
 * val writer = DocxWriter()
 * val content = DocxStructuredText.fromPlainText("Hello World!")
 * FileOutputStream("output.docx").use { output ->
 *     writer.write(content, output)
 * }
 * ```
 *
 * @param config Configuration for document generation
 */
class DocxWriter(private val config: DocxConfig = DocxConfig()) {

    /**
     * Writes a simple DOCX document without text positioning.
     *
     * @param content The structured text content to write
     * @param output The output stream to write to
     */
    fun write(content: DocxStructuredText, output: OutputStream) {
        val paragraphsXml = buildSimpleParagraphsXml(content)
        val documentXml = createSimpleDocumentXml(
            paragraphsXml,
            config.pageSize.widthTwips,
            config.pageSize.heightTwips
        )
        writeDocxFile(output, documentXml)
    }

    /**
     * Writes a DOCX document with direct coordinate positioning (no scaling or margins).
     * Use this for visual composers where coordinates are already in page-relative dp/points.
     * Coordinates in bounding boxes are treated as dp (points) and converted to EMU directly.
     * Formula: 1 dp = 1 point = 914400/72 = 12700 EMU
     *
     * @param content The structured text content to write
     * @param output The output stream to write to
     */
    fun writeDirect(content: DocxStructuredText, output: OutputStream) {
        // EMU_PER_POINT = 914400 / 72 = 12700 (since 1 inch = 914400 EMU and 72 points = 1 inch)
        val emuPerPoint = 12700.0
        val textBoxesXml = buildTextBoxesXml(content, emuPerPoint, 0, 0L, 0L)
        val documentXml = createDocumentXml(
            textBoxesXml,
            config.pageSize.widthTwips,
            config.pageSize.heightTwips
        )
        writeDocxFile(output, documentXml)
    }

    /**
     * Writes a DOCX document with positioned text boxes based on bounding boxes.
     * Text is scaled and centered to fit within the page.
     *
     * @param content The structured text content to write
     * @param output The output stream to write to
     * @param contentWidth Original content width in pixels
     * @param contentHeight Original content height in pixels
     */
    fun writePositioned(
        content: DocxStructuredText,
        output: OutputStream,
        contentWidth: Int,
        contentHeight: Int
    ) {
        // Group blocks by pageIndex to handle multi-page documents
        val blocksByPage = content.blocks.groupBy { it.pageIndex }

        if (blocksByPage.size <= 1) {
            // Single page
            val layout = calculateContentLayout(contentWidth, contentHeight)
            val emuScale = UnitConverter.EMU_PER_PIXEL.toDouble() * layout.scaleFactor
            val textBoxesXml = buildTextBoxesXml(content, emuScale, 0, layout.offsetXEmu, layout.offsetYEmu)
            val documentXml = createDocumentXml(
                textBoxesXml,
                config.pageSize.widthTwips,
                config.pageSize.heightTwips
            )
            writeDocxFile(output, documentXml)
        } else {
            // Multi-page document
            val pages = blocksByPage.entries.sortedBy { it.key }.mapIndexed { index, (_, blocks) ->
                // Calculate content dimensions from blocks on this page
                var maxRight = 0
                var maxBottom = 0

                blocks.forEach { block ->
                    block.boundingBox?.let { box ->
                        if (box.right > maxRight) maxRight = box.right
                        if (box.bottom > maxBottom) maxBottom = box.bottom
                    }
                    block.cornerPoints?.forEach { point ->
                        if (point[0] > maxRight) maxRight = point[0]
                        if (point[1] > maxBottom) maxBottom = point[1]
                    }
                }

                val pageContentWidth = if (maxRight > 0) maxRight else contentWidth
                val pageContentHeight = if (maxBottom > 0) maxBottom else contentHeight

                val layout = calculateContentLayout(pageContentWidth, pageContentHeight)
                val emuScale = UnitConverter.EMU_PER_PIXEL.toDouble() * layout.scaleFactor

                val pageContent = DocxStructuredText(blocks, blocks.joinToString("\n") { it.text })
                val textBoxesXml = buildTextBoxesXml(pageContent, emuScale, index * 1000, layout.offsetXEmu, layout.offsetYEmu)

                PageData(config.pageSize.widthTwips, config.pageSize.heightTwips, textBoxesXml)
            }

            val documentXml = createCombinedDocumentXml(pages)
            writeDocxFile(output, documentXml)
        }
    }

    /**
     * Writes a multi-page DOCX document with each page having its own content and dimensions.
     *
     * @param pages List of pages to include in the document
     * @param output The output stream to write to
     */
    fun writeMultiPage(pages: List<DocxPage>, output: OutputStream) {
        val pageDataList = pages.mapIndexed { index, page ->
            val layout = calculateContentLayout(page.contentWidth, page.contentHeight)
            val emuScale = UnitConverter.EMU_PER_PIXEL.toDouble() * layout.scaleFactor
            val textBoxesXml = buildTextBoxesXml(page.content, emuScale, index * 1000, layout.offsetXEmu, layout.offsetYEmu)
            PageData(config.pageSize.widthTwips, config.pageSize.heightTwips, textBoxesXml)
        }

        val documentXml = createCombinedDocumentXml(pageDataList)
        writeDocxFile(output, documentXml)
    }

    // ==================== Content Layout ====================

    /**
     * Data class to hold content scaling and positioning info.
     */
    private data class ContentLayout(
        val scaleFactor: Double,
        val offsetXEmu: Long,
        val offsetYEmu: Long
    )

    /**
     * Calculates how to scale and position content to fit within A4 page.
     */
    private fun calculateContentLayout(imageWidth: Int, imageHeight: Int): ContentLayout {
        val pageWidthTwips = PageSize.A4.widthTwips
        val pageHeightTwips = PageSize.A4.heightTwips
        val marginTwips = PAGE_MARGIN_TWIPS

        val contentWidthTwips = pageWidthTwips - (2 * marginTwips)
        val contentHeightTwips = pageHeightTwips - (2 * marginTwips)

        val imageAspect = imageWidth.toDouble() / imageHeight.toDouble()
        val contentWidthPx = contentWidthTwips.toDouble() / UnitConverter.TWIPS_PER_PIXEL
        val contentHeightPx = contentHeightTwips.toDouble() / UnitConverter.TWIPS_PER_PIXEL
        val contentAspect = contentWidthPx / contentHeightPx

        val scaleFactor = if (imageAspect > contentAspect) {
            contentWidthPx / imageWidth
        } else {
            contentHeightPx / imageHeight
        }

        val scaledWidthPx = imageWidth * scaleFactor
        val scaledHeightPx = imageHeight * scaleFactor

        val marginEmu = marginTwips.toLong() * UnitConverter.EMU_PER_INCH / UnitConverter.TWIPS_PER_INCH
        val offsetXPx = (contentWidthPx - scaledWidthPx) / 2.0
        val offsetYPx = (contentHeightPx - scaledHeightPx) / 2.0

        val offsetXEmu = marginEmu + (offsetXPx * UnitConverter.EMU_PER_PIXEL).toLong()
        val offsetYEmu = marginEmu + (offsetYPx * UnitConverter.EMU_PER_PIXEL).toLong()

        return ContentLayout(scaleFactor, offsetXEmu, offsetYEmu)
    }

    // ==================== Text Box XML Building ====================

    private fun buildTextBoxesXml(
        structured: DocxStructuredText,
        scale: Double,
        blockIdOffset: Int = 0,
        offsetXEmu: Long = 0,
        offsetYEmu: Long = 0
    ): String {
        val paragraphsXml = StringBuilder()

        structured.blocks.forEachIndexed { index, block ->
            val frame = block.boundingBox ?: return@forEachIndexed
            val corners = block.cornerPoints

            // Calculate rotation angle and actual dimensions from corner points
            val (actualWidth, actualHeight, rotationDegrees) = if (corners != null && corners.size >= 4) {
                val topLeft = corners[0]
                val topRight = corners[1]
                val bottomLeft = corners[3]

                val widthPixels = sqrt(
                    (topRight[0] - topLeft[0]).toDouble().pow(2.0) +
                            (topRight[1] - topLeft[1]).toDouble().pow(2.0)
                )
                val heightPixels = sqrt(
                    (bottomLeft[0] - topLeft[0]).toDouble().pow(2.0) +
                            (bottomLeft[1] - topLeft[1]).toDouble().pow(2.0)
                )
                val deltaX = topRight[0] - topLeft[0]
                val deltaY = topRight[1] - topLeft[1]
                val angle = Math.toDegrees(atan2(deltaY.toDouble(), deltaX.toDouble()))

                Triple(widthPixels, heightPixels, angle)
            } else {
                Triple(frame.width().toDouble(), frame.height().toDouble(), 0.0)
            }

            val widthEmu = (actualWidth * scale).toLong()
            val heightEmu = (actualHeight * scale).toLong()

            val (baseXEmu, baseYEmu) = if (corners != null && corners.size >= 4) {
                val centerX = corners.map { it[0] }.average()
                val centerY = corners.map { it[1] }.average()
                val centerXEmu = (centerX * scale).toLong()
                val centerYEmu = (centerY * scale).toLong()
                Pair(centerXEmu - widthEmu / 2, centerYEmu - heightEmu / 2)
            } else {
                Pair((frame.left * scale).toLong(), (frame.top * scale).toLong())
            }

            val xEmu = baseXEmu + offsetXEmu
            val yEmu = baseYEmu + offsetYEmu
            val rotationEmu = (rotationDegrees * 60000).toLong()
            val isRotated = abs(rotationDegrees) > 1.0

            if (isRotated) {
                val text = block.text
                val fontSize = block.fontSize?.let { it * 2 } ?: calculateFontSize(text, widthEmu, heightEmu)
                val escapedText = escapeXml(text)
                paragraphsXml.append(wrapInParagraph(createTextBoxXml(escapedText, xEmu, yEmu, widthEmu, heightEmu, fontSize, block.isBold, block.isItalic, rotationEmu, blockIdOffset + index + 1)))
            } else {
                var lineCounter = 0
                block.lines.forEach { line ->
                    val lineFrame = line.boundingBox
                    if (lineFrame != null) {
                        val lineXEmu = (lineFrame.left * scale).toLong() + offsetXEmu
                        val lineYEmu = (lineFrame.top * scale).toLong() + offsetYEmu
                        val lineWidthEmu = (lineFrame.width() * scale).toLong()
                        val lineHeightEmu = (lineFrame.height() * scale).toLong()

                        val fontSize = block.fontSize?.let { it * 2 } ?: calculateFontSize(line.text, lineWidthEmu, lineHeightEmu)
                        val escapedText = escapeXml(line.text)
                        paragraphsXml.append(wrapInParagraph(createTextBoxXml(escapedText, lineXEmu, lineYEmu, lineWidthEmu, lineHeightEmu, fontSize, block.isBold, block.isItalic, 0L, blockIdOffset + index * 1000 + lineCounter + 1)))
                        lineCounter++
                    }
                }

                if (lineCounter == 0) {
                    val text = block.text
                    val fontSize = block.fontSize?.let { it * 2 } ?: calculateFontSize(text, widthEmu, heightEmu)
                    val escapedText = escapeXml(text)
                    paragraphsXml.append(wrapInParagraph(createTextBoxXml(escapedText, xEmu, yEmu, widthEmu, heightEmu, fontSize, block.isBold, block.isItalic, 0L, blockIdOffset + index + 1)))
                }
            }
        }

        return paragraphsXml.toString()
    }

    private fun wrapInParagraph(content: String): String {
        return """<w:p w:rsidR="00000000" w:rsidDel="00000000" w:rsidP="00000000" w:rsidRDefault="00000000" w:rsidRPr="00000000"><w:pPr><w:rPr/></w:pPr>$content</w:p>"""
    }

    private fun buildSimpleParagraphsXml(structured: DocxStructuredText): String {
        val paragraphsXml = StringBuilder()

        if (structured.blocks.isNotEmpty()) {
            structured.blocks.forEach { block ->
                paragraphsXml.append("""<w:p><w:pPr><w:rPr/></w:pPr>""")
                block.lines.forEachIndexed { index, line ->
                    val escapedText = escapeXml(line.text)
                    paragraphsXml.append("""<w:r><w:rPr/><w:t xml:space="preserve">$escapedText</w:t></w:r>""")
                    if (index < block.lines.size - 1) {
                        paragraphsXml.append("""<w:r><w:br/></w:r>""")
                    }
                }
                paragraphsXml.append("""</w:p>""")
            }
        } else {
            val lines = structured.fullText.split("\n")
            lines.forEach { line ->
                val escapedText = escapeXml(line)
                paragraphsXml.append("""<w:p><w:pPr><w:rPr/></w:pPr><w:r><w:rPr/><w:t xml:space="preserve">$escapedText</w:t></w:r></w:p>""")
            }
        }

        return paragraphsXml.toString()
    }

    private fun calculateFontSize(text: String, widthEmu: Long, heightEmu: Long): Int {
        val lines = text.split("\n")
        val longestLine = lines.maxByOrNull { it.length } ?: ""
        val lineCount = lines.size

        val widthPoints = widthEmu * 72.0 / UnitConverter.EMU_PER_INCH
        val heightPoints = heightEmu * 72.0 / UnitConverter.EMU_PER_INCH

        val fontSizeByWidth = if (longestLine.isNotEmpty()) {
            (widthPoints / (longestLine.length * 0.65)).toInt()
        } else {
            12
        }

        val fontSizeByHeight = if (lineCount > 0) {
            (heightPoints / (lineCount * 1.0)).toInt()
        } else {
            12
        }

        val fontSizePt = maxOf(6, minOf(fontSizeByWidth, fontSizeByHeight, 96))
        return fontSizePt * 2 // Return in half-points
    }

    // ==================== XML Generation ====================

    private fun createTextBoxXml(
        text: String,
        xEmu: Long,
        yEmu: Long,
        widthEmu: Long,
        heightEmu: Long,
        fontSize: Int,
        isBold: Boolean,
        isItalic: Boolean,
        rotation: Long,
        blockId: Int
    ): String {
        val rotAttr = if (rotation != 0L) """ rot="$rotation"""" else ""
        // Each block gets a unique relativeHeight to ensure proper z-ordering
        val zOrder = blockId * 100
        
        // Handle styling
        val boldXml = if (isBold) "<w:b/>" else ""
        val italicXml = if (isItalic) "<w:i/>" else ""
        // Only use w:sz (Latin/ASCII size). Removing w:szCs to avoid potential viewer confusion if not needed.
        val rPrContent = """<w:sz w:val="$fontSize"/>$boldXml$italicXml"""
        
        // Handle newlines by converting them to <w:br/>
        val textLines = text.split("\n")
        val contentXml = StringBuilder()
        textLines.forEachIndexed { index, line ->
            contentXml.append("""<w:t xml:space="preserve">$line</w:t>""")
            if (index < textLines.size - 1) {
                contentXml.append("<w:br/>")
            }
        }

        return """<w:r w:rsidDel="00000000" w:rsidR="00000000" w:rsidRPr="00000000"><w:rPr/><mc:AlternateContent><mc:Choice Requires="wps"><w:drawing><wp:anchor allowOverlap="1" behindDoc="0" distB="0" distT="0" distL="0" distR="0" hidden="0" layoutInCell="1" locked="0" relativeHeight="$zOrder" simplePos="0"><wp:simplePos x="0" y="0"/><wp:positionH relativeFrom="margin"><wp:posOffset>$xEmu</wp:posOffset></wp:positionH><wp:positionV relativeFrom="margin"><wp:posOffset>$yEmu</wp:posOffset></wp:positionV><wp:extent cx="$widthEmu" cy="$heightEmu"/><wp:effectExtent b="0" l="0" r="0" t="0"/><wp:wrapNone/><wp:docPr id="$blockId" name="TextBox$blockId"/><a:graphic><a:graphicData uri="http://schemas.microsoft.com/office/word/2010/wordprocessingShape"><wps:wsp><wps:cNvSpPr txBox="1"/><wps:spPr><a:xfrm$rotAttr><a:off x="0" y="0"/><a:ext cx="$widthEmu" cy="$heightEmu"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom><a:noFill/><a:ln><a:noFill/></a:ln></wps:spPr><wps:txbx><w:txbxContent><w:p w:rsidR="00000000" w:rsidDel="00000000" w:rsidP="00000000" w:rsidRDefault="00000000" w:rsidRPr="00000000"><w:pPr><w:spacing w:after="0" w:line="240" w:lineRule="auto"/></w:pPr><w:r w:rsidDel="00000000" w:rsidR="00000000" w:rsidRPr="00000000"><w:rPr>$rPrContent</w:rPr>$contentXml</w:r></w:p></w:txbxContent></wps:txbx><wps:bodyPr anchorCtr="0" anchor="t" bIns="0" lIns="0" rIns="0" rot="0" vert="horz" wrap="square" tIns="0"><a:spAutoFit/></wps:bodyPr></wps:wsp></a:graphicData></a:graphic></wp:anchor></w:drawing></mc:Choice><mc:Fallback><w:drawing><wp:anchor allowOverlap="1" behindDoc="0" distB="0" distT="0" distL="0" distR="0" hidden="0" layoutInCell="1" locked="0" relativeHeight="$zOrder" simplePos="0"><wp:simplePos x="0" y="0"/><wp:positionH relativeFrom="margin"><wp:posOffset>$xEmu</wp:posOffset></wp:positionH><wp:positionV relativeFrom="margin"><wp:posOffset>$yEmu</wp:posOffset></wp:positionV><wp:extent cx="$widthEmu" cy="$heightEmu"/><wp:effectExtent b="0" l="0" r="0" t="0"/><wp:wrapNone/><wp:docPr id="$blockId" name="TextBox$blockId"/><a:graphic><a:graphicData uri="http://schemas.microsoft.com/office/word/2010/wordprocessingShape"><wps:wsp><wps:cNvSpPr txBox="1"/><wps:spPr><a:xfrm$rotAttr><a:off x="0" y="0"/><a:ext cx="$widthEmu" cy="$heightEmu"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom><a:noFill/><a:ln><a:noFill/></a:ln></wps:spPr><wps:txbx><w:txbxContent><w:p w:rsidR="00000000" w:rsidDel="00000000" w:rsidP="00000000" w:rsidRDefault="00000000" w:rsidRPr="00000000"><w:pPr><w:spacing w:after="0" w:line="240" w:lineRule="auto"/></w:pPr><w:r w:rsidDel="00000000" w:rsidR="00000000" w:rsidRPr="00000000"><w:rPr>$rPrContent</w:rPr>$contentXml</w:r></w:p></w:txbxContent></wps:txbx><wps:bodyPr anchorCtr="0" anchor="t" bIns="0" lIns="0" rIns="0" rot="0" vert="horz" wrap="square" tIns="0"><a:spAutoFit/></wps:bodyPr></wps:wsp></a:graphicData></a:graphic></wp:anchor></w:drawing></mc:Fallback></mc:AlternateContent></w:r>"""
    }

    private fun createDocumentXml(contentXml: String, pageWidthTwips: Int, pageHeightTwips: Int): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
            xmlns:o="urn:schemas-microsoft-com:office:office"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
            xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
            xmlns:v="urn:schemas-microsoft-com:vml"
            xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
            xmlns:w10="urn:schemas-microsoft-com:office:word"
            xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml"
            xmlns:sl="http://schemas.openxmlformats.org/schemaLibrary/2006/main"
            xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
            xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
            xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart"
            xmlns:lc="http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas"
            xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram"
            xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape"
            xmlns:wpg="http://schemas.microsoft.com/office/word/2010/wordprocessingGroup"
            xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml"
            xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml">
    <w:body>
        $contentXml
        <w:sectPr>
            <w:pgSz w:h="$pageHeightTwips" w:w="$pageWidthTwips" w:orient="portrait"/>
            <w:pgMar w:bottom="0" w:top="0" w:left="0" w:right="0" w:header="360" w:footer="360"/>
            <w:pgNumType w:start="1"/>
        </w:sectPr>
    </w:body>
</w:document>"""
    }

    private fun createSimpleDocumentXml(contentXml: String, pageWidthTwips: Int, pageHeightTwips: Int): String {
        val margins = config.margins
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
            xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml">
    <w:body>
        $contentXml
        <w:sectPr>
            <w:pgSz w:h="$pageHeightTwips" w:w="$pageWidthTwips"/>
            <w:pgMar w:bottom="${margins.bottom}" w:top="${margins.top}" w:left="${margins.left}" w:right="${margins.right}"/>
        </w:sectPr>
    </w:body>
</w:document>"""
    }

    /**
     * Data class to hold page information for combined documents.
     */
    private data class PageData(
        val widthTwips: Int,
        val heightTwips: Int,
        val textBoxesXml: String
    )

    private fun createCombinedDocumentXml(pages: List<PageData>): String {
        val bodyContent = StringBuilder()

        pages.forEachIndexed { index, page ->
            bodyContent.append("""
            ${page.textBoxesXml}
            """)

            if (index < pages.size - 1) {
                bodyContent.append("""
        <w:p><w:pPr><w:sectPr>
            <w:pgSz w:h="${page.heightTwips}" w:w="${page.widthTwips}" w:orient="portrait"/>
            <w:pgMar w:bottom="0" w:top="0" w:left="0" w:right="0" w:header="360" w:footer="360"/>
        </w:sectPr></w:pPr></w:p>""")
            }
        }

        val lastPage = pages.lastOrNull() ?: PageData(12240, 15840, "")

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml" xmlns:sl="http://schemas.openxmlformats.org/schemaLibrary/2006/main" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" xmlns:lc="http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas" xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram" xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape" xmlns:wpg="http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml">
    <w:body>
        $bodyContent
        <w:sectPr>
            <w:pgSz w:h="${lastPage.heightTwips}" w:w="${lastPage.widthTwips}" w:orient="portrait"/>
            <w:pgMar w:bottom="0" w:top="0" w:left="0" w:right="0" w:header="360" w:footer="360"/>
            <w:pgNumType w:start="1"/>
        </w:sectPr>
    </w:body>
</w:document>"""
    }

    // ==================== File Writing ====================

    private fun writeDocxFile(output: OutputStream, documentXml: String) {
        val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default ContentType="application/xml" Extension="xml"/><Default ContentType="application/vnd.openxmlformats-package.relationships+xml" Extension="rels"/><Override ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml" PartName="/word/settings.xml"/><Override ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml" PartName="/word/styles.xml"/><Override ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml" PartName="/word/fontTable.xml"/><Override ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml" PartName="/word/document.xml"/></Types>"""

        val relsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>"""

        val documentRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings" Target="settings.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable" Target="fontTable.xml"/><Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>"""

        val stylesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii="${config.defaultFontFamily}" w:hAnsi="${config.defaultFontFamily}" w:eastAsia="${config.defaultFontFamily}" w:cs="${config.defaultFontFamily}"/><w:sz w:val="${config.defaultFontSizePt * 2}"/><w:szCs w:val="${config.defaultFontSizePt * 2}"/></w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after="0" w:line="240" w:lineRule="auto"/></w:pPr></w:pPrDefault></w:docDefaults></w:styles>"""

        val settingsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><w:settings xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml" xmlns:sl="http://schemas.openxmlformats.org/schemaLibrary/2006/main" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" xmlns:lc="http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas" xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram" xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape" xmlns:wpg="http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml"><w:defaultTabStop w:val="720"/><w:compat><w:compatSetting w:val="15" w:name="compatibilityMode" w:uri="http://schemas.microsoft.com/office/word"/></w:compat></w:settings>"""

        val fontTableXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><w:fonts xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:font w:name="Arial"><w:charset w:val="00"/><w:family w:val="swiss"/><w:pitch w:val="variable"/></w:font><w:font w:name="Times New Roman"><w:charset w:val="00"/><w:family w:val="roman"/><w:pitch w:val="variable"/></w:font></w:fonts>"""

        ZipOutputStream(output).use { zipOut ->
            zipOut.putNextEntry(ZipEntry("[Content_Types].xml"))
            zipOut.write(contentTypesXml.toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(ZipEntry("_rels/.rels"))
            zipOut.write(relsXml.toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(ZipEntry("word/document.xml"))
            zipOut.write(documentXml.toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(ZipEntry("word/_rels/document.xml.rels"))
            zipOut.write(documentRelsXml.toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(ZipEntry("word/styles.xml"))
            zipOut.write(stylesXml.toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(ZipEntry("word/settings.xml"))
            zipOut.write(settingsXml.toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(ZipEntry("word/fontTable.xml"))
            zipOut.write(fontTableXml.toByteArray())
            zipOut.closeEntry()
        }
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    companion object {
        private const val PAGE_MARGIN_TWIPS = 720 // 0.5 inch
    }
}

/**
 * Represents a single page in a multi-page document.
 */
data class DocxPage(
    val content: DocxStructuredText,
    val contentWidth: Int,
    val contentHeight: Int
)
