package de.magisit.vncclient.protocol.handshake

import android.util.Log
import de.magisit.vncclient.RfbSettings
import de.magisit.vncclient.utils.ExtendedDataInputStream
import de.magisit.vncclient.utils.ExtendedDataOutputStream
import de.magisit.vncclient.utils.Utils
import de.magisit.vncclient.utils.exceptions.VncProtocolException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

class Handshaker(val socket: Socket, val settings: RfbSettings, val onInitializedListener: () -> Unit) {

    val TAG = "Handshaker"
    val CLIENT_RFB_VERSION = ProtocolVersion.RFB_3_8

    fun doHandshake() {
        thread {
            // Connect to socket
            socket.connect(InetSocketAddress(settings.host, settings.port))
            Log.i(this.TAG, "doHandshake: Successfully connected to host ${settings.host} on port ${settings.port}")

            // Get the Inputstream and Outputstream
            val dataInputStream = ExtendedDataInputStream(socket.getInputStream())
            val dataOutputStream = ExtendedDataOutputStream(socket.getOutputStream())

            // Read the Protocol Version from the socket
            val protocolVersion = this.getProtocolVersion(dataInputStream = dataInputStream)
            Log.i(this.TAG, "doHandshake: Got protocol version $protocolVersion from server")

            // Reply the server that we accept the version we got
            dataOutputStream.write("${protocolVersion!!.versionString}\n".toByteArray(StandardCharsets.UTF_8))

            // Get the available security types from the server
            val availableSecurityTypes = getAvailableSecurityTypes(dataInputStream, protocolVersion)
            Log.i(
                this.TAG,
                "doHandshake: Got following security types from server: ${availableSecurityTypes.contentToString()}"
            )

            // Get the security type specified in the settings
            val securityType = settings.securityType

            // Check if the given security type is supported by the server
            if (!Utils.inArray(securityType.securityTypeId, availableSecurityTypes)) {
                throw VncProtocolException("The server does not except the given security Type")
            }

            // If the protocol version is 3.3 we have to send the security type id before authenticating
            if (protocolVersion != ProtocolVersion.RFB_3_3) {
                Log.d(this.TAG, "Send the Server the selected Security Type ID")
                dataOutputStream.write(securityType.securityTypeId)
            }

            // Try to authenticate using the given securityType
            Log.d(
                this.TAG,
                "Trying to authenticate using the following security type (id): ${securityType.securityTypeId}"
            )
            securityType.authenticate(protocolVersion, dataInputStream, dataOutputStream)

            // Get the result from the server
            this.getSecurityResult(protocolVersion, dataInputStream)
            Log.d(this.TAG, "Authentication successful!")

            onInitializedListener()

        }

    }

    /**
     * Reads the Protocol Version from the Input Stream and returns a ProtocolVersion Object
     */
    private fun getProtocolVersion(dataInputStream: ExtendedDataInputStream): ProtocolVersion? {
        // Create the Byte Array Buffer
        val protocolVersionBuffer = ByteArray(12)
        // Read the Bytes from the Input Stream
        dataInputStream.readFully(protocolVersionBuffer)

        // Convert the ByteArray to a String
        val protocolVersion = String(protocolVersionBuffer, StandardCharsets.UTF_8).replace("\n", "")
        Log.d(this.TAG, "getProtocolVersion: Received Protocol Version $protocolVersion")

        // Return the ProtocolVersion Object
        return ProtocolVersion.getVersionFromString(protocolVersion)
    }

    /**
     * Returns an integer array of the available security types
     */
    private fun getAvailableSecurityTypes(
        dataInputStream: ExtendedDataInputStream,
        protocolVersion: ProtocolVersion
    ): IntArray {
        // Protocol version 3.3 uses only None and VncAuthentication as Authentication
        if (protocolVersion == ProtocolVersion.RFB_3_3) {
            // Read the securityType from the input stream
            val securityType = dataInputStream.readInt()

            // If the securityType is 0 there was a problem getting it
            if (securityType == 0) {
                throw VncProtocolException("Cannot get security type of RFB protocol version 3.3")
            }

            // Return the available security type
            return intArrayOf(securityType)
        }

        // Get the number of security types available
        val securityTypeCount = dataInputStream.readUnsignedByte()

        // If there are no securityTypes available read the reason and throw an exception
        if (securityTypeCount == 0) {
            throw VncProtocolException(
                "Connect get security types from server Reason: ${getReasonMessage(
                    dataInputStream
                )}"
            )
        }

        // Get the securityTypes and return them as a byteArray
        val securityTypes = IntArray(securityTypeCount)
        dataInputStream.readFullyUnsignedBytes(securityTypes)

        return securityTypes
    }

    /**
     * Reads the reason Message of there was an error while getting the security types
     */
    private fun getReasonMessage(dataInputStream: ExtendedDataInputStream): String {
        // Get the length of the reason
        val reasonLength = dataInputStream.readInt()

        // If the reason length is less than zero there was probably a overflow
        if (reasonLength < 0) {
            throw VncProtocolException("The reason length exceeded the maximum integer value")
        }

        // Read the reasonBytes from the input stream
        val reasonBytes = ByteArray(reasonLength)
        // TODO Bytes lesen?!

        return String(reasonBytes, StandardCharsets.US_ASCII)
    }

    /**
     * Reads the security result from the input stream and throws an exception if it was not successful
     */
    private fun getSecurityResult(protocolVersion: ProtocolVersion, dataInputStream: ExtendedDataInputStream) {
        // Read the result
        val result = dataInputStream.readInt()

        // If the result is not 0 there was a problem authenticating
        if (result != 0) {
            // If the protocol version is 3.8 the server sends a reason
            if (protocolVersion == ProtocolVersion.RFB_3_8) {
                throw VncProtocolException("Failed to authenticate: ${getReasonMessage(dataInputStream)}")
            }

            // If the protocol version is less than 3.8 we get no reason
            throw VncProtocolException("Failed to authenticate")
        }
    }
}















