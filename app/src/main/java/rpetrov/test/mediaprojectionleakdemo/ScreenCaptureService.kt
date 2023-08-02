package rpetrov.test.mediaprojectionleakdemo

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import java.io.FileOutputStream
import java.io.IOException

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var mHandler: Handler? = null


    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        object : Thread() {
            override fun run() {
                Looper.prepare()
                mHandler = Handler()
                Looper.loop()
                Log.e("ScreenCaptureService", "looper stopped" )
            }
        }.start()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val notification: Pair<Int, Notification> = NotificationUtils.getNotification(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                notification.first, notification.second, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(notification.first, notification.second)
        }


        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        imageReader = ImageReader.newInstance(640, 480, PixelFormat.RGBA_8888, 2)
        imageReader!!.setOnImageAvailableListener({
            try {
                it.acquireLatestImage().use { image ->
                    if (image != null) {
                        Log.e("ScreenCaptureService", "captured image: " + image)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mHandler)

        mediaProjection = mediaProjectionManager.getMediaProjection(
            intent.getIntExtra("RESULT_CODE", 0),
            intent.getParcelableExtra<Intent>("DATA")!!
        )


        virtualDisplay = mediaProjection!!.createVirtualDisplay(
            "ScreenCapture",
            640,
            480,
            440,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface,
            null, null
        )


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaProjection?.stop()
        virtualDisplay?.release()
        imageReader?.setOnImageAvailableListener(null, null)
        mediaProjection = null
        virtualDisplay = null
        imageReader = null
        mHandler?.looper?.quitSafely()
        mHandler =  null
    }
}