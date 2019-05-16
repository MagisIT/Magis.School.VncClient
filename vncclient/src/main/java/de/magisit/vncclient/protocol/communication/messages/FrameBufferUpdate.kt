package de.magisit.vncclient.protocol.communication.messages

import android.util.Log
import de.magisit.vncclient.utils.ExtendedDataInputStream

class FrameBufferUpdate : MessageType(messageTypeId = 0) {

    private val TAG = "FrameBufferUpdate"

    override fun messageTypeIdReceived(inputStream: ExtendedDataInputStream) {
        Log.i(this.TAG, "Received a FrameBufferUpdateMessage!")
    }

}