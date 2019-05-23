package de.magisit.vncclient.protocol.encodings

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.utils.ExtendedDataInputStream

abstract class FrameEncoding(encodingId: Int) : Encoding(encodingId) {
    abstract fun readAndDecode(
            inputStream: ExtendedDataInputStream,
            rfbClient: RfbClient,
            width: Int,
            height: Int,
            xPosition: Int,
            yPosition: Int
    )
}