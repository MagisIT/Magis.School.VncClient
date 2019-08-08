package de.magisit.vncclient.protocol.communication.messages.incoming

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.IncomingMessage
import de.magisit.vncclient.protocol.communication.messages.outgoing.ClientFence
import de.magisit.vncclient.protocol.communication.messages.outgoing.EnableContinuousUpdates
import de.magisit.vncclient.utils.ExtendedDataInputStream

class ServerFence : IncomingMessage(messageId = 248) {

    val skipBytes = ByteArray(3)

    var flags: Long = 0
    var length: Int = 0
    var payload: ByteArray = ByteArray(1)
    var clientFence: ClientFence? = null
    var clearedRequestFlag: Long = 0

    override fun onMessageReceived(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {
        inputStream.readFully(skipBytes, 0, skipBytes.size)

        flags = inputStream.readUInt32()
        length = inputStream.readUnsignedByte()

        if (payload.size != length) payload = ByteArray(length)
        inputStream.readFully(payload)

        clearedRequestFlag = flags and 0b00000000_00000000_00000000_00000111

        clientFence = ClientFence(
            flags = clearedRequestFlag,
            length = length,
            payload = payload
        )

        rfbClient.rfbProtocol.sendMessage(clientFence!!, true)
        rfbClient.rfbProtocol.sendMessage(EnableContinuousUpdates(true, 0, 0, 1920, 1080))
    }

}