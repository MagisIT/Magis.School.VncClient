package de.magisit.vncclient.protocol.communication

import android.util.Log
import de.magisit.vncclient.protocol.communication.messages.MessageType
import de.magisit.vncclient.utils.ExtendedDataInputStream

class RfbMessageReceiver() {

    private val TAG = "RfbMessageReceiver"
    private val messageTypes: HashMap<Int, MessageType> = HashMap()

    fun startReceiving(inputStream: ExtendedDataInputStream) {
        var messageTypeId: Int = -1
        while (inputStream.readUnsignedByte().also { messageTypeId = it } != -1) {

            Log.d(this.TAG, "startReceiving: Received a message with id $messageTypeId")

            val messageType = messageTypes[messageTypeId]

            if (messageType == null) {
                Log.i(this.TAG, "Received unregistered message type")
                continue
            }

            messageType.messageTypeIdReceived(inputStream)
        }
    }

    fun registerMessageType(messageType: MessageType) {
        this.messageTypes[messageType.messageTypeId] = messageType
        Log.i(this.TAG, "Added Message Type ${messageType.messageTypeId} to the List of Message Types")
    }

}