package es.soutullo.blitter.model.ocr

import android.util.Log
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock

class OcrDetectorProcessor : Detector.Processor<TextBlock> {

    override fun receiveDetections(detections: Detector.Detections<TextBlock>?) {
        detections?.let {
            val items = it.detectedItems

            for(i in 0 until items.size()) {
                Log.i("TEXT", items.valueAt(i).value)
            }
        }
    }

    override fun release() {
        // TODO
    }
}