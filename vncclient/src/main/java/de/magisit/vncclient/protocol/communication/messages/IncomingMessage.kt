package de.magisit.vncclient.protocol.communication.messages

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.utils.ExtendedDataInputStream

abstract class IncomingMessage(messageId: Int) : Message(messageId) {
    abstract fun onMessageReceived(inputStream: ExtendedDataInputStream, rfbClient: RfbClient)
}