package com.samoba.docxkit.model

/**
 * Platform-agnostic bounding box for text positioning.
 * Replaces Android's Rect for library portability.
 *
 * @param left Left edge coordinate in pixels
 * @param top Top edge coordinate in pixels
 * @param right Right edge coordinate in pixels
 * @param bottom Bottom edge coordinate in pixels
 */
data class DocxBoundingBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    /**
     * Returns the width of the bounding box.
     */
    fun width(): Int = right - left

    /**
     * Returns the height of the bounding box.
     */
    fun height(): Int = bottom - top

    /**
     * Returns the center X coordinate.
     */
    fun centerX(): Int = (left + right) / 2

    /**
     * Returns the center Y coordinate.
     */
    fun centerY(): Int = (top + bottom) / 2

    companion object {
        /**
         * Creates a bounding box from left, top, width, and height.
         */
        fun fromDimensions(left: Int, top: Int, width: Int, height: Int): DocxBoundingBox {
            return DocxBoundingBox(left, top, left + width, top + height)
        }
    }
}
