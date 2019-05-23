package de.magisit.vncclient.protocol.handshake

/**
 * Class to represent the frame buffer info received in the handshake
 */
class FrameBufferInfo(
        val frameBufferWidth: Int,
        val frameBufferHeight: Int,
        val pixelFormat: PixelFormat,
        val desktopName: String
) {

    /**
     * toString() method for logging
     */
    override fun toString(): String {
        return "FrameBufferInfo{" +
                "frameBufferWidth = $frameBufferWidth, " +
                "frameBufferHeight = $frameBufferHeight, " +
                "pixelFormat = $pixelFormat, " +
                "desktopName = $desktopName}"
    }
}