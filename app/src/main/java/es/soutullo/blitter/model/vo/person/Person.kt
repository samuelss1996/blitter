package es.soutullo.blitter.model.vo.person

import android.content.Context
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.view.util.BlitterUtils
import java.util.*

/** Represents a person, who may partially pay a bill */
data class Person(val id: Long?, val name: String, val lastDate: Date, val lines: MutableList<BillLine> = mutableListOf()) {

    /**
     * Calculates the amount of money this person has to pay, considering his assigned lines and the tip percent of the bill
     * @return The amount of money this person has to pay
     */
    fun getPayingAmountWithTip(): Float = this.getPayingAmountWithoutTip() * ((this.lines.firstOrNull()?.bill?.tipPercent ?: 0f) + 1)

    /**
     * Calculates the amount of money this person has to pay, considering his assigned lines and the tip percent of the bill
     * and returns it as a string, properly formatted depending on whether or not the bill has any tip
     * @param context The Android context
     * @return The amount of money this person has to pay
     */
    fun getBeautifulPayingAmount(context: Context): String =
            BlitterUtils.getBeatifulPrice(context, this.getPayingAmountWithoutTip(), this.lines.firstOrNull()?.bill?.tipPercent ?: 0f)

    /**
     * Calculates the amount of money this person has to pay, considering his assigned lines. The tip is NOT considered
     * @return The amount of money this person has to pay
     */
    private fun getPayingAmountWithoutTip(): Float = this.lines.map { line -> line.price / line.persons.size }.sum()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int = name.hashCode()
}