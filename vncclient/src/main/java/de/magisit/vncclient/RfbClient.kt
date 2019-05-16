package de.magisit.vncclient

import android.util.Log
import de.magisit.vncclient.protocol.RfbProtocol
import de.magisit.vncclient.protocol.encodings.Encoding
import de.magisit.vncclient.protocol.handshake.Handshaker
import java.net.Socket

class RfbClient(val settings: RfbSettings) {

    private lateinit var internalCodings: ArrayList<Encoding>
    private val TAG = "RfbClient"

    init {

    }

    fun connect() {
        val socket = Socket()
        val rfbProtocol = RfbProtocol(socket)
        Handshaker(
            settings = settings,
            socket = socket
        ) {
            Log.i(this.TAG, "connect: Received handshake callback")
            rfbProtocol.startProtocol()
        }.doHandshake()
    }

    fun disconnect() {

    }

    fun setMousePosition() {

    }

    fun sendKeyboardInput() {

    }

    fun setDesktopSize() {

    }

    fun sendMouseClick() {

    }

    fun requestFrameBufferUpdate() {

    }


}