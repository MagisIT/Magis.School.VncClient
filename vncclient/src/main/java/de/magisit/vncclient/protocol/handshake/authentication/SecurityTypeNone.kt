package de.magisit.vncclient.protocol.handshake.authentication

import de.magisit.vncclient.protocol.handshake.ProtocolVersion
import de.magisit.vncclient.utils.ExtendedDataInputStream
import de.magisit.vncclient.utils.ExtendedDataOutputStream

class SecurityTypeNone() : SecurityType(1) {
    override fun authenticate(
        protocolVersion: ProtocolVersion,
        dataInputStream: ExtendedDataInputStream,
        dataOutputStream: ExtendedDataOutputStream
    ) {
        // No authentication needed for the Type "None"
        return
    }

}