package es.soutullo.blitter.model.vo.bill

import es.soutullo.blitter.model.vo.person.Person
import java.io.Serializable
import java.util.*

/** Represents a bill, with its lines and persons */
data class Bill(var id: Long?, var name: String, var date: Date, val source: EBillSource, var status: EBillStatus, var tax: Double = 0.0,
                val lines: MutableList<BillLine> = mutableListOf(), var subtotal: Double = 0.0, var tipPercent: Double = -1.0): Serializable {

    /**
     * Adds a bill line to the bill
     * @param line The bill line to be added
     */
    fun addLine(line: BillLine) {
        this.subtotal += line.price
        this.lines.add(line)
    }

    /**
     * Returns a bill line given its position
     * @param index The position of the line
     * @return The bill line
     */
    fun getLine(index: Int) = this.lines[index]

    /**
     * Finds a person across all the bill lines given its name
     * @param personName The name of the person to be found
     * @return The found person or null if there isn't any match
     */
    fun findPerson(personName: String): Person? = this.lines.flatMap { line -> line.persons }.find { person -> person.name == personName }

    fun calculateTaxPercent() = this.tax / this.subtotal

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bill

        if (id != other.id) return false

        return id != null
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}