package de.magisit.vncclient.protocol.communication.messages.incoming

import android.util.Log
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.IncomingMessage
import de.magisit.vncclient.utils.ExtendedDataInputStream

class Bell : IncomingMessage(messageId = 2) {
    override fun onMessageReceived(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {
        Log.i("Bell", "Ring Ring")
    }

}