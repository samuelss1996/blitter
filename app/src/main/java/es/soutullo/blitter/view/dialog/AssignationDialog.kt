package es.soutullo.blitter.view.dialog

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.DialogProductAssignationBinding
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.model.vo.person.Person
import es.soutullo.blitter.view.activity.AssignationActivity
import es.soutullo.blitter.view.adapter.generic.GenericListAdapter
import es.soutullo.blitter.view.dialog.data.AssignationDialogPerson
import es.soutullo.blitter.view.dialog.generic.CustomLayoutDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

/**
 * Dialog the user uses to assign persons to one or more products
 * @param assignationActivity The assignation activity reference
 * @param handler The handler, which manages the dialog buttons clicks
 * @param billLines The bill lines the user is assigning persons to
 * @param extraPeople Extra people to show on the dialog. People already assigned to any of the lines
 *        in [billLines] are implicitly shown on the dialog
 */
class AssignationDialog(private val assignationActivity: AssignationActivity, handler: IDialogHandler, val billLines: List<BillLine>,
        private val extraPeople: List<Person>) : CustomLayoutDialog(assignationActivity, handler, assignationActivity.resources
            .getQuantityString(R.plurals.assignation_dialog_title, billLines.size, billLines.firstOrNull()?.name, billLines.size)) {

    private val personsAdapter = AssignationDialogAdapter()
    private lateinit var binding: DialogProductAssignationBinding

    override fun getPositiveText(): String = this.context.getString(R.string.generic_dialog_positive_button)
    override fun getNegativeText(): String? = this.context.getString(R.string.generic_dialog_cancel)
    override fun getNeutralText(): String? = this.context.getString(R.string.assignation_dialog_neutral_button)
    override fun preventDismissOnButtonClicked(): Boolean = true

    override fun getCustomView(): View {
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(this.context), R.layout.dialog_product_assignation, null, false)

        this.updatePeopleList(this.extraPeople)

        this.binding.billLines = this.billLines
        this.binding.root.findViewById<RecyclerView>(R.id.product_assignation_people).adapter = this.personsAdapter

        return this.binding.root
    }

    /**
     * Updates the people list shown in the dialog
     * @param extraPeople Set of more people to include at the bottom of the list. The people assigned to the
     * bill lines are shown on top of the list, and don't need to be passed to this method
     */
    fun updatePeopleList(extraPeople: List<Person>) {
        val peopleGroups = billLines.map { it.persons }
        val people = peopleGroups.flatten().distinct().map { AssignationDialogPerson(this.assignationActivity, ObservableField(it.name)) }.toMutableList()

        for (person in people) {
            if(peopleGroups.all { it.contains(Person(null, person.name.get()!!)) }) {
                person.status = ObservableField(true)
                person.canBeIndeterminate = false
            }
        }

        people.addAll(extraPeople.map { AssignationDialogPerson(this.assignationActivity, ObservableField(it.name), ObservableField(false)) })

        this.personsAdapter.clear()
        this.personsAdapter.addAll(people.distinct())

        this.binding.persons = this.personsAdapter.items
    }

    /** @return The persons whose checkboxes are selected */
    fun getAssignedPersons(): List<Person> = this.personsAdapter.items.filter { it.status.get() == true }.map { Person(null, it.name.get()!!) }

    /** @return The persons whose checkboxes are unselected */
    fun getUnassignedPersons(): List<Person> = this.personsAdapter.items.filter { it.status.get() == false }.map { Person(null, it.name.get()!!) }

    /** RecyclerView adapter for the persons list on the assignation dialog */
    private inner class AssignationDialogAdapter : GenericListAdapter<AssignationDialogPerson>() {
        override fun getItemLayout(): Int = R.layout.item_assignation_dialog_person
        override fun showSeparators(): Boolean = false
    }
}