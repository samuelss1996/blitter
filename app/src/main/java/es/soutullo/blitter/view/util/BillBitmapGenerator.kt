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

/**
 * Generates the shareable receipt image
 * @param context The Android context
 * @param bill The bill object to be converted to bitmap
 * @param includeDetails Indicates whether or not the detailed breakdown for each person should be included
 */
class BillBitmapGenerator(private val context: Context, private val bill: Bill, private val includeDetails: Boolean) {
    companion object{
        const val RECEIPT_WIDTH = 428
        const val RECEIPT_PADDING = 16f
        const val SEPARATORS_WIDTH = 36
        const val HEADER_HEIGHT = 140
        const val LINE_HEIGHT = 16
        const val STANDARD_TOTAL_PRICE_HEIGHT = 74
        const val TAXED_TOTAL_PRICE_HEIGHT = 106
        const val TIP_PRICE_HEIGHT = 16
        const val BREAKDOWN_HEADER_HEIGHT = 60
        const val DETAILED_BREAKDOWN_LINE_HEIGHT = 14
    }

    private var lastTextLinePosition = 0f
    private lateinit var canvas: Canvas
    private lateinit var typeface: Typeface

    /**
     * Generates the receipt image and returns an URI, so it can be shared
     * @return The URI
     */
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

    /**
     * Prepares de file system to write the temporary image
     * @param cachePath The path of the folder where te temporary image should be stored
     */
    private fun prepareOutputStream(cachePath: File): FileOutputStream {
        cachePath.mkdirs()
        return FileOutputStream("$cachePath/image.png")
    }

    /** Draws the receipt over the bitmap */
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

    /** Draws the white background of the receipt */
    private fun drawBackground() {
        val backgroundPaint = Paint()

        backgroundPaint.color = Color.WHITE
        this.canvas.drawRect(0f, 0f, RECEIPT_WIDTH.toFloat(), this.receiptHeight().toFloat(), backgroundPaint)
    }

    /** Draws the receipt title */
    private fun drawBlitterTitle() {
        this.drawText(this.context.getString(R.string.bill_summary_header), true, android.graphics.Paint.Align.CENTER, 24f, 8f)
    }

    /** Draws a thin ASCII char separator over the receipt */
    private fun drawThinSeparator(marginTop: Float) {
        val separator = this.context.getString(R.string.bill_summary_separator_product_count).repeat(SEPARATORS_WIDTH)
        this.drawText(separator, true, Paint.Align.CENTER, marginTop = marginTop)
    }

    /** Draws the product count line */
    private fun drawProductsCount() {
        this.drawText(this.context.getString(R.string.bill_summary_total_products_text), true, Paint.Align.LEFT)
        this.drawText(this.bill.lines.size.toString(), false, Paint.Align.RIGHT)
    }

    /**
     * Draws a header over the receipt
     * @param headerText The text to be included on the header
     * @param marginTop The top margin of this header
     */
    private fun drawHeader(headerText: String, marginTop: Float) {
        val separator = this.context.getString(R.string.bill_summary_separator_products_title).repeat(SEPARATORS_WIDTH)

        this.drawText(headerText, true, Paint.Align.CENTER, 20f, marginTop)
        this.drawText(separator, true, Paint.Align.CENTER)
    }

    /** Draws the products section (products list) */
    private fun drawProducts() {
        this.bill.lines.forEach {
            this.drawText(it.name.toUpperCase(), true, Paint.Align.LEFT)
            this.drawText(BlitterUtils.getPriceAsString(it.price), false, Paint.Align.RIGHT)
        }
    }

    /** Draws a thick ASCII char separator over the receipt */
    private fun drawThickSeparator(marginTop: Float) {
        val separator = this.context.getString(R.string.bill_summary_separator_total_price).repeat(SEPARATORS_WIDTH)
        this.drawText(separator, true, Paint.Align.CENTER, marginTop = marginTop)
    }

