package de.magisit.vncclient.protocol.communication.messages.incoming

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.IncomingMessage
import de.magisit.vncclient.utils.ExtendedDataInputStream

/**
 * EndOfContinuousUpdates Message
 */
class EndOfContinuousUpdates : IncomingMessage(messageId = 150) {
    override fun onMessageReceived(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {
        // No Payload included in the message, only indicates that the server hast stopped sending continuous updates
        return
    }
}