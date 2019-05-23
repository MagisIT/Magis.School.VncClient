package de.magisit.vncclient.protocol.communication

import android.util.Log
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.IncomingMessage
import de.magisit.vncclient.utils.ExtendedDataInputStream
import de.magisit.vncclient.utils.exceptions.RfbProtocolException

class RfbMessageReceiver {

    private val TAG = "RfbMessageReceiver"
    private val messageTypes: HashMap<Int, IncomingMessage> = HashMap()

    fun startReceiving(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {
        var messageId: Int

        while (inputStream.readUnsignedByte().also { messageId = it } != -1) {
            // Log the received message type
            Log.d(this.TAG, "startReceiving: Received a message with id $messageId")

            // Check if the message type can be handled by one of the message types otherwise throw an exception
            // because the length of the following message is unknown and you cant tell where the next messsage
            // starts
            val messageType = messageTypes[messageId]
                    ?: throw RfbProtocolException("Received a message type that is not supported, aborting!")

            // Trigger the function on the specific message type object
            messageType.onMessageReceived(inputStream, rfbClient)
        }

        Log.i(this.TAG, "Aborting...")
    }

    fun registerMessageType(incomingMessage: IncomingMessage) {
        this.messageTypes[incomingMessage.messageId] = incomingMessage
        Log.i(this.TAG, "Added Message Type ${incomingMessage.messageId} to the List of Message Types")
    }

}