    /** Draws the total price block over the receipt */
    private fun drawTotal() {
        if(this.bill.tax > 0) {
            this.drawText(this.context.getString(R.string.bill_summary_subtotal_text), true, Paint.Align.LEFT)
            this.drawText(BlitterUtils.getPriceAsString(this.bill.subtotal), false, Paint.Align.RIGHT)

            this.drawText(this.context.getString(R.string.bill_summary_tax_text), true, Paint.Align.LEFT)
            this.drawText(BlitterUtils.getPriceAsString(this.bill.tax), false, Paint.Align.RIGHT)
        }

        this.drawText(this.context.getString(R.string.bill_summary_total_price_text), true, Paint.Align.LEFT, 18f)
        this.drawText(BlitterUtils.getPriceAsString(this.bill.subtotal + this.bill.tax), false, Paint.Align.RIGHT, 18f)

        if(this.bill.tipPercent > 0) {
            this.drawText(this.context.getString(R.string.bill_summary_tip_value), true, Paint.Align.LEFT, 18f)
            this.drawText(BlitterUtils.getPriceAsString((this.bill.subtotal + this.bill.tax) * this.bill.tipPercent), false, Paint.Align.RIGHT, 18f)
        }
    }

    /** Draws the payment breakdown (how much should each person pay) over the receipt */
    private fun drawBreakdown() {
        this.persons().forEachIndexed { index, it ->
            this.drawText(it.name.toUpperCase(), true, Paint.Align.LEFT, marginTop = if(this.includeDetails && index > 0) 14f else 0f)
            this.drawText(BlitterUtils.getPriceAsString(it.getPayingAmountWithTip()), false, Paint.Align.RIGHT)

            if(this.includeDetails) {
                it.lines.forEach { line ->
                    val priceText = (if(line.persons.size > 1) "(1/${line.persons.size}) " else "") +
                            BlitterUtils.getPriceAsString(line.price / line.persons.size)

                    this.drawText("  ${line.name}", true, Paint.Align.LEFT, 14f)
                    this.drawText(priceText, false, Paint.Align.RIGHT, 14f)
                }

                if(it.getTipPercent() > 0) {
                    val tipValue = it.getTipPercent() * it.getPayingAmountWithoutTip()

                    this.drawText("  " + this.context.getString(R.string.bill_share_tip_line_name), true, Paint.Align.LEFT, 14f)
                    this.drawText(BlitterUtils.getPriceAsString(tipValue), false, Paint.Align.RIGHT, 14f)
                }
            }
        }
    }

    /**
     * Draws generic text over the receipt
     * @param text The text to be drawn
     * @param drawOnNewLine Starts to draw on a new line, considering the last drawn text
     * @param align The alignment of the text to be drawn
     * @param size The size of the text to be drawn
     * @param marginTop The margin top of the text block to be drawn
     */
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

    /** Calculates the receipt image height for the current bill */
    private fun receiptHeight(): Int {
        var totalPriceHeight = if(this.bill.tax > 0) TAXED_TOTAL_PRICE_HEIGHT else STANDARD_TOTAL_PRICE_HEIGHT
        val productsHeight = this.bill.lines.size * LINE_HEIGHT
        val breakdownHeight = this.persons().size * LINE_HEIGHT
        val detailedBreakdownHeight = DETAILED_BREAKDOWN_LINE_HEIGHT * when(this.includeDetails) {
            true -> this.persons().flatMap { it.lines }.size + this.persons().size - 1 + if(this.bill.tipPercent > 0) this.persons().size else 0
            false -> 0
        }

        totalPriceHeight += if(this.bill.tipPercent > 0) TIP_PRICE_HEIGHT else 0

        return HEADER_HEIGHT + productsHeight + totalPriceHeight + BREAKDOWN_HEADER_HEIGHT + breakdownHeight + detailedBreakdownHeight
    }

    /** @return The persons included in this bill's breakdown */
    private fun persons() = this.bill.lines.map { it.persons }.flatten().distinct()
}