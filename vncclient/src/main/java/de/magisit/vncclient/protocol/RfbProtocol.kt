package de.magisit.vncclient.protocol

import de.magisit.vncclient.protocol.communication.RfbMessageReceiver
import de.magisit.vncclient.protocol.communication.messages.FrameBufferUpdate
import de.magisit.vncclient.utils.ExtendedDataInputStream
import java.net.Socket
import kotlin.concurrent.thread

class RfbProtocol(
    val socket: Socket
) {

    fun startProtocol() {
        thread {
            val messageReceiver = RfbMessageReceiver()

            messageReceiver.registerMessageType(FrameBufferUpdate())

            messageReceiver.startReceiving(ExtendedDataInputStream(socket.getInputStream()))
        }

    }


}