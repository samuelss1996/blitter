package es.soutullo.blitter.view.activity

import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (this.assignationAdapter.choosingModeEnabled) {
            this.menuInflater.inflate(R.menu.menu_app_bar_activity_assignation_choosing, menu)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> this.onSupportNavigateUp()
            R.id.action_assign -> {
                val selectedLines = this.assignationAdapter.items.filterIndexed {
                    index, _ -> this.assignationAdapter.selectedIndexes.contains(index)
                }

                this.onAssignClicked(selectedLines)
            }
        }

        return true
    }

    override fun onBackPressed() {
        if(this.assignationAdapter.choosingModeEnabled) {
            this.assignationAdapter.finishChoiceMode()
        } else {
            super.onBackPressed()
        }
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

        this.assignationAdapter.finishChoiceMode()
        this.assignationAdapter.notifyDataSetChanged()
    }

    override fun onItemClicked(listIndex: Int, clickedViewId: Int) {
        this.onAssignClicked(listOf(this.assignationAdapter.get(listIndex)))
    }

    override fun onChoiceModeStarted() {
        this.changeBarLayout(true)
    }

    override fun onChoiceModeFinished() {
        this.changeBarLayout(false)
    }

    override fun onChosenItemsChanged() {
        val allCheckbox = this.findViewById<CheckBox>(R.id.select_all_checkbox)
        val checkAll = (this.assignationAdapter.itemCount == this.assignationAdapter.selectedIndexes.size)

        this.findViewById<TextView>(R.id.selected_items_count_text).text = this.assignationAdapter.selectedIndexes.size.toString()

        allCheckbox.setOnCheckedChangeListener(null)
        this.findViewById<CheckBox>(R.id.select_all_checkbox).isChecked = checkAll
        allCheckbox.setOnCheckedChangeListener(this.createCheckAllListener())
    }

    private fun onTryToFinishWithMissingAssignation() {
        // TODO implement here
    }

    /**
     * Changes the bar layout and color depending on whether or not the activity is in choosing mode
     * @param choosingMode Indicates whether or not the activity is in choosing mode
     */
    private fun changeBarLayout(choosingMode: Boolean) {
        val appBarColorId = if(choosingMode) R.color.colorPrimaryLight else R.color.colorPrimary
        val statusBarColorId = if(choosingMode) R.color.colorPrimary else R.color.colorPrimaryDark

        this.invalidateOptionsMenu()
        this.supportActionBar?.setDisplayHomeAsUpEnabled(!choosingMode)
        this.findViewById<ViewGroup>(R.id.assignation_bar_content).visibility = if(choosingMode) View.VISIBLE else View.GONE

        this.supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,appBarColorId)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.window.statusBarColor = ContextCompat.getColor(this, statusBarColorId)
        }
    }

    /** Initializes some fields of the activity */
    private fun init() {
        val bill = this.intent.getSerializableExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY) as Bill
        val assignationRecycler = this.findViewById<RecyclerView>(R.id.assignation_bill_lines)

        this.findViewById<CheckBox>(R.id.select_all_checkbox).setOnCheckedChangeListener(this.createCheckAllListener())
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

    /** Creates the listener for the "select all" checkbox, displayed on the action bar in the choosing mode */
    private fun createCheckAllListener(): CompoundButton.OnCheckedChangeListener {
        return CompoundButton.OnCheckedChangeListener { _, checked ->
            if(checked) {
                this.assignationAdapter.selectAll()
            } else {
                this.assignationAdapter.deselectAll()
            }
        }
    }
}
