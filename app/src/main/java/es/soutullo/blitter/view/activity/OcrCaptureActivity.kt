package es.soutullo.blitter.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.text.TextRecognizer
import es.soutullo.blitter.R
import es.soutullo.blitter.model.ocr.OcrDetectorProcessor

class OcrCaptureActivity : AppCompatActivity() {
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_ocr_capture)

        // TODO check for camera permissions
        this.createCameraSource()
    }

    override fun onResume() {
        super.onResume()
        this.startCameraSource()
    }

    private fun createCameraSource() {
        val textRecognizer = TextRecognizer.Builder(this.applicationContext).build()
        textRecognizer.setProcessor(OcrDetectorProcessor())

        if(!textRecognizer.isOperational) {
            val lowStorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = this.registerReceiver(null, lowStorageFilter) != null

            if(hasLowStorage) {
                Toast.makeText(this, this.getString(R.string.toast_ocr_low_storage), Toast.LENGTH_LONG).show()
            }
        }

        this.cameraSource = CameraSource.Builder(this.applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK).setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(30.0f).setAutoFocusEnabled(true).build()
    }

    @SuppressLint("MissingPermission")
    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.applicationContext)

        if(code != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, code, 0).show()
        }

        this.cameraSource.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO release
    }
}
