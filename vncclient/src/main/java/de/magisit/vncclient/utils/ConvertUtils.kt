package de.magisit.vncclient.utils

/**
 * Convert Utils
 */
object ConvertUtils {
    /**
     * Converts an sint32 to uInt16
     */
    fun toUInt16(sint32: Int): ByteArray {
        return byteArrayOf((sint32 shr 8 and 0xFF).toByte(), (sint32 and 0xFF).toByte())
    }

    /**
     * Converts an sint32 to a sint32 array
     */
    fun toSInt32Array(sint32: Int): ByteArray {
        return byteArrayOf((sint32 shr 24).toByte(), (sint32 shr 16).toByte(), (sint32 shr 8).toByte(), sint32.toByte())
    }
}