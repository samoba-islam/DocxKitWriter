package com.samoba.docxkit

/**
 * Utility object for unit conversions used in DOCX generation.
 *
 * DOCX uses multiple unit systems:
 * - EMU (English Metric Units): Used for positioning in drawing elements
 * - Twips: Used for page dimensions and margins (1/20 of a point)
 * - Points: Used for font sizes
 */
object UnitConverter {
    
    /** EMUs per inch */
    const val EMU_PER_INCH = 914400L
    
    /** Twips per inch */
    const val TWIPS_PER_INCH = 1440
    
    /** Default screen DPI for pixel conversions */
    const val DEFAULT_DPI = 96
    
    /** EMUs per pixel at default DPI */
    const val EMU_PER_PIXEL = EMU_PER_INCH / DEFAULT_DPI  // 9525
    
    /** Twips per pixel at default DPI */
    const val TWIPS_PER_PIXEL = TWIPS_PER_INCH / DEFAULT_DPI  // 15
    
    /** Points per inch */
    const val POINTS_PER_INCH = 72
    
    // ==================== Pixel Conversions ====================
    
    /**
     * Converts pixels to EMUs at default DPI.
     */
    fun pixelsToEmu(pixels: Int): Long = pixels.toLong() * EMU_PER_PIXEL
    
    /**
     * Converts pixels to EMUs at custom DPI.
     */
    fun pixelsToEmu(pixels: Int, dpi: Int): Long = (pixels.toLong() * EMU_PER_INCH) / dpi
    
    /**
     * Converts pixels to twips at default DPI.
     */
    fun pixelsToTwips(pixels: Int): Int = pixels * TWIPS_PER_PIXEL
    
    /**
     * Converts pixels to twips at custom DPI.
     */
    fun pixelsToTwips(pixels: Int, dpi: Int): Int = (pixels * TWIPS_PER_INCH) / dpi
    
    // ==================== EMU Conversions ====================
    
    /**
     * Converts EMUs to pixels at default DPI.
     */
    fun emuToPixels(emu: Long): Int = (emu / EMU_PER_PIXEL).toInt()
    
    /**
     * Converts EMUs to pixels at custom DPI.
     */
    fun emuToPixels(emu: Long, dpi: Int): Int = ((emu * dpi) / EMU_PER_INCH).toInt()
    
    /**
     * Converts EMUs to twips.
     */
    fun emuToTwips(emu: Long): Int = ((emu * TWIPS_PER_INCH) / EMU_PER_INCH).toInt()
    
    /**
     * Converts EMUs to points.
     */
    fun emuToPoints(emu: Long): Double = (emu * POINTS_PER_INCH).toDouble() / EMU_PER_INCH
    
    // ==================== Twips Conversions ====================
    
    /**
     * Converts twips to EMUs.
     */
    fun twipsToEmu(twips: Int): Long = (twips.toLong() * EMU_PER_INCH) / TWIPS_PER_INCH
    
    /**
     * Converts twips to pixels at default DPI.
     */
    fun twipsToPixels(twips: Int): Int = twips / TWIPS_PER_PIXEL
    
    /**
     * Converts twips to points.
     */
    fun twipsToPoints(twips: Int): Double = twips.toDouble() / 20.0
    
    // ==================== Point Conversions ====================
    
    /**
     * Converts points to half-points (DOCX internal format for font sizes).
     */
    fun pointsToHalfPoints(points: Int): Int = points * 2
    
    /**
     * Converts half-points to points.
     */
    fun halfPointsToPoints(halfPoints: Int): Int = halfPoints / 2
    
    /**
     * Converts points to twips.
     */
    fun pointsToTwips(points: Int): Int = points * 20
    
    /**
     * Converts points to EMUs.
     */
    fun pointsToEmu(points: Int): Long = (points.toLong() * EMU_PER_INCH) / POINTS_PER_INCH
    
    // ==================== Inch Conversions ====================
    
    /**
     * Converts inches to EMUs.
     */
    fun inchesToEmu(inches: Float): Long = (inches * EMU_PER_INCH).toLong()
    
    /**
     * Converts inches to twips.
     */
    fun inchesToTwips(inches: Float): Int = (inches * TWIPS_PER_INCH).toInt()
    
    // ==================== Millimeter Conversions ====================
    
    /**
     * Converts millimeters to EMUs.
     */
    fun mmToEmu(mm: Float): Long = (mm * EMU_PER_INCH / 25.4f).toLong()
    
    /**
     * Converts millimeters to twips.
     */
    fun mmToTwips(mm: Float): Int = (mm * TWIPS_PER_INCH / 25.4f).toInt()
}
