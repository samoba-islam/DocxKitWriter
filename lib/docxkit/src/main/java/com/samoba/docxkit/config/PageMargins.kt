package com.samoba.docxkit.config

/**
 * Page margins configuration for DOCX documents.
 * All values are in twips (1 inch = 1440 twips).
 *
 * @param top Top margin in twips
 * @param bottom Bottom margin in twips
 * @param left Left margin in twips
 * @param right Right margin in twips
 * @param header Header margin in twips
 * @param footer Footer margin in twips
 */
data class PageMargins(
    val top: Int = 1440,      // 1 inch default
    val bottom: Int = 1440,   // 1 inch default
    val left: Int = 1440,     // 1 inch default
    val right: Int = 1440,    // 1 inch default
    val header: Int = 720,    // 0.5 inch default
    val footer: Int = 720     // 0.5 inch default
) {
    companion object {
        /** Default margins (1 inch on all sides) */
        val DEFAULT = PageMargins()
        
        /** Narrow margins (0.5 inch on all sides) */
        val NARROW = PageMargins(720, 720, 720, 720, 360, 360)
        
        /** No margins (useful for positioned content) */
        val NONE = PageMargins(0, 0, 0, 0, 0, 0)
        
        /** Moderate margins (0.75 inch on all sides) */
        val MODERATE = PageMargins(1080, 1080, 1080, 1080, 540, 540)
        
        /**
         * Creates margins from inch values.
         */
        fun fromInches(
            top: Float = 1f,
            bottom: Float = 1f,
            left: Float = 1f,
            right: Float = 1f,
            header: Float = 0.5f,
            footer: Float = 0.5f
        ): PageMargins {
            return PageMargins(
                top = (top * PageSize.TWIPS_PER_INCH).toInt(),
                bottom = (bottom * PageSize.TWIPS_PER_INCH).toInt(),
                left = (left * PageSize.TWIPS_PER_INCH).toInt(),
                right = (right * PageSize.TWIPS_PER_INCH).toInt(),
                header = (header * PageSize.TWIPS_PER_INCH).toInt(),
                footer = (footer * PageSize.TWIPS_PER_INCH).toInt()
            )
        }
    }
}
