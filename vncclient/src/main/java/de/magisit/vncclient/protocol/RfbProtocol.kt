package de.magisit.vncclient.protocol

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.RfbMessageReceiver
import de.magisit.vncclient.protocol.communication.RfbMessageSender
import de.magisit.vncclient.protocol.communication.messages.OutgoingMessage
import de.magisit.vncclient.protocol.communication.messages.incoming.*
import de.magisit.vncclient.protocol.communication.messages.outgoing.FrameBufferUpdateRequest
import de.magisit.vncclient.protocol.communication.messages.outgoing.SetEncodings
import de.magisit.vncclient.protocol.encodings.Encoding
import de.magisit.vncclient.utils.ExtendedDataInputStream
import de.magisit.vncclient.utils.ExtendedDataOutputStream
import java.net.Socket
import kotlin.concurrent.thread

/**
 * Represents the structure of the protocol, handles message receiving and message sending
 */
class RfbProtocol(
    val socket: Socket,
    val encodingsList: HashMap<Int, Encoding>,
    val rfbClient: RfbClient
) {
    // Initialize the message sender and the message receiver
    private val messageReceiver: RfbMessageReceiver = RfbMessageReceiver()
    private val messageSender: RfbMessageSender = RfbMessageSender()

    /**
     * Function to start the protocol, starts the message sender and receiver and sends some needed messages at the beginning
     */
    fun startProtocol() {
        // Initialize the inputStreams
        val inputStream = ExtendedDataInputStream(socket.getInputStream())
        val outputStream = ExtendedDataOutputStream(socket.getOutputStream())

        // Start the thread to receive incoming messages
        thread {
            // Register the messageTypes supported
            // Messages that must be supported
            messageReceiver.registerMessageType(FrameBufferUpdate())
            messageReceiver.registerMessageType(ServerCutText())
            messageReceiver.registerMessageType(Bell())

            // Optional
            messageReceiver.registerMessageType(ServerFence())
            messageReceiver.registerMessageType(EndOfContinuousUpdates())

            // Start receiving messages
            messageReceiver.startReceiving(inputStream, this.rfbClient)
        }

        thread {
            // Start sending messages
            messageSender.startSending(outputStream, this.rfbClient)
            // Send the list of encodings to the server
            messageSender.sendMessage(SetEncodings(this.encodingsList))


            // Request the first frame buffer (fully)
            messageSender.sendMessage(
                FrameBufferUpdateRequest(
                    false,
                    0,
                    0,
                    this.rfbClient.frameBufferInfo.frameBufferWidth,
                    this.rfbClient.frameBufferInfo.frameBufferHeight
                )
            )
        }
    }

    fun sendMessage(message: OutgoingMessage, highPriority: Boolean = false) {
        this.messageSender.sendMessage(message, highPriority)
    }

}