package es.soutullo.blitter.model.vo.bill

import android.content.Context
import android.text.format.DateFormat
import es.soutullo.blitter.model.vo.person.Person
import es.soutullo.blitter.view.util.BlitterUtils
import java.util.*

/** Represents a bill, with its lines and persons */
data class Bill(var id: Long?, val name: String, val date: Date, val source: EBillSource, val status: EBillStatus,
                val lines: MutableList<BillLine> = mutableListOf(), val priceWithoutTip: Float = 0f, val tipPercent: Float = 0f) {

    /**
     * Returns a bill line given its position
     * @param index The position of the line
     * @return The bill line
     */
    fun getLine(index: Int) = this.lines[index]

    /**
     * Adds a bill line to the bill
     * @param line The bill line to be added
     * @return True if the bill line was added
     */
    fun addLine(line: BillLine) = this.lines.add(line)

    /**
     * Finds a person across all the bill lines given its name
     * @param personName The name of the person to be found
     * @return The found person or null if there isn't any match
     */
    fun findPerson(personName: String): Person? = this.lines.flatMap { line -> line.persons }.find { person -> person.name == personName }

    /**
     * Returns the date of the bill as a string, working with internationalization
     * @param context The Android context
     * @return The date as string
     */
    fun getBeautifulDate(context: Context): String = DateFormat.getLongDateFormat(context).format(this.date)

    /**
     * Returns the price of the bill as string, with the currency based on the device locale and well
     * formatted according to whether or not the bill includes a tip
     * @param context The Android context
     * @return The price as string
     */
    fun getBeautifulPrice(context: Context): String = BlitterUtils.getBeatifulPrice(context, this.priceWithoutTip, this.tipPercent)
}