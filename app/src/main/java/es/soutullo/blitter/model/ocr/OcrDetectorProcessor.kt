package es.soutullo.blitter.model.ocr

import android.util.Log
import android.util.SparseArray
import com.google.android.gms.vision.Detector
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
class OcrDetectorProcessor(private val activity: OcrCaptureActivity, private val overlay: GraphicOverlay<OcrGraphic>) : Detector.Processor<TextBlock> {
    private val countedSamples = mutableMapOf<RecognizedData, Int>()

    override fun receiveDetections(detections: Detector.Detections<TextBlock>?) {
        val items = detections?.detectedItems
        this.drawOverlay(items)

        if (items != null && items.size() > 0) {
            val bounds = (0 until items.size()).map { items.valueAt(it).boundingBox }
            val minX = bounds.map { it.left }.min()!!
            val maxX = bounds.map { it.right }.max()!!
            val pricesBlock = this.findPricesBlock(items, minX, maxX)

            (0 until items.size()).map { items.valueAt(it) }.flatMap { it.components }.forEach { Log.i("COMP", it.value) }

            if(pricesBlock != null) {
                this.findProducts(items, pricesBlock)?.let { recognizedData ->
                    val sampleCount = this.countedSamples.getOrDefault(recognizedData, 0)

                    if(sampleCount >= 1) {
                        val bill = this.createBill(recognizedData)
                        this.activity.billRecognized(bill)
                    } else {
                        this.countedSamples.put(recognizedData, sampleCount + 1)
                    }
                }
            }
        }
    }

    private fun findPricesBlock(items: SparseArray<TextBlock>, minX: Int, maxX: Int): List<Product>? {
        val leftColumnThreshold = (9 * minX + maxX) / 10
        val rightColumnThreshold = (minX + 9 * maxX) / 10
        val numericComponentBlocks = mutableListOf<List<Product>>()

        val leftColumnComponents = (0 until items.size()).map { items.valueAt(it) }.flatMap { it.components }
                .filter { it.boundingBox.left < leftColumnThreshold }

        val rightColumnComponents = (0 until items.size()).map { items.valueAt(it) }.flatMap { it.components }
                .filter { it.boundingBox.right > rightColumnThreshold }.sortedBy { it.boundingBox.top }


        var currentBlock = mutableListOf<Product>()
        rightColumnComponents.forEach {
            val value = it.value.trimDecimalSeparator().trim().split(Regex(" +")).last()

            if(value.removeNumeric().length <= 3 && value.toPriceOrNull() != null && !it.value.contains(Regex("[#%&/():]"))
                    && (value.contains(Regex("[.,]")) || value.preserveNumeric().length < 4)) {
                val name = leftColumnComponents.minBy { nameBlock -> abs(it.boundingBox.top - nameBlock.boundingBox.top) }
                        ?.value?.removeNumeric() ?: ""

                currentBlock.add(Product(name, value.toPrice()))
            } else {
                if(currentBlock.isNotEmpty()) {
                    numericComponentBlocks.add(currentBlock)
                    currentBlock = mutableListOf()
                }
            }
        }

        if(currentBlock.isNotEmpty()) {
            numericComponentBlocks.add(currentBlock)
        }

        return numericComponentBlocks.maxBy { it.size }
    }

    private fun findProducts(items: SparseArray<TextBlock>, pricesBlock: List<Product>): RecognizedData? {
        val totalExpectedPrice = pricesBlock.sumByDouble { it.price }
        val totalReadPrice = this.findTotalPrice(items, totalExpectedPrice)

        for(topIndex in pricesBlock.size downTo 1) {
            val validProducts = pricesBlock.subList(0, topIndex)
            val validProductsSum = validProducts.sumByDouble { it.price }

            for(taxesIndex in 0..5) {
                val possibleTaxes = pricesBlock.map { it.price }.getOrNull(pricesBlock.size - taxesIndex) ?: 0.0

                if(totalReadPrice!= null && abs(validProductsSum + possibleTaxes - totalReadPrice) <= 1e-6) {
                    return RecognizedData(validProducts, possibleTaxes)
                }
            }
        }

        return null
    }

    private fun createBill(recognizedData: RecognizedData): Bill {
        val bill = Bill(null, this.activity.getString(R.string.bill_uncompleted_default_name), Date(), EBillSource.CAMERA, EBillStatus.UNCONFIRMED)

        recognizedData.products.forEachIndexed { index, product ->
            bill.addLine(BillLine(null, bill, index, product.name, product.price.toFloat()))
        }

        if(recognizedData.taxes > 0) {
            bill.addLine(BillLine(null, bill, recognizedData.products.size, this.activity.getString(R.string.product_name_taxes), recognizedData.taxes.toFloat()))
        }

        return bill
    }

    private fun findTotalPrice(items: SparseArray<TextBlock>, totalExpectedPrice: Double): Double? {
        return (0 until items.size()).map { items.valueAt(it) }.flatMap { it.components }.map { it.value.trimDecimalSeparator() }
                .filter { it.removeNumeric().length <= 3 }.map { it.toPriceOrNull() ?: -1.0 }
                .filter { it < 4 * totalExpectedPrice }.max()
    }

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

    private fun String.removeNumeric() = this.replace(Regex("[0-9,.]"), "")
    private fun String.preserveNumeric() = this.replace(Regex("[^0-9,.]"), "")
    private fun String.trimDecimalSeparator() = this.replace(Regex(" *([,.]) *"), {result ->  result.groupValues[1] })
    private fun String.toPrice() = this.toPriceOrNull()!!
    private fun String.toPriceOrNull() = this.preserveNumeric().toDoubleOrNull() ?: this.replace(",", "#")
            .replace(".", ",").replace("#", ".").preserveNumeric().toDoubleOrNull()

    private data class RecognizedData(val products: List<Product>, val taxes: Double)

    private data class Product(val name: String, val price: Double) {
        override fun equals(other: Any?): Boolean = other is Product && other.price == this.price
        override fun hashCode(): Int = this.price.hashCode()
    }
}