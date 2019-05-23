package de.magisit.vncclient.protocol.encodings

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.utils.ExtendedDataInputStream

abstract class PseudoEncoding(encodingId: Int) : Encoding(encodingId) {
    // TODO the function is probably not needed (check in rfc)
    abstract fun readAndDecode(inputStream: ExtendedDataInputStream, rfbClient: RfbClient)
}