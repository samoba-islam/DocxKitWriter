package com.samoba.docxkit.config

/**
 * Configuration for DOCX document generation.
 *
 * @param pageSize The page size to use
 * @param margins Page margins
 * @param defaultFontFamily Default font family for text
 * @param defaultFontSizePt Default font size in points
 * @param orientation Page orientation (portrait or landscape)
 */
data class DocxConfig(
    val pageSize: PageSize = PageSize.A4,
    val margins: PageMargins = PageMargins.NONE,
    val defaultFontFamily: String = "Arial",
    val defaultFontSizePt: Int = 11,
    val orientation: PageOrientation = PageOrientation.PORTRAIT
) {
    /**
     * Creates a copy with the page size set to landscape orientation.
     */
    fun withLandscape(): DocxConfig = copy(
        pageSize = PageSize.Custom(pageSize.heightTwips, pageSize.widthTwips),
        orientation = PageOrientation.LANDSCAPE
    )
    
    /**
     * Builder for DocxConfig with fluent API.
     */
    class Builder {
        private var pageSize: PageSize = PageSize.A4
        private var margins: PageMargins = PageMargins.NONE
        private var defaultFontFamily: String = "Arial"
        private var defaultFontSizePt: Int = 11
        private var orientation: PageOrientation = PageOrientation.PORTRAIT
        
        fun pageSize(size: PageSize) = apply { this.pageSize = size }
        fun margins(margins: PageMargins) = apply { this.margins = margins }
        fun fontFamily(family: String) = apply { this.defaultFontFamily = family }
        fun fontSize(sizePt: Int) = apply { this.defaultFontSizePt = sizePt }
        fun orientation(orientation: PageOrientation) = apply { this.orientation = orientation }
        
        fun build(): DocxConfig = DocxConfig(
            pageSize = pageSize,
            margins = margins,
            defaultFontFamily = defaultFontFamily,
            defaultFontSizePt = defaultFontSizePt,
            orientation = orientation
        )
    }
    
    companion object {
        /** Default configuration for positioned text (A4, no margins) */
        val POSITIONED = DocxConfig(
            pageSize = PageSize.A4,
            margins = PageMargins.NONE
        )
        
        /** Default configuration for simple text (A4, standard margins) */
        val SIMPLE = DocxConfig(
            pageSize = PageSize.A4,
            margins = PageMargins.DEFAULT
        )
        
        /**
         * Creates a new builder instance.
         */
        fun builder(): Builder = Builder()
    }
}

/**
 * Page orientation for DOCX documents.
 */
enum class PageOrientation {
    PORTRAIT,
    LANDSCAPE
}
