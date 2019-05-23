package de.magisit.vncclient.protocol.communication.messages.incoming

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.IncomingMessage
import de.magisit.vncclient.utils.ExtendedDataInputStream

class EndOfContinuousUpdates : IncomingMessage(messageId = 150) {
    override fun onMessageReceived(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {
    }

}