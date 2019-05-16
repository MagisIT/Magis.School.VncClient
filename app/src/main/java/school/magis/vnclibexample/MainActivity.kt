package school.magis.vnclibexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.RfbSettings
import de.magisit.vncclient.protocol.handshake.authentication.SecurityTypeVncAuthentication

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val client = RfbClient(
            RfbSettings(
                host = "kst-vechta.de",
                port = 11006,
                securityType = SecurityTypeVncAuthentication("rbySHcP2"),
                leaveOtherClientsConnected = true
            )
        )
        client.connect()

    }
}
