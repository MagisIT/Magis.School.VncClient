package de.magisit.vncclient

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import de.magisit.vncclient.protocol.RfbProtocol
import de.magisit.vncclient.protocol.encodings.Encoding
import de.magisit.vncclient.protocol.encodings.FrameEncoding
import de.magisit.vncclient.protocol.encodings.PseudoEncoding
import de.magisit.vncclient.protocol.encodings.frame.*
import de.magisit.vncclient.protocol.encodings.pseudo.ContinuousUpdatesPseudoEncoding
import de.magisit.vncclient.protocol.encodings.pseudo.DesktopSizePseudoEncoding
import de.magisit.vncclient.protocol.encodings.pseudo.FencePseudoEncoding
import de.magisit.vncclient.protocol.encodings.pseudo.LastRectPseudoEncoding
import de.magisit.vncclient.protocol.handshake.FrameBufferInfo
import de.magisit.vncclient.protocol.handshake.Handshaker
import java.net.Socket

/**
 * RfbClient
 */
@SuppressLint("UseSparseArrays")
class RfbClient(val settings: RfbSettings, val updateBitmap: (Bitmap) -> Unit) {

    // Tag for logging
    private val TAG = "RfbClient"

    // Frame buffer info from handshake
    lateinit var frameBufferInfo: FrameBufferInfo

    // Internally supported pseudo encodings
    private val internalPseudoEncodings: ArrayList<PseudoEncoding> = arrayListOf()

    // internal supported frame encodings
    private val internalFrameEncodings: ArrayList<FrameEncoding> = arrayListOf()

    // Merged encodings (all pseudo and frame encodings, internally and custom)
    val mergedEncodings = HashMap<Int, Encoding>()

    // Merged pseudo encodings (internally and custom)
    val mergedPseudoEncodings = HashMap<Int, PseudoEncoding>()

    // Merged frame encodings (internally and custom)
    val mergedFrameEncodings = HashMap<Int, FrameEncoding>()

    var copiedText: String = ""

    lateinit var rfbProtocol: RfbProtocol

    // Bitmap to show the frame buffer
    // TODO Build an abstract system to support custom resources for image showing not only a bitmap (which is android internal)
    lateinit var bitmap: Bitmap


    /**
     * Init function of the class
     */
    init {
        // Add the internal pseudo encodings to the list
        this.internalPseudoEncodings.add(DesktopSizePseudoEncoding())
        this.internalPseudoEncodings.add(FencePseudoEncoding())
        this.internalPseudoEncodings.add(ContinuousUpdatesPseudoEncoding())
        this.internalPseudoEncodings.add(LastRectPseudoEncoding())

        // Add the internal frame encodings to the list
        this.internalFrameEncodings.add(RawEncoding())
        this.internalFrameEncodings.add(CopyRectEncoding())
        this.internalFrameEncodings.add(RREEncoding())
        this.internalFrameEncodings.add(CoRREEncoding())
        this.internalFrameEncodings.add(HextileEncoding())
        this.internalFrameEncodings.add(ZLibEncoding())
        this.internalFrameEncodings.add(TightEncoding())
        this.internalFrameEncodings.add(ZLibHexEncoding())
        this.internalFrameEncodings.add(ZRLEEncoding())
        this.internalFrameEncodings.add(TightPngEncoding())

        this.internalFrameEncodings.add(LastRectEncoding())
        this.internalFrameEncodings.add(DesktopResizeEncoding())

        // Merge the encodings
        this.mergeEncodings()
    }

    /**
     * Connect the rfb client
     */
    fun connect() {
        // Create a socket
        val socket = Socket()

        // Initialize the rfb protocol
        rfbProtocol = RfbProtocol(
            socket = socket,
            encodingsList = mergedEncodings,
            rfbClient = this
        )

        // Initialize a handshaker and start the handshake
        Handshaker(
            settings = settings,
            socket = socket
        ) {
            // If the handshake is done, start the protocol
            Log.i(this.TAG, "connect: Received handshake callback")
            frameBufferInfo = it

            // Initialize the bitmap
            val bitmapConfig = Bitmap.Config.ARGB_8888
            bitmap = Bitmap.createBitmap(
                frameBufferInfo.frameBufferWidth,
                frameBufferInfo.frameBufferHeight,
                bitmapConfig
            )

            rfbProtocol.startProtocol()
        }.doHandshake()
    }

    fun disconnect() {

    }

    fun setMousePosition() {

    }

    fun sendKeyboardInput() {

    }

    fun setDesktopSize() {

    }

    fun sendMouseClick() {

    }

    fun requestFrameBufferUpdate() {

    }

    /**
     * Merge the internal and custom frame and pseudo encodings
     */
    private fun mergeEncodings() {

        internalPseudoEncodings.forEach {
            mergedPseudoEncodings[it.encodingId] = it
            mergedEncodings[it.encodingId] = it
        }

        settings.customPseudoEncodings.forEach {
            mergedPseudoEncodings[it.encodingId] = it
            mergedEncodings[it.encodingId] = it
        }

        internalFrameEncodings.forEach {
            mergedFrameEncodings[it.encodingId] = it
            mergedEncodings[it.encodingId] = it
        }

        settings.customFrameEncodings.forEach {
            mergedFrameEncodings[it.encodingId] = it
            mergedEncodings[it.encodingId] = it
        }
    }

}