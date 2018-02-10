package es.soutullo.blitter.model.vo.person

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import es.soutullo.blitter.model.vo.bill.BillLine
import java.io.Serializable
import java.util.*

/** Represents a person, who may partially pay a bill */
data class Person(val id: Long?, val name: String, val lastDate: Date = Date(), val lines: MutableList<BillLine> = mutableListOf()): Serializable {
    companion object {
        @JvmStatic @BindingAdapter("app:srcCompat")
        fun setImageDrawable(imageView: ImageView, drawable: TextDrawable) {
            imageView.setImageDrawable(drawable)
        }
    }

    /**
     * Calculates the amount of money this person has to pay, considering his assigned lines, taxes and the tip percent of the bill
     * @return The amount of money this person has to pay
     */
    fun getPayingAmountWithTip() = this.getPayingAmountWithoutTip() * (this.getTipPercent() + 1)

    /** @return The tip percent of the bill this person is currently attached to */
    fun getTipPercent() = this.lines.firstOrNull()?.bill?.tipPercent ?: 0.0

    /**
     * Calculates the amount of money this person has to pay, considering his assigned lines and taxes. The tip is NOT considered
     * @return The amount of money this person has to pay
     */
    fun getPayingAmountWithoutTip() = this.lines.map { line -> line.price / line.persons.size }.sum() * (this.calculateTaxPercent() + 1)

    /** @return The user profile photo based on its name */
    fun generateUserProfilePhoto() : TextDrawable {
        val colorGenerator = ColorGenerator.MATERIAL
        val initials: String = with(this.name.split(" ")) {(this.firstOrNull()?.firstOrNull()?.toString() ?: "") + (this.getOrNull(1)?.firstOrNull() ?: "")}

        return TextDrawable.builder().buildRound(initials, colorGenerator.getColor(this.name))
    }

    private fun calculateTaxPercent() = this.lines.firstOrNull()?.bill?.calculateTaxPercent() ?: 0.0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int = name.hashCode()
}