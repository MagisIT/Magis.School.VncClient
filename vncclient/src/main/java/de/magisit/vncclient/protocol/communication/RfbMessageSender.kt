package de.magisit.vncclient.protocol.communication

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.OutgoingMessage
import de.magisit.vncclient.utils.ExtendedDataOutputStream
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.thread

class RfbMessageSender {

    // Initialize a blocking queue to ensure message synchronization
    private val blockingQueue: LinkedBlockingDeque<OutgoingMessage> = LinkedBlockingDeque()

    /**
     * Adds a message to the blocking queue
     */
    fun sendMessage(outgoingMessage: OutgoingMessage, highPriority: Boolean = false) {
        if (highPriority) {
            blockingQueue.addFirst(outgoingMessage)
        } else {
            blockingQueue.add(outgoingMessage)
        }
    }

    /**
     * Starts a thread which checks the queue for new entries to send
     */
    fun startSending(outputStream: ExtendedDataOutputStream, rfbClient: RfbClient) {
        // Start the sending thread
        thread {
            while (true) {
                // Get an entry from the queue
                val message = blockingQueue.take()

                // Send the message
                message.sendMessage(outputStream, rfbClient)
            }
        }
    }

}

