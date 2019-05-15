package de.magisit.vncclient

import de.magisit.vncclient.protocol.encodings.Encoding
import de.magisit.vncclient.protocol.handshake.Handshaker
import java.net.Socket

class RfbClient(val settings: RfbSettings) {

    private lateinit var internalCodings: ArrayList<Encoding>

    init {

    }

    fun connect() {
        val socket = Socket()
        Handshaker(
            settings = settings,
            socket = socket
        ) {
            // TODO Go on with the protocol
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


}