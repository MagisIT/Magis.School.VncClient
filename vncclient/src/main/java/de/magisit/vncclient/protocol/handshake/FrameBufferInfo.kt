package de.magisit.vncclient.protocol.handshake

class FrameBufferInfo(
    val frameBufferWidth: Int,
    val frameBufferHeight: Int,
    val pixelFormat: PixelFormat,
    val desktopName: String
) {

    override fun toString(): String {
        return "FrameBufferInfo{" +
                "frameBufferWidth = $frameBufferWidth, " +
                "frameBufferHeight = $frameBufferHeight, " +
                "pixelFormat = $pixelFormat, " +
                "desktopName = $desktopName}"
    }
}