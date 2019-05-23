package de.magisit.vncclient

import de.magisit.vncclient.protocol.encodings.FrameEncoding
import de.magisit.vncclient.protocol.encodings.PseudoEncoding
import de.magisit.vncclient.protocol.handshake.authentication.SecurityType
import de.magisit.vncclient.protocol.handshake.authentication.SecurityTypeNone

class RfbSettings constructor(
        val host: String = "localhost",
        val port: Int = 5901,
        val securityType: SecurityType = SecurityTypeNone(),
        val leaveOtherClientsConnected: Boolean = true,
        val customPseudoEncodings: ArrayList<PseudoEncoding> = arrayListOf(),
        val customFrameEncodings: ArrayList<FrameEncoding> = arrayListOf()
)