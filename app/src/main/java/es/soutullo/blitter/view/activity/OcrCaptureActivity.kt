package es.soutullo.blitter.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import es.soutullo.blitter.view.component.CameraSource
import com.google.android.gms.vision.text.TextRecognizer
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.ActivityOcrCaptureBinding
import es.soutullo.blitter.model.ocr.OcrDetectorProcessor
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.view.component.CameraSourcePreview
import es.soutullo.blitter.view.component.GraphicOverlay
import es.soutullo.blitter.view.component.OcrGraphic

// TODO maybe add grid to the view
class OcrCaptureActivity : AppCompatActivity() {
    private lateinit var cameraSourcePreview: CameraSourcePreview
    private lateinit var graphicOverlay: GraphicOverlay<OcrGraphic>
    private var cameraSource: CameraSource? = null
    private lateinit var binding: ActivityOcrCaptureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_ocr_capture)
        this.binding.flashEnabled = false

        this.cameraSourcePreview = this.findViewById(R.id.camera_source_preview)
        this.graphicOverlay = this.findViewById(R.id.graphic_overlay)

        this.createCameraSource()
    }

    override fun onResume() {
        super.onResume()
        this.startCameraSource()
    }

    fun switchFlash(view: View) {
        this.cameraSource?.let {
            this.binding.flashEnabled = !this.binding.flashEnabled
            it.flashMode = if(this.binding.flashEnabled) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF

            this.binding.notifyChange()
        }
    }

    fun billRecognized(bill: Bill) {
        this.startActivity(Intent(this, BillSummaryActivity::class.java).putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, bill))
        this.finish()
    }

    private fun createCameraSource() {
        val textRecognizer = TextRecognizer.Builder(this.applicationContext).build()
        textRecognizer.setProcessor(OcrDetectorProcessor(this, this.graphicOverlay))

        if(!textRecognizer.isOperational) {
            val lowStorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = this.registerReceiver(null, lowStorageFilter) != null

            if(hasLowStorage) {
                Toast.makeText(this, this.getString(R.string.toast_ocr_low_storage), Toast.LENGTH_LONG).show()
            }
        }

        this.cameraSource = CameraSource.Builder(this.applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK).setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(30.0f).setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE).build()
    }

    @SuppressLint("MissingPermission")
    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.applicationContext)

        if(code != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, code, 0).show()
        }

        this.cameraSource?.let {
            try {
                this.cameraSourcePreview.start(it, this.graphicOverlay)
            } catch (e: Exception) {
                this.cameraSource = null
            }
        }
    }

    override fun onPause() {
        super.onPause()
        this.cameraSourcePreview.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.cameraSourcePreview.release()
    }

    companion object {
        @JvmStatic @BindingAdapter("app:srcCompat")
        fun setImageDrawable(imageButton: ImageButton, drawable: Drawable) {
            imageButton.setImageDrawable(drawable)
        }
    }
}
