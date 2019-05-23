package de.magisit.vncclient.protocol.handshake.authentication

import android.annotation.SuppressLint
import android.util.Log
import de.magisit.vncclient.protocol.handshake.ProtocolVersion
import de.magisit.vncclient.utils.ExtendedDataInputStream
import de.magisit.vncclient.utils.ExtendedDataOutputStream
import de.magisit.vncclient.utils.Utils
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * VncAuthentification security type
 */
class SecurityTypeVncAuthentication(private var password: String) : SecurityType(2) {

    // TAG for logging
    private val TAG = "STypeVncAuthentication"

    // Password Bytes
    private var passwordBytes = ByteArray(8)
    private var reversedPassword = ByteArray(8)

    /**
     * Initialisation method for setting the password and filling the array
     */
    init {
        // Truncate the password if it is longer than 8 digits
        if (password.length > 8) {
            Log.w(this.TAG, "Constructor: The given password is too long and will be truncated")
            this.password = this.password.substring(0, 7)
        }

        // Fill the password byte array with zeros
        this.passwordBytes.fill(0.toByte())

        // Copy the password into the array
        System.arraycopy(password.toByteArray(), 0, this.passwordBytes, 0, password.length)

        // Store the password reversed in the array
        for (i in passwordBytes.indices) {
            this.reversedPassword[i] = Utils.reverseBitsByte(passwordBytes[i])
        }

    }

    /**
     * Authenticates the User with the specific SecurityMethod
     */
    override fun authenticate(
            protocolVersion: ProtocolVersion,
            dataInputStream: ExtendedDataInputStream,
            dataOutputStream: ExtendedDataOutputStream
    ) {
        // Read the server challenge
        val serverChallenge = ByteArray(16)
        dataInputStream.readFully(serverChallenge)

        // Encrypt the challenge with DES
        val encryptedChallenge = this.encrypt(serverChallenge, reversedPassword)

        dataOutputStream.write(encryptedChallenge)
    }

    /**
     * Encrypts the password using DES and a server challenge
     */
    @SuppressLint("GetInstance")
    private fun encrypt(serverChallenge: ByteArray, passwordBytes: ByteArray): ByteArray {

        // Key for DES
        val key = SecretKeySpec(passwordBytes, "DES")
        // Cipher to encrypt the password
        val cipher = Cipher.getInstance("DES/ECB/NoPadding")

        // Initialize the cipher
        cipher.init(Cipher.ENCRYPT_MODE, key)

        // Return the encrypted password
        return cipher.doFinal(serverChallenge)
    }

}