package de.magisit.vncclient.protocol.handshake

/**
 * Enum to represent the available protocol versions
 */
enum class ProtocolVersion constructor(val versionString: String) {
    RFB_3_3("RFB 003.003"),
    RFB_3_7("RFB 003.007"),
    RFB_3_8("RFB 003.008");

    /**
     * Returns a protocol version from a version string if known, otherwise returns null
     */
    companion object {
        fun getVersionFromString(versionString: String): ProtocolVersion? {
            values().forEach {
                if (it.versionString == versionString) return it
            }
            return null
        }
    }
}
