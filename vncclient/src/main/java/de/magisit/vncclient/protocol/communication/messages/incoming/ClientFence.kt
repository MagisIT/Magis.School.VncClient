package de.magisit.vncclient.protocol.communication.messages.incoming

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.IncomingMessage
import de.magisit.vncclient.utils.ExtendedDataInputStream

class ClientFence : IncomingMessage(messageId = 248) {
    override fun onMessageReceived(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {
    }

}