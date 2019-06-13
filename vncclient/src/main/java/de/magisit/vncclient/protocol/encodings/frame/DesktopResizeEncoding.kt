package de.magisit.vncclient.protocol.encodings.frame

import android.graphics.Bitmap
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.encodings.FrameEncoding
import de.magisit.vncclient.utils.ExtendedDataInputStream

class DesktopResizeEncoding : FrameEncoding(encodingId = -223) {
    override fun readAndDecode(
        inputStream: ExtendedDataInputStream,
        rfbClient: RfbClient,
        width: Int,
        height: Int,
        xPosition: Int,
        yPosition: Int
    ) {
        // TODO Bug mit 4k Auflösung fixen
        rfbClient.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

}