package de.magisit.vncclient.protocol.communication.messages.outgoing

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.OutgoingMessage
import de.magisit.vncclient.utils.ExtendedDataOutputStream

class ClientFence(
    private val flags: Long,
    private val length: Int,
    private val payload: ByteArray
) : OutgoingMessage(messageId = 248) {
    override fun sendMessage(outputStream: ExtendedDataOutputStream, rfbClient: RfbClient) {

        outputStream.write(this.messageId)
        outputStream.write(byteArrayOf(0, 0, 0))
        outputStream.writeUInt32(this.flags)
        outputStream.write(this.length)
        outputStream.write(payload)
    }

}