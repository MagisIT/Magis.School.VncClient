package de.magisit.vncclient.protocol.communication.messages

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.utils.ExtendedDataOutputStream

abstract class OutgoingMessage(messageId: Int) : Message(messageId) {
    abstract fun sendMessage(outputStream: ExtendedDataOutputStream, rfbClient: RfbClient)
}