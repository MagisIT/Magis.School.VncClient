package de.magisit.vncclient.protocol.encodings.frame

import android.graphics.Color
import androidx.core.graphics.set
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.encodings.FrameEncoding
import de.magisit.vncclient.utils.ExtendedDataInputStream

class RREEncoding : FrameEncoding(encodingId = 2) {

    private lateinit var rectInfo: ByteArray
    private lateinit var sData: ByteArray

    override fun readAndDecode(
        inputStream: ExtendedDataInputStream,
        rfbClient: RfbClient,
        width: Int,
        height: Int,
        xPosition: Int,
        yPosition: Int
    ) {
        // Bits per pixel from the PixelFormat information
        val bitsPerPixel = rfbClient.frameBufferInfo.pixelFormat.bitsPerPixel

        // Bits per pixel divided by 8 to get bytes
        val bytesPerPixel = bitsPerPixel / 8

        if (!::rectInfo.isInitialized) {
            rectInfo = ByteArray(bytesPerPixel)
        }

        // Pixel color mask to extract the colors after shifting
        var pixelColorMask = 0

        // Set the pixel mask
        repeat(bitsPerPixel / 4) {
            pixelColorMask = pixelColorMask or (1 shl it)
        }

        // Initialize the pixel data
        var pixelData = 0

        // Get the blue, green and red shift from PixelData
        val blueShift = rfbClient.frameBufferInfo.pixelFormat.blueShift
        val greenShift = rfbClient.frameBufferInfo.pixelFormat.greenShift
        val redShift = rfbClient.frameBufferInfo.pixelFormat.redShift

        var blue: Int
        var green: Int
        var red: Int

        var pixelColor: Int

        val numberOfSubRectangles = inputStream.readInt()

        inputStream.readFully(rectInfo)

        repeat(bytesPerPixel) {
            // Add the new byte to the existing pixel data
            pixelData = pixelData or (rectInfo[it].toInt() and 0xFF shl (it * 8))
        }
        blue = (pixelData shr blueShift) and pixelColorMask
        green = (pixelData shr greenShift) and pixelColorMask
        red = (pixelData shr redShift) and pixelColorMask

        pixelColor = Color.rgb(red, green, blue)

        repeat(height) { y ->
            repeat(width) { x ->
                rfbClient.bitmap[xPosition + x, yPosition + y] = pixelColor
            }
        }

        var sX: Int
        var sY: Int
        var sW: Int
        var sH: Int

        if (!::sData.isInitialized) {
            sData = ByteArray(bytesPerPixel + 2 + 2 + 2 + 2)
        }

        repeat(numberOfSubRectangles) {
            inputStream.readFully(sData, 0, sData.size)

            repeat(bytesPerPixel) {
                // Add the new byte to the existing pixel data
                pixelData = pixelData or (sData[it].toInt() and 0xFF shl (it * 8))
            }

            blue = (pixelData shr blueShift) and pixelColorMask
            green = (pixelData shr greenShift) and pixelColorMask
            red = (pixelData shr redShift) and pixelColorMask

            pixelColor = Color.rgb(red, green, blue)

            sX = ((sData[bytesPerPixel].toInt() and 255) shl 8) or ((sData[bytesPerPixel + 1].toInt() and 255))
            sY = ((sData[bytesPerPixel + 2].toInt() and 255) shl 8) or ((sData[bytesPerPixel + 3].toInt() and 255))
            sW = ((sData[bytesPerPixel + 4].toInt() and 255) shl 8) or ((sData[bytesPerPixel + 5].toInt() and 255))
            sH = ((sData[bytesPerPixel + 6].toInt() and 255) shl 8) or ((sData[bytesPerPixel + 7].toInt() and 255))

            repeat(sH) { y ->
                repeat(sW) { x ->
                    rfbClient.bitmap[xPosition + sX + x, yPosition + sY + y] = pixelColor
                }
            }
        }
    }

}