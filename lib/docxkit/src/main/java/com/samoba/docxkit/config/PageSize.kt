package com.samoba.docxkit.config

/**
 * Predefined page sizes for DOCX documents.
 * Dimensions are in Twips (1 inch = 1440 twips).
 *
 * @param widthTwips Page width in twips
 * @param heightTwips Page height in twips
 */
sealed class PageSize(val widthTwips: Int, val heightTwips: Int) {
    
    /** A4 size: 210mm x 297mm (8.27 x 11.69 inches) */
    data object A4 : PageSize(11906, 16838)
    
    /** A3 size: 297mm x 420mm (11.69 x 16.54 inches) */
    data object A3 : PageSize(16838, 23811)
    
    /** A5 size: 148mm x 210mm (5.83 x 8.27 inches) */
    data object A5 : PageSize(8391, 11906)
    
    /** US Letter size: 8.5 x 11 inches */
    data object Letter : PageSize(12240, 15840)
    
    /** US Legal size: 8.5 x 14 inches */
    data object Legal : PageSize(12240, 20160)
    
    /** Tabloid size: 11 x 17 inches */
    data object Tabloid : PageSize(15840, 24480)
    
    /** B4 size: 250mm x 353mm */
    data object B4 : PageSize(14173, 20012)
    
    /** B5 size: 176mm x 250mm */
    data object B5 : PageSize(9979, 14173)
    
    /**
     * Custom page size with user-defined dimensions.
     *
     * @param width Width in twips
     * @param height Height in twips
     */
    data class Custom(val width: Int, val height: Int) : PageSize(width, height)
    
    companion object {
        /** Twips per inch */
        const val TWIPS_PER_INCH = 1440
        
        /** Twips per millimeter (approximately) */
        const val TWIPS_PER_MM = 56.7f
        
        /**
         * Creates a custom page size from inches.
         */
        fun fromInches(widthInches: Float, heightInches: Float): Custom {
            return Custom(
                (widthInches * TWIPS_PER_INCH).toInt(),
                (heightInches * TWIPS_PER_INCH).toInt()
            )
        }
        
        /**
         * Creates a custom page size from millimeters.
         */
        fun fromMillimeters(widthMm: Float, heightMm: Float): Custom {
            return Custom(
                (widthMm * TWIPS_PER_MM).toInt(),
                (heightMm * TWIPS_PER_MM).toInt()
            )
        }
        
        /**
         * Creates a custom page size from pixels at a given DPI.
         *
         * @param widthPx Width in pixels
         * @param heightPx Height in pixels
         * @param dpi Dots per inch (default 96)
         */
        fun fromPixels(widthPx: Int, heightPx: Int, dpi: Int = 96): Custom {
            val widthInches = widthPx.toFloat() / dpi
            val heightInches = heightPx.toFloat() / dpi
            return fromInches(widthInches, heightInches)
        }
    }
}
