package de.magisit.vncclient.protocol.handshake

import de.magisit.vncclient.utils.ExtendedDataInputStream

/**
 * PixelFormat class to represent the pixel format received in the handshake
 */
class PixelFormat(
        val bitsPerPixel: Int,
        val depth: Int,
        val bigEndianFlag: Int,
        val trueColorFlag: Int,
        val redMax: Int,
        val greenMax: Int,
        val blueMax: Int,
        val redShift: Int,
        val greenShift: Int,
        val blueShift: Int
) {

    /**
     * Converts the values to a byte array
     */
    fun toByteArray(): ByteArray {
        return byteArrayOf(
                bitsPerPixel.toByte(),
                depth.toByte(),
                bigEndianFlag.toByte(),
                trueColorFlag.toByte(),
                (redMax shr 8 and 0xFF).toByte(),
                (redMax and 0xFF).toByte(),
                (greenMax shr 8 and 0xFF).toByte(),
                (greenMax and 0xFF).toByte(),
                (blueMax shr 8 and 0xFF).toByte(),
                (blueMax and 0xFF).toByte(),
                redShift.toByte(),
                greenShift.toByte(),
                blueShift.toByte()
        )
    }

    /**
     * Creates a new pixel format from an input stream
     */
    companion object {
        fun fromBuffer(extendedDataInputStream: ExtendedDataInputStream): PixelFormat {

            // Read the 16 bytes needed
            val pixelFormatArray = ByteArray(16)
            extendedDataInputStream.readFully(pixelFormatArray)

            // Return a new PixelFormat object
            return PixelFormat(
                    bitsPerPixel = 0xFF and pixelFormatArray[0].toInt(),
                    depth = 0xFF and pixelFormatArray[1].toInt(),
                    bigEndianFlag = 0xFF and pixelFormatArray[2].toInt(),
                    trueColorFlag = 0xFF and pixelFormatArray[3].toInt(),
                    redMax = 0xFF and (pixelFormatArray[4].toInt() shl 8) or (0xFF and pixelFormatArray[5].toInt()),
                    greenMax = 0xFF and (pixelFormatArray[6].toInt() shl 8) or (0xFF and pixelFormatArray[7].toInt()),
                    blueMax = 0xFF and (pixelFormatArray[8].toInt() shl 8) or (0xFF and pixelFormatArray[9].toInt()),
                    redShift = 0xFF and pixelFormatArray[10].toInt(),
                    greenShift = 0xFF and pixelFormatArray[11].toInt(),
                    blueShift = 0xFF and pixelFormatArray[12].toInt()
            )
        }
    }

    /**
     * toString() method for logging
     */
    override fun toString(): String {
        return "PixelFormat{" +
                "bitsPerPixel = $bitsPerPixel, " +
                "depth = $depth, " +
                "bigEndianFlag = $bigEndianFlag, " +
                "trueColorFlag = $trueColorFlag, " +
                "redMax = $redMax, " +
                "greenMax = $greenMax, " +
                "blueMax = $blueMax, " +
                "redShift = $redShift, " +
                "greenShift = $greenShift, " +
                "blueShift = $blueShift}"
    }
}