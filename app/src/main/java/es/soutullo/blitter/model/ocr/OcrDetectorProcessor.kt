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

class OcrDetectorProcessor(private val activity: OcrCaptureActivity, private val overlay: GraphicOverlay<OcrGraphic>) : Detector.Processor<TextBlock> {
    private data class ProductsPrices(val prices: List<Double>, val taxes: Double)
    private val countedSamples = mutableMapOf<ProductsPrices, Int>()

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
                this.findProductsPrices(items, pricesBlock)?.let { productsPrices ->
                    val sampleCount = this.countedSamples.getOrDefault(productsPrices, 0)

                    if(sampleCount >= 2) {
                        val bill = this.createBill(productsPrices)
                        this.activity.billRecognized(bill)
                    } else {
                        this.countedSamples.put(productsPrices, sampleCount + 1)
                    }
                }
            }
        }
    }

    private fun findPricesBlock(items: SparseArray<TextBlock>, minX: Int, maxX: Int): List<Double>? {
        val rightColumnThreshold = (minX + 9 * maxX) / 10
        val numericComponentBlocks = mutableListOf<List<Double>>()
        val rightColumnComponents = (0 until items.size()).map { items.valueAt(it) }
                .filter { it.boundingBox.right > rightColumnThreshold }.flatMap { it.components }
                .sortedBy { it.boundingBox.top }

        var currentBlock = mutableListOf<Double>()
        rightColumnComponents.forEach {
            val value = it.value.trimDecimalSeparator().trim().split(Regex(" +")).last()

            if(value.removeNumeric().length <= 3 && value.toPriceOrNull() != null && !it.value.contains(Regex("[#%&/():]"))
                    && (value.contains(Regex("[.,]")) || value.preserveNumeric().length < 4)) {
                currentBlock.add(value.toPrice())
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

    private fun findProductsPrices(items: SparseArray<TextBlock>, pricesBlock: List<Double>): ProductsPrices? {
        val totalExpectedPrice = pricesBlock.sum()
        val totalReadPrice = this.findTotalPrice(items, totalExpectedPrice)

        for(topIndex in pricesBlock.size downTo 1) {
            val validPrices = pricesBlock.subList(0, topIndex)
            val sum = validPrices.sum()

            for(taxesIndex in 0..3) {
                val possibleTaxes = pricesBlock.getOrNull(pricesBlock.size - taxesIndex) ?: 0.0

                if(totalReadPrice!= null && abs(sum + possibleTaxes - totalReadPrice) <= 1e-6) {
                    return ProductsPrices(validPrices, possibleTaxes)
                }
            }
        }

        return null
    }

    private fun createBill(productsPrices: ProductsPrices): Bill {
        val bill = Bill(null, this.activity.getString(R.string.bill_uncompleted_default_name), Date(), EBillSource.CAMERA, EBillStatus.UNCONFIRMED)

        productsPrices.prices.forEachIndexed { index, price ->
            bill.addLine(BillLine(null, bill, index, "Product " + index, price.toFloat()))
        }

        if(productsPrices.taxes > 0) {
            bill.addLine(BillLine(null, bill, productsPrices.prices.size, "Taxes", productsPrices.taxes.toFloat()))
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
}