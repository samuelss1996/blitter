package es.soutullo.blitter.model.vo.person

import es.soutullo.blitter.model.vo.bill.BillLine
import java.io.Serializable
import java.util.*

/** Represents a person, who may partially pay a bill */
data class Person(val id: Long?, val name: String, val lastDate: Date = Date(), val lines: MutableList<BillLine> = mutableListOf()): Serializable {

    /**
     * Calculates the amount of money this person has to pay, considering his assigned lines and the tip percent of the bill
     * @return The amount of money this person has to pay
     */
    fun getPayingAmountWithTip(): Float = this.getPayingAmountWithoutTip() * ((this.lines.firstOrNull()?.bill?.tipPercent ?: 0f) + 1)

    /** @return The tip percent of the bill this person is currently attached to */
    fun getTipPercent(): Float = this.lines.firstOrNull()?.bill?.tipPercent ?: 0f

    /**
     * Calculates the amount of money this person has to pay, considering his assigned lines. The tip is NOT considered
     * @return The amount of money this person has to pay
     */
    fun getPayingAmountWithoutTip(): Float = this.lines.map { line -> line.price / line.persons.size }.sum()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int = name.hashCode()
}