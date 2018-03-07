package es.soutullo.blitter.model.ocr

import android.graphics.Rect
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
import es.soutullo.blitter.view.component.DefaultOcrGraphic
import es.soutullo.blitter.view.component.GraphicOverlay
import es.soutullo.blitter.view.component.OcrGraphic
import java.util.*
import kotlin.math.abs

/**
 * Processes the received image from the camera and applies OCR techniques to get the receipt data
 * @param activity The activity where the camera preview is being displayed
 * @param overlay A reference to the graphic overlay used to draw lines over the preview
 */
class OcrDetectorProcessor(private val activity: OcrCaptureActivity, private val overlay: GraphicOverlay<OcrGraphic>) : Detector.Processor<TextBlock> {
    companion object {
        private val TOTAL_KEYWORDS = arrayOf("合計", "합계", "共计", "कुल", "итог", "total", "amount", "summe", "jumlah", "toplam", "suma", "totale")
        private val TAX_KEYWORDS = arrayOf("tax", "impuesto")
    }

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

    /**
     * Finds the valid lines of the receipt. At this point, any line containing a text on the left half
     * and a price on the right half is considered a valid line.
     * @param items An array containing all the text blocks detected by the OCR
     * @param minX The lowest horizontal coordinate were text was found by the OCR
     * @param maxX The highest horizontal coordinate were text was found by the OCR
     * @return A list containing all the valid lines and their attributes
     */
    private fun findReceiptLines(items: SparseArray<TextBlock>, minX: Int, maxX: Int): List<ReceiptLine> {
        val leftColumnThreshold = (7 * minX + 3 * maxX) / 10
        val rightColumnThreshold = (3 * minX + 7 * maxX) / 10

        val leftColumnComponents = (0 until items.size()).map { items.valueAt(it) }.flatMap { it.components }
                .filter { it.boundingBox.left < leftColumnThreshold || this.isTotalText(it.value) || this.isTaxText(it.value) }

        val rightColumnComponents = (0 until items.size()).map { items.valueAt(it) }.flatMap { it.components }
                .filter { it.boundingBox.right > rightColumnThreshold }.sortedBy { it.boundingBox.top }

        return rightColumnComponents.mapNotNull { it.value.findPriceOrNull()?.let { price -> Pair(it, price) }}
                .filter { (component, price) -> price > 0 && !rightColumnComponents.any { component.boundingBox.right < it.boundingBox.left } }
                .map { (it, price) -> ReceiptLine(this.findProductName(leftColumnComponents, it), price, this.findBlockVerticalCoordinate(it)) }
    }

    /**
     * Processes the receipt and determines all the products, the taxes and the total given the
     * all the valid lines determined by the [findReceiptLines] method.
     * @param receiptLines The valid lines given by the [findReceiptLines] method
     * @return An object containing all the recognized data of the receipt (products-prices, taxes, total, etc.)
     */
    private fun processReceipt(receiptLines: List<ReceiptLine>): RecognizedData {
        var totalLine = receiptLines.firstOrNull { this.isTotalText(it.name) }
        val taxLines = receiptLines.filter { this.isTaxText(it.name) }
        val linesBeforeTotal = receiptLines.filter { it.verticalCoordinate < totalLine?.verticalCoordinate ?: Double.MAX_VALUE }
        val computedTotal = linesBeforeTotal.sumByDouble { it.price }

        var taxesValue = 0.0

        if(taxLines.isNotEmpty() && totalLine != null && receiptLines.any { it.price > (totalLine?.price ?: 0.0) }) {
            taxesValue = taxLines.sumByDouble { it.price }
            totalLine = receiptLines.firstOrNull { this.isTotalText(it.name) && it.price == computedTotal + taxesValue } ?: totalLine
        }

        return RecognizedData(linesBeforeTotal, computedTotal, totalLine?.price, taxesValue)
    }

    /**
     * Gets called after the [processReceipt] method, which had already processed and encapsulated the
     * data recognized in the current frame. Saves the data to make comparisons with future and previous frames,
     * increasing the global precision of the receipt scanning. When the bill is considered to be properly scanned, this
     * method also notifies the UI that the scanning is finished, and passes the gathered data.
     * @param recognizedData The recognized data in the current frame, given by the [processReceipt] method
     */
    private fun confirmScan(recognizedData: RecognizedData) {
        if(recognizedData.receiptLines.isNotEmpty()) {
            val sampleCount = this.countedSamples[recognizedData] ?: 0
            val totalMatching = recognizedData.computedTotal + recognizedData.tax == recognizedData.recognizedTotal
            this.successfulScans++

            if((totalMatching && recognizedData.receiptLines.size > 1) || (sampleCount >= 2 && this.successfulScans > 3)) {
                val bill = this.createBill(recognizedData)
                this.activity.billRecognized(bill)
            } else {
                this.countedSamples[recognizedData] = sampleCount + 1
            }

            this.activity.onReceiptPresenceChanged(true)
        }
    }

