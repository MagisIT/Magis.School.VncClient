package de.magisit.vncclient.protocol.handshake.authentication

import de.magisit.vncclient.protocol.handshake.ProtocolVersion
import de.magisit.vncclient.utils.ExtendedDataInputStream
import de.magisit.vncclient.utils.ExtendedDataOutputStream

abstract class SecurityType(val securityTypeId: Int) {
    abstract fun authenticate(
        protocolVersion: ProtocolVersion,
        dataInputStream: ExtendedDataInputStream,
        dataOutputStream: ExtendedDataOutputStream
    )
}