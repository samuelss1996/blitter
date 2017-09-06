package es.soutullo.blitter.view.filter

import android.text.InputFilter
import android.text.Spanned

/** Filter for inputs which limits a numeric value between a minimum and a maximum */
class InputFilterMinMax(private val min: Int, private val max: Int) : InputFilter {

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        (dest.toString() + source.toString()).toIntOrNull()?.let { input ->
            if(this.isInRange(this.min, this.max, input)) {
                return null
            }
        }

        return ""
    }

    /** @return True if [value] is between [rangeA] and [rangeB] or vice versa */
    private fun isInRange(rangeA: Int, rangeB: Int, value: Int): Boolean = if (rangeB > rangeA) value in rangeA..rangeB else value in rangeB..rangeA
}