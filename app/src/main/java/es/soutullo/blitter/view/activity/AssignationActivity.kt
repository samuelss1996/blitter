package es.soutullo.blitter.view.activity

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import es.soutullo.blitter.R
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.model.vo.person.Person
import es.soutullo.blitter.view.adapter.AssignationAdapter
import es.soutullo.blitter.view.adapter.handler.IChoosableItemsListHandler
import es.soutullo.blitter.view.dialog.AssignationDialog
import es.soutullo.blitter.view.dialog.PromptDialog
import es.soutullo.blitter.view.dialog.generic.CustomDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

class AssignationActivity : AppCompatActivity(), IChoosableItemsListHandler {
    private val assignationAdapter = AssignationAdapter(this)
    private var peopleAddedOnSession = mutableListOf<Person>()
    private var assignationDialog: AssignationDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_assignation)
        this.setSupportActionBar(this.findViewById(R.id.toolbar))

        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        this.init()
    }

    override fun onSupportNavigateUp(): Boolean {
        this.onBackPressed()
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        Handler().postDelayed({this.assignationAdapter.notifyDataSetChanged()}, 100)
    }

    fun onFinishButtonClicked() {
        // TODO implement here
    }

    fun onTipPercentageConfirmed(tipPercentage: Float) {
        // TODO implement here
    }

    fun onAssignClicked(billLinesToAssign: List<BillLine>) {
        // TODO implement here
    }

    fun onSelectAllClicked() {
        // TODO implement here
    }

    /** Gets called when the user presses the 'new person' button on the assignation dialog */
    fun onAddNewPersonClicked() {
        PromptDialog(this, this.createNewPersonDialogHandler(), this.getString(R.string.dialog_add_person_title),
                this.getString(R.string.generic_dialog_cancel), this.getString(R.string.dialog_add_person_positive_button),
                this.getString(R.string.dialog_add_person_field_hint)).show()
    }

    /**
     * Gets called when the user confirms he/she wants to add a new person, and its name
     * @param newPersonName The name of the new person
     */
    fun onNewPersonAdded(newPersonName: String) {
        val newPerson = Person(null, newPersonName)
        DaoFactory.getFactory(this).getPersonDao().insertRecentPerson(newPerson)

        this.peopleAddedOnSession.add(0, newPerson)
        this.peopleAddedOnSession = this.peopleAddedOnSession.distinct().toMutableList()

        this.assignationDialog?.updatePeopleList(this.peopleAddedOnSession + DaoFactory.getFactory(this)
                .getPersonDao().queryRecentPersons(10, this.peopleAddedOnSession))
    }

    /**
     * Gets called when the assignation for a bill line or a set of bill lines is done, i.e. when the user
     * clicks the 'OK' button on the assignation dialog
     * @param affectedBillLines The bill lines selected when the dialog was launched
     * @param assignedPersons The persons whose checkboxes are marked on the assignation dialog right before clicking the 'OK' button
     * @param unassignedPersons The persons whose checkboxes are unmarked on the assignation dialog right before clicking the 'OK' button
     */
    fun onAssignationDone(affectedBillLines: List<BillLine>, assignedPersons: List<Person>, unassignedPersons: List<Person>) {
        for (line in affectedBillLines) {
            line.assignAllPersons(assignedPersons)
            line.unassignAllPersons(unassignedPersons)
        }

        this.assignationAdapter.notifyDataSetChanged()
    }

    override fun onItemClicked(listIndex: Int, clickedViewId: Int) {
        this.assignationDialog = AssignationDialog(this, this.createAssignationDialogHandler(),
                listOf(this.assignationAdapter.get(listIndex)), this.peopleAddedOnSession + DaoFactory.getFactory(this).getPersonDao().queryRecentPersons(10, this.peopleAddedOnSession ))
        this.assignationDialog?.show()
    }

    override fun onChoiceModeStarted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChoiceModeFinished() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChosenItemsChanged() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun onTryToFinishWithMissingAssignation() {
        // TODO implement here
    }

    /** Initializes some fields of the activity */
    private fun init() {
        val bill = this.intent.getSerializableExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY) as Bill
        val assignationRecycler = this.findViewById<RecyclerView>(R.id.assignation_bill_lines)

        this.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener({ onFinishButtonClicked() })
        this.assignationAdapter.addAll(bill.lines)

        assignationRecycler.adapter = this.assignationAdapter
    }

    /** Creates the handler for the assignation dialog, which manages the click events on its buttons */
    private fun createAssignationDialogHandler(): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                dialog.dismiss()

                if(dialog is AssignationDialog) {
                    onAssignationDone(dialog.billLines, dialog.getAssignedPersons(), dialog.getUnassignedPersons())
                }
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) {
                dialog.dismiss()
            }

            override fun onNeutralButtonClicked(dialog: CustomDialog) {
                onAddNewPersonClicked()
            }
        }
    }

    /** Creates the handler for the new person dialog, which manages the click events on its buttons */
    private fun createNewPersonDialogHandler(): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                if(dialog is PromptDialog) {
                    onNewPersonAdded(dialog.getUserInput())
                }
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) { }
            override fun onNeutralButtonClicked(dialog: CustomDialog) { }
        }
    }
}
