package es.soutullo.blitter.model.ocr

import android.util.SparseArray
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.Text
import com.google.android.gms.vision.text.TextBlock
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.model.vo.bill.EBillSource
import es.soutullo.blitter.model.vo.bill.EBillStatus
import es.soutullo.blitter.view.activity.OcrCaptureActivity
import es.soutullo.blitter.view.component.GraphicOverlay
import es.soutullo.blitter.view.component.OcrGraphic
import java.util.*
import kotlin.math.abs

// TODO maybe take tax name from the receipt itself
// TODO http://i.imgur.com/gyGY1TP.jpg - zero price and one product not on the very left
// TODO find the total by searching the "total" word in different languages
// TODO what if "total" text is above the actual total amount
// TODO maybe add button on bill summary to edit taxes
// TODO add condition for lines. Price must be greater than zero
class OcrDetectorProcessor(private val activity: OcrCaptureActivity, private val overlay: GraphicOverlay<OcrGraphic>) : Detector.Processor<TextBlock> {
    private val countedSamples = mutableMapOf<RecognizedData, Int>()
    private var successfulScans = 0

    override fun receiveDetections(detections: Detector.Detections<TextBlock>?) {
        val items = detections?.detectedItems
        this.drawOverlay(items)

        if (items != null && items.size() > 0) {
            val bounds = (0 until items.size()).map { items.valueAt(it).boundingBox }
            val minX = bounds.map { it.left }.min()!!
            val maxX = bounds.map { it.right }.max()!!

            val receiptLines = this.findReceiptLines(items, minX, maxX)
            val recognizedData = this.processReceipt(receiptLines)

            this.confirmScan(recognizedData)
        }
    }

    private fun findReceiptLines(items: SparseArray<TextBlock>, minX: Int, maxX: Int): List<ReceiptLine> {
        val leftColumnThreshold = (7 * minX + 3 * maxX) / 10
        val rightColumnThreshold = (3 * minX + 7 * maxX) / 10

        val leftColumnComponents = (0 until items.size()).map { items.valueAt(it) }.flatMap { it.components }
                .filter { it.boundingBox.left < leftColumnThreshold }

        val rightColumnComponents = (0 until items.size()).map { items.valueAt(it) }.flatMap { it.components }
                .filter { it.boundingBox.right > rightColumnThreshold }.sortedBy { it.boundingBox.top }

        return rightColumnComponents.mapNotNull { it.value.findPriceOrNull()?.let { price -> Pair(it, price) }}
                .map { (it, price) -> ReceiptLine(this.findProductName(leftColumnComponents, it), price, this.findBlockHorizontalCoordinate(it)) }
    }

    private fun processReceipt(receiptLines: List<ReceiptLine>): RecognizedData {
        val totalLine = receiptLines.firstOrNull { it.name.toLowerCase().contains("total") } // TODO change this crap
        val linesBeforeTotal = receiptLines.filter { it.horizontalCoordinate < totalLine?.horizontalCoordinate ?: Double.MAX_VALUE }

        return RecognizedData(linesBeforeTotal, linesBeforeTotal.sumByDouble { it.price }, totalLine?.price, null) // TODO recognize taxes
    }

    private fun confirmScan(recognizedData: RecognizedData) {
        if(recognizedData.receiptLines.isNotEmpty()) {
            val sampleCount = this.countedSamples[recognizedData] ?: 0
            this.successfulScans++

            if((recognizedData.computedTotal == recognizedData.recognizedTotal && recognizedData.receiptLines.size > 1)
                || (sampleCount >= 2 && this.successfulScans > 3)) {

                val bill = this.createBill(recognizedData)
                this.activity.billRecognized(bill)
            } else {
                this.countedSamples[recognizedData] = sampleCount + 1
            }
        }
    }

    // TODO manage taxes
    private fun createBill(recognizedData: RecognizedData): Bill {
        val bill = Bill(null, this.activity.getString(R.string.bill_uncompleted_default_name), Date(), EBillSource.CAMERA, EBillStatus.UNCONFIRMED)

        recognizedData.receiptLines.forEachIndexed { index, product ->
            bill.addLine(BillLine(null, bill, index, product.name.take(21), product.price.toFloat()))
        }

//        if(recognizedData.taxes > 0) {
//            bill.addLine(BillLine(null, bill, recognizedData.receiptLines.size, this.activity.getString(R.string.product_name_taxes), recognizedData.taxes.toFloat()))
//        }

        return bill
    }

    private fun findProductName(leftColumnComponents: List<Text>, priceBlock: Text): String =
            leftColumnComponents.minBy { abs(this.findBlockHorizontalCoordinate(it) - this.findBlockHorizontalCoordinate(priceBlock)) }?.value ?: ""

    private fun findBlockHorizontalCoordinate(block: Text) = (block.boundingBox.top + block.boundingBox.bottom) / 2.0

    private fun drawOverlay(items: SparseArray<TextBlock>?) {
        this.overlay.clear()

        if (items != null) {
            for(i in 0 until items.size()) {
                this.overlay.add(OcrGraphic(this.overlay, items.valueAt(i)))
            }
        }
    }

    override fun release() {
        this.overlay.clear()
    }

    private fun String.removeNumeric() = this.replace(Regex("[0-9,.]"), "").trim()
    private fun String.preserveNumeric() = this.replace(Regex("[^0-9,.]"), "").trim()
    private fun String.trimDecimalSeparator() = this.replace(Regex(" *([,.]) *"), {result ->  result.groupValues[1] })
    private fun String.toPrice() = this.toPriceOrNull()!!
    private fun String.toPriceOrNull() = this.preserveNumeric().toDoubleOrNull() ?: this.replace(",", "#")
            .replace(".", ",").replace("#", ".").preserveNumeric().toDoubleOrNull()

    private fun String.findPriceOrNull() = this.trimDecimalSeparator().trim().split(Regex(" +"))
            .lastOrNull { it.toPriceOrNull() != null && it.preserveNumeric().matches(Regex("[0-9]+[.,][0-9]{2}")) && it.removeNumeric().length < 3 && !it.contains(Regex("[#%&/():]"))
                && (it.contains(Regex("[.,]")) || it.preserveNumeric().length < 4)}?.toPrice()

    private data class RecognizedData(val receiptLines: List<ReceiptLine>, val computedTotal: Double, val recognizedTotal: Double?, val taxes: Double?)

    private data class ReceiptLine(val name: String, val price: Double, val horizontalCoordinate: Double) {
        override fun equals(other: Any?): Boolean = other is ReceiptLine && other.price == this.price
        override fun hashCode(): Int = this.price.hashCode()
    }
}