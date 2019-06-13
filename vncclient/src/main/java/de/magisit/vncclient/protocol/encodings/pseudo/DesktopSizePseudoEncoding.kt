package de.magisit.vncclient.protocol.encodings.pseudo

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.encodings.PseudoEncoding
import de.magisit.vncclient.utils.ExtendedDataInputStream

class DesktopSizePseudoEncoding : PseudoEncoding(encodingId = -223) {
    override fun readAndDecode(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {
    }
}