    /**
     * Converts the recognized data object to a bill object, so it can be used by the other modules of the app
     * @param recognizedData The recognized data to be converted
     * @return The converted bill object
     */
    private fun createBill(recognizedData: RecognizedData): Bill {
        val bill = Bill(null, this.activity.getString(R.string.bill_uncompleted_default_name), Date(), EBillSource.CAMERA, EBillStatus.UNCONFIRMED, recognizedData.tax)

        recognizedData.receiptLines.forEachIndexed { index, product ->
            bill.addLine(BillLine(null, bill, index, product.name.take(21), product.price))
        }

        return bill
    }

    /**
     * Draws the required boxes and texts over the preview
     * @param items The text blocks recognized by the OCR
     */
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

    /**
     * Retrieves the name of a product while scanning the receipt image, given all the products names text blocks
     * and the required product price block.
     * @param leftColumnComponents All the products names text blocks
     * @param priceBlock The required product price block
     * @return The product name as a String
     */
    private fun findProductName(leftColumnComponents: List<Text>, priceBlock: Text): String =
            leftColumnComponents.minBy { abs(this.findBlockVerticalCoordinate(it) - this.findBlockVerticalCoordinate(priceBlock)) }?.value ?: ""

    /**
     * Calculates the text block vertical coordinate as an average of the top and bottom coordinates
     * @param block The block
     * @return The vertical coordinate
     */
    private fun findBlockVerticalCoordinate(block: Text) = (block.boundingBox.top + block.boundingBox.bottom) / 2.0

    /**
     * Determines whether or not a specific text is the "total price" text of the receipt, in any
     * of the considered languages.
     * @param text The text
     * @return True if it is the "total price" text
     */
    private fun isTotalText(text: String) = TOTAL_KEYWORDS.any { text.toLowerCase().contains(it) }

    /**
     * Determines whether or not a specific text is the "tax" text of the receipt, in any
     * of the considered languages.
     * @param text The text
     * @return True if it is the "tax" text
     */
    private fun isTaxText(text: String) = TAX_KEYWORDS.any { text.toLowerCase().contains(it) }

    /** Finds any valid price contained in the String and converts it to double. If this is not possible,
     * null value is returned */
    private fun String.findPriceOrNull() = this.trimDecimalSeparator().trim().split(Regex(" +"))
            .lastOrNull { it.toPriceOrNull() != null && it.preserveNumeric().matches(Regex("-?[0-9]+[.,][0-9]{2}")) && it.removeNumeric().length < 3 && !it.contains(Regex("[#%&/():]"))
                    && (it.contains(Regex("[.,]")) || it.preserveNumeric().length < 4)}?.toPrice()

    /** Tries to convert the given String to a valid price. If this is not possible, null is returned */
    private fun String.toPriceOrNull() = this.preserveNumeric().toDoubleOrNull() ?: this.replace(",", "#")
            .replace(".", ",").replace("#", ".").preserveNumeric().toDoubleOrNull()

    /** The same as [toPriceOrNull] but null can never be returned. Instead, an exception will be thrown if the conversion
     * is not possible */
    private fun String.toPrice() = this.toPriceOrNull()!!

    /** Removes all the numeric characters from a String (numbers, periods, commas and minuses) */
    private fun String.removeNumeric() = this.replace(Regex("[0-9,.\\-]"), "").trim()

    /** Removes all the non-numeric characters from a String (removes everything except numbers, periods, commas and minuses)  */
    private fun String.preserveNumeric() = this.replace(Regex("[^0-9,.\\-]"), "").trim()

    /** Removes the surrounding spaces of a decimal separator (comma or period) given a floating number as String */
    private fun String.trimDecimalSeparator() = this.replace(Regex(" *([,.]) *"), {result ->  result.groupValues[1] })

    /**
     * Encapsulates the data recognized on a single frame
     * @param receiptLines The recognized product lines
     * @param computedTotal The total price, calculated by summing all the prices from the [receiptLines]
     * @param recognizedTotal The total price explicitly recognized on the receipt, or null if this was not found
     * @param tax The tax explicitly recognized on the receipt, or zero if this was not found
     */
    private data class RecognizedData(val receiptLines: List<ReceiptLine>, val computedTotal: Double, val recognizedTotal: Double?, val tax: Double)

    /**
     * Encapsulates each product line of the recognized data
     * @param name The recognized name of the product
     * @param price The recognized price of the product
     * @param verticalCoordinate The vertical coordinate where the product was found on the receipt
     */
    private data class ReceiptLine(val name: String, val price: Double, val verticalCoordinate: Double) {
        override fun equals(other: Any?): Boolean = other is ReceiptLine && other.price == this.price
        override fun hashCode(): Int = this.price.hashCode()
    }
}