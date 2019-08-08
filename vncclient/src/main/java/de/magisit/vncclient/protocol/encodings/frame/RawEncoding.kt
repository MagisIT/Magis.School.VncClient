package de.magisit.vncclient.protocol.encodings.frame

import android.graphics.Color
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.encodings.FrameEncoding
import de.magisit.vncclient.utils.ExtendedDataInputStream

/**
 * Decodes a rectangle of PixelData into argb pixels
 */
class RawEncoding : FrameEncoding(0) {
    var bitsPerPixel: Int = 0
    var bytesPerPixel: Int = 0
    var pixelColorMask: Int = 0
    var blueShift: Int = 0
    var greenShift: Int = 0
    var redShift: Int = 0
    var pixelData: Int = 0
    var blue: Int = 0
    var green: Int = 0
    var red: Int = 0
    var totalBytes: Int = 0
    var totalReadBytes: Int = 0
    var chunkBuffer: ByteArray = ByteArray(16000)
    var colorOffset: Int = 0
    var remainingBytes: Int = 0
    var chunkLength: Int = 0
    var computedBytes: Int = 0
    var pixels = intArrayOf()
    var currentPixelIndex = 0


    /**
     * Read the data from the input stream and decode it
     */
    override fun readAndDecode(
        inputStream: ExtendedDataInputStream,
        rfbClient: RfbClient,
        width: Int,
        height: Int,
        xPosition: Int,
        yPosition: Int
    ) {
        pixels = IntArray(width * height)
        currentPixelIndex = 0

        // Bits per pixel from the PixelFormat information
        bitsPerPixel = rfbClient.frameBufferInfo.pixelFormat.bitsPerPixel

        // Bits per pixel divided by 8 to get bytes
        bytesPerPixel = bitsPerPixel / 8

        // Pixel color mask to extract the colors after shifting
        pixelColorMask = 0

        // Set the pixel mask
        repeat(bitsPerPixel / 4) {
            pixelColorMask = pixelColorMask or (1 shl it)
        }

        // Get the blue, green and red shift from PixelData
        blueShift = rfbClient.frameBufferInfo.pixelFormat.blueShift
        greenShift = rfbClient.frameBufferInfo.pixelFormat.greenShift
        redShift = rfbClient.frameBufferInfo.pixelFormat.redShift

        // Initialize the pixel data
        pixelData = 0

        // Calculate the total bytes to read
        totalBytes = height * width * bytesPerPixel

        // Set the read bytes to 0
        totalReadBytes = 0

        // Initialize the color offset
        colorOffset = 0

        // Repeat until all bytes of the current rectangle are read
        while (totalReadBytes < totalBytes) {
            // Update the remaining bytes
            remainingBytes = totalBytes - totalReadBytes

            // If the remaining bytes are greater than the chunk array set the remaining bytes to the size
            if (remainingBytes > chunkBuffer.size)
                remainingBytes = chunkBuffer.size

            // Read a chunk out of the input stream
            chunkLength = inputStream.read(chunkBuffer, 0, remainingBytes)

            // Reset the computed bytes
            computedBytes = 0

            // Repeat for each byte in the chunk
            repeat(chunkLength) {
                // Add the new byte to the existing pixel data
                pixelData =
                    pixelData or (chunkBuffer[computedBytes++].toInt() and 0xFF shl (colorOffset * 8))

                // If we read the needed 4 bytes (RGBA) shift them by the shift value and apply the color mask
                if (++colorOffset == bytesPerPixel) {
                    blue = (pixelData shr blueShift) and pixelColorMask
                    green = (pixelData shr greenShift) and pixelColorMask
                    red = (pixelData shr redShift) and pixelColorMask

                    // TODO: Multiply color values to support 24 and 16 bit colors.

                    pixels[currentPixelIndex++] = Color.rgb(red, green, blue)


                    // Reset the pixel data and color offset
                    pixelData = 0
                    colorOffset = 0
                }
            }

            // Increase the total read bytes by the length of the chunk processed
            totalReadBytes += chunkLength
        }

        rfbClient.bitmap.setPixels(pixels, 0, width, xPosition, yPosition, width, height)
    }
}