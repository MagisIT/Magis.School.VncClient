package school.magis.vnclibexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.RfbSettings

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val client = RfbClient(RfbSettings())
        client.connect()

    }
}
