package es.soutullo.blitter.view.util

import java.util.*

/**
 *
 */
object BlitterUtils {
    fun getCurrencySymbol(): String = Currency.getInstance(Locale.getDefault()).symbol
}