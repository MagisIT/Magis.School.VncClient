package de.magisit.vncclient

import de.magisit.vncclient.protocol.handshake.authentication.SecurityType
import de.magisit.vncclient.protocol.handshake.authentication.SecurityTypeNone

class RfbSettings constructor(
    val host: String = "localhost",
    val port: Int = 5901,
    val securityType: SecurityType = SecurityTypeNone()
)