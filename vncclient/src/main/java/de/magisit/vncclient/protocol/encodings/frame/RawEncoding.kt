package de.magisit.vncclient.protocol.encodings.frame

import android.graphics.Color
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.encodings.FrameEncoding
import de.magisit.vncclient.utils.ExtendedDataInputStream

/**
 * Decodes a rectangle of PixelData into argb pixels
 */
class RawEncoding : FrameEncoding(0) {

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
        // Bits per pixel from the PixelFormat information
        val bitsPerPixel = rfbClient.frameBufferInfo.pixelFormat.bitsPerPixel

        // Bits per pixel divided by 8 to get bytes
        val bytesPerPixel = bitsPerPixel / 8

        // Pixel color mask to extract the colors after shifting
        var pixelColorMask = 0

        // Set the pixel mask
        repeat(bitsPerPixel / 4) {
            pixelColorMask = pixelColorMask or (1 shl it)
        }

        // Get the blue, green and red shift from PixelData
        val blueShift = rfbClient.frameBufferInfo.pixelFormat.blueShift
        val greenShift = rfbClient.frameBufferInfo.pixelFormat.greenShift
        val redShift = rfbClient.frameBufferInfo.pixelFormat.redShift

        // Initialize the pixel data
        var pixelData = 0

        // Initialize the variables for blue, green and red color values
        var blue: Int
        var green: Int
        var red: Int

        // Calculate the total bytes to read
        val totalBytes = height * width * bytesPerPixel

        // Set the read bytes to 0
        var totalReadBytes = 0

        // Initialize the chunk buffer array to hold max. 160000 bytes
        val chunkBuffer = ByteArray(16000)

        // Initialize the x and y coordinate
        var x = 0
        var y = 0

        // Initialize the color offset
        var colorOffset = 0

        // Initialize a variable for the remaining bytes
        var remainingBytes: Int

        // Initialize a variable for the current chunk length (because is.read() does not always read the full array)
        var chunkLength: Int

        // Initialize a variable to save the current position in the chunk byte array
        var computedBytes: Int

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
                pixelData = pixelData or (chunkBuffer[computedBytes++].toInt() and 0xFF shl (colorOffset * 8))

                // If we read the needed 4 bytes (RGBA) shift them by the shift value and apply the color mask
                if (++colorOffset == bytesPerPixel) {
                    blue = (pixelData shr blueShift) and pixelColorMask
                    green = (pixelData shr greenShift) and pixelColorMask
                    red = (pixelData shr redShift) and pixelColorMask

                    // TODO: Multiply color values to support 24 and 16 bit colors.
                    // Set the pixel on the bitmap
                    rfbClient.bitmap.setPixel(xPosition + x, yPosition + y, Color.argb(255, red, green, blue))

                    // If we reached the end of one row set the x coordinate to 0 and increase y by 1
                    if (++x == width) {
                        x = 0
                        y++
                    }

                    // Reset the pixel data and color offset
                    pixelData = 0
                    colorOffset = 0
                }
            }

            // Increase the total read bytes by the length of the chunk processed
            totalReadBytes += chunkLength
            // Notify the client that the bitmap was updated
            rfbClient.updateBitmap(rfbClient.bitmap)
        }
    }
}