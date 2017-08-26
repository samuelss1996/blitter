package es.soutullo.blitter.view.util

import android.content.Context
import es.soutullo.blitter.R
import java.util.*

/**
 *
 */
object BlitterUtils {
    fun getCurrencySymbol(): String = Currency.getInstance(Locale.getDefault()).symbol

    fun getBeatifulPrice(context: Context, priceWithoutTip: Float, tipPercent: Float): String {
        return if(tipPercent == 0f) context.getString(R.string.bill_beautiful_price_without_tip,
                context.getString(R.string.generic_item_price, priceWithoutTip, BlitterUtils.getCurrencySymbol()))
        else context.getString(R.string.bill_beautiful_price_with_tip,
                context.getString(R.string.generic_item_price, priceWithoutTip, BlitterUtils.getCurrencySymbol()),
                context.getString(R.string.generic_item_price, priceWithoutTip * tipPercent, BlitterUtils.getCurrencySymbol()))
    }
}