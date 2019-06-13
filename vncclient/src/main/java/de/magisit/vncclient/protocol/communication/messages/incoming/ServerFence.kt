package de.magisit.vncclient.protocol.communication.messages.incoming

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.IncomingMessage
import de.magisit.vncclient.protocol.communication.messages.outgoing.ClientFence
import de.magisit.vncclient.protocol.communication.messages.outgoing.EnableContinuousUpdates
import de.magisit.vncclient.utils.ExtendedDataInputStream

class ServerFence : IncomingMessage(messageId = 248) {

    val skipBytes = ByteArray(3)

    override fun onMessageReceived(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {
        inputStream.readFully(skipBytes, 0, skipBytes.size)

        val flags = inputStream.readUInt32()
        val length = inputStream.readUnsignedByte()

        val payload = ByteArray(length)
        inputStream.readFully(payload)

        val clearedRequestFlag = flags and 0b01111111_11111111_11111111_11111111

        val clientFence = ClientFence(
            flags = clearedRequestFlag,
            length = length,
            payload = payload
        )

        rfbClient.rfbProtocol.sendMessage(clientFence, true)
        rfbClient.rfbProtocol.sendMessage(EnableContinuousUpdates(true, 0, 0, 1920, 1080))
    }

}