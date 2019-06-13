package de.magisit.vncclient.protocol.communication.messages.outgoing

import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.communication.messages.OutgoingMessage
import de.magisit.vncclient.utils.ConvertUtils
import de.magisit.vncclient.utils.ExtendedDataOutputStream

class EnableContinuousUpdates(
    private val enable: Boolean,
    private val xPosition: Int,
    private val yPosition: Int,
    private val width: Int,
    private val height: Int
) : OutgoingMessage(messageId = 150) {

    override fun sendMessage(outputStream: ExtendedDataOutputStream, rfbClient: RfbClient) {
        val enableContinuousUpdatesMessage = ByteArray(10)

        enableContinuousUpdatesMessage[0] = messageId.toByte()

        when (this.enable) {
            true -> enableContinuousUpdatesMessage[1] = 1
            false -> enableContinuousUpdatesMessage[0] = 0
        }

        val x = ConvertUtils.toUInt16(this.xPosition)
        val y = ConvertUtils.toUInt16(this.yPosition)
        val w = ConvertUtils.toUInt16(this.width)
        val h = ConvertUtils.toUInt16(this.height)

        System.arraycopy(x, 0, enableContinuousUpdatesMessage, 2, 2)
        System.arraycopy(y, 0, enableContinuousUpdatesMessage, 4, 2)
        System.arraycopy(w, 0, enableContinuousUpdatesMessage, 6, 2)
        System.arraycopy(h, 0, enableContinuousUpdatesMessage, 8, 2)

        outputStream.write(enableContinuousUpdatesMessage)

    }

}