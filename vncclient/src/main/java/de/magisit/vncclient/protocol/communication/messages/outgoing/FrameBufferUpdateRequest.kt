package de.magisit.vncclient.protocol.communication.messages.outgoing

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.OutgoingMessage
import de.magisit.vncclient.utils.ConvertUtils
import de.magisit.vncclient.utils.ExtendedDataOutputStream

class FrameBufferUpdateRequest(
        private val incremental: Boolean,
        private val xPosition: Int,
        private val yPosition: Int,
        private val width: Int,
        private val height: Int
) : OutgoingMessage(messageId = 3) {

    private val TAG = "FrameBufferUpdateReq"

    override fun sendMessage(outputStream: ExtendedDataOutputStream, rfbClient: RfbClient) {
        outputStream.write(this.getFrameBufferUpdateMessage())
    }

    private fun getFrameBufferUpdateMessage(): ByteArray {
        val fbuEncodingMessage = ByteArray(1 + 1 + 2 + 2 + 2 + 2)

        fbuEncodingMessage[0] = this.messageId.toByte()

        when (this.incremental) {
            true -> fbuEncodingMessage[1] = 1
            false -> fbuEncodingMessage[1] = 0
        }

        val x = ConvertUtils.toUInt16(this.xPosition)
        val y = ConvertUtils.toUInt16(this.yPosition)
        val w = ConvertUtils.toUInt16(this.width)
        val h = ConvertUtils.toUInt16(this.height)

        System.arraycopy(x, 0, fbuEncodingMessage, 2, 2)
        System.arraycopy(y, 0, fbuEncodingMessage, 4, 2)
        System.arraycopy(w, 0, fbuEncodingMessage, 6, 2)
        System.arraycopy(h, 0, fbuEncodingMessage, 8, 2)

        return fbuEncodingMessage
    }
}