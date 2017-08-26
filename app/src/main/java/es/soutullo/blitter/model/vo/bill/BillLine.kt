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
        val realPerson = this.bill.findPerson(person.name) ?: person

        realPerson.lines.add(this)
        this.persons.add(realPerson)
    }

    /**
     * Unassigns a person to the bill line. The bill line is unassigned from the person too.
     * @param person The person to unassign
     */
    fun unassignPerson(person: Person) {
        this.persons.find{personIt -> personIt.name == person.name}?.let { realPerson ->
            realPerson.lines.remove(this)
            this.persons.remove(realPerson)
        }
    }

    /**
     * Returns the assigned people to this bill line as a string like "A, B" or "A, B, C and 2 more"
     * @param context The android context
     * @param textView The text view where the text will be placed. Required for measurements
     * @return The assigned people as string
     */
    fun getAssignedPeopleAsString(context: Context, textView: TextView) : String {
        var previousString = if(persons.isEmpty()) context.getString(R.string.item_assignation_persons_unassigned)
            else context.resources.getQuantityString(R.plurals.item_assignation_persons_assigned_all_as_number, persons.size, persons.size)

        for(i in 1..persons.size) {
            val separator = context.getString(R.string.persons_array_separator)
            var newString = persons.map { it.name }.subList(0, i).reduce { acc, s ->  acc + s + separator}.removeSuffix(separator)

            if(persons.size > i) {
                newString += context.resources.getQuantityString(R.plurals.item_assignation_persons_more, persons.size - 1, persons.size - 1)
            }

            if (textView.paint.measureText(newString) <= textView.width) {
                previousString = newString
            } else {
                break
            }
        }

        return previousString
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