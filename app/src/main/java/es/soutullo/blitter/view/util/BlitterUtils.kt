package es.soutullo.blitter.view.util

import android.graphics.drawable.Drawable
import java.util.*

/**
 *
 */
object BlitterUtils {

    /**
     * @param name
     * @return
     */
    fun generateUserProfilePic(name: String): Drawable {
        TODO("not implemented")
    }

    fun getCurrencySymbol(): String = Currency.getInstance(Locale.getDefault()).symbol
}