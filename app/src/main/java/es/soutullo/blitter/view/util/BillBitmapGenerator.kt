package es.soutullo.blitter.view.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.Bill
import java.io.File
import java.io.FileOutputStream

class BillBitmapGenerator(private val context: Context, private val bill: Bill) {
    companion object{
        const val RECEIPT_WIDTH = 428
        const val RECEIPT_PADDING = 16f
        const val SEPARATORS_WIDTH = 36
        const val HEADER_HEIGHT = 140
        const val LINE_HEIGHT = 16
        const val STANDARD_TOTAL_PRICE_HEIGHT = 74
        const val TAXED_TOTAL_PRICE_HEIGHT = 106
        const val BREAKDOWN_HEADER_HEIGHT = 60
    }

    private var lastTextLinePosition = 0f
    private lateinit var canvas: Canvas
    private lateinit var typeface: Typeface

    fun generateBillBitmap(): Uri {
        val cachePath = File(this.context.cacheDir, "images")
        val bitmap = Bitmap.createBitmap(RECEIPT_WIDTH, this.receiptHeight(), Bitmap.Config.ARGB_8888)
        val stream = this.prepareOutputStream(cachePath)

        this.canvas = Canvas(bitmap)
        this.typeface = Typeface.createFromAsset(this.context.assets, BlitterUtils.BILL_FONT_PATH)

        this.drawReceipt()

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()

        val newFile = File(cachePath, "image.png")
        return FileProvider.getUriForFile(this.context, "es.soutullo.blitter.fileprovider", newFile)
    }

    private fun prepareOutputStream(cachePath: File): FileOutputStream {
        cachePath.mkdirs()
        return FileOutputStream("$cachePath/image.png")
    }

    private fun drawReceipt() {
        this.drawBackground()
        this.drawBlitterTitle()

        this.drawThinSeparator(8f)
        this.drawProductsCount()
        this.drawThinSeparator(0f)

        this.drawHeader(this.context.getString(R.string.bill_summary_products_header), 8f)
        this.drawProducts()

        this.drawThickSeparator(24f)
        this.drawTotal()
        this.drawThickSeparator(0f)

        this.drawHeader(this.context.getString(R.string.bill_share_breakdown), 24f)
        this.drawBreakdown()
    }

    private fun drawBackground() {
        val backgroundPaint = Paint()

        backgroundPaint.color = Color.WHITE
        this.canvas.drawRect(0f, 0f, RECEIPT_WIDTH.toFloat(), this.receiptHeight().toFloat(), backgroundPaint)
    }

    private fun drawBlitterTitle() {
        this.drawText(this.context.getString(R.string.bill_summary_header), true, android.graphics.Paint.Align.CENTER, 24f, 8f)
    }

    private fun drawThinSeparator(marginTop: Float) {
        val separator = this.context.getString(R.string.bill_summary_separator_product_count).repeat(SEPARATORS_WIDTH)
        this.drawText(separator, true, Paint.Align.CENTER, marginTop = marginTop)
    }

    private fun drawProductsCount() {
        this.drawText(this.context.getString(R.string.bill_summary_total_products_text), true, Paint.Align.LEFT)
        this.drawText(this.bill.lines.size.toString(), false, Paint.Align.RIGHT)
    }

    private fun drawHeader(headerText: String, marginTop: Float) {
        val separator = this.context.getString(R.string.bill_summary_separator_products_title).repeat(SEPARATORS_WIDTH)

        this.drawText(headerText, true, Paint.Align.CENTER, 20f, marginTop)
        this.drawText(separator, true, Paint.Align.CENTER)
    }

    private fun drawProducts() {
        this.bill.lines.forEach {
            this.drawText(it.name.toUpperCase(), true, Paint.Align.LEFT)
            this.drawText(BlitterUtils.getPriceAsString(it.price), false, Paint.Align.RIGHT)
        }
    }

    private fun drawThickSeparator(marginTop: Float) {
        val separator = this.context.getString(R.string.bill_summary_separator_total_price).repeat(SEPARATORS_WIDTH)
        this.drawText(separator, true, Paint.Align.CENTER, marginTop = marginTop)
    }

    private fun drawTotal() {
        if(this.bill.tax > 0) {
            this.drawText(this.context.getString(R.string.bill_summary_subtotal_text), true, Paint.Align.LEFT)
            this.drawText(BlitterUtils.getPriceAsString(this.bill.subtotal), false, Paint.Align.RIGHT)

            this.drawText(this.context.getString(R.string.bill_summary_tax_text), true, Paint.Align.LEFT)
            this.drawText(BlitterUtils.getPriceAsString(this.bill.tax), false, Paint.Align.RIGHT)
        }

        this.drawText(this.context.getString(R.string.bill_summary_total_price_text), true, Paint.Align.LEFT, 18f)
        this.drawText(BlitterUtils.getPriceAsString(this.bill.subtotal + this.bill.tax), false, Paint.Align.RIGHT, 18f)
    }

    private fun drawBreakdown() {
        this.persons().forEach {
            this.drawText(it.name.toUpperCase(), true, Paint.Align.LEFT)
            this.drawText(BlitterUtils.getPriceAsString(it.getPayingAmountWithTip()), false, Paint.Align.RIGHT)
        }
    }

    private fun drawText(text: String, drawOnNewLine: Boolean, align: Paint.Align, size: Float = 16f, marginTop: Float = 0f) {
        this.lastTextLinePosition += if(drawOnNewLine) size + marginTop else marginTop
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        val horizontalPosition = when(align) {
            Paint.Align.LEFT -> RECEIPT_PADDING
            Paint.Align.CENTER ->  RECEIPT_WIDTH / 2f
            Paint.Align.RIGHT -> RECEIPT_WIDTH - RECEIPT_PADDING
        }

        textPaint.typeface = this.typeface
        textPaint.color = ContextCompat.getColor(this.context, R.color.color_bill_summary_text)
        textPaint.textAlign = align
        textPaint.textSize = size

        this.canvas.drawText(text, horizontalPosition, this.lastTextLinePosition, textPaint)
    }

    private fun receiptHeight(): Int {
        val totalPriceHeight = if(this.bill.tax > 0) TAXED_TOTAL_PRICE_HEIGHT else STANDARD_TOTAL_PRICE_HEIGHT
        val productsHeight = this.bill.lines.size * LINE_HEIGHT
        val breakdownHeight = this.persons().size * LINE_HEIGHT

        return HEADER_HEIGHT + productsHeight + totalPriceHeight + BREAKDOWN_HEADER_HEIGHT + breakdownHeight
    }

    private fun persons() = this.bill.lines.map { it.persons }.flatten().distinct()
}