package de.magisit.vncclient.protocol.communication.messages

import de.magisit.vncclient.utils.ExtendedDataInputStream

abstract class MessageType(val messageTypeId: Int) {
    abstract fun messageTypeIdReceived(inputStream: ExtendedDataInputStream)
}