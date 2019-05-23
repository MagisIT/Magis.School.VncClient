package de.magisit.vncclient.protocol.handshake.authentication

import de.magisit.vncclient.protocol.handshake.ProtocolVersion
import de.magisit.vncclient.utils.ExtendedDataInputStream
import de.magisit.vncclient.utils.ExtendedDataOutputStream

/**
 * Abstract class which all Security types must implement
 */
abstract class SecurityType(val securityTypeId: Int) {
    /**
     * Abstract function which authenticates the user to the server
     */
    abstract fun authenticate(
            protocolVersion: ProtocolVersion,
            dataInputStream: ExtendedDataInputStream,
            dataOutputStream: ExtendedDataOutputStream
    )
}