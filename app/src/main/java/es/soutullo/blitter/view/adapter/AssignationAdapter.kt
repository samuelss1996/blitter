package es.soutullo.blitter.view.adapter

import android.content.Context
import android.widget.TextView
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.view.adapter.generic.ChoosableItemsAdapter
import es.soutullo.blitter.view.adapter.handler.IChoosableItemsListHandler

/** RecyclerView adapter for the bill lines on the assignation activity */
class AssignationAdapter(handler: IChoosableItemsListHandler) : ChoosableItemsAdapter<BillLine>(handler) {
    override fun onBindViewHolder(holder: GenericListViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val assignationsText = holder.itemView.findViewById<TextView>(R.id.bill_line_assignations_text)
        assignationsText.post({assignationsText.text = this.getAssignedPeopleAsString(this.items[position], assignationsText.context, assignationsText)})
    }

    /**
     * Returns the assigned people to this bill line as a string like "A, B" or "A, B, C and 2 more"
     * @param context The android context
     * @param textView The text view where the text will be placed. Required for measurements
     * @return The assigned people as string
     */
    private fun getAssignedPeopleAsString(billLine: BillLine, context: Context, textView: TextView) : String {
        val separator = context.getString(R.string.persons_array_separator)
        var result: String

        if (billLine.persons.isNotEmpty() && textView.width > 0) {
            result = billLine.persons.map { it.name }.reduce { acc, s ->  acc + separator + s}

            var i = 1
            while(textView.paint.measureText(result) > textView.width) {
                val morePersons = context.resources.getQuantityString(R.plurals.item_assignation_persons_more, i, i)

                result = when(i) {
                    billLine.persons.size -> context.resources.getQuantityString(R.plurals.item_assignation_persons_assigned_all_as_number, billLine.persons.size, billLine.persons.size)
                    else -> billLine.persons.map { it.name }.subList(0, billLine.persons.size - i).reduce { acc, s ->  acc + separator+ s } + morePersons
                }

                i++
            }
        } else {
            result = context.getString(R.string.item_assignation_persons_unassigned)
        }

        return result
    }

    override fun getItemLayout(): Int = R.layout.item_bill_line_assignation
}