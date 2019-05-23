package de.magisit.vncclient.protocol.encodings.pseudo

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.encodings.PseudoEncoding
import de.magisit.vncclient.utils.ExtendedDataInputStream

class FencePseudoEncoding : PseudoEncoding(encodingId = -312) {
    override fun readAndDecode(inputStream: ExtendedDataInputStream, rfbClient: RfbClient) {

    }

}