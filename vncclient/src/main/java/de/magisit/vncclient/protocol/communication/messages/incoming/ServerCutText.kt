package de.magisit.vncclient.protocol.communication.messages.incoming

import android.util.Log
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.IncomingMessage
import de.magisit.vncclient.utils.ExtendedDataInputStream
import java.nio.charset.StandardCharsets

class ServerCutText : IncomingMessage(messageId = 3) {


    override fun onMessageReceived(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {

        inputStream.skipBytes(3)

        val length = inputStream.readInt()

        val text = ByteArray(length)
        inputStream.readFully(text)

        rfbClient.copiedText = String(text, StandardCharsets.ISO_8859_1)

        Log.i("ServerCutText", "Server requested the cutting of text \"${rfbClient.copiedText}\"")
    }

}