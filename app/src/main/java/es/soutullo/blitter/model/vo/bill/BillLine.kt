package es.soutullo.blitter.model.vo.bill

import android.content.Context
import android.widget.TextView
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.person.Person
import java.io.Serializable

/** Represents a bill line, with its assigned persons if any */
data class BillLine(val id: Long?, val bill: Bill, val lineNumber: Int, val name: String, val price: Float, val persons: MutableList<Person> = mutableListOf()): Serializable {

    /**
     * Assigns a new person to the bill line. If the person already exists on any line of the bill, then
     * such person is assigned to the bill line. The line is assigned to the person too
     * @param person The person to assign
     */
    fun assignPerson(person: Person) {
        if (!this.persons.contains(person)) {
            val realPerson = this.bill.findPerson(person.name) ?: person

            realPerson.lines.add(this)
            this.persons.add(realPerson)
        }
    }

    /**
     * Unassigns a person from the bill line. The bill line is unassigned from the person too.
     * @param person The person to unassign
     */
    fun unassignPerson(person: Person) {
        this.persons.find{personIt -> personIt.name == person.name}?.let { realPerson ->
            realPerson.lines.remove(this)
            this.persons.remove(realPerson)
        }
    }

    /**
     * Assigns a set of persons to the bill line. If any person already exists on any line of the bill, then
     * such person is assigned to the bill line. The line is assigned to the person too
     * @param persons The persons to assign
     */
    fun assignAllPersons(persons: Collection<Person>) {
        persons.forEach { this.assignPerson(it) }
    }

    /**
     * Unassigns a set of persons from the bill line. The bill line is unassigned from the person too.
     * @param persons The set of persons to unassign
     */
    fun unassignAllPersons(persons: Collection<Person>) {
        persons.forEach { this.unassignPerson(it) }
    }

    /**
     * Returns the assigned people to this bill line as a string like "A, B" or "A, B, C and 2 more"
     * @param context The android context
     * @param textView The text view where the text will be placed. Required for measurements
     * @return The assigned people as string
     */
    fun getAssignedPeopleAsString(context: Context, textView: TextView) : String {
        val separator = context.getString(R.string.persons_array_separator)
        var result: String

        if (this.persons.isNotEmpty() && textView.width > 0) {
            result = this.persons.map { it.name }.reduce { acc, s ->  acc + separator + s}

            var i = 1
            while(textView.paint.measureText(result) > textView.width) {
                val morePersons = context.resources.getQuantityString(R.plurals.item_assignation_persons_more, i, i)

                result = when(i) {
                    this.persons.size -> context.resources.getQuantityString(R.plurals.item_assignation_persons_assigned_all_as_number, persons.size, persons.size)
                    else -> this.persons.map { it.name }.subList(0, persons.size - i).reduce { acc, s ->  acc + separator+ s } + morePersons
                }

                i++
            }
        } else {
            result = context.getString(R.string.item_assignation_persons_unassigned)
        }

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BillLine

        if (lineNumber != other.lineNumber) return false

        return true
    }

    override fun hashCode(): Int = lineNumber
}