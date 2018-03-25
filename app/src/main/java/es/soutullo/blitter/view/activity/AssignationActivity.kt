package es.soutullo.blitter.view.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import es.soutullo.blitter.R
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.model.vo.bill.EBillStatus
import es.soutullo.blitter.model.vo.person.Person
import es.soutullo.blitter.view.adapter.AssignationAdapter
import es.soutullo.blitter.view.dialog.AssignationDialog
import es.soutullo.blitter.view.dialog.ConfirmationDialog
import es.soutullo.blitter.view.dialog.PromptDialog
import es.soutullo.blitter.view.dialog.TipDialog
import es.soutullo.blitter.view.dialog.generic.CustomDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

class AssignationActivity : ChoosingLayoutActivity() {
    override val itemsAdapter = AssignationAdapter(this)
    override val showHomeAsUp: Boolean = true

    private lateinit var bill: Bill
    private var peopleAddedOnSession = mutableListOf<Person>()
    private var assignationDialog: AssignationDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_assignation)
        this.setSupportActionBar(this.findViewById(R.id.toolbar))

        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.bill = this.intent.getSerializableExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY) as Bill

        this.findViewById<View>(R.id.assignation_root).post { this.itemsAdapter.notifyDataSetChanged() }

        if (this.bill.status != EBillStatus.COMPLETED) {
            this.doBackup()
        }

        this.prepareIdResult()
        this.init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (this.itemsAdapter.isChoosingModeEnabled()) {
            this.menuInflater.inflate(R.menu.menu_app_bar_activity_assignation_choosing, menu)
        } else {
            this.menuInflater.inflate(R.menu.menu_app_bar_activity_assignation, menu)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> this.onSupportNavigateUp()
            R.id.action_done -> this.onFinishButtonClicked()
            R.id.action_clear_assignations -> this.onClearAssignationsClicked()
            R.id.action_assign -> {
                val selectedLines = this.itemsAdapter.items.filterIndexed {
                    index, _ -> this.itemsAdapter.getSelectedIndexes().contains(index)
                }

                this.onAssignClicked(selectedLines)
            }
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        this.onBackPressed()
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        Handler().postDelayed({this.itemsAdapter.notifyDataSetChanged()}, 100)
    }

    override fun onItemClicked(listIndex: Int, clickedViewId: Int) {
        this.onAssignClicked(listOf(this.itemsAdapter.get(listIndex)))
    }

    /** Gets called when the finish button is clicked */
    private fun onFinishButtonClicked() {
        if (this.itemsAdapter.items.all { it.persons.isNotEmpty() }) {
            TipDialog(this, this.createTipDialogHandler(), this.bill).show()
        } else {
            this.onTryToFinishWithMissingAssignation()
        }
    }

    /**
     * Gets called when the user confirms the tip percent on the tip dialog
     * @param tipPercent The tip percent specified by the user
     */
    private fun onTipPercentageConfirmed(tipPercent: Double) {
        val intent = Intent(this, AdMobActivity::class.java)
        intent.putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, this.bill)

        this.bill.tipPercent = tipPercent
        this.startActivity(intent)
    }

    /**
     * Gets called when the assignation is triggered. This can be caused by a click on the action bar button
     * while selecting multiple items, or by a simple click on an item
     * @param billLinesToAssign The bill lines the user wants to assign (i.e. the selected bill lines or the clicked bill line)
     */
    private fun onAssignClicked(billLinesToAssign: List<BillLine>) {
        this.assignationDialog = AssignationDialog(this, this.createAssignationDialogHandler(),
                billLinesToAssign, this.peopleAddedOnSession + DaoFactory.getFactory(this)
                    .getPersonDao().queryRecentPersons(10, this.peopleAddedOnSession ))

        this.assignationDialog?.show()
    }

    /** Gets called when the user presses the 'new person' button on the assignation dialog */
    fun onAddNewPersonClicked() {
        PromptDialog(this, this.createNewPersonDialogHandler(), this.getString(R.string.dialog_add_person_title),
                this.getString(R.string.generic_dialog_cancel), this.getString(R.string.dialog_add_person_positive_button),
                this.getString(R.string.dialog_add_person_field_hint)).show()
    }

    /** Gets called when the user presses the "clear assignations" button on the app bar */
    private fun onClearAssignationsClicked() {
        val selectedLines = this.itemsAdapter.items.filterIndexed { index, _ -> this.itemsAdapter.getSelectedIndexes().contains(index) }
        val dialogTitle = this.getString(R.string.app_bar_button_clear_assignations)
        val dialogMessage = this.getString(R.string.dialog_clear_assignations_message)
        val positiveButtonText = this.getString(R.string.app_bar_button_clear_assignations)
        val negativeButtonText = this.getString(R.string.generic_dialog_cancel)

        ConfirmationDialog(this, this.createClearAssignationsDialogHandler(selectedLines), dialogTitle,
                dialogMessage, positiveButtonText, negativeButtonText).show()
    }

    /**
     * Gets called when the user confirms he/she wants to add a new person, and its name
     * @param newPersonName The name of the new person
     */
    private fun onNewPersonAdded(newPersonName: String) {
        val newPerson = Person(null, newPersonName)
        DaoFactory.getFactory(this).getPersonDao().insertRecentPerson(newPerson)

        this.peopleAddedOnSession.add(0, newPerson)
        this.peopleAddedOnSession = this.peopleAddedOnSession.distinct().toMutableList()

        this.assignationDialog?.updatePeopleList(this.peopleAddedOnSession + DaoFactory.getFactory(this)
                .getPersonDao().queryRecentPersons(10, this.peopleAddedOnSession))
    }

    /**
     * Gets called when the user clicks the delete button over a recent person in the assignation dialog
     * @param name The name of the person to be removed
     */
    fun onDeleteRecentPersonClicked(name: String) {
        val isAlreadyAssigned = this.bill.lines.flatMap { it.persons }.contains(Person(null, name))
        val dialogTitle = this.getString(R.string.dialog_delete_recent_person_title, name)
        val negativeButtonText = this.getString(R.string.generic_dialog_cancel)

        val positiveButtonText = when(isAlreadyAssigned) {
            true -> this.getString(R.string.dialog_delete_recent_person_positive_assigned)
            false -> this.getString(R.string.dialog_generic_delete_button)
        }

        val dialogMessage = when(isAlreadyAssigned) {
            true -> this.getString(R.string.dialog_delete_recent_person_message_assigned)
            false -> this.getString(R.string.dialog_delete_recent_person_message_not_assigned)
        }

        ConfirmationDialog(this, this.createDeleteRecentPersonDialogHandler(name), dialogTitle, dialogMessage,
                positiveButtonText, negativeButtonText).show()
    }

    /**
     * Gets called when the assignation for a bill line or a set of bill lines is done, i.e. when the user
     * clicks the 'OK' button on the assignation dialog
     * @param affectedBillLines The bill lines selected when the dialog was launched
     * @param assignedPersons The persons whose checkboxes are marked on the assignation dialog right before clicking the 'OK' button
     * @param unassignedPersons The persons whose checkboxes are unmarked on the assignation dialog right before clicking the 'OK' button
     */
    private fun onAssignationDone(affectedBillLines: List<BillLine>, assignedPersons: List<Person>, unassignedPersons: List<Person>) {
        val delay: Long = if(this.itemsAdapter.isChoosingModeEnabled()) 500 else 0

        for (line in affectedBillLines) {
            line.assignAllPersons(assignedPersons)
            line.unassignAllPersons(unassignedPersons)
        }

        assignedPersons.forEach { DaoFactory.getFactory(this).getPersonDao().insertRecentPerson(it) }

        this.itemsAdapter.finishChoiceMode()
        this.doBackup()

        Handler().postDelayed({this.itemsAdapter.notifyDataSetChanged()}, delay)

    }

    /**
     * Gets called when the user confirms he/she wants to delete a recent person
     * @param name The name of the person to be deleted
     */
    private fun deleteRecentPerson(name: String) {
        val person = Person(null, name)

        this.onAssignationDone(this.bill.lines, listOf(), listOf(person))
        DaoFactory.getFactory(this).getPersonDao().deleteRecentPerson(name)

        this.peopleAddedOnSession.remove(person)
        this.assignationDialog?.updatePeopleList(this.peopleAddedOnSession + DaoFactory.getFactory(this)
                .getPersonDao().queryRecentPersons(10, this.peopleAddedOnSession))
    }

    /** Gets called when the user tries to finish the assignations but there is yet one or more missing assignations */
    private fun onTryToFinishWithMissingAssignation() {
        Toast.makeText(this, this.getString(R.string.toast_assignation_must_assign_all), Toast.LENGTH_SHORT).show()
    }

    /** Saves the bill status and its assignations on the database */
    private fun doBackup() {
        this.bill.status = EBillStatus.ASSIGNING
        DaoFactory.getFactory(this).getBillDao().updateBill(this.bill.id, this.bill)
    }

    /** Initializes some fields of the activity */
    private fun init() {
        val assignationRecycler = this.findViewById<RecyclerView>(R.id.assignation_bill_lines)

        this.findViewById<CheckBox>(R.id.select_all_checkbox).setOnCheckedChangeListener(this.createCheckAllListener())

        this.itemsAdapter.addAll(this.bill.lines)
        assignationRecycler.adapter = this.itemsAdapter
    }

    /** Prepares to return the ID of the bill if the back button is pressed to prevent bill duplication */
    private fun prepareIdResult() {
        this.setResult(Activity.RESULT_OK, Intent().putExtra(BillSummaryActivity.INTENT_DATA_RETURNED_BILL_ID, this.bill.id))
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

    /** Creates the dialog handler for the assignation clear assignations dialog, which is a confirmation dialog */
    private fun createClearAssignationsDialogHandler(affectedBillLines: List<BillLine>): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                this@AssignationActivity.onAssignationDone(affectedBillLines, listOf(), affectedBillLines.map { it.persons }.flatten() )
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) { }
            override fun onNeutralButtonClicked(dialog: CustomDialog) { }
        }
    }

    /** Creates the dialog handler for the tip dialog */
    private fun createTipDialogHandler(): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                (dialog as? TipDialog)?.getTipPercent()?.let { this@AssignationActivity.onTipPercentageConfirmed(it) }
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) { }
            override fun onNeutralButtonClicked(dialog: CustomDialog) { }
        }
    }

    private fun createDeleteRecentPersonDialogHandler(personName: String): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                this@AssignationActivity.deleteRecentPerson(personName)
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) { }
            override fun onNeutralButtonClicked(dialog: CustomDialog) { }
        }
    }
}