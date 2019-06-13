package school.magis.vnclibexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.RfbSettings
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView = main_image_view


        val client = RfbClient(
            RfbSettings()
        ) {
            runOnUiThread {
                imageView.setImageBitmap(it)
            }
        }
        client.connect()


    }
}
