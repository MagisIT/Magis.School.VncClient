package de.magisit.vncclient.protocol.encodings.frame

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.encodings.FrameEncoding
import de.magisit.vncclient.utils.ExtendedDataInputStream

class CopyRectEncoding : FrameEncoding(encodingId = 1) {

    private lateinit var toCopyPixels: IntArray

    override fun readAndDecode(
        inputStream: ExtendedDataInputStream,
        rfbClient: RfbClient,
        width: Int,
        height: Int,
        xPosition: Int,
        yPosition: Int
    ) {
        val srcX = inputStream.readUnsignedShort()
        val srcY = inputStream.readUnsignedShort()

        if (!::toCopyPixels.isInitialized) {
            toCopyPixels =
                IntArray(rfbClient.frameBufferInfo.frameBufferWidth * rfbClient.frameBufferInfo.frameBufferHeight)
        }

        rfbClient.bitmap.getPixels(toCopyPixels, 0, width, srcX, srcY, width, height)
        rfbClient.bitmap.setPixels(toCopyPixels, 0, width, xPosition, yPosition, width, height)
    }

}