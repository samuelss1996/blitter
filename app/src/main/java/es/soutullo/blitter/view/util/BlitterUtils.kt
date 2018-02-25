package es.soutullo.blitter.view.util

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import android.text.format.DateFormat
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import es.soutullo.blitter.R
import java.text.NumberFormat
import java.util.*

/** Provides some useful methods relative to the presentation logic */
object BlitterUtils {
    private val BILL_FONT_PATH = "fonts/fake_receipt.ttf"

    /** @return The currency symbol based on the device's locale (e.g. €, $) */
    fun getCurrencySymbol(): String = Currency.getInstance(Locale.getDefault()).symbol

    /**
     * Converts a floating number to a string representing a price, with its symbol, based on the device's locale
     * @param price The price to convert
     * @return The converted price as string
     */
    fun getPriceAsString(price: Double): String = NumberFormat.getCurrencyInstance().format(price)

    /**
     * Converts a floating number to a string representing a price, without its symbol, based on the device's locale
     * @param price The price to convert
     * @return The converted price as string
     */
    fun getEditablePriceAsString(price: Double): String = NumberFormat.getCurrencyInstance().parse(getPriceAsString(price)).toString()

    /**
     * Returns a date as a long string, working with internationalization
     * @param context The Android context
     * @param date The date
     * @return The date as string
     */
    fun getBeautifulDate(context: Context, date: Date): String = DateFormat.getLongDateFormat(context).format(date)

    /**
     * Converts a price and a tip percentage to a beautiful human readable string that shows the base price and the tip
     * @param context The Android context
     * @param priceWithoutTip The price without the tip
     * @param tipPercent The tip percentage
     * @return The human readable string
     */
    fun getPriceAsStringWithTip(context: Context, priceWithoutTip: Double, tipPercent: Double): String {
        val tipPrice = priceWithoutTip * tipPercent

        if(tipPercent == 0.0) {
            return context.getString(R.string.bill_beautiful_price_without_tip, getPriceAsString(priceWithoutTip))
        } else {
            return context.getString(R.string.bill_beautiful_price_with_tip, getPriceAsString(priceWithoutTip + tipPrice),
                    getPriceAsString(priceWithoutTip), getPriceAsString(priceWithoutTip * tipPercent))
        }
    }

    /**
     * Changes the font to give a bill-like look and feel to all the children text views of a given view group
     * @param root The view group whose children font will be changed
     * @param assets The assets manager
     */
    fun applyBillFontToChildren(root: ViewGroup, assets: AssetManager) {
        (0 until root.childCount).map { root.getChildAt(it) }.filter { it !is Button }.filterIsInstance<TextView>().forEach { textView ->
            textView.typeface = Typeface.createFromAsset(assets, BILL_FONT_PATH)
        }
    }
}