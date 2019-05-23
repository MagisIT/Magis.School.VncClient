package de.magisit.vncclient.protocol.communication.messages.outgoing

import android.util.Log
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.OutgoingMessage
import de.magisit.vncclient.protocol.encodings.Encoding
import de.magisit.vncclient.utils.ConvertUtils
import de.magisit.vncclient.utils.ExtendedDataOutputStream

class SetEncodings(private val encodings: HashMap<Int, Encoding>) :
        OutgoingMessage(messageId = 2) {

    private val TAG = "SetEncodings"

    override fun sendMessage(outputStream: ExtendedDataOutputStream, rfbClient: RfbClient) {
        Log.i(this.TAG, "sendMessage: Sending the supported encodings to the server")
        outputStream.write(this.getSupportEncodingArray())
    }

    private fun getSupportEncodingArray(): ByteArray {
        // Initialize the ByteArray to send the Encodings
        // 1B MessageType + 1B Padding + 2B Number of Encodings + 4B  for each Encoding
        val encodingMessage = ByteArray(1 + 1 + 2 + (4 * this.encodings.size))

        // Set the message type
        encodingMessage[0] = this.messageId.toByte()

        // Set the padding byte
        encodingMessage[1] = 0

        // Set the number of encodings
        val encodingCount = ConvertUtils.toUInt16(this.encodings.size)
        System.arraycopy(encodingCount, 0, encodingMessage, 2, 2)

        var i = 0
        this.encodings.values.forEach {
            System.arraycopy(
                    ConvertUtils.toSInt32Array(it.encodingId),
                    0,
                    encodingMessage,
                    4 + (4 * i),
                    4
            )
            i++
        }

        return encodingMessage
    }
}