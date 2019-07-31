package de.magisit.vncclient.protocol.communication.messages.incoming

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.IncomingMessage
import de.magisit.vncclient.protocol.encodings.frame.LastRectEncoding
import de.magisit.vncclient.utils.ExtendedDataInputStream
import de.magisit.vncclient.utils.exceptions.RfbProtocolException

/**
 * FrameBufferUpdate incoming message
 */
class FrameBufferUpdate : IncomingMessage(messageId = 0) {

    /**
     * On message received
     */
    override fun onMessageReceived(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {

        // Skip one byte padding
        inputStream.skipBytes(1)

        // Read the number of rectangles contained in the frame buffer update
        val numberOfRectangles = inputStream.readUnsignedShort()

        // Repeat numberOfRectangles times
        repeat(numberOfRectangles) {
            // Read the x and y position of the current rectangle
            val xPosition = inputStream.readUnsignedShort()
            val yPosition = inputStream.readUnsignedShort()

            // Read the width and height of the current rectangle
            val width = inputStream.readUnsignedShort()
            val height = inputStream.readUnsignedShort()

            // Read the type of encoding received
            val encodingType: Int = inputStream.readInt()

            // Check if the received encoding is supported, if not the server did something wrong and an exception is thrown
            if (!rfbClient.mergedFrameEncodings.containsKey(encodingType)) throw RfbProtocolException("Received an unsupported encoding type")

            val frameEncoding = rfbClient.mergedFrameEncodings[encodingType]!!

            if (frameEncoding is LastRectEncoding) {
                rfbClient.updateBitmap(rfbClient.bitmap)
                return
            }

            // Read and decode the rectangle received
            frameEncoding.readAndDecode(
                inputStream,
                rfbClient,
                width,
                height,
                xPosition,
                yPosition
            )
        }
    }
}