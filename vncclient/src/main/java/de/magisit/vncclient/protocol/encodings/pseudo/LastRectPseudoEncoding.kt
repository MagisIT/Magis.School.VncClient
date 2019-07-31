package de.magisit.vncclient.protocol.encodings.pseudo

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.encodings.PseudoEncoding
import de.magisit.vncclient.utils.ExtendedDataInputStream

class LastRectPseudoEncoding : PseudoEncoding(encodingId = -224) {
    override fun readAndDecode(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {
    }

}