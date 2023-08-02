package rpetrov.test.mediaprojectionleakdemo

import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.startButton).setOnClickListener {
            val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_CODE
            )
        }
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopService(Intent(this, ScreenCaptureService::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val intent = Intent(this, ScreenCaptureService::class.java)
                intent.putExtra("RESULT_CODE", resultCode)
                intent.putExtra("DATA", data)
                startService(intent)
            }
        }
    }